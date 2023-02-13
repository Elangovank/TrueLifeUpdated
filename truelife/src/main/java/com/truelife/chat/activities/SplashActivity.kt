package com.truelife.chat.activities

import android.app.DownloadManager
import android.content.Context
import com.truelife.chat.utils.network.FireManager.Companion.isLoggedIn
import com.truelife.storage.LocalStorageSP.getLoginUser
import android.os.Bundle
import com.truelife.R
import com.truelife.chat.utils.SharedPreferencesManager
import android.content.Intent
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.api.AppServices
import com.truelife.chat.activities.authentication.AuthenticationActivity
import com.truelife.app.activity.TLDashboardActivity
import com.truelife.app.model.AccountSettingModel
import com.truelife.app.model.AppUpdateModel
import com.truelife.chat.activities.AgreePrivacyPolicyActivity
import com.truelife.chat.activities.main.MainActivity
import com.truelife.chat.activities.setup.SetupUserActivity
import com.truelife.chat.utils.DetachableClickListener
import com.truelife.chat.utils.PermissionsUtil
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import com.truelife.util.Util
import com.truelife.util.Utility
import org.json.JSONObject

//this is the First Activity that launched when user starts the App
class SplashActivity : AppCompatActivity(), ResponseListener {
    private var isFromChatClick = false
    private var mContext: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme) //revert back to default theme after loading splash image
        setContentView(R.layout.activity_splash)
        mContext = this
        val intent = intent.extras
        if (intent != null) if (intent.containsKey("isFromChatClick")) {
            isFromChatClick = intent.getBoolean("isFromChatClick")
        }

    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            PermissionsUtil.permissions,
            PERMISSION_REQUEST_CODE
        )
    }

    private fun startLoginActivity() {
        val user = getLoginUser(this)
        if (user.mUserId == null || user.mUserId.isEmpty()) {
            val intent = Intent(this, AuthenticationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        } else {
            val intent = Intent(this, TLDashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
        finish()
    }

    private fun startPrivacyPolicyActivity() {
        val intent = Intent(this, AgreePrivacyPolicyActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun startNextActivity() {
        if (!SharedPreferencesManager.hasAgreedToPrivacyPolicy()) {
            startPrivacyPolicyActivity()
        } else if (SharedPreferencesManager.isUserInfoSaved()) {
            val user = getLoginUser(this)
            if (user.mMobileNumber != null) {
                if (isFromChatClick) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, TLDashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                startLoginActivity()
            }
        } else {
            val intent = Intent(this, SetupUserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showAlertDialog() {
        val positiveClickListener =
            DetachableClickListener.wrap { dialogInterface, i -> requestPermissions() }
        val negativeClickListener = DetachableClickListener.wrap { dialogInterface, i -> finish() }
        val builder = AlertDialog.Builder(this)
            .setTitle(R.string.missing_permissions)
            .setMessage(R.string.you_have_to_grant_permissions)
            .setPositiveButton(R.string.ok, positiveClickListener)
            .setNegativeButton(R.string.no_close_the_app, negativeClickListener)
            .create()

        //avoid memory leaks
        positiveClickListener.clearOnDetach(builder)
        negativeClickListener.clearOnDetach(builder)
        builder.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionsUtil.permissionsGranted(grantResults)) {
            if (!isLoggedIn()) startLoginActivity() else startNextActivity()
        } else showAlertDialog()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 451
    }

    override fun onResume() {
        super.onResume()
      checkUpdate()
    }

    fun checkUpdate() {
        AppDialogs.showProgressDialog(mContext!!)
        val result = Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
            getAppVersionCaseString()!!
        )
        AppServices.execute(
                mContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.appupdate,
                AppUpdateModel::class.java
        )
    }

    private fun getAppVersionCaseString(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("device_id", "1")
            val jsonParam = JSONObject()
            jsonParam.put("appVersion", jsonParam1)
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onResponse(r: Response?) {
        try {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.appupdate.hashCode()) {
                if (r.response!!.isSuccess) {
                    val res = r.response?.appVersion as AppUpdateModel
                    if(res.versionCode!!.toInt() > BuildConfig.VERSION_CODE ){
                        AppDialogs.customOkAction(
                            this,
                            getString(R.string.app_update_available_title),
                            getString(R.string.app_update_available_msg),
                            "Update",
                            object : AppDialogs.ConfirmListener{
                                override fun yes(){
                                    Helper.navigatePlaystore(mContext!!)
                                }

                            },
                            false
                        )
                    }
                    else{
                        splashscreenFlow()
                    }

                }
            }
        }
        }
        catch (e : Exception){
            splashscreenFlow()
        }
    }

    private fun splashscreenFlow(){
        if (!SharedPreferencesManager.hasAgreedToPrivacyPolicy()) {
            startPrivacyPolicyActivity()
            //check if user isLoggedIn
        } else if (!isLoggedIn()) {
            startLoginActivity()
            //request permissions if there are no permissions granted
        } else if (isLoggedIn() && !PermissionsUtil.hasPermissions(this)) {
            requestPermissions()
        } else {
            startNextActivity()
        }
    }
}