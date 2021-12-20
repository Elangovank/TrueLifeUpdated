package com.truelife.app.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.base.progressInterface
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import kotlinx.android.synthetic.main.activity_reset_password.*
import org.json.JSONObject

class TLResetPassword : BaseActivity(), ResponseListener, progressInterface {
    lateinit var mContext: Context


    var mMobileNo: String? = null
    var mEmail: String? = null
    var mIsKey: String? = null

    override fun clickListener() {

        activity_reset_submit_BUT.setOnClickListener {

            if (Validate()) {
                submitPassword()
            }
        }
    }

    fun Validate(): Boolean {

        if (getETValue(activity_password_edt).isNullOrEmpty()) {

            AppDialogs.showToastDialog(myContext, "Enter password")
            return false

        } else if (getETValue(activity_password_edt).length < 7) {
            AppDialogs.showToastDialog(
                myContext,
                getString(R.string.alert_message_validation_valid_password)
            )
            return false
        } else if (getETValue(activity_confirm_password_edt).isNullOrEmpty()) {
            AppDialogs.showToastDialog(myContext, "Enter confirm password")
            return false
        } else if (getETValue(activity_confirm_password_edt).length < 7) {
            AppDialogs.showToastDialog(
                myContext,
                getString(R.string.alert_message_validation_valid_confirm_password)
            )
            return false
        } else if (!getETValue(activity_confirm_password_edt).equals(
                getETValue(
                    activity_password_edt
                ), false
            )
        ) {
            AppDialogs.showToastDialog(
                myContext,
                getString(R.string.alert_message_validation_match_confirm_password)
            )
            return false
        } else {
            return true
        }
    }


    override fun init() {
        mContext = this
        if (intent.hasExtra("mobileNo"))
            mMobileNo = intent.getStringExtra("mobileNo")
        if (intent.hasExtra("EmailId"))
            mEmail = intent.getStringExtra("EmailId")
        if (intent.hasExtra("isKey"))
            mIsKey = intent.getStringExtra("isKey")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        init()
        clickListener()
    }

    private fun submitPassword() {

        AppDialogs.showProgressDialog(myContext)
        val mCase = getResetPasswordCaseString()
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            this,
            result,
            Request.Method.POST,
            AppServices.API.resetPassword,
            User::class.java
        )

    }


    private fun getResetPasswordCaseString(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("email", mEmail)
            jsonParam1.put("is_key", mIsKey)
            jsonParam1.put("mobile_number", mMobileNo)
            jsonParam1.put("password", getETValue(activity_password_edt))
            val jsonParam = JSONObject()
            jsonParam.put("UpdatePassword", jsonParam1)
            Log.e("UpdatePassword", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onResponse(r: Response?) {
        hideProgress()
        if (r != null) {
            if (r.requestType!! == AppServices.API.resetPassword.hashCode()) {

                if (r.response!!.isSuccess) {
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        object : AppDialogs.ConfirmListener {
                            override fun yes() {
                                startActivity(Intent(myContext, TLSigninActivity::class.java))
                                finish()
                            }
                        },

                        false
                    )
                }else{
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        object : AppDialogs.ConfirmListener {
                            override fun yes() {

                            }
                        },

                        false
                    )
                }
            }
        } else AppDialogs.customOkAction(this, null, TLConstant.SERVER_NOT_REACH, null, null, false)
    }
}
