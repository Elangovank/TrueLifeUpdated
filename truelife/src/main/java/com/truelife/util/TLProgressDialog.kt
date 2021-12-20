package com.truelife.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.truelife.R
import com.truelife.base.BaseActivity

class TLProgressDialog : Dialog {

    private var myContext: FragmentActivity? = null
    private var myLoadingTxt: TextView? = null
    private var myProgressWheel: ProgressBar? = null

    constructor(context: BaseActivity?) : super(context!!) {
        myContext = context
        try {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE)
            this.setContentView(R.layout.custom_dialog_box)
            this.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myLoadingTxt = this.findViewById(R.id.custom_dialog_box_TXT_loading) as TextView
            myProgressWheel = this.findViewById(R.id.custom_dialog_box_PB_loading) as ProgressBar
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    constructor(context: FragmentActivity?) : super(context!!) {
        myContext = context
        try {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE)
            this.setContentView(R.layout.custom_dialog_box)
            this.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myLoadingTxt = this.findViewById(R.id.custom_dialog_box_TXT_loading) as TextView
            myProgressWheel = this.findViewById(R.id.custom_dialog_box_PB_loading) as ProgressBar
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun setMessage(aLoadingText: String) {
        myLoadingTxt!!.text = aLoadingText
    }


}