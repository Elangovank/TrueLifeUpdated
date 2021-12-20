package com.truelife.app.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.LocationModel
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.base.progressInterface
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import com.truelife.util.SingleChoiceAdapter
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_tlforgot_password.*
import kotlinx.android.synthetic.main.activity_tlforgot_password.activity_log_in_TXT
import kotlinx.android.synthetic.main.activity_tlforgot_password.activity_signup_EDT_mobile
import kotlinx.android.synthetic.main.activity_tlforgot_password.country_code_EDT
import kotlinx.android.synthetic.main.activity_tlforgot_password.email_img
import kotlinx.android.synthetic.main.activity_tlforgot_password.email_lay
import kotlinx.android.synthetic.main.activity_tlforgot_password.email_or_mob_selection_lay
import kotlinx.android.synthetic.main.activity_tlforgot_password.mobile_img
import kotlinx.android.synthetic.main.activity_tlforgot_password.mobile_lay
import kotlinx.android.synthetic.main.activity_tlforgot_password.selection_email_lay
import kotlinx.android.synthetic.main.activity_tlforgot_password.selection_mobile_lay
import org.json.JSONObject

class TLForgotPassword : BaseActivity(), ResponseListener, progressInterface {
    lateinit var mContext: Context
    var mCountryList = ArrayList<LocationModel>()
    lateinit var mUserId: String
    var mCode: String = ""
    var mKey: String? = null

    override fun clickListener() {

        activity_forgot_submit_BUT.setOnClickListener {
            if(activity_forgot_submit_BUT.text.equals(getString(R.string.the_otp_sent))){
                sendOtp()
            }else if(activity_forgot_submit_BUT.text.equals(getString(R.string.the_otp_submit)))
            verify(getETValue(otp_edt))
        }

        selection_email_lay.setOnClickListener {

            activity_forgot_email_EDT.setText("")
            activity_signup_EDT_mobile.setText("")
            country_code_EDT.setText("")
            email_or_mob_selection_lay.visibility = View.GONE
            email_lay.visibility = View.VISIBLE
            line.visibility = View.VISIBLE
            activity_forgot_email_EDT.requestFocus()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(activity_forgot_email_EDT, InputMethodManager.SHOW_IMPLICIT)
        }

        selection_mobile_lay.setOnClickListener {
            line.visibility = View.VISIBLE
            activity_forgot_email_EDT.setText("")
            activity_signup_EDT_mobile.setText("")
            country_code_EDT.setText("")
            email_or_mob_selection_lay.visibility = View.GONE
            mobile_lay.visibility = View.VISIBLE
            email_lay.visibility = View.GONE

            activity_signup_EDT_mobile.requestFocus()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(activity_signup_EDT_mobile, InputMethodManager.SHOW_IMPLICIT)
        }

        email_img.setOnClickListener {
            activity_forgot_email_EDT.setText("")
            activity_signup_EDT_mobile.setText("")
            country_code_EDT.setText("")
            email_or_mob_selection_lay.visibility = View.GONE
            email_lay.visibility = View.GONE
            mobile_lay.visibility = View.VISIBLE
            activity_signup_EDT_mobile.requestFocus()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(activity_signup_EDT_mobile, InputMethodManager.SHOW_IMPLICIT)
        }


        mobile_img.setOnClickListener {
            email_or_mob_selection_lay.visibility = View.GONE
            mobile_lay.visibility = View.GONE
            email_lay.visibility = View.VISIBLE
            activity_forgot_email_EDT.setText("")
            activity_signup_EDT_mobile.setText("")
            country_code_EDT.setText("")
            activity_forgot_email_EDT.requestFocus()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(activity_signup_EDT_mobile, InputMethodManager.SHOW_IMPLICIT)
        }

        activity_log_in_TXT.setOnClickListener {
            onBackPressed()
        }

        country_code_EDT.setOnClickListener {
            showCountry()
        }


        resend_otp_txt.setOnClickListener {

            sendOtp()
        }


        /*   resend_otp_txt.setOnClickListener {

               val mUser = activity_forgot_email_EDT.text.toString()

               if (mUser.isEmpty()) {
                   AppDialogs.customOkAction(
                       this,
                       getString(R.string.app_name),
                       "Enter a valid email",
                       null,
                       null,
                       true
                   )
                   return@setOnClickListener
               }


               if (Helper.isValidEmail(mUser)) {
                   showProgress()
                   val mCase = getForgotPasswordCaseString(mUser)
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
                       AppServices.API.forgotPass,
                       User::class.java
                   )
                   resend_otp_txt.text= "RESEND OTP"
               } else {
                   AppDialogs.customOkAction(
                       this,
                       getString(R.string.app_name),
                       "Please enter valid email id",
                       null,
                       null,
                       true
                   )

               }
           }*/
        activity_vip_RV.setOnClickListener {
            startActivity(Intent(this, TLCelebrityLogin::class.java))
        }
    }

    override fun init() {
        mContext = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tlforgot_password)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        init()
        clickListener()
    }

    private fun getForgotPasswordCaseString(aEmail: String, aMobile: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("email", aEmail)
            jsonParam1.put("mobile_number",aMobile)
            jsonParam1.put("cuntry_code", mCode + aMobile)
            if (aEmail.isNotEmpty()) {
                mKey = "1"
                jsonParam1.put("is_key", "1")
            } else {
                mKey = "2"
                jsonParam1.put("is_key", "2")
            }
            val jsonParam = JSONObject()
            jsonParam.put("ResetPassword", jsonParam1)
            Log.e("ResetPassword", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun initCountry(list: ArrayList<String>) {
        AppDialogs.showSingleChoice(
            mContext,
            "Country",
            list,
            object : SingleChoiceAdapter.Callback {
                override fun info(position: Int, text: String) {
                    AppDialogs.hideSingleChoice()
                    for (i in mCountryList.indices) {
                        if (text == mCountryList[i].name!!) {
                            country_code_EDT.setText(String.format("+%s", mCountryList[i].code)
                            )
                            mCode = String.format("+%s", mCountryList[i].code)
                        }
                    }

                }
            }, true, isSearch = true
        )
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.forgotPass.hashCode()) {
                if (r.response!!.isSuccess) {
                    val muser = r as User
                    mUserId = muser.mUserId!!
                    AppDialogs.showToastDialog(myContext, r.response!!.responseMessage!!)
                    otp_lay.visibility = View.VISIBLE
                    otp_lay_line.visibility = View.VISIBLE
                    activity_forgot_submit_BUT.text= getString(R.string.the_otp_submit)
                } else {
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
            } else if (r.requestType!! == AppServices.API.OTPVerify.hashCode()) {
                if (r.response!!.isSuccess) {
                    AppDialogs.showToastDialog(myContext, r.response!!.responseMessage!!)

                    val intent = Intent(myContext, TLResetPassword::class.java)
                    intent.putExtra("isKey", mKey)
                    intent.putExtra("mobileNo", getETValue(activity_signup_EDT_mobile))
                    intent.putExtra("EmailId", getETValue(activity_forgot_email_EDT))
                    startActivity(intent)
                    finish()
                } else {
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
            } else if (r.requestType!! == AppServices.API.country.hashCode()) {
                if (r.response!!.isSuccess) {
                    mCountryList = (r as LocationModel).mLocation!!
                    if (mCountryList.isNotEmpty()) {
                        val list = ArrayList<String>()
                        for (i in mCountryList.indices)
                            list.add(mCountryList[i].name!!)
                        initCountry(list)
                    }
                } else AppDialogs.customOkAction(
                    mContext,
                    null,
                    r.response!!.responseMessage!!,
                    null,
                    object : AppDialogs.ConfirmListener {
                        override fun yes() {
                            AppDialogs.hidecustomView()
                        }
                    },
                    false
                )
            }
        } else AppDialogs.customOkAction(this, null, TLConstant.SERVER_NOT_REACH, null, null, false)
    }

    private fun verify(otp: String) {
        if (checkInternet()) {
            AppDialogs.showProgressDialog(mContext)
            val result = Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getOTPVerifyParam(otp)!!
            )
            AppServices.execute(
                mContext, this,
                result,
                Request.Method.POST,
                AppServices.API.OTPVerify,
                Response::class.java
            )
        }
    }

    private fun getOTPVerifyParam(otp: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("login_user_id", mUserId)
            jsonObject.put("otp", otp)
            jsonObject.put("status", "1")
            val jsonParam = JSONObject()
            jsonParam.put("OTPVerify", jsonObject)

            Log.i("OTPVerify --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun getListParam(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("category", "0")
            jsonObject.put("category_id", "0")
            jsonObject.put("state_id", "0")
            val jsonParam = JSONObject()
            jsonParam.put("GetAddressList", jsonObject)

            Log.i("GetAddressList --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun showCountry() {
        if (checkInternet()) {
            AppDialogs.showProgressDialog(mContext)
            val mCase = getListParam()
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                mContext, this,
                result,
                Request.Method.POST,
                AppServices.API.country,
                LocationModel::class.java
            )
        }
    }

    private fun sendOtp(){

        if (getETValue(activity_forgot_email_EDT).isEmpty() && getETValue(
                activity_signup_EDT_mobile
            ).isEmpty()
        ) {
            AppDialogs.customOkAction(
                this,
                getString(R.string.app_name),
                getString(R.string.alert_message_validation_empty_email_or_mobile),
                null,
                null,
                true
            )

        } else if (getETValue(activity_forgot_email_EDT).length > 0 && !Helper.isValidEmail(
                getETValue(activity_forgot_email_EDT)
            )
        ) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_valid_email),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                    }
                },
                false
            )

        } else if (getETValue(activity_signup_EDT_mobile).length > 0 && !Helper.isValidMobileNo(
                getETValue(activity_signup_EDT_mobile)
            )
        ) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_message_validation_valid_mobile),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                    }
                },
                false
            )

        } else if (getETValue(activity_signup_EDT_mobile).length > 0 && mCode.isEmpty()) {
            AppDialogs.customOkAction(
                mContext,
                null,
                getString(R.string.alert_country_code),
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                    }
                },
                false
            )

        } else {
            AppDialogs.showProgressDialog(myContext)
            val mCase = getForgotPasswordCaseString(
                getETValue(activity_forgot_email_EDT),
                getETValue(activity_signup_EDT_mobile)
            )
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
                AppServices.API.forgotPass,
                User::class.java
            )

        }
    }

}
