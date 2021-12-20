package com.truelife.app.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.util.Linkify
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.android.volley.Request
import com.squareup.picasso.Picasso
import com.truelife.BuildConfig
import com.truelife.ClickListener
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.FeedMultiImagesAdapter
import com.truelife.app.model.PublicFeedModel
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.CustomTypefaceSpan
import com.truelife.util.Helper
import com.truelife.util.Utility
import kotlinx.android.synthetic.main.activity_feed_share.*
import org.apache.commons.lang3.StringEscapeUtils
import org.json.JSONObject
import java.util.regex.Pattern


class FeedShareActivity : BaseActivity() {


    var feed_id = ""
    var level = ""
    var clubid = ""
    var source = ""
    lateinit var mFeed: PublicFeedModel.FeedList
    lateinit var mUser: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_share)
        mFeed = intent.getSerializableExtra("feed") as PublicFeedModel.FeedList
        feed_id = intent.getStringExtra("feed_id")!!
        level = intent.getStringExtra("level")!!
        clubid = intent.getStringExtra("clubid")!!
        source = intent.getStringExtra("source")!!
        mUser = LocalStorageSP.getLoginUser(myContext)

        var normal = "You're sharing a post in your "
        var bold = level


        when (level) {
            "Internationally" -> {
                normal = "You're sharing this post "
            }

            "Friends" -> {
                normal = "You're sharing this post with your "
            }
            "Friends of friends" -> {
                normal = "You're sharing this post with your "
            }
            "Friends of friends" -> {
                normal = "You're sharing this post with your "
            }
        }

        if (source.equals("3")) {
            normal = "You're sharing this post in a "
            bold = "Club"
        }

        val str: SpannableString = SpannableString(normal + bold)
        val typeface = Typeface.create(
            ResourcesCompat.getFont(this, R.font.opensans_extrabold),
            Typeface.NORMAL
        )



        str.setSpan(
            CustomTypefaceSpan("", typeface),
            normal.length,
            normal.length + bold.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        header_subtitile_preffix.text = str


        onBind(mFeed, this)
        close_club_button.setOnClickListener {
            finish()
        }

        Share_post_btn.setOnClickListener {
            val mUser = LocalStorageSP.getLoginUser(myContext)
            val mCase =
                getShareInfo()
            val result =
                Helper.GenerateEncrptedUrl(
                    BuildConfig.API_URL,
                    mCase!!
                )
            Log.e("URL", result)
            AppDialogs.showProgressDialog(myContext)
            AppServices.execute(
                myContext, object : ResponseListener {
                    override fun onResponse(r: Response?) {
                        AppDialogs.hideProgressDialog()
                        if (r != null) {
                            if (r.response!!.isSuccess) {

                                Toast.makeText(
                                        myContext,
                                        "Post successfully shared",
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


        new_content.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                fragment_feed_post_share_screen_share_count_TXT.text =
                    p0!!.length.toString() + "/" + "140"
            }

        })
    }

    override fun clickListener() {

    }

    override fun init() {

    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    @SuppressLint("SetTextI18n")
    fun onBind(
        mediaObject: PublicFeedModel.FeedList,
        mContext: Context
    ) {

        try {

            yourtubelogo.visibility = View.GONE
            club_post_shared_by.visibility = View.GONE
            feed_user_name.text = mediaObject.username
            if (!mediaObject.userImage.isNullOrEmpty() && !mediaObject.userImage.equals(""))
                Picasso.get().load(mediaObject.userImage).into(feed_user_image)
            else
                Utility.loadPlaceHolder(mContext, mediaObject.gender!!, feed_user_image)

            if (!mUser.mProfileImage.isNullOrEmpty() && !mUser.mProfileImage.equals(""))
                Picasso.get().load(mUser.mProfileImage).into(user_img_top)
            else
                Utility.loadPlaceHolder(mContext, mUser.mGender!!, user_img_top)
            user_name_top.text = mUser.mFullname
            feed_date_time.text = Helper.postTimeConvertFormat(mediaObject.postedTime)
            content_txt.text = StringEscapeUtils.unescapeJava(mediaObject.content!!)




            if (mediaObject.media!!.size > 1) {
                multi_medial_rv.visibility = View.VISIBLE
                imageViewSingle.visibility = View.GONE
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
                multi_medial_rv.layoutManager = gridLayoutManager
                multi_medial_rv.isNestedScrollingEnabled = false
                val mAdapter = FeedMultiImagesAdapter(mediaObject.media!!, object : ClickListener {
                    override fun click(position: Int) {
                        val intent =
                            Intent(this@FeedShareActivity, ImagePreview::class.java)
                        intent.putExtra("media", mediaObject)
                        intent.putExtra("focus", position)
                        startActivity(intent)
                    }
                })
                multi_medial_rv.adapter = mAdapter


            } else if (mediaObject.media!!.size == 1) {
                multi_medial_rv.visibility = View.GONE
                imageViewSingle.visibility = View.VISIBLE
                Picasso.get().load(mediaObject.media!![0].thumb).into(imageViewSingle)
            } else {
                multi_medial_rv.visibility = View.GONE
                imageViewSingle.visibility = View.GONE
                content_txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F);
                if (android.util.Patterns.WEB_URL.matcher(content_txt.text.toString().trim())
                        .matches()
                ) {
                    Linkify.addLinks(content_txt, Linkify.WEB_URLS)
                    content_txt.setLinksClickable(true)
                    content_txt.setLinkTextColor(
                        mContext.resources.getColor(
                            R.color.colorPrimary
                        )
                    )
                    if (isYoutubeUrl(content_txt.text.toString()) && mediaObject.media!!.size == 0) {
                        content_txt.setTextSize(
                            TypedValue.COMPLEX_UNIT_SP,
                            16F
                        );
                        val ytThumbUrl =
                            "http://img.youtube.com/vi/" + extractYTId(content_txt.text.toString()) + "/mqdefault.jpg"
                        imageViewSingle.visibility = View.VISIBLE
                        Picasso.get()
                            .load(ytThumbUrl)
                            .placeholder(R.drawable.new_feed_image_place_holder)
                            .error(R.drawable.new_feed_image_place_holder)
                            .into(imageViewSingle)
                        yourtubelogo.visibility = View.VISIBLE
                    }
                }
            }


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

    private fun getShareInfo(
    ): String? {

        val aClubArray: StringBuilder = StringBuilder()
        aClubArray.append(clubid)
        var aCaseStr = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", mUser.mUserId)
            jsonParam1.put("level", level)
            jsonParam1.put("type", "users")
            if (source.equals("1"))
                jsonParam1.put("club_id", "0")
            else
                jsonParam1.put("club_id", aClubArray)
            jsonParam1.put("privacy_post", "1")
            jsonParam1.put("source", source)
            jsonParam1.put("post_id", feed_id)
            jsonParam1.put("share_post_content", new_content.text.toString())
            val jsonParam = JSONObject()
            jsonParam.put("SharePost", jsonParam1)
            Log.e("SharePost", "" + jsonParam.toString())
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            Log.e("SharePost", " $aCaseStr")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

}
