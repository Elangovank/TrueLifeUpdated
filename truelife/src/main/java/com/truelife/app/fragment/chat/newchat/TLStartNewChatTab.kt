package com.truelife.app.fragment.chat.newchat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.truelife.R
import com.truelife.app.activity.TLChatActivity
import com.truelife.app.fragment.TLComingSoonFragment
import com.truelife.app.fragment.chat.friends.TLChatFriendsFragment
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager


/**
 * Created by Elango on 07-02-2020.
 */

class TLStartNewChatTab : BaseFragment() {

    private var mContext: Context? = null
    private var mFragmentManager: TLFragmentManager? = null
    private var mTab: TabLayout? = null
    private var mViewPager: ViewPager? = null

    private var mView: View? = null

    companion object {
        var TAG: String = TLStartNewChatTab::class.java.simpleName
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_start_new_chat_tab, container, false)
        init(mView!!)
        return mView
    }

    override fun init(view: View) {
        mContext = activity
        mFragmentManager = TLFragmentManager(activity!!)

        mTab = view.findViewById(R.id.tabs)
        mViewPager = view.findViewById(R.id.viewPager)
        val adapter = PagerAdapter(childFragmentManager, resources.getStringArray(R.array.new_chat))
        mViewPager!!.adapter = adapter
        mTab!!.setupWithViewPager(mViewPager)

        clickListener()
    }

    override fun clickListener() {
        mView!!.findViewById<ImageView>(R.id.back_arrow).setOnClickListener {
            onBackPressed()
        }
    }


    override fun initBundle() {

    }


    override fun onBackPressed() {
        mFragmentManager!!.onBackPress()
    }

    override fun onResumeFragment() {
        (mContext as TLChatActivity).showBottomBar(false)
    }

    class PagerAdapter(
        aFragmentManager: FragmentManager?,
        var mTitles: Array<String>
    ) :
        FragmentStatePagerAdapter(aFragmentManager!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getPageTitle(position: Int): CharSequence? {
            return mTitles[position]
        }

        override fun getCount(): Int {
            return mTitles.size
        }

        override fun getItemPosition(`object`: Any): Int {
            return androidx.viewpager.widget.PagerAdapter.POSITION_NONE
        }

        override fun getItem(position: Int): Fragment {
            return if (position == 0)
                TLChatFriendsFragment(false)
            else
                TLComingSoonFragment(false, "Contacts", "Coming soon...")
        }

        override fun destroyItem(
            container: ViewGroup,
            position: Int,
            `object`: Any
        ) {
            super.destroyItem(container, position, `object`)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        (mContext as TLChatActivity).showBottomBar(true)
    }

}