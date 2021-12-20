package com.truelife.app.activity.feedpost.activity

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.truelife.BuildConfig
import com.truelife.ClickListener
import com.truelife.R
import com.truelife.app.activity.club.ClubListActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.ClubInfo
import com.truelife.app.model.MediaTypeModel
import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.http.MyHttpEntity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.*
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
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
import kotlin.Comparator

class PostFeedDirectActivity : BaseActivity(), ResponseListener, ClickListener {
    var mPostTypeList: ArrayList<String>? = ArrayList()
    lateinit var selectedValue: String
    private var mPermission: Array<String> = arrayOf()
    private var mFilePath: ArrayList<String> = arrayListOf()
    var myPostImageFile: ArrayList<File> = arrayListOf()

    // var mCompressedImageFile: ArrayList<File> = arrayListOf()
    var count: Int = 0
    var myClundImages: ArrayList<MediaTypeModel> = arrayListOf()
    private var myPostFileName: ArrayList<String> = arrayListOf()
    var mWhereToPostStr: String = ""
    var mSourceTypeStr: String = "0"
    var mClubIdStr: String = "0"
    var mUser: User? = null
    var mNotifyArray: StringBuilder = java.lang.StringBuilder()
    var MClubArray: StringBuilder = java.lang.StringBuilder()
    private var mClubInfo: ArrayList<ClubInfo> = arrayListOf()


    private fun isHasIncomingShare(): Boolean {
        val intent = intent
        if (intent != null) {
            val action = intent.action
            val type = intent.type
            if (!(action.isNullOrEmpty() || type.isNullOrEmpty())) {
                Log.e("Image shared", type)
                return if (Intent.ACTION_SEND == action && type != null || Intent.ACTION_SEND_MULTIPLE == action && type != null) {
                    myPostImageFile.clear()
                    if (Intent.ACTION_SEND_MULTIPLE == action) {
                        val imageUris: ArrayList<Uri> =
                            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)

                        if (!imageUris.isNullOrEmpty()) {
                            imageUris.forEach {
                                val filePath = RealPathUtil.getRealPath(this, it)
                                myPostImageFile.add(File(filePath))
                                Log.e("File Path", filePath)
                            }
                        } else {
                            //One Image
                            val imageUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM) as Uri
                            val filePath = RealPathUtil.getRealPath(this, imageUri)
                            myPostImageFile.add(File(filePath))

                        }
                    }else{
                        val imageUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM) as Uri
                        val filePath = RealPathUtil.getRealPath(this, imageUri)
                        myPostImageFile.add(File(filePath))
                    }

                    true
                } else false
            } else {
                return false
            }
        } else {
            return false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(myContext).unregisterReceiver(myBroadcastReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_feed_direct)
        init()

        LocalBroadcastManager.getInstance(myContext)
            .registerReceiver(myBroadcastReceiver, IntentFilter(TLConstant.SelectedClubList))
    }

    fun addResource() {
        if (LocalStorageSP.get(myContext, TLConstant.SourceType, "0").equals("2")) {
            mPostTypeList!!.add("Friends")
            mPostTypeList!!.add("Friends of friends")
        } else {
            mPostTypeList!!.add("Your Area")
            mPostTypeList!!.add("Your City")
            mPostTypeList!!.add("Your State")
            mPostTypeList!!.add("Your Country")
            mPostTypeList!!.add("International")
        }
    }

    fun chooseImages() {

        if (PermissionChecker().checkAllPermission(myContext, mPermission)) {
            FilePickerBuilder.instance.setMaxCount(5)
                //.setSelectedFiles(filePaths)
                .setActivityTheme(R.style.FilePickerTheme)
                .enableVideoPicker(true)
                .enableCameraSupport(true)
                .showGifs(false)
                .showFolderView(true)
                .enableImagePicker(true)
                .setCameraPlaceholder(R.drawable.ic_action_camera)
                .pickPhoto(this)
        }
    }

    override fun clickListener() {

        fragment_feed_post_your_club_puls_TXT.setOnClickListener {
            val aIntent = Intent(myContext, ClubListActivity::class.java)
            myContext.startActivity(aIntent)

        }

        fragment_feed_post_SPN_where_post.setOnClickListener {

            alertDialogWhereDoPost(mPostTypeList!!, "")
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
        aDlg.setCancelable(false)
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
        if (LocalStorageSP.get(myContext, TLConstant.SourceType, "0").equals("1") || mSourceTypeStr.equals("1")) {
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
        aBackArrow.setOnClickListener {
            aDlg.dismiss()
            this@PostFeedDirectActivity.finish()
        }
        aDoneBtn.setOnClickListener {
            fragment_feed_post_SPN_where_post.setText(selectedValue)
            aDlg.dismiss()
            initUpload()
        }
    }

    private fun initUpload() {
        mUser = LocalStorageSP.getLoginUser(myContext)
        if (mUser!!.mIsMobileVerified == "1") {
            if (myPostImageFile.size > 0) {
                uploadCloudnary()
            } else AppDialogs.customSuccessAction(
                myContext,
                null,
                "Please select attachment",
                null,
                object : AppDialogs.ConfirmListener {
                    override fun yes() {
                        chooseImages()
                    }

                },
                false
            )
        } else {
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
    }

    override fun init() {
        try {
            mUser = LocalStorageSP.getLoginUser(myContext)
            mSourceTypeStr = LocalStorageSP.get(myContext, TLConstant.SourceType, "0")!!
            addResource()
            mPermission = arrayOf(
                TLConstant.CAMERA,
                TLConstant.READ_EXTERNAL_STORAGE,
                TLConstant.WRITE_EXTERNAL_STORAGE
            )
            PermissionChecker().askAllPermissions(myContext!!, mPermission)
            clickListener()
            reset()
            /* val intent = intent
                 val action = intent.action
                 val type: String = intent.type!!
                 Log.e("Image shared",type)*/
            if (isHasIncomingShare()) {

                AppDialogs.ShowShareDialogDirect(myContext, this)
                /* if (type.startsWith(MimeTypes.IMAGE)) {
     *//*
                    val imageUris: ArrayList<Uri> =
                        getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM)
                    val filePath: String = RealPathUtil.getRealPath(this,  imageUris.get(0))*//*
                    Log.e("Image shared", "filePath")
                    //   handleImageShare(selectedUsers)
                } else if (type.startsWith(MimeTypes.VIDEO)) {
                    //    handleVideoShare()
                }*/
            } else {
                Handler().postDelayed({
                    try {
                        chooseImages()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, 1500)
            }
        } catch (e: Exception) {
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

    override fun onResponse(r: Response?) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data == null)
            initUpload()
        else if (data == null)
            this@PostFeedDirectActivity.finish()

        try {
            if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
                myPostImageFile.clear()
                mFilePath =
                    java.util.ArrayList(data!!.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA))

                for (i in mFilePath.indices) {
                    val aFile = File(mFilePath.get(i))
                    if (Utility.findMediaType(aFile.absolutePath)!!.contains("image"))
                        myPostImageFile.add(FileCompression.compressImage(myContext, aFile))
                    else
                        myPostImageFile.add(File(mFilePath.get(i)))
                    //  myPostImageFile.add(FileCompression.compressImage(myContext, File(mFilePath.get(i))))
                    //    myPostImageFile.add(File(mFilePath.get(i)))
                }
//                checkvisbility()
                if (mSourceTypeStr.equals("3")) {
                    fragment_feed_post_your_club_puls_TXT.performClick()
                } else
                    fragment_feed_post_SPN_where_post.performClick()

            }
        } catch (e: Exception) {
        }
    }

    fun checkvisbility() {

        if (mSourceTypeStr.equals("3")) {
            fragment_feed_post_Liner_LAY_where_post.visibility = View.INVISIBLE
            fragment_feed_post_Liner_LAY_your_club.visibility = View.VISIBLE
        } else {
            fragment_feed_post_Liner_LAY_where_post.visibility = View.VISIBLE
            fragment_feed_post_Liner_LAY_your_club.visibility = View.INVISIBLE
        }
    }

    fun reset() {
        if (mSourceTypeStr.equals("3")) {
            fragment_feed_post_Liner_LAY_where_post.visibility = View.INVISIBLE
            fragment_feed_post_Liner_LAY_your_club.visibility = View.INVISIBLE
        }
    }


    /*  fun upload() {


              if (myPostImageFile.size > 0) {
                  mCompressedImageFile.clear()
                  for (i in 0 until myPostImageFile.size) {
                      AppDialogs.showProgressDialog(myContext)
                      val size = Utility.getFileSize(myPostImageFile.get(i))
                      if (size > TLConstant.IMAGE_FILE_SIZE_LIMIT_3MB && ((myPostImageFile.get(i).absoluteFile.toString()
                              .toLowerCase()
                              .contains(".jpg")) || (myPostImageFile.get(i).absoluteFile.toString()
                              .toLowerCase().contains(".png")))
                      ) {


                          mCompressedImageFile.add(
                              FileCompression.compressImage(myContext, myPostImageFile.get(i))
                          )



                      } else {
                          mCompressedImageFile.add(myPostImageFile.get(i))
                      }
                      AppDialogs.hideProgressDialog()

                  }

              }
              if (mCompressedImageFile.size == myPostImageFile.size)
                  uploadCloudnary()

      }*/
    fun uploadCloudnary() {
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
                        //Log.e("uploaded progress $bytes ", "Total - $totalBytes")
                        val aStr = ((bytes * 100) / totalBytes)
                        //    Log.e("uploaded progress ", "" + aStr)
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
                                for (i in myClundImages.indices) {
                                    temp.add(myClundImages.get(i).path!!)
                                    myPostFileName.add(myClundImages.get(i).path!!)
                                    Log.d("Final_URL --> ", myClundImages.get(i).path)
                                    if (myClundImages.size == myPostFileName.size) {
                                        AppDialogs.hideProgressBarCounting()
                                        report()
                                    }
                                }
                                //  AppDialogs.hideProgressBarCounting()
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


    fun report() {
        if (checkInternet())
            AppDialogs.showProgressDialog(myContext)
            post()
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


        var httpclient: HttpClient? = DefaultHttpClient()
        val httppost = HttpPost(result)


        val multipartEntity: MultipartEntityBuilder = MultipartEntityBuilder.create()

        httppost.entity = MyHttpEntity(multipartEntity.build(), MyHttpEntity.ProgressListener {
            runOnUiThread { AppDialogs.setProgressCount(it.toInt()) }
            Log.e("Progress --> ", it.toString())
        })
        Thread({
            //Do some Network Request
            val httpResponse = httpclient!!.execute(httppost)
            val httpEntity = httpResponse.entity

            var aJsonResponse = EntityUtils.toString(httpEntity)
            Log.e("Report Response --> ", aJsonResponse)
            runOnUiThread({


                if (aJsonResponse != "") { //  TLHelper.hideDialog(myProgressDialog);
                    try {
                        var aJsonObject = JSONObject(aJsonResponse)
                        AppDialogs.hideProgressBarCounting()
                        if (aJsonObject.has("response")) {
                            var response_msg: JSONObject = aJsonObject.getJSONObject("response")
                            var ss = response_msg.getString("response_msg")
                            var responsecode = response_msg.getString("response_code")
                            AppDialogs.showToastDialog(myContext, ss)
                            if (responsecode.equals("1"))
                                finish()
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {

                }
            })
        }).start()
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

    // public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (intent.action.equals(TLConstant.SelectedClubList)) {
                    val bundle = intent.extras
                    if (bundle!!.containsKey(TLConstant.SelectedClubList)) {
                        mClubInfo =
                            bundle.getSerializable(TLConstant.SelectedClubList) as ArrayList<ClubInfo>
                        if (mClubInfo.size > 0) {
                            if (myPostImageFile.size > 0) {
                                uploadCloudnary()
                            } else AppDialogs.customSuccessAction(
                                myContext,
                                null,
                                "Please select attachment",
                                null,
                                object : AppDialogs.ConfirmListener {
                                    override fun yes() {
                                        chooseImages()
                                    }

                                },
                                false
                            )
                        } else {
                            /*  AppDialogs.customSuccessAction(
                                  myContext,
                                  null,
                                  "Please select atleast one club",
                                  null,
                                  null,
                                  false
                              )*/
                            finish()
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
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

    override fun click(position: Int) {
        mSourceTypeStr = position.toString()
        Log.e("CLicked", position.toString())
        if (mSourceTypeStr.equals("3")) {
            fragment_feed_post_your_club_puls_TXT.performClick()
        } else if(mSourceTypeStr.equals("2")) {
            mPostTypeList!!.clear()
            mPostTypeList!!.add("Friends")
            mPostTypeList!!.add("Friends of friends")
            fragment_feed_post_SPN_where_post.performClick()
        }  else if(mSourceTypeStr.equals("1")) {
            mPostTypeList!!.clear()
            mPostTypeList!!.add("Your Area")
            mPostTypeList!!.add("Your City")
            mPostTypeList!!.add("Your State")
            mPostTypeList!!.add("Your Country")
            mPostTypeList!!.add("International")
            fragment_feed_post_SPN_where_post.performClick()
        }else
            fragment_feed_post_SPN_where_post.performClick()
    }

}