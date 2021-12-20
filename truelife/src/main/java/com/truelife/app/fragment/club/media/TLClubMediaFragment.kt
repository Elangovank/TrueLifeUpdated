package com.truelife.app.fragment.club.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.constants.TLConstant.DD_MM_YY
import com.truelife.app.constants.TLConstant.MMM_YYYY
import com.truelife.app.fragment.club.adapter.ClubMembersAdapter
import com.truelife.app.model.Club
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import com.truelife.util.DateUtil
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


/**
 * Created by Elango on 21-02-2020.
 */

class TLClubMediaFragment(var mClubMedia: ArrayList<Club.Media>) : BaseFragment(),
    ClubMembersAdapter.ClickListener {

    private var mContext: FragmentActivity? = null
    private var mFragmentManager: TLFragmentManager? = null

    private lateinit var mNoDataText: TextView

    private var mMediaRecycler: RecyclerView? = null
    var mAdapter: TLClubMediaHeaderAdapter? = null
    var mMediaMap = TreeMap<String, ArrayList<Club.Media>>(MyComparator())

    private var mView: View? = null

    companion object {
        var TAG: String = TLClubMediaFragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_create_club_by_you_media, container, false)
        init(mView!!)
        return mView
    }

    override fun init(view: View) {
        mContext = activity
        mFragmentManager = TLFragmentManager(mContext!!)
        mMediaRecycler = view.findViewById(R.id.fragment_horizontal_recycler)
        mMediaRecycler!!.isNestedScrollingEnabled = false
        mNoDataText = view.findViewById(R.id.layout_nodata_message)
        loadData()
    }

    private fun loadData() {
        try {
            if (mClubMedia.isEmpty()) {
                mNoDataText.text = "No Media found"
                mNoDataText.visibility = View.VISIBLE
            } else {
                mNoDataText.visibility = View.GONE

                val date = HashSet<String>()

                for (i in mClubMedia.indices) {
                    date.add(mClubMedia[i].created!!)
                }

                for (x in date) {
                    val aDisplayDate = DateUtil.convertDateFormat(x, DD_MM_YY, MMM_YYYY)
                    val myMedia = ArrayList<Club.Media>()
                    for (i in mClubMedia.indices) {
                        if (aDisplayDate == DateUtil.convertDateFormat(
                                mClubMedia[i].created!!,
                                DD_MM_YY,
                                MMM_YYYY
                            )
                        ) {
                            myMedia.add(mClubMedia[i])
                        }
                    }
                    mMediaMap[aDisplayDate] = myMedia
                }
                initRecycler()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun initRecycler() {
        mMediaRecycler!!.setHasFixedSize(true)
        mMediaRecycler!!.layoutManager = LinearLayoutManager(mContext)
        mMediaRecycler!!.isNestedScrollingEnabled = true
        mAdapter = TLClubMediaHeaderAdapter(mContext!!, mMediaMap)
        mMediaRecycler!!.adapter = mAdapter
    }

    override fun clickListener() {

    }

    class MyComparator : Comparator<String> {
        override fun compare(o1: String, o2: String): Int {
            return SimpleDateFormat(MMM_YYYY).parse(o2).time.compareTo(
                SimpleDateFormat(MMM_YYYY).parse(o1).time
            )
        }
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