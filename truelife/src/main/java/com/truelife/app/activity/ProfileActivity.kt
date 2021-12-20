package com.truelife.app.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.truelife.*
import com.truelife.api.AppServices
import com.truelife.api.AppServices.API.AddFollow
import com.truelife.api.AppServices.API.addfriend
import com.truelife.app.VideoPlayerRecyclerPlayerAdapter
import com.truelife.app.VideoPlayerRecyclerView
import com.truelife.app.VideoPreviewActivity
import com.truelife.app.activity.feedpost.FeedDetailActivity
import com.truelife.app.adapter.ProfileFollowersListAdapter
import com.truelife.app.adapter.ProfileFollowingListAdapter
import com.truelife.app.adapter.ProfileFriendsListAdapter
import com.truelife.app.adapter.ProfilePhotoListAdapter
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.ReportDailogFragment
import com.truelife.app.fragment.club.more.TLClubMemberRights
import com.truelife.app.listeners.Feedistener
import com.truelife.app.listeners.ImageLoaderCallback
import com.truelife.app.model.LikeList
import com.truelife.app.model.Profile
import com.truelife.app.model.PublicFeedModel
import com.truelife.app.model.User
import com.truelife.app.touchimageview.TLSingleImagePreview
import com.truelife.base.BaseActivity
import com.truelife.base.TLFragmentManager
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.*
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst.KEY_SELECTED_MEDIA
import droidninja.filepicker.FilePickerConst.REQUEST_CODE_PHOTO
import kotlinx.android.synthetic.main.activity_profile.*
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

class ProfileActivity : BaseActivity(), ResponseListener, ProfileClickListener, Feedistener,
    FeedClickListener, FeedMenuClickListener, UserMoreClickListener {

    var mRequestType = ""
    var isMessaging = false
    var profileImage = ""
    var user: User? = null
    var userId = ""
    lateinit var mFriendAdapter: ProfileFriendsListAdapter
    lateinit var mFollowerAdapter: ProfileFollowersListAdapter
    lateinit var mFollowingAdapter: ProfileFollowingListAdapter
    lateinit var mImageAdapter: ProfilePhotoListAdapter
    lateinit var mProfileValue: Profile
    private var myPath = ArrayList<String>()
    lateinit var mAdapter: VideoPlayerRecyclerPlayerAdapter
    var mPage: Int = 1
    lateinit var mRecyclerView: VideoPlayerRecyclerView
    var mTotalPages: Int = 0
    lateinit var mList: ArrayList<PublicFeedModel.FeedList>

    private var myProfileFile: File? = null
    private var mProfileUpdateType: String? = null

    lateinit var isFollow: String
    lateinit var isFriend: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        Utility.loadPlaceHolder(myContext, LocalStorageSP.getLoginUser(myContext).mGender!!, profile_img)
        LocalBroadcastManager.getInstance(myContext)
            .registerReceiver(
                myBroadcastReceiver,
                IntentFilter(TLConstant.VIDEO_SEEK_POSITION_PROFILE)
            )

        mRecyclerView = findViewById(R.id.publicRv)


        mList = ArrayList()

        val g = PublicFeedModel.FeedList()
        g.isProfile = "yes"
        mList.add(g)

        init()
        clickListener()
        if (intent.hasExtra("userid")) {
            userId = intent.getStringExtra("userid")!!
        } else {
            userId = user!!.mUserId!!
        }

        if (userId.equals(user!!.mUserId)) {
            fragment_profile_add_LAY.visibility = View.GONE
            profile_edit_icon.visibility = View.VISIBLE
            profile_edit_icon_view.visibility = View.VISIBLE
        } else {
            fragment_profile_add_LAY.visibility = View.VISIBLE
            profile_edit_icon.visibility = View.GONE
            profile_edit_icon_view.visibility = View.GONE
        }
        getProfile()
        getFeeds()
    }

    private fun initGlide(): RequestManager? {
        val options: RequestOptions = RequestOptions()
            .placeholder(R.drawable.bg_border_view_white)
            .error(R.drawable.bg_border_view_white)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
        return Glide.with(this)
            .setDefaultRequestOptions(options)
    }

    private fun getFeeds() {

        val mCase = getFeedCaseInfo(
            "1",
            user!!.mUserId!!,
            user!!.mCountryId!!,
            user!!.mStateId!!,
            user!!.mCurrentCityId!!,
            mPage.toString(),
            userId,
            user!!.mPincode!!
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

    override fun clickListener() {

        profile_img.setOnClickListener {
            val mMediaList = ArrayList<String>()
            mMediaList.add(profileImage)
            var mMediaType = ArrayList<String>()
            mMediaType.add("image")
            Utility.navigateImageView(myContext, mMediaList, mMediaType, 0)
        }

        fragment_profile_status_LAY.setOnClickListener {
            if (userId.equals(user!!.mUserId)) {
                alertDialogDetailsUpdate("Status", status_tv.text.toString())
            }
        }

        fragment_profile_hometown_LAY.setOnClickListener {
            if (userId.equals(user!!.mUserId))
                alertDialogDetailsUpdate("Hometown", hometown_tv.text.toString())
        }

        fragment_profile_education_LAY.setOnClickListener {
            val aIntent = Intent(myContext, ProfileSettingActivity::class.java)
            myContext.startActivity(aIntent)
        }
        fragment_profile_profession_LAY.setOnClickListener {
            if (userId.equals(user!!.mUserId))
                alertDialogDetailsUpdate("Profession", profession_tv.text.toString())
        }
        add_friends_TET.setOnClickListener {
            mRequestType = addfriend

            when (isFriend) {
                "0" -> {
                    addFriend()
                }
                "1" -> {
                    AppDialogs.customDoubleAction(
                        myContext,
                        null,
                        "Do you want to unfriend ?",
                        "Yes", "No",
                        object : AppDialogs.OptionListener {
                            override fun yes() {
                                AddFriendsAPI(userId, "2", mRequestType)
                            }

                            override fun no() {
                                AppDialogs.hidecustomView()
                            }

                        }, false, isOptionable = false
                    )

                }
                "2" -> {
                    // cancel friend request

                    AppDialogs.customDoubleAction(
                        myContext,
                        null,
                        "Do you want to cancel your friend request?",
                        "Yes", "No",
                        object : AppDialogs.OptionListener {
                            override fun yes() {
                                AddFriendsAPI(userId, "3", mRequestType)
                            }

                            override fun no() {
                                AppDialogs.hidecustomView()
                            }

                        }, false, isOptionable = false
                    )
                }

                "3" -> {

                    AppDialogs.customDoubleAction(
                        myContext,
                        null,
                        "Are you confirm the friend request ?",
                        "Yes", "No",
                        object : AppDialogs.OptionListener {
                            override fun yes() {
                                AddFriendsAPI(userId, "1", mRequestType)
                            }

                            override fun no() {
                                AppDialogs.hidecustomView()
                            }

                        }, false, isOptionable = false
                    )
                }


            }


        }

        add_follow_TET.setOnClickListener {
            mRequestType = AddFollow
            when (isFollow) {
                "0" -> {
                    AddFriendsAPI(userId, "1", mRequestType)
                }
                "1" -> {
                    AppDialogs.customDoubleAction(
                        myContext,
                        null,
                        "Do you want to unfollow ?",
                        "Yes", "No",
                        object : AppDialogs.OptionListener {
                            override fun yes() {
                                AddFriendsAPI(userId, "2", mRequestType)
                            }

                            override fun no() {
                                AppDialogs.hidecustomView()
                            }

                        }, false, isOptionable = false
                    )
                }
            }
        }

        profile_edit_icon.setOnClickListener {
            selectProfilePicture()
        }
        back_icon.setOnClickListener {
            finish()
        }
        radio_group.setOnCheckedChangeListener { group, checkedId ->

            when (checkedId) {
                R.id.profile_screen_friends_rd_button -> {
                    profile_screen_friends_rd_button.setTextColor(
                        Utility.getColor(
                            myContext,
                            R.color.white
                        )
                    )
                    profile_screen_follower_rd_button.setTextColor(
                        Utility.getColor(
                            myContext,
                            R.color.black
                        )
                    )
                    profile_screen_following_rd_button.setTextColor(
                        Utility.getColor(
                            myContext,
                            R.color.black
                        )
                    )
                    setFriendsRecylceview()
                }
                R.id.profile_screen_follower_rd_button -> {
                    profile_screen_friends_rd_button.setTextColor(
                        Utility.getColor(
                            myContext,
                            R.color.black
                        )
                    )
                    profile_screen_follower_rd_button.setTextColor(
                        Utility.getColor(
                            myContext,
                            R.color.white
                        )
                    )
                    profile_screen_following_rd_button.setTextColor(
                        Utility.getColor(
                            myContext,
                            R.color.black
                        )
                    )
                    setFollowersRecylceview()
                }
                R.id.profile_screen_following_rd_button -> {
                    profile_screen_friends_rd_button.setTextColor(
                        Utility.getColor(
                            myContext,
                            R.color.black
                        )
                    )
                    profile_screen_follower_rd_button.setTextColor(
                        Utility.getColor(
                            myContext,
                            R.color.black
                        )
                    )
                    profile_screen_following_rd_button.setTextColor(
                        Utility.getColor(
                            myContext,
                            R.color.white
                        )
                    )
                    setFollowingRecylceview()
                }
            }
        }

        add_message_TET.setOnClickListener {
            when (isFriend) {
                "0" -> if (checkInternet()) {
                    AppDialogs.customDoubleAction(
                        myContext,
                        "Send a friend request to this person to enable messaging",
                        "Are you sure you want to send request?",
                        "Ok", "Cancel",
                        object : AppDialogs.OptionListener {
                            override fun yes() {
                                isMessaging = true
                                mRequestType = addfriend
                                addFriend()
                            }

                            override fun no() {
                                AppDialogs.hidecustomView()
                            }

                        }, false, isOptionable = false
                    )
                }

                "1" -> {
                    if (checkInternet()) {
                        val msg = String.format(
                            "You can search for %s here and send a message",
                            mProfileValue.userdetails!!.fullname
                        )
                        startActivity(
                            Intent(this, TLChatActivity::class.java)
                                .putExtra("content", msg)
                        )
                    }
                }

                "2" -> {

                    AppDialogs.customOkAction(
                        this,
                        null,
                        "Already sent a request wait for user acceptance",
                        null,
                        null,
                        false
                    )
                }
            }
        }


        add_more_TET.setOnClickListener {
            AppDialogs.show_user_more(
                this, userId, myContext
            )
        }
    }

    private fun addFriend() {
        AddFriendsAPI(userId, "0", mRequestType)
    }

    override fun init() {
        myContext = this
        user = LocalStorageSP.getLoginUser(myContext)
        LocalStorageSP.put(myContext!!, "profile_Screen", "1")
    }

    fun getProfile() {
        AppDialogs.showProgressDialog(myContext)
        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL,
            getProfilegCaseString(userId)!!
        )
        AppServices.execute(
            myContext, this,
            result,
            Request.Method.POST,
            AppServices.API.profile,
            Profile::class.java
        )

    }

    private fun getProfilegCaseString(
        aUserId: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("login_user_id", LocalStorageSP.getLoginUser(myContext).mUserId)
            val jsonParam = JSONObject()
            jsonParam.put("ViewProfile", jsonParam1)
            Log.e("ViewProfile", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.profile.hashCode()) {
                if (r.response!!.isSuccess) {

                    mProfileValue = r as Profile
                    val mUser = LocalStorageSP.getLoginUser(myContext)
                    if (mProfileValue.userdetails!!.privacy!!.viewYourProfile.equals("3") && !mUser.mUserId.equals(
                            mProfileValue.userdetails!!.userId
                        )
                    )
                        AppDialogs.customOkAction(
                            myContext,
                            myContext.getString(R.string.app_name),
                            "Not allowed by the user",
                            "ok",
                            object : AppDialogs.ConfirmListener {
                                override fun yes() {
                                    finish()
                                }

                            },
                            false
                        )
                    else if (mProfileValue.userdetails!!.privacy!!.viewYourProfile.equals("2")
                        && !mProfileValue.userdetails!!.is_friend.equals("1") && !mUser.mUserId.equals(
                            mProfileValue.userdetails!!.userId
                        )
                    )
                        AppDialogs.customOkAction(
                            myContext,
                            myContext.getString(R.string.app_name),
                            "Not allowed by the user",
                            "ok",
                            object : AppDialogs.ConfirmListener {
                                override fun yes() {
                                    finish()
                                }

                            },
                            false
                        )
                    else if (mProfileValue.userdetails!!.userBlocked!!.equals("1"))
                        AppDialogs.customOkAction(
                            myContext,
                            myContext.getString(R.string.app_name),
                            "Not allowed by the user",
                            "ok",
                            object : AppDialogs.ConfirmListener {
                                override fun yes() {
                                    finish()
                                }

                            },
                            false
                        )
                    else {
                        profileImage = mProfileValue.userdetails!!.profileImage!!
                        setProfileValues(mProfileValue.userdetails!!)
                    }
                }
            } else if (r.requestType!! == AppServices.API.publicFeed.hashCode()) {
                if (r.response!!.isSuccess) {
                    mTotalPages = r.response!!.totalPages
                    mList.addAll((r as PublicFeedModel).feedList!!)
                    mRecyclerView.setMediaObjects(mList)
                    if (mPage == 1) {
                        mAdapter.notifyDataSetChanged()
                        mRecyclerView.smoothScrollBy(0, 1)
                    } else {
                        mAdapter.notifyItemChanged(mList.size - 10)
                    }
                } else AppDialogs.showToastDialog(myContext, r.response!!.responseMessage!!)
            } else if (r.requestType!! == addfriend.hashCode()) {
                if (r.response!!.isSuccess) {
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                    val value = mProfileValue.userdetails!!
                    value.is_friend =
                        if (isFriend == "3") "1" else (if (isFriend == "0") "2" else "0")
                    setProfileValues(value)

                }
            } else if (r.requestType!! == AddFollow.hashCode()) {
                if (r.response!!.isSuccess) {
                    AppDialogs.customSuccessAction(
                        this,
                        null,
                        r.response!!.responseMessage!!,
                        null,
                        null,
                        false
                    )
                    val value = mProfileValue.userdetails!!
                    value.isFollow = if (isFollow == "0") "1" else "0"
                    setProfileValues(value)
                }
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

    fun setProfileValues(aValues: Profile) {
        profileImage = aValues.profileImage!!

        isFriend = aValues.is_friend!!
        isFollow = aValues.isFollow!!
        when (isFriend) {
            "0" -> add_friends_TET.text = "Add Friend"
            "1" -> add_friends_TET.text = "Friends"
            "2" -> add_friends_TET.text = "Cancel Request"
        }
        when (isFollow) {
            "0" ->
                add_follow_TET.text = "Follow"
            "1" ->
                add_follow_TET.text = "Following"
        }

        profile_user_name.text = aValues.fullname
        lives_now.text = aValues.livesNow
        status_tv.text = aValues.status

        if (aValues.collegeInfo!!.size > 0) {
            education_tv.text = aValues.collegeInfo.get(aValues.collegeInfo.size - 1).college
        } else if (aValues.schoolInfo!!.size > 0) {
            education_tv.text = aValues.schoolInfo.get(aValues.schoolInfo.size - 1).school
        } else {
            education_tv.text = aValues.education
        }
        profession_tv.text = aValues.profession
        hometown_tv.text = aValues.homeTown

        if (aValues.profileImage.isNullOrEmpty())
            Utility.loadPlaceHolder(myContext, aValues.gender!!, profile_img)
        else {
            Utility.loadImageWithCallback(aValues.profileImage, profile_img, object : ImageLoaderCallback {
                override fun load() {
                    progress_bar.visibility = View.GONE
                }

            })
            profile_img.setOnClickListener {
                val dialogFragment =
                    TLSingleImagePreview(profileImage, R.drawable.image_placeholder)
                dialogFragment.show(myContext.supportFragmentManager, TLClubMemberRights.TAG)
            }
        }
     //   progress_bar.visibility = View.GONE
        setFriendsRecylceview()
        setImageRecylceview(aValues)
        profile_screen_photo_count.text = aValues.totalPhotos
        if (aValues.photosMore.equals("1"))
            profile_screen_dynamic_profile_more_list_TET.visibility = View.VISIBLE
        else profile_screen_dynamic_profile_more_list_TET.visibility = View.GONE

        profile_screen_dynamic_profile_more_list_TET.setOnClickListener {
            if (checkInternet()) {
                val intent = Intent(this, TLProfileTotalPhotos::class.java)
                intent.putExtra("user_id", userId)
                startActivity(intent)
            }
        }
    }

    fun setFriendsRecylceview() {

        val layoutManager =
            LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false)

        friendsRV.layoutManager = layoutManager
        friendsRV.setHasFixedSize(true)
        mFriendAdapter =
            ProfileFriendsListAdapter(
                myContext,
                mProfileValue.userdetails!!.friendsDetails!!,
                this
            )
        friendsRV.adapter = mFriendAdapter
        if (mProfileValue.userdetails!!.friendsMore != "0")
            profile_screen_dynamic_more_list_TET.visibility = View.VISIBLE
        else
            profile_screen_dynamic_more_list_TET.visibility = View.GONE

        if (mProfileValue.userdetails!!.friendsDetails!!.isEmpty()) {
            profile_screen_empty_friends_follow_list.visibility = View.VISIBLE
            friendsRV.visibility = View.GONE
        } else {
            profile_screen_empty_friends_follow_list.visibility = View.GONE
            friendsRV.visibility = View.VISIBLE
        }


        profile_screen_dynamic_more_list_TET.setOnClickListener {
            navigate("FriendsList", "Friends")
        }

    }

    private fun navigate(screen: String, title: String) {
        val aIntent = Intent(myContext, FriendsActivity::class.java)
        aIntent.putExtra("userId", userId)
        aIntent.putExtra("from", screen)
        aIntent.putExtra("title", title)
        myContext.startActivity(aIntent)
    }

    fun setFollowersRecylceview() {
        val layoutManager =
            LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false)
        friendsRV.layoutManager = layoutManager
        friendsRV.setHasFixedSize(true)
        mFollowerAdapter =
            ProfileFollowersListAdapter(
                myContext,
                mProfileValue.userdetails!!.followersDetails!!,
                this
            )
        friendsRV.adapter = mFollowerAdapter

        if (mProfileValue.userdetails!!.followersMore != "0")
            profile_screen_dynamic_more_list_TET.visibility = View.VISIBLE
        else profile_screen_dynamic_more_list_TET.visibility = View.GONE

        profile_screen_dynamic_more_list_TET.setOnClickListener {
            navigate("FollowersList", "Followers")
        }
    }

    fun setFollowingRecylceview() {
        val layoutManager =
            LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false)
        friendsRV.layoutManager = layoutManager
        friendsRV.setHasFixedSize(true)
        mFollowingAdapter =
            ProfileFollowingListAdapter(
                myContext,
                mProfileValue.userdetails!!.followingDetails!!,
                this
            )
        friendsRV.adapter = mFollowingAdapter
        if (mProfileValue.userdetails!!.followingMore != "0")
            profile_screen_dynamic_more_list_TET.visibility = View.VISIBLE
        else profile_screen_dynamic_more_list_TET.visibility = View.GONE

        profile_screen_dynamic_more_list_TET.setOnClickListener {
            navigate("FollowingList", "Following")
        }
    }

    fun setImageRecylceview(aValues: Profile) {
        val linearlayoutManager = LinearLayoutManager(myContext)
        mRecyclerView.layoutManager = linearlayoutManager
        mRecyclerView.setMediaObjects(mList)
        mRecyclerView.setFragmentActivity(myContext, TLFragmentManager(myContext))
        mRecyclerView.setInterface(this)
        mAdapter = VideoPlayerRecyclerPlayerAdapter(
            mList,
            initGlide()!!,
            myContext,
            this,
            "",
            "0",
            mProfileValue.userdetails!!.photoList!!,
            aValues,
            userId
        )
        mRecyclerView.adapter = mAdapter
        val listener = object : EndlessRecyclerViewScrollListener(linearlayoutManager, 2) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                try {
                    if (mPage < mTotalPages) {
                        mAdapter.showBottomLoading()
                        mPage++
                        getFeeds()
                    } else {
                        mAdapter.stopBottomLoading()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

        mRecyclerView.addOnScrollListener(listener)
        val layoutManager = GridLayoutManager(myContext, 4)
        images_rv.layoutManager = layoutManager
        images_rv.setHasFixedSize(true)
        mImageAdapter =
            ProfilePhotoListAdapter(myContext, mProfileValue.userdetails!!.photoList!!, this)
        images_rv.adapter = mImageAdapter
    }

    override fun click(position: Int, type: String) {

    }

    fun selectProfilePicture() {

        FilePickerBuilder.instance.setMaxCount(1)
            .setActivityTheme(R.style.FilePickerTheme)
            .enableVideoPicker(false)
            .enableCameraSupport(true)
            .showGifs(false)
            .showFolderView(true)
            .enableImagePicker(true)
            .setCameraPlaceholder(R.drawable.ic_action_camera)
            .pickPhoto(myContext)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PHOTO) {
            try {
                if (!data!!.getStringArrayListExtra(KEY_SELECTED_MEDIA).isEmpty()) {
                    myPath = ArrayList(
                        data.getStringArrayListExtra(KEY_SELECTED_MEDIA)
                    )
                    //val aFile = File(myPath.get(0))
                    myProfileFile = File(myPath.get(0))
                    myProfileFile = FileCompression.compressImage(myContext, File(myPath.get(0)))
                    //myProfileFile = Compressor(myContext).setQuality(70).compressToFile(File(myPath.get(0)))


                    mProfileUpdateType = "profil_photo_update"
                    if (Utility.getFileSize(myProfileFile) > 0)
                        updateProfile("profile_pic")
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getReportAsString(): String {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", user!!.mUserId)
            jsonParam1.put("status", status_tv.text)
            jsonParam1.put("profession", profession_tv.text)
            jsonParam1.put("education", education_tv.text)
            jsonParam1.put("about_us", "")
            jsonParam1.put("home_town", hometown_tv.text)
            val jsonParam = JSONObject()
            jsonParam.put("UpdateProfile", jsonParam1)
            Log.e("UpdateProfile", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr!!
    }

    fun updateProfile(type: String) {

        myContext.runOnUiThread(java.lang.Runnable {
            AppDialogs.showProgressDialog(myContext)
        })


        var aJsonResponse = ""
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, getReportAsString())


        var httpclient: HttpClient? = DefaultHttpClient()
        val httppost = HttpPost(result)
        Thread {
            if (type.equals("profile_pic")) {
                val multipartEntity: MultipartEntityBuilder = MultipartEntityBuilder.create()
                multipartEntity.addBinaryBody("profile_image", myProfileFile)

                Log.e("Size", "" + myProfileFile!!.length() / 1024)
                httppost.entity = multipartEntity.build()
                val httpResponse = httpclient!!.execute(httppost)
                val httpEntity = httpResponse.entity

                aJsonResponse = EntityUtils.toString(httpEntity)

            } else if (type.equals("status")) {
                val httpResponse = httpclient!!.execute(httppost)
                val httpEntity = httpResponse.entity
                aJsonResponse = EntityUtils.toString(httpEntity)
            }
            //   Log.e("Report Response --> ", aJsonResponse)
            runOnUiThread {


                if (aJsonResponse != "") { //  TLHelper.hideDialog(myProgressDialog);
                    try {
                        var aJsonObject = JSONObject(aJsonResponse)
                        Log.e("Response", aJsonObject.toString())
                        AppDialogs.hideProgressDialog()
                        if (aJsonObject.has("profile_picture")) {
                            profileImage = aJsonObject.getString("profile_picture")
                        }
                        if (aJsonObject.has("response")) {
                            var response_msg: JSONObject = aJsonObject.getJSONObject("response")
                            var ss = response_msg.getString("response_msg")
                            var responsecode = response_msg.getString("response_code")
                            AppDialogs.showToastDialog(myContext, ss)
                            if (responsecode.equals("1")) {
                                if (type == "profile_pic") {
                                    progress_bar.visibility = View.VISIBLE
                                    Utility.loadImage(profileImage, profile_img)
                                    progress_bar.visibility = View.GONE
/*                                    profile_img(
                                        Utility.rotateImage(
                                            myProfileFile!!))*/

                                } else {
                                    status_tv.text = aJsonObject.getString("status")
                                    profession_tv.text = aJsonObject.getString("profession")
                                    education_tv.text = aJsonObject.getString("education")
                                    hometown_tv.text = aJsonObject.getString("home_town")
                                }
                            }


                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {

                }
            }
        }.start()


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
            jsonParam1.put("search_user_id", aSearchUserId)
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

    override fun volumeStateChange(isMute: Boolean) {
        mAdapter.changeVolumeState(isMute)
    }

    override fun onResume() {
        super.onResume()
        LocalStorageSP.put(myContext, "profile_Screen", "1")
        if (mRecyclerView.isVideoViewAdded)
            mRecyclerView.startPlayer()

    }

    override fun onPause() {
        super.onPause()
        mRecyclerView.pausePlayer()
        LocalStorageSP.put(myContext, "profile_Screen", "0")
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalStorageSP.put(myContext, "profile_Screen", "0")
        mRecyclerView.releasePlayer()
        LocalBroadcastManager.getInstance(myContext).unregisterReceiver(myBroadcastReceiver)
    }


    private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            try {
                if (intent.action.equals(TLConstant.VIDEO_SEEK_POSITION_PROFILE)) {
                    val bundle = intent.extras
                    if (bundle!!.containsKey(TLConstant.VIDEO_SEEK_POSITION_PROFILE)) {
                        var pos: Long = 0
                        pos = bundle.getLong(TLConstant.VIDEO_SEEK_POSITION_PROFILE, 0)
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

    override fun share_click(position: Int, imgV: ImageView) {

        showShareMenu(mList[position], imgV)
    }

    override fun share_click(position: Int) {

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

    override fun comment_click(position: Int) {
        openFeedDetail(mList[position])
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
        intent.putExtra("action", TLConstant.VIDEO_SEEK_POSITION_PROFILE)
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

    fun AddFriendsAPI(
        aProfileUserId: String,
        aStatus: String,
        aRequestName: String
    ) {

        if (checkInternet()) {

            AppDialogs.showProgressDialog(myContext)
            val result = Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                AddFriendandAddFolllowCaseString(
                    user!!.mUserId!!,
                    aProfileUserId,
                    aStatus,
                    aRequestName
                )!!
            )
            AppServices.execute(
                myContext, this,
                result,
                Request.Method.POST,
                aRequestName,
                Response::class.java
            )
        }
    }

    private fun AddFriendandAddFolllowCaseString(
        aUserId: String,
        aProfileUserId: String,
        aStatus: String,
        aRequestName: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            aCaseStr = if (aRequestName == addfriend) {
                jsonParam1.put("user_id", aUserId)
                jsonParam1.put("friend_id", aProfileUserId)
                jsonParam1.put("is_accepted", aStatus)
                val jsonParam = JSONObject()
                jsonParam.put("FriendRequest", jsonParam1)
                Log.e("FriendRequest", " $jsonParam")
                Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            } else if (aRequestName == "AcceptClubRequest") {
                jsonParam1.put("login_user_id", aUserId)
                jsonParam1.put("request_id", aProfileUserId)
                jsonParam1.put("club_id", "")
                jsonParam1.put("status", aStatus)
                val jsonParam = JSONObject()
                jsonParam.put("AcceptClubRequest", jsonParam1)
                Log.e("AcceptClubRequest", " $jsonParam")
                Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            } else {
                jsonParam1.put("user_id", aUserId)
                jsonParam1.put("follows_id", aProfileUserId)
                jsonParam1.put("follow_status", aStatus)
                val jsonParam = JSONObject()
                jsonParam.put("FollowUnfollow", jsonParam1)
                Log.e("FollowUnfollow", " $jsonParam")
                Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    fun customSuccessAction(
        context: Context,
        title: String?,
        msg: String,
        action: String?,
        isCancelable: Boolean, userId: String?, aStatus: String?
    ) {

        AppDialogs.hidecustomView()

        val builder = AlertDialog.Builder(context, R.style.BottomSheetDialog)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_success_action, null)

        val dialogTitle = view.findViewById(R.id.dialog_title) as TextView
        val dialogMessage = view.findViewById(R.id.dialog_message) as TextView
        val dialogAction = view.findViewById(R.id.dialog_action_button) as TextView
        val dialogClose = view.findViewById(R.id.dialog_close_button) as ImageView

        builder.setCancelable(false)

        if (isCancelable)
            dialogClose.visibility = View.VISIBLE
        else dialogClose.visibility = View.GONE

        dialogTitle.text = if ((title == null)) context.getString(R.string.app_name) else title
        dialogMessage.text = msg

        dialogAction.text = if ((action == null)) context.getString(R.string.ok) else action
        dialogAction.setOnClickListener {
            AppDialogs.custom_dialog!!.dismiss()
            AddFriendsAPI(userId!!, aStatus!!, mRequestType)
        }

        dialogClose.setOnClickListener {
            AppDialogs.custom_dialog!!.dismiss()
        }

        builder.setView(view)
        AppDialogs.custom_dialog = builder.create()
        AppDialogs.custom_dialog!!.show()
    }

    private fun alertDialogDetailsUpdate(
        aTitleName: String,
        aName: String
    ) {
        val aDlg = Dialog(myContext)
        val inflater = myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_inflate_profile_details_dialog, null)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        aDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        aDlg.setTitle(aName)
        aDlg.setContentView(view)
        aDlg.setCancelable(true)

        val aHeaderName =
            aDlg.findViewById<View>(R.id.header_name) as TextView
        val aProfileDetails =
            aDlg.findViewById<View>(R.id.chat_status_edt) as EditText
        aHeaderName.text = aTitleName.toUpperCase()
        val aCancelBtn =
            aDlg.findViewById<View>(R.id.layout_inflate_profile_details_dialog_cancel_Btn) as Button
        val aUpdateBtn =
            aDlg.findViewById<View>(R.id.layout_inflate_profile_details_dialog_update_Btn) as Button
        aUpdateBtn.setTextColor(myContext.resources.getColor(R.color.blue))
        aCancelBtn.setTextColor(myContext.resources.getColor(R.color.blue))
        aDlg.show()
        aProfileDetails.setText(aName)
        aCancelBtn.setOnClickListener { aDlg.dismiss() }
        aUpdateBtn.setOnClickListener {
            if (!aProfileDetails.text.toString().isEmpty()) {
                when (aTitleName) {
                    "Status" -> status_tv.text = aProfileDetails.text.toString()
                    "Profession" -> profession_tv.text = aProfileDetails.text.toString()
                    "Education" -> education_tv.text = aProfileDetails.text.toString()
                    "Hometown" -> hometown_tv.text = aProfileDetails.text.toString()
                }

                aDlg.dismiss()
                updateProfile("status")

            } else {
                AppDialogs.customOkAction(
                    myContext,
                    null,
                    "Please Enter the " + aTitleName,
                    null,
                    object : AppDialogs.ConfirmListener {
                        override fun yes() {

                        }

                    }, false
                )
            }
        }
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
                            mList.remove(feedList)
                            mAdapter.update(mList)
                            mRecyclerView.smoothScrollBy(0, 1)
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
                                        mList.remove(feedList)
                                        mAdapter.update(mList)
                                        mRecyclerView.smoothScrollBy(0, 1)
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
        args?.putString("post_id", feedList.id);
        args?.putString("FromScreen", "feed_list");
        dialogFragment.setArguments(args)
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

    override fun ReportThisUser(id: String) {
        val fm = myContext.supportFragmentManager
        val dialogFragment = ReportDailogFragment()
        val args: Bundle? = Bundle()
        args?.putString("user_id", userId);
        args?.putString("FromScreen", "profile");
        dialogFragment.setArguments(args)
        dialogFragment.show(fm, "ReportDailogFragment")
    }

    override fun BlockThisUser(id: String) {
        val mCase = getSettingBlockFriendMemberString(
            user!!.mUserId!!,
            userId,
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
                        } else {
                            Toast.makeText(
                                myContext,
                                r.response!!.responseMessage,
                                Toast.LENGTH_SHORT
                            ).show()
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