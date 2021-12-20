package com.truelife.app.activity

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.truelife.R
import com.truelife.base.BaseActivity
import kotlinx.android.synthetic.main.app_chat_header.*

class AboutTrueLife : BaseActivity() {

    var mContext: FragmentActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_true_life)
        init()
        setToolBar()
        clickListener()
    }


    override fun clickListener() {
        app_header_back_arrow.visibility = View.VISIBLE
        app_header_menu.visibility = View.GONE
        app_header_back_arrow.setOnClickListener {
            finish()
        }
    }

    override fun init() {
        mContext = this
    }

    fun setToolBar() {
        app_header_title!!.text = "About True Life"
        app_header_title!!.typeface = ResourcesCompat.getFont(mContext!!, R.font.opensans_semibold)
        app_header_title!!.textSize = 18f
    }
}