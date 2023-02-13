package com.truelife.app.activity.feedpost.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.activity.club.ClubListActivity
import com.truelife.app.activity.feedpost.adapter.SelectedClubListAdapter
import com.truelife.app.activity.feedpost.adapter.TLFeedPostImageListAdapter
import com.truelife.app.constants.TLConstant
import com.truelife.app.constants.TLConstant.SelectedClubList
import com.truelife.app.constants.TLConstant.SourceType
import com.truelife.app.model.ClubInfo
import com.truelife.app.model.MediaTypeModel
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.base.TLFragmentManager
import com.truelife.chat.activities.main.messaging.ChatActivity
import com.truelife.chat.utils.FileUtils
import com.truelife.http.MyHttpEntity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.*
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst.KEY_SELECTED_MEDIA
import droidninja.filepicker.FilePickerConst.REQUEST_CODE_PHOTO
import kotlinx.android.synthetic.main.activity_post_screen.*
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class TLScreenPostFeedActivity : BaseActivity(), ResponseListener,
    SelectedClubListAdapter.ClickListener {


    var mView: View? = null
    var mUser: User? = null
    var mPostTypeList: ArrayList<String>? = ArrayList()
    lateinit var selectedValue: String
    lateinit var postButton: Button
    lateinit var mFilePickerTxt: TextView
    lateinit var feedPostTypeTxt: TextView
    lateinit var mImageRecycleView: RecyclerView
    private var mPermission: Array<String> = arrayOf()
    private lateinit var myMenuRecyclerAdapter: TLFeedPostImageListAdapter
    private lateinit var mSelectedClubAdapter: SelectedClubListAdapter
    var mFragmentManager: TLFragmentManager? = null
    var mBackButton: ImageView? = null
    private var mFilePath: ArrayList<String> = arrayListOf()
    private var myPostFileName: ArrayList<String> = arrayListOf()
    private var mClubInfo: ArrayList<ClubInfo> = arrayListOf()

    var mNotifyArray: StringBuilder = java.lang.StringBuilder()
    var MClubArray: StringBuilder = java.lang.StringBuilder()
    var count: Int = 0

    var mWhereToPostStr: String = ""
    var myPostImageFile: ArrayList<File> = arrayListOf()
    var myClundImages: ArrayList<MediaTypeModel> = arrayListOf()
    var mClubDetail: ClubInfo = ClubInfo()

    //  var myGetFileSize = 0

    // 1- public
    //2 - friends
    // 3 - Club
    var mSourceTypeStr: String = "0"
    var mClubIdStr: String = "0"

    companion object {
        var TAG: String = TLScreenPostFeedActivity::class.java.simpleName
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_screen)
        init()
        setRecylceview()
        LocalBroadcastManager.getInstance(myContext)
            .registerReceiver(myBroadcastReceiver, IntentFilter(SelectedClubList))
    }

    private fun pickImages() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    Matisse.from(myContext)
                        .choose(
                            MimeType.of(
                                MimeType.MP4,
                                MimeType.JPEG,
                                MimeType.PNG,
                            )
                        )
                        .countable(true)
                        .maxSelectable(ChatActivity.MAX_SELECTABLE)
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .thumbnailScale(0.85f)
                        .imageEngine(GlideEngine())
                        .maxSelectable(5)
                        .forResult(ChatActivity.PICK_GALLERY_REQUEST)
                }

                override fun onPermissionRationaleShouldBeShown(
                    list: List<PermissionRequest>,
                    permissionToken: PermissionToken
                ) {
                    Toast.makeText(
                        myContext,
                        R.string.missing_permissions,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            .check()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(myContext).unregisterReceiver(myBroadcastReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == ChatActivity.PICK_GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
                 mFilePath = Matisse.obtainPathResult(data) as ArrayList<String> /* = java.util.ArrayList<kotlin.String> */
                for (mPath in mFilePath) {
                    if (!FileUtils.isFileExists(mPath)) {
                        Toast.makeText(
                            myContext,
                            R.string.image_video_not_found,
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }
                myPostImageFile.clear()
                mImageRecycleView.visibility = View.VISIBLE

                for (i in mFilePath.indices) {
                    val aFile = File(mFilePath.get(i))
                    if (Utility.findMediaType(aFile.absolutePath)!!.contains("image"))
                        myPostImageFile.add(aFile)
                    // myPostImageFile.add(FileCompression.compressImage(myContext, aFile))
                    else
                        myPostImageFile.add(File(mFilePath.get(i)))

                }
                setRecylceview()
            }
            if (requestCode == REQUEST_CODE_PHOTO) {
                myPostImageFile.clear()
                mImageRecycleView.visibility = View.VISIBLE
                mFilePath = java.util.ArrayList(data!!.getStringArrayListExtra(KEY_SELECTED_MEDIA))
                for (i in mFilePath.indices) {
                    val aFile = File(mFilePath.get(i))
                    if (Utility.findMediaType(aFile.absolutePath)!!.contains("image"))
                        myPostImageFile.add(aFile)
                       // myPostImageFile.add(FileCompression.compressImage(myContext, aFile))
                    else
                        myPostImageFile.add(File(mFilePath.get(i)))

                }
                setRecylceview()//  myMenuRecyclerAdapter.notifyDataSetChanged()
            } else if (requestCode == 1 && resultCode == Activity.RESULT_OK)
                feedPost()
        } catch (e: Exception) {
        }
    }

    fun setRecylceview() {
        myMenuRecyclerAdapter = TLFeedPostImageListAdapter(myContext, mFilePath)
        val horizontalLayoutManagaer =
            LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false)
        mImageRecycleView.setLayoutManager(horizontalLayoutManagaer)
        mImageRecycleView.setAdapter(myMenuRecyclerAdapter)
    }


    fun setClubRecylceview() {
        fragment_feed_post_screen_your_club_RCY.visibility = View.VISIBLE
        mSelectedClubAdapter = SelectedClubListAdapter(myContext, mClubInfo, this)
        val horizontalLayoutManagaer = GridLayoutManager(myContext, 3)
        fragment_feed_post_screen_your_club_RCY.setLayoutManager(horizontalLayoutManagaer)
        fragment_feed_post_screen_your_club_RCY.setAdapter(mSelectedClubAdapter)
    }

    override fun clickListener() {
        fragment_feed_post_your_club_puls_TXT.setOnClickListener {
            val aIntent = Intent(myContext, ClubListActivity::class.java)
            var bundle = Bundle()
            bundle.putSerializable(SelectedClubList, mClubInfo)
            aIntent.putExtras(bundle)
            myContext.startActivity(aIntent)

        }

        mBackButton!!.setOnClickListener {
            finish()
        }

        postButton.setOnClickListener(View.OnClickListener {
            feedPost()
        })
        feedPostTypeTxt.setOnClickListener {
            alertDialogWhereDoPost(mPostTypeList!!, "")
        }
        mFilePickerTxt.setOnClickListener {


            if (PermissionChecker().checkAllPermission(myContext, mPermission)) {
                pickImages()
               /* FilePickerBuilder.instance.setMaxCount(5)
                    //.setSelectedFiles(filePaths)
                    .setActivityTheme(R.style.FilePickerTheme)
                    .enableVideoPicker(true)
                    .enableCameraSupport(true)
                    .showGifs(false)
                    .showFolderView(true)
                    .enableImagePicker(true)
                    .setCameraPlaceholder(R.drawable.ic_action_camera)
                    .pickPhoto(this)*/
            }
        }

    }

    fun visibility() {
        if (mSourceTypeStr.equals("3")) {
            fragment_feed_post_Liner_LAY_where_post.visibility = View.GONE
            fragment_feed_post_Liner_LAY_your_club.visibility = View.VISIBLE
            common_text_header.setText("CLUBS")
            text_header.visibility = View.VISIBLE
        } else {
            fragment_feed_post_Liner_LAY_your_club.visibility = View.GONE
            text_header.visibility = View.GONE
            fragment_feed_post_Liner_LAY_where_post.visibility = View.VISIBLE
        }
    }

    override fun init() {
        mFragmentManager = TLFragmentManager(myContext)
        mUser = LocalStorageSP.getLoginUser(myContext)
        postButton = findViewById(R.id.feed_post_button) as Button
        feedPostTypeTxt = findViewById(R.id.fragment_feed_post_SPN_where_post) as TextView
        mBackButton = findViewById(R.id.common_back_arrow) as ImageView
        mImageRecycleView =
            findViewById(R.id.fragment_feed_post_screen_post_RCY) as RecyclerView
        mFilePickerTxt = findViewById(R.id.fragment_feed_post_photo_TXT) as TextView
        mSourceTypeStr = LocalStorageSP.get(myContext, TLConstant.SourceType, "0")!!
        addResource()
        setRecylceview()
        visibility()
        mPermission = arrayOf(
            TLConstant.CAMERA,
            TLConstant.READ_EXTERNAL_STORAGE,
            TLConstant.WRITE_EXTERNAL_STORAGE
        )
        PermissionChecker().askAllPermissions(myContext, mPermission)
        clickListener()
        if (intent.hasExtra("share_thoughts")) {
            fragment_feed_post_your_club_puls_TXT.visibility = View.GONE
            mClubDetail = intent.getSerializableExtra("share_thoughts") as ClubInfo
            mClubInfo.add(mClubDetail)
            setClubRecylceview()
        } else {
            fragment_feed_post_your_club_puls_TXT.visibility = View.VISIBLE
        }
    }

    fun feedPost() {
        if (validate()) {

            mUser = LocalStorageSP.getLoginUser(myContext)
            if (mUser!!.mIsMobileVerified!! == "1" || mUser!!.mIsMessengerMobileVerified!! == "1") {
                if (mFilePath.size > 0)
                    uploadCloudnary()
                else {
                    report()
                }
            } else openOTP()
        }
    }

    fun addResource() {
        if (mSourceTypeStr.equals("2")) {
            mPostTypeList!!.add("Friends")
            mPostTypeList!!.add("Friends of friends")
        } else {
            if (mUser?.mCountryId.isNullOrEmpty() || mUser?.mCountryId  == "0") {
                mPostTypeList!!.add("Internationally")
            } else {
                mPostTypeList!!.add("Your Area")
                mPostTypeList!!.add("Your City")
                mPostTypeList!!.add("Your State")
                mPostTypeList!!.add("Your Country")
                mPostTypeList!!.add("Internationally")
            }
        }
    }

    private fun alertDialogWhereDoPost(
        aVisibleList: List<String>,
        aName: String
    ) {
        val aDlg = Dialog(myContext)
        val window = aDlg.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER
        wlp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window.attributes = wlp
        aDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        aDlg.setTitle(aName)
        aDlg.setContentView(R.layout.layout_inflate_feed_post_type)
        aDlg.setCanceledOnTouchOutside(true)
        aDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val aListView: ListView
        val aWhereToPostNP: NumberPicker
        val aHeaderName =
            aDlg.findViewById<View>(R.id.header_name) as TextView
        val aBackArrow =
            aDlg.findViewById<View>(R.id.layout_inflate_feed_post_type_back_arrow) as ImageView
        val aPostType =
            aDlg.findViewById<View>(R.id.layout_inflate_feed_post_type_post_type_image) as ImageView
        val aPostName =
            aDlg.findViewById<View>(R.id.layout_inflate_feed_post_type_post_type) as TextView
        val aDoneBtn =
            aDlg.findViewById<View>(R.id.dialog_post_done_btn) as Button
        if (LocalStorageSP.get(myContext, SourceType, "0").equals("1")) {
            aPostType.setImageDrawable(
                myContext.getResources().getDrawable(R.drawable.ic_public)
            )
            aPostName.setText(myContext.getResources().getString(R.string.label_share_public))
        } else {
            aPostType.setImageDrawable(
                myContext.getResources().getDrawable(R.drawable.clubs_main_icon)
            )
            aPostName.setText(myContext.getResources().getString(R.string.label_share_friends))
        }
        aHeaderName.text = getString(R.string.labe_where_do_you_want_to_post)

        aWhereToPostNP =
            aDlg.findViewById<View>(R.id.where_to_post_np) as NumberPicker
        val strarray = aVisibleList.toTypedArray()
        selectedValue = strarray[0]
        //Populate NumberPicker values from String array values
//Set the minimum value of NumberPicker
        aWhereToPostNP.minValue = 0 //from array first value
        //Specify the maximum value/number of NumberPicker
        aWhereToPostNP.maxValue = strarray.size - 1 //to array last value
        //Specify the NumberPicker data source as array elements
        aWhereToPostNP.displayedValues = strarray
        aWhereToPostNP.isMeasureWithLargestChildEnabled = true
        //Gets whether the selector wheel wraps when reaching the min/max value.
        aWhereToPostNP.wrapSelectorWheel = false
        aWhereToPostNP.isVerticalFadingEdgeEnabled = true
        aWhereToPostNP.isHorizontalFadingEdgeEnabled = true
        aWhereToPostNP.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        //Set a value change listener for NumberPicker
        aWhereToPostNP.setOnValueChangedListener { picker, oldVal, newVal ->
            //Display the newly selected value from picker
            selectedValue = strarray[newVal]
        }
        aDlg.show()
        aBackArrow.setOnClickListener { aDlg.dismiss() }
        aDoneBtn.setOnClickListener {
            fragment_feed_post_SPN_where_post.setText(selectedValue)
            aDlg.dismiss()
            // dummyEdt.requestFocus()
            //   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // fragment_feed_post_photo_TXT.requestFocus()
            //  }
            AppDialogs.hideSoftKeyboard(myContext, fragment_feed_post_SPN_where_post)
        }
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.feedpost.hashCode()) {
                if (r.response!!.isSuccess) {

                }
            }

        }
    }

    fun whereToPost() {

        mWhereToPostStr =
            when (fragment_feed_post_SPN_where_post.getText().toString().trim({ it <= ' ' })) {
                "Your Area" -> "Area"
                "Your City" -> "City"
                "Your State" -> "State"
                "Your Country" -> "Country"
                "Internationally" -> "International"
                "Friends" -> "Friends"
                "Friends of friends" -> "Friends of friends"
                else -> ""
            }

    }


    private fun getFeedPostInfo(
        aShareTXT: String,
        aFromPost: String,
        aNotifyArray: StringBuilder,
        aClubArray: StringBuilder,
        sourceId: String,
        aClubPostClubid: String
    ): String? {
        var aFromPost = aFromPost
        var aCaseStr = " "
        try {
            if (aFromPost.isEmpty() && sourceId == "1") {
                aFromPost = "state"
            }
            val jsonParam1 = JSONObject()
            jsonParam1.put("content", aShareTXT)
            jsonParam1.put("user_id", mUser!!.mUserId)
            jsonParam1.put("privacy_post", "1")
            jsonParam1.put("source", sourceId)
            jsonParam1.put("level", aFromPost)
            jsonParam1.put("type", "users")
            jsonParam1.put("tagged", "1")
            jsonParam1.put("tagged_users", aNotifyArray)
            if (!aClubArray.isEmpty()) {
                jsonParam1.put("club_id", aClubArray)
            } else {
                jsonParam1.put("club_id", aClubPostClubid)
            }
            val jsonArray_image = JSONArray()
            val jsonArray_MediaType = JSONArray()
            val jsonArray_resoulation = JSONArray()
            // StringBuilder apostImagesArray = new StringBuilder();


            if (myClundImages.size > 0) {
                for (i in myClundImages.indices) {
                    if (i != myClundImages.size - 1) {
                        jsonArray_image.put(myClundImages[i].path.toString() + "")
                        jsonArray_MediaType.put(myClundImages[i].media_type.toString() + "")
                        jsonArray_resoulation.put(myClundImages[i].resolution.toString() + "")
                        //apostImagesArray.append( myPostFileName.get( i )+"," );
                    } else {
                        jsonArray_image.put(myClundImages[i].path.toString() + "")
                        jsonArray_MediaType.put(myClundImages[i].media_type.toString() + "")
                        jsonArray_resoulation.put(myClundImages[i].resolution.toString() + "")
                        //apostImagesArray.append( myPostFileName.get( i )+"" );
                    }
                }
            } else {
                jsonParam1.put("media", jsonArray_image)
                jsonParam1.put("media_type", jsonArray_MediaType)
                jsonParam1.put("resolution", jsonArray_resoulation)
            }
            jsonParam1.put("media", jsonArray_image)
            jsonParam1.put("media_type", jsonArray_MediaType)
            jsonParam1.put("resolution", jsonArray_resoulation)


            val jsonParam = JSONObject()
            jsonParam.put("PostFeed", jsonParam1)
            Log.e("PostFeed", "" + jsonParam.toString())
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            Log.e("PostFeed", " $aCaseStr")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    fun uploadCloudnary() {
        myClundImages.clear()
        count = 0
        AppDialogs.showProgressBarCounting(myContext)

        for (i in myPostImageFile.indices) {
            Log.d("getAbsolutePath --> ", myPostImageFile.get(i).getAbsolutePath())
            val isImage =
                if (Utility.findMediaType(
                        myPostImageFile.get(i).getAbsolutePath()
                    ) === "video"
                ) "video" else "image"
            val cloudData = MediaTypeModel()
            cloudData.id = i.toString()
            if (isImage == "image") {
                cloudData.media_type = "0"
            } else {
                cloudData.media_type = "1"
            }
            MediaManager.get()
                .upload(myPostImageFile.get(i).getAbsolutePath())
                .option("resource_type", isImage)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(
                        requestId: String,
                        bytes: Long,
                        totalBytes: Long
                    ) {
                        val aStr = ((bytes * 100) / totalBytes)
                        //  Log.e("uploaded progress ", "" + aStr)
                        runOnUiThread {
                            AppDialogs.setProgressCount(

                                aStr.toInt()
                            )
                        }
                    }

                    override fun onSuccess(
                        requestId: String,
                        resultData: Map<*, *>
                    ) {
                        try {
                            count++
                            Log.d("onSuccess --> ", requestId)
                            Log.d("URL --> ", resultData["url"].toString())
                            cloudData.path =
                                resultData["url"].toString() //width  // "height" -> "1280"
                            cloudData.resolution =
                                resultData["width"].toString() + "X" + resultData["height"].toString() //width  // "height" -> "1280"
                            myClundImages.add(cloudData)
                            if (count == myPostImageFile.size) { // reach the last upload image
                                Collections.sort(
                                    myClundImages,
                                    Comparator<MediaTypeModel> { c1, c2 ->
                                        c1.id!!.compareTo(
                                            c2.id!!,
                                            true
                                        )
                                    })
                                val temp = java.util.ArrayList<String>()
                                myPostFileName.clear()
                                for (i in myClundImages.indices) {
                                    temp.add(myClundImages.get(i).path!!)
                                    myPostFileName.add(myClundImages.get(i).path!!)
                                    Log.d("Final_URL --> ", myClundImages.get(i).path)
                                    if (myClundImages.size == myPostFileName.size) {
                                        AppDialogs.hideProgressBarCounting()
                                        report()
                                    }
                                }
                                AppDialogs.hideProgressDialog()
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.d("onError --> ", error.toString())
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.d("onReschedule --> ", error.toString())
                    }
                }).dispatch()
        }
    }

    fun getClubId() {

        if (!mClubInfo.isNullOrEmpty()) {
            if (mClubInfo.size > 0) {
                mClubInfo.forEach {
                    MClubArray.append(it.id + ",")
                }
            }
        }
        if (!MClubArray.isEmpty()) {
            if (MClubArray.contains(","))
                MClubArray.substring(0, MClubArray.length - 1)
        }
    }

    fun validate(): Boolean {

        if (mFilePath.size == 0 && getETValue(fragment_feed_post_thoughts_Edt).length == 0) {
            AppDialogs.customSuccessAction(
                this,
                null,
                "Not to post without content or media",
                null,
                null,
                false
            )
            return false
        }
        if (LocalStorageSP.get(myContext, SourceType, "0").equals("3")) {
            if (mClubInfo.size == 0) {
                AppDialogs.customSuccessAction(
                    this,
                    null,
                    "Select Club",
                    null,
                    null,
                    false
                )
                return false
            }
        } else {

            if (getTXTValue(fragment_feed_post_SPN_where_post).length == 0) {
                AppDialogs.customSuccessAction(
                    this,
                    null,
                    "Select where do you want to post",
                    null,
                    null,
                    false
                )
                return false
            }
        }
        return true
    }

    fun report() {
        AppDialogs.showProgressDialog(myContext)
        if (checkInternet())
            post()
    }

    private fun openOTP() {
        when (mSourceTypeStr) {
            "1" -> Helper.navigateOTPScreen(
                myContext,
                String.format(getString(R.string.label_post_otp_verify), "public"), 1
            )
            "2" -> Helper.navigateOTPScreen(
                myContext,
                String.format(getString(R.string.label_post_otp_verify), "friends"), 1
            )
            "3" -> Helper.navigateOTPScreen(
                myContext,
                String.format(getString(R.string.label_post_otp_verify), "clubs"), 1
            )
        }
    }

    private fun post() {
        getClubId()
        whereToPost()

        val result = Helper.GenerateEncrptedUrl(
            BuildConfig.API_URL, getFeedPostInfo(
                Utility.EncodeEmojiandText(
                    getETValue(
                        fragment_feed_post_thoughts_Edt
                    )
                )!!,
                mWhereToPostStr,
                mNotifyArray,
                MClubArray,
                mSourceTypeStr,
                mClubIdStr
            )!!
        )


        val httpclient: HttpClient? = DefaultHttpClient()
        val httppost = HttpPost(result)


        val multipartEntity: MultipartEntityBuilder = MultipartEntityBuilder.create()

        httppost.entity = MyHttpEntity(multipartEntity.build(), MyHttpEntity.ProgressListener {
            /* runOnUiThread { AppDialogs.setProgressCount(it.toInt()) }
             Log.e("Progress --> ", it.toString())*/
        })
        Thread({
            //Do some Network Request
            val httpResponse = httpclient!!.execute(httppost)
            val httpEntity = httpResponse.entity

            val aJsonResponse = EntityUtils.toString(httpEntity)
            Log.e("Report Response --> ", aJsonResponse)
            runOnUiThread({

                if (aJsonResponse != "") { //  TLHelper.hideDialog(myProgressDialog);
                    try {
                        val aJsonObject = JSONObject(aJsonResponse)
                        AppDialogs.hideProgressBarCounting()
                        AppDialogs.hideProgressDialog()
                        if (aJsonObject.has("response")) {
                            val response_msg: JSONObject = aJsonObject.getJSONObject("response")
                            val ss = response_msg.getString("response_msg")
                            val responsecode = response_msg.getString("response_code")
                            AppDialogs.showToastDialog(myContext, ss)
                            if (responsecode.equals("1"))
                                finish()
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            })
        }).start()
    }

    override fun onClick(tt: Any, position: Int) {
        mClubInfo.removeAt(position)
        mSelectedClubAdapter.notifyDataSetChanged()

    }

    override fun onLongClick(view: View?, position: Int) {

    }

    // public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (intent.action.equals(SelectedClubList)) {
                    val bundle = intent.extras
                    if (bundle!!.containsKey(SelectedClubList)) {
                        mClubInfo =
                            bundle.getSerializable(SelectedClubList) as ArrayList<ClubInfo>

                        setClubRecylceview()
                        //   Log.e("Selected Clubs", "" + mClubInfo.size)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}