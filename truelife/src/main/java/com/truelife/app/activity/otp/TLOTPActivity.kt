package com.truelife.app.activity.otp

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.LocationModel
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import com.truelife.util.SingleChoiceAdapter
import org.json.JSONObject


/**
 * Created by Elango on 04-03-2020.
 */

class TLOTPActivity : BaseActivity(), ResponseListener {

    private var mContext: Context? = null
    lateinit var mOTPFirstDialog: View
    lateinit var mOTPSecondDialog: View
    lateinit var mCountry: TextView
    var mMessage = ""
    var mCode = "+91"
    var mNumber = "-"
    var mCountryList = ArrayList<LocationModel>()
    lateinit var mMobile: EditText
    companion object {
        var TAG: String = TLOTPActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        mContext = this
        init()
    }

    override fun init() {
        initBundle()
        showOTPRequest()
    }

    private fun initBundle() {
        try {
            val intent = intent
            if (intent != null && intent.extras != null)
                mMessage = intent.getStringExtra("msg")!!
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun showOTPRequest() {
        AppDialogs.hidecustomView()
        if (checkInternet()) {
            mOTPFirstDialog =
                AppDialogs.showcustomView(
                    mContext!!, R.layout.fragment_otp_request, false,
                    fullScreen = true
                )!!

            mOTPFirstDialog.findViewById<ImageView>(R.id.close_club_button).setOnClickListener {
                finish()
                AppDialogs.hidecustomView()
            }

            mCountry = mOTPFirstDialog.findViewById(R.id.country_select)

            if (!LocalStorageSP.getLoginUser(mContext).countryCode.isNullOrEmpty()) {
                mCountry.text = LocalStorageSP.getLoginUser(mContext).countryCode
            }
            mOTPFirstDialog.findViewById<TextView>(R.id.otp_content).text = mMessage

            mCountry.setOnClickListener {
                showCountry()
            }

            mMobile = mOTPFirstDialog.findViewById<EditText>(R.id.mobile_no)

            if (!LocalStorageSP.getLoginUser(mContext).mMobileNumber.isNullOrEmpty())
                mMobile.setText(LocalStorageSP.getLoginUser(mContext).mMobileNumber)
            mOTPFirstDialog.findViewById<Button>(R.id.send_otp_btn).setOnClickListener {
                when {
                    getETValue(mMobile).isEmpty() -> showAlert(
                        mMobile,
                        getString(R.string.alert_otp_mobile_no)
                    )
                    getETValue(mMobile).length < 10 -> showAlert(
                        mMobile,
                        getString(R.string.alert_otp_mobile_no_validate)
                    )
                    (mCode.isEmpty()) -> showAlert(
                        mMobile,
                        getString(R.string.alert_otp_country_code_validate)
                    )
                    else -> requestOTP(mCode, getETValue(mMobile))
                }
            }
        }
    }

    private fun showCountry() {
        if (checkInternet()) {
            AppDialogs.showProgressDialog(mContext!!)
            val mCase = getListParam()
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                mContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.country,
                LocationModel::class.java
            )
        }
    }

    private fun requestOTP(code: String, number: String) {
        if (checkInternet()) {
            AppDialogs.showProgressDialog(mContext!!)
            val result = Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getOTPRequestParam(code, number)!!
            )
            AppServices.execute(
                mContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.OTPRequest,
                Response::class.java
            )
        }
    }

    private fun showAlert(editText: EditText, msg: String) {
        editText.error = msg
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

    private fun getOTPRequestParam(code: String, number: String): String? {
        var aCaseStr: String? = " "
        try {
            mNumber = number
            val jsonObject = JSONObject()
            jsonObject.put("login_user_id", LocalStorageSP.getLoginUser(mContext).mUserId)
            jsonObject.put("mobile_number", number)
            jsonObject.put("country_code", code)
            val jsonParam = JSONObject()
            jsonParam.put("OTPRequest", jsonObject)

            Log.i("OTPRequest --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun getOTPVerifyParam(otp: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("login_user_id", LocalStorageSP.getLoginUser(mContext).mUserId)
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

    override fun clickListener() {

    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.country.hashCode()) {
                if (r.response!!.isSuccess) {
                    mCountryList = (r as LocationModel).mLocation!!
                    if (mCountryList.isNotEmpty()) {
                        val list = ArrayList<String>()
                        for (i in mCountryList.indices)
                            list.add(mCountryList[i].name!!)
                        initCountry(list)
                    }
                } else AppDialogs.customOkAction(
                    mContext!!,
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
            } else if (r.requestType!! == AppServices.API.OTPRequest.hashCode()) {
                AppDialogs.showToastshort(mContext!!, r.response!!.responseMessage!!)
                if (r.response!!.isSuccess)
                    showOTPVerify()
            } else if (r.requestType!! == AppServices.API.OTPVerify.hashCode()) {
                if (r.response!!.isSuccess) {
                    AppDialogs.customOkAction(
                        mContext!!,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        object : AppDialogs.ConfirmListener {
                            override fun yes() {
                                val user = LocalStorageSP.getLoginUser(mContext)
                                user.mIsMessengerMobileVerified = "1"
                                user.mIsMobileVerified = "1"
                                LocalStorageSP.storeLoginUser(mContext!!, user)
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        },
                        false
                    )
                } else
                    AppDialogs.showToastshort(mContext!!, r.response!!.responseMessage!!)
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

    private fun showOTPVerify() {
        if (checkInternet()) {
            mOTPSecondDialog =
                AppDialogs.showcustomView(
                    mContext!!, R.layout.fragment_otp_verify, false,
                    fullScreen = true
                )!!

            mOTPSecondDialog.findViewById<ImageView>(R.id.close_club_button).setOnClickListener {
                finish()
                AppDialogs.hidecustomView()
            }

            val mOTP = mOTPSecondDialog.findViewById<EditText>(R.id.otp_send)
            val mCount = mOTPSecondDialog.findViewById<TextView>(R.id.counter_time_TET)
            val mResendOTP = mOTPSecondDialog.findViewById<Button>(R.id.resend_otp_btn)

            mOTPSecondDialog.findViewById<TextView>(R.id.otp_receive_mobile_no).text = mNumber
            mOTPSecondDialog.findViewById<TextView>(R.id.change_number).setOnClickListener {
                AppDialogs.hidecustomView()
                showOTPRequest()
            }

            mResendOTP.setOnClickListener {
                when {
                    getETValue(mMobile).isEmpty() -> showAlert(
                        mMobile,
                        getString(R.string.alert_otp_mobile_no)
                    )
                    getETValue(mMobile).length < 10 -> showAlert(
                        mMobile,
                        getString(R.string.alert_otp_mobile_no_validate)
                    )
                    (mCode.isEmpty()) -> showAlert(
                        mMobile,
                        getString(R.string.alert_otp_country_code_validate)
                    )
                    else -> requestOTP(mCode, getETValue(mMobile))
                }
            }

            CountDownTimer(mCount, mResendOTP)

            mOTPSecondDialog.findViewById<Button>(R.id.otp_submit).setOnClickListener {
                when {
                    getETValue(mOTP).isEmpty() -> showAlert(
                        mOTP,
                        getString(R.string.label_enter_the_otp)
                    )
                    getETValue(mOTP).length < 5 -> showAlert(
                        mOTP,
                        getString(R.string.label_enter_the_otp_validate)
                    )
                    else -> verify(getETValue(mOTP))
                }
            }
        }
    }

    private fun CountDownTimer(mCount: TextView, mResendOTP: Button) {
        val animBlink = AnimationUtils.loadAnimation(myContext, R.anim.blink)
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mCount.startAnimation(animBlink)
                mResendOTP.visibility = View.GONE
                mCount.visibility = View.VISIBLE
                mCount.text = "RESEND OTP Enable in 00:" + millisUntilFinished / 1000
            }

            override fun onFinish() {
                mCount.clearAnimation()
                mCount.visibility = View.GONE
                mResendOTP.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun verify(otp: String) {
        if (checkInternet()) {
            AppDialogs.showProgressDialog(mContext!!)
            val result = Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getOTPVerifyParam(otp)!!
            )
            AppServices.execute(
                mContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.OTPVerify,
                Response::class.java
            )
        }
    }

    private fun initCountry(list: ArrayList<String>) {
        AppDialogs.showSingleChoice(
            mContext!!,
            "Country",
            list,
            object : SingleChoiceAdapter.Callback {
                override fun info(position: Int, text: String) {
                    AppDialogs.hideSingleChoice()
                    for (i in mCountryList.indices) {
                        if (text == mCountryList[i].name!!) {
                            mCountry.text = String.format(
                                "%s (%s)",
                                mCountryList[i].name,
                                mCountryList[i].code
                            )
                            mCode = String.format("+%s", mCountryList[i].code)
                        }
                    }

                }
            }, true, isSearch = true
        )
    }

}