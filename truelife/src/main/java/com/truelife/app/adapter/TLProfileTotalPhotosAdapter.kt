package com.truelife.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.truelife.R
import com.truelife.app.model.Profile
import com.truelife.util.Utility

/**
 * Created by Elango on 24-02-2020.
 */

class TLProfileTotalPhotosAdapter(
    var mContext: Context,
    var mData: ArrayList<Profile.Photos>
) : RecyclerView.Adapter<TLProfileTotalPhotosAdapter.ItemViewHolder>() {

    val mType = ArrayList<String>()
    val mURL = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_inflate_profile_total_photo_item, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val data = mData[position]
        Picasso.get().load(data.thumb)
            .placeholder(R.drawable.image_placeholder)
            .error(R.drawable.image_placeholder)
            .resize(150,150)
            .into(holder.mImage)

        holder.mImage.setOnClickListener {
            Utility.navigateImageView(mContext, getImages(), getTypes(), position)
        }
    }

    private fun getImages(): ArrayList<String> {
        mURL.clear()
        for (i in mData.indices) {
            mURL.add(mData[i].thumb)
        }
        return mURL
    }

    private fun getTypes(): ArrayList<String> {
        mType.clear()
        for (i in mData.indices) {
            mType.add("image")
        }
        return mType
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    open class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mImage = view.findViewById<ImageView>(R.id.layout_club_image)!!

    }

    interface Callback {
        fun info(position: Int)
    }
}
