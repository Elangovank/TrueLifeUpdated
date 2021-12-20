package com.truelife.app.fragment.club.more.invite

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.Club
import com.truelife.util.Utility
import java.util.*

/**
 * Created by Elango on 19-02-2020.
 */

class TLClubInviteAdapter(
    var mContext: Context,
    var mData: ArrayList<Club.FriendList>,
    var mCallback: Callback
) : RecyclerView.Adapter<TLClubInviteAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_inflate_general_public_area_item, parent, false)
        return ItemViewHolder(itemView)
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(
        holder: ItemViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val data = mData[position]

        holder.mName.text = data.fullname

        if (data.isSelected)
            holder.mTick.visibility = View.VISIBLE
        else holder.mTick.visibility = View.GONE

        if (!data.profile_image.isNullOrEmpty())
            Utility.loadImage(data.profile_image, holder.mImage)
        else holder.mImage.setImageResource(R.color.app_red)

        holder.mMainLayout.setOnClickListener {
            mCallback.info(data.id!!)
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    open class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mMainLayout = view.findViewById<CardView>(R.id.layout_club_linear_layout)!!
        var mName = view.findViewById<TextView>(R.id.layout_club_name)!!
        var mTick = view.findViewById<ImageView>(R.id.inflate_menu_right_arrow)!!
        var mImage = view.findViewById<ImageView>(R.id.layout_club_image)!!

    }

    interface Callback {
        fun info(id: String)
    }
}
