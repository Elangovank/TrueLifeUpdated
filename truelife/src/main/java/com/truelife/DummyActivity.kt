package com.truelife

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.truelife.chat.activities.AcceptInviteLink
import com.truelife.base.BaseActivity


/**
 * Created by Elango on 20-02-2020.
 */

class DummyActivity : BaseActivity() {

    private var mContext: Context? = null

    companion object {
        var TAG: String = DummyActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_club_details)
        mContext = this
        init()

    //    startActivity(Intent(this, AcceptInviteLink::class.java))
    }

    override fun init() {


    }

    override fun clickListener() {

    }

}