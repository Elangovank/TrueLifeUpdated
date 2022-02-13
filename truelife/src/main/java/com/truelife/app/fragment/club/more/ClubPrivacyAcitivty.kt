package com.truelife.app.fragment.club.more

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.FragmentActivity
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
import com.truelife.util.SingleChoiceAdapter
import kotlinx.android.synthetic.main.activity_club_by_you_privacy.*
import org.json.JSONObject

/**
 * Created by Elango on 20-02-2020.
 */

class ClubPrivacyAcitivty : BaseActivity(), ResponseListener {

    lateinit var mclubObj: Club.Clubs
    lateinit var mContext: FragmentActivity
    var mBackBtn: ImageButton? = null
    var mSave: Button? = null
    var user: User? = null
    var list = ArrayList<String>()
    lateinit var mNotificationSwitch: SwitchCompat
    lateinit var mPostVisibilitySwitch: SwitchCompat
    var mClubVisibility = 0
    var mMemberVisibility = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_club_by_you_privacy)

        mclubObj = Gson().fromJson(intent.extras!!.get("obj").toString(), Club.Clubs::class.java)!!
        addlist()
        init()
        loadValues()
    }

    fun addlist() {

        list.add(getString(R.string.label_visible_to_Everyone))
        list.add(getString(R.string.label_visible_to_members_only))
        list.add(getString(R.string.label_visible_only_to_me))
    }

    fun validate(): Boolean {
        return when {
            club_item.text.isEmpty() -> {
                AppDialogs.showToastDialog(mContext, getString(R.string.alert_club_visible))
                false
            }
            member_item.length() == 0 -> {
                AppDialogs.showToastDialog(mContext, getString(R.string.alert_member_visible))
                false
            }
            else -> {
                true
            }
        }
    }

    override fun clickListener() {
        mSave!!.setOnClickListener {

            if (validate())
                saveAPI()
        }
        mBackBtn!!.setOnClickListener {
            finish()
        }

        Club_member_LAY!!.setOnClickListener {
            AppDialogs.showSingleChoice(
                mContext,
                "Club Visibility",
                list,
                object : SingleChoiceAdapter.Callback {
                    override fun info(position: Int, text: String) {
                        AppDialogs.hideSingleChoice()
                        club_item!!.text = list[position]
                        mClubVisibility = position
                    }
                }, true
            )

        }

        member_visibility_LAY!!.setOnClickListener {
            AppDialogs.showSingleChoice(
                mContext,
                "Member Visibility",
                list,
                object : SingleChoiceAdapter.Callback {
                    override fun info(position: Int, text: String) {
                        AppDialogs.hideSingleChoice()
                        mMemberVisibility = position
                        member_item!!.text = list.get(position)
                    }
                }, true
            )

        }

    }

    override fun init() {
        mContext = this
        user = LocalStorageSP.getLoginUser(mContext)
        mBackBtn = findViewById(R.id.close_club_button)
        mSave = findViewById(R.id.privacy_post_btn)
        mNotificationSwitch = findViewById(R.id.notification_switch)
        mPostVisibilitySwitch = findViewById(R.id.post_approval_switch)

        clickListener()
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.privacysettingsave.hashCode()) {
                if (r.response!!.isSuccess) {
                    AppDialogs.customOkAction(
                        mContext,
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
                } else AppDialogs.showToastDialog(mContext, r.response!!.responseMessage!!)
            }
        } else AppDialogs.customOkAction(
            mContext,
            null,
            TLConstant.SERVER_NOT_REACH,
            null,
            null,
            false
        )
    }


    fun loadValues() {

        club_item.text = when (mclubObj.clubs?.clubVisibility!!) {
            "1" -> getString(R.string.label_visible_to_members_only)
            "2" -> getString(R.string.label_visible_only_to_me)
            "0" -> getString(R.string.label_visible_to_Everyone)
            else -> getString(R.string.label_visible_to_Everyone)
        }
        member_item.text = when (mclubObj.clubs?.clubRights?.memberVisibility!!) {
            "1" -> getString(R.string.label_visible_to_members_only)
            "2" -> getString(R.string.label_visible_only_to_me)
            "0" -> getString(R.string.label_visible_to_Everyone)
            else -> getString(R.string.label_visible_to_Everyone)
        }

        mNotificationSwitch.isChecked = when (mclubObj.clubs?.notification!!) {
            "1" -> true
            "0" -> false
            else -> false
        }

        mPostVisibilitySwitch.isChecked = when (mclubObj.clubs?.clubRights?.post_visibility!!) {
            "1" -> true
            "0" -> false
            else -> false
        }

    }


    fun saveAPI() {

        AppDialogs.showProgressDialog(mContext)
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getClubByYouPrivacyCaseString()!!
            )
        AppServices.execute(
            mContext, this,
            result,
            Request.Method.POST,
            AppServices.API.privacysettingsave,
            Response::class.java
        )
    }


    private fun getClubByYouPrivacyCaseString(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("club_id", mclubObj.clubs!!.id)
            jsonParam1.put("club_visibility", mClubVisibility)
            jsonParam1.put("member_visibility", mMemberVisibility)
            jsonParam1.put("rule", "2")
            jsonParam1.put("notification", if (mNotificationSwitch.isChecked) "1" else "0")
            jsonParam1.put("post_visibility", if (mPostVisibilitySwitch.isChecked) "1" else "0")
            val jsonParam = JSONObject()
            jsonParam.put("ClubPrivacySettings", jsonParam1)
            Log.e("ClubPrivacySettings", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }
}
