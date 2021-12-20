package com.truelife.app.fragment.chat.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.truelife.R
import com.truelife.app.fragment.chat.message.TLFeedDetailsVideo
import com.truelife.app.fragment.chat.message.TLScreenFeedPhotoView
import com.truelife.app.fragment.chat.profile.TLChatProfileImageAdapter.SingleItemRowHolder
import com.truelife.app.model.ChatProfile.ClubMedia
import com.truelife.app.viewer.TLDocFileViewer
import com.truelife.base.TLFragmentManager
import java.util.*

/**
 * Created by Elango on 19-07-2018.
 */
class TLChatProfileImageAdapter internal constructor(
    private val myContext: FragmentActivity,
    private val myMedia: ArrayList<ClubMedia>?
) : RecyclerView.Adapter<SingleItemRowHolder>() {
    private val myFragmentMananager: TLFragmentManager
    private val myImageListonly =
        ArrayList<String>()
    private val myMediaType =
        ArrayList<String>()
    private val myVideoListOnly =
        ArrayList<String>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SingleItemRowHolder {
        val v =
            LayoutInflater.from(myContext).inflate(R.layout.list_single_card, viewGroup, false)
        return SingleItemRowHolder(v)
    }

    override fun onBindViewHolder(holder: SingleItemRowHolder, position: Int) {
        val singleItem = myMedia!![position]
        if (singleItem.file_type == "image" || singleItem.file_type == "video") {
            holder.mImageLAY.visibility = View.VISIBLE
            holder.mDocuLAY.visibility = View.GONE
            if (singleItem.file_type == "image") {
                holder.myVideoPlayIMG.visibility = View.GONE
                holder.myThumbImg.visibility = View.GONE
                holder.itemImage.visibility = View.VISIBLE

                if (singleItem.url.isNotEmpty())
                    Picasso.get().load(singleItem.url)
                        .placeholder(R.drawable.new_feed_image_place_holder)
                        .error(R.drawable.new_feed_image_place_holder)
                        .into(holder.itemImage)

            } else {
                holder.myVideoPlayIMG.visibility = View.VISIBLE
                holder.myThumbImg.visibility = View.GONE
                holder.itemImage.visibility = View.VISIBLE

                if (singleItem.thumb != "" && singleItem.thumb.isNotEmpty()) {
                    Picasso.get().load(singleItem.thumb)
                        .placeholder(R.drawable.new_feed_image_place_holder)
                        .error(R.drawable.new_feed_image_place_holder)
                        .into(holder.itemImage)
                }
            }
            holder.itemImage.setOnClickListener {
                myImageListonly.clear()
                myMediaType.clear()
                myVideoListOnly.clear()
                for (i in myMedia.indices) {
                    if (myMedia[i].file_type == "image") {
                        myImageListonly.add(java.lang.String.valueOf(myMedia[i].url))
                        myMediaType.add("image")
                    } else {
                        myVideoListOnly.add(java.lang.String.valueOf(myMedia[i].thumb))
                        myMediaType.add("video")
                    }
                }
                if (singleItem.file_type == "image") {
                    val aBundle = Bundle()
                    val gson = Gson()
                    val jsonMediaType = gson.toJson(myMediaType)
                    val json = gson.toJson(myImageListonly)
                    aBundle.putString("feed_images", json)
                    aBundle.putString("feed_images_Type", jsonMediaType)
                    aBundle.putInt("Position", position)
                    myFragmentMananager.addContent(
                        TLScreenFeedPhotoView(),
                        TLScreenFeedPhotoView.TAG,
                        aBundle
                    )
                    // myImageListonly.clear();
                } else {
                    val aBundle = Bundle()
                    aBundle.putString("Video_url", singleItem.thumb)
                    myFragmentMananager.addContent(
                        TLFeedDetailsVideo(),
                        TLFeedDetailsVideo.TAG,
                        aBundle
                    )
                }
            }
        } else {
            if (singleItem.file_type == "document") {
                holder.mImageLAY.visibility = View.GONE
                holder.mDocuLAY.visibility = View.VISIBLE
                holder.myVideoPlayIMG.visibility = View.GONE
                holder.myThumbImg.visibility = View.GONE
                val filename =
                    singleItem.url.substring(singleItem.url.lastIndexOf("/") + 1)
                holder.mDocuNameTXT.text = filename
                holder.mDocIMG.setImageDrawable(myContext.resources.getDrawable(R.drawable.msg_file_placeholder))
                holder.mDocuLAY.setOnClickListener {
                    val aBundle = Bundle()
                    aBundle.putString("FILE_PATH", singleItem.url)
                    myFragmentMananager.addContent(
                        TLDocFileViewer(),
                        TLDocFileViewer.TAG,
                        aBundle
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return myMedia?.size ?: 0
    }

    inner class SingleItemRowHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        var itemImage: ImageView
        var myThumbImg: ImageView
        var myVideoPlayIMG: ImageView
        var mDocIMG: ImageView
        var mImageLAY: LinearLayout
        var mDocuLAY: LinearLayout
        var mDocuNameTXT: TextView

        init {
            itemImage = view.findViewById(R.id.itemImage)
            myThumbImg =
                view.findViewById(R.id.layout_inflate_feed_details_IMG_feed_thumb)
            myVideoPlayIMG =
                view.findViewById(R.id.layout_inflate_feed_details_video_play_IMG)
            mDocuLAY = view.findViewById(R.id.doc_list_Image_LAY)
            mImageLAY = view.findViewById(R.id.image_list_LAY)
            mDocIMG = view.findViewById(R.id.doc_image)
            mDocuNameTXT = view.findViewById(R.id.doc_name)
        }
    }

    init {
        myFragmentMananager = TLFragmentManager(myContext)
    }
}