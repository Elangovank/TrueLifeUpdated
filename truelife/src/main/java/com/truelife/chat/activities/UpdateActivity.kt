package com.truelife.chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import  com.truelife.R
import com.truelife.chat.activities.main.MainActivity
import com.truelife.chat.events.ExitUpdateActivityEvent
import com.truelife.chat.utils.IntentUtils
import kotlinx.android.synthetic.main.activity_update.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class UpdateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        btn_update.setOnClickListener {
            try {
                startActivity(IntentUtils.getOpenWebsiteIntent(getString(R.string.update_app_link)))
            } catch (e: Exception) {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun exitActivity( data: ExitUpdateActivityEvent){
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        finish()
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }
}