package com.truelife.app.fragment.more

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.activity.*
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.more.adapter.TLMoreScreenRecyclerAdapter
import com.truelife.app.fragment.more.adapter.TLMoreScreenRecyclerAdapter.ClickListener
import com.truelife.app.fragment.setting.TLSettingScreen
import com.truelife.app.model.User
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import com.truelife.storage.LocalStorageSP

class TLMoreFragment : BaseFragment(), ClickListener {
    var user: User? = null
    private var mContext: Context? = null
    private var mFragmentManager: TLFragmentManager? = null

    private var mMenuRecycler: RecyclerView? = null
    private var mView: View? = null

    private var mMenuAdapter: TLMoreScreenRecyclerAdapter? = null

    companion object {
        var TAG: String = TLMoreFragment::class.java.simpleName
    }


    private fun setRecycleViewAdapter() {
        mMenuRecycler!!.setHasFixedSize(true)
        mMenuRecycler!!.setLayoutManager(LinearLayoutManager(mContext))
        mMenuRecycler!!.setNestedScrollingEnabled(true)
        mMenuAdapter = TLMoreScreenRecyclerAdapter(mContext!!, this)
        mMenuRecycler!!.adapter = mMenuAdapter
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_more_screen, container, false)
        init(mView!!)
        return mView
    }

    override fun init(view: View) {
        mContext = activity
        user = LocalStorageSP.getLoginUser(mContext)
        mFragmentManager = TLFragmentManager(activity!!)
        mMenuRecycler = view.findViewById(R.id.fragment_menu_list_RV) as RecyclerView
        setRecycleViewAdapter()
    }

    override fun clickListener() {


        /*mMenuRecycler!!.addOnItemTouchListener(TLMoreScreenRecyclerAdapter.RecyclerTouchListener(
            mContext!!,
            mMenuRecycler!!,
            object : ClickListener {
                override fun onClick(view: View?, position: Int) {

                    when (position) {
                        1 ->
                            mFragmentManager!!.replaceContent(
                                TLMoreFragment(),
                                TLMoreFragment.TAG,
                                null
                            )
                        2 ->
                            mFragmentManager!!.replaceContent(
                                TLMoreFragment(),
                                TLMoreFragment.TAG,
                                null
                            )
                        3 ->
                            mFragmentManager!!.replaceContent(
                                TLMoreFragment(),
                                TLMoreFragment.TAG,
                                null
                            )
                        4 ->
                            mFragmentManager!!.replaceContent(
                                TLSettingScreen(),
                                TLSettingScreen.TAG,
                                null
                            )
                    }
                }

                override fun onLongClick(view: View?, position: Int) {

                }


            }

        ))*/
    }


    override fun initBundle() {

    }


    override fun onBackPressed() {
        mFragmentManager!!.onBackPress()
    }

    override fun onResumeFragment() {
        (mContext as TLDashboardActivity?)!!.showBottomBar()
        (mContext as TLDashboardActivity?)!!.hideToolbar()
        (mContext as TLDashboardActivity).setHeader(TLConstant.ABOUT_TITLE)
    }

    override fun onClick(view: View?, position: Int) {
        when (position) {
            0 -> startActivity(Intent(mContext, TLAutoGraphActivity::class.java))
            1 -> startActivity(Intent(mContext, TLRaiseIssueActivity::class.java))
            2 -> startActivity(Intent(mContext, TLCinemaActivity::class.java))
            3 -> startActivity(Intent(mContext, TLMomentsActivity::class.java))
            4 -> {

                val aIntent = Intent(mContext, ProfileActivity::class.java).putExtra(
                    "userid",
                    user!!.mUserId!!
                )
                mContext!!.startActivity(aIntent)
            }
            5 ->
                mFragmentManager!!.replaceContent(
                    TLSettingScreen(),
                    TLSettingScreen.TAG,
                    null
                )
        }
    }

    override fun onLongClick(view: View?, position: Int) {
    }
}