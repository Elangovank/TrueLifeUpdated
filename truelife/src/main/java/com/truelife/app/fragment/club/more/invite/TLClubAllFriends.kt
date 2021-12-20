package com.truelife.app.fragment.club.more.invite

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import com.truelife.util.EndlessRecyclerViewScrollListener
import com.truelife.util.Helper
import org.json.JSONObject


/**
 * Created by Elango on 20-02-2020.
 */

class TLClubAllFriends : BaseActivity(), ResponseListener, TLClubFriendsAdapter.Callback,
    View.OnClickListener {

    private var mContext: Context? = null
    private lateinit var mView: View

    private lateinit var mFriendsRecycler: RecyclerView
    private lateinit var mFriendsAdapter: TLClubFriendsAdapter
    private lateinit var mSwipe: SwipeRefreshLayout
    private lateinit var mClose: ImageView
    private lateinit var mListener: EndlessRecyclerViewScrollListener
    private lateinit var mSearch: EditText
    private lateinit var mSearchCancel: TextView
    private lateinit var mProgress: ProgressBar


    private lateinit var mClub: Club.Clubs
    private var mData = ArrayList<Club.FriendList>()
    var mFriendId = ""
    var mTotal = 0
    var mPage = 1

    companion object {
        var TAG: String = TLClubAllFriends::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_club_all_friends)
        mContext = this
        init()
    }

    override fun init() {
        mFriendsRecycler = findViewById(R.id.fragment_chat_recycler)
        mSwipe = findViewById(R.id.fragment_chat_swipe_ref)
        mClose = findViewById(R.id.common_back_arrow)
        mSearch = findViewById(R.id.fragment_search)
        mSearchCancel = findViewById(R.id.fragment_cancel)
        mProgress = findViewById(R.id.progress_bar)

        mView = mFriendsRecycler.rootView

        mSwipe.setOnRefreshListener {
            mPage = 1
            mListener.resetState()
            getData(false)
        }

        mSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0)
                    mSearchCancel.visibility = View.VISIBLE

                mFriendsAdapter.filter.filter(s!!.toString()) {
                    AppDialogs.showNoData(
                        mView,
                        it == 0,
                        String.format("We couldn't find anything for\n'%s'", s)
                    )
                }
            }
        })

        initBundle()
        clickListener()
    }

    private fun initBundle() {
        val intent = intent
        if (intent != null) {
            mClub = Gson().fromJson(
                intent.extras!!.getString("club_details"),
                Club.Clubs::class.java
            )
        }
        initRecyler()
        getData(true)
    }

    private fun initRecyler() {
        val layoutManager = LinearLayoutManager(mContext)
        mFriendsRecycler.layoutManager = layoutManager
        mFriendsRecycler.setHasFixedSize(true)
        mFriendsAdapter = TLClubFriendsAdapter(
            mContext!!, mData, this, true, isRemove = false
        )
        mFriendsRecycler.adapter = mFriendsAdapter

        mListener = object : EndlessRecyclerViewScrollListener(layoutManager, 5) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                try {
                    if (mPage < mTotal) {
                        mPage++
                        getData(false)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

        mFriendsRecycler.addOnScrollListener(mListener)
    }

    private fun getData(show: Boolean) {
        if (checkInternet()) {

            if (show)
                AppDialogs.showProgressDialog(mContext!!)
            else if (!mSwipe.isRefreshing)
                mProgress.visibility = View.VISIBLE

            val mCase = getParam(mClub.id!!)
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                mContext!!, this,
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
            jsonObject.put("page_no", mPage)
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

    override fun clickListener() {
        mClose.setOnClickListener(this)
        mSearchCancel.setOnClickListener(this)
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        mSwipe.isRefreshing = false
        mProgress.visibility = View.GONE
        if (r != null) {
            if (r.requestType!! == AppServices.API.clubfriendsList.hashCode()) {
                if (r.response!!.isSuccess) {
                    mTotal = (r as Club.FriendList).total_page!!.toInt()
                    if (mPage == 1)
                        mData.clear()
                    mData.addAll(r.mList)
                    mFriendsAdapter.notifyDataSetChanged()
                } else AppDialogs.showToastDialog(mContext!!, r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.AddClubMember.hashCode()) {
                if (r.response!!.isSuccess) {
                    for (i in mData.indices) {
                        if (mData[i].id == mFriendId) {
                            mData[i].is_member_status = "1"
                        }
                    }
                    mFriendsAdapter.notifyDataSetChanged()
                }
                AppDialogs.showToastDialog(mContext!!, "Invited successfully")
            }
        } else AppDialogs.customOkAction(
            mContext!!, null, TLConstant.SERVER_NOT_REACH, null, null, false
        )
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
        AppDialogs.showProgressDialog(mContext!!)
        val mCase = getAddParam(id)
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.AddClubMember,
            Club.FriendList::class.java
        )
    }

    private fun getAddParam(ids: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
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

    override fun onClick(view: View?) {
        when (view) {
            mClose -> finish()

            mSearchCancel -> {
                AppDialogs.hideSoftKeyboard(this, mFriendsRecycler)
                mSearch.text = null
                mSearchCancel.visibility = View.GONE
                mSearch.clearFocus()
            }
        }
    }

}