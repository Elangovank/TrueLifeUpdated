package com.truelife.app.fragment.club.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.Club
import com.truelife.util.Utility

/**
 * Created by Elango on 18-12-2019.
 */
class ClubAdapter(
    private val mContext: FragmentActivity,
    var mUList: List<Club.Clubs>, val listener: ClickListener
) : RecyclerView.Adapter<ClubAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.inflate_club_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        var data = getNewsInfo(position)
        aHolder.mClubName.text = data.clubName
        aHolder.mClubNumber.text =
            if (data.totalMembers.equals("0") || data.totalMembers.equals("1")) data.totalMembers + " Member" else data.totalMembers + " Members"

        aHolder.mClubType.text = when (data.clubFor) {
            "1" -> "Public Club"
            "2" -> "Friends Club"
            "3" -> "Contacts Club"
            else -> ""
        }

        aHolder.mRootlayout.setOnClickListener {

            listener.onClick(data, position)
        }

        if (!data.profileImage.isNullOrEmpty())
            Utility.loadImage(data.profileImage!!, aHolder.mImageView, R.drawable.club_placeholder)
        else
            aHolder.mImageView.setImageResource(R.drawable.club_placeholder)
    }

    fun getNewsInfo(position: Int): Club.Clubs {
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
        val mClubName: TextView
        val mClubNumber: TextView
        val mClubType: TextView
        val mRootlayout: CardView
        val mImageView: ImageView

        init {
            mClubName = aView.findViewById(R.id.layout_club_name)
            mClubNumber = aView.findViewById(R.id.layout_club_members)
            mClubType = aView.findViewById(R.id.layout_club_type)
            mRootlayout = aView.findViewById(R.id.layout_club_linear_layout)
            mImageView = aView.findViewById(R.id.layout_club_image)
        }
    }


}