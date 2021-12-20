package com.truelife.app.fragment.club.more

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.android.volley.Request
import com.google.gson.Gson
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.Club
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import com.truelife.util.Utility
import com.truelife.util.wheelpicker.ImageWheelPicker
import com.truelife.util.wheelpicker.ImageWheelPosition
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_create_club_by_you_change_admin.*
import org.json.JSONObject


/**
 * Created by Elango on 21-02-2020.
 */

class TLClubChangeAdmin : BaseActivity(), View.OnClickListener, ResponseListener,
    ImageWheelPosition {

    private var mContext: Context? = null

    private var mUserImage: CircleImageView? = null
    private var mUser: User? = null
    private lateinit var mSelect: Button
    private lateinit var mImageWheelPicker: LinearLayout
    private lateinit var myNoDatafound: TextView

    private lateinit var mSearchLay: LinearLayout
    private lateinit var mSearchCancel: TextView
    private lateinit var mSearch: EditText

    private lateinit var mClub: Club.Clubs
    private var mData = ArrayList<Club.FriendList>()

    var mPage = 1
    var mTotal = 1
    private var mScrollPosition = 0
    private var mFriendID = 0
    var isAdmin = false

    companion object {
        var TAG: String = TLClubChangeAdmin::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_create_club_by_you_change_admin)
        mContext = this
        init()
    }

    override fun init() {
        mUser = LocalStorageSP.getLoginUser(mContext)

        mUserImage = findViewById(R.id.inflate_profile_icon)
        mSelect = findViewById(R.id.change_admin_post_btn)
        mImageWheelPicker = findViewById(R.id.imageWheelLay)
        mSearchLay = findViewById(R.id.fragment_notify_friend_list_Search_LAY)
        myNoDatafound = findViewById(R.id.noDataFoundTET)
        mSearchCancel = findViewById(R.id.fragment_cancel)
        mSearch = findViewById(R.id.fragment_search)

        Utility.loadImage(mUser!!.mProfileImage!!, mUserImage!!)
        common_back_arrow.setOnClickListener { finish() }

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
            isAdmin = intent.extras!!.getBoolean("admin")
        }
        getData(true)

        mSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0) {
                    mSearchCancel.visibility = View.VISIBLE
                } else mSearchCancel.visibility = View.GONE
                getData(true)
            }
        })
    }

    override fun clickListener() {
        mSelect.setOnClickListener(this)
        mSearchCancel.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view) {
            mSelect -> changeAdmin(mFriendID)

            mSearchCancel -> {
                AppDialogs.hideSoftKeyboard(myContext, mSelect)
                mSearch.text = null
                mSearchCancel.visibility = View.GONE
                mSearch.clearFocus()
            }
        }
    }

    private fun changeAdmin(id: Int) {
        if (checkInternet()) {
            AppDialogs.showProgressDialog(mContext!!)
            val mCase = getAdminParam(id)
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                mContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.ChangeAdministrator,
                Club.FriendList::class.java
            )
        }
    }

    private fun getData(show: Boolean) {
        if (checkInternet()) {
            if (show)
                AppDialogs.showProgressDialog(mContext!!)
            val mCase = getParam()
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


    private fun getParam(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            val user = LocalStorageSP.getLoginUser(mContext)
            jsonObject.put("club_id", mClub.id!!)
            jsonObject.put("page_no", mPage)
            jsonObject.put("member_name", getETValue(mSearch))
            jsonObject.put("login_user_id", user.mUserId)

            val jsonParam = JSONObject()
            jsonParam.put("ClubMembers", jsonObject)

            Log.i("ClubMembers --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun getAdminParam(id: Int): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("club_id", mClub.id!!)
            jsonObject.put("user_id", id)

            val jsonParam = JSONObject()
            jsonParam.put("ChangeAdministrator", jsonObject)

            Log.i("Admin--> ", jsonParam.toString())

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
                    if (mPage == 1)
                        mData.clear()
                    mData.addAll((r as Club.FriendList).mList)
                    mTotal = r.total_page!!.toInt()
                    initData()
                } else {
                    if (getETValue(mSearch).isNotEmpty())
                        mData.clear()
                }

                initNoRecord(r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.ChangeAdministrator.hashCode()) {
                if (r.response!!.isSuccess) {
                    AppDialogs.customOkAction(
                        mContext!!,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        object : AppDialogs.ConfirmListener {
                            override fun yes() {
                                setResult(1)
                                finish()
                            }

                        }, false
                    )
                } else AppDialogs.showToastDialog(myContext, r.response!!.responseMessage!!)
            }
        } else AppDialogs.customOkAction(
            mContext!!, null, TLConstant.SERVER_NOT_REACH, null, null, false
        )
    }

    private fun initNoRecord(responseMessage: String) {
        if (mData.isEmpty()) {
            myNoDatafound.text = responseMessage
            myNoDatafound.visibility = View.VISIBLE
            mImageWheelPicker.visibility = View.GONE
            mSelect.visibility = View.GONE
            if (getETValue(mSearch).isEmpty())
                mSearchLay.visibility = View.GONE
        } else {
            myNoDatafound.visibility = View.GONE
            mImageWheelPicker.visibility = View.VISIBLE
            mSelect.visibility = View.VISIBLE
            mSearchLay.visibility = View.VISIBLE
        }
    }


    private fun initData() {
        try {
            if (mData.isNotEmpty()) {
                try {
                    mImageWheelPicker.removeAllViews()
                    val aImgWheel = ImageWheelPicker(myContext, mData, this)
                    mImageWheelPicker.addView(aImgWheel)
                    aImgWheel.setSelection(1, mScrollPosition)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }
    }

    override fun selectedPostition(position: Int, id: Int) {
        mScrollPosition = position
        mFriendID = id

        if (position >= mData.size - 1) {
            if (mTotal != mPage) {
                val updateHandler = Handler()
                val runnable = Runnable {
                    mPage++
                    getData(true)
                }
                updateHandler.postDelayed(runnable, 1000)
            }
        }
    }

}