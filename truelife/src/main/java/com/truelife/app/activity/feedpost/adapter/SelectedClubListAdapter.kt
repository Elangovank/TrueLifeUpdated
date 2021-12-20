package com.truelife.app.activity.feedpost.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.ClubInfo
import com.truelife.app.model.FriendsList

/**
 * Created by Elango on 18-12-2019.
 */
class SelectedClubListAdapter(
    private val mContext: FragmentActivity,
    var mUList: List<ClubInfo>, val listener: ClickListener
) : RecyclerView.Adapter<SelectedClubListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.inflate_selected_club_list_item, parent, false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        var data = getNewsInfo(position)
        aHolder.mName.text = data.clubName

        aHolder.mcloseBtn.setOnClickListener {
            listener.onClick(data, position)
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
        fun onClick(tt: Any, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {
        val mName: TextView
        //val mRootlayout: LinearLayout
        val mcloseBtn: ImageButton

        init {

            mName = aView.findViewById(R.id.inflate_menu_list_name)
            //   mRootlayout = aView.findViewById(R.id.inflate_menu_container_LAY)
            mcloseBtn = aView.findViewById(R.id.inflate_menu_right_arrow)
        }
    }

    fun updateAdapter(
        aUList: List<FriendsList>
    ) {
    }

}