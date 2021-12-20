package com.truelife.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.activity.ProfileActivity
import com.truelife.app.fragment.club.TLClubDetails
import com.truelife.app.model.DashboardSearchModel
import com.truelife.util.Utility
import org.apache.commons.lang3.StringEscapeUtils


class SearchFriendsListAdapter(
    val mContext: Context,
    val mValues: DashboardSearchModel.SearchInfo,
    val aType: Int
) :
    RecyclerView.Adapter<SearchFriendsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_search_friends_list_adapter, parent, false)
        return ViewHolder(v)
    }


    @SuppressLint("NewApi")
    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {

        if (aType == 1) {
            if (!mValues.friendsDetails!![position].profileImage.isNullOrEmpty())
               // Utility.loadImageWithGender(mValues.friendsDetails!![position].profileImage!!, aHolder.mImage,mValues.friendsDetails!![position].!!)
               Utility.loadImage(mValues.friendsDetails!![position].profileImage!!, aHolder.mImage)
            else aHolder.mImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_male))

            val aTitle: String =
                StringEscapeUtils.unescapeJava(mValues.friendsDetails!![position].friendName)
            aHolder.mText.text = aTitle

            aHolder.mLayout.setOnClickListener {
                val aIntent = Intent(mContext, ProfileActivity::class.java).putExtra(
                    "userid",
                    mValues.friendsDetails!![position].friendId!!
                )
                mContext.startActivity(aIntent)
            }
        } else {

            if (!mValues.clubDetails!![position].clubImage.isNullOrEmpty())
                Utility.loadImage(mValues.clubDetails!![position].clubImage!!, aHolder.mImage)
            else aHolder.mImage.setImageDrawable(mContext.getDrawable(R.drawable.club_placeholder))

            val aTitle: String =
                StringEscapeUtils.unescapeJava(mValues.clubDetails!![position].clubName)
            aHolder.mText.text = aTitle

            aHolder.mLayout.setOnClickListener {
                if (Utility.isInternetAvailable(mContext)) {
                    mContext.startActivity(
                        Intent(mContext, TLClubDetails::class.java)
                            .putExtra("Club_id", mValues.clubDetails!![position].id)
                    )
                }
            }
        }

    }


    override fun getItemCount(): Int {
        return if (aType == 1)
            mValues.friendsDetails!!.size
        else
            mValues.clubDetails!!.size
    }


    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {

        var mImage: ImageView = aView.findViewById(R.id.inflate_menu_list_icon)
        var mText: TextView = aView.findViewById(R.id.inflate_menu_list_name)
        var mLayout: LinearLayout = aView.findViewById(R.id.inflate_menu_container_LAY)
    }

}


