package com.truelife.app.fragment.club.more.invite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.google.gson.Gson
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.Club
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import kotlinx.android.synthetic.main.fragment_create_club_by_you_friend_contact.*
import org.json.JSONObject


/**
 * Created by Elango on 18-02-2020.
 */

class TLClubFriendsContacts : BaseActivity(), ResponseListener, TLClubFriendsAdapter.Callback,
    View.OnClickListener {

    private lateinit var mContext: Context

    private lateinit var mFriendsRecycler: RecyclerView
    private lateinit var mFriendsAdapter: TLClubFriendsAdapter
    private lateinit var mMoreLayout: LinearLayout
    private lateinit var mMore: TextView
    var mFriendId = ""

    private lateinit var mClub: Club.Clubs
    private var mData = ArrayList<Club.FriendList>()

    companion object {
        var TAG: String = TLClubFriendsContacts::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_create_club_by_you_friend_contact)
        init()
    }

    override fun init() {
        mContext = this
        mFriendsRecycler = findViewById(R.id.fragment_horizontal_friend_recycler)
        mMoreLayout = findViewById(R.id.friends_more_LAY)
        mMore = findViewById(R.id.layout_friend_more_TET)
        common_back_arrow.setOnClickListener {
            finish()
        }
        initBundle()
        clickListener()
    }

    private fun initBundle() {
        val intent = intent
        if (intent != null) {
            mClub = Gson().fromJson(
                intent.extras!!.getString("club_details"),
                Club.Clubs::class.java
            ).clubs!!
        }
        initRecyler()
        getData()
    }

    private fun initRecyler() {
        val layoutManager = LinearLayoutManager(mContext)
        mFriendsRecycler.layoutManager = layoutManager
        mFriendsRecycler.setHasFixedSize(true)
        mFriendsAdapter = TLClubFriendsAdapter(
            mContext, mData, this, false, isRemove = false
        )
        mFriendsRecycler.adapter = mFriendsAdapter
    }

    override fun clickListener() {
        mMore.setOnClickListener(this)
    }

    private fun getData() {
        if (checkInternet()) {
            AppDialogs.showProgressDialog(mContext)
            val mCase = getParam(mClub.id!!)
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                mContext, this,
                result,
                Request.Method.POST,
                AppServices.API.clubfriendsList,
                Club.FriendList::class.java
            )
        }
    }

    private fun getParam(ids: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            val user = LocalStorageSP.getLoginUser(mContext)
            jsonObject.put("club_id", ids)
            jsonObject.put("page_no", "1")
            jsonObject.put("login_user_id", user.mUserId)

            val jsonParam = JSONObject()
            jsonParam.put("ClubFriendsList", jsonObject)

            Log.i("ClubFriendsList --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.clubfriendsList.hashCode()) {
                if (r.response!!.isSuccess) {
                    mData.clear()
                    mData.addAll((r as Club.FriendList).mList)
                    mFriendsAdapter.notifyDataSetChanged()
                } else {
                    friend_view.visibility = View.VISIBLE
                    mFriendsRecycler.visibility = View.GONE
                }
                initMore()
            } else if (r.requestType!! == AppServices.API.AddClubMember.hashCode()) {
                if (r.response!!.isSuccess) {
                    for (i in mData.indices) {
                        if (mData[i].id == mFriendId) {
                            mData[i].is_member_status = "1"
                        }
                    }
                    mFriendsAdapter.notifyDataSetChanged()
                }
                AppDialogs.showToastDialog(mContext, "Invited successfully")
            }
        } else AppDialogs.customOkAction(
            mContext, null, TLConstant.SERVER_NOT_REACH, null, null, false
        )
    }

    private fun initMore() {
        if (mData.size > 3)
            mMoreLayout.visibility = View.VISIBLE
        else mMoreLayout.visibility = View.GONE
    }

    override fun info(id: String) {
        if (checkInternet()) {
            AppDialogs.customDoubleAction(
                myContext,
                null,
                "Do you want to Add Member to Club?",
                "Ok", "Cancel",
                object : AppDialogs.OptionListener {
                    override fun yes() {
                        addFriend(id)
                    }

                    override fun no() {
                        AppDialogs.hidecustomView()
                    }

                }, false, isOptionable = false
            )
        }
    }

    private fun addFriend(id: String) {
        mFriendId = id
        AppDialogs.showProgressDialog(mContext)
        val mCase = getAddParam(id)
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        AppServices.execute(
            mContext, this,
            result,
            Request.Method.POST,
            AppServices.API.AddClubMember,
            Club.FriendList::class.java
        )
    }

    override fun onClick(view: View?) {
        when (view) {
            mMore -> {
                if (checkInternet()) {
                    val intent = Intent(mContext, TLClubAllFriends::class.java)
                    intent.putExtra("club_details", Gson().toJson(mClub))
                    startActivity(intent)
                }
            }
        }
    }

    private fun getAddParam(ids: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            val user = LocalStorageSP.getLoginUser(mContext)
            jsonObject.put("club_id", mClub.id)
            jsonObject.put("club_members", ids)

            val jsonParam = JSONObject()
            jsonParam.put("AddClubMember", jsonObject)

            Log.i("AddClubMember --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

}