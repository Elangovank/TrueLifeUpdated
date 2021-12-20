package com.truelife.app.fragment.chat.friends

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.google.gson.Gson
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.activity.TLChatActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.chat.message.TLChatFragment
import com.truelife.app.model.Chat
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.EndlessRecyclerViewScrollListener
import com.truelife.util.Helper
import kotlinx.android.synthetic.main.app_chat_header.*
import org.json.JSONObject


/**
 * Created by Elango on 24-12-2019.
 */

class TLChatFriendsFragment(val mShowHeader: Boolean) : BaseFragment(), View.OnClickListener,
    TLFriendsListAdapter.Callback,
    ResponseListener {

    private var mContext: Context? = null
    private var mFragmentManager: TLFragmentManager? = null
    private lateinit var mBack: ImageView
    private lateinit var mSearch: EditText
    private lateinit var mChatRecycler: RecyclerView
    private lateinit var mNoDataText: TextView
    private var mAdapter: TLFriendsListAdapter? = null
    private var mSwipeRefresh: SwipeRefreshLayout? = null
    var mData = ArrayList<Chat.FriendsList>()
    var mSearchData = ArrayList<Chat.FriendsList>()
    private lateinit var mSearchCancel: TextView
    private var mPage = 1
    private var mTotalPage = 0
    var isClear = true

    private var mView: View? = null

    companion object {
        var TAG: String = TLChatFriendsFragment::class.java.simpleName
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_friends_list, container, false)
        init(mView!!)
        clickListener()

        return mView
    }

    override fun init(view: View) {
        mContext = activity
        mFragmentManager = TLFragmentManager(activity!!)

        mBack = mView!!.findViewById(R.id.app_header_back_arrow)

        mSwipeRefresh = mView!!.findViewById(R.id.fragment_chat_swipe_ref)
        mChatRecycler = mView!!.findViewById(R.id.fragment_chat_recycler)
        mSearch = mView!!.findViewById(R.id.fragment_search)
        mNoDataText = mView!!.findViewById(R.id.no_data_found_txt)
        mSearchCancel = mView!!.findViewById(R.id.fragment_cancel)

        showHeader(mShowHeader)

        initRecycler()

        AppDialogs.swipeRefColor(mContext!!, mSwipeRefresh!!)
        mSwipeRefresh!!.setOnRefreshListener {
            mPage = 1
            isClear = true
            getData(false)
        }

        getData(true)


        mBack.visibility = View.VISIBLE

        mSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0) {
                    mSearchCancel.visibility = View.VISIBLE
                    searchFriend()
                }

                if (count == 0 && mSearchData.isNotEmpty()) {
                    mNoDataText.visibility = View.GONE
                    mData.clear()
                    mData.addAll(mSearchData)
                    mSearchData.clear()
                    mAdapter!!.notifyDataSetChanged()
                }
            }
        })
    }

    fun showHeader(show: Boolean) {
        if (show)
            mView!!.findViewById<CardView>(R.id.tool).visibility = View.VISIBLE
        else mView!!.findViewById<CardView>(R.id.tool).visibility = View.GONE
    }

    private fun noText(text: String) {
        mNoDataText.text = String.format("We couldn't find anything for\n'%s'", text)
        mNoDataText.visibility = View.VISIBLE
    }

    override fun clickListener() {
        mBack.setOnClickListener(this)
        mSearchCancel.setOnClickListener(this)
    }

    private fun getData(showProgress: Boolean) {
        if (checkInternet()) {
            AppDialogs.hideSoftKeyboard((mContext as Activity?)!!, mChatRecycler)
            if (showProgress)
                AppDialogs.showProgressDialog(mContext!!)

            val mCase =
                getParam(mPage.toString(), LocalStorageSP.getLoginUser(mContext!!).mUserId!!)
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                mContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.friendList,
                Chat.FriendsList::class.java
            )
        }
    }

    private fun searchFriend() {
        if (checkInternet()) {
            val mCase = getSearchParam("1", getETValue(mSearch))
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                mContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.searchFriend,
                Chat.FriendsList::class.java
            )
        }
    }

    private fun getParam(page: String, id: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("page_no", page)
            jsonObject.put("login_user_id", id)
            jsonObject.put("user_id", id)
            val jsonParam = JSONObject()
            jsonParam.put("FriendsList", jsonObject)

            Log.i("Param -->> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun getSearchParam(page: String, text: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("page_no", page)
            jsonObject.put("login_user_id", LocalStorageSP.getLoginUser(mContext!!).mUserId!!)
            jsonObject.put("search_text", text)
            val jsonParam = JSONObject()
            jsonParam.put("SearchFriend", jsonObject)
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(mContext)
        mChatRecycler.layoutManager = layoutManager
        mChatRecycler.setHasFixedSize(true)
        mAdapter = TLFriendsListAdapter(
            mContext!!,
            mData,
            this
        )
        mChatRecycler.adapter = mAdapter

        val listener = object : EndlessRecyclerViewScrollListener(layoutManager, 5) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                try {
                    if (mTotalPage > mPage) {
                        isClear = false
                        mPage++
                        getData(true)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

        mChatRecycler.addOnScrollListener(listener)
    }


    override fun initBundle() {

    }

    override fun onClick(v: View?) {
        when (v!!) {
            mBack -> (mContext as TLChatActivity).finishActivity()
            mSearchCancel -> {
                AppDialogs.hideSoftKeyboard(activity!!, mChatRecycler)
                mSearch.text = null
                mSearchCancel.visibility = View.GONE
                mSearch.clearFocus()
            }
        }
    }

    override fun onBackPressed() {
        (mContext as TLChatActivity).finishActivity()
    }

    override fun onResumeFragment() {
        app_header_title.text = "Friends"
        app_header_title.typeface = ResourcesCompat.getFont(mContext!!, R.font.calling_heart)
        app_header_title.textSize = 30f
        app_header_menu.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun info(position: Int) {
        if (checkInternet()) {
            if (!mShowHeader)
                mFragmentManager!!.onBackPress()
            val bundle = Bundle()
            val chat = Chat.ChatList()
            chat.user_id = mData[position].id
            chat.profile_image = mData[position].profile_image
            chat.blocked = mData[position].is_blocked
            chat.user_name = mData[position].fullname
            chat.gender = mData[position].gender
            chat.last_seen = mData[position].last_seen
            bundle.putString("data", Gson().toJson(chat))
            mFragmentManager!!.addContent(TLChatFragment(), TLChatFragment.TAG, bundle)
        }
    }

    override fun onResponse(r: Response?) {
        mSwipeRefresh!!.isRefreshing = false
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.friendList.hashCode()) {
                mNoDataText.visibility = View.GONE
                if (r.response!!.isSuccess) {
                    mTotalPage = (r as Chat.FriendsList).total_page!!.toInt()
                    if (isClear)
                        mData.clear()
                    mData.addAll(r.mData)
                    mAdapter!!.notifyDataSetChanged()
                } else AppDialogs.showToastDialog(mContext!!, r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.searchFriend.hashCode()) {
                if (r.response!!.isSuccess) {
                    mNoDataText.visibility = View.GONE
                    if (mSearchData.isEmpty())
                        mSearchData.addAll(mData)

                    mData.clear()
                    mData.addAll((r as Chat.FriendsList).mData)
                    mAdapter!!.notifyDataSetChanged()
                } else noText(getETValue(mSearch))
            }
        } else AppDialogs.customOkAction(
            mContext!!,
            null,
            TLConstant.SERVER_NOT_REACH,
            null,
            null,
            false
        )
    }

}