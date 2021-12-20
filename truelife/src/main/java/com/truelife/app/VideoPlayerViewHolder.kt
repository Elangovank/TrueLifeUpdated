package com.truelife.app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.squareup.picasso.Picasso
import com.truelife.ClickListener
import com.truelife.R
import com.truelife.app.activity.YoutubePalyerActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.PublicFeedModel.FeedList
import com.truelife.app.viewer.TLWebViewView
import com.truelife.util.FeedClickListener
import com.truelife.util.Helper
import com.truelife.util.Utility
import org.apache.commons.lang3.StringEscapeUtils
import java.util.*
import java.util.regex.Pattern


class VideoPlayerViewHolder(var parent: View) :
    RecyclerView.ViewHolder(parent) {
    var media_container: FrameLayout
    var myFeedDate_TXT: TextView
    var userName: TextView
    var title: TextView
    var aTitle: String = ""
    var lable_shared: TextView
    var culb_shared_by: TextView
    var sharedTitle: TextView
    var sharedUserName: TextView
    var sharedFeedDate_TXT: TextView
    var shareLay: RelativeLayout
    var shareUserImg: ImageView
    var images_rv: RecyclerView
    var imageCount: TextView
    var seeAll: TextView


    var itemlay: LinearLayout
    var parentLay: LinearLayout
    var likeCountLay: LinearLayout
    var progressLay: ProgressBar

    var imageListLay: LinearLayout

    var thumbnail: ImageView
    var youtubeThumbnail: ImageView
    var replay: RelativeLayout
    var youThumbLay: RelativeLayout
    var userImg: com.truelife.util.TLCircularImageView
    var singleImage: ImageView
    var volumeControl: ImageView
    var progressBar: ProgressBar
    var dropDownMenu: ImageView
    var requestManager: RequestManager? = null
    var isVideo: Boolean = false
    var likeCount: TextView
    var likeLay: LinearLayout
    var likeButtonLay: LinearLayout
    var shareBtnLay: LinearLayout
    var commentBtnLay: LinearLayout
    var likeImg: ImageView
    var likeSep: View
    var commnetSep: View
    var shareBottom: View
    var commentCount: TextView
    var commentText: TextView
    var commentLay: LinearLayout
    var counterSep: View
    var switchToHD: ImageView
    var isHD: Boolean = false

    var shareCount: TextView
    var shareSuffix: TextView
    var shareCountLay: LinearLayout
    lateinit var mContext: FragmentActivity
    lateinit var mCallBack: FeedClickListener

    var like_suffix: TextView
    var like_count_suffix: TextView

    var multiRv: RecyclerView
    var menu_button: ImageView
    var one_more_lay: RelativeLayout

    private val urlPattern: Pattern = Pattern.compile(
        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
        Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
    )

    @SuppressLint("SetTextI18n")
    fun onBind(
        mediaObject: FeedList,
        requestManager: RequestManager?,
        mContext: FragmentActivity,
        videoVolumeState: Boolean,
        aCallBack: FeedClickListener,
        i: Int,
        searchText: String
    ) {

        this.mContext = mContext
        this.mCallBack = aCallBack
        try {
            itemlay.visibility = View.VISIBLE
            progressLay.visibility = View.GONE
            //title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
            youtubeThumbnail.visibility = View.GONE
            youThumbLay.visibility = View.GONE

            if (mediaObject.isSharedPost == 1) {

                shareLay.visibility = View.VISIBLE
                shareBottom.visibility = View.VISIBLE
                lable_shared.visibility = View.VISIBLE
                sharedTitle.visibility = View.VISIBLE
                culb_shared_by.visibility = View.GONE
                sharedUserName.text = mediaObject.originalPostUser
                myFeedDate_TXT.text = Helper.postTimeConvertFormat(mediaObject.postedTime)
                if (!mediaObject.originalPostUserImage.isNullOrEmpty() && !mediaObject.originalPostUserImage.equals(
                        ""
                    )
                )
                    Picasso.get().load(mediaObject.originalPostUserImage).into(shareUserImg)
                else
                    Utility.loadPlaceHolder(
                        mContext,
                        mediaObject.originalPostUserGender!!,
                        shareUserImg
                    )

                val aTitle: String = StringEscapeUtils.unescapeJava(mediaObject.sharePostContent!!)
                val aOriginal: String = StringEscapeUtils.unescapeJava(mediaObject.content!!)

                if (aOriginal.equals("") || aOriginal.isEmpty())
                    sharedTitle.visibility = View.GONE


                if (!searchText.isEmpty() && !searchText.equals("")) {

                    if (!aTitle.isEmpty()) {
                        title.visibility = View.VISIBLE
                        highlightString(searchText, aTitle, title)
                    } else {
                        title.visibility = View.GONE
                    }
                    if (!aOriginal.isEmpty()) {
                        sharedTitle.visibility = View.VISIBLE
                        highlightString(searchText, aOriginal, sharedTitle)
                    } else {
                        sharedTitle.visibility = View.GONE
                    }
                    /* val newString: String = aTitle.replace(
                         searchText,
                         "<span style=\"\n" +
                                 "    background: yellow;\n" +
                                 "\">" + searchText.toString() + "</span>", true
                     )*/

                    /*   val newOgString: String = aOriginal.replace(
                           searchText,
                           "<span style=\"\n" +
                                   "    background: yellow;\n" +
                                   "\">" + searchText.toString() + "</span>", true
                       )

                      title.text = Html.fromHtml(newString)
                       sharedTitle.text = Html.fromHtml(newOgString)*/
                } else {
                    setTitle(aTitle, title)
                    // title.text = aTitle
                    //  sharedTitle.text = aOriginal
                    setTitle(aOriginal, sharedTitle)

                }


                sharedFeedDate_TXT.text = Helper.postTimeConvertFormat(mediaObject.originalPostTime)
                userName.text = mediaObject.username
                if (!mediaObject.userImage.isNullOrEmpty() && !mediaObject.userImage.equals(""))
                    Picasso.get().load(mediaObject.userImage).into(userImg)
                else
                    Utility.loadPlaceHolder(mContext, mediaObject.gender!!, userImg)
                if (!mediaObject.clubId.equals("0") && !mediaObject.clubId.isNullOrEmpty())
                    lable_shared.text =
                        mContext.getString(R.string.lable_shared_a_post) + " to " + mediaObject.clubName


            } else {
                shareLay.visibility = View.GONE
                shareBottom.visibility = View.GONE
                lable_shared.visibility = View.GONE
                sharedTitle.visibility = View.GONE
                if (!mediaObject.clubId.equals("0") && !mediaObject.clubId.isNullOrEmpty()) {
                    culb_shared_by.visibility = View.VISIBLE
                    culb_shared_by.text = "by " + mediaObject.username
                    userName.text = mediaObject.clubName
                    userImg.setImageResource(R.drawable.club_placeholder)
                    if (!mediaObject.clubImage.isNullOrEmpty() && !mediaObject.clubImage.equals(""))
                        Utility.loadImageWithClubplaceholder(mediaObject.clubImage!!, userImg)

                    //    Picasso.get().load(mediaObject.clubImage).into(userImg)
                } else {
                    culb_shared_by.visibility = View.GONE
                    userName.text = mediaObject.username
                    if (!mediaObject.userImage.isNullOrEmpty() && !mediaObject.userImage.equals(""))
                        Picasso.get().load(mediaObject.userImage).into(userImg)
                    else
                        Utility.loadPlaceHolder(mContext, mediaObject.gender!!, userImg)
                }

                myFeedDate_TXT.text = Helper.postTimeConvertFormat(mediaObject.postedTime)
                aTitle = StringEscapeUtils.unescapeJava(mediaObject.content!!)
                if (!aTitle.isEmpty()) {
                    title.visibility = View.VISIBLE
                } else {
                    title.visibility = View.GONE
                }
                if (!searchText.isNullOrEmpty() && !searchText.equals("")) {

                    highlightString(searchText, aTitle, title)

                } else {
                    setTitle(aTitle, title)
                }


            }


            if (videoVolumeState)
                volumeControl.setImageResource(R.drawable.ic_unmute)
            else
                volumeControl.setImageResource(R.drawable.ic_mute)

            /**
             * Setting up likes
             */
            likeCount.text = mediaObject.totalLikes
            if (!mediaObject.totalLikes.equals("0")) {
                likeLay.visibility = View.VISIBLE
                if (mediaObject.totalLikes.equals("1"))
                    like_count_suffix.text = "Like"
                else
                    like_count_suffix.text = "Likes"
            } else if (mediaObject.totalLikes.equals("0")) {
                likeLay.visibility = View.GONE
            }
            if (mediaObject.isUserLike.equals("1")) {
                likeImg.setImageResource(R.drawable.ic_like_red)
                like_suffix.text = "Liked"
            } else {
                likeImg.setImageResource(R.drawable.ic_like_outline)
                like_suffix.text = "Like"
            }
            /**
             * Setting up share
             */
            shareCount.text = mediaObject.totalShares
            if (!mediaObject.totalShares.equals("0")) {
                shareCountLay.visibility = View.VISIBLE
                if (Integer.parseInt(mediaObject.totalShares!!) > 1) {
                    shareSuffix.text = "Shares"
                } else {
                    shareSuffix.text = "Share"
                }
            } else
                shareCountLay.visibility = View.INVISIBLE

            /**
             * Setting up separator
             */

            if (!mediaObject.totalLikes.equals("0") && !mediaObject.totalComments.equals("0"))
                likeSep.visibility = View.VISIBLE
            else
                likeSep.visibility = View.INVISIBLE


            if (!mediaObject.totalComments.equals("0") && !mediaObject.totalShares.equals("0"))
                commnetSep.visibility = View.VISIBLE
            else
                commnetSep.visibility = View.INVISIBLE


            if (!mediaObject.totalComments.equals("0")
                || !mediaObject.totalShares.equals("0")
                || !mediaObject.totalLikes.equals("0")
            )
                counterSep.visibility = View.VISIBLE
            else
                counterSep.visibility = View.INVISIBLE
            /**
             * Setting up comments
             */

            if (!mediaObject.totalComments.equals("0")) {
                commentLay.visibility = View.VISIBLE
                commentCount.text = mediaObject.totalComments
                if (Integer.parseInt(mediaObject.totalComments!!) > 1) {
                    commentText.text = "Comments"
                } else {
                    commentText.text = "Comment"
                }


            } else
                commentLay.visibility = View.INVISIBLE

            if (mediaObject.media!!.size == 5)
                one_more_lay.visibility = View.VISIBLE
            else
                one_more_lay.visibility = View.GONE

            if (mediaObject.media!!.size == 1) {
                multiRv.visibility = View.GONE
                if (mediaObject.mediaType.equals("video")) {
                    this.requestManager = requestManager
                    singleImage.visibility = View.GONE
                    media_container.visibility = View.VISIBLE
                    volumeControl.visibility = View.VISIBLE
                    this.isVideo = true
                    parent.tag = this
                    Picasso.get().load(mediaObject.media!![0].thumb).into(thumbnail)
                    switchToHD.visibility = View.VISIBLE

                } else if (mediaObject.mediaType.equals("image")) {
                    this.isVideo = false
                    this.requestManager = requestManager
                    singleImage.visibility = View.VISIBLE
                    media_container.visibility = View.GONE
                    parent.tag = this

                    Picasso.get()
                        .load(mediaObject.media!![0].thumb)
                        .placeholder(R.drawable.new_feed_image_place_holder)
                        .error(R.drawable.new_feed_image_place_holder)
                        .into(singleImage)

                    switchToHD.visibility = View.GONE
                    volumeControl.visibility = View.GONE
                }
            } else if (mediaObject.media!!.size > 1) {
                multiRv.visibility = View.VISIBLE
                singleImage.visibility = View.GONE
                media_container.visibility = View.GONE
                switchToHD.visibility = View.GONE
                volumeControl.visibility = View.GONE
                val gridLayoutManager = GridLayoutManager(mContext, 2)
                gridLayoutManager.spanSizeLookup =
                    (object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            if (mediaObject.media!!.size == 3) {
                                if (position == 0 || position == 1)
                                    return 1
                                else
                                    return 2
                            } else if (mediaObject.media!!.size > 3)
                                return 1
                            else
                                return 2
                        }
                    })
                multiRv.layoutManager = gridLayoutManager
                multiRv.isNestedScrollingEnabled = false
                val mAdapter = FeedMultiImagesAdapter(mediaObject.media!!, object : ClickListener {
                    override fun click(position: Int) {
                        mCallBack.video_preview(i, true, false, position)
                    }
                })
                multiRv.adapter = mAdapter


            } else {
                multiRv.visibility = View.GONE
                this.isVideo = false
                this.requestManager = requestManager
                singleImage.visibility = View.GONE
                media_container.visibility = View.GONE
                parent.tag = this
               /* if (mediaObject.isSharedPost != 1)
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F);
                else
                    //sharedTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F);*/
                switchToHD.visibility = View.GONE
                volumeControl.visibility = View.GONE
            }

            clickableLink(title.text.toString().trim(), mediaObject)

            if (sharedTitle.visibility == View.VISIBLE) {
                clickableLinkSharedTitle(sharedTitle.text.toString(), mediaObject )
            }
            /*if (android.util.Patterns.WEB_URL.matcher(title.text.toString().trim()).matches()) {
           // if (title.text.toString().trim().contains(android.util.Patterns.WEB_URL.pattern()) {

                if (isYoutubeUrl(title.text.toString()) && mediaObject.media!!.size == 0 && mediaObject.isSharedPost != 1) {
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
                    title.setPaintFlags(title.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
                    title.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                    val ytThumbUrl =
                        "http://img.youtube.com/vi/" + extractYTId(aTitle) + "/mqdefault.jpg"
                    youtubeThumbnail.visibility = View.VISIBLE
                    youThumbLay.visibility = View.VISIBLE
                    Picasso.get()
                        .load(ytThumbUrl)
                        .placeholder(R.drawable.new_feed_image_place_holder)
                        .error(R.drawable.new_feed_image_place_holder)
                        .into(youtubeThumbnail)
                } else {
                    youtubeThumbnail.visibility = View.GONE
                    youThumbLay.visibility = View.GONE
                    if (isYoutubeUrl(title.text.toString())) {
                        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
                        title.setPaintFlags(title.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
                        title.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                    } else {
                        Linkify.addLinks(title, Linkify.WEB_URLS)
                        title.setLinksClickable(true)
                        title.setLinkTextColor(mContext.resources.getColor(R.color.colorPrimary))
                    }
                }
            } else {
                youtubeThumbnail.visibility = View.GONE
                youThumbLay.visibility = View.GONE
                title.setPaintFlags(title.getPaintFlags() and Paint.UNDERLINE_TEXT_FLAG.inv())
                title.setLinksClickable(false)
                title.setTextColor(mContext.resources.getColor(R.color.black))
            }*/

            /*   if (sharedTitle.visibility == View.VISIBLE) {
                   if (android.util.Patterns.WEB_URL.matcher(sharedTitle.text.toString().trim())
                           .matches()
                   ) {
                       *//*Linkify.addLinks(sharedTitle, Linkify.WEB_URLS)
                    sharedTitle.setLinksClickable(true)
                    sharedTitle.setLinkTextColor(mContext.resources.getColor(R.color.colorPrimary))*//*
                    if (isYoutubeUrl(sharedTitle.text.toString()) && mediaObject.media!!.size == 0) {
                        sharedTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
                        sharedTitle.setPaintFlags(sharedTitle.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
                        sharedTitle.setTextColor(
                            ContextCompat.getColor(
                                mContext,
                                R.color.colorPrimary
                            )
                        );
                        val ytThumbUrl =
                            "http://img.youtube.com/vi/" + extractYTId(sharedTitle.text.toString()) + "/mqdefault.jpg"
                        youtubeThumbnail.visibility = View.VISIBLE
                        youThumbLay.visibility = View.VISIBLE
                        Picasso.get()
                            .load(ytThumbUrl)
                            .placeholder(R.drawable.new_feed_image_place_holder)
                            .error(R.drawable.new_feed_image_place_holder)
                            .into(youtubeThumbnail)
                    } else {
                        Linkify.addLinks(sharedTitle, Linkify.WEB_URLS)
                        sharedTitle.setLinksClickable(true)
                        sharedTitle.setLinkTextColor(mContext.resources.getColor(R.color.colorPrimary))
                    }
                }
            }*/

        } catch (e: Exception) {
            print(e.toString())
        }
    }

    fun isYoutubeUrl(youTubeURl: String): Boolean {
        val pattern: Regex =
            "^(http(s)?:\\/\\/)?((w){3}.)?((m){1}.)?youtu(be|.be)?(\\.com)?\\/.+".toRegex()

        return youTubeURl.trim().matches(pattern)
    }

    fun extractYTId(ytUrl: String?): String? {
        var vId: String? = null
        val pattern = "(?<=watch\\?v=|/videos/|embed/|.be\\/)[^#\\&\\?]*"
        val compiledPattern = Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(ytUrl)
        if (matcher.find()) {
            vId = matcher.group()
        }
        return vId
    }

    private fun highlightString(
        input: String,
        wholeString: String,
        mTextView: TextView
    ) { //Get the text from text view and create a spannable string
        val spannableString = SpannableString(wholeString)
        //Get the previous spans and remove them
        val backgroundSpans =
            spannableString.getSpans(
                0, spannableString.length,
                BackgroundColorSpan::class.java
            )
        for (span in backgroundSpans) {
            spannableString.removeSpan(span)
        }
        //Search for all occurrences of the keyword in the string
        var indexOfKeyword = spannableString.toString().toLowerCase(Locale.ENGLISH).indexOf(
            input.toLowerCase(
                Locale.ENGLISH
            )
        )
        while (indexOfKeyword >= 0) { //Create a background color span on the keyword
            spannableString.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                indexOfKeyword,
                indexOfKeyword + input.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            //Get the next index of the keyword
            indexOfKeyword =
                spannableString.toString().indexOf(input, indexOfKeyword + input.length)
        }
        //Set the final text on TextView
        mTextView.setText(spannableString)
    }

    init {
        media_container = parent.findViewById(R.id.media_container)
        userImg = parent.findViewById(R.id.feed_user_image)
        singleImage = parent.findViewById(R.id.imageViewSingle)
        userName = parent.findViewById(R.id.feed_user_name)
        myFeedDate_TXT = parent.findViewById(R.id.feed_date_time)
        thumbnail = parent.findViewById(R.id.thumbnail)
        youtubeThumbnail = parent.findViewById(R.id.youtube_thumb)
        replay = parent.findViewById(R.id.replay)
        youThumbLay = parent.findViewById(R.id.youtube_thumb_lay)
        title = parent.findViewById(R.id.title)
        progressBar = parent.findViewById(R.id.progressBar)
        volumeControl = parent.findViewById(R.id.volume_control)
        dropDownMenu = parent.findViewById(R.id.layout_inflate_feed_details_IMG_down_arrow)
        likeCount = parent.findViewById(R.id.layout_inflate_feed_details_TXT_like_count_TXT)
        likeLay = parent.findViewById(R.id.layout_inflate_feed_details_TXT_like_count_LAY)
        likeButtonLay = parent.findViewById(R.id.layout_inflate_feed_details_LAY_like_IMG)
        commentBtnLay = parent.findViewById(R.id.comment_btn)
        shareBtnLay = parent.findViewById(R.id.share_btn)
        likeImg = parent.findViewById(R.id.layout_inflate_feed_details_LB_like)
        likeSep = parent.findViewById(R.id.likeview)
        commentCount = parent.findViewById(R.id.layout_inflate_feed_details_TXT_comments_count_TXT)
        commentText = parent.findViewById(R.id.layout_inflate_feed_details_TXT_comments_count)
        commentLay = parent.findViewById(R.id.layout_inflate_feed_details_TXT_comments_count_LAY)
        commnetSep = parent.findViewById(R.id.comment_sep)
        shareBottom = parent.findViewById(R.id.sep_share_bottom)
        counterSep = parent.findViewById(R.id.counter_seperator)
        switchToHD = parent.findViewById(R.id.quality_control)
        lable_shared = parent.findViewById(R.id.lable_shared_post)
        sharedTitle = parent.findViewById(R.id.shared_title)
        sharedUserName = parent.findViewById(R.id.feed_shared_user_name)
        sharedFeedDate_TXT = parent.findViewById(R.id.feed_shared_date_time)
        shareLay = parent.findViewById(R.id.feed_shared_profile_LAY)
        shareUserImg = parent.findViewById(R.id.feed_shared_user_image)
        culb_shared_by = parent.findViewById(R.id.club_post_shared_by)
        itemlay = parent.findViewById(R.id.item_lay)
        progressLay = parent.findViewById(R.id.progress_bar_LAY)
        shareCount = parent.findViewById(R.id.share_count)
        shareCountLay = parent.findViewById(R.id.share_count_lay)
        shareSuffix = parent.findViewById(R.id.share_suffix)
        multiRv = parent.findViewById(R.id.multi_medial_rv)
        menu_button = parent.findViewById(R.id.layout_inflate_feed_details_IMG_down_arrow)
        one_more_lay = parent.findViewById(R.id.one_more_lay)
        like_suffix = parent.findViewById(R.id.layout_inflate_feed_details_TXT_like_comments)
        like_count_suffix = parent.findViewById(R.id.layout_inflate_feed_details_TXT_like_count)
        imageListLay = parent.findViewById(R.id.imagelist_lay)
        images_rv = parent.findViewById(R.id.images_rv)
        imageCount = parent.findViewById(R.id.profile_screen_photo_count)
        seeAll = parent.findViewById(R.id.profile_screen_dynamic_profile_more_list_TET)
        parentLay = parent.findViewById(R.id.parent)
        likeCountLay = parent.findViewById(R.id.layout_inflate_feed_details_TXT_like_count_LAY)

    }

    private fun clickableLink(longTextt: String, mediaObject: FeedList) {
        try {
            var longText = longTextt
            if (longText.length >= TLConstant.POST_TEXT_LIMIT) {
                if (longText.substring(TLConstant.POST_TEXT_LIMIT).contains(" ")) {
                    val firstInd = longText.indexOf(" ", TLConstant.POST_TEXT_LIMIT)
                    longText = longText.substring(
                        0,
                        firstInd
                    ) + " " + mContext.getString(R.string.see_more)


                }
            }
            val str = SpannableString(longText)
            val matcher = urlPattern.matcher(longText)
            var matchStart: Int
            var matchEnd: Int

            while (matcher.find()) {
                matchStart = matcher.start(1)
                matchEnd = matcher.end()

                var url = longText.substring(matchStart, matchEnd)
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "https://$url"
                if (isYoutubeUrl(url) && mediaObject.media!!.size == 0 && mediaObject.isSharedPost != 1) {
                    //title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
                    val ytThumbUrl =
                        "http://img.youtube.com/vi/" + extractYTId(aTitle) + "/mqdefault.jpg"
                    youtubeThumbnail.visibility = View.VISIBLE
                    youThumbLay.visibility = View.VISIBLE
                    Picasso.get()
                        .load(ytThumbUrl)
                        .placeholder(R.drawable.new_feed_image_place_holder)
                        .error(R.drawable.new_feed_image_place_holder)
                        .into(youtubeThumbnail)
                } else {
                    youtubeThumbnail.visibility = View.GONE
                    youThumbLay.visibility = View.GONE
                }


                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        /* val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                         mContext.startActivity(intent)*/
                        if (isYoutubeUrl(url)) {
                            var id: String? = null
                            if (mediaObject.isSharedPost != 1)

                                id = extractYTId(title.text.toString().trim())!!
                            else
                                id = extractYTId(sharedTitle.text.toString().trim())!!
                            val intent = Intent(mContext, YoutubePalyerActivity::class.java)
                            intent.putExtra("id", id)
                            mContext.startActivity(intent)
                        } else if (url.startsWith("http://") || url.startsWith("https://")) {
                            val intent = Intent(mContext, TLWebViewView::class.java)
                            intent.putExtra("url", url)
                            mContext.startActivity(intent)
                        }
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                        ds.color = mContext.resources.getColor(R.color.colorPrimaryDark)
                    }
                }
                str.setSpan(clickableSpan, matchStart, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            }
            if (title.text.length >= TLConstant.POST_TEXT_LIMIT) {
                if (longText.substring(TLConstant.POST_TEXT_LIMIT).contains(" ")) {
                    str.setSpan(
                        ForegroundColorSpan(Color.GRAY),
                        longText.length - 10,
                        longText.length,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }
            }
            title.text = str
            title.movementMethod = LinkMovementMethod.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun clickableLinkSharedTitle(longTextt: String, mediaObject: FeedList) {
        try {
            var longText = longTextt
            if (longText.length >= TLConstant.POST_TEXT_LIMIT) {
                if (longText.substring(TLConstant.POST_TEXT_LIMIT).contains(" ")) {
                    val firstInd = longText.indexOf(" ", TLConstant.POST_TEXT_LIMIT)
                    longText = longText.substring(
                        0,
                        firstInd
                    ) + " " + mContext.getString(R.string.see_more)


                }
            }

            val str = SpannableString(longText)
            val matcher = urlPattern.matcher(longText)
            var matchStart: Int
            var matchEnd: Int

            while (matcher.find()) {
                matchStart = matcher.start(1)
                matchEnd = matcher.end()

                var url = longText.substring(matchStart, matchEnd)
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "https://$url"
                if (isYoutubeUrl(url) && mediaObject.media!!.size == 0) {
                    //sharedTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
                    val ytThumbUrl =
                        "http://img.youtube.com/vi/" + extractYTId(
                            sharedTitle.text.toString().trim()
                        ) + "/mqdefault.jpg"
                    youtubeThumbnail.visibility = View.VISIBLE
                    youThumbLay.visibility = View.VISIBLE
                    Picasso.get()
                        .load(ytThumbUrl)
                        .placeholder(R.drawable.new_feed_image_place_holder)
                        .error(R.drawable.new_feed_image_place_holder)
                        .into(youtubeThumbnail)
                } else {
                    youtubeThumbnail.visibility = View.GONE
                    youThumbLay.visibility = View.GONE
                }


                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        /* val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                         mContext.startActivity(intent)*/
                        if (isYoutubeUrl(url)) {
                            var id: String = extractYTId(sharedTitle.text.toString().trim())!!
                            val intent = Intent(mContext, YoutubePalyerActivity::class.java)
                            intent.putExtra("id", id)
                            mContext.startActivity(intent)
                        } else if (url.startsWith("http://") || url.startsWith("https://")) {
                            val intent = Intent(mContext, TLWebViewView::class.java)
                            intent.putExtra("url", url)
                            mContext.startActivity(intent)
                        }
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                        ds.color = mContext.resources.getColor(R.color.colorPrimaryDark)
                    }
                }
                //   val span = SpannableString(clickableSpan.toString())
                //    span.setSpan(ForegroundColorSpan(Color.blue()),matchStart, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                str.setSpan(clickableSpan, matchStart, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (sharedTitle.text.length >= TLConstant.POST_TEXT_LIMIT) {
                if (longText.substring(TLConstant.POST_TEXT_LIMIT).contains(" ")) {
                    str.setSpan(
                        ForegroundColorSpan(Color.GRAY),
                        longText.length - 10,
                        longText.length,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }
            }
            sharedTitle.text = str
            sharedTitle.movementMethod = LinkMovementMethod.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    /* private fun clickableLink(longText: String, mediaObject: FeedList) {
         try {
             val str = SpannableString(longText)
             val matcher = urlPattern.matcher(longText)
             var matchStart: Int
             var matchEnd: Int

             while (matcher.find()) {
                 matchStart = matcher.start(1)
                 matchEnd = matcher.end()

                 var url = longText.substring(matchStart, matchEnd)
                 if (!url.startsWith("http://") && !url.startsWith("https://")) {
                     url = "https://$url"

                   *//*  if (isYoutubeUrl(url) && mediaObject.media!!.size == 0 && mediaObject.isSharedPost != 1) {
                        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
                        title.setPaintFlags(title.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
                        title.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                        val ytThumbUrl =
                            "http://img.youtube.com/vi/" + extractYTId(aTitle) + "/mqdefault.jpg"
                        youtubeThumbnail.visibility = View.VISIBLE
                        youThumbLay.visibility = View.VISIBLE
                        Picasso.get()
                            .load(ytThumbUrl)
                            .placeholder(R.drawable.new_feed_image_place_holder)
                            .error(R.drawable.new_feed_image_place_holder)
                            .into(youtubeThumbnail)
                    } else {
                        youtubeThumbnail.visibility = View.GONE
                        youThumbLay.visibility = View.GONE
                    }*//*
                    val clickableSpan: ClickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            mContext?.startActivity(intent)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = false
                        }
                    }
                    str.setSpan(
                        clickableSpan,
                        matchStart,
                        matchEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            title.text = str
            title.movementMethod = LinkMovementMethod.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }*/

    fun setTitle(aGivenTxt: String, textView: TextView) {
        /* if (aGivenTxt.length >= TLConstant.POST_TEXT_LIMIT) {
             if (aGivenTxt.substring(TLConstant.POST_TEXT_LIMIT).contains(" ")) {
                 val firstInd = aGivenTxt.indexOf(" ", TLConstant.POST_TEXT_LIMIT)
                 textView.text = aGivenTxt
               *//*  textView.text = aGivenTxt.substring(
                    0,
                    firstInd
                ) + mContext.getString(R.string.see_more)*//*


              *//*  val sText = SpannableString(textView.text)
                sText.setSpan(
                    ForegroundColorSpan(Color.GRAY),
                    textView.text.length - 10,
                    textView.text.length,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )*//*
                textView.text = sText


            } else
                textView.text = aGivenTxt
        } else {*/
        if (aGivenTxt.isNotEmpty() && aGivenTxt.length > 0) {
            textView.visibility = View.VISIBLE
            textView.text = aGivenTxt
        } else {
            textView.visibility = View.GONE
        }
        //  }

    }

}