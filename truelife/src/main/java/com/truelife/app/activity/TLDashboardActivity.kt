package com.truelife.app.activity

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.truelife.FeedMenuClickListener
import com.truelife.R
import com.truelife.app.VideoPreviewActivity
import com.truelife.app.activity.feedpost.FeedDetailActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.constants.TLConstant.ADD
import com.truelife.app.constants.TLConstant.CHAT
import com.truelife.app.constants.TLConstant.FEED
import com.truelife.app.constants.TLConstant.LASTADDEDITEM
import com.truelife.app.constants.TLConstant.NOTIFICATION
import com.truelife.app.constants.TLConstant.PUSH_CHAT_TYPE
import com.truelife.app.constants.TLConstant.PUSH_DASHBOARD
import com.truelife.app.constants.TLConstant.PUSH_FEED_TYPE
import com.truelife.app.constants.TLConstant.SETTINGS
import com.truelife.app.fragment.feed.TLFeedScreen
import com.truelife.app.fragment.more.TLMoreFragment
import com.truelife.app.fragment.notification.TLNotificationFragment
import com.truelife.app.model.PublicFeedModel
import com.truelife.base.BaseActivity
import com.truelife.base.TLFragmentManager
import com.truelife.chat.activities.SplashActivity
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import com.truelife.util.PermissionChecker
import com.truelife.util.Utility
import kotlinx.android.synthetic.main.app_main_header.*


/**
 * Created by Elango on 28/12/19.
 **/

class TLDashboardActivity : BaseActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {


    var exitCount = 0
    private var bottom_dialog: BottomSheetDialog? = null
    private var mPermission: Array<String> = arrayOf()
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        try {
            val intent = intent

            if (intent?.extras != null && LocalStorageSP[this, "push_notification", false]!!) {

                // Reset Notification
                LocalStorageSP.put(this, "push_notification", false)
                clearNotification(intent.extras!!.getString("message_type")!!)

                when (intent.extras!!.getString("message_type")) {
                    PUSH_FEED_TYPE -> {
                        // navigateFragment(FEED)
                        val i = Intent(mContext, FeedDetailActivity::class.java)
                        i.putExtra("post_id", intent.extras!!.getString("post_id"))
                        startActivity(i)
                    }
                    PUSH_CHAT_TYPE -> navigateFragment(CHAT)

                    PUSH_DASHBOARD -> {

                    }
                    else -> navigateFragment(NOTIFICATION)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.e("Notification received", "Notification received")
    }

    override fun onBackPressed() {

        if (LASTADDEDITEM.equals(FEED)) {
            val count = myContext.supportFragmentManager.backStackEntryCount
            if (count >= 2) {
                exitCount++
                if (exitCount == 1) AppDialogs.showToastDialog(
                    myContext,
                    "Press one more time to exit"
                )
                else if (exitCount == 2) finish()
            } else {
                super.onBackPressed()
            }
        } else {
            val count = myContext.supportFragmentManager.backStackEntryCount
            if (count == 1) {
                exitCount++
                if (exitCount == 1) AppDialogs.showToastDialog(
                    myContext,
                    "Press one more time to exit"
                )
                else if (exitCount == 2) finish()
            } else {
                super.onBackPressed()
            }
        }
    }

    private lateinit var mContext: Context
    private var mFragmentManager: TLFragmentManager? = null
    private var mBottomNavigation: BottomNavigationView? = null
    var mToolbar: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        mContext = this
        init()
        clickListener()
        mPermission = arrayOf(
            TLConstant.CAMERA,
            TLConstant.READ_EXTERNAL_STORAGE,
            TLConstant.WRITE_EXTERNAL_STORAGE
        )
        PermissionChecker().askAllPermissions(myContext, mPermission)
        // Set Home Fragment
        navigateFragment(FEED)
    }

    fun navigateFragment(id: Int) {
        if (id != CHAT && id != ADD)
            resetTabIcons(id)
        exitCount = 0
        if (id != CHAT && !checkInternet()) {
            return
        }

        when (id) {
            FEED -> {
                val bundle = Bundle()

                mFragmentManager!!.clearAllFragments()
                mFragmentManager!!.replaceContent(TLFeedScreen(), "TLFeedScreen",null)
                hideToolbar()
                LASTADDEDITEM = FEED
            }
            NOTIFICATION -> {
                mFragmentManager!!.clearAllFragments()
                mFragmentManager!!.replaceContent(
                    TLNotificationFragment(),
                    TLNotificationFragment.TAG,
                    null
                )
                LASTADDEDITEM = NOTIFICATION
            }
            ADD -> {
                /* val aIntent = Intent(mContext, CreateClub::class.java)
                 mContext!!.startActivity(aIntent)*/
                mBottomNavigation!!.menu.getItem(ADD).setIcon(R.drawable.post_select)
                AppDialogs.showFeedPostDialog(
                    myContext,
                    mFragmentManager,
                    mBottomNavigation!!,
                    DialogInterface.OnCancelListener {
                        mBottomNavigation!!.menu.getItem(ADD).setIcon(R.drawable.post_unselect)
                    })

            }

            CHAT -> {

                startActivity(Intent(this, SplashActivity::class.java))

               /* val user = LocalStorageSP.getLoginUser(mContext)
                if (user.mIsMessengerMobileVerified == "1") {
                    navigateChat()
                    LASTADDEDITEM = CHAT
                } else
                    Helper.navigateOTPScreen(
                        myContext,
                        getString(R.string.label_chat_otp_verify),
                        1
                    )*/
            }

            SETTINGS -> {
                LASTADDEDITEM = SETTINGS
                mFragmentManager!!.clearAllFragments()
                mFragmentManager!!.replaceContent(
                    TLMoreFragment(),
                    TLMoreFragment.TAG,
                    null
                )
            }
        }
    }

    private fun navigateChat() {
        startActivity(Intent(this, TLChatActivity::class.java))
        Utility.isInternetAvailable(myContext, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK)
            navigateChat()

    }

    private fun resetTabIcons(i: Int) {
        try {
            mBottomNavigation!!.menu.getItem(i).isChecked = true

            val aUnselectedDrawables = intArrayOf(
                R.drawable.newscast_icon_unsel,
                R.drawable.notifi_unselect,
                R.drawable.post_unselect,
                R.drawable.icon_tabbar_message,
                R.drawable.menu_unselect
            )
            val aSelectedDrawables = intArrayOf(
                R.drawable.newscast_icon_sel,
                R.drawable.notifi_select,
                R.drawable.post_select,
                R.drawable.icon_tabbar_message,
                R.drawable.menu_select
            )

            for (x in 0 until mBottomNavigation!!.menu.size()) {
                if (x != i)
                    mBottomNavigation!!.menu.getItem(x).setIcon(aUnselectedDrawables[x])
                else mBottomNavigation!!.menu.getItem(x).setIcon(aSelectedDrawables[x])
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun init() {
        mFragmentManager = TLFragmentManager(this)
        mBottomNavigation = findViewById(R.id.bottom_navigation)
        mToolbar = findViewById(R.id.tool)
        mBottomNavigation!!.setOnNavigationItemSelectedListener(this)
        mBottomNavigation!!.itemIconTintList = null
    }

    override fun clickListener() {

    }

    /**
     * @param aTitle String
     */
    fun setHeader(aTitle: String) {
        app_header_title.text = aTitle
    }

    override fun onNavigationItemSelected(menu: MenuItem): Boolean {
        when (menu.itemId) {
            R.id.bottom_news -> {

                showToolbar()
                navigateFragment(FEED)
            }
            R.id.bottom_notification -> navigateFragment(NOTIFICATION)
            R.id.bottom_post -> navigateFragment(ADD)
            R.id.bottom_chat -> navigateFragment(CHAT)
            R.id.bottom_menu -> navigateFragment(SETTINGS)
        }
        return true
    }

    fun hideToolbar() {

        mToolbar!!.visibility = View.GONE
    }

    fun showToolbar() {
        mToolbar!!.visibility = View.VISIBLE
    }

    fun hideBottomBar() {
        mBottomNavigation!!.visibility = View.GONE
    }

    fun showBottomBar() {
        mBottomNavigation!!.visibility = View.VISIBLE
    }

    fun showFeedMenu(
        callback: FeedMenuClickListener,
        feedList: PublicFeedModel.FeedList
    ) {

        AppDialogs.show_feed_menu(callback, feedList, this)

    }


    fun showShareMenu(
        feedList: PublicFeedModel.FeedList, imgView: ImageView
    ) {
        val aPermission = arrayOf(
            TLConstant.WRITE_EXTERNAL_STORAGE
        )
        if (PermissionChecker().checkAllPermission(myContext, aPermission)) {
            AppDialogs.ShowShareDialog(feedList, this, imgView)
        }

    }

    fun showShareMenu(
        feedList: PublicFeedModel.FeedList
    ) {

        AppDialogs.ShowShareDialog(feedList, this)

    }

    fun openVideoPreview(
        original: String?,
        playPosition: Long,
        action: String
    ) {
        val intent =
            Intent(this, VideoPreviewActivity::class.java)
        intent.putExtra("url", original.toString())
        intent.putExtra("play_postion", playPosition)
        intent.putExtra("action", action)
        startActivity(intent)
    }

    fun openImagePreview(
        media: PublicFeedModel.FeedList,
        focusPos: Int
    ) {
        val intent =
            Intent(this, ImagePreview::class.java)
        intent.putExtra("media", media)
        intent.putExtra("focus", focusPos)
        startActivity(intent)
    }

    fun openFeedDetail(feedList: PublicFeedModel.FeedList) {
        val intent =
            Intent(this, FeedDetailActivity::class.java)
        intent.putExtra("media", feedList)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        getFirebase()
    }


    /**
     * --------------------------------->>> Push Notification Area <<<---------------------------
     */

    private fun getFirebase() {
        try {
            val intent = intent

            if (intent.extras != null && LocalStorageSP[this, "push_notification", false]!!) {

                // Reset Notification
                LocalStorageSP.put(this, "push_notification", false)
                clearNotification(intent.extras!!.getString("message_type")!!)

                when (intent.extras!!.getString("message_type")) {
                    PUSH_FEED_TYPE -> {
                       // navigateFragment(FEED)
                        val i = Intent(mContext, FeedDetailActivity::class.java)
                        i.putExtra("post_id", intent.extras!!.getString("post_id"))
                        startActivity(i)
                    }
                    PUSH_CHAT_TYPE -> navigateFragment(CHAT)

                    PUSH_DASHBOARD -> {

                    }
                    else -> navigateFragment(NOTIFICATION)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearNotification(channelId: String) {
        try {
            val mNotificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.cancel(channelId, 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}

private fun Intent.putExtra(name: String, videoSource: MediaSource?) {


}

