package com.truelife.app.fragment.club

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.fragment.club.adapter.ClubMembersAdapter
import com.truelife.app.model.Club
import com.truelife.app.model.UserList
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager

class TLClubMembersFragment(var clubmembers : ArrayList<Club.ClubMember>) : BaseFragment(), ClubMembersAdapter.ClickListener {

    private var mContext: FragmentActivity? = null
    private var mFragmentManager: TLFragmentManager? = null
    var mAdapter: ClubMembersAdapter? = null
    private var mMenuRecycler: RecyclerView? = null
    private var mView: View? = null
    var mUList: List<UserList.UList> = arrayListOf()

    companion object {
        var TAG: String = TLClubMembersFragment::class.java.simpleName
    }


    private fun setRecycleViewAdapter() {
        mMenuRecycler!!.setHasFixedSize(true)
        mMenuRecycler!!.layoutManager = GridLayoutManager(mContext, 3)
        mMenuRecycler!!.isNestedScrollingEnabled = true
        mAdapter = ClubMembersAdapter(mContext!!, clubmembers, this)
        mMenuRecycler!!.adapter = mAdapter
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_club_members_screen, container, false)
        init(mView!!)
        return mView
    }

    fun  getBundle(){

    }
    override fun init(view: View) {
        mContext = activity
        mFragmentManager = TLFragmentManager(mContext!!)
        mMenuRecycler = view.findViewById(R.id.fragment_menu_list_RV) as RecyclerView
        getBundle()
        setRecycleViewAdapter()
    }

    override fun clickListener() {

    }


    override fun initBundle() {

    }


    override fun onBackPressed() {
        mFragmentManager!!.onBackPress()
    }

    override fun onResumeFragment() {

    }


    override fun onClick(tt: Any, position: Int) {
    }


    override fun onLongClick(view: View?, position: Int) {
    }
}