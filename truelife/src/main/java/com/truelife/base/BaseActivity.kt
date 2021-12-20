package com.truelife.base

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.truelife.util.TLProgressDialog
import com.truelife.util.Utility

/**
 * Created by Elango on 28/01/19.
 **/

abstract class BaseActivity : AppCompatActivity(),progressInterface {

    lateinit var mProgressDailog: TLProgressDialog

    lateinit var myContext: BaseActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myContext = this
        mProgressDailog = TLProgressDialog(myContext)
    }

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

    abstract fun clickListener()


    abstract fun init()

    fun checkInternet(): Boolean {
        return Utility.isInternetAvailable(this)
    }

    fun getETValue(aEditText: EditText?): String {
        return aEditText?.text?.toString()?.trim { it <= ' ' } ?: ""
    }

    fun getTXTValue(aTextText: TextView?): String {
        return aTextText?.text?.toString()?.trim { it <= ' ' } ?: ""
    }

}
