package com.truelife.app.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import com.android.volley.Request
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.base.progressInterface
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import kotlinx.android.synthetic.main.activity_tlsignin.*
import org.json.JSONObject

class TLSigninActivity : BaseActivity(), ResponseListener, progressInterface {


    lateinit var mContext: Context
    var myFirebaseToken: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tlsignin)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        mContext = this

        val intenttData = intent.extras
        intenttData?.let {
            if (it.containsKey("alreadyUser")) {

            } else {
                val number = it.getString("number")

                val user = LocalStorageSP.getLoginUser(mContext)
                if (user.mUserId.isNullOrEmpty())
                    callLoginwithRelayService(number!!)
                else {
                    startActivity(Intent(this, TLDashboardActivity::class.java))
                    finish()
                }
            }
        }


        init()
        clickListener()
    }

    private fun getToken() {

        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener(this,
                OnSuccessListener<InstanceIdResult> { instanceIdResult ->
                    Log.e("newToken", instanceIdResult.token)
                    myFirebaseToken = instanceIdResult.token
                })
    }

    override fun clickListener() {

        activity_sign_up_txt.setOnClickListener {
            startActivity(Intent(mContext, TLSignupActivity::class.java))
        }

        activity_forgot_password_txt.setOnClickListener {
            startActivity(Intent(mContext, TLForgotPassword::class.java))
        }

        activity_vip_RV.setOnClickListener {
            startActivity(Intent(mContext, TLCelebrityLogin::class.java))
        }
        signin_button.setOnClickListener {


            val mUser = fragment_signin_EDT_username.text.toString()
            val mPass = fragment_signin_EDT_password.text.toString()

            if (mUser.isEmpty()) {
                AppDialogs.customOkAction(
                    this,
                    getString(R.string.app_name),
                    "Please enter email",
                    null,
                    null,
                    true
                )
            } else if (mPass.isEmpty()) {
                AppDialogs.customOkAction(
                    this,
                    getString(R.string.app_name),
                    "Please enter your password",
                    null,
                    null,
                    true
                )
            } else if (!Helper.isValidEmail(mUser) && !Helper.isValidMobileNo(mUser)) {
                AppDialogs.customOkAction(
                    this,
                    getString(R.string.app_name),
                    "Enter a valid email or Mobile No",
                    null,
                    null,
                    true
                )

            } else {
                AppDialogs.showProgressDialog(mContext)
                val mCase = getLoginCaseString(mUser, mPass)
                val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
                Log.e("URL", result)
                AppServices.login(this, result)
            }
        }

    }

    override fun init() {
        getToken()
    }

    private fun callLoginwithRelayService(
        mobileNo: String = "",
    ) {
        val aMobileNo = LocalStorageSP.get(
            mContext,
            TLConstant.SELECTED_MOBILE_NUMBER, ""
        )!!
        val aCC = LocalStorageSP.get(
            mContext,
            TLConstant.SELECTED_COUNTRY_CODE, ""
        )!!
        if (aMobileNo.length > 0 && aCC.length > 0) {
            AppDialogs.showProgressDialog(mContext)
            val mCase = getLoginWithRelayMobileNumber(
                aMobileNo, aCC
            )
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            Log.e("URL", result)
            AppServices.loginWithRelay(this, result)
        } else {
            AppDialogs.showSnackbar(
                activity_sign_up_txt,
                getString(R.string.error_mob_country_code)
            )
        }
    }


    private fun getLoginCaseString(
        aEmail: String,
        aPassword: String
    ): String? {
        var aCaseStr = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("username", aEmail)
            jsonParam1.put("password", aPassword)
            val jsonParam = JSONObject()
            jsonParam.put("Login", jsonParam1)
            Log.e("GetLoginCase", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            Log.e("GetLoginCase", " $aCaseStr")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun getLoginWithRelayMobileNumber(
        aMobile: String = "",
        aCountryCode: String = ""
    ): String? {
        var aCaseStr = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("country_code", aCountryCode)
            jsonParam1.put("mobile_number", aMobile)
            jsonParam1.put("sendOTP", "0")
            val jsonParam = JSONObject()
            jsonParam.put("LoginWithOTP", jsonParam1)
            Log.e("LoginWithOTP", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            Log.e("LoginWithOTP", " $aCaseStr")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onResponse(r: Response?) {
        runOnUiThread {
            AppDialogs.hideProgressDialog()
        }

        if (r != null) {
            if (r.requestType!! == AppServices.API.login.hashCode()) {
                if (r.response!!.isSuccess) {
                    val user = (r as User).mUserdetails
                    registerGCM(user!!.mUserId!!)
                    LocalStorageSP.storeLoginUser(this, user)
                    startActivity(Intent(this, TLDashboardActivity::class.java))
                    finish()
                } else AppDialogs.showToastDialog(this, r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.loginwithRelay.hashCode()) {
                if (r.response!!.isSuccess) {
                    val user = (r as User).mUserdetails
                    registerGCM(user!!.mUserId!!)
                    LocalStorageSP.storeLoginUser(this, user)
                    startActivity(Intent(this, TLDashboardActivity::class.java))
                    finish()
                } else AppDialogs.showToastDialog(this, r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.gcmRegister.hashCode()) {
                if (r.response!!.isSuccess) {
                    startActivity(Intent(this, TLDashboardActivity::class.java))
                    finish()
                } else AppDialogs.showToastDialog(this, r.response!!.responseMessage!!)
            }
        } else AppDialogs.customOkAction(this, null, TLConstant.SERVER_NOT_REACH, null, null, false)
    }

    fun registerGCM(userId: String) {

        // AppDialogs.showProgressDialog(mContext)
        Thread(Runnable {
            val result = Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getGcmRegistrationString(userId)!!
            )
            AppServices.execute(
                mContext, this,
                result,
                Request.Method.POST,
                AppServices.API.gcmRegister,
                Response::class.java
            )
        }).start()

    }

    private fun getGcmRegistrationString(
        aUserId: String
    ): String? {
        var aCaseStr = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("device_type", "1")
            jsonParam1.put("device_imei", "")
            jsonParam1.put("device_token", myFirebaseToken)
            val jsonParam = JSONObject()
            jsonParam.put("GcmRegistration", jsonParam1)
            Log.e("GcmRegistration", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            Log.e("GcmRegistration", " $aCaseStr")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


}
