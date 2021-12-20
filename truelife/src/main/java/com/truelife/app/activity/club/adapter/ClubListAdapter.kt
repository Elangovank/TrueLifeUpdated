package com.truelife.app.activity.club.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.ClubInfo
import com.truelife.util.Utility

/**
 * Created by Elango on 18-12-2019.
 */
class ClubListAdapter(
    private val mContext: FragmentActivity,
    var mUList: List<ClubInfo>, val listener: ClickListener
) : RecyclerView.Adapter<ClubListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.club_list_adapter, parent, false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        var data = getNewsInfo(position)
        aHolder.mName.text = data.clubName
        aHolder.mMemberCountTxt.text = "Members " + data.totalMembers
        if (!data.profileImage.isNullOrEmpty())
            data.profileImage?.let { Utility.loadImage(it, aHolder.myMenuImageIcon) }

        if (data.isSelected)
            aHolder.mTickLayout.visibility = View.VISIBLE
        else aHolder.mTickLayout.visibility = View.INVISIBLE
        aHolder.mRootlayout.setOnClickListener {
            if (aHolder.mTickLayout.visibility == View.VISIBLE) {
                listener.onClick(data, position, false)
                aHolder.mTickLayout.visibility = View.INVISIBLE
            } else {
                listener.onClick(data, position, true)
                aHolder.mTickLayout.visibility = View.VISIBLE
            }
        }
    }

    fun getNewsInfo(position: Int): ClubInfo {
        return mUList[position]
    }

    override fun getItemCount(): Int {
        return mUList.size
    }

    /**
     * interface onclick
     */
    interface ClickListener {
        fun onClick(tt: Any, position: Int, type: Boolean)
        fun onLongClick(view: View?, position: Int)
    }

    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {
        val myMenuImageIcon: ImageView
        val mName: TextView
        val mMemberCountTxt: TextView
        val mRootlayout: RelativeLayout
        val mTickLayout: LinearLayout


        init {
            myMenuImageIcon = aView.findViewById(R.id.inflate_menu_list_icon)
            mName = aView.findViewById(R.id.inflate_menu_list_name)
            mMemberCountTxt = aView.findViewById(R.id.member_count_txt)
            mRootlayout = aView.findViewById(R.id.inflate_menu_container_LAY)
            mTickLayout = aView.findViewById(R.id.tick_lay)
        }
    }

    fun updateAdapter(
        aUList: List<ClubInfo>
    ) {
        mUList = aUList
    }

}