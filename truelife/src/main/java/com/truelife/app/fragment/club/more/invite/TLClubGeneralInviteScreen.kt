package com.truelife.app.fragment.club.more.invite

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
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
import com.truelife.util.EndlessRecyclerViewScrollListener
import com.truelife.util.Helper
import kotlinx.android.synthetic.main.fragment_create_club_by_you_general_public.*
import org.json.JSONObject


/**
 * Created by Elango on 19-02-2020.
 */

class TLClubGeneralInviteScreen : BaseActivity(), ResponseListener, TLClubInviteAdapter.Callback,
    View.OnClickListener {

    private lateinit var mContext: Context
    private lateinit var mView: View

    private lateinit var mRecycler: RecyclerView
    private lateinit var mHeader: TextView
    private lateinit var mProgress: ProgressBar
    private lateinit var mInvite: Button

    private lateinit var mAdapter: TLClubInviteAdapter
    private lateinit var mClub: Club.Clubs
    var mTitle = ""
    var mID = ""
    var mTotal = 0
    var mPage = 1
    private var mData = ArrayList<Club.FriendList>()

    companion object {
        var TAG: String = TLClubGeneralInviteScreen::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_create_club_by_you_general_public_invite)
        init()
    }

    override fun init() {
        mContext = this
        mRecycler = findViewById(R.id.fragment_horizontal_recycler_member)
        mHeader = findViewById(R.id.common_text_header)
        mProgress = findViewById(R.id.progress_bar)
        mInvite = findViewById(R.id.invite_general_public_btn)

        mView = mRecycler.rootView

        common_back_arrow.setOnClickListener {
            finish()
        }
        initBundle()
        clickListener()
    }

    private fun initBundle() {
        val intent = intent
        if (intent != null) {
            mTitle = intent.extras!!.getString("action")!!
            mClub = Gson().fromJson(
                intent.extras!!.getString("club_details"),
                Club.Clubs::class.java
            )
        }
        mHeader.text = String.format("general public\n%s", mTitle).toUpperCase()
        initRecyler()
        getData(true)
    }

    private fun getData(show: Boolean) {
        if (checkInternet()) {
            if (show)
                AppDialogs.showProgressDialog(mContext)
            else mProgress.visibility = View.VISIBLE

            val mCase = getParam(mClub.id!!)
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                mContext, this,
                result,
                Request.Method.POST,
                AppServices.API.ClubPublicMembers,
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

            when (mTitle) {
                "area" -> {
                    jsonObject.put("street", "")
                    jsonObject.put("pin_code", user.mPincode)
                    jsonObject.put("city_id", "")
                    jsonObject.put("state_id", "")
                    jsonObject.put("country_id", "")
                }
                "city" -> {
                    jsonObject.put("street", "")
                    jsonObject.put("pin_code", "")
                    jsonObject.put("city_id", user.mCurrentCityId)
                    jsonObject.put("state_id", "")
                    jsonObject.put("country_id", "")
                }
                "state" -> {
                    jsonObject.put("state_id", user.mStateId)
                    jsonObject.put("street", "")
                    jsonObject.put("pin_code", "")
                    jsonObject.put("city_id", "")
                    jsonObject.put("country_id", "")

                }
                "country" -> {
                    jsonObject.put("street", "")
                    jsonObject.put("pin_code", "")
                    jsonObject.put("city_id", "")
                    jsonObject.put("state_id", "")
                    jsonObject.put("country_id", user.mCountryId)
                }
            }

            jsonObject.put("page_no", mPage)
            val jsonParam = JSONObject()
            jsonParam.put("ClubPublicMembers", jsonObject)

            Log.i("ClubPublicMembers --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun initRecyler() {
        val layoutManager = GridLayoutManager(mContext, 3)
        mRecycler.layoutManager = layoutManager
        mRecycler.setHasFixedSize(true)
        mAdapter = TLClubInviteAdapter(
            mContext,
            mData,
            this
        )
        mRecycler.adapter = mAdapter

        val listener = object : EndlessRecyclerViewScrollListener(layoutManager, 5) {
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

        mRecycler.addOnScrollListener(listener)
    }

    override fun clickListener() {
        mInvite.setOnClickListener(this)
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        mProgress.visibility = View.GONE
        if (r != null) {
            if (r.requestType!! == AppServices.API.ClubPublicMembers.hashCode()) {
                if (r.response!!.isSuccess) {
                    mTotal = (r as Club.FriendList).total_page!!.toInt()

                    if (mPage == 1)
                        mData.clear()

                    mData.addAll(r.mList)
                    mAdapter.notifyDataSetChanged()
                }

                initNoData(r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.Invites.hashCode()) {
                if (r.response!!.isSuccess) {
                    finish()
                }
                AppDialogs.showToastDialog(mContext, r.response!!.responseMessage!!)
            }
        } else AppDialogs.customOkAction(
            mContext, null, TLConstant.SERVER_NOT_REACH, null, null, false
        )
    }

    private fun initNoData(msg: String) {
        if (mData.isEmpty())
            mRecycler.visibility = View.GONE
        else mRecycler.visibility = View.VISIBLE

        AppDialogs.showNoData(mView, mData.isEmpty(), msg)
    }

    override fun info(id: String) {
        for (i in mData.indices) {
            if (mData[i].id == id) {
                mData[i].isSelected = !mData[i].isSelected
            }

        }
        mAdapter.notifyDataSetChanged()
    }

    private fun getInviteParam(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            val user = LocalStorageSP.getLoginUser(mContext)
            jsonObject.put("club_id", mClub.id!!)
            jsonObject.put("login_user_id", user.mUserId)
            jsonObject.put("to_user_id", mID)
            jsonObject.put("request_type", "1")
            val jsonParam = JSONObject()
            jsonParam.put("Invites", jsonObject)

            Log.i("Invites --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onClick(view: View?) {
        when (view) {
            mInvite -> {
                if (checkInternet()) {
                    mID = getSelectedID()

                    AppDialogs.showProgressDialog(mContext)
                    val mCase = getInviteParam()
                    val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
                    AppServices.execute(
                        mContext, this,
                        result,
                        Request.Method.POST,
                        AppServices.API.Invites,
                        Club.FriendList::class.java
                    )
                }
            }
        }
    }

    private fun getSelectedID(): String {
        val id = ArrayList<String>()
        for (i in mData.indices) {
            if (mData[i].isSelected) {
                id.add(mData[i].id!!)
            }
        }
        return TextUtils.join(",", id)
    }

}