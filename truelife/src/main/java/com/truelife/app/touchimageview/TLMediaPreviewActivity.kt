package com.truelife.app.touchimageview

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.truelife.R
import com.truelife.app.fragment.chat.message.TLFeedDetailsVideo
import com.truelife.app.fragment.chat.message.TLScreenFeedPhotoView
import com.truelife.base.BaseActivity

/**
 * Created by Elango on 21-02-2020.
 */

class TLMediaPreviewActivity : BaseActivity(), View.OnClickListener {

    lateinit var mContext: FragmentActivity
    lateinit var mClose: TextView

    companion object {
        var TAG: String = TLMediaPreviewActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_preview_dialog)
        mContext = this
        init()
    }

    override fun init() {
        mClose = findViewById(R.id.close_club_button)
        initData()
        clickListener()
    }

    private fun initData() {
        val mFragmentManager = mContext.supportFragmentManager
        val mFragmentTransaction = mFragmentManager.beginTransaction()
        val mFragment = TLScreenFeedPhotoView()
        mFragment.arguments = intent.getBundleExtra("bundle")
        mFragmentTransaction.add(R.id.frame_container, mFragment)
        mFragmentTransaction.commit()
    }

    override fun clickListener() {
        mClose.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view) {
            mClose -> finish()
        }
    }
}