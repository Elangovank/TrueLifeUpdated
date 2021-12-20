package com.truelife.app.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.android.volley.Request
import com.squareup.picasso.Picasso
import com.truelife.BuildConfig
import com.truelife.ClickListener
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.EditFeedMultiImagesAdapter
import com.truelife.app.FeedMultiImagesAdapter
import com.truelife.app.model.PublicFeedModel
import com.truelife.base.BaseActivity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import kotlinx.android.synthetic.main.activity_feed_edit.*
import org.apache.commons.lang3.StringEscapeUtils
import org.json.JSONObject

class FeedEditActivity : BaseActivity() {

    lateinit var mFeed: PublicFeedModel.FeedList
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_edit)
        mFeed = intent.getSerializableExtra("feed") as PublicFeedModel.FeedList
        onBind(mFeed, this)
        close_club_button.setOnClickListener {
            finish()
        }

        Share_post_btn.setOnClickListener {
            val mUser = LocalStorageSP.getLoginUser(myContext)
            val mCase =

                if (mFeed.isSharedPost == 1)
                    PostEditFeedCaseString(
                        mUser.mUserId!!,
                        mFeed.id!!, mFeed.content!!, new_content.text.toString()
                    )
                else
                    PostEditFeedCaseString(
                        mUser.mUserId!!,
                        mFeed.id!!, new_content.text.toString(), mFeed.sharePostContent!!
                    )
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
                        if (r != null) {
                            if (r.response!!.isSuccess) {

                                Toast.makeText(
                                    myContext,
                                    "Post successfully updated",
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

            if (!mediaObject.clubId.equals("0")) {
                club_post_shared_by.visibility = View.VISIBLE
                club_post_shared_by.text = "by " + mediaObject.username
                feed_user_name.text = mediaObject.clubName
                if (!mediaObject.clubImage.isNullOrEmpty() && !mediaObject.clubImage.equals(""))
                    Picasso.get().load(mediaObject.clubImage).into(feed_user_image)
            } else {
                club_post_shared_by.visibility = View.GONE
                feed_user_name.text = mediaObject.username
                if (!mediaObject.userImage.isNullOrEmpty() && !mediaObject.userImage.equals(""))
                    Picasso.get().load(mediaObject.userImage).into(feed_user_image)
            }
            if (!mediaObject.userImage.isNullOrEmpty() && !mediaObject.userImage.equals(""))
                Picasso.get().load(mediaObject.userImage).into(user_img_top)
            user_name_top.text = mediaObject.username

            feed_date_time.text = Helper.postTimeConvertFormat(mediaObject.postedTime)
            if (mediaObject.isSharedPost == 1) {
                content_txt.text = StringEscapeUtils.unescapeJava(mediaObject.sharePostContent!!)
                new_content.text =
                    StringEscapeUtils.unescapeJava(mediaObject.sharePostContent!!).toEditable()
            } else {
                content_txt.text = StringEscapeUtils.unescapeJava(mediaObject.content!!)
                new_content.text =
                    StringEscapeUtils.unescapeJava(mediaObject.content!!).toEditable()
            }



            if (mediaObject.media!!.size > 0) {
                multi_medial_rv.visibility = View.VISIBLE
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
                val mAdapter = EditFeedMultiImagesAdapter(mediaObject.media!!, object : ClickListener {
                    override fun click(position: Int) {
                        val intent =
                            Intent(this@FeedEditActivity, ImagePreview::class.java)
                        intent.putExtra("media", mediaObject)
                        intent.putExtra("focus", 0)
                        startActivity(intent)
                    }
                })
                multi_medial_rv.adapter = mAdapter


            } else {
                multi_medial_rv.visibility = View.GONE
                findViewById<TextView>(R.id.title).setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F);
            }


        } catch (e: Exception) {
            print(e.toString())
        }
    }


    private fun PostEditFeedCaseString(
        user_id: String,
        aPostId: String,
        aContent: String,
        aShareContent: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", user_id)
            jsonParam1.put("post_id", aPostId)
            jsonParam1.put("content", aContent)
            jsonParam1.put("share_post_content", aShareContent)
            val jsonParam = JSONObject()
            jsonParam.put("EditPost", jsonParam1)
            Log.e("EditPost", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

}
