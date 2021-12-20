package com.truelife.app.activity

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.truelife.R
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.TLComingSoonFragment
import com.truelife.app.fragment.chat.chatlist.TLChatListFragment
import com.truelife.app.fragment.chat.friends.TLChatFriendsFragment
import com.truelife.base.BaseActivity
import com.truelife.base.TLFragmentManager
import com.truelife.util.AppDialogs
import com.truelife.util.Utility
import kotlinx.android.synthetic.main.app_main_header.*


/**
 * Created by Elango on 11/12/19.
 **/

class TLChatActivity : BaseActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var mContext: Context
    private var mFragmentManager: TLFragmentManager? = null
    private var mBottomNavigation: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        mContext = this
        init()
        clickListener()

        // Set Home Fragment
        navigateFragment(TLConstant.CHAT_LIST)
    }

    override fun onBackPressed() {
        val count = myContext.supportFragmentManager.backStackEntryCount
        if (count == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun init() {
        mFragmentManager = TLFragmentManager(this)
        mBottomNavigation = findViewById(R.id.bottom_navigation)

        initBundle()
    }

    private fun initBundle() {
        try {
            val intent = intent
            if (intent?.extras != null && intent.extras!!.containsKey("content")) {
                AppDialogs.customOkAction(
                    this,
                    null,
                    intent.extras!!.getString("content")!!,
                    null,
                    null,
                    false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun clickListener() {
        mBottomNavigation!!.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(menu: MenuItem): Boolean {
        when (menu.itemId) {
            R.id.chat -> navigateFragment(TLConstant.CHAT_LIST)
            R.id.friends -> navigateFragment(TLConstant.CHAT_FRIENDS)
            R.id.contacts -> navigateFragment(TLConstant.CHAT_CONTACTS)
            R.id.wifi -> navigateFragment(TLConstant.CHAT_WIFI)
        }
        return true
    }

    private fun navigateFragment(id: Int) {

        if (id != TLConstant.CHAT_LIST && !checkInternet()) {
            return
        }

        mFragmentManager!!.clearAllFragments()

        when (id) {
            TLConstant.CHAT_LIST -> {
                Utility.isInternetAvailable(myContext, false)
                mFragmentManager!!.replaceContent(
                    TLChatListFragment(),
                    TLChatListFragment.TAG,
                    null
                )
            }

            TLConstant.CHAT_FRIENDS -> mFragmentManager!!.replaceContent(
                TLChatFriendsFragment(true),
                TLChatFriendsFragment.TAG,
                null
            )
            TLConstant.CHAT_CONTACTS ->

                mFragmentManager!!.replaceContent(
                    TLComingSoonFragment(true, "Contacts", "Coming soon..."),
                    TLComingSoonFragment.TAG,
                    null
                )

            /*mFragmentManager!!.replaceContent(
            TLChatContactsFragment(),
            TLChatContactsFragment.TAG,
            null)*/


            TLConstant.CHAT_WIFI -> mFragmentManager!!.replaceContent(
                TLComingSoonFragment(true, "Wi-Messenger", "Coming soon..."),
                TLComingSoonFragment.TAG,
                null
            )
        }
    }


    /**
     * @param aTitle String
     */
    fun setHeaderTitle(aTitle: String) {
        app_header_title.text = aTitle
    }

    fun finishActivity() {
        finish()
    }

    fun showBottomBar(show: Boolean) {
        if (show)
            mBottomNavigation!!.visibility = View.VISIBLE
        else mBottomNavigation!!.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

