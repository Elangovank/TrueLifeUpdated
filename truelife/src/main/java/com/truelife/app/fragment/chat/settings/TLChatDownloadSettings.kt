package com.truelife.app.fragment.chat.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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

class TLChatDownloadSettings : BaseFragment(), View.OnClickListener {

    private var mContext: Context? = null
    private var mFragmentManager: TLFragmentManager? = null
    private lateinit var mBack: ImageView
    private lateinit var mImagesPopup: LinearLayout
    private lateinit var mVideosPopup: LinearLayout
    private lateinit var mAudiosPopup: LinearLayout
    private lateinit var mDocsPopup: LinearLayout
    private lateinit var mImages: TextView
    private lateinit var mVideos: TextView
    private lateinit var mAudio: TextView
    private lateinit var mDocs: TextView
    private lateinit var mValues: ArrayList<String>


    private var mView: View? = null

    companion object {
        var TAG: String = TLChatDownloadSettings::class.java.simpleName
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_messenger_file_settings, container, false)
        init(mView!!)
        return mView
    }

    override fun init(view: View) {
        mContext = activity
        mFragmentManager = TLFragmentManager(activity!!)

        mBack = view.findViewById(R.id.app_header_back_arrow)
        mImagesPopup = view.findViewById(R.id.images_popup)
        mVideosPopup = view.findViewById(R.id.videos_popup)
        mAudiosPopup = view.findViewById(R.id.audios_popup)
        mDocsPopup = view.findViewById(R.id.docs_popup)

        mValues = arrayListOf(*resources.getStringArray(R.array.downloads))

        mImages = view.findViewById(R.id.fragment_messenger_privacy_settings_image_TXT)
        mVideos = view.findViewById(R.id.fragment_messenger_privacy_settings_video_TXT)
        mAudio = view.findViewById(R.id.fragment_messenger_privacy_settings_audio_TXT)
        mDocs = view.findViewById(R.id.fragment_messenger_privacy_settings_document_TXT)

        initBundle()

        clickListener()
    }

    override fun clickListener() {
        mBack.setOnClickListener(this)
        mImagesPopup.setOnClickListener(this)
        mVideosPopup.setOnClickListener(this)
        mAudiosPopup.setOnClickListener(this)
        mDocsPopup.setOnClickListener(this)
    }


    override fun initBundle() {
        if (LocalStorageSP[mContext!!, "chat_image", ""]!!.isNotEmpty())
            mImages.text = LocalStorageSP[mContext!!, "chat_image", ""]

        if (LocalStorageSP[mContext!!, "chat_video", ""]!!.isNotEmpty())
            mVideos.text = LocalStorageSP[mContext!!, "chat_video", ""]

        if (LocalStorageSP[mContext!!, "chat_audio", ""]!!.isNotEmpty())
            mAudio.text = LocalStorageSP[mContext!!, "chat_audio", ""]

        if (LocalStorageSP[mContext!!, "chat_docs", ""]!!.isNotEmpty())
            mDocs.text = LocalStorageSP[mContext!!, "chat_docs", ""]

    }

    override fun onClick(view: View) {
        when (view) {
            mBack -> onBackPressed()
            mImagesPopup -> showPopup(0, mImages)
            mVideosPopup -> showPopup(1, mVideos)
            mAudiosPopup -> showPopup(2, mAudio)
            mDocsPopup -> showPopup(3, mDocs)
        }
    }

    private fun showPopup(i: Int, textView: TextView) {
        AppDialogs.showSingleChoice(
            mContext!!, null,
            mValues, object : SingleChoiceAdapter.Callback {
                override fun info(position: Int, text: String) {
                    AppDialogs.hideSingleChoice()
                    textView.text = text
                    when (i) {
                        0 -> LocalStorageSP.put(mContext!!, "chat_image", text)
                        1 -> LocalStorageSP.put(mContext!!, "chat_video", text)
                        2 -> LocalStorageSP.put(mContext!!, "chat_audio", text)
                        3 -> LocalStorageSP.put(mContext!!, "chat_docs", text)
                    }
                }
            }, true
        )
    }


    override fun onBackPressed() {
        mFragmentManager!!.onBackPress()
    }

    override fun onResumeFragment() {
        app_header_title.text = "Files"
        app_header_title.typeface = ResourcesCompat.getFont(mContext!!, R.font.calling_heart)
        app_header_title.textSize = 30f
        app_header_menu.visibility = View.GONE
        app_header_back_arrow.visibility = View.VISIBLE
        (mContext as TLChatActivity).showBottomBar(false)

    }

}