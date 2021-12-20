package com.truelife.app.activity

import android.os.Bundle
import com.truelife.R
import com.truelife.base.BaseActivity
import kotlinx.android.synthetic.main.activity_tlcelebrity_login.*

class TLCelebrityLogin : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tlcelebrity_login)
        init()
    }

    override fun clickListener() {

        back_btn.setOnClickListener {
            finish()
        }
    }

    override fun init() {
        clickListener()
    }
}
