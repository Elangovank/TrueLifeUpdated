package com.truelife.app.activity

import android.os.Bundle
import android.text.Html
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.model.AccountSettingModel
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import org.json.JSONObject

class AccountSetting : BaseActivity(), ResponseListener {
    lateinit var mContext: FragmentActivity
    var mBackBtn: ImageView? = null
    var mToolbarTitle: TextView? = null
    var user: User? = null
    var mSaveBtn: Button? = null
    var mSavePasswordBtn: Button? = null

    var mFirstNameED: EditText? = null
    var mLastNameED: EditText? = null
    var mUserNameED: EditText? = null
    var mEmailED: EditText? = null
    var mMobileNoED: EditText? = null
    var mCurrentPasswordED: EditText? = null
    var mNewPasswordED: EditText? = null
    var mConfirmPasswordED: EditText? = null


    var mMaleRB: RadioButton? = null
    var mFeMaleRB: RadioButton? = null
    var mGenderRG: RadioGroup? = null

    var mFirstNameVal: String? = null
    var mLastNameVal: String? = null
    var muserNameVal: String? = null
    var mMobileVal: String? = null
    var mEmailVal: String? = null
    var mGenderVal: String? = null

    var mCurrentPasswordVal: String? = null
    var mNewPasswordVal: String? = null
    var mConfirmPasswordVal: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_setting)
        init()
        loadValues()
    }

    fun getPasswordValues() {
        mCurrentPasswordVal = getETValue(mCurrentPasswordED)
        mNewPasswordVal = getETValue(mNewPasswordED)
        mConfirmPasswordVal = getETValue(mConfirmPasswordED)
    }

    fun passwordValidation(): Boolean {

        getPasswordValues()

        if (mCurrentPasswordVal.isNullOrEmpty() || mCurrentPasswordVal!!.length == 0) {
            AppDialogs.showToastDialog(
                mContext,
                getString(R.string.alert_message_validation_empty_current_password)
            )
            return false
        } else if (((mCurrentPasswordVal!!.length) <= 6)) {
            AppDialogs.showToastDialog(
                mContext,
                getString(R.string.alert_message_validation_valid_password)
            )
            return false
        } else if (mNewPasswordVal.isNullOrEmpty() || mNewPasswordVal!!.length == 0) {
            AppDialogs.showToastDialog(
                mContext,
                getString(R.string.alert_message_validation_empty_new_password)
            )
            return false
        } else if (((mNewPasswordVal!!.length) <= 6)) {
            AppDialogs.showToastDialog(
                mContext,
                getString(R.string.alert_message_validation_valid_new_password)
            )
            return false
        } else if (((mConfirmPasswordVal!!.length) <= 6)) {
            AppDialogs.showToastDialog(
                mContext,
                getString(R.string.alert_message_validation_valid_confirm_password)
            )
            return false
        } else if (!(mConfirmPasswordVal!!.equals(mNewPasswordVal!!, false))) {
            AppDialogs.showToastDialog(
                mContext,
                getString(R.string.alert_message_validation_match_confirm_password)
            )
            return false
        }
        return true
    }

    fun submitPasswordAPI() {
        if (passwordValidation()) {
            savePasswordValues()
        }

    }


    fun getValues() {

        mFirstNameVal = getETValue(mFirstNameED)
        mLastNameVal = getETValue(mLastNameED)
        muserNameVal = getETValue(mUserNameED)
        mMobileVal = getETValue(mMobileNoED)
        mEmailVal = getETValue(mEmailED)

        if (mMaleRB!!.isChecked) {
            mGenderVal = "1"
        } else if (mFeMaleRB!!.isChecked) {
            mGenderVal = "2"
        } else {
            mGenderVal = "3"
        }
    }

    fun validate(): Boolean {

        getValues()
        if (mFirstNameVal.isNullOrEmpty() || mFirstNameVal!!.length == 0) {
            AppDialogs.showToastDialog(
                mContext,
                getString(R.string.alert_message_validation_empty_first_name)
            )
            return false
        } else if (mLastNameVal.isNullOrEmpty() || mLastNameVal!!.length == 0) {
            AppDialogs.showToastDialog(
                mContext,
                getString(R.string.alert_message_validation_empty_last_name)
            )
            return false
        } else if (muserNameVal.isNullOrEmpty() || muserNameVal!!.length == 0) {
            AppDialogs.showToastDialog(
                mContext,
                getString(R.string.alert_message_validation_empty_user_name)
            )
            return false
        } else if (mMobileVal.isNullOrEmpty() || mMobileVal!!.length < 10) {
            AppDialogs.showToastDialog(
                mContext,
                getString(R.string.alert_message_validation_empty_Mobile_no)
            )
            return false
        } else if (mEmailVal.isNullOrEmpty() || mEmailVal!!.length == 0) {
            AppDialogs.showToastDialog(
                mContext,
                getString(R.string.alert_message_validation_empty_email)
            )
            return false
        } else if (!Helper.isValidEmail(mEmailVal!!)) {
            AppDialogs.showToastDialog(
                mContext,
                getString(R.string.alert_message_validation_valid_email)
            )
            return false
        } else if (!mMaleRB!!.isChecked && !mFeMaleRB!!.isChecked) {
            AppDialogs.showToastDialog(
                mContext,
                getString(R.string.alert_message_validation_empty_gender)
            )
            return false
        }
        return true

    }

    override fun clickListener() {
        mSaveBtn!!.setOnClickListener {

            if (validate())
                saveValues()
        }

        mSavePasswordBtn!!.setOnClickListener {

            submitPasswordAPI()
        }

        mBackBtn!!.setOnClickListener {
            finish()
        }
    }

    override fun init() {
        mContext = this
        user = LocalStorageSP.getLoginUser(mContext)
        mBackBtn = findViewById(R.id.common_back_arrow) as ImageView
        mToolbarTitle = findViewById(R.id.header_title) as TextView
        mSavePasswordBtn = findViewById(R.id.change_password_save_btn) as Button
        mSaveBtn = findViewById(R.id.details_post_save_text) as Button
        mFirstNameED = findViewById(R.id.fragment_setting_account_fname) as EditText
        mLastNameED = findViewById(R.id.fragment_setting_account_lname) as EditText
        mUserNameED = findViewById(R.id.fragment_setting_account_username) as EditText
        mMobileNoED = findViewById(R.id.fragment_setting_account_mobile_no) as EditText
        mEmailED = findViewById(R.id.fragment_setting_account_email) as EditText

        mCurrentPasswordED = findViewById(R.id.fragment_setting_account_current_pwd) as EditText
        mNewPasswordED = findViewById(R.id.fragment_setting_account_new_pwd) as EditText
        mConfirmPasswordED = findViewById(R.id.fragment_setting_account_confirm_pwd) as EditText

        mGenderRG = findViewById(R.id.gender_rg) as RadioGroup
        mMaleRB = findViewById(R.id.male_rd) as RadioButton
        mFeMaleRB = findViewById(R.id.female_rd) as RadioButton

        val aTitle =
            "<b><font color='#000000'>ACCOUNT</font></b>" + "<br>" + " <font color='#000000'>SETTINGS</font> "
        mToolbarTitle!!.setText(Html.fromHtml(aTitle))
        clickListener()
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.accountsetting.hashCode()) {
                if (r.response!!.isSuccess) {
                    var res = r as AccountSettingModel
                    setValues(res.accountSetting)
                } else {
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                    //   AppDialogs.showToastDialog(mContext, r.response!!.responseMessage!!)
                }
            } else if (r.requestType!! == AppServices.API.savepassworddetails.hashCode()) {
                if (r.response!!.isSuccess) {
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                } else {
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                }
            } else if (r.requestType!! == AppServices.API.saveaccountdetails.hashCode()) {
                if (r.response!!.isSuccess) {
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                } else {
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                }
            }
        }
    }

    private fun setValues(accountSetting: AccountSettingModel?) {

        mFirstNameED!!.setText(accountSetting!!.firstName)
        mLastNameED!!.setText(accountSetting!!.lastName)
        mUserNameED!!.setText(accountSetting!!.userName)
        mEmailED!!.setText(accountSetting!!.email)
        mMobileNoED!!.setText(accountSetting!!.mobileNumber)

        if (accountSetting.gender.equals("1")) {
            mMaleRB!!.isChecked = true
        } else if (accountSetting.gender.equals("2")) {
            mFeMaleRB!!.isChecked = true
        }

    }

    fun loadValues() {

        AppDialogs.showProgressDialog(mContext!!)
        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL,
            getAccountSettingCaseString(user!!.mUserId!!, "AccountSetting")!!
        )
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.accountsetting,
            AccountSettingModel::class.java
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


    fun saveValues() {

        AppDialogs.showProgressDialog(mContext!!)
        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL,
            PostAccountSettingCaseString(
                user!!.mUserId!!,
                mFirstNameVal!!,
                mLastNameVal!!,
                mEmailVal!!,
                muserNameVal!!,
                mMobileVal!!,
                mGenderVal!!
            )!!
        )
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.saveaccountdetails,
            Response::class.java
        )
    }

    private fun PostAccountSettingCaseString(
        aUserId: String,
        aFname: String,
        aLname: String,
        aEmail: String,
        aUname: String,
        aMobileNo: String,
        aGender: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("first_name", aFname)
            jsonParam1.put("last_name", aLname)
            jsonParam1.put("email", aEmail)
            jsonParam1.put("username", aUname)
            jsonParam1.put("mobile_number", aMobileNo)
            jsonParam1.put("gender", aGender)
            val jsonParam = JSONObject()
            jsonParam.put("AccountSettings", jsonParam1)
            Log.e("AccountSettings", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    fun savePasswordValues() {

        AppDialogs.showProgressDialog(mContext!!)
        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL,
            PostChangePasswordCaseString(
                user!!.mUserId!!,
                mCurrentPasswordVal!!,
                mNewPasswordVal!!
            )!!
        )
        AppServices.execute(
            mContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.savepassworddetails,
            Response::class.java
        )
    }

    private fun PostChangePasswordCaseString(
        aUserId: String,
        aOldpwd: String,
        aNewPwd: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("old_password", aOldpwd)
            jsonParam1.put("new_password", aNewPwd)
            val jsonParam = JSONObject()
            jsonParam.put("ChangePassword", jsonParam1)
            Log.e("ChangePassword", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }
}
