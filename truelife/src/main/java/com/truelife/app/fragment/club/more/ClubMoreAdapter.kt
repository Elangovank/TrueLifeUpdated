package com.truelife.app.fragment.club.more

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R

/**
 * Created by Elango on 18-12-2019.
 */
class ClubMoreAdapter(
    private val mContext: FragmentActivity, val isAdmin: Boolean, val listener: ClickListener
) : RecyclerView.Adapter<ClubMoreAdapter.ViewHolder>() {

    var mClubByYouThumbIds = arrayOf<Int>(
        R.drawable.add_invite, R.drawable.remove_member,
        R.drawable.admin, R.drawable.member_rights,
        R.drawable.privacy, R.drawable.exit_from_club
    )
    var mClubInThumbIds = arrayOf(
        R.drawable.add_invite, R.drawable.club_report_icon,
        R.drawable.messanger_admin, R.drawable.exit_from_club
    )
    var myClubByYouTitleArray = mContext.resources.getStringArray(R.array.club_by_you_more)
    var myClubInTitleArray = mContext.resources.getStringArray(R.array.club_in_more)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.inflate_club_more_adapter, parent, false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {

        if (isAdmin) {
            aHolder.mName.text = myClubByYouTitleArray.get(position)
            aHolder.myMenuImageIcon.setImageResource(mClubByYouThumbIds[position])
        } else {
            aHolder.mName.text = myClubInTitleArray.get(position)
            aHolder.myMenuImageIcon.setImageResource(mClubInThumbIds[position])
        }

        aHolder.mRootlayout.setOnClickListener {

            listener.onClick(aHolder.mName.text as String, position)
        }

    }


    override fun getItemCount(): Int {
        return if (isAdmin) {
            6
        } else {
            4
        }
    }

    /**
     * interface onclick
     */
    interface ClickListener {
        fun onClick(tt: String, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {
        val myMenuImageIcon: ImageView
        val mName: TextView
        val mRootlayout: LinearLayout


        init {
            myMenuImageIcon = aView.findViewById(R.id.settings_icon)
            mName = aView.findViewById(R.id.settings_name)
            mRootlayout = aView.findViewById(R.id.root_lay)

        }
    }


}