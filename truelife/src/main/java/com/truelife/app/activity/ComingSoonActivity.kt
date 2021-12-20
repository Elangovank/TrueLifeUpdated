package com.truelife.app.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.truelife.R
import com.truelife.base.BaseActivity

class ComingSoonActivity : BaseActivity() {

    lateinit var mTitle: TextView
    lateinit var mBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coming_soon)
        init()
    }

    override fun clickListener() {
        mBack.setOnClickListener() {
            finish()
        }
    }

    override fun init() {
        mTitle = findViewById(R.id.chat_title)
        mBack = findViewById(R.id.back_icon)
        if (intent != null)
            mTitle.text = intent.getStringExtra("title")

        clickListener()
    }
}
