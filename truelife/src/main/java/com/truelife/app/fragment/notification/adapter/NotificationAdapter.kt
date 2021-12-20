package com.truelife.app.fragment.notification.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.fragment.notification.model.NotificationModel
import com.truelife.util.DateUtil
import com.truelife.util.Utility

/**
 * Created by Elango on 18-12-2019.
 */
class NotificationAdapter(
    private val mContext: Context,
    var mUList: List<NotificationModel>, val listener: NotificationListener
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.inflate_notification_item, parent, false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        var data = getInfo(position)
        //        aHolder.mNotificationText.setText(data.notificationText)

        loadData(data, aHolder, position)
    }


    fun loadData(aValue: NotificationModel, aHolder: ViewHolder, aPos: Int) {

        if (aValue.type.equals("friends", true)) {

            aHolder.mFriendsRequestLay.setVisibility(View.VISIBLE)
            aHolder.mDataLayout.setVisibility(View.GONE)
            aHolder.mClubLay.setVisibility(View.GONE)
            aHolder.mFriendsNameTxt.setText(aValue.notificationText)

            if (!aValue.userImage.isNullOrEmpty()) {
                Utility.loadImageNotification(aValue.userImage!!, aHolder.mFriendImage)
            } else {
                aHolder.mFriendImage.setImageResource(R.drawable.notification_placeholder)
            }

            aHolder.mFriendAcceptBtn.setOnClickListener {
                listener.addFriend(aPos, aValue, "1", "AddFriends")
            }

            aHolder.mFriendDeleteBtn.setOnClickListener {
                listener.addFriend(aPos, aValue, "3", "AddFriends")
            }

        } else if (aValue.type.equals("clubs", true)) {

            aHolder.mFriendsRequestLay.setVisibility(View.GONE)
            aHolder.mDataLayout.setVisibility(View.GONE)
            aHolder.mClubLay.setVisibility(View.VISIBLE)

            aHolder.mClubNameTxt.setText(aValue.notificationText)
            if (!aValue.userImage.isNullOrEmpty()) {
                Utility.loadImageNotification(aValue.userImage!!, aHolder.mClubImage)
            } else {
                aHolder.mClubImage.setImageResource(R.drawable.notification_placeholder)
            }

            aHolder.mClubAcceptBtn.setOnClickListener {
                listener.addFriend(aPos, aValue, "1", "AcceptClubRequest")
            }

            aHolder.mClubDeleteBtn.setOnClickListener {
                listener.addFriend(aPos, aValue, "3", "AcceptClubRequest")
            }
        } else if (aValue.type.equals("invites", true)) {

            aHolder.mFriendsRequestLay.setVisibility(View.GONE)
            aHolder.mDataLayout.setVisibility(View.GONE)
            aHolder.mClubLay.setVisibility(View.VISIBLE)

            aHolder.mClubNameTxt.setText(aValue.notificationText)
            if (!aValue.userImage.isNullOrEmpty()) {
                Utility.loadImageNotification(aValue.userImage!!, aHolder.mClubImage)
            } else {
                aHolder.mClubImage.setImageResource(R.drawable.notification_placeholder)
            }
            aHolder.mClubAcceptBtn.setOnClickListener {
                listener.inviteClub(aPos, aValue, "1", "2")
            }

            aHolder.mClubDeleteBtn.setOnClickListener {
                listener.inviteClub(aPos, aValue, "0", "2")
            }

        } else if (aValue.type.equals("club_request", true)) {
            loadShareTimeData(aValue.created, aHolder)
            aHolder.mFriendsRequestLay.setVisibility(View.GONE)
            aHolder.mDataLayout.setVisibility(View.VISIBLE)
            aHolder.mClubLay.setVisibility(View.GONE)

            aHolder.mNotificationText.setText(aValue.notificationText)

            if (!aValue.userImage.isNullOrEmpty()) {
                Utility.loadImageNotification(aValue.userImage!!, aHolder.mProfileImage)
            } else {
                aHolder.mProfileImage.setImageResource(R.drawable.notification_placeholder)
            }

        } else if (aValue.type.equals("accept_friends", true)) {
            aHolder.mFriendsRequestLay.setVisibility(View.GONE)
            aHolder.mDataLayout.setVisibility(View.VISIBLE)
            aHolder.mClubLay.setVisibility(View.GONE)
            aHolder.mNotificationText.setText(aValue.notificationText)
            if (!aValue.userImage.isNullOrEmpty()) {
                Utility.loadImageNotification(aValue.userImage!!, aHolder.mProfileImage)
            } else {
                aHolder.mProfileImage.setImageResource(R.drawable.notification_placeholder)
            }
            loadShareTimeData(aValue.created, aHolder)

        } else if (aValue.type.equals("like", true) || aValue.type.equals("comment", true)) {

            loadShareTimeData(aValue.created, aHolder)
            aHolder.mFriendsRequestLay.setVisibility(View.GONE)
            aHolder.mDataLayout.setVisibility(View.VISIBLE)
            aHolder.mClubLay.setVisibility(View.GONE)

            aHolder.mNotificationText.setText(aValue.notificationText)

            if (!aValue.userImage.isNullOrEmpty()) {
                Utility.loadImageNotification(aValue.userImage!!, aHolder.mProfileImage)
            } else {
                aHolder.mProfileImage.setImageResource(R.drawable.notification_placeholder)
            }
            aHolder.mProfileImage.setOnClickListener {

                listener.likeComment(aPos, aValue, "", "like")
            }
        }
    }

    fun getInfo(position: Int): NotificationModel {
        return mUList[position]
    }

    override fun getItemCount(): Int {
        return mUList.size
    }

    fun loadShareTimeData(
        aaDate: String?,
        holder: ViewHolder
    ) {
        val output = DateUtil.getTLNotificationformat(aaDate!!)
        try {
            holder.mDateTxt.setText(output)
        } catch (e: Exception) { // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    /**
     * interface onclick
     */
    interface ClickListener {
        fun onClick(tt: Any, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {

        val mNotificationText: TextView
        val mDateTxt: TextView
        val mFriendsNameTxt: TextView
        val mClubNameTxt: TextView
        val mFriendAcceptBtn: Button
        val mFriendDeleteBtn: Button

        val mClubAcceptBtn: Button
        val mClubDeleteBtn: Button

        val mFriendImage: ImageView
        val mProfileImage: ImageView
        val mClubImage: ImageView
        val mDataLayout: LinearLayout
        val mFriendsRequestLay: LinearLayout
        val mClubLay: LinearLayout


        init {
            mNotificationText = aView.findViewById(R.id.inflate_menu_list_name)
            mDataLayout = aView.findViewById(R.id.inflate_notification_like_list_item_like_LAY)
            mFriendsRequestLay =
                aView.findViewById(R.id.inflate_notification_like_list_item_Friends_LAY)
            mClubLay = aView.findViewById(R.id.inflate_notification_like_list_item_Club_LAY)
            mProfileImage = aView.findViewById(R.id.profile_image)
            mFriendImage = aView.findViewById(R.id.inflate_notification_Friends_LAY_Profile_IMG)
            mClubImage = aView.findViewById(R.id.inflate_notification_Club_LAY_Profile_IMG)
            mFriendsNameTxt = aView.findViewById(R.id.inflate_notification_Friends_LAY_Profile_name)
            mClubNameTxt = aView.findViewById(R.id.inflate_notification_Club_LAY_Profile_name)
            mFriendAcceptBtn = aView.findViewById(R.id.inflate_notification_Friends_LAY_accept_Btn)
            mFriendDeleteBtn = aView.findViewById(R.id.inflate_notification_Friends_LAY_delete_Btn)

            mClubAcceptBtn = aView.findViewById(R.id.inflate_notification_Club_LAY_accept_Btn)
            mClubDeleteBtn = aView.findViewById(R.id.inflate_notification_Club_LAY_delete_Btn)

            mDateTxt =
                aView.findViewById(R.id.inflate_notification_like_list_item_notification_time_TXT)

        }
    }

    fun updateAdapter(
        aUList: List<NotificationModel>
    ) {
        mUList = aUList
    }


    interface NotificationListener {
        fun addFriend(
            position: Int,
            aData: NotificationModel,
            aStatus: String,
            aRequestName: String
        )


        fun inviteClub(
            position: Int,
            aData: NotificationModel,
            aStatus: String,
            aRequestType: String
        )

        fun likeComment(
            position: Int,
            aData: NotificationModel,
            aStatus: String,
            aRequestType: String
        )
    }

}