package com.truelife.app.activity.feedpost.adapter

import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.truelife.R
import com.truelife.util.Utility
import java.io.File
import java.util.*

/**
 * Created by Elango on 18-12-2019.
 */
class TLFeedPostImageListAdapter(
    private val myContext: Context,
    var myMeadiaList: ArrayList<String>
) : RecyclerView.Adapter<TLFeedPostImageListAdapter.ViewHolder>() {


    //  var myMeadiaList = ArrayList<String>()

    private val filePaths: ArrayList<String>? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(myContext)
            .inflate(R.layout.inflate_club_by_you_media, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        val uri = Uri.fromFile(File(getNewsInfo(position)))
        Picasso.get().load(uri).resize(750, 750).centerCrop().into(aHolder.myMenuImageIcon)

        val thumb = ThumbnailUtils.createVideoThumbnail(
            uri.path,
            MediaStore.Video.Thumbnails.MINI_KIND
        )
        if (Utility.findMediaType(uri.toString()).equals("video")) {
            aHolder.myVideoplayBtn.visibility = View.VISIBLE
        } else {
            aHolder.myVideoplayBtn.visibility = View.GONE
        }
        if (thumb != null) {
            aHolder.myMenuImageIcon.maxWidth = 750
            aHolder.myMenuImageIcon.maxHeight = 750
            aHolder.myMenuImageIcon.setImageBitmap(thumb)
        }
        /*myOption = TLHelper.displayOption(myContext);
        myImageLoader.displayImage( myMeadiaList.get(position), aHolder.myMenuImageIcon, myOption);*/
    }

    fun getNewsInfo(position: Int): String {
        return myMeadiaList[position]
    }

    override fun getItemCount(): Int {
        return myMeadiaList.size
    }

    /**
     * interface onclick
     */
    interface ClickListener {
        fun onClick(view: View?, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    /* */
    /**
     * RecyclerTouchListener
     *//*
    class RecyclerTouchListener(
        context: Context?,
        recyclerView: RecyclerView,
        private val clickListener: ClickListener?
    ) : OnItemTouchListener {
        // GestureDetector handling singleClick or longClick
        private val gestureDetector: GestureDetector

        override fun onInterceptTouchEvent(
            rv: RecyclerView,
            e: MotionEvent
        ): Boolean {
            val child = rv.findChildViewUnder(e.x, e.y)
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child))
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

        init {
            gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    val child =
                        recyclerView.findChildViewUnder(e.x, e.y)
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child))
                    }
                }
            })
        }
    }*/

    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {
        val myMenuImageIcon: ImageView
        val myVideoplayBtn: ImageView

        init {
            myMenuImageIcon =
                aView.findViewById(R.id.inflate_club_by_you_iamge)
            myVideoplayBtn = aView.findViewById(R.id.video_play_IMG_one)
        }
    }
}