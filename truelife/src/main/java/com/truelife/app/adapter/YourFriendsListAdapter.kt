package com.truelife.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.UserList
import com.truelife.util.Utility

/**
 * Created by Elango on 18-12-2019.
 */
class YourFriendsListAdapter(
    private val mContext: FragmentActivity,
    var mUList: List<UserList.UList>, val mType: String, val listener: ClickListener
) : RecyclerView.Adapter<YourFriendsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.your_friends_list_adapter, parent, false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        val data = getNewsInfo(position)
        aHolder.mName.text = data.fullname
        if (!data.profileImage.isNullOrEmpty())
            data.profileImage?.let {  Utility.loadImageWithGender(it, aHolder.myMenuImageIcon,data.gender!!) }
        else Utility.loadPlaceHolder(mContext, data.gender!!, aHolder.myMenuImageIcon)

        when (mType) {

            "FriendsList" -> {
                aHolder.addDriendsTxt.isEnabled != data.is_friend.equals("1")
                when {
                    data.is_friend.equals("1") -> {
                        aHolder.addDriendsTxt.text = "Friends"
                    }
                    data.is_friend.equals("0") -> {
                        aHolder.addDriendsTxt.text = "Add to Friend"
                    }
                    data.is_friend.equals("2") -> {
                        aHolder.addDriendsTxt.text = "Cancel Request"
                    }
                }
            }

            "FollowersList" -> {
                when (data.is_follow) {
                    "0" -> {
                        aHolder.addDriendsTxt.text = "Follow"
                    }
                    "1" -> {
                        aHolder.addDriendsTxt.text = "Following"
                    }
                }
            }

            "FollowingList" -> {
                when (data.is_follow) {
                    "0" -> {
                        aHolder.addDriendsTxt.text = "Follow"
                    }
                    "1" -> {
                        aHolder.addDriendsTxt.text = "Following"
                    }
                }
            }

        }

        aHolder.addDriendsTxt.setOnClickListener {
            listener.onClick(data, position)
        }

        aHolder.mRootlayout.setOnClickListener {
            Utility.navigateUserProfile(mContext, data.id!!)
        }
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
        fun onClick(tt: UserList.UList, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {
        val myMenuImageIcon: ImageView
        val mName: TextView
        val mRootlayout: LinearLayout
        val addDriendsTxt: TextView


        init {
            addDriendsTxt = aView.findViewById(R.id.add_friends)
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