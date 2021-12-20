package com.truelife.app.fragment.chat.profile

import android.app.Activity
import android.content.Intent
import android.content.res.Resources.NotFoundException
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.gturedi.views.StatefulLayout
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.club.more.TLClubMemberRights
import com.truelife.app.model.ChatProfile
import com.truelife.app.model.User
import com.truelife.app.touchimageview.TLSingleImagePreview
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import com.truelife.http.MyHttpEntity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP.getLoginUser
import com.truelife.util.*
import com.truelife.util.Utility.loadImage
import com.truelife.util.Utility.loadPlaceHolder
import de.hdodenhof.circleimageview.CircleImageView
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst.KEY_SELECTED_MEDIA
import droidninja.filepicker.FilePickerConst.REQUEST_CODE_PHOTO
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Created by Elango on 08-02-2020.
 */
class TLChatProfile : BaseFragment(),
    View.OnClickListener, ResponseListener {
    private var myUserProfileIMG: ImageView? = null
    private var myUserNameTXT: TextView? = null
    private var myUserCityNameTXT: TextView? = null
    private var myContext: FragmentActivity? = null
    private var myFragmentManager: TLFragmentManager? = null
    private var myChatUserUserID: String? = ""
    private var isLoginUser = false
    private var myImageMoreTXT: TextView? = null
    private var myDocuMoreTXT: TextView? = null
    private var myDocuAdapter: TLMsgProfileIDocuAdapter? = null
    private var myImageRCY: RecyclerView? = null
    private var myDocuRCY: RecyclerView? = null
    private var myImageAdapter: TLMsgTotalProfileImageAdapter? = null
    private var myRV_LayoutManager: GridLayoutManager? = null
    private var myBackIcon: ImageView? = null
    var myImageCreatedList =
        ArrayList<String>()
    private var myMainProfileStateLAY: StatefulLayout? = null
    private var myLoginUserView: View? = null
    private var myProfileIMGIcon: ImageView? = null
    private var myPath = ArrayList<String>()
    private var mUserInfo: User? = null
    private var myChatUserid: String? = ""
    private var myUserEmailIdTET: TextView? = null
    private var myUserPhoneNoTET: TextView? = null
    private var myProfileLAY: LinearLayout? = null

    // Collapse View
    private var mMainView: LinearLayout? = null
    private var mCollapseView: LinearLayout? = null
    private var mCollapseBack: ImageView? = null
    private var mCollapseIcon: CircleImageView? = null
    private var mCollapseName: TextView? = null

    private var mNoPhotosVideos: TextView? = null
    private var mNoDocs: TextView? = null

    private var mPermission: Array<String> = arrayOf()
    var myAppBar: AppBarLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        val rootView =
            inflater.inflate(R.layout.fragment_messager_profile, container, false)
        init(rootView)
        bundleValues
        myChatUserid = if (isLoginUser) {
            mUserInfo!!.mUserId
        } else {
            myChatUserUserID
        }
        addListeners()
        return rootView
    }

    override fun init(aView: View) {
        myContext = activity
        myFragmentManager = TLFragmentManager(myContext!!)
        mUserInfo = getLoginUser(myContext)
        myUserProfileIMG =
            aView.findViewById(R.id.fragment_profile_IMG_profile)
        myUserNameTXT = aView.findViewById(R.id.profile_user_name)
        myUserCityNameTXT = aView.findViewById(R.id.profile_city_name)
        mNoPhotosVideos = aView.findViewById(R.id.no_photos)
        mNoDocs = aView.findViewById(R.id.no_docs)
        myMainProfileStateLAY = aView.findViewById(R.id.fragment_profile_main_LAY)
        myImageMoreTXT = aView.findViewById(R.id.layout_inflate_feed_details_TXT_feed_image_more)
        myDocuMoreTXT = aView.findViewById(R.id.fragment_messager_profile_document_more_TXT)
        myLoginUserView = aView.findViewById(R.id.fragment_messager_profile_login_user_status_view)
        myImageRCY = aView.findViewById(R.id.fragment_feed_new_search_list_friends_recycler)
        myDocuRCY = aView.findViewById(R.id.fragment_messager_profile_document_RCY)
        myBackIcon = aView.findViewById(R.id.back_icon)
        myProfileIMGIcon = aView.findViewById(R.id.profile_edit_icon)
        myUserEmailIdTET = aView.findViewById(R.id.user_email_id)
        myUserPhoneNoTET = aView.findViewById(R.id.user_phone_number)
        myProfileLAY = aView.findViewById(R.id.profile_image_LAY)

        mMainView = aView.findViewById(R.id.main_view)
        mCollapseView = aView.findViewById(R.id.collapse_view)
        myAppBar = aView.findViewById(R.id.app_bar_layout)
        mCollapseIcon = aView.findViewById(R.id.collapse_user_image)
        mCollapseBack = aView.findViewById(R.id.collapse_back_image)
        mCollapseName = aView.findViewById(R.id.collapse_user_name)
        setupToolbar()
    }

    private fun setupToolbar() {
        myAppBar!!.addOnOffsetChangedListener(object : OnOffsetChangedListener {
            var scrollRange = -1
            var isShow = true
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                try {
                    if (scrollRange == -1) {
                        scrollRange = appBarLayout.totalScrollRange
                    }
                    if (scrollRange + verticalOffset == 0) {
                        mMainView!!.visibility = View.GONE
                        mCollapseView!!.visibility = View.VISIBLE
                        isShow = true
                    } else if (isShow) {
                        mCollapseView!!.visibility = View.GONE
                        mMainView!!.visibility = View.VISIBLE
                        isShow = false
                    }
                } catch (e: NotFoundException) {
                    e.printStackTrace()
                }
            }
        })
    }

    private val bundleValues: Unit
        private get() {
            val aBundle = arguments
            if (aBundle != null) {
                myChatUserUserID = aBundle.getString("ChatUserId")
                isLoginUser = getLoginUser(myContext!!).mUserId == myChatUserUserID
                if (isLoginUser) {
                    myMainProfileStateLAY!!.visibility = View.GONE
                    myUserCityNameTXT!!.visibility = View.VISIBLE
                    myLoginUserView!!.visibility = View.VISIBLE
                    myProfileIMGIcon!!.visibility = View.VISIBLE

                    mPermission = arrayOf(
                        TLConstant.CAMERA,
                        TLConstant.READ_EXTERNAL_STORAGE,
                        TLConstant.WRITE_EXTERNAL_STORAGE
                    )
                    PermissionChecker().askAllPermissions(myContext!!, mPermission)

                } else {
                    myMainProfileStateLAY!!.visibility = View.VISIBLE
                    myUserCityNameTXT!!.visibility = View.VISIBLE
                    myLoginUserView!!.visibility = View.GONE
                    myProfileIMGIcon!!.visibility = View.GONE
                }

                getProfile()
            }
        }

    private fun initProfile(aData: ChatProfile?) {
        if (aData != null) {
            myProfileLAY!!.visibility = View.VISIBLE
            myUserNameTXT!!.text = aData.fullname
            mCollapseName!!.text = aData.fullname

            profileImgURL = aData.profile_image!!
            loadImage(profileImgURL, myUserProfileIMG!!)
            loadImage(profileImgURL, mCollapseIcon!!)

            if (profileImgURL.isEmpty())
                loadPlaceHolder(myContext!!, aData.gender!!, myUserProfileIMG!!)
            else {
                myUserProfileIMG!!.setOnClickListener {
                    val dialogFragment =
                        TLSingleImagePreview(profileImgURL, R.drawable.image_placeholder)
                    dialogFragment.show(myContext!!.supportFragmentManager, TLClubMemberRights.TAG)
                }
            }

            myUserCityNameTXT!!.text = aData.status
            myUserEmailIdTET!!.text = aData.emailId
            myUserPhoneNoTET!!.text = aData.phonoNo

            if (aData.img_info.size > 0) {
                mNoPhotosVideos!!.visibility = View.GONE
                if (aData.img_info.size >= 8) {
                    myImageMoreTXT!!.visibility = View.VISIBLE
                } else {
                    myImageMoreTXT!!.visibility = View.GONE
                }
                myImageRCY!!.visibility = View.VISIBLE
                myImageAdapter = TLMsgTotalProfileImageAdapter(
                    myContext,
                    aData.img_info,
                    myImageCreatedList,
                    "Profile"
                )
                myRV_LayoutManager = GridLayoutManager(myContext, 4)
                myImageRCY!!.layoutManager = myRV_LayoutManager
                myImageRCY!!.adapter = myImageAdapter
                myImageRCY!!.isNestedScrollingEnabled = true
            } else {
                mNoPhotosVideos!!.visibility = View.VISIBLE
                mNoPhotosVideos!!.text = getNoDataText("photos and videos")
                myImageMoreTXT!!.visibility = View.GONE
                myImageRCY!!.visibility = View.GONE
            }


            if (aData.doc_info.size > 0) {
                mNoDocs!!.visibility = View.GONE
                myDocuRCY!!.visibility = View.VISIBLE
                if (aData.doc_info.size >= 5) {
                    myDocuMoreTXT!!.visibility = View.VISIBLE
                } else myDocuMoreTXT!!.visibility = View.GONE
                myDocuAdapter = TLMsgProfileIDocuAdapter(myContext, aData.doc_info)
                myRV_LayoutManager = GridLayoutManager(myContext, 2)
                myDocuRCY!!.layoutManager = myRV_LayoutManager
                myDocuRCY!!.adapter = myDocuAdapter
                myDocuRCY!!.isNestedScrollingEnabled = true
            } else {
                mNoDocs!!.visibility = View.VISIBLE
                mNoDocs!!.text = getNoDataText("documents")
                myDocuMoreTXT!!.visibility = View.GONE
                myDocuRCY!!.visibility = View.GONE
            }
        }
    }

    private fun getNoDataText(info: String): String? {
        return String.format("No shared %s ", info)
    }

    private fun addListeners() {
        myImageMoreTXT!!.setOnClickListener {
            val aBundle = Bundle()
            aBundle.putString("Chat_User_Id", myChatUserid)
            aBundle.putString("Login_User_Id", mUserInfo!!.mUserId)
            aBundle.putString("list_type", "image_list")
            myFragmentManager!!.addContent(TLChatMedia(), TLChatMedia.TAG, aBundle)
        }
        myDocuMoreTXT!!.setOnClickListener {
            val aBundle = Bundle()
            aBundle.putString("Chat_User_Id", myChatUserid)
            aBundle.putString("Login_User_Id", mUserInfo!!.mUserId)
            aBundle.putString("list_type", "document_list")
            myFragmentManager!!.addContent(TLChatMedia(), TLChatMedia.TAG, aBundle)
        }
        myBackIcon!!.setOnClickListener { myFragmentManager!!.onBackPress() }
        mCollapseBack!!.setOnClickListener { myFragmentManager!!.onBackPress() }

        myProfileIMGIcon!!.setOnClickListener {
            if (PermissionChecker().checkAllPermission(myContext!!, mPermission)) {
                FilePickerBuilder.instance.setMaxCount(1)
                    .enableImagePicker(true)
                    .enableCameraSupport(true)
                    .enableVideoPicker(false)
                    .pickPhoto(this)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PHOTO) {
            try {
                if (!data!!.getStringArrayListExtra(KEY_SELECTED_MEDIA).isEmpty()) {
                    myPath = ArrayList(data.getStringArrayListExtra(KEY_SELECTED_MEDIA))
                    updateProfile()
                } else {
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBackPressed() {}
    override fun onResumeFragment() {}
    override fun initBundle() {}
    override fun clickListener() {}

    private fun updateProfile() {
        if (checkInternet() && myPath.size > 0) {
            updatePhoto()
        }
    }

    private fun updateProfileParam(): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("user_id", getLoginUser(myContext).mUserId)
            jsonObject.put("status", getLoginUser(myContext).mStatus)
            jsonObject.put("profession", getLoginUser(myContext).mProfession)
            jsonObject.put("education", getLoginUser(myContext).mEducation)
            jsonObject.put("about_us", "Test")
            jsonObject.put("home_town", getLoginUser(myContext).mHomeTown)
            val jsonParam = JSONObject()
            jsonParam.put("UpdateProfile", jsonObject)

            Log.i("Param -->> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    private fun getProfile() {
        if (checkInternet()) {
            AppDialogs.hideSoftKeyboard((myContext as Activity?)!!, myBackIcon!!)
            AppDialogs.showProgressDialog(myContext!!)

            val mCase = getProfileParam(myChatUserUserID!!)
            val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, mCase!!)
            AppServices.execute(
                myContext!!, this,
                result,
                Request.Method.POST,
                AppServices.API.viewProfile,
                ChatProfile::class.java
            )
        }
    }

    private fun getProfileParam(id: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonObject = JSONObject()
            jsonObject.put("login_user_id", getLoginUser(myContext).mUserId)
            jsonObject.put("user_id", id)
            val jsonParam = JSONObject()
            jsonParam.put("ViewMessengerProfile", jsonObject)

            Log.i("Param -->> ", jsonParam.toString())

            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun onResume() {
        super.onResume()
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                myFragmentManager!!.onBackPress()
                //myFragmentManager.removeFragment(1);
// finish your previous fragments here.
                return@OnKeyListener true
            }
            false
        })
    }

    var profileImgURL = ""

    override fun onClick(v: View) {}
    override fun onPause() {
        super.onPause()
    }

    companion object {
        var TAG = TLChatProfile::class.java.simpleName
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.viewProfile.hashCode()) {
                if (r.response!!.isSuccess) {
                    val data = (r as ChatProfile).mData
                    initProfile(data)
                } else AppDialogs.showToastDialog(myContext!!, r.response!!.responseMessage!!)
            }
        } else AppDialogs.customOkAction(
            myContext!!,
            null,
            TLConstant.SERVER_NOT_REACH,
            null,
            null,
            false
        )
    }

    fun updatePhoto() {
        activity!!.runOnUiThread {
            AppDialogs.showProgressDialog(myContext!!)
        }
        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, updateProfileParam()!!)
        val httpclient: HttpClient? = DefaultHttpClient()
        val httppost = HttpPost(result)

        val multipartEntity: MultipartEntityBuilder = MultipartEntityBuilder.create()
        if (myPath.size > 0) {
            multipartEntity.addBinaryBody("profile_image", File(myPath[0]))
        }
        httppost.entity = MyHttpEntity(multipartEntity.build(), MyHttpEntity.ProgressListener {
        })
        Thread {
            //Do some Network Request
            val httpResponse = httpclient!!.execute(httppost)
            val httpEntity = httpResponse.entity

            val aJsonResponse = EntityUtils.toString(httpEntity)
            Log.i("Image -->", aJsonResponse)

            activity!!.runOnUiThread {
                if (aJsonResponse.isNotEmpty()) {
                    try {
                        AppDialogs.hideProgressDialog()
                        val aJsonObject = JSONObject(aJsonResponse)
                        if (aJsonObject.has("response")) {
                            val response_msg: JSONObject = aJsonObject.getJSONObject("response")
                            AppDialogs.showToastDialog(
                                myContext!!,
                                response_msg.getString("response_msg")
                            )

                            if (response_msg.getString("response_code") == "1") {
                                myUserProfileIMG!!.setImageBitmap(Utility.rotateImage(File(myPath[0])))
                            }

                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()


    }
}