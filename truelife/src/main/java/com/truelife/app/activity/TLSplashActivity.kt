package com.truelife.app.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.truelife.R
import com.truelife.app.constants.TLConstant
import com.truelife.base.BaseActivity
import com.truelife.chat.utils.IntentUtils
import com.truelife.storage.LocalStorageSP
import com.truelife.util.PermissionChecker
import kotlinx.android.synthetic.main.activity_splash_screen.*

/**
 * Created by Elango on 28/01/19.
 **/

class TLSplashActivity : BaseActivity() {

    private var mContext: Context? = null
    private var mGoButtonAnimation: Animation? = null

    override fun clickListener() {
        go_button.setOnClickListener {
            startActivity(Intent(mContext, TLSigninActivity::class.java))
            finish()
        }
    }

    override fun init() {
        mContext = this
        LocalStorageSP.put(mContext!!, "profile_Screen", "0")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash_screen)
        init()
        clickListener()
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({
            try {
                val user = LocalStorageSP.getLoginUser(this)
                if (user.mFirstName != null) {
                    startActivity(Intent(this, TLDashboardActivity::class.java))
                    finish()
                } else
                    showGoButton()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 1500)
    }

    fun showGoButton() {
        mGoButtonAnimation = AnimationUtils.loadAnimation(mContext, R.anim.scale_image)
        go_button.visibility = View.VISIBLE
        go_button.animation = mGoButtonAnimation
    }


    /*   private void sendRegistrationId() {
        try {
            GCMRegistrar.checkDevice(this);
            GCMRegistrar.checkManifest(this);
            // Retrive the sender ID from GCMIntentService.java
            // Sender ID will be registered into GCMRegistrar
            GCMRegistrar.register(getApplicationContext(),
                    GCMIntentService.SENDER_ID);
        } catch (Exception e) {
            //   Toast.makeText(myContext, "Catch-22", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }*/


}
