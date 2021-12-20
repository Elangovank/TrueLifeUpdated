package com.truelife.app.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import com.truelife.R
import com.truelife.app.activity.TLChatActivity
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import kotlinx.android.synthetic.main.app_main_header.*
import kotlinx.android.synthetic.main.fragment_coming_soon.*


/**
 * Created by Elango on 03-02-2020.
 */

class TLComingSoonFragment(val mShowHeader: Boolean, var mTitle: String, var mMessage: String) :
    BaseFragment(),
    View.OnClickListener {

    private var mContext: Context? = null
    private var mFragmentManager: TLFragmentManager? = null
    private lateinit var mBack: ImageView

    private var mView: View? = null

    companion object {
        var TAG: String = TLComingSoonFragment::class.java.simpleName
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_coming_soon, container, false)
        init(mView!!)
        return mView
    }

    override fun init(view: View) {
        mContext = activity
        mFragmentManager = TLFragmentManager(activity!!)

        mBack = mView!!.findViewById(R.id.app_header_back_arrow)
        mBack.visibility = View.VISIBLE

        showHeader(mShowHeader)

        clickListener()

    }

    fun showHeader(show: Boolean) {
        if (show)
            mView!!.findViewById<CardView>(R.id.tool).visibility = View.VISIBLE
        else mView!!.findViewById<CardView>(R.id.tool).visibility = View.GONE
    }

    override fun clickListener() {
        mBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!) {
            mBack -> (mContext as TLChatActivity).finishActivity()
        }
    }


    override fun initBundle() {

    }


    override fun onBackPressed() {
        mFragmentManager!!.onBackPress()
    }

    override fun onResumeFragment() {
        app_header_title.text = mTitle
        message.text = mMessage
        app_header_title.typeface = ResourcesCompat.getFont(mContext!!, R.font.calling_heart)
        app_header_title.textSize = 30f
        app_header_menu.visibility = View.GONE
    }

}