package com.truelife.app.fragment.chat.settings

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.core.content.res.ResourcesCompat
import com.truelife.R
import com.truelife.app.activity.TLChatActivity
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.SingleChoiceAdapter
import kotlinx.android.synthetic.main.app_main_header.*
import java.util.*


/**
 * Created by Elango on 17-02-2020.
 */

class TLChatNotificationSettings : BaseFragment(), View.OnClickListener {

    private var mContext: Context? = null
    private var mFragmentManager: TLFragmentManager? = null
    private lateinit var mBack: ImageView
    private lateinit var mChatTone: LinearLayout
    private lateinit var mChatName: TextView
    private lateinit var mPreviewToggle: ToggleButton
    private lateinit var mAlertToggle: ToggleButton
    var mMediaPlayer: MediaPlayer? = null
    var mAlert = true
    var mPreview = true

    private var mView: View? = null
    private lateinit var mTones: Array<Int>
    private var mToneNames = arrayListOf(*resources.getStringArray(R.array.chat_tone))

    companion object {
        var TAG: String = TLChatNotificationSettings::class.java.simpleName
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView =
            inflater.inflate(R.layout.fragment_messenger_notification_settings, container, false)
        init(mView!!)
        return mView
    }

    override fun init(view: View) {
        mContext = activity
        mFragmentManager = TLFragmentManager(activity!!)

        mBack = view.findViewById(R.id.app_header_back_arrow)
        mChatTone = view.findViewById(R.id.select_tone)
        mChatName = view.findViewById(R.id.fragment_messenger_privacy_settings_privacy_TXT)
        mPreviewToggle = view.findViewById(R.id.preview_message)
        mAlertToggle = view.findViewById(R.id.message_alert)

        mTones = arrayOf(R.raw.chat_tone_1, R.raw.chat_tone_2, R.raw.chat_tone_3, R.raw.chat_tone_4)
        mChatName.text = mToneNames[LocalStorageSP[mContext!!, "chat_tone", 0]!!]
        mPreviewToggle.isChecked = LocalStorageSP[mContext!!, "chat_preview", true]!!
        mAlertToggle.isChecked = LocalStorageSP[mContext!!, "chat_alerts", true]!!

        mAlert = mAlertToggle.isChecked
        mPreview = mPreviewToggle.isChecked

        clickListener()
    }

    override fun clickListener() {
        mBack.setOnClickListener(this)
        mChatTone.setOnClickListener(this)
        mPreviewToggle.setOnClickListener(this)
        mAlertToggle.setOnClickListener(this)
    }


    override fun initBundle() {

    }

    override fun onClick(view: View) {
        when (view) {
            mBack -> onBackPressed()

            mAlertToggle -> {
                mAlert = !mAlert
                mAlertToggle.isChecked = mAlert
                LocalStorageSP.put(mContext!!, "chat_alerts", mAlertToggle.isChecked)
            }

            mPreviewToggle -> {
                mPreview = !mPreview
                mPreviewToggle.isChecked = mPreview
                LocalStorageSP.put(mContext!!, "chat_preview", mPreviewToggle.isChecked)
            }

            mChatTone -> {
                AppDialogs.showSingleChoice(
                    mContext!!, "Alert Tones",
                    mToneNames, object : SingleChoiceAdapter.Callback {
                        override fun info(position: Int, text: String) {
                            mChatName.text = text
                            LocalStorageSP.put(mContext!!, "chat_tone", position)
                            playMusic(position)
                        }
                    }, true
                )
            }
        }
    }

    private fun playMusic(position: Int) {
        try {
            mMediaPlayer = MediaPlayer.create(mContext, mTones[position])
            mMediaPlayer!!.start()
            mMediaPlayer!!.setOnCompletionListener {
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onBackPressed() {
        mFragmentManager!!.onBackPress()
    }

    override fun onResumeFragment() {
        app_header_title.text = "Notifications"
        app_header_title.typeface = ResourcesCompat.getFont(mContext!!, R.font.calling_heart)
        app_header_title.textSize = 30f
        app_header_menu.visibility = View.GONE
        app_header_back_arrow.visibility = View.VISIBLE
        (mContext as TLChatActivity).showBottomBar(false)

    }
}