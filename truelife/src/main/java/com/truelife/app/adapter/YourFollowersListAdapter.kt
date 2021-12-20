package com.truelife.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.UserList
import com.truelife.util.Utility
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by Elango on 18-12-2019.
 */
class YourFollowersListAdapter(
    private val mContext: FragmentActivity,
    var mUList: List<UserList.UList>, val listener: ClickListener
) : RecyclerView.Adapter<YourFollowersListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.your_followers_list_adapter, parent, false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        var data = getNewsInfo(position)
        aHolder.mName.text = data.fullname
        if (!data.profileImage.isNullOrEmpty())
            data.profileImage?.let { Utility.loadImage(it, aHolder.myMenuImageIcon) }
        else Utility.loadPlaceHolder(mContext, data.gender!!, aHolder.myMenuImageIcon)


    }

    fun getNewsInfo(position: Int): UserList.UList {
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
        val myMenuImageIcon: CircleImageView
        val mName: TextView
        val mRootlayout: LinearLayout


        init {
            myMenuImageIcon = aView.findViewById(R.id.inflate_menu_list_icon)
            mName = aView.findViewById(R.id.inflate_menu_list_name)
            mRootlayout = aView.findViewById(R.id.inflate_menu_container_LAY)
        }
    }

    fun updateAdapter(
        aUList: List<UserList.UList>
    ) {
        mUList = aUList
    }

}