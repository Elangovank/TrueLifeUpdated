package com.truelife.app.activity

import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.adapter.FriendsListAdapter
import com.truelife.app.listeners.TLClubEndlessRecyclerViewScrollListener
import com.truelife.app.model.FriendsList
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import kotlinx.android.synthetic.main.activity_add_block_list_member.*
import org.json.JSONObject

class AddBlockedList : BaseActivity(), ResponseListener, FriendsListAdapter.ClickListener {
    var user: User? = null
    var mSelectedUserId: String? = null
    var LIST_LOADING_DURATION = 1000
    var mAdapter: FriendsListAdapter? = null
    private var scrollListener: TLClubEndlessRecyclerViewScrollListener? = null
    var mFriendsList = arrayListOf<FriendsList>()
    var mMemberId: String = ""
    var mCurrentPage = 1
    var mTotalPage = 1
    var mSelectedPost = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_block_list_member)
        init()
        setRecylceview()
        loadValues(mCurrentPage)
        segmented_control.setSelectedSegment(0)
    }

    override fun clickListener() {
        close_club_button.setOnClickListener {
            finish()
        }

        segmented_control.addOnSegmentClickListener {

            when (it.sectionPosition) {
                0 -> {
                    friends_blockList_LAY.visibility = View.VISIBLE
                    contact_blockList_LAY.visibility = View.GONE
                }
                1 -> {
                    friends_blockList_LAY.visibility = View.GONE
                    contact_blockList_LAY.visibility = View.VISIBLE
                }
            }
        }


    }


    override fun init() {

        user = LocalStorageSP.getLoginUser(myContext)
        clickListener()
    }


    fun loadValues(aPageNo: Int) {

        AppDialogs.showProgressDialog(myContext)
        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL,
            getTotalFriendsFollowCaseString(aPageNo, "", "FriendsList")!!
        )
        AppServices.execute(
            myContext, this,
            result,
            Request.Method.POST,
            AppServices.API.friendList,
            FriendsList::class.java
        )
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.friendList.hashCode()) {
                if (r.response!!.isSuccess) {
                    var obj = r as FriendsList
                    mTotalPage = obj!!.total_page!!.toInt()
                    mFriendsList.addAll(obj.mData)
                    mAdapter!!.notifyDataSetChanged()
                    Log.e("Count", "" + obj.mData.size)
                }
            } else if (r.requestType!! == AppServices.API.blockOrUnblocklist.hashCode()) {
                if (r.response!!.isSuccess) {
                    mFriendsList!!.removeAt(mSelectedPost)
                    mAdapter!!.notifyDataSetChanged()
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                } else {
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                }
            }
        }
    }

    fun setRecylceview() {
        val layoutManager = LinearLayoutManager(myContext)
        friend_recycler.layoutManager = layoutManager
        friend_recycler.setHasFixedSize(true)
        mAdapter = FriendsListAdapter(myContext, mFriendsList, this)
        friend_recycler.adapter = mAdapter

        scrollListener =
            object : TLClubEndlessRecyclerViewScrollListener(layoutManager) {
                override fun onLoadMore(
                    page: Int,
                    totalItemsCount: Int,
                    view: RecyclerView
                ) { // Triggered only when new data needs to be appended to the list
// Add whatever code is needed to append new items to the bottom of the list
                    if (mTotalPage > page) {
                        val updateHandler = Handler()
                        val runnable = Runnable {
                            mCurrentPage++
                            loadValues(mCurrentPage)
                        }
                        updateHandler.postDelayed(runnable, LIST_LOADING_DURATION.toLong())
                    }
                }
            }
        friend_recycler!!.addOnScrollListener(scrollListener!!)

    }

    private fun getTotalFriendsFollowCaseString(
        aPage: Int,
        aUserId: String,
        aRequestType: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", user!!.mUserId)
            jsonParam1.put("login_user_id", user!!.mUserId)
            jsonParam1.put("page_no", aPage)
            val jsonParam = JSONObject()
            when (aRequestType) {
                "FollowersList" -> {
                    jsonParam.put("FollowersList", jsonParam1)
                    Log.e("FollowersList", " $jsonParam")
                }
                "FriendsList" -> {
                    jsonParam.put("FriendsList", jsonParam1)
                    Log.e("FriendsList", " $jsonParam")
                }
                "FollowingList" -> {
                    jsonParam.put("FollowingList", jsonParam1)
                    Log.e("FollowingList", " $jsonParam")
                }
            }
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    override fun onClick(tt: Any, position: Int) {
        mSelectedPost = position
        var selectedBlockedUser = tt as FriendsList

        AppDialogs.customDoubleAction(
            myContext,
            null,
            "Do you want to block this friend",
            "Ok", "Cancel",
            object : AppDialogs.OptionListener {
                override fun yes() {
                    mMemberId = selectedBlockedUser.id!!
                    blockUnblockAPI()
                }

                override fun no() {
                    AppDialogs.hidecustomView()
                }

            }, false, isOptionable = false
        )

    }

    override fun onLongClick(view: View?, position: Int) {

    }

    fun blockUnblockAPI() {
        AppDialogs.showProgressDialog(myContext)
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getBlockCaseString(user!!.mUserId!!)!!
            )
        AppServices.execute(
            myContext, this,
            result,
            Request.Method.POST,
            AppServices.API.blockOrUnblocklist,
            Response::class.java
        )
    }

    private fun getBlockCaseString(
        aUserId: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", aUserId)
            jsonParam1.put("friend_id", mMemberId)
            jsonParam1.put("block_status", "1")
            val jsonParam = JSONObject()
            jsonParam.put("BlockFriend", jsonParam1)
            Log.e("BlockFriend", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }
}

