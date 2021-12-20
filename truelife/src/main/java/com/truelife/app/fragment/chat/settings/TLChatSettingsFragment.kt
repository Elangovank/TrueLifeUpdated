package com.truelife.app.fragment.chat.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.truelife.R
import com.truelife.app.activity.BlockedListActivity
import com.truelife.app.activity.TLChatActivity
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import kotlinx.android.synthetic.main.app_main_header.*


/**
 * Created by Elango on 13-02-2020.
 */

class TLChatSettingsFragment : BaseFragment(), View.OnClickListener {

    private var mContext: Context? = null
    private var mFragmentManager: TLFragmentManager? = null

    private lateinit var mBack: ImageView
    private lateinit var mPrivacy: LinearLayout
    private lateinit var mBlockList: LinearLayout
    private lateinit var mNotification: LinearLayout
    private lateinit var mFiles: LinearLayout

    private var mView: View? = null

    companion object {
        var TAG: String = TLChatSettingsFragment::class.java.simpleName
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_messenger_settings, container, false)
        init(mView!!)
        return mView
    }

    override fun init(view: View) {
        mContext = activity
        mFragmentManager = TLFragmentManager(activity!!)

        mBack = view.findViewById(R.id.app_header_back_arrow)
        mPrivacy = view.findViewById(R.id.fragment_messenger_settings_privacy_LAY)
        mBlockList = view.findViewById(R.id.fragment_messenger_settings_block_LAY)
        mNotification = view.findViewById(R.id.fragment_messenger_settings_notification_LAY)
        mFiles = view.findViewById(R.id.fragment_messenger_settings_file_LAY)

        clickListener()
    }

    override fun clickListener() {
        mBack.setOnClickListener(this)
        mPrivacy.setOnClickListener(this)
        mBlockList.setOnClickListener(this)
        mNotification.setOnClickListener(this)
        mFiles.setOnClickListener(this)
    }


    override fun initBundle() {

    }


    override fun onBackPressed() {
        mFragmentManager!!.onBackPress()
    }

    override fun onResumeFragment() {
        app_header_title.text = "Settings"
        app_header_title.typeface = ResourcesCompat.getFont(mContext!!, R.font.calling_heart)
        app_header_title.textSize = 30f
        app_header_menu.visibility = View.GONE
        app_header_back_arrow.visibility = View.VISIBLE
        (mContext as TLChatActivity).showBottomBar(false)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        (mContext as TLChatActivity).showBottomBar(true)
    }

    override fun onClick(view: View) {
        when (view) {
            mBack -> onBackPressed()

            mPrivacy -> mFragmentManager!!.addContent(
                TLChatPrivacySettings(),
                TLChatPrivacySettings.TAG,
                null
            )

            mBlockList -> mContext!!.startActivity(
                Intent(
                    mContext,
                    BlockedListActivity::class.java
                )
            )

            mNotification -> mFragmentManager!!.addContent(
                TLChatNotificationSettings(),
                TLChatNotificationSettings.TAG,
                null
            )

            mFiles -> mFragmentManager!!.addContent(
                TLChatDownloadSettings(),
                TLChatDownloadSettings.TAG,
                null
            )
        }
    }

}