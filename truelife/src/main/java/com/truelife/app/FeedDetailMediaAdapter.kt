package com.truelife.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.truelife.ClickListener
import com.truelife.R
import com.truelife.app.model.PublicFeedModel


class FeedDetailMediaAdapter(val mList: List<PublicFeedModel.Medium>, val callback: ClickListener) :
    RecyclerView.Adapter<FeedDetailMediaAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.inflate_detail_media_item, parent, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mList[position].mediaType.equals("video")) {
            holder.mSignImage.visibility = View.VISIBLE
            holder.mVideoThumb.visibility = View.VISIBLE
            holder.mImage.visibility = View.GONE

            Picasso.get()
                .load(mList[position].thumb)
                .placeholder(R.drawable.new_feed_image_place_holder)
                .error(R.drawable.new_feed_image_place_holder)
                .into(holder.mVideoThumb)

        } else {
            holder.mSignImage.visibility = View.GONE
            holder.mVideoThumb.visibility = View.GONE
            holder.mImage.visibility = View.VISIBLE

            Picasso.get()
                .load(mList[position].thumb)
                .placeholder(R.drawable.new_feed_image_place_holder)
                .error(R.drawable.new_feed_image_place_holder)
                .into(holder.mImage)
        }

        holder.mRoot.setOnClickListener {
            callback.click(position);
        }


    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mImage: ImageView = itemView.findViewById(R.id.image)
        val mVideoThumb: ImageView = itemView.findViewById(R.id.video)
        val mSignImage: ImageView = itemView.findViewById(R.id.play_sign)
        val mRoot: RelativeLayout = itemView.findViewById(R.id.root_layout)
    }


}

