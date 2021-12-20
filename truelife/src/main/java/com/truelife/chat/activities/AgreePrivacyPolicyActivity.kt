package com.truelife.chat.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.webkit.WebView
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.truelife.R

import com.truelife.chat.activities.authentication.AuthenticationActivity
import com.truelife.chat.activities.main.MainActivity
import com.truelife.chat.activities.setup.SetupUserActivity
import com.truelife.chat.adapters.IntroSliderAdapter
import com.truelife.chat.model.SliderModel
import com.truelife.chat.utils.DetachableClickListener
import com.truelife.chat.utils.PermissionsUtil
import com.truelife.chat.utils.SharedPreferencesManager
import com.truelife.chat.utils.network.FireManager
import kotlinx.android.synthetic.main.activity_agree_privacy_policy.*
import java.util.*


class AgreePrivacyPolicyActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 451
    private var listItems: ArrayList<SliderModel>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agree_privacy_policy)

        // Make a copy of the slides you'll be presenting.
        listItems = ArrayList<SliderModel>()
        listItems!!.add(SliderModel(R.drawable.chat_intro, "Quick & Smooth Instant Messaging", "Send and receive instant messages and enjoy our incredibly fine-tuned app to communicate with your contacts"))
        listItems!!.add(SliderModel(R.drawable.call_intro, "Audio Calling", "Enjoy clean and sharp audio calls"))
        listItems!!.add(SliderModel(R.drawable.video_intro, "Video Calling", " You can also make use of high quality video calls to connect with your contacts"))
        listItems!!.add(SliderModel(R.drawable.ic_wifi_msg, "Wi-Msg", "Send and receive messages, videos and images with the people near you, without using the internet!"))
        val itemsPager_adapter = IntroSliderAdapter(this, listItems)
        my_pager.setAdapter(itemsPager_adapter)

        my_pager.setOnTouchListener(OnTouchListener { v, event -> true })

        btn_agree.setOnClickListener {
            if (my_pager.currentItem == 3) {
                //showContactsConfirmationDialog()
                SharedPreferencesManager.setAgreedToPrivacyPolicy(true)
                if (!FireManager.isLoggedIn())
                    startLoginActivity()
                else
                    startNextActivity()
            } else {
                my_pager.setCurrentItem(my_pager.currentItem + 1, true)
            }
        }
        // The_slide_timer
        /*Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread(Runnable {
                    if (my_pager.getCurrentItem() < listItems!!.size - 1) {
                        my_pager.setCurrentItem(my_pager.getCurrentItem() + 1)
                    } else my_pager.setCurrentItem(0)
                })
            }
        }, 3000,3000)*/
        my_tablayout.setupWithViewPager(my_pager, true)
    }

    private fun showContactsConfirmationDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Terms and Conditions")
        dialog.setCancelable(false)

        val view = LayoutInflater.from(this).inflate(R.layout.privacy_policy_dialog, null, false)
        dialog.setView(view)
       // val tv = view.findViewById<TextView>(R.id.tv_privacy_policy_dialog)
        val checkBox = view.findViewById<CheckBox>(R.id.chb_agree)
        checkBox.text = "By Checking this, You agree to the collection and use of information in accordance with this Privacy Policy"
        val wv: WebView
        wv = view.findViewById<WebView>(R.id.webview_privacy_policy_dialog)
        wv.loadUrl("file:///android_asset/terms_and_condition.html")
        //getHtml4(tv)
        dialog.setNegativeButton("DECLINE", null)
        dialog.setPositiveButton("AGREE") { dialog, which ->
            SharedPreferencesManager.setAgreedToPrivacyPolicy(true)
            if (!FireManager.isLoggedIn())
                startLoginActivity()
            else
                startNextActivity()
        }
        val mDialog = dialog.show()
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = isChecked
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, PermissionsUtil.permissions, PERMISSION_REQUEST_CODE)
    }

    private fun startPrivacyPolicyActivity() {
        val intent = Intent(this, AgreePrivacyPolicyActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun getHtml4(textView: TextView) {
        val html = resources.getString(R.string.privacy_policy_html)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT))
        } else {
            textView.setText(Html.fromHtml(html))
        }
    }

    private fun startLoginActivity() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun startNextActivity() {
        if (SharedPreferencesManager.isUserInfoSaved()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, SetupUserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionsUtil.permissionsGranted(grantResults)) {
            if (!FireManager.isLoggedIn())
                startLoginActivity()
            else
                startNextActivity()
        } else
            showAlertDialog()
    }

    private fun showAlertDialog() {
        val positiveClickListener = DetachableClickListener.wrap { dialogInterface, i -> requestPermissions() }
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
}
