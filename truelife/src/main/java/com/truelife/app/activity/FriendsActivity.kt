package com.truelife.app.activity

import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.adapter.YourFriendsListAdapter
import com.truelife.app.listeners.TLClubEndlessRecyclerViewScrollListener
import com.truelife.app.model.User
import com.truelife.app.model.UserList
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import org.json.JSONObject

class FriendsActivity : BaseActivity(), ResponseListener,
    YourFriendsListAdapter.ClickListener {
    var LIST_LOADING_DURATION = 1000
    lateinit var mContext: FragmentActivity
    var mSelectedPos = -1
    var mTotalPage = 1
    var Currentpage = 1

    private var scrollListener: TLClubEndlessRecyclerViewScrollListener? = null
    var user: User? = null
    var mUserList = ArrayList<UserList.UList>()
    lateinit var mRecycleView: RecyclerView
    var mToolbarTitle: TextView? = null
    var mNoDataFound: TextView? = null
    //var mFollowersCountTxt: TextView? = null
    var mBackBtn: ImageView? = null

    var mAdapter: YourFriendsListAdapter? = null

    var mUserId = ""
    var mScreenType = ""
    var mRequestType = ""
    var isFriend = ""
    var isFollow = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_friends)
        init()

        getBundle()
        getFollowersList()
    }

    fun getBundle() {
        try {
            mUserId = if (intent.hasExtra("userId"))
                intent.getStringExtra("userId")!!
            else user?.mUserId!!

            if (intent != null) {
                mScreenType = intent.extras!!.getString("from")!!
                mToolbarTitle!!.text = intent.extras!!.getString("title")!!
            }
            setRecylceview()
        } catch (e: java.lang.Exception) {
        }
    }

    override fun clickListener() {
        mBackBtn!!.setOnClickListener {
            finish()
        }
    }

    override fun init() {

        mContext = this
        mRecycleView = findViewById<RecyclerView>(R.id.fragment_horizontal_friend_recycler)
        mBackBtn = findViewById<ImageView>(R.id.back_icon)
        mToolbarTitle = findViewById<TextView>(R.id.chat_title)
        mNoDataFound = findViewById<TextView>(R.id.no_block_friends_found)
        //   mFollowersCountTxt = findViewById(R.id.followers_count) as TextView
        //  mToolbarTitle!!.typeface = ResourcesCompat.getFont(mContext!!, R.font.calling_heart)
        user = LocalStorageSP.getLoginUser(mContext)

        clickListener()
    }

    fun getFollowersList() {

        AppDialogs.showProgressDialog(mContext)

        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getFollowersListCaseString(mUserId, Currentpage, mScreenType)!!
            )
        AppServices.execute(
            mContext, this,
            result,
            Request.Method.POST,
            AppServices.API.followerslist,
            UserList::class.java
        )
    }

    private fun getFollowersListCaseString(
        aUserId: String, aPage: Int, aRequestType: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("login_user_id", aUserId)
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

    override fun onResponse(r: Response?) {

        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.followerslist.hashCode()) {
                if (r.response!!.isSuccess) {
                    val mUser: UserList? = r as UserList

                    when (mScreenType) {
                        "FollowersList" -> mUserList.addAll(mUser!!.followers_list!!)
                        "FriendsList" -> mUserList.addAll(mUser!!.friends_list!!)
                        "FollowingList" -> mUserList.addAll(mUser!!.following_list!!)
                    }

                    mTotalPage = mUser!!.totalPage?.toInt()!!
                    mAdapter!!.notifyDataSetChanged()
                } else {
                    if (mUserList.isNullOrEmpty() || mUserList.size == 0) {
                        mNoDataFound!!.visibility = View.VISIBLE
                        mRecycleView.visibility = View.GONE
                    } else {
                        mRecycleView.visibility = View.VISIBLE
                        mNoDataFound!!.visibility = View.GONE
                    }
                }
            } else if (r.requestType!! == AppServices.API.addfriend.hashCode()) {
                if (r.response!!.isSuccess) {
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                    mUserList[mSelectedPos].is_friend =
                        if (isFriend == "3") "1" else (if (isFriend == "0") "2" else "0")
                    mAdapter!!.notifyDataSetChanged()
                } else AppDialogs.showToastDialog(mContext, r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.AddFollow.hashCode()) {
                if (r.response!!.isSuccess) {
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                    mUserList[mSelectedPos].is_follow = if (isFollow == "0") "1" else "0"
                    mAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    fun setRecylceview() {

        val layoutManager = LinearLayoutManager(mContext)
        mRecycleView.layoutManager = layoutManager
        mRecycleView.setHasFixedSize(true)
        mAdapter = YourFriendsListAdapter(mContext, mUserList, mScreenType, this)
        mRecycleView.adapter = mAdapter

        scrollListener = object : TLClubEndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(
                page: Int,
                totalItemsCount: Int,
                view: RecyclerView
            ) { // Triggered only when new data needs to be appended to the list
// Add whatever code is needed to append new items to the bottom of the list
                if (mTotalPage > page) {
                    Currentpage++
                    val updateHandler = Handler()
                    val runnable = Runnable {
                        System.gc()
                        getFollowersList()
                    }
                    updateHandler.postDelayed(runnable, LIST_LOADING_DURATION.toLong())
                }
            }
        }
        mRecycleView.addOnScrollListener(scrollListener!!)
    }

    override fun onClick(tt: UserList.UList, position: Int) {

        if (!checkInternet())
            return

        mSelectedPos = position


        when (mScreenType) {
            "FollowersList" -> {
                followAction(tt)
            }

            "FriendsList" -> {

                mRequestType = AppServices.API.addfriend
                isFriend = tt.is_friend!!

                when (isFriend) {
                    "0" -> {
                        addFriend(tt.id!!)
                    }
                    "1" -> {
                        AppDialogs.customDoubleAction(
                            myContext,
                            null,
                            "Do you want to unfriend ?",
                            "Yes", "No",
                            object : AppDialogs.OptionListener {
                                override fun yes() {
                                    AddFriendsAPI(tt.id!!, "2", mRequestType)
                                }

                                override fun no() {
                                    AppDialogs.hidecustomView()
                                }

                            }, false, isOptionable = false
                        )

                    }
                    "2" -> {
                        // cancel friend request

                        AppDialogs.customDoubleAction(
                            myContext,
                            null,
                            "Do you want to cancel your friend request?",
                            "Yes", "No",
                            object : AppDialogs.OptionListener {
                                override fun yes() {
                                    AddFriendsAPI(tt.id!!, "3", mRequestType)
                                }

                                override fun no() {
                                    AppDialogs.hidecustomView()
                                }

                            }, false, isOptionable = false
                        )
                    }

                    "3" -> {

                        AppDialogs.customDoubleAction(
                            myContext,
                            null,
                            "Are you confirm the friend request ?",
                            "Yes", "No",
                            object : AppDialogs.OptionListener {
                                override fun yes() {
                                    AddFriendsAPI(tt.id!!, "1", mRequestType)
                                }

                                override fun no() {
                                    AppDialogs.hidecustomView()
                                }

                            }, false, isOptionable = false
                        )
                    }
                }
            }
            "FollowingList" -> {
                followAction(tt)
            }
        }
    }

    private fun followAction(tt: UserList.UList) {
        mRequestType = AppServices.API.AddFollow
        isFollow = tt.is_follow!!

        when (isFollow) {
            "0" -> {
                AddFriendsAPI(tt.id!!, "1", mRequestType)
            }
            "1" -> {
                AppDialogs.customDoubleAction(
                    myContext,
                    null,
                    "Do you want to unfollow ?",
                    "Yes", "No",
                    object : AppDialogs.OptionListener {
                        override fun yes() {
                            AddFriendsAPI(tt.id!!, "2", mRequestType)
                        }

                        override fun no() {
                            AppDialogs.hidecustomView()
                        }

                    }, false, isOptionable = false
                )
            }
        }
    }

    private fun addFriend(id: String) {
        AddFriendsAPI(id, "0", mRequestType)
    }

    override fun onLongClick(view: View?, position: Int) {

    }

    fun AddFriendsAPI(
        aProfileUserId: String,
        aStatus: String,
        aRequestName: String
    ) {

        AppDialogs.showProgressDialog(mContext)
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
            mContext, this,
            result,
            Request.Method.POST,
            aRequestName,
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
            aCaseStr = if (aRequestName == AppServices.API.addfriend) {
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
}
