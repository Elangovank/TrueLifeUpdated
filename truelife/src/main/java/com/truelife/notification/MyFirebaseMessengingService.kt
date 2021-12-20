package com.truelife.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.truelife.R
import com.truelife.app.activity.TLDashboardActivity
import com.truelife.app.activity.TLSplashActivity
import com.truelife.storage.LocalStorageSP
import com.truelife.storage.LocalStorageSP.getLoginUser
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Elango on 27-02-2020.
 */

class MyFirebaseMessengingService : FirebaseMessagingService() {

    lateinit var mIntent: Intent
    lateinit var mPendingIntent: PendingIntent


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("New Token", token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            if (remoteMessage.data != null) {
                Log.i("Firebase Message --->> ", remoteMessage.data.toString())
                if (!remoteMessage.data["message"].isNullOrEmpty() && !remoteMessage.data["notification_type"].isNullOrEmpty()) {
                    generateNotification(remoteMessage)
                    Log.i("Firebase Type --->> ", remoteMessage.data["notification_type"]!!)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateNotification(data: RemoteMessage) {
        try {

            // Push Notification action handling..
            LocalStorageSP.put(this, "push_notification", true)

            val user = getLoginUser(application)
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