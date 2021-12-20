package com.truelife.app.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.truelife.ProfileClickListener
import com.truelife.R
import com.truelife.app.activity.ProfileActivity
import com.truelife.app.model.Profile
import com.truelife.util.Utility
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by Elango on 18-12-2019.
 */
class ProfileFollowingListAdapter(
    private val mContext: FragmentActivity,
    var mUList: List<Profile.FollowingDetails>, val listener: ProfileClickListener
) : RecyclerView.Adapter<ProfileFollowingListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.inflate_profile_screen_friends_follow_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        var data = getNewsInfo(position)

        aHolder.mName.text = data.userName
        if (!data.profile.isNullOrEmpty())
            data.profile?.let {  Utility.loadImageWithGender(it, aHolder.mProfileimage,data.gender!!) }
        else Utility.loadPlaceHolder(mContext, data.gender!!, aHolder.mProfileimage)

        aHolder.mRootlayout.setOnClickListener {

            val aIntent = Intent(mContext, ProfileActivity::class.java).putExtra(
                "userid",
                data.id
            )
            mContext.startActivity(aIntent)
        }
    }

    fun getNewsInfo(position: Int): Profile.FollowingDetails {
        return mUList[position]
    }

    override fun getItemCount(): Int {
        return mUList.size
    }


    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {
        val mProfileimage: CircleImageView
        val mName: TextView
        val mRootlayout: LinearLayout

        init {
            mProfileimage = aView.findViewById(R.id.profile_image)
            mName = aView.findViewById(R.id.name_tv)
            mRootlayout = aView.findViewById(R.id.root_layout)
        }
    }

    fun updateAdapter(
        aUList: List<Profile.FollowingDetails>
    ) {
        mUList = aUList
    }

}