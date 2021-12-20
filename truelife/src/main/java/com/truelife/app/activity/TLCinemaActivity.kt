package com.truelife.app.activity

import android.content.Context
import android.os.Bundle
import com.truelife.R
import com.truelife.base.BaseActivity
import kotlinx.android.synthetic.main.activity_autograph.*


/**
 * Created by Elango on 20-05-2020.
 */

class TLCinemaActivity : BaseActivity() {

    private var mContext: Context? = null

    companion object {
        var TAG: String = TLCinemaActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cinema)
        mContext = this
        init()
    }

    override fun init() {

        back_arrow.setOnClickListener {
            finish()
        }
    }

    override fun clickListener() {

    }

}