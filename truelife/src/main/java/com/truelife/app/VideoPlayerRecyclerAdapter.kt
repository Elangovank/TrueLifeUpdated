package com.truelife.app

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.truelife.R
import com.truelife.app.activity.ProfileActivity
import com.truelife.app.activity.YoutubePalyerActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.club.TLClubDetails
import com.truelife.app.model.PublicFeedModel.FeedList
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.FeedClickListener
import com.truelife.util.Helper


class VideoPlayerRecyclerAdapter(
    private var mediaObjects: ArrayList<FeedList>,
    private val requestManager: RequestManager,
    val mContext: FragmentActivity,
    val mCallBack: FeedClickListener,
    val searchText: String,
    val source: String
) : RecyclerView.Adapter<VideoPlayerViewHolder>() {

    var lastItemPosition: Int = -1
    var bottonLoadingEnable: Boolean = true
    private var videoVolumeState: Boolean = true
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): VideoPlayerViewHolder {
        return VideoPlayerViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(
                R.layout.layout_video_item,
                viewGroup,
                false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: VideoPlayerViewHolder, i: Int) {

        if (i != mediaObjects.size) {
            if (i == mediaObjects.size - 1 && bottonLoadingEnable) {
                viewHolder.progressLay.visibility = View.VISIBLE
            } else {
                viewHolder.progressLay.visibility = View.GONE
            }
            if (mediaObjects.get(i).id != null) {

                val mUser = LocalStorageSP.getLoginUser(mContext)
                var id = ""
                viewHolder.youThumbLay.setOnClickListener {
                    if (mediaObjects[i].isSharedPost != 1)

                        id = viewHolder.extractYTId(viewHolder.title.text.toString().trim())!!
                    else
                        id = viewHolder.extractYTId(viewHolder.sharedTitle.text.toString().trim())!!
                    val intent = Intent(mContext, YoutubePalyerActivity::class.java)
                    intent.putExtra("id", id)
                    mContext.startActivity(intent)

                }
                viewHolder.userImg.setOnClickListener {

                    if (mediaObjects[i].clubId == "0" || mediaObjects[i].clubId == null) {
                        val aIntent = Intent(mContext, ProfileActivity::class.java).putExtra(
                            "userid",
                            mediaObjects[i].userId
                        )
                        mContext.startActivity(aIntent)
                    } else {
                        mContext.startActivity(
                            Intent(mContext, TLClubDetails::class.java)
                                .putExtra("Club_id", mediaObjects[i].clubId)
                        )
                    }

                }


                viewHolder.likeCountLay.setOnClickListener {

                    mCallBack.like_details(i)
                }

                viewHolder.userName.setOnClickListener {

                    if (mediaObjects[i].clubId == "0" || mediaObjects[i].clubId == null) {
                        val aIntent = Intent(mContext, ProfileActivity::class.java).putExtra(
                            "userid",
                            mediaObjects[i].userId
                        )
                        mContext.startActivity(aIntent)
                    } else {
                        mContext.startActivity(
                            Intent(mContext, TLClubDetails::class.java)
                                .putExtra("Club_id", mediaObjects[i].clubId)
                        )
                    }
                }

                viewHolder.shareUserImg.setOnClickListener {
                    val aIntent = Intent(mContext, ProfileActivity::class.java).putExtra(
                        "userid",
                        mediaObjects[i].originalPostUserId
                    )
                    mContext.startActivity(aIntent)
                }

                viewHolder.sharedUserName.setOnClickListener {
                    val aIntent = Intent(mContext, ProfileActivity::class.java).putExtra(
                        "userid",
                        mediaObjects[i].originalPostUserId
                    )
                    mContext.startActivity(aIntent)
                }

                viewHolder.culb_shared_by.setOnClickListener {
                    val aIntent = Intent(mContext, ProfileActivity::class.java).putExtra(
                        "userid", mediaObjects[i].userId
                    )
                    mContext.startActivity(aIntent)
                }


                viewHolder.likeButtonLay.setOnClickListener {
                    mCallBack.like_click(i, mediaObjects[i].isUserLike.equals("0"))
                    if (mediaObjects[i].isUserLike.equals("0")) {

                        viewHolder.likeButtonLay.animate()
                            .scaleY(1.3f)
                            .scaleX(1.3f)
                            .setDuration(200)
                            .setInterpolator(LinearInterpolator())
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animator: Animator?) {
                                    viewHolder.likeButtonLay.scaleX = 1f
                                    viewHolder.likeButtonLay.scaleY = 1f
                                    viewHolder.likeImg.setImageResource(R.drawable.ic_like_red)
                                    mediaObjects[i].isUserLike = "1"
                                    mediaObjects[i].totalLikes =
                                        (mediaObjects[i].totalLikes!!.toInt() + 1).toString()
                                    try {
                                        viewHolder.likeCount.text = mediaObjects[i].totalLikes
                                        if (!mediaObjects[i].totalLikes.equals("0")) {
                                            viewHolder.likeLay.visibility = View.VISIBLE
                                            if (mediaObjects[i].totalLikes.equals("1"))
                                                viewHolder.like_count_suffix.text = "Like"
                                            else
                                                viewHolder.like_count_suffix.text = "Likes"
                                        } else if (mediaObjects[i].totalLikes.equals("0")) {
                                            viewHolder.likeLay.visibility = View.GONE
                                        }
                                    } catch (e: Exception) {
                                    }
                                    //  notifyDataSetChanged()
                                }
                            })

                    } else {
                        viewHolder.likeImg.setImageResource(R.drawable.ic_like_outline)
                        mediaObjects[i].isUserLike = "0"
                        mediaObjects[i].totalLikes =
                            (mediaObjects[i].totalLikes!!.toInt() - 1).toString()
                        //  notifyDataSetChanged()
                        try {
                            viewHolder.likeCount.text = mediaObjects[i].totalLikes
                            if (!mediaObjects[i].totalLikes.equals("0")) {
                                viewHolder.likeLay.visibility = View.VISIBLE
                                if (mediaObjects[i].totalLikes.equals("1"))
                                    viewHolder.like_count_suffix.text = "Like"
                                else
                                    viewHolder.like_count_suffix.text = "Likes"
                            } else if (mediaObjects[i].totalLikes.equals("0")) {
                                viewHolder.likeLay.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                        }
                    }

                }

                viewHolder.shareBtnLay.setOnClickListener {
                    val aUser = LocalStorageSP.getLoginUser(mContext)
                    if (aUser.mIsMobileVerified != "1") {
                        Helper.navigateOTPScreen(
                            mContext, mContext.getString(R.string.label_share_otp_verify), 3
                        )
                        return@setOnClickListener
                    }
                    mCallBack.share_click(i, viewHolder.singleImage)
                }

                viewHolder.commentBtnLay.setOnClickListener {

                    val aUser = LocalStorageSP.getLoginUser(mContext)
                    if (aUser.mIsMobileVerified != "1") {
                        Helper.navigateOTPScreen(
                            mContext, mContext.getString(R.string.label_comment_otp_verify), 2
                        )
                        return@setOnClickListener
                    }


                    if (source.equals("1") && mediaObjects[i].mPrivacy?.get(0)?.commentOnPublic.equals(
                            "3"
                        )
                        && !mediaObjects[i].userId.equals(mUser.mUserId)
                    )
                        AppDialogs.customOkAction(
                            mContext,
                            mContext.getString(R.string.app_name),
                            "Not allowed by the user",
                            "ok",
                            null,
                            false
                        )
                    else if (source.equals("1") && mediaObjects[i].mPrivacy?.get(0)?.commentOnPublic.equals(
                            "2"
                        )
                        && !mediaObjects[i].isFriend.equals("1")
                        && !mediaObjects[i].userId.equals(mUser.mUserId)
                    )
                        AppDialogs.customOkAction(
                            mContext,
                            mContext.getString(R.string.app_name),
                            "Not allowed by the user",
                            "ok",
                            null,
                            false
                        )
                    else if (source.equals("2") && mediaObjects[i].mPrivacy?.get(0)?.commentOnFrndsPosts.equals(
                            "3"
                        )
                        && !mediaObjects[i].userId.equals(mUser.mUserId)
                    )
                        AppDialogs.customOkAction(
                            mContext,
                            mContext.getString(R.string.app_name),
                            "Not allowed by the user",
                            "ok",
                            null,
                            false
                        )
                    else if (source.equals("2") && mediaObjects[i].mPrivacy?.get(0)?.commentOnFrndsPosts.equals(
                            "2"
                        )
                        && !mediaObjects[i].isFriend.equals("1")
                        && !mediaObjects[i].userId.equals(mUser.mUserId)
                    )
                        AppDialogs.customOkAction(
                            mContext,
                            mContext.getString(R.string.app_name),
                            "Not allowed by the user",
                            "ok",
                            null,
                            false
                        )
                    else
                        mCallBack.comment_click(i)
                }

                viewHolder.one_more_lay.setOnClickListener {
                    mCallBack.comment_click(i)
                }

                viewHolder.media_container.setOnClickListener {

                    mCallBack.video_preview(i, false, true, 0)
                }

                viewHolder.menu_button.setOnClickListener {
                    mCallBack.menu_click(i)
                }

                viewHolder.singleImage.setOnClickListener {
                    mCallBack.video_preview(i, false, false, 0)
                }

                viewHolder.multiRv.setOnClickListener {
                    mCallBack.video_preview(i, true, false, 0)
                }

                viewHolder.onBind(
                    mediaObjects[i],
                    requestManager,
                    mContext,
                    videoVolumeState,
                    mCallBack, i, searchText
                )


                viewHolder.title.setOnClickListener {
                    mediaObjects[i].content.let {
                        if (it!!.length > TLConstant.POST_TEXT_LIMIT) {
                            mCallBack.comment_click(i)

                        }
                    }

                }

                if (viewHolder.isYoutubeUrl(viewHolder.sharedTitle.text.toString())) {
                } else {
                    viewHolder.progressLay.visibility = View.GONE
                }
                viewHolder.sharedTitle.setOnClickListener {

                    if (viewHolder.sharedTitle.text.length > TLConstant.POST_TEXT_LIMIT) {
                        mCallBack.comment_click(i)
                    }
                }

            }
        }


    }

    override fun getItemCount(): Int {
        return if (bottonLoadingEnable)
            mediaObjects.size + 1
        else
            mediaObjects.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun changeVolumeState(isMute: Boolean) {
        this.videoVolumeState = isMute
        notifyDataSetChanged()
    }

    fun update(aMediaObjects: ArrayList<FeedList>) {
        this.mediaObjects = aMediaObjects
        notifyDataSetChanged()
    }

    fun stopBottomLoading() {
        try {
            this.bottonLoadingEnable = false

            notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showBottomLoading() {
        try {
            this.bottonLoadingEnable = true
            notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}