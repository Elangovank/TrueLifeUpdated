package com.truelife.chat.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.truelife.R
import com.truelife.app.activity.TLDashboardActivity
import com.truelife.app.activity.TLSplashActivity
import com.truelife.chat.activities.calling.model.CallType
import com.truelife.chat.model.constants.DBConstants
import com.truelife.chat.model.constants.DownloadUploadStat
import com.truelife.chat.model.constants.FireCallDirection
import com.truelife.chat.model.constants.MessageType
import com.truelife.chat.model.realms.*
import com.truelife.chat.utils.*
import com.truelife.chat.utils.network.FireManager
import com.truelife.chat.utils.network.FireManager.Companion.isLoggedIn
import com.truelife.chat.utils.network.FireManager.Companion.uid
import com.truelife.chat.utils.update.UpdateChecker
import com.truelife.storage.LocalStorageSP
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.atomic.AtomicInteger

class MyFCMService : FirebaseMessagingService() {

    lateinit var mIntent: Intent
    lateinit var mPendingIntent: PendingIntent

    private var fireManager = FireManager()
    private var disposables = CompositeDisposable()
    private lateinit var newMessageHandler: NewMessageHandler

    private val updateChecker: UpdateChecker by lazy {
        UpdateChecker(this)
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        if (!isLoggedIn()) return  //if the user clears the app data or sign out we don't wan't to do nothing


        SharedPreferencesManager.setTokenSaved(false)
        ServiceHelper.saveToken(this, s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.containsKey("app_type")) {
            if (!remoteMessage.data["message"].isNullOrEmpty() && !remoteMessage.data["notification_type"].isNullOrEmpty()) {
                generateNotification(remoteMessage)
            }
        } else {
            if (FireManager.isLoggedIn()
                    .not()
            ) return  //if the user clears the app data or sign out we don't wan't to do nothing
            if (updateChecker.needsUpdate()) return

            val mainHandler = Handler(mainLooper)
            //run on main thread
            val myRunnable = Runnable {
                newMessageHandler = NewMessageHandler(this, fireManager, disposables)



                if (remoteMessage.data.containsKey("event")) {
                    //this will called when something is changed in group.
                    // like member removed,added,admin changed, group info changed...
                    when {
                        remoteMessage.data["event"] == "group_event" -> {
                            handleGroupEvent(remoteMessage)
                        }
                        remoteMessage.data["event"] == "new_group" -> {
                            handleNewGroup(remoteMessage)
                        }
                        remoteMessage.data["event"] == "message_deleted" -> {
                            handleDeletedMessage(remoteMessage)
                        }
                        remoteMessage.data["event"] == "new_call" -> {
                            handleNewCall(remoteMessage)
                        }
                    }
                } else {
                    if (remoteMessage.data.containsKey(DBConstants.MESSAGE_ID))
                        handleNewMessage(remoteMessage)
                }
            }
            mainHandler.post(myRunnable)
        }
    }

    private fun handleNewCall(map: RemoteMessage) {
        val data = map.data

        data["callId"]?.let { callId ->


            val fromId = data["callerId"] ?: ""

            val typeInt = data["callType"]?.toIntOrNull() ?: CallType.VOICE.value
            val type = CallType.fromInt(typeInt)


            val groupId = data["groupId"] ?: ""

            val isGroupCall = type.isGroupCall()

            if (!isGroupCall && uid.isEmpty()) return@let
            if (isGroupCall && groupId.isEmpty()) return@let
            val channel = data["channel"] ?: return@let

            val groupName = data["groupName"] ?: ""

            val timestamp = data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
            val phoneNumber = data["phoneNumber"] ?: ""

            val isVideo = type.isVideo()

            val uid = if (isGroupCall) groupId else fromId


            var user: User

            val storedUser = RealmHelper.getInstance().getUser(uid)

            if (storedUser == null) {
                //make dummy user temporarily
                user = User().apply {
                    if (isGroupCall) {
                        this.uid = groupId!!
                        this.isGroupBool = true
                        this.userName = groupName
                        this.group = Group().apply {
                            this.groupId = groupId
                            this.isActive = true
                            this.setUsers(mutableListOf(SharedPreferencesManager.getCurrentUser()))
                        }

                    } else {
                        this.uid = uid
                        this.phone = phoneNumber
                    }
                }
            } else {
                user = storedUser
            }

            val fireCall = FireCall(
                callId,
                user,
                FireCallDirection.INCOMING,
                timestamp,
                phoneNumber,
                isVideo,
                typeInt,
                channel
            )


            newMessageHandler.handleNewCall(fireCall)

        }

    }

    private fun handleNewMessage(remoteMessage: RemoteMessage) {
        val messageId = remoteMessage.data[DBConstants.MESSAGE_ID]

        //if message is deleted do not save it
        if (RealmHelper.getInstance().getDeletedMessage(messageId) != null) return


        val isGroup = remoteMessage.data.containsKey("isGroup")
        //getting data from fcm message and convert it to a message
        val phone = remoteMessage.data[DBConstants.PHONE] ?: ""
        val content = remoteMessage.data[DBConstants.CONTENT]
        val timestamp = remoteMessage.data[DBConstants.TIMESTAMP]
        val type = remoteMessage.data[DBConstants.TYPE]?.toInt() ?: 0
        //get sender uid
        val fromId = remoteMessage.data[DBConstants.FROM_ID]
        val toId = remoteMessage.data[DBConstants.TOID]
        val metadata = remoteMessage.data[DBConstants.METADATA]
        //convert sent type to received
        val convertedType = MessageType.convertSentToReceived(type)

        //if it's a group message and the message sender is the same
        if (fromId == uid) return

        //create the message
        val message = Message()
        message.content = content

        message.timestamp = timestamp

        message.fromId = fromId
        message.type = convertedType
        message.messageId = messageId
        message.metadata = metadata
        message.toId = toId
        val chatId = if (isGroup) toId else fromId
        message.chatId = chatId
        message.isGroup = isGroup
        if (isGroup) message.fromPhone = phone
        //set default state
        message.downloadUploadStat = DownloadUploadStat.FAILED


        //check if it's text message
        if (MessageType.isSentText(type)) {
            //set the state to default
            message.downloadUploadStat = DownloadUploadStat.DEFAULT


            //check if it's a contact
        } else if (remoteMessage.data.containsKey(DBConstants.CONTACT)) {
            message.downloadUploadStat = DownloadUploadStat.DEFAULT
            //get the json contact as String
            val jsonString = remoteMessage.data[DBConstants.CONTACT]
            //convert contact numbers from JSON to ArrayList
            val phoneNumbersList = JsonUtil.getPhoneNumbersList(jsonString)
            // convert it to RealmContact and set the contact name using content
            val realmContact = RealmContact(content, phoneNumbersList)
            message.contact = realmContact


            //check if it's a location message
        } else if (remoteMessage.data.containsKey(DBConstants.LOCATION)) {
            message.downloadUploadStat = DownloadUploadStat.DEFAULT
            //get the json location as String
            val jsonString = remoteMessage.data[DBConstants.LOCATION]
            //convert location from JSON to RealmLocation
            val location = JsonUtil.getRealmLocationFromJson(jsonString)
            message.location = location
        } else if (remoteMessage.data.containsKey(DBConstants.THUMB)) {
            val thumb = remoteMessage.data[DBConstants.THUMB]

            //Check if it's Video and set Video Duration
            if (remoteMessage.data.containsKey(DBConstants.MEDIADURATION)) {
                val mediaDuration = remoteMessage.data[DBConstants.MEDIADURATION]
                message.mediaDuration = mediaDuration
            }
            message.thumb = thumb


            //check if it's Voice Message or Audio File
        } else if (remoteMessage.data.containsKey(DBConstants.MEDIADURATION)
            && type == MessageType.SENT_VOICE_MESSAGE || type == MessageType.SENT_AUDIO
        ) {

            //set audio duration
            val mediaDuration = remoteMessage.data[DBConstants.MEDIADURATION]
            message.mediaDuration = mediaDuration

            //check if it's a File
        } else if (remoteMessage.data.containsKey(DBConstants.FILESIZE)) {
            val fileSize = remoteMessage.data[DBConstants.FILESIZE]
            message.fileSize = fileSize
        }

        //if the message was quoted save it and get the quoted message
        if (remoteMessage.data.containsKey("quotedMessageId")) {
            val quotedMessageId = remoteMessage.data["quotedMessageId"]
            //sometimes the message is not saved because of threads,
            //so we need to make sure that we refresh the database before checking if the message is exists
            RealmHelper.getInstance().refresh()
            val quotedMessage = RealmHelper.getInstance().getMessage(quotedMessageId, chatId)
            if (quotedMessage != null)
                message.quotedMessage = QuotedMessage.messageToQuotedMessage(quotedMessage)
        }

        //if the message was quoted save it and get the quoted message
        if (remoteMessage.data.containsKey("statusId")) {
            val statusId = remoteMessage.data["statusId"]
            //sometimes the message is not saved because of threads,
            //so we need to make sure that we refresh the database before checking if the message is exists
            RealmHelper.getInstance().refresh()
            val status = RealmHelper.getInstance().getStatus(statusId)
            if (status != null) {
                message.status = status
                val quotedMessage = Status.statusToMessage(status, fromId)
                quotedMessage?.fromId = uid
                quotedMessage?.chatId = fromId
                if (quotedMessage != null)
                    message.quotedMessage = QuotedMessage.messageToQuotedMessage(quotedMessage)


            }
            //Save it to database and fire notification
        }

        newMessageHandler.handleNewMessage(phone, message)


    }

    private fun handleDeletedMessage(remoteMessage: RemoteMessage) {
        newMessageHandler.handleDeletedMessage(remoteMessage.data)
    }

    private fun handleNewGroup(remoteMessage: RemoteMessage) {
        newMessageHandler.handleNewGroup(remoteMessage.data)
    }

    private fun handleGroupEvent(remoteMessage: RemoteMessage) {

        newMessageHandler.handleGroupEvent(remoteMessage.data)
    }


    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    private fun generateNotification(data: RemoteMessage) {
        try {

            // Push Notification action handling..
            LocalStorageSP.put(this, "push_notification", true)

            val user = LocalStorageSP.getLoginUser(application)
            var mChannelId = this.getString(R.string.app_name)
            val requestID = System.currentTimeMillis().toInt()
            var badge = 1

            // User Not Logged In
            if (user.mFullname.isNullOrEmpty()) {
                mIntent = Intent(this, TLSplashActivity::class.java)
                mPendingIntent = PendingIntent.getActivity(
                    this, requestID, mIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            // User Logged In
            else {
                mIntent = Intent(this, TLDashboardActivity::class.java)

                mIntent.putExtra("TrueLife", "TrueLife")
                mIntent.putExtra("message", data.data["message"])
                mIntent.putExtra("message_type", data.data["notification_type"])
                mIntent.putExtra("post_id", data.data["post_id"])
                mIntent.putExtra("feed_user_id", user.mUserId)
                mPendingIntent = PendingIntent.getActivity(
                    this, requestID, mIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            val mNotification: Notification
            val mNotificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            mChannelId = data.data["notification_type"]!!

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                val mNotificaionChannel = NotificationChannel(
                    mChannelId, getAppName(),
                    NotificationManager.IMPORTANCE_HIGH
                )

                mNotificaionChannel.description = data.data["message"]
                mNotificaionChannel.enableLights(true)
                mNotificaionChannel.lightColor = Color.BLUE
                mNotificaionChannel.setShowBadge(true)
                mNotificationManager.createNotificationChannel(mNotificaionChannel)

                if (data.data["badge"] != null && data.data["badge"]!!.isNotEmpty())
                    badge = data.data["badge"]!!.toInt()

                mNotification = NotificationCompat.Builder(this, mChannelId)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentTitle(getAppName())
                    .setStyle(NotificationCompat.BigTextStyle().bigText(data.data["message"]))
                    .setContentText(data.data["message"])
                    .setContentIntent(mPendingIntent)
                    .setAutoCancel(true)
                    .setNumber(badge)
                    .build()

                with(NotificationManagerCompat.from(this)) {
                    mNotificationManager.notify(getNotificationId(), mNotification)
                }

            } else {
                mNotification = Notification.Builder(this)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentTitle(data.data["message"])
                    .setStyle(Notification.BigTextStyle().bigText(data.data["body"]))
                    .setContentText(data.data["body"])
                    .setContentIntent(mPendingIntent)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .notification

                mNotification.flags = mNotification.flags or Notification.FLAG_AUTO_CANCEL
                mNotification.defaults = mNotification.defaults or Notification.DEFAULT_SOUND
                mNotificationManager.notify(getNotificationId(), mNotification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAppName(): String? {
        return this.getString(R.string.app_name)
    }

    private fun getNotificationId(): Int {
        val c = AtomicInteger(0)
        return c.incrementAndGet()
    }
}

