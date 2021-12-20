package com.truelife.app.activity

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.adapter.YourFollowersListAdapter
import com.truelife.app.model.User
import com.truelife.app.model.UserList
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import org.json.JSONObject

class YourFollowersActivity : BaseActivity(), ResponseListener,
    YourFollowersListAdapter.ClickListener {

    lateinit var mContext: FragmentActivity

    var user: User? = null
    var mUserList = ArrayList<UserList.UList>()
    lateinit var mRecycleView: RecyclerView
    var mToolbarTitle: TextView? = null
    var mNoDataFound: TextView? = null
    var mFollowersCountTxt: TextView? = null
    var mBackBtn: ImageView? = null

    var mAdapter: YourFollowersListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_followers)
        init()
        getFollowersList()
    }

    override fun clickListener() {
        mBackBtn!!.setOnClickListener {
            finish()
        }
    }

    override fun init() {

        mContext = this
        mRecycleView = findViewById(R.id.fragment_horizontal_friend_recycler) as RecyclerView
        mBackBtn = findViewById(R.id.back_icon) as ImageView
        mToolbarTitle = findViewById(R.id.chat_title) as TextView
        mNoDataFound = findViewById(R.id.no_block_friends_found) as TextView
        mFollowersCountTxt = findViewById(R.id.followers_count) as TextView
        mToolbarTitle!!.setText(getString(R.string.label_your_followers))
        mToolbarTitle!!.typeface = ResourcesCompat.getFont(mContext!!, R.font.calling_heart)
        user = LocalStorageSP.getLoginUser(mContext)

        setRecylceview()
        clickListener()
    }

    fun getFollowersList() {

        AppDialogs.showProgressDialog(mContext!!)

        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getFollowersListCaseString(user!!.mUserId!!, 1, "FollowersList")!!
            )
        AppServices.execute(
            mContext!!, this,
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
                    var mUser: UserList? = r as UserList
                    mUserList.addAll(mUser!!.followers_list!!)
                    if (mUserList.isNullOrEmpty() || mUserList.size == 0) {
                        mFollowersCountTxt!!.text = "0 Follower"
                    } else if (mUserList.size >= 1) {
                        mFollowersCountTxt!!.text = (mUserList.size).toString() + " Followers"
                    }
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
            }
        }
    }

    fun setRecylceview() {

        val layoutManager = LinearLayoutManager(mContext)
        mRecycleView.layoutManager = layoutManager
        mRecycleView.setHasFixedSize(true)
        mAdapter = YourFollowersListAdapter(mContext, mUserList, this)
        mRecycleView.adapter = mAdapter


    }

    override fun onClick(tt: Any, position: Int) {

    }

    override fun onLongClick(view: View?, position: Int) {

    }
}
