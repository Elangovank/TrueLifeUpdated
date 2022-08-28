package com.truelife.app.activity

import android.os.Bundle
import android.text.Html
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.model.Setting
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import com.truelife.util.SingleChoiceAdapter
import org.json.JSONObject

class PrivacySetting : BaseActivity(), ResponseListener {

    val EVERYONE = "Everyone"
    val FRIENDS = "Friends"
    val _ONLY_ME = "Only me"

    lateinit var mContext: FragmentActivity
    var mBackBtn: ImageView? = null
    var mToolbarTitle: TextView? = null

    var mPrivacyProfileViewTxt: TextView? = null
    var mPrivacyProfileCommentTxt: TextView? = null
    var mPrivacyProfileCommentPostTxt: TextView? = null
    var mFriendProfileViewTxt: TextView? = null
    var mSave: TextView? = null

    var mPrivacyProfileViewValue: Int = -1
    var mPrivacyProfileCommentValue: Int = -1
    var mPrivacyProfileCommentPostValue: Int = -1
    var mFriendProfileViewValue: Int = -1

    var user: User? = null

    var list = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_setting)
        addlist()
        init()
        loadValues()
    }

    fun addlist() {

        list.add("Everyone")
        list.add("Friends")
        list.add("Only me")
    }

    override fun clickListener() {
        mSave!!.setOnClickListener {
            saveAPI()
        }
        mBackBtn!!.setOnClickListener {
          super.onBackPressed()
        }

        mPrivacyProfileViewTxt!!.setOnClickListener {
            AppDialogs.showSingleChoice(
                mContext!!,
                "Select any One",
                list,
                object : SingleChoiceAdapter.Callback {
                    override fun info(position: Int, text: String) {
                        AppDialogs.hideSingleChoice()
                        mPrivacyProfileViewValue = position + 1
                        mPrivacyProfileViewTxt!!.text = list.get(position)
                    }
                }, true
            )

        }

        mPrivacyProfileCommentTxt!!.setOnClickListener {
            AppDialogs.showSingleChoice(
                mContext!!,
                "Select any One",
                list,
                object : SingleChoiceAdapter.Callback {
                    override fun info(position: Int, text: String) {
                        AppDialogs.hideSingleChoice()
                        mPrivacyProfileCommentValue = position + 1
                        mPrivacyProfileCommentTxt!!.text = list.get(position)
                    }
                }, true
            )

        }

        mPrivacyProfileCommentPostTxt!!.setOnClickListener {
            AppDialogs.showSingleChoice(
                mContext!!,
                "Select any One",
                list,
                object : SingleChoiceAdapter.Callback {
                    override fun info(position: Int, text: String) {
                        AppDialogs.hideSingleChoice()
                        mPrivacyProfileCommentPostValue = position + 1
                        mPrivacyProfileCommentPostTxt!!.text = list.get(position)
                    }
                }, true
            )

        }
    }

    override fun init() {
        mContext = this
        user = LocalStorageSP.getLoginUser(mContext)
        mBackBtn = findViewById(R.id.common_back_arrow) as ImageView
        mSave = findViewById(R.id.privacy_setting_post_save_text) as TextView
        mToolbarTitle = findViewById(R.id.set_privacy_header) as TextView
        mPrivacyProfileViewTxt =
            findViewById(R.id.fragment_setting_privacy_profile_view) as TextView
        mPrivacyProfileCommentTxt =
            findViewById(R.id.fragment_setting_privacy_public_post_comment) as TextView
        mPrivacyProfileCommentPostTxt =
            findViewById(R.id.fragment_setting_privacy_friends_post_comment) as TextView

        mFriendProfileViewTxt =
            findViewById(R.id.fragment_setting_privacy_friends_section) as TextView

        val aTitle =
            "<b><font color='#000000'>PRIVACY</font></b>" + "<br>" + " <font color='#000000'>SETTINGS</font> "
        mToolbarTitle!!.setText(Html.fromHtml(aTitle))
        clickListener()
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.setting.hashCode()) {
                if (r.response!!.isSuccess) {

                    var privacySetting = r as Setting
                    setProfileView(privacySetting.setting!!.viewYourProfile!!)
                    setProfileComment(privacySetting.setting!!.commentOnPublic!!)
                    setProfileCommentPostFriend(privacySetting.setting!!.commentOnFrndsPosts!!)
                    setFriendProfileView(privacySetting.setting!!.viewYourPostsFrnds!!)
                }
            } else if (r.requestType!! == AppServices.API.privacysettingsave.hashCode()) {
                // if (r.response!!.isSuccess) {
                AppDialogs.customSuccessAction(
                    this,
                    null,
                    r.response!!.responseMessage!!,
                    null,
                    null,
                    false
                )
                // }
            }
        }
    }

    fun setProfileView(value: String) {
        mPrivacyProfileViewValue = value.toInt()
        when (value) {

            "1" -> mPrivacyProfileViewTxt!!.text = EVERYONE
            "2" -> mPrivacyProfileViewTxt!!.text = FRIENDS
            "3" -> mPrivacyProfileViewTxt!!.text = _ONLY_ME
        }
    }

    fun setProfileComment(value: String) {
        mPrivacyProfileCommentValue = value.toInt()
        when (value) {
            "1" -> mPrivacyProfileCommentTxt!!.text = EVERYONE
            "2" -> mPrivacyProfileCommentTxt!!.text = FRIENDS
            "3" -> mPrivacyProfileCommentTxt!!.text = _ONLY_ME
        }
    }

    fun setProfileCommentPostFriend(value: String) {
        mPrivacyProfileCommentPostValue = value.toInt()
        when (value) {
            "1" -> mPrivacyProfileCommentPostTxt!!.text = EVERYONE
            "2" -> mPrivacyProfileCommentPostTxt!!.text = FRIENDS
            "3" -> mPrivacyProfileCommentPostTxt!!.text = _ONLY_ME
        }
    }

    fun setFriendProfileView(value: String) {
        mFriendProfileViewValue = value.toInt()
        when (value) {
            "1" -> mFriendProfileViewTxt!!.text = EVERYONE
            "2" -> mFriendProfileViewTxt!!.text = FRIENDS
            "3" -> mFriendProfileViewTxt!!.text = _ONLY_ME
        }
    }


    fun loadValues() {

        AppDialogs.showProgressDialog(mContext!!)
        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL,
            getAccountSettingCaseString(user!!.mUserId!!, "PrivacyScreen")!!
        )
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.setting,
            Setting::class.java
        )
    }

    private fun getAccountSettingCaseString(
        aUserId: String,
        aFromScreen: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", aUserId)
            val jsonParam = JSONObject()
            when (aFromScreen) {
                "AccountSetting" -> {
                    jsonParam.put("AccountSettingList", jsonParam1)
                    Log.e("AccountSettingList", " $jsonParam")
                }
                "PrivacyScreen" -> {
                    jsonParam.put("PrivacySettingsList", jsonParam1)
                    Log.e("PrivacySettingsList", " $jsonParam")
                }
                "PersonalSetting" -> {
                    jsonParam.put("PersonalSettingList", jsonParam1)
                    Log.e("PersonalSettingList", " $jsonParam")
                }
                "ProfileSetting" -> {
                    jsonParam.put("ProfileSettingList", jsonParam1)
                    Log.e("ProfileSettingList", " $jsonParam")
                }
            }
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    fun saveAPI() {
        AppDialogs.showProgressDialog(mContext!!)
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getPrivacySettingCaseString()!!
            )
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.privacysettingsave,
            Response::class.java
        )
    }

    fun getPrivacySettingCaseString(
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", user!!.mUserId!!)
            jsonParam1.put("view_your_profile", mPrivacyProfileViewValue)
            jsonParam1.put("view_your_posts_frnds", mFriendProfileViewValue)
            jsonParam1.put("comment_on_public", mPrivacyProfileCommentValue)
            jsonParam1.put("comment_on_frnds_posts", mPrivacyProfileCommentPostValue)
            val jsonParam = JSONObject()
            jsonParam.put("PrivacySettings", jsonParam1)
            Log.e("PrivacySettings", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }
}
