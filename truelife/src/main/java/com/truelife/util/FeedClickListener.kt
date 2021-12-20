package com.truelife.util

import android.widget.ImageView

interface FeedClickListener {

    fun like_details(position: Int)

    fun like_click( position: Int,isLike:Boolean)
    fun share_click( position: Int, imgView : ImageView)
    fun share_click( position: Int)
    fun comment_click( position: Int)
    fun menu_click( position: Int)
    fun video_preview(
        position: Int,
        isRecycler: Boolean,
        isVideo: Boolean,
        focus_pos: Int
    )
}