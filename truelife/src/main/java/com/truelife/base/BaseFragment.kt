package com.truelife.base

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.truelife.storage.LocalStorageSP
import com.truelife.util.TLProgressDialog
import com.truelife.util.Utility

/**
 * Created by Elango on 28/01/19.
 **/

abstract class BaseFragment : Fragment(),progressInterface {

    lateinit var mProgressDailog: TLProgressDialog

    abstract fun onBackPressed()

    abstract fun onResumeFragment()

    abstract fun init(view: View)

    abstract fun initBundle()

    abstract fun clickListener()


    override fun showProgress() {
        try {
            mProgressDailog.setMessage("Loading wait...")
            mProgressDailog.setCancelable(false)
            mProgressDailog.show()
        } catch (e: Exception) {
        }
    }

    override fun showProgressCancelable() {
        try {
            mProgressDailog.setMessage("Loading wait...")
            mProgressDailog.setCancelable(true)
            mProgressDailog.show()
        } catch (e: Exception) {
        }
    }

    override fun hideProgress() {
        try {
            mProgressDailog.dismiss()
        } catch (e: Exception) {
        }
    }

    fun checkInternet(): Boolean {
        return Utility.isInternetAvailable(this.context)
    }

    override fun onResume() {
        onResumeFragment()
        super.onResume()
    }

    fun getETValue(aEditText: EditText?): String {
        return aEditText?.text?.toString()?.trim { it <= ' ' } ?: ""
    }

    fun getTXTValue(aTextText: TextView?): String {
        return aTextText?.text?.toString()?.trim { it <= ' ' } ?: ""
    }

    fun isProvider(): Boolean {
        return LocalStorageSP[activity!!, "is_provider", false]!!
    }

}
