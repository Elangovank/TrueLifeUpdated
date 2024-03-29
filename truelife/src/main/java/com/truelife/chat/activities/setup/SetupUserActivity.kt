package com.truelife.chat.activities.setup

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.canhub.cropper.CropImage
import com.google.android.gms.tasks.OnSuccessListener
import com.truelife.BuildConfig
import  com.truelife.R
import com.truelife.chat.activities.main.MainActivity
import com.truelife.chat.exceptions.BackupFileMismatchedException
import com.truelife.chat.exceptions.NoDefaultImageException
import com.truelife.chat.utils.*
import com.truelife.chat.utils.MediaStoreUtil.getBitmapByUri
import com.truelife.chat.utils.keyboard.KeyboardHelper
import com.truelife.chat.utils.network.FireManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.truelife.api.AppServices
import com.truelife.app.activity.TLDashboardActivity
import com.truelife.app.activity.TLSigninActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.User
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import io.reactivex.disposables.CompositeDisposable
import io.realm.exceptions.RealmMigrationNeededException
import kotlinx.android.synthetic.main.activity_setup_user.*
import kotlinx.android.synthetic.main.activity_tlsignin.*
import kotlinx.android.synthetic.main.flag_item.*
import kotlinx.android.synthetic.main.fragment_verify_phone.*
import org.json.JSONObject
import java.io.IOException

class SetupUserActivity : AppCompatActivity(), ResponseListener {

     val disposables = CompositeDisposable()
    val fireManager = FireManager()
    private var choosenPhoto: String? = null
    private var progressDialog: ProgressDialog? = null

    private val viewModel: SetupUserViewModel by viewModels()
     private lateinit var mContext : Context
    var myFirebaseToken: String = ""

    private fun getToken() {

        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener(this,
                OnSuccessListener<InstanceIdResult> { instanceIdResult ->
                    Log.e("newToken", instanceIdResult.token)
                    myFirebaseToken = instanceIdResult.token
                })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_user)
        mContext = this
        getToken()
        viewModel.completeSetupLiveData.observe(this, androidx.lifecycle.Observer { pair ->
            progressDialog?.dismiss()
            val isSuccess = pair.first
            val throwable = pair.second
            if (isSuccess) {
                startMainActivity()
            } else {
                if (throwable != null) {
                    if (throwable is NoDefaultImageException && BuildConfig.DEBUG) {
                        Toast.makeText(this, "Please upload Default User Image", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        showSnackbar()
                    }
                }
            }
        })


        viewModel.loadUserImageLiveData.observe(this, androidx.lifecycle.Observer { photoUrl ->
            //incase of backup enabled, uncomment this line
            //    loadUserPhoto(photoUrl)
        })

        fab_setup_user.setOnClickListener {
            KeyboardHelper.hideSoftKeyboard(this, et_username_setup)
            validate()
        }
        user_img_setup.setOnClickListener { pickImage() }

        //On Done Keyboard Button Click
        et_username_setup.setOnEditorActionListener(TextView.OnEditorActionListener
        { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validate()
                return@OnEditorActionListener true
            }
            false
        })


        /* if (RealmBackupRestore.isBackupFileExists()) {
             check_text_view_number.visibility = View.VISIBLE
         } else {
             check_text_view_number.visibility = View.GONE
         }*/

        viewModel.fetchUserImage()

    }

    private fun validate(){
        if (TextUtils.isEmpty(et_username_setup.text.toString())) {
            et_username_setup.error = getString(R.string.username_is_empty)
        }else{
            showPopup()
        }
    }
    private fun loadUserPhoto(photoUrl: String) {
        //load the image
        //we are using listener to determine when the image loading is finished
        //so we can hide the progressBar

        Glide.with(this@SetupUserActivity).load(photoUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progress_bar_setup_user_img.visibility = View.GONE
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    progress_bar_setup_user_img.visibility = View.GONE
                    return false;
                }

            }).into(user_img_setup)
    }

    fun startPersonalDataUpdate(value : String) {
        disposables.add(
            fireManager.updateMyPersonalIdentifiables(value).subscribe({
                completeSetup()
            }
            ) { throwable: Throwable? ->
                Toast.makeText(
                    this,
                    R.string.no_internet_connection,
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    fun showPopup() {
        AlertDialog.Builder(this).apply {
            setMessage(R.string.question_agree_personal_data_access)
            setNegativeButton(R.string.don_not_consent) { _, _ ->
                startPersonalDataUpdate("0")

            }
            setPositiveButton(R.string.label_consent) { _, _ ->
                startPersonalDataUpdate("1")
            }
            show()
        }
    }

    private fun completeSetup() {
        //check if user not entered his username
        if (TextUtils.isEmpty(et_username_setup.text.toString())) {
            et_username_setup.error = getString(R.string.username_is_empty)
        } else {

            progressDialog = ProgressDialog(this@SetupUserActivity)
            progressDialog?.setMessage(getString(R.string.loading))
            progressDialog?.setCancelable(false)
            progressDialog?.show()

            if (check_text_view_number.visibility == View.VISIBLE && check_text_view_number.isChecked) {
                try {
                    RealmBackupRestore(this).restore()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, R.string.error_restoring_backup, Toast.LENGTH_SHORT).show()
                } catch (e: RealmMigrationNeededException) {
                    e.printStackTrace()
                    Toast.makeText(this, R.string.error_restoring_backup, Toast.LENGTH_SHORT).show()
                } catch (e: BackupFileMismatchedException) {
                    e.printStackTrace()
                    Toast.makeText(this, R.string.backup_file_mismatched, Toast.LENGTH_SHORT).show()
                }

            }
            viewModel.completeSetup(choosenPhoto, et_username_setup.text.toString())

        }
    }


    private fun showSnackbar() {
        Snackbar.make(
            findViewById(android.R.id.content),
            R.string.no_internet_connection,
            Snackbar.LENGTH_SHORT
        ).show()
    }


    private fun pickImage() {
        CropImageRequest.getCropImageRequest().start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result?.uri ?: return


                val file = DirManager.getMyPhotoPath()
                try {
                    //copy image to the App Folder
                    val bitmap = getBitmapByUri(this, resultUri)

                    if (bitmap == null) {
                        Toast.makeText(this, "could not get file", Toast.LENGTH_SHORT).show()
                        return
                    }


                    BitmapUtils.convertBitmapToJpeg(bitmap, file)

                    Glide.with(this).load(file).into(user_img_setup!!)
                    choosenPhoto = file.path
                    progress_bar_setup_user_img!!.visibility = View.GONE
                } catch (e: IOException) {
                    e.printStackTrace()
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, R.string.could_not_get_this_image, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun startMainActivity() {

        callLoginwithRelayService()
       /* val intent = Intent(this, TLSigninActivity::class.java)
        intent.putExtra("number",SharedPreferencesManager.getCurrentUser().phone)
        intent.putExtra("name",SharedPreferencesManager.getCurrentUser().properUserName)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()*/
    }

    override fun onResume() {
        super.onResume()
        if (SharedPreferencesManager.isCurrentUserInfoSaved()) {
            progressDialog?.dismiss()

            startMainActivity()
        }
    }

    override fun onPause() {
        super.onPause()

        progressDialog?.dismiss()
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

    private fun getLoginWithRelayMobileNumber(
        aMobile: String = "",
        aCountryCode: String = ""
    ): String? {
        var aCaseStr = " "
        try {
            val name = SharedPreferencesManager.getCurrentUser().properUserName
            val jsonParam1 = JSONObject()
            jsonParam1.put("country_code", aCountryCode)
            jsonParam1.put("mobile_number", aMobile)
            jsonParam1.put("name", name)
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

