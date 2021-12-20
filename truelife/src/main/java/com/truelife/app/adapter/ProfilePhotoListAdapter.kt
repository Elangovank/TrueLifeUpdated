package com.truelife.app.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.truelife.ProfileClickListener
import com.truelife.R
import com.truelife.app.model.Profile
import com.truelife.util.Utility
import java.util.*

/**
 * Created by Elango on 18-12-2019.
 */
class ProfilePhotoListAdapter(
    private val mContext: FragmentActivity,
    var mUList: List<Profile.PhotoList>, val listener: ProfileClickListener
) : RecyclerView.Adapter<ProfilePhotoListAdapter.ViewHolder>() {
    private val mMediaList = ArrayList<String>()
    var mMediaType = ArrayList<String>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View = LayoutInflater.from(mContext)
            .inflate(R.layout.inflate_profile_image_list_item, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(aHolder: ViewHolder, position: Int) {
        var data = getNewsInfo(position)

        // aHolder.mName!!.text = data.userName
        //  if (!data.photos!!.endsWith(".jpg")) {
        if (!data.photos.isNullOrEmpty()) {
            mMediaList.add(data.photos!!)
            mMediaType.add("image")
            data.photos?.let {
                Utility.loadImage(
                    it,
                    aHolder.mCropImage,
                    R.drawable.new_feed_image_place_holder
                )
            }
        } else {
            aHolder.mCropImage.setImageDrawable(mContext.getDrawable(R.drawable.image_placeholder))
        }


        aHolder.mCropImage.setOnClickListener {
            Utility.navigateImageView(mContext, mMediaList, mMediaType, position)
        }
        //  }
        /*  else
              Utility.loadImage(it, aHolder.mProfileimage)*/
        /*  aHolder.myClubLayout!!.setOnClickListener {

              listener.click(position, "friends")
          }*/

    }

    fun getNewsInfo(position: Int): Profile.PhotoList {
        return mUList[position]
    }

    override fun getItemCount(): Int {
        return mUList.size
    }


    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {
        // val mProfileimage: CircleImageView
        val mCropImage: ImageView
        val mVideoIMG: ImageView
        val myClubLayout: RelativeLayout
        val mName: TextView

        init {
            mCropImage = aView.findViewById(R.id.layout_club_image) as ImageView
            mVideoIMG =
                aView.findViewById(R.id.layout_inflate_feed_details_video_play_IMG) as ImageView
            myClubLayout = aView.findViewById(R.id.layout_club_linear_layout)
            mName = aView.findViewById(R.id.layout_club_name)
        }
    }

    fun updateAdapter(
        aUList: List<Profile.PhotoList>
    ) {
        mUList = aUList
    }

}