package com.truelife.app.fragment.club

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.gson.Gson
import com.truelife.BuildConfig
import com.truelife.FeedMenuClickListener
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.VideoPlayerRecyclerAdapter
import com.truelife.app.VideoPlayerRecyclerView
import com.truelife.app.VideoPreviewActivity
import com.truelife.app.activity.FeedEditActivity
import com.truelife.app.activity.ImagePreview
import com.truelife.app.activity.ProfileActivity
import com.truelife.app.activity.TLDashboardActivity
import com.truelife.app.activity.feedpost.FeedDetailActivity
import com.truelife.app.activity.feedpost.activity.TLScreenPostFeedActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.ReportDailogFragment
import com.truelife.app.fragment.club.media.TLClubMediaFragment
import com.truelife.app.fragment.club.more.ClubMoreAdapter
import com.truelife.app.fragment.club.more.TLClubMemberRights
import com.truelife.app.fragment.club.more.TLClubMoreFragment
import com.truelife.app.listeners.Feedistener
import com.truelife.app.model.*
import com.truelife.app.touchimageview.TLSingleImagePreview
import com.truelife.base.BaseActivity
import com.truelife.base.TLFragmentManager
import com.truelife.http.MyHttpEntity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.*
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import kotlinx.android.synthetic.main.fragment_club_details.*
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.net.URI

class TLClubDetails : BaseActivity(), ResponseListener,
    ClubMoreAdapter.ClickListener, Feedistener, FeedClickListener, FeedMenuClickListener {
    var user: User? = null
    var mySelectedPosition = 0
    var myClubType: String? = ""
    var mAdapter: ClubMoreAdapter? = null
    val about = 1
    val media = 2
    val members = 3
    val more = 4
    var aClubId: String? = ""
    var mWebSocketClient: WebSocketClient? = null

    lateinit var mEditImage: ImageView
    private var mPermission: Array<String> = arrayOf()
    private var myPath = java.util.ArrayList<String>()

    lateinit var mRvAdapter: VideoPlayerRecyclerAdapter
    var mPage: Int = 1
    lateinit var mRecyclerView: VideoPlayerRecyclerView
    var mTotalPages: Int = 0
    lateinit var mList: java.util.ArrayList<PublicFeedModel.FeedList>

    lateinit var mClubs: Club.Clubs
    var clubMembers: ArrayList<Club.ClubMember> = arrayListOf()
    private var mFragmentManager: TLFragmentManager? = null
    var isMember = true

    lateinit var mAppBar: AppBarLayout
    lateinit var mCollapseToolBar: CollapsingToolbarLayout

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.clubdetails.hashCode()) {
                if (r.response!!.isSuccess) {
                    mClubs = r as Club.Clubs
                    clubMembers.addAll(mClubs.clubs!!.clubMembers!!)
                    Log.e("Success", mClubs.clubs!!.clubName!!)
                    setValues(mClubs.clubs!!)
                }
            } else if (r.requestType!! == AppServices.API.publicFeed.hashCode()) {
                if (r.response!!.isSuccess) {
                    mTotalPages = r.response!!.totalPages
                    mList.addAll((r as PublicFeedModel).feedList!!)
                    mRecyclerView.setMediaObjects(mList)
                    mRvAdapter.notifyDataSetChanged()
                } else AppDialogs.showToastDialog(myContext, r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.AcceptClubRequest.hashCode()) {
                AppDialogs.showToastDialog(myContext, r.response!!.responseMessage!!)
                finishActivity()
                LocalStorageSP.put(this, "refresh", true)
            } else if (r.requestType!! == AppServices.API.ClubRequest.hashCode()) {
                AppDialogs.showToastDialog(myContext, r.response!!.responseMessage!!)
                fragment_club_by_you_member_or_admin.text =
                    getString(R.string.label_club_request_send)
                fragment_club_by_you_member_or_admin.isEnabled = false
                fragment_club_by_you_member_or_admin.isClickable = false
                LocalStorageSP.put(this, "refresh", true)
            }else if (r.requestType!! == AppServices.API.likedetails.hashCode()) {
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
        }
    }

    fun setValues(obj: Club.Clubs) {

        try {
            Utility.loadImage(obj.profileImage!!, fragment_club_by_you_profile_IMG)
            if (obj.clubVisibility == "1" && !obj.adminId.equals(user!!.mUserId, false) &&
                obj.is_member_status!!.isNullOrEmpty()
            ) {
                AppDialogs.customOkAction(
                    this,
                    getString(R.string.app_name),
                    "Not allowed by the Administrator",
                    null,
                    object : AppDialogs.ConfirmListener {
                        override fun yes() {
                            finish()
                        }
                    },
                    true
                )
            }

            fragment_club_by_you_profile_IMG.setOnClickListener {
                val dialogFragment = TLSingleImagePreview(obj.profileImage!!, R.drawable.club_placeholder)
                dialogFragment.show(myContext.supportFragmentManager, TLClubMemberRights.TAG)
            }

            mEditImage.setOnClickListener {
                if (PermissionChecker().checkAllPermission(myContext, mPermission)) {
                    FilePickerBuilder.instance.setMaxCount(1)
                        .enableImagePicker(true)
                        .enableCameraSupport(true)
                        .enableVideoPicker(false)
                        .pickPhoto(this)
                }
            }


            if (obj.totalMembers.equals("0") || obj.totalMembers.equals("1")) {
                fragment_club_by_you_total_member_count.text = obj.totalMembers + " Member"
            } else {
                fragment_club_by_you_total_member_count.text = obj.totalMembers + " Members"
            }

            when {
                obj.adminId.equals(user!!.mUserId, false) -> {
                    PermissionChecker().askAllPermissions(myContext, mPermission)
                    mEditImage.visibility = View.VISIBLE
                    fragment_club_by_you_member_or_admin.text =
                        getString(R.string.label_you_are_admin)
                }
                obj.is_member_status == "0" -> {
                    fragment_club_by_you_member_or_admin.text =
                        getString(R.string.label_club_request_send)
                }
                obj.is_member_status == "1" -> {
                    fragment_club_by_you_member_or_admin.text =
                        getString(R.string.label_you_are_member)
                }
                else -> {
                    if (mClubs.clubs!!.clubRights!!.rule == "4" || (mClubs.clubs!!.clubFor == "2" && mClubs.clubs!!.is_friend_with_admin == 0))
                        fragment_club_by_you_member_or_admin.visibility = View.GONE
                    else {
                        fragment_club_by_you_member_or_admin.text = getString(R.string.label_join)
                        fragment_club_by_you_member_or_admin.visibility = View.VISIBLE
                    }
                }
            }

            if (obj.is_member_status!!.isEmpty() || obj.is_member_status!! == "0" || mClubs.clubs!!.clubRights!!.rule == "4" || (mClubs.clubs!!.clubFor == "2" && mClubs.clubs!!.is_friend_with_admin == 0)) {
                moreMenu.visibility = View.GONE
                memberMenu.visibility = View.GONE
                mediaMenu.visibility = View.GONE
                isMember = false
            }

            fragment_club_by_you_member_or_admin.isEnabled =
                fragment_club_by_you_member_or_admin.text == getString(R.string.label_join)

            fragment_club_by_you_member_or_admin.setOnClickListener {
                if (checkInternet()) {
                    if (mClubs.clubs!!.clubRights!!.rule == "2") {
                        if (mClubs.clubs!!.totalMembers!!.toInt() <= mClubs.clubs!!.clubRights!!.maximum_member!!.toInt())
                            joinClub()
                        else sendClubRequest()
                    } else if (mClubs.clubs!!.clubRights!!.rule == "3") {
                        sendClubRequest()
                    } else joinClub()
                }
            }

            fragment_club_by_you_admin_name.text =
                getString(R.string.label_admin) + " " + obj.clubAdministrator
            fragment_club_by_you_description.text =
                getString(R.string.label_club_type) + " " + obj.clubType

            toolbar_title.text = obj.clubName

            resetBackground(about)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun joinClub() {
        AppDialogs.showProgressDialog(myContext)
        val mCase = getJoinClubParam()
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        AppServices.execute(
            myContext, this,
            result,
            Request.Method.POST,
            AppServices.API.AcceptClubRequest,
            Club.FriendList::class.java
        )
    }

    private fun getJoinClubParam(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            val user = LocalStorageSP.getLoginUser(myContext)
            jsonObject.put("club_id", mClubs.clubs!!.id)
            jsonObject.put("request_id", "0")
            jsonObject.put("status", "1")
            jsonObject.put("login_user_id", user.mUserId)

            val jsonParam = JSONObject()
            jsonParam.put("AcceptClubRequest", jsonObject)

            Log.i("AcceptClubRequest --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun sendClubRequest() {
        AppDialogs.showProgressDialog(myContext)
        val mCase = getRequestParam()
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
        AppServices.execute(
            myContext, this,
            result,
            Request.Method.POST,
            AppServices.API.ClubRequest,
            Club.FriendList::class.java
        )
    }

    private fun getRequestParam(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            val user = LocalStorageSP.getLoginUser(myContext)
            jsonObject.put("club_id", mClubs.clubs!!.id)
            jsonObject.put("status", "0")
            jsonObject.put("login_user_id", user.mUserId)

            val jsonParam = JSONObject()
            jsonParam.put("ClubRequest", jsonObject)

            Log.i("ClubRequest --> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_club_details)

        mRecyclerView = findViewById(R.id.publicRv)
        mAppBar = findViewById(R.id.fragment_amro_appbar_layout)
        mCollapseToolBar = findViewById(R.id.collapsing_toolbar)

        val layoutManager = LinearLayoutManager(myContext)
        mList = java.util.ArrayList()
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.setMediaObjects(mList)
        mRecyclerView.setFragmentActivity(myContext, TLFragmentManager(myContext))
        mRecyclerView.setInterface(this)
        mRvAdapter = VideoPlayerRecyclerAdapter(mList, initGlide()!!, myContext, this, "", "0")
        mRecyclerView.adapter = mRvAdapter
        val listener = object : EndlessRecyclerViewScrollListener(layoutManager, 5) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                try {
                    if (mPage < mTotalPages) {
                        mRvAdapter.showBottomLoading()

                        mPage++
                        getFeeds()
                    } else {
                        mRvAdapter.stopBottomLoading()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

        mRecyclerView.addOnScrollListener(listener)
        LocalBroadcastManager.getInstance(myContext)
            .registerReceiver(
                myBroadcastReceiver,
                IntentFilter(TLConstant.VIDEO_SEEK_POSITION_CLUB_DETAIL)
            )

        init()
    }


    private fun getFeeds() {

        val mCase = getClubInFeedCaseInfo(
            user!!.mUserId!!,

            mPage
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            myContext, this,
            result,
            Request.Method.POST,
            AppServices.API.publicFeed,
            PublicFeedModel::class.java
        )
    }


    private fun getClubInFeedCaseInfo(
        aUserId: String,
        aPage: Int
    ): String? {
        var aCaseStr = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("club_id", aClubId)
            jsonParam1.put("page", aPage.toString())
            jsonParam1.put("login_user_id", aUserId)
            val jsonParam = JSONObject()
            jsonParam.put("ClubNewsFeeds", jsonParam1)
            Log.e("ClubNewsFeeds", "" + jsonParam.toString())
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            Log.e("ClubNewsFeeds", " $aCaseStr")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun getFeedCaseInfo(
        Type: String,
        aUserId: String,
        aCountryId: String,
        aStateId: String,
        aCurrentCityId: String,
        aPage: String,
        aSearchUserId: String,
        aPincode: String
    ): String? {
        var aCaseStr = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("source", Type)
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("country_id", aCountryId)
            jsonParam1.put("state_id", aStateId)
            jsonParam1.put("current_city_id", aCurrentCityId)
            jsonParam1.put("page", aPage)
            jsonParam1.put("search_user_id", aClubId)
            jsonParam1.put("pincode", aPincode)
            val jsonParam = JSONObject()
            jsonParam.put("NewsFeeds", jsonParam1)
            Log.e("NewsFeeds", "" + jsonParam.toString())
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            Log.e("NewsFeeds", " $aCaseStr")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun initGlide(): RequestManager? {
        val options: RequestOptions = RequestOptions()
            .placeholder(R.drawable.bg_border_view_white)
            .error(R.drawable.bg_border_view_white)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
        return Glide.with(this)
            .setDefaultRequestOptions(options)
    }

    private fun resetBackground(aPos: Int) {

        try {
            when (aPos) {
                about -> {
                    club_frame_layout.visibility = View.GONE
                    mRecyclerView.visibility = View.VISIBLE
                    about_lay.visibility = View.VISIBLE
                    if (isMember)
                        getFeeds()
                    aboutMenu.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
                    mediaMenu.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    memberMenu.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    moreMenu.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                }
                media -> {
                    club_frame_layout.visibility = View.VISIBLE
                    about_lay.visibility = View.GONE
                    mRecyclerView.visibility = View.GONE
                    aboutMenu.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    mediaMenu.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
                    memberMenu.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    moreMenu.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    loadFragment(
                        TLClubMediaFragment(
                            mClubs.clubs!!.mClubMedia!!
                        ), null
                    )
                }
                members -> {
                    club_frame_layout.visibility = View.VISIBLE
                    mRecyclerView.visibility = View.GONE
                    about_lay.visibility = View.GONE
                    aboutMenu.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    mediaMenu.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    memberMenu.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
                    moreMenu.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    loadFragment(TLClubMembersFragment(clubMembers), null)
                }
                more -> {
                    club_frame_layout.visibility = View.VISIBLE
                    mRecyclerView.visibility = View.GONE
                    about_lay.visibility = View.GONE
                    aboutMenu.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    mediaMenu.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    memberMenu.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    moreMenu.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))

                    loadFragment(
                        TLClubMoreFragment(mClubs), null
                    )
                }
            }

            stopCollapse(aPos != about)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun clickListener() {

        back_icon.setOnClickListener {
            finish()
        }

        aboutMenu.setOnClickListener {
            resetBackground(about)
        }
        mediaMenu.setOnClickListener {
            resetBackground(media)
        }
        memberMenu.setOnClickListener {
            resetBackground(members)
        }
        moreMenu.setOnClickListener {

            resetBackground(more)
        }
        fragment_club_by_you_post.setOnClickListener {

            if (mClubs.clubs!!.is_member_status.isNullOrEmpty()) {
                AppDialogs.customOkAction(
                    this,
                    getString(R.string.app_name),
                    "You must become a member to post here",
                    null,
                    null,
                    true
                )
                return@setOnClickListener
            }

            LocalStorageSP.put(myContext, TLConstant.SourceType, "3")
            val aIntent = Intent(myContext, TLScreenPostFeedActivity::class.java)
            val mClub: ClubInfo = ClubInfo()
            mClub!!.id = mClubs.clubs!!.id
            mClub!!.clubName = mClubs.clubs!!.clubName
            mClub!!.clubAdmin = mClubs.clubs!!.clubAdmin
            mClub!!.profileImage = mClubs.clubs!!.profileImage
            mClub!!.totalMembers = mClubs.clubs!!.totalMembers
            mClub!!.clubPostingRight = mClubs.clubs!!.clubPostingRight
            mClub!!.isSelected = true
            aIntent.putExtra("share_thoughts", mClub)
            startActivity(aIntent)

        }

    }

    private fun loadFragment(fragment: Fragment, aBundle: Bundle?) {
        if (aBundle != null) {
            fragment.arguments = aBundle
        }
        val fm = myContext.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fm.beginTransaction()
        fragmentTransaction.replace(R.id.club_frame_layout, fragment)
        fragmentTransaction.commit()
    }

    override fun init() {

        mEditImage = findViewById(R.id.profile_edit_icon)

        mPermission = arrayOf(
            TLConstant.CAMERA,
            TLConstant.READ_EXTERNAL_STORAGE,
            TLConstant.WRITE_EXTERNAL_STORAGE
        )

        user = LocalStorageSP.getLoginUser(myContext)
        mFragmentManager = TLFragmentManager(myContext)

        getBundle()

        clickListener()
    }

    private fun getBundle() {
        try {

            var aBundle = intent
            if (aBundle != null) {
                if (aBundle.hasExtra("Club_id")) {
                    aClubId = aBundle.getStringExtra("Club_id")
                }
                getClubDetails(aClubId!!)
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun finishActivity() {
        finish()
    }

    fun getClubDetails(aClubId: String) {


        AppDialogs.showProgressDialog(myContext)
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                getClubDetails(user!!.mUserId!!, aClubId)!!
            )
        AppServices.execute(
            myContext, this,
            result,
            Request.Method.POST,
            AppServices.API.clubdetails,
            Club.Clubs::class.java
        )
    }

    private fun getClubDetails(
        aUserId: String,
        aClubId: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", aUserId)
            jsonParam1.put("club_id", aClubId)
            val jsonParam = JSONObject()
            jsonParam.put("ClubDetails", jsonParam1)
            Log.e("ClubDetails", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }
/*
    fun loadRecycleView() {
        more_recycle!!.setHasFixedSize(true)
        more_recycle!!.setLayoutManager(GridLayoutManager(myContext, 3))
        more_recycle!!.setNestedScrollingEnabled(true)
        mAdapter = ClubMoreAdapter(myContext, myClubType!!, this)
        more_recycle!!.adapter = mAdapter
    }*/

    override fun onClick(tt: String, position: Int) {

    }

    override fun onLongClick(view: View?, position: Int) {
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
            try {
                if (!data!!.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA).isEmpty()) {
                    myPath =
                        java.util.ArrayList(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA))
                    updateProfile()
                } else {
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateProfile() {
        if (checkInternet() && myPath.size > 0) {
            updatePhoto()
        }
    }

    fun updatePhoto() {
        runOnUiThread {
            AppDialogs.showProgressDialog(myContext)
        }
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, updateProfileParam()!!)
        val httpclient: HttpClient? = DefaultHttpClient()
        val httppost = HttpPost(result)

        val multipartEntity: MultipartEntityBuilder = MultipartEntityBuilder.create()
        if (myPath.size > 0) {
            multipartEntity.addBinaryBody("image",  FileCompression.compressImage(myContext, File(myPath[0])))
        }
        httppost.entity = MyHttpEntity(multipartEntity.build(), MyHttpEntity.ProgressListener {
        })
        Thread {
            //Do some Network Request
            val httpResponse = httpclient!!.execute(httppost)
            val httpEntity = httpResponse.entity

            val aJsonResponse = EntityUtils.toString(httpEntity)
            Log.i("Image -->", aJsonResponse)

            runOnUiThread {
                if (aJsonResponse.isNotEmpty()) {
                    try {
                        AppDialogs.hideProgressDialog()
                        val aJsonObject = JSONObject(aJsonResponse)
                        if (aJsonObject.has("response")) {
                            val response_msg: JSONObject = aJsonObject.getJSONObject("response")
                            AppDialogs.showToastDialog(
                                myContext,
                                response_msg.getString("response_msg")
                            )

                            if (response_msg.getString("response_code") == "1") {
                                fragment_club_by_you_profile_IMG.setImageBitmap(
                                    Utility.rotateImage(
                                        File(myPath[0])
                                    )
                                )
                            }

                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()
    }

    private fun updateProfileParam(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("club_id", mClubs.clubs!!.id)
            val jsonParam = JSONObject()
            jsonParam.put("UpdateClub", jsonParam1)
            Log.e("UpdateClub", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun volumeStateChange(isMute: Boolean) {
        mRvAdapter.changeVolumeState(isMute)
    }

    override fun onResume() {
        super.onResume()
        initSocket()
        if (mRecyclerView.isVideoViewAdded)
            mRecyclerView.startPlayer()
    }

    override fun onPause() {
        super.onPause()
        mRecyclerView.pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectSocket()
        mRecyclerView.releasePlayer()
        LocalBroadcastManager.getInstance(myContext).unregisterReceiver(myBroadcastReceiver)
    }

    private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            try {
                if (intent.action.equals(TLConstant.VIDEO_SEEK_POSITION_CLUB_DETAIL)) {
                    val bundle = intent.extras
                    if (bundle!!.containsKey(TLConstant.VIDEO_SEEK_POSITION_CLUB_DETAIL)) {
                        var pos: Long = 0
                        pos = bundle.getLong(TLConstant.VIDEO_SEEK_POSITION_CLUB_DETAIL, 0)
                        try {
                            mRecyclerView.setPlayPostion(pos)
                        } catch (e: Exception) {
                            print(e.toString())
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
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

    override fun like_details(position: Int) {
        AppDialogs.showProgressDialog(myContext)
        AppServices.postLikeDetailsString(mList[position].id!!, myContext, this, user!!.mUserId!!)
    }

    override fun like_click(position: Int, isLike: Boolean) {
        var like: String = ""
        if (isLike)
            like = "1"
        else
            like = "0"


        val mCase = postLikeString(
            user!!.mUserId!!,
            mList[position].id!!,
            like,
            "", "1"
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            myContext, this,
            result,
            Request.Method.POST,
            AppServices.API.publicFeed,
            PublicFeedModel::class.java
        )
    }
    fun showShareMenu(
        feedList: PublicFeedModel.FeedList, imgView: ImageView
    ) {
        val aPermission = arrayOf(
            TLConstant.WRITE_EXTERNAL_STORAGE
        )
        if (PermissionChecker().checkAllPermission(myContext, aPermission)) {
            AppDialogs.ShowShareDialog(feedList, this, imgView)
        }

    }

    override fun share_click(position: Int, imgV: ImageView) {

       showShareMenu(mList[position], imgV)
    }

    override fun share_click(position: Int) {

    }

    override fun comment_click(position: Int) {
        openFeedDetail(mList[position])
    }


    override fun menu_click(position: Int) {
        AppDialogs.show_feed_menu(this, mList[position], this)
    }

    override fun EditThisPost(feedList: PublicFeedModel.FeedList) {

        val intent = Intent(myContext, FeedEditActivity::class.java)
        intent.putExtra("feed", feedList)
        startActivity(intent)

    }

    override fun HideThisPost(feedList: PublicFeedModel.FeedList) {

        val mCase = getFeedHidePostCaseString(
            user!!.mUserId!!,
            feedList.id!!
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            this, object : ResponseListener {
                override fun onResponse(r: Response?) {
                    if (r != null) {
                        if (r.response!!.isSuccess) {
                            Toast.makeText(
                                myContext,
                                "Post successfully hidden",
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
        mList.remove(feedList)
        mRvAdapter.update(mList)
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
                                    }
                                }
                            }
                        },
                        result,
                        Request.Method.POST,
                        AppServices.API.delete_post,
                        Response::class.java
                    )


                    mList.remove(feedList)
                    mRvAdapter.update(mList)
                }

            },
            false,
            false
        )

    }

    override fun ReportThisPost(feedList: PublicFeedModel.FeedList) {

        /*val intent = Intent(mContext, ReportProblemActivity::class.java)
        intent.putExtra("post_id", feedList.id)
        intent.putExtra("FromScreen", "feed_list")
        startActivity(intent)*/

        val fm = myContext.supportFragmentManager
        val dialogFragment = ReportDailogFragment()
        val args: Bundle? = Bundle()
        args?.putString("post_id", feedList.id)
        args?.putString("FromScreen", "feed_list")
        dialogFragment.arguments = args
        dialogFragment.show(fm, "ReportDailogFragment")

    }

    override fun BlockThisPost(feedList: PublicFeedModel.FeedList) {

        val mCase = getSettingBlockFriendMemberString(
            user!!.mUserId!!,
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
            user!!.mUserId!!,
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

    override fun video_preview(
        position: Int,
        isRecycler: Boolean,
        isVideo: Boolean,
        focus_pos: Int
    ) {

        if (isVideo)
            openVideoPreview(mList[position].media!![0].original)
        else
            openImagePreview(mList[position], focus_pos)

    }


    fun openVideoPreview(original: String?) {
        var pos: Long = 0
        try {
            pos = mRecyclerView.getPlayPosition()
        } catch (e: Exception) {
            print(e.toString())
        }

        val intent =
            Intent(this, VideoPreviewActivity::class.java)
        intent.putExtra("url", original)
        intent.putExtra("play_postion", pos)
        intent.putExtra("action", TLConstant.VIDEO_SEEK_POSITION_CLUB_DETAIL)
        startActivity(intent)
    }

    fun openImagePreview(
        media: PublicFeedModel.FeedList,
        focusPos: Int
    ) {
        val intent =
            Intent(this, ImagePreview::class.java)
        intent.putExtra("media", media)
        intent.putExtra("focus", focusPos)
        startActivity(intent)
    }

    fun openFeedDetail(feedList: PublicFeedModel.FeedList) {
        val intent =
            Intent(this, FeedDetailActivity::class.java)
        intent.putExtra("media", feedList)
        startActivity(intent)
    }


    private fun initSocket() {
        val uri = URI(BuildConfig.SOCKET_URL)
        var isFound = false

        mWebSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake) {
                Log.i("Socket onOpen --> ", handshakedata.toString())
            }

            override fun onMessage(message: String) {

                if (mWebSocketClient == null || !mWebSocketClient!!.connection.isOpen)
                    return

                Log.i("Socket onMessage --> ", message)

                myContext.runOnUiThread {
                    if (message.isNotEmpty() && mList.isNotEmpty()) {
                        val obj = JSONObject(message)

                        // Update User Messages
                        if (obj.has("feed_list")) {

                            val mValue =
                                Gson().fromJson(message, PublicFeedModel.FeedList::class.java)

                            if (mValue.data.isNullOrEmpty()) {
                                return@runOnUiThread
                            }

                            for (i in mList.indices) {
                                if (mList[i].id.equals(mValue.data!![0].id)) {
                                    val oldlike = mList[i].isUserLike
                                    mList.removeAt(i)
                                    mValue.data!![0].isUserLike = oldlike
                                    mList.add(i, mValue.data!![0])
                                    isFound = true
                                    break
                                }
                            }

                            if (!isFound) {
                                mList.add(0, mValue.data!![0])
                            }

                            mRecyclerView.setMediaObjects(mList)
                            mRvAdapter!!.notifyDataSetChanged()
                        }

                    }
                }
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                Log.i("Socket onClose --> ", reason)
            }

            override fun onError(ex: Exception) {
                Log.i("Socket onError --> ", ex.printStackTrace().toString())
            }
        }

        mWebSocketClient!!.connect()

    }

    private fun disconnectSocket() {
        if (mWebSocketClient != null && mWebSocketClient!!.connection.isOpen) {
            mWebSocketClient!!.connection.close()
            mWebSocketClient!!.close()
        }
    }


    /**
     * TODO Elango
     * Stop Collapsing toolbar to collapse
     */

    fun stopCollapse(stop: Boolean) {
        try {
            val params = mCollapseToolBar.layoutParams as AppBarLayout.LayoutParams
            val appBarLayoutParams = mAppBar.layoutParams as CoordinatorLayout.LayoutParams

            if (stop) {
                params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
                mCollapseToolBar.layoutParams = params

                appBarLayoutParams.behavior = null
                mAppBar.layoutParams = appBarLayoutParams
            } else {
                params.scrollFlags =
                    AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                mCollapseToolBar.layoutParams = params

                appBarLayoutParams.behavior = AppBarLayout.Behavior()
                mAppBar.layoutParams = appBarLayoutParams;
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun Bundle.putCharSequenceArrayList(s: String, clubMembers: ArrayList<Club.ClubMember>?) {

}


