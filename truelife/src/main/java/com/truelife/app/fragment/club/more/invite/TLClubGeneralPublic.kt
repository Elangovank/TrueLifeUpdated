package com.truelife.app.fragment.club.more.invite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
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
import com.truelife.util.Helper
import kotlinx.android.synthetic.main.fragment_create_club_by_you_general_public.*
import org.json.JSONObject


/**
 * Created by Elango on 18-02-2020.
 */

class TLClubGeneralPublic : BaseActivity(),
    TLClubByYouGeneralPublicAdapter.ClickListener
    , ResponseListener {

    private lateinit var mContext: Context
    private lateinit var mRecycler: RecyclerView
    private lateinit var mAdapter: TLClubByYouGeneralPublicAdapter
    private lateinit var mClub: Club.Clubs
    private var mData = Club.PublicCategory()

    companion object {
        var TAG: String = TLClubGeneralPublic::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_create_club_by_you_general_public)
        init()
    }

    override fun init() {
        mContext = this
        mRecycler = findViewById(R.id.fragment_horizontal_recycler_member)
        common_back_arrow.setOnClickListener {
            finish()
        }
        initBundle()
    }

    private fun initBundle() {
        val intent = intent
        if (intent != null) {
            mClub = Gson().fromJson(
                intent.extras!!.getString("club_details"),
                Club.Clubs::class.java
            ).clubs!!
        }
        getData()
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
                AppServices.API.ClubPublicCategory,
                Club.PublicCategory::class.java
            )
        }
    }

    private fun getParam(ids: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            val user = LocalStorageSP.getLoginUser(mContext)
            jsonObject.put("club_id", ids)
            jsonObject.put("street", user.mStreetName)
            jsonObject.put("pin_code", user.mPincode)
            jsonObject.put("city_id", user.mCurrentCityId)
            jsonObject.put("state_id", user.mStateId)
            jsonObject.put("country_id", user.mCountryId)
            val jsonParam = JSONObject()
            jsonParam.put("ClubPublicCategory", jsonObject)

            Log.i("ClubPublicCategory --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun initRecyler() {
        val layoutManager = GridLayoutManager(mContext, 2)
        mRecycler.layoutManager = layoutManager
        mRecycler.setHasFixedSize(true)
        mAdapter =
            TLClubByYouGeneralPublicAdapter(
                mContext,
                mData,
                this
            )
        mRecycler.adapter = mAdapter
    }

    override fun clickListener() {

    }

    override fun onClick(position: Int) {
        if (checkInternet()) {
            val intent = Intent(mContext, TLClubGeneralInviteScreen::class.java)
            when (position) {
                0 -> intent.putExtra("action", "area")
                1 -> intent.putExtra("action", "city")
                2 -> intent.putExtra("action", "state")
                3 -> intent.putExtra("action", "country")
            }
            intent.putExtra("club_details", Gson().toJson(mClub))
            startActivity(intent)
        }
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.ClubPublicCategory.hashCode()) {
                if (r.response!!.isSuccess) {
                    mData = (r as Club.PublicCategory).mCategory!!
                    initRecyler()
                } else AppDialogs.showToastDialog(mContext, r.response!!.responseMessage!!)
            }
        } else AppDialogs.customOkAction(
            mContext, null, TLConstant.SERVER_NOT_REACH, null, null, false
        )
    }

}