package com.truelife.app.fragment.club.media

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.truelife.R
import com.truelife.app.model.Club
import com.truelife.app.touchimageview.TLMediaPreviewActivity
import com.truelife.util.Utility
import java.util.*

/**
 * Created by Elango on 21-02-2020.
 */
class TLClubMediaItemsAdapter internal constructor(
    private val myContext: FragmentActivity,
    private val myMedia: ArrayList<Club.Media>?
) : RecyclerView.Adapter<TLClubMediaItemsAdapter.SingleItemRowHolder>() {

    private val mMediaList = ArrayList<String>()
    var mMediaType = ArrayList<String>()


    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): SingleItemRowHolder {
        val v =
            LayoutInflater.from(myContext).inflate(R.layout.list_single_card, viewGroup, false)
        return SingleItemRowHolder(v)
    }

    override fun onBindViewHolder(
        holder: SingleItemRowHolder,
        position: Int
    ) {
        val singleItem = myMedia!![position]
        holder.itemImage.visibility = View.VISIBLE
        mMediaType.add(singleItem.media_type!!)
        if (singleItem.media_type == "image") {
            mMediaList.add(myMedia[position].thumb!!)
            holder.myVideoPlayIMG.visibility = View.GONE
            holder.myThumbImg.visibility = View.GONE
        } else {
            mMediaList.add(myMedia[position].original!!)
            holder.myVideoPlayIMG.visibility = View.VISIBLE
            holder.myThumbImg.visibility = View.GONE
        }

        Utility.loadImage(singleItem.thumb!!, holder.itemImage, R.drawable.ic_place_club)

        holder.itemImage.setOnClickListener {
            val intent = Intent(myContext, TLMediaPreviewActivity::class.java)
            val aBundle = Bundle()
            val gson = Gson()
            val json = gson.toJson(mMediaList)
            aBundle.putString("feed_images", json)
            aBundle.putString("feed_images_Type", gson.toJson(mMediaType))
            aBundle.putInt("Position", position)
            intent.putExtra("bundle", aBundle)
            intent.putExtra("isImage", true)
            myContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return myMedia?.size ?: 0
    }

    inner class SingleItemRowHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        var itemImage: ImageView = view.findViewById(R.id.itemImage)
        var myThumbImg: ImageView =
            view.findViewById(R.id.layout_inflate_feed_details_IMG_feed_thumb)
        var myVideoPlayIMG: ImageView =
            view.findViewById(R.id.layout_inflate_feed_details_video_play_IMG)
        var myImageLAY: LinearLayout = view.findViewById(R.id.image_list_LAY)

        init {
            myImageLAY.visibility = View.VISIBLE
        }
    }

}