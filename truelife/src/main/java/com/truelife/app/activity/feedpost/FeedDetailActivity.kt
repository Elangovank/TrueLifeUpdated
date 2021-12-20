package com.truelife.app.activity.feedpost

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.squareup.picasso.Picasso
import com.truelife.BuildConfig
import com.truelife.ClickListener
import com.truelife.FeedMenuClickListener
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.FeedDetailMediaAdapter
import com.truelife.app.VideoPreviewActivity
import com.truelife.app.activity.FeedEditActivity
import com.truelife.app.activity.ImagePreview
import com.truelife.app.activity.ProfileActivity
import com.truelife.app.activity.YoutubePalyerActivity
import com.truelife.app.activity.feedpost.adapter.CommentListAdapter
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.ReportDailogFragment
import com.truelife.app.model.ComentsListModel
import com.truelife.app.model.LikeList
import com.truelife.app.model.PublicFeedModel
import com.truelife.app.model.User
import com.truelife.app.viewer.TLWebViewView
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.*
import kotlinx.android.synthetic.main.activity_feed_detail.*
import org.apache.commons.lang3.StringEscapeUtils
import org.json.JSONObject
import java.util.regex.Pattern

class FeedDetailActivity : BaseActivity(), ResponseListener, FeedMenuClickListener {

    var user: User? = null
    lateinit var myFeedDate_TXT: TextView
    lateinit var userName: TextView
    lateinit var title: TextView
    lateinit var lable_shared: TextView
    lateinit var culb_shared_by: TextView
    lateinit var sharedTitle: TextView
    lateinit var sharedUserName: TextView
    lateinit var sharedFeedDate_TXT: TextView
    lateinit var shareLay: RelativeLayout
    lateinit var shareUserImg: ImageView
    lateinit var userImg: ImageView
    lateinit var dropDownMenu: ImageView
    lateinit var likeCount: TextView
    lateinit var comment_count: TextView
    lateinit var comment_suffix: TextView
    lateinit var likeLay: LinearLayout
    lateinit var likeButtonLay: LinearLayout
    lateinit var comment_count_lay: LinearLayout
    lateinit var shareBtnLay: LinearLayout
    lateinit var likeImg: ImageView
    lateinit var likeSep: View
    lateinit var comment_sep: View
    lateinit var counterSep: View
    lateinit var shareCount: TextView
    lateinit var shareSuffix: TextView
    lateinit var shareCountLay: LinearLayout
    lateinit var multiRv: RecyclerView
    lateinit var commentRv: RecyclerView
    var mMedia: PublicFeedModel.FeedList? = null
    var mList: ComentsListModel = ComentsListModel()
    lateinit var mCommentsAdapter: CommentListAdapter
    lateinit var mUser: User
    lateinit var like_suffix: TextView
    lateinit var like_count_suffix: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_detail)
        user = LocalStorageSP.getLoginUser(myContext)
        userImg = findViewById(R.id.feed_user_image)
        userName = findViewById(R.id.feed_user_name)
        myFeedDate_TXT = findViewById(R.id.feed_date_time)
        title = findViewById(R.id.title)
        dropDownMenu = findViewById(R.id.layout_inflate_feed_details_IMG_down_arrow)
        likeCount = findViewById(R.id.layout_inflate_feed_details_TXT_like_count_TXT)
        likeLay = findViewById(R.id.layout_inflate_feed_details_TXT_like_count_LAY)
        likeButtonLay = findViewById(R.id.layout_inflate_feed_details_LAY_like_IMG)
        shareBtnLay = findViewById(R.id.share_btn)
        likeImg = findViewById(R.id.layout_inflate_feed_details_LB_like)
        likeSep = findViewById(R.id.likeview)
        counterSep = findViewById(R.id.counter_seperator)
        lable_shared = findViewById(R.id.lable_shared_post)
        sharedTitle = findViewById(R.id.shared_title)
        sharedUserName = findViewById(R.id.feed_shared_user_name)
        sharedFeedDate_TXT = findViewById(R.id.feed_shared_date_time)
        shareLay = findViewById(R.id.feed_shared_profile_LAY)
        shareUserImg = findViewById(R.id.feed_shared_user_image)
        culb_shared_by = findViewById(R.id.club_post_shared_by)
        shareCount = findViewById(R.id.share_count)
        shareCountLay = findViewById(R.id.share_count_lay)
        shareSuffix = findViewById(R.id.share_suffix)
        multiRv = findViewById(R.id.media_rv)
        commentRv = findViewById(R.id.comments_rv)
        comment_count = findViewById(R.id.comment_count)
        comment_suffix = findViewById(R.id.comment_suffix)
        comment_sep = findViewById(R.id.comment_sep)
        comment_count_lay = findViewById(R.id.comments_count_lay)
        mUser = LocalStorageSP.getLoginUser(myContext);
        like_suffix = findViewById(R.id.layout_inflate_feed_details_TXT_like_comments)
        like_count_suffix = findViewById(R.id.layout_inflate_feed_details_TXT_like_count)

        back_arrow.setOnClickListener {
            finish()
        }

        likeLay.setOnClickListener {

            AppDialogs.showProgressDialog(myContext)
            AppServices.postLikeDetailsString(mMedia!!.id!!, myContext, this, mUser.mUserId!!)
        }
        post_comment.setOnClickListener {
            postComment()
        }

        shareBtnLay.setOnClickListener {
            AppDialogs.ShowShareDialog(mMedia!!, this)
        }

        dropDownMenu.setOnClickListener {
            AppDialogs.show_feed_menu(this, mMedia!!, this)
        }


        val gridLayoutManager = LinearLayoutManager(this)
        commentRv.layoutManager = gridLayoutManager
        commentRv.isNestedScrollingEnabled = true
        mCommentsAdapter = CommentListAdapter(this, mList, object : ClickListener {
            override fun click(position: Int) {
                if (mList.commentsList!![position].isUserLike.equals("0")) {
                    like_click(true, "2", mList.commentsList!![position].id!!)
                    mList.commentsList!![position].isUserLike = "1"
                    mList.commentsList!![position].likeCount =
                        (mList.commentsList!![position].likeCount!!.toInt() + 1).toString()
                } else {
                    like_click(false, "2", mList.commentsList!![position].id!!)
                    mList.commentsList!![position].isUserLike = "0"
                    mList.commentsList!![position].likeCount =
                        (mList.commentsList!![position].likeCount!!.toInt() - 1).toString()
                }
                mCommentsAdapter.update(mList)
            }
        })
        commentRv.adapter = mCommentsAdapter

        galleryData


        likeButtonLay.setOnClickListener {
            if (mMedia?.isUserLike.equals("0")) {
                like_click(true, "1", mMedia?.id!!)
                likeButtonLay.animate()
                    .scaleY(1.3f)
                    .scaleX(1.3f)
                    .setDuration(200)
                    .setInterpolator(LinearInterpolator())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator?) {
                            likeButtonLay.scaleX = 1f
                            likeButtonLay.scaleY = 1f
                            likeImg.setImageResource(R.drawable.ic_like_red)
                            mMedia?.isUserLike = "1"
                            mMedia?.totalLikes =
                                (mMedia?.totalLikes!!.toInt() + 1).toString()
                            likeCount.text = mMedia?.totalLikes
                            if (!mMedia?.totalLikes.equals("0")) {
                                counter_seperator.visibility = View.VISIBLE
                                likeLay.visibility = View.VISIBLE
                                likeSep.visibility = View.VISIBLE
                            } else {
                                counter_seperator.visibility = View.INVISIBLE
                                likeLay.visibility = View.GONE
                                likeSep.visibility = View.GONE
                            }

                        }
                    })

            } else {
                like_click(false, "1", mMedia?.id!!)
                likeImg.setImageResource(R.drawable.ic_like_outline)
                mMedia?.isUserLike = "0"
                mMedia?.totalLikes =
                    (mMedia?.totalLikes!!.toInt() - 1).toString()
                likeCount.text = mMedia?.totalLikes
                if (!mMedia?.totalLikes.equals("0")) {
                    counter_seperator.visibility = View.VISIBLE
                    likeLay.visibility = View.VISIBLE
                    likeSep.visibility = View.VISIBLE
                } else {
                    counter_seperator.visibility = View.INVISIBLE
                    likeLay.visibility = View.GONE
                    likeSep.visibility = View.GONE
                }
            }

        }
    }

    private fun postComment() {

        val mUser = LocalStorageSP.getLoginUser(this);
        val aVal: ComentsListModel.CommentsList = ComentsListModel.CommentsList()
        aVal.comment = screen_feed_detail_write_comment_Edt.text.toString()
        aVal.userName = mUser.mFullname
        aVal.userImage = mUser.mProfileImage
        aVal.isUserLike = "0"
        aVal.likeCount = "0"
        aVal.commentDate = DateUtil.getCurrentDTForamat("yyyy-MM-dd HH:mm:ss")

        mList.commentsList!!.add(aVal)
        mCommentsAdapter.update(mList)


        val mCase = getPostCommentsCaseString(
            mUser.mUserId!!,
            mMedia?.id!!, screen_feed_detail_write_comment_Edt.text.toString(), "0"
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            this, this,
            result,
            Request.Method.POST,
            AppServices.API.post_comment,
            Response::class.java
        )

        screen_feed_detail_write_comment_Edt.text = "".toEditable()

        Handler().postDelayed({
            screen_feed_detail_write_comment_Edt.requestFocus()

            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            imm.showSoftInput(
                screen_feed_detail_write_comment_Edt,
                InputMethodManager.SHOW_IMPLICIT
            );
            nestedScrollView.fullScroll(View.FOCUS_DOWN);
        }, 100)


    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    private fun getPostCommentsCaseString(
        aUserId: String,
        aPostId: String,
        aComments: String,
        aContentType: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("post_id", aPostId)
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("content", aComments)
            jsonParam1.put("comment_type", aContentType)
            val jsonParam = JSONObject()
            jsonParam.put("CommentPost", jsonParam1)
            Log.e("CommentPost", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun clickListener() {

    }

    override fun init() {


    }


    private val galleryData: Unit
        get() { // Getting position of image tap in gallery by user
            val i = intent
            if (i.hasExtra("media")) {
                mMedia = i.getSerializableExtra("media") as PublicFeedModel.FeedList

                onBind(mMedia!!)
            }
            if (i.hasExtra("post_id")) {
                getFeedDetails(i.getStringExtra("post_id")!!)
            }

        }

    @SuppressLint("SetTextI18n")
    fun onBind(
        mediaObject: PublicFeedModel.FeedList
    ) {
        try {
            title.setOnClickListener {
                if (isYoutubeUrl(title.text.toString())) {
                    val intent =
                        Intent(myContext, YoutubePalyerActivity::class.java)
                    intent.putExtra(
                        "id", extractYTId(title.text.toString())
                    )
                    myContext.startActivity(intent)
                }
            }

            sharedTitle.setOnClickListener {
                if (isYoutubeUrl(sharedTitle.text.toString())) {
                    val intent =
                        Intent(myContext, YoutubePalyerActivity::class.java)
                    intent.putExtra(
                        "id", extractYTId(sharedTitle.text.toString())
                    )
                    myContext.startActivity(intent)
                }
            }
            if (mediaObject.isSharedPost == 1) {
                sep_1.visibility = View.GONE
                shareLay.visibility = View.VISIBLE
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

                val aTitle: String = StringEscapeUtils.unescapeJava(mediaObject.sharePostContent!!)
                val aOriginal: String = StringEscapeUtils.unescapeJava(mediaObject.content!!)
                if (!aTitle.isEmpty()) {
                    title.visibility = View.VISIBLE
                } else {
                    title.visibility = View.GONE
                }
                if (!aOriginal.isEmpty()) {
                    sharedTitle.visibility = View.VISIBLE

                } else {
                    sharedTitle.visibility = View.GONE
                }
              //  Log.e("Content", aTitle)
                title.text = aTitle
                sharedTitle.text = aOriginal
                sharedFeedDate_TXT.text = Helper.postTimeConvertFormat(mediaObject.originalPostTime)
                userName.text = mediaObject.username
                if (!mediaObject.userImage.isNullOrEmpty() && !mediaObject.userImage.equals(""))
                    Picasso.get().load(mediaObject.userImage).into(userImg)
                if (!mediaObject.clubId.equals("0"))
                    lable_shared.text =
                        this.getString(R.string.lable_shared_a_post) + " to " + mediaObject.clubName


            } else {
                shareLay.visibility = View.GONE
                sep_1.visibility = View.VISIBLE
                lable_shared.visibility = View.GONE
                sharedTitle.visibility = View.GONE
                if (!(mediaObject.clubId == null) && !mediaObject.clubId.equals("0")) {
                    culb_shared_by.visibility = View.VISIBLE
                    culb_shared_by.text = "by " + mediaObject.username
                    userName.text = mediaObject.clubName
                    if (!mediaObject.clubImage.isNullOrEmpty() && !mediaObject.clubImage.equals("")) {
                        Utility.loadImageWithClubplaceholderImageView(mediaObject.clubImage!!,userImg)
                       // Picasso.get().load(mediaObject.clubImage).into(userImg)
                    }else{
                        userImg.setImageResource(R.drawable.club_placeholder)
                    }
                } else {
                    culb_shared_by.visibility = View.GONE
                    userName.text = mediaObject.username
                    if (!mediaObject.userImage.isNullOrEmpty() && !mediaObject.userImage.equals(""))
                        Picasso.get().load(mediaObject.userImage).into(userImg)
                }

                myFeedDate_TXT.text = Helper.postTimeConvertFormat(mediaObject.postedTime)
                val aTitle: String = StringEscapeUtils.unescapeJava(mediaObject.content!!)
                Log.e("Content", aTitle)
                title.text = aTitle

                if (!aTitle.isEmpty()) {
                    title.visibility = View.VISIBLE
                } else {
                    title.visibility = View.GONE
                }
            }
            clickableLink(title.text.toString(), mediaObject)
            /**
             * Setting up likes
             */
            likeCount.text = mediaObject.totalLikes
            if (!mediaObject.totalLikes.equals("0")) {
                likeLay.visibility = View.VISIBLE
                likeSep.visibility = View.VISIBLE
                if (mediaObject.totalLikes.equals("1"))
                    like_count_suffix.text = "Like"
                else
                    like_count_suffix.text = "Likes"
            } else {
                likeLay.visibility = View.GONE
                likeSep.visibility = View.GONE
            }
            if (mediaObject.isUserLike.equals("1")) {
                likeImg.setImageResource(R.drawable.ic_like_red)
                like_suffix.text = "Liked"
            } else {
                likeImg.setImageResource(R.drawable.ic_like_outline)
                like_suffix.text = "Like"
            }

            /**
             * Setting Up Comments
             */
            comment_count.setText(mediaObject.totalComments)
            if (mediaObject.totalComments.equals("0")) {
                comment_count_lay.visibility = View.GONE
            } else if (!mediaObject.totalComments.equals("0")) {
                comment_count_lay.visibility = View.VISIBLE
                if (mediaObject.totalComments.equals("1"))
                    comment_suffix.text = "Comment"
                else
                    comment_suffix.text = "Comments"
            } else {
                comment_count_lay.visibility = View.GONE

            }

            /**
             * Setting up share
             */
            shareCount.text = mediaObject.totalShares
            if (!mediaObject.totalShares.equals("0"))
                shareCountLay.visibility = View.VISIBLE
            else
                shareCountLay.visibility = View.GONE


            /**
             * Setting up separator
             */

            if (!mediaObject.totalLikes.equals("0") && !mediaObject.totalComments.equals("0"))
                likeSep.visibility = View.VISIBLE
            else
                likeSep.visibility = View.INVISIBLE


            if (!mediaObject.totalComments.equals("0") && !mediaObject.totalShares.equals("0"))
                comment_sep.visibility = View.VISIBLE
            else
                comment_sep.visibility = View.INVISIBLE

            if (!mediaObject.totalComments.equals("0")
                || !mediaObject.totalShares.equals("0")
                || !mediaObject.totalLikes.equals("0")
            )
                counterSep.visibility = View.VISIBLE
            else
                counterSep.visibility = View.INVISIBLE

            if (mediaObject.media!!.size > 0) {

                multiRv.visibility = View.VISIBLE
                val gridLayoutManager = LinearLayoutManager(this)
                multiRv.layoutManager = gridLayoutManager
                multiRv.isNestedScrollingEnabled = true
                val mAdapter = FeedDetailMediaAdapter(mediaObject.media!!, object : ClickListener {
                    override fun click(position: Int) {
                        if (mediaObject.media!![position].mediaType.equals("video")) {
                            val intent =
                                Intent(this@FeedDetailActivity, VideoPreviewActivity::class.java)
                            intent.putExtra("url", mMedia?.media!![position].original)
                            startActivity(intent)
                        } else {

                            val intent =
                                Intent(this@FeedDetailActivity, ImagePreview::class.java)
                            intent.putExtra("media", mMedia)
                            intent.putExtra("focus", position)
                            startActivity(intent)
                        }
                    }
                })
                multiRv.adapter = mAdapter


            } else {
                multiRv.visibility = View.GONE
                if (mediaObject.isSharedPost != 1)
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F);
                else
                    sharedTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F);

                if (android.util.Patterns.WEB_URL.matcher(title.text.toString().trim())
                        .matches() || android.util.Patterns.WEB_URL.matcher(
                        sharedTitle.text.toString().trim()
                    ).matches()
                ) {

                    if (android.util.Patterns.WEB_URL.matcher(sharedTitle.text.toString().trim())
                            .matches()
                    ) {
                        sharedTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
                        sharedTitle.setPaintFlags(title.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
                        sharedTitle.setTextColor(
                            ContextCompat.getColor(
                                myContext,
                                R.color.colorPrimary
                            )
                        );
                    }

                    if (android.util.Patterns.WEB_URL.matcher(title.text.toString().trim())
                            .matches()
                    ) {
                        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
                        title.setPaintFlags(title.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
                        title.setTextColor(ContextCompat.getColor(myContext, R.color.colorPrimary));
                    }

                    if ((isYoutubeUrl(title.text.toString()) || isYoutubeUrl(sharedTitle.text.toString())) && mediaObject.media!!.size == 0) {
                        title.setTextSize(
                            TypedValue.COMPLEX_UNIT_SP,
                            16F
                        );
                        var ytThumbUrl = ""
                        if (isYoutubeUrl(title.text.toString())) {
                            ytThumbUrl =
                                "http://img.youtube.com/vi/" + extractYTId(title.text.toString()) + "/mqdefault.jpg"
                        } else if (isYoutubeUrl(sharedTitle.text.toString())) {
                            ytThumbUrl =
                                "http://img.youtube.com/vi/" + extractYTId(sharedTitle.text.toString()) + "/mqdefault.jpg"
                        }
                        ytimageViewSingle.visibility = View.VISIBLE
                        Picasso.get()
                            .load(ytThumbUrl)
                            .placeholder(R.drawable.new_feed_image_place_holder)
                            .error(R.drawable.new_feed_image_place_holder)
                            .into(ytimageViewSingle)
                        youtubeLay.visibility = View.VISIBLE


                        ytimageViewSingle.setOnClickListener {

                            if (isYoutubeUrl(sharedTitle.text.toString())) {
                                val intent =
                                    Intent(myContext, YoutubePalyerActivity::class.java)
                                intent.putExtra(
                                    "id", extractYTId(sharedTitle.text.toString())
                                )
                                myContext.startActivity(intent)
                            } else if (isYoutubeUrl(title.text.toString())) {
                                val intent =
                                    Intent(myContext, YoutubePalyerActivity::class.java)
                                intent.putExtra(
                                    "id", extractYTId(title.text.toString())
                                )
                                myContext.startActivity(intent)
                            }
                        }
                    }
                }
            }
            if (sharedTitle.visibility == View.VISIBLE)
                clickableLinkSharedTitle(sharedTitle.text.toString(), mediaObject)
            nestedScrollView.fullScroll(View.FOCUS_UP)
            loadData()
        } catch (e: Exception) {
            print(e.toString())
        }
    }

    fun isYoutubeUrl(youTubeURl: String): Boolean {
        val pattern: Regex =
            "^(http(s)?:\\/\\/)?((w){3}.)?((m){1}.)?youtu(be|.be)?(\\.com)?\\/.+".toRegex()

        return youTubeURl.matches(pattern)
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

    fun loadData() {
        AppDialogs.showProgressDialog(this)
        val mUser = LocalStorageSP.getLoginUser(this);
        val mCase = getCommentListCaseString(
            mUser.mUserId!!,
            mMedia?.id!!
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            this, this,
            result,
            Request.Method.POST,
            AppServices.API.commentList,
            ComentsListModel::class.java
        )
    }


    private fun getCommentListCaseString(
        aUserId: String,
        aPostId: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("post_id", aPostId)
            jsonParam1.put("login_user_id", aUserId)
            val jsonParam = JSONObject()
            jsonParam.put("CommentsList", jsonParam1)
            Log.e("CommentsList", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.commentList.hashCode()) {
                if (r.response!!.isSuccess) {

                    mList = (r as ComentsListModel)
                    mCommentsAdapter.update(mList)

                } //else AppDialogs.showToastDialog(this, r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.feeddetails.hashCode()) {
                if (r.response!!.isSuccess) {
                    var obj = r as PublicFeedModel.FeedList
                    mMedia = obj.data?.get(0)
                    onBind(mMedia!!)
                }
            } else if (r.requestType!! == AppServices.API.likedetails.hashCode()) {
                if (r.response!!.isSuccess) {
                    val value = r as LikeList

                    AppDialogs.showLikeList(
                        myContext,
                        "",
                        value.mDataList,
                        object : LikeListAdapter.Callback {
                            override fun info(position: Int, text: String) {

                                val aIntent =
                                    Intent(myContext, ProfileActivity::class.java).putExtra(
                                        "userid",
                                        text
                                    )
                                myContext.startActivity(aIntent)
                            }

                        },
                        true,
                        true
                    )


                }
            }
        } else AppDialogs.customOkAction(
            this,
            null,
            TLConstant.SERVER_NOT_REACH,
            null,
            null,
            false
        )
    }

    private fun postLikeString(
        aUserId: String,
        aPostId: String,
        aLikeStatus: String,
        aSourceType: String,
        aLikeType: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("post_id", aPostId)
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("like", aLikeStatus)
            jsonParam1.put("like_type", aLikeType)
            jsonParam1.put("share", "0")
            //   if(aLikeType.equals( "1" )){
            jsonParam1.put("level", "")
            jsonParam1.put("source", aSourceType)
            //  }
            val jsonParam = JSONObject()
            jsonParam.put("LikeShare", jsonParam1)
            Log.e("PostLike", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    fun like_click(isLike: Boolean, likeType: String, id: String) {
        var like: String = ""
        if (isLike)
            like = "1"
        else
            like = "0"
        val mUser = LocalStorageSP.getLoginUser(this);


        val mCase = postLikeString(
            mUser.mUserId!!,
            id,
            like,
            "", likeType
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            this, this,
            result,
            Request.Method.POST,
            AppServices.API.like,
            PublicFeedModel::class.java
        )
    }

    override fun EditThisPost(feedList: PublicFeedModel.FeedList) {

        val intent = Intent(myContext, FeedEditActivity::class.java)
        intent.putExtra("feed", feedList)
        startActivity(intent)

    }

    override fun HideThisPost(feedList: PublicFeedModel.FeedList) {
        val mCase = getFeedHidePostCaseString(
            mUser.mUserId!!,
            feedList.id!!
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            myContext, object : ResponseListener {
                override fun onResponse(r: Response?) {
                    if (r != null) {
                        if (r.response!!.isSuccess) {
                            Toast.makeText(
                                myContext,
                                "Post successfully hidden",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            finish()
                        }
                    }
                }
            },
            result,
            Request.Method.POST,
            AppServices.API.hide_post,
            Response::class.java
        )
    }

    override fun DeleteThisPost(feedList: PublicFeedModel.FeedList) {

        AppDialogs.customDoubleAction(
            myContext,
            "",
            "Are you sure want to delete this post?",
            "Yes",
            "No",
            object : AppDialogs.OptionListener {
                override fun no() {

                }

                override fun yes() {
                    val mCase = getFeedDeletePostCaseString(
                        feedList.id!!
                    )
                    val result =
                        Helper.GenerateEncrptedUrl(
                            BuildConfig.API_URL,
                            mCase!!
                        )
                    Log.e("URL", result)
                    AppServices.execute(
                        myContext, object : ResponseListener {
                            override fun onResponse(r: Response?) {
                                if (r != null) {
                                    if (r.response!!.isSuccess) {
                                        Toast.makeText(
                                            myContext,
                                            "Post successfully deleted",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }
                                }
                            }
                        },
                        result,
                        Request.Method.POST,
                        AppServices.API.delete_post,
                        Response::class.java
                    )
                }

            },
            false,
            false
        )

    }

    override fun ReportThisPost(feedList: PublicFeedModel.FeedList) {

        /*val intent = Intent(myContext, ReportProblemActivity::class.java)
        intent.putExtra("post_id", feedList.id)
        intent.putExtra("FromScreen", "feed_list")
        startActivity(intent)*/

        val fm = myContext.supportFragmentManager
        val dialogFragment = ReportDailogFragment()
        val args: Bundle? = Bundle()
        args?.putString("post_id", feedList.id);
        args?.putString("FromScreen", "feed_list");
        dialogFragment.setArguments(args)
        dialogFragment.show(fm, "ReportDailogFragment")

    }

    override fun BlockThisPost(feedList: PublicFeedModel.FeedList) {

        val mCase = getSettingBlockFriendMemberString(
            mUser.mUserId!!,
            feedList.userId!!,
            "1"
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            myContext, object : ResponseListener {
                override fun onResponse(r: Response?) {
                    if (r != null) {
                        if (r.response!!.isSuccess) {
                            Toast.makeText(
                                myContext,
                                r.response!!.responseMessage,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            },
            result,
            Request.Method.POST,
            AppServices.API.hide_post,
            Response::class.java
        )

    }

    override fun FollowThisPost(feedList: PublicFeedModel.FeedList) {
        val status = if (feedList.isFollow.equals("0"))
            "1"
        else "0"

        val mCase = follow_unfollow_case(
            mUser.mUserId!!,
            feedList.userId!!,
            status
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            myContext, object : ResponseListener {
                override fun onResponse(r: Response?) {
                    if (r != null) {
                        if (r.response!!.isSuccess) {
                            Toast.makeText(
                                myContext,
                                r.response!!.responseMessage,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            },
            result,
            Request.Method.POST,
            AppServices.API.hide_post,
            Response::class.java
        )
    }


    private fun follow_unfollow_case(
        aUserId: String,
        aProfileUserId: String,
        aStatus: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("follows_id", aProfileUserId)
            jsonParam1.put("follow_status", aStatus)

            val jsonParam = JSONObject()
            jsonParam.put("FollowUnfollow", jsonParam1)
            Log.e("FollowUnfollow", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun getSettingBlockFriendMemberString(
        aLoginUserId: String,
        aFriendId: String,
        aBlockStatus: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", aLoginUserId)
            jsonParam1.put("friend_id", aFriendId)
            jsonParam1.put("block_status", aBlockStatus)
            val jsonParam = JSONObject()
            jsonParam.put("BlockFriend", jsonParam1)
            Log.e("BlockFriend", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun getFeedHidePostCaseString(
        aUserId: String,
        aPostId: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", aUserId)
            jsonParam1.put("post_id", aPostId)
            val jsonParam = JSONObject()
            jsonParam.put("HidePost", jsonParam1)
            Log.e("HidePost", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun getFeedDeletePostCaseString(aPostId: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("post_id", aPostId)
            val jsonParam = JSONObject()
            jsonParam.put("DeletePost", jsonParam1)
            Log.e("DeletePost", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    fun getFeedDetails(postId: String) {

        AppDialogs.showProgressDialog(myContext)

        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getFeedDetailes(postId, user!!.mUserId!!)!!
            )
        AppServices.execute(
            myContext!!, this,
            result,
            Request.Method.POST,
            AppServices.API.feeddetails,
            PublicFeedModel.FeedList::class.java
        )
    }

    private fun getFeedDetailes(
        aPostId: String,
        aUserId: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("post_id", aPostId)
            jsonParam1.put("login_user_id", aUserId)
            val jsonParam = JSONObject()
            jsonParam.put("SearchFeedByID", jsonParam1)
            Log.e("SearchFeedByID", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun clickableLink(longTextt: String, mediaObject: PublicFeedModel.FeedList) {
        try {
            var longText = longTextt
            /*if (longText.length >= TLConstant.POST_TEXT_LIMIT) {
                if (longText.substring(TLConstant.POST_TEXT_LIMIT).contains(" ")) {
                    val firstInd = longText.indexOf(" ", TLConstant.POST_TEXT_LIMIT)
                    longText = longText.substring(
                        0,
                        firstInd
                    ) + " " + myContext.getString(R.string.see_more)


                }
            }*/
            val str = SpannableString(longText)
            val matcher = TLConstant.URL_PATTERN.matcher(longText)
            var matchStart: Int
            var matchEnd: Int

            while (matcher.find()) {
                matchStart = matcher.start(1)
                matchEnd = matcher.end()

                var url = longText.substring(matchStart, matchEnd)
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "https://$url"
                if (Helper.isYoutubeUrl(url) && mediaObject.media!!.size == 0 && mediaObject.isSharedPost != 1) {
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
                    val ytThumbUrl =
                        "http://img.youtube.com/vi/" + extractYTId(title.text.toString()) + "/mqdefault.jpg"
                    ytimageViewSingle.visibility = View.VISIBLE
                    youtubeLay.visibility = View.VISIBLE
                    Picasso.get()
                        .load(ytThumbUrl)
                        .placeholder(R.drawable.new_feed_image_place_holder)
                        .error(R.drawable.new_feed_image_place_holder)
                        .into(ytimageViewSingle)
                } else {
                    ytimageViewSingle.visibility = View.GONE
                    youtubeLay.visibility = View.GONE
                }


                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        /* val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                         myContext.startActivity(intent)*/
                        if (Helper.isYoutubeUrl(url)) {
                            var id: String? = null
                            if (mediaObject.isSharedPost != 1)

                                id = extractYTId(title.text.toString().trim())!!
                            else
                                id = extractYTId(sharedTitle.text.toString().trim())!!
                            val intent = Intent(myContext, YoutubePalyerActivity::class.java)
                            intent.putExtra("id", id)
                            myContext.startActivity(intent)
                        } else if (url.startsWith("http://") || url.startsWith("https://")) {
                            val intent = Intent(myContext, TLWebViewView::class.java)
                            intent.putExtra("url", url)
                            myContext.startActivity(intent)
                        }
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                        ds.color = myContext.resources.getColor(R.color.colorPrimaryDark)
                    }
                }
                str.setSpan(clickableSpan, matchStart, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            }
           /* if (title.text.length >= TLConstant.POST_TEXT_LIMIT)
                str.setSpan(
                    ForegroundColorSpan(Color.GRAY),
                    longText.length - 10,
                    longText.length,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )*/
            title.text = str
            title.movementMethod = LinkMovementMethod.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clickableLinkSharedTitle(longTextt: String, mediaObject: PublicFeedModel.FeedList) {
        try {
            var longText = longTextt
           /* if (longText.length >= TLConstant.POST_TEXT_LIMIT) {
                if (longText.substring(TLConstant.POST_TEXT_LIMIT).contains(" ")) {
                    val firstInd = longText.indexOf(" ", TLConstant.POST_TEXT_LIMIT)
                    longText = longText.substring(
                        0,
                        firstInd
                    ) + " " + myContext.getString(R.string.see_more)


                }
            }*/

            val str = SpannableString(longText)
            val matcher = TLConstant.URL_PATTERN.matcher(longText)
            var matchStart: Int
            var matchEnd: Int

            while (matcher.find()) {
                matchStart = matcher.start(1)
                matchEnd = matcher.end()

                var url = longText.substring(matchStart, matchEnd)
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "https://$url"
                if (isYoutubeUrl(url) && mediaObject.media!!.size == 0) {
                    sharedTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
                    val ytThumbUrl =
                        "http://img.youtube.com/vi/" + extractYTId(
                            sharedTitle.text.toString().trim()
                        ) + "/mqdefault.jpg"
                    youtubeLay.visibility = View.VISIBLE
                    ytimageViewSingle.visibility = View.VISIBLE
                    Picasso.get()
                        .load(ytThumbUrl)
                        .placeholder(R.drawable.new_feed_image_place_holder)
                        .error(R.drawable.new_feed_image_place_holder)
                        .into(ytimageViewSingle)
                } else {
                    ytimageViewSingle.visibility = View.GONE
                    youtubeLay.visibility = View.GONE
                }


                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        /* val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                         myContext.startActivity(intent)*/
                        if (isYoutubeUrl(url)) {
                            var id: String = extractYTId(sharedTitle.text.toString().trim())!!
                            val intent = Intent(myContext, YoutubePalyerActivity::class.java)
                            intent.putExtra("id", id)
                            myContext.startActivity(intent)
                        } else if (url.startsWith("http://") || url.startsWith("https://")) {
                            val intent = Intent(myContext, TLWebViewView::class.java)
                            intent.putExtra("url", url)
                            myContext.startActivity(intent)
                        }
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                        ds.color = myContext.resources.getColor(R.color.colorPrimaryDark)
                    }
                }
                //   val span = SpannableString(clickableSpan.toString())
                //    span.setSpan(ForegroundColorSpan(Color.blue()),matchStart, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                str.setSpan(clickableSpan, matchStart, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
         /*   if (sharedTitle.text.length >= TLConstant.POST_TEXT_LIMIT) {
                if (longText.substring(TLConstant.POST_TEXT_LIMIT).contains(" ")) {
                    str.setSpan(
                        ForegroundColorSpan(Color.GRAY),
                        longText.length - 10,
                        longText.length,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }
            }*/
            sharedTitle.text = str
            sharedTitle.movementMethod = LinkMovementMethod.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
