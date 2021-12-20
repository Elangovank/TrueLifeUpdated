package com.truelife.app.fragment.notification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.activity.feedpost.FeedDetailActivity
import com.truelife.app.fragment.notification.adapter.NotificationAdapter
import com.truelife.app.fragment.notification.model.NotificationModel
import com.truelife.app.model.User
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import org.json.JSONObject

class TLNotificationFragment : BaseFragment(), ResponseListener,
    NotificationAdapter.NotificationListener {

    companion object {
        var TAG: String = TLNotificationFragment::class.java.simpleName
    }

    private var mContext: Context? = null
    private var mFragmentManager: TLFragmentManager? = null
    var user: User? = null

    //view
    private var mRecycleView: RecyclerView? = null
    private var mSwipeRefresh: SwipeRefreshLayout? = null
    private var mAdapter: NotificationAdapter? = null
    private var mNotificationList: ArrayList<NotificationModel> = arrayListOf()
    private var mSelectedPosition: Int = -1

    fun setAdapter() {
        val layoutManager = LinearLayoutManager(mContext)
        mRecycleView!!.layoutManager = layoutManager
        mRecycleView!!.setHasFixedSize(true)
        mAdapter = NotificationAdapter(mContext!!, mNotificationList, this)
        mRecycleView!!.adapter = mAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView = inflater.inflate(R.layout.fragment_notification_list, container, false)
        init(mView!!)
        setAdapter()
        loadValues()
        return mView
    }


    override fun onBackPressed() {

    }

    override fun onResumeFragment() {

    }

    override fun init(view: View) {
        mContext = activity

        user = LocalStorageSP.getLoginUser(mContext)
        mFragmentManager = TLFragmentManager(activity!!)
        mRecycleView = view.findViewById(R.id.notification_recycle) as RecyclerView
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh) as SwipeRefreshLayout

        clickListener()
    }

    override fun initBundle() {

    }

    override fun clickListener() {
        mSwipeRefresh!!.setOnRefreshListener {
            loadValues()
        }
    }

    override fun onResponse(r: Response?) {
        if (mSwipeRefresh != null) {
            if (mSwipeRefresh!!.isRefreshing) {
                mSwipeRefresh!!.isRefreshing = false
            }
        }
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.notificationlist.hashCode()) {
                if (r.response!!.isSuccess) {
                    mNotificationList.clear()
                    val ss = r as NotificationModel
                    val acceptRequestInfoList: ArrayList<NotificationModel.AcceptRequestInfo>? =
                        ss.acceptRequestInfo
                    val invitesNotificationInfoList: ArrayList<NotificationModel.InvitesNotificationInfo>? =
                        ss.invitesNotificationInfo
                    val clubAcceptInfoList: ArrayList<NotificationModel.ClubAcceptInfo>? =
                        ss.clubAcceptInfo

                    val clubRequestInfoList: ArrayList<NotificationModel.ClubRequestInfo>? =
                        ss.clubRequestInfo

                    val friendRequestInfoList: ArrayList<NotificationModel.FriendRequestInfo>? =
                        ss.friendRequestInfo

                    for (i in clubRequestInfoList!!.indices) {
                        var aData = clubRequestInfoList.get(i)
                        var aNotificationModel = NotificationModel()
                        aNotificationModel.notificationText = aData.notification
                        aNotificationModel.type = "clubs"
                        aNotificationModel.id = aData.id
                        aNotificationModel.userId = aData.user_id
                        aNotificationModel.created = aData.created
                        aNotificationModel.userImage = aData.userImage
                        aNotificationModel.clubId = aData.clubId
                        mNotificationList.add(aNotificationModel)
                    }


                    for (i in friendRequestInfoList!!.indices) {
                        var aData = friendRequestInfoList.get(i)
                        var aNotificationModel = NotificationModel()
                        aNotificationModel.notificationText = aData.notification
                        aNotificationModel.type = "friends"
                        aNotificationModel.id = aData.id
                        aNotificationModel.userId = aData.user_id
                        aNotificationModel.created = aData.created
                        aNotificationModel.userImage = aData.userImage
                        mNotificationList.add(aNotificationModel)
                    }

                    for (i in invitesNotificationInfoList!!.indices) {
                        var aData = invitesNotificationInfoList.get(i)
                        var aNotificationModel = NotificationModel()
                        aNotificationModel.notificationText = aData.notification
                        aNotificationModel.type = aData.type
                        aNotificationModel.invitationId = aData.inviteId
                        aNotificationModel.userId = aData.user_id
                        aNotificationModel.created = aData.created
                        aNotificationModel.userImage = aData.userImage
                        aNotificationModel.clubId = aData.clubId
                        aNotificationModel.clubName = aData.clubName
                        aNotificationModel.clubAdmin = aData.clubAdmin
                        mNotificationList.add(aNotificationModel)
                    }
                    for (i in acceptRequestInfoList!!.indices) {
                        var aData = acceptRequestInfoList.get(i)
                        var aNotificationModel = NotificationModel()
                        aNotificationModel.notificationText = aData.notification
                        aNotificationModel.type = "accept_friends"
                        aNotificationModel.id = aData.id
                        aNotificationModel.userId = aData.user_id
                        aNotificationModel.created = aData.created
                        aNotificationModel.userImage = aData.userImage
                        mNotificationList.add(aNotificationModel)
                    }
                    for (i in clubAcceptInfoList!!.indices) {
                        var aData = clubAcceptInfoList.get(i)
                        var aNotificationModel = NotificationModel()
                        aNotificationModel.notificationText = aData.notification
                        aNotificationModel.type = aData.type
                        aNotificationModel.id = aData.id
                        aNotificationModel.userId = aData.user_id
                        aNotificationModel.created = aData.created
                        aNotificationModel.userImage = aData.userImage
                        aNotificationModel.clubId = aData.clubId
                        mNotificationList.add(aNotificationModel)
                    }

                    mNotificationList.addAll(ss.notification!!)
                    updateAdapter()
                }
            } else if (r.requestType!! == AppServices.API.addfriend.hashCode()) {
                if (r.response!!.isSuccess) {

                    mNotificationList.removeAt(mSelectedPosition)
                    updateAdapter()
                }

            } else if (r.requestType!! == AppServices.API.clubinvite.hashCode()) {
                if (r.response!!.isSuccess) {
                    mNotificationList.removeAt(mSelectedPosition)
                    updateAdapter()
                    AppDialogs.customSuccessAction(
                        mContext!!,
                        null,
                        "Club Invitation accepted",
                        null,
                        null,
                        false
                    )
                    // AppDialogs.showToastDialog(mContext!!, "club Sucess")
                }

            }
        }
    }

    fun loadValues() {

        AppDialogs.showProgressDialog(mContext!!)
        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL,
            getNotificationDetailsCaseString(user!!.mUserId!!)!!
        )
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.notificationlist,
            NotificationModel::class.java
        )
    }

    private fun getNotificationDetailsCaseString(aUserId: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", aUserId)
            val jsonParam = JSONObject()
            jsonParam.put("NotificationList", jsonParam1)
            Log.e("NotificationList", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    fun updateAdapter() {
        mAdapter!!.notifyDataSetChanged()
    }


    fun AddFriendsAPI(
        aProfileUserId: String,
        aStatus: String,
        aRequestName: String
    ) {

        AppDialogs.showProgressDialog(mContext!!)
        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL,
            AddFriendandAddFolllowCaseString(
                user!!.mUserId!!,
                aProfileUserId,
                aStatus,
                aRequestName
            )!!
        )
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.addfriend,
            Response::class.java
        )
    }

    private fun AddFriendandAddFolllowCaseString(
        aUserId: String,
        aProfileUserId: String,
        aStatus: String,
        aRequestName: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            aCaseStr = if (aRequestName == "AddFriends") {
                jsonParam1.put("user_id", aUserId)
                jsonParam1.put("friend_id", aProfileUserId)
                jsonParam1.put("is_accepted", aStatus)
                val jsonParam = JSONObject()
                jsonParam.put("FriendRequest", jsonParam1)
                Log.e("FriendRequest", " $jsonParam")
                Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            } else if (aRequestName == "AcceptClubRequest") {
                jsonParam1.put("login_user_id", aUserId)
                jsonParam1.put("request_id", aProfileUserId)
                jsonParam1.put("club_id", "")
                jsonParam1.put("status", aStatus)
                val jsonParam = JSONObject()
                jsonParam.put("AcceptClubRequest", jsonParam1)
                Log.e("AcceptClubRequest", " $jsonParam")
                Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            } else {
                jsonParam1.put("user_id", aUserId)
                jsonParam1.put("follows_id", aProfileUserId)
                jsonParam1.put("follow_status", aStatus)
                val jsonParam = JSONObject()
                jsonParam.put("FollowUnfollow", jsonParam1)
                Log.e("FollowUnfollow", " $jsonParam")
                Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun addFriend(
        position: Int,
        aData: NotificationModel,
        aStatus: String,
        aRequestName: String
    ) {
        mSelectedPosition = position
        if (aRequestName == "AddFriends")
            AddFriendsAPI(aData.userId!!, aStatus, aRequestName)
        else AddFriendsAPI(aData.id!!, aStatus, aRequestName)
    }

    override fun inviteClub(
        position: Int,
        aData: NotificationModel,
        aStatus: String,
        aRequestType: String
    ) {
        mSelectedPosition = position
        clubInviteAPI(aStatus, aData.userId!!, aData.clubId!!, aRequestType, aData.invitationId!!)
    }

    override fun likeComment(
        position: Int,
        aData: NotificationModel,
        aStatus: String,
        aRequestType: String
    ) {
        val intent = Intent(mContext, FeedDetailActivity::class.java)
        intent.putExtra("post_id", aData.postId)
        startActivity(intent)
    }


    fun clubInviteAPI(
        aStatus: String,
        aProfileUserId: String,
        aClubId: String,
        aRequestType: String,
        aInviteId: String
    ) {

        AppDialogs.showProgressDialog(mContext!!)
        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL,
            AddClubInviteAcceptCaseString(
                aStatus,
                aProfileUserId,
                aClubId,
                aInviteId,
                aRequestType
            )!!
        )
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.clubinvite,
            Response::class.java
        )
    }

    private fun AddClubInviteAcceptCaseString(
        aStstus: String,
        aTouserId: String,
        aClubid: String,
        aRequestType: String,
        InviteId: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("status", aStstus)
            jsonParam1.put("to_user_id", aTouserId)
            jsonParam1.put("club_id", aClubid)
            jsonParam1.put("request_type", aRequestType)
            jsonParam1.put("login_user_id", user!!.mUserId!!)
            jsonParam1.put("invite_id", InviteId)
            val jsonParam = JSONObject()
            jsonParam.put("InvitesAccept", jsonParam1)
            Log.e("InvitesAccept", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }
}