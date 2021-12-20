package com.truelife.app.touchimageview

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.truelife.R
import com.truelife.util.Utility

/**
 * Created by Elango on 20-02-2020.
 */

class TLSingleImagePreview(var mURL: String, val mPlaceHolder: Int) : DialogFragment(),
    View.OnClickListener {

    lateinit var mContext: Context
    lateinit var mView: View

    lateinit var mClose: TextView
    lateinit var mImageView: TLTouchImageView

    companion object {
        var TAG: String = TLSingleImagePreview::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.single_image_preview, container, false)
        mContext = activity!!
        initView()
        return mView
    }

    private fun initView() {
        mClose = mView.findViewById(R.id.close_club_button)
        mImageView = mView.findViewById(R.id.image)

        initData()

        clickListener()
    }

    private fun initData() {
        Utility.loadImage(mURL, mImageView, mPlaceHolder)
    }

    private fun clickListener() {
        mClose.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            mClose -> dismiss()
        }
    }
}