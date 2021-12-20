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
import com.truelife.app.model.CreateClubFriendsList
import com.truelife.util.Utility
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by Elango on 18-12-2019.
 */
class CreateClubFriendsListAdapter(
    private val mContext: FragmentActivity,
    var mUList: List<CreateClubFriendsList.FriendsList>, val listener: ClickListener
) : RecyclerView.Adapter<CreateClubFriendsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.inflate_create_club_friends_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        var data = getNewsInfo(position)

        if (data.isSelected == true) {
            aHolder.mTickBtn.visibility = View.GONE
            aHolder.mUnTickBtn.visibility = View.VISIBLE
        } else if (data.isSelected == false) {
            aHolder.mTickBtn.visibility = View.VISIBLE
            aHolder.mUnTickBtn.visibility = View.GONE
        }
        aHolder.mName.text = data.fullname
        if (!data.profileimage.isNullOrEmpty())
            data.profileimage?.let { Utility.loadImage(it, aHolder.myMenuImageIcon) }
        else Utility.loadPlaceHolder(mContext, data.gender!!, aHolder.myMenuImageIcon)
        aHolder.mTickBtn.setOnClickListener {
            aHolder.mTickBtn.visibility = View.GONE
            aHolder.mUnTickBtn.visibility = View.VISIBLE
            listener.onSelected(true, data)
        }
        aHolder.mUnTickBtn.setOnClickListener {
            aHolder.mTickBtn.visibility = View.VISIBLE
            aHolder.mUnTickBtn.visibility = View.GONE
            listener.onSelected(false, data)
        }
    }

    fun getNewsInfo(position: Int): CreateClubFriendsList.FriendsList {
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

        fun onSelected(type: Boolean, data: CreateClubFriendsList.FriendsList)
    }

    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {
        val myMenuImageIcon: CircleImageView
        val mName: TextView
        val mRootlayout: LinearLayout
        val mTickBtn: Button
        val mUnTickBtn: Button

        init {
            myMenuImageIcon = aView.findViewById(R.id.inflate_friend_image)
            mName = aView.findViewById(R.id.inflate_country_list_item_TXT_name)
            mRootlayout = aView.findViewById(R.id.inflate_friends_list_item_LAY)
            mTickBtn = aView.findViewById(R.id.inflate_invite_button)
            mUnTickBtn = aView.findViewById(R.id.inflate_tick_button)
        }
    }

    fun updateAdapter(
        aUList: List<CreateClubFriendsList.FriendsList>
    ) {
        mUList = aUList
    }

    fun getSelectedFriendList(): ArrayList<String>? {
        var mStringArray: ArrayList<String> = arrayListOf()

        return mStringArray
    }
}