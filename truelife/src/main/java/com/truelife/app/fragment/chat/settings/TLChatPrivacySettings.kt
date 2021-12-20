package com.truelife.app.fragment.chat.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.truelife.R
import com.truelife.app.activity.TLChatActivity
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import kotlinx.android.synthetic.main.app_main_header.*


/**
 * Created by Elango on 13-02-2020.
 */

class TLChatPrivacySettings : BaseFragment(), View.OnClickListener {

    private var mContext: Context? = null
    private var mFragmentManager: TLFragmentManager? = null
    private lateinit var mBack: ImageView

    private var mView: View? = null

    companion object {
        var TAG: String = TLChatPrivacySettings::class.java.simpleName
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_messenger_privacy_settings, container, false)
        init(mView!!)
        return mView
    }

    override fun init(view: View) {
        mContext = activity
        mFragmentManager = TLFragmentManager(activity!!)

        mBack = view.findViewById(R.id.app_header_back_arrow)

        clickListener()
    }

    override fun clickListener() {
        mBack.setOnClickListener(this)
    }


    override fun initBundle() {

    }

    override fun onClick(view: View) {
        when (view) {
            mBack -> onBackPressed()
        }
    }


    override fun onBackPressed() {
        mFragmentManager!!.onBackPress()
    }

    override fun onResumeFragment() {
        app_header_title.text = "Privacy"
        app_header_title.typeface = ResourcesCompat.getFont(mContext!!, R.font.calling_heart)
        app_header_title.textSize = 30f
        app_header_menu.visibility = View.GONE
        app_header_back_arrow.visibility = View.VISIBLE
        (mContext as TLChatActivity).showBottomBar(false)

    }

}