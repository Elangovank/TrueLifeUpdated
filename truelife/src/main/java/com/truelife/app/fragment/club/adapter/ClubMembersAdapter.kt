package com.truelife.app.fragment.club.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.Club
import com.truelife.util.Utility

/**
 * Created by Elango on 18-02-2020.
 */
class ClubMembersAdapter(
    private val mContext: FragmentActivity,
    var mUList: ArrayList<Club.ClubMember>, val listener: ClickListener
) : RecyclerView.Adapter<ClubMembersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.layout_inflate_club_member_item, parent, false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        var data = getNewsInfo(position)
        aHolder.mName.text = data.firstName
        if (!data.profileImage.isNullOrEmpty())
            Utility.loadImageWithGender(data.profileImage!!, aHolder.myMenuImageIcon,data.gender!!)
        else Utility.loadPlaceHolder(mContext, data.gender!!, aHolder.myMenuImageIcon)
    }

    fun getNewsInfo(position: Int): Club.ClubMember {
        return mUList[position]
    }

    override fun getItemCount(): Int {
        return mUList.size
    }

    /**
     * interface onclick
     */
    interface ClickListener {
        fun onClick(tt: Any, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {
        val myMenuImageIcon: ImageView
        val mName: TextView
        val mRootlayout: LinearLayout


        init {
            myMenuImageIcon = aView.findViewById(R.id.layout_club_image)
            mName = aView.findViewById(R.id.layout_club_name)
            mRootlayout = aView.findViewById(R.id.root_lay)
        }
    }

    fun updateAdapter(
        aUList: ArrayList<Club.ClubMember>
    ) {
        mUList = aUList
    }

}