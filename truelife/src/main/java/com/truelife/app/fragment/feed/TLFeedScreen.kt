package com.truelife.app.fragment.feed

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.truelife.R
import com.truelife.app.activity.DashboardSearchActivity
import com.truelife.app.fragment.TLClubsFeedFragment
import com.truelife.app.fragment.TLFriendsFeedFragment
import com.truelife.app.fragment.TLPublicFeedFragment
import com.truelife.base.BaseActivity
import com.truelife.base.BaseFragment
import com.truelife.base.TLFeedFragmentManager

class TLFeedScreen : BaseFragment() {
    lateinit var myContext: BaseActivity

    // var mTabLayout: TabLayout? = null
    // var mViewPager: ViewPager? = null
    //var mPagerAdapter: Pager? = null
    var mSearchLay: LinearLayout? = null
    var mRootLayout: CoordinatorLayout? = null
    private var myFragmentManager: TLFeedFragmentManager? = null

    var mPublicLay: LinearLayout? = null
    var mFriendsLay: LinearLayout? = null
    var mClubsLay: LinearLayout? = null

    var mPublicLine: View? = null
    var mFriendsLine: View? = null
    var mClubsLine: View? = null

    var publicTxt: TextView? = null
    var mFriendTxt: TextView? = null
    var mClubTxt: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        myContext = activity as BaseActivity
        val aView = inflater.inflate(R.layout.fragment_tlnews_feed_new, container, false)
        initiate(aView)
        return aView
    }

    fun navigate(position: Int) {
        if (position == 1) {

            publicTxt!!.setTextColor(resources.getColor(R.color.white))
            mFriendTxt!!.setTextColor(resources.getColor(R.color.white_trans))
            mClubTxt!!.setTextColor(resources.getColor(R.color.white_trans))

            mPublicLine!!.visibility = View.VISIBLE
            mFriendsLine!!.visibility = View.INVISIBLE
            mClubsLine!!.visibility = View.INVISIBLE

            myFragmentManager!!.replaceContent(
                TLPublicFeedFragment(),
                TLPublicFeedFragment.TAG,
                null
            )
        } else if (position == 2) {
            publicTxt!!.setTextColor(resources.getColor(R.color.white_trans))
            mFriendTxt!!.setTextColor(resources.getColor(R.color.white))
            mClubTxt!!.setTextColor(resources.getColor(R.color.white_trans))

            mPublicLine!!.visibility = View.INVISIBLE
            mFriendsLine!!.visibility = View.VISIBLE
            mClubsLine!!.visibility = View.INVISIBLE
            myFragmentManager!!.replaceContent(
                TLFriendsFeedFragment(),
                TLFriendsFeedFragment.TAG,
                null
            )
        } else if (position == 3) {
            publicTxt!!.setTextColor(resources.getColor(R.color.white_trans))
            mFriendTxt!!.setTextColor(resources.getColor(R.color.white_trans))
            mClubTxt!!.setTextColor(resources.getColor(R.color.white))
            mPublicLine!!.visibility = View.INVISIBLE
            mFriendsLine!!.visibility = View.INVISIBLE
            mClubsLine!!.visibility = View.VISIBLE
            myFragmentManager!!.replaceContent(
                TLClubsFeedFragment(),
                TLClubsFeedFragment.TAG,
                null
            )
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    fun initiate(mView: View) {
        myFragmentManager = TLFeedFragmentManager(myContext)
        /* mTabLayout = mView.findViewById(R.id.tabs)
         mViewPager = mView.findViewById(R.id.viewpager)*/
        mRootLayout = mView.findViewById(R.id.rootLay)
        mSearchLay = mView.findViewById(R.id.search_LAY)
        publicTxt = mView.findViewById(R.id.public_txt)
        mFriendTxt = mView.findViewById(R.id.friends_txt)
        mClubTxt = mView.findViewById(R.id.club_txt)

        mPublicLay = mView.findViewById(R.id.public_lay)
        mFriendsLay = mView.findViewById(R.id.friend_lay)
        mClubsLay = mView.findViewById(R.id.club_lay)

        mPublicLine = mView.findViewById(R.id.public_line)
        mFriendsLine = mView.findViewById(R.id.friends_line)
        mClubsLine = mView.findViewById(R.id.clubs_line)


        publicTxt!!.setOnClickListener {

            navigate(1)
        }

        mFriendTxt!!.setOnClickListener {

            navigate(2)
        }

        mClubTxt!!.setOnClickListener {

            navigate(3)
        }
        initBundle()


        LocalBroadcastManager.getInstance(activity!!).registerReceiver(
            myBroadcastReceiver,
            IntentFilter("DIMLAY")
        )
        mSearchLay!!.setOnClickListener {
            startActivity(Intent(activity, DashboardSearchActivity::class.java))
        }
    }

    private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            val aVal = intent.getBooleanExtra("isDim", false)
            if (aVal) applyDim(0.5f) else clearDim()
        }
    }

    fun applyDim(dimAmount: Float) {
        val dim: Drawable = ColorDrawable(Color.BLACK)
        dim.setBounds(0, 0, mRootLayout!!.width, mRootLayout!!.height)
        dim.alpha = (255 * dimAmount).toInt()
        val overlay = mRootLayout!!.overlay
        overlay.add(dim)
    }

    fun clearDim() {
        val overlay = mRootLayout!!.overlay
        overlay.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(myBroadcastReceiver)
    }


    override fun onBackPressed() {}
    override fun onResumeFragment() {}

    override fun initBundle() {
        try {
            val bundle = arguments
            if (bundle != null) {
                val isClub = bundle.getString("isClub")
                if (isClub.equals("1", true))
                    navigate(3)
                else
                    navigate(1)
            } else {
                navigate(1)
            }
        } catch (e: Exception) {
            navigate(1)
            e.printStackTrace()
        }
    }

    override fun clickListener() {}
    override fun init(view: View) {}

}