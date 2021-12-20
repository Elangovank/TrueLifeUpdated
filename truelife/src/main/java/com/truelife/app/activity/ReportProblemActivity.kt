package com.truelife.app.activity

import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.activity.feedpost.adapter.TLFeedPostImageListAdapter
import com.truelife.app.constants.TLConstant
import com.truelife.app.constants.TLConstant.IMAGE_FILE_SIZE_LIMIT_3MB

import com.truelife.app.model.User
import com.truelife.base.BaseActivity
import com.truelife.http.MyHttpEntity
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.*
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import kotlinx.android.synthetic.main.activity_report_problem.*
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class ReportProblemActivity : BaseActivity(), ResponseListener {

    lateinit var mContext: FragmentActivity;

    var mQueryED: EditText? = null
    var mSubmitBTN: Button? = null
    var mImageRecycleView: RecyclerView? = null
    var mClose: ImageButton? = null


    var user: User? = null

    var mPostId: String? = "0"
    var mFromScreenName: String? = ""
    var mReport: String? = "3"
    var mType: String? = "users"
    var mContent: String? = null
    var mReportFor: String? = "setting"
    var mClubId: String? = "0"
    var mReportUserId: String? = "0"
    private val mPostImageFile: ArrayList<File> = arrayListOf()
    private val mCompressedImageFile: ArrayList<File> = arrayListOf()
    private var mFilePath: ArrayList<String> = arrayListOf()
    private lateinit var myMenuRecyclerAdapter: TLFeedPostImageListAdapter
    private var mPermission: Array<String> = arrayOf()


    //


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_problem)

        /* if (android.os.Build.VERSION.SDK_INT > 9) {
             var strict: StrictMode.ThreadPolicy =
                 StrictMode.ThreadPolicy.Builder().permitAll().build();
             StrictMode.setThreadPolicy(strict);
         }*/
        init()
        mPermission = arrayOf(
            TLConstant.CAMERA,
            TLConstant.READ_EXTERNAL_STORAGE,
            TLConstant.WRITE_EXTERNAL_STORAGE
        )
        PermissionChecker().askAllPermissions(mContext!!, mPermission)

        clickListener()
    }

    override fun clickListener() {

        pick_image_lay!!.setOnClickListener {
            if (PermissionChecker().checkAllPermission(mContext!!, mPermission)) {
                FilePickerBuilder.instance.setMaxCount(5)
                    // .setSelectedFiles(filePaths)
                    .setActivityTheme(R.style.FilePickerTheme)
                    .enableVideoPicker(true)
                    .enableCameraSupport(true)
                    .showGifs(false)
                    .showFolderView(true)
                    .enableImagePicker(true)
                    .setCameraPlaceholder(R.drawable.ic_action_camera)
                    //        .withOrientation()
                    .pickPhoto(mContext)
            }
        }

        mSubmitBTN!!.setOnClickListener {

            if (!mQueryED!!.text.isEmpty() || !mFilePath.isEmpty()) {
                submitReport()
            } else {
                AppDialogs.showToastlong(mContext, getString(R.string.label_report_screen_alert))
            }
        }


        mClose!!.setOnClickListener {
            finish()
        }

    }

    fun submitReport() {

        if (validate()) {
            if (mPostImageFile.size > 0) {
                mCompressedImageFile.clear()
                for (i in 0 until mPostImageFile.size) {
                    AppDialogs.showProgressDialog(mContext)
                    val size = Utility.getFileSize(mPostImageFile.get(i))
                    if (size > IMAGE_FILE_SIZE_LIMIT_3MB && ((mPostImageFile.get(i).absoluteFile.toString()
                            .toLowerCase()
                            .contains(".jpg")) || (mPostImageFile.get(i).absoluteFile.toString()
                            .toLowerCase().contains(".png")))
                    ) {
                        /* val outPath: String = (Environment.getExternalStorageDirectory()
                         .toString() + File.separator
                             + APP_DIR
                             + COMPRESSED_IMAGES_DIR)


                     val filePath = Compressor(mContext)
                         .setQuality(75)
                         .setCompressFormat(Bitmap.CompressFormat.WEBP)
                         .setDestinationDirectoryPath(outPath)
                         .compressToFile(mPostImageFile.get(i));*/

                        mCompressedImageFile.add(
                            FileCompression.compressImage(mContext, mPostImageFile.get(i))
                        )


                        //  ImageCompressor().execute(mPostImageFile.get(i).absolutePath)
                    } else {
                        mCompressedImageFile.add(mPostImageFile.get(i))
                    }
                    AppDialogs.hideProgressDialog()

                }

            }
            if (mCompressedImageFile.size == mPostImageFile.size)
                report()
        }
    }

    override fun init() {
        mContext = this
        user = LocalStorageSP.getLoginUser(mContext)
        mQueryED = findViewById(R.id.fragment_feed_post_thoughts_Edt) as EditText
        mSubmitBTN = findViewById(R.id.privacy_post_btn) as Button
        mImageRecycleView = findViewById(R.id.fragment_club_in_report_post_RCY) as RecyclerView
        mClose = findViewById(R.id.close_club_button) as ImageButton


        fun getBundle() {


            if (intent.getStringExtra("FromScreen").isNullOrEmpty()) {
                mFromScreenName = intent.getStringExtra("FromScreen")
                when (mFromScreenName) {
                    "settingList" -> {
                        mPostId = "0"
                        mClubId = "0"
                        mReportUserId = "0"
                        mReportFor = "setting"
                    }
                    "ClubInMore" -> {
                        mPostId = "0"
                        mClubId = intent.getStringExtra("club_id")
                        mReportUserId = "0"
                        mReportFor = "club"
                    }
                    "feed_list" -> {
                        mPostId = intent.getStringExtra("post_id")
                        mClubId = "0"
                        mReportUserId = "0"
                        mReportFor = "feed"
                    }
                    "profile" -> {
                        header_txt!!.text = getResources().getString(R.string.report_user_label)
                        mPostId = "0"
                        mClubId = "0"
                        mReportUserId = intent.getStringExtra("user_id")
                        mReportFor = "profile"
                    }
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
                mPostImageFile.clear()
                mImageRecycleView!!.visibility = View.VISIBLE
                mFilePath =
                    java.util.ArrayList(data!!.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA))

                for (i in mFilePath.indices) {
                    mPostImageFile.add(File(mFilePath.get(i)))
                    Log.e("image$i", mPostImageFile[i].toString())
                }
                setRecylceview()//  myMenuRecyclerAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
        }
    }

    fun setRecylceview() {
        myMenuRecyclerAdapter = TLFeedPostImageListAdapter(mContext, mFilePath)
        val horizontalLayoutManagaer =
            LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        mImageRecycleView!!.setLayoutManager(horizontalLayoutManagaer)
        mImageRecycleView!!.setAdapter(myMenuRecyclerAdapter)
    }


    fun report() {
        this@ReportProblemActivity.runOnUiThread(java.lang.Runnable {
            AppDialogs.showProgressBarCounting(mContext)
        })

        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, getReportAsString())
        val httpclient: HttpClient? = DefaultHttpClient()
        val httppost = HttpPost(result)

        val multipartEntity: MultipartEntityBuilder = MultipartEntityBuilder.create()

        if (mCompressedImageFile.size > 0) {
            var aThumb = 0
            for (i in mCompressedImageFile.indices) {
                val ImagepathURL: String = mCompressedImageFile.get(i).toString()


                multipartEntity.addBinaryBody("media$i", mCompressedImageFile.get(i))
                Log.e("media$i", mCompressedImageFile.get(i).toString())
                if (ImagepathURL != null || ImagepathURL != "null") {
                    if (ImagepathURL.substring(ImagepathURL.lastIndexOf("."))
                            .toLowerCase() != ".jpeg" ||
                        ImagepathURL.substring(ImagepathURL.lastIndexOf("."))
                            .toLowerCase() != ".jpg" ||
                        ImagepathURL.substring(ImagepathURL.lastIndexOf("."))
                            .toLowerCase() != ".png"
                    ) {
                        val uri =
                            Uri.fromFile(File(mCompressedImageFile.get(i).toString()))
                        val thumb = ThumbnailUtils.createVideoThumbnail(
                            uri.path,
                            MediaStore.Video.Thumbnails.MINI_KIND
                        )
                        val filesDir = myContext.filesDir
                        val imageFile =
                            File(filesDir, aThumb.toString() + "Test" + ".jpeg")
                        var os: OutputStream
                        try {
                            if (thumb != null) {
                                os = FileOutputStream(imageFile)
                                thumb.compress(Bitmap.CompressFormat.JPEG, 100, os)
                                os.flush()
                                os.close()
                            }
                        } catch (e: java.lang.Exception) {
                            Log.e(javaClass.simpleName, "Error writing bitmap", e)
                        }
                        if (thumb != null) {
                            multipartEntity.addBinaryBody("mythumb$i", imageFile)
                            Log.e(
                                "mythumb",
                                multipartEntity.addBinaryBody(
                                    "mythumb$aThumb",
                                    imageFile
                                ).toString()
                            )
                            aThumb = aThumb + 1
                        }
                    }
                }
            }
        }

        // httppost.setEntity(multipartEntity.build());
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
                            AppDialogs.customSuccessAction(
                                this,
                                null,
                                ss,
                                null,
                                object : AppDialogs.ConfirmListener {
                                    override fun yes() {
                                        finish()
                                    }

                                },
                                false
                            )


                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {

                }
            })
        }).start()

    }


    fun getReportAsString(): String {
        var aCaseStr = " "
        try {


            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", user!!.mUserId)
            jsonParam1.put("post_id", mPostId)
            jsonParam1.put("report", mReport)
            jsonParam1.put("type", mType)
            jsonParam1.put("content", mContent)
            jsonParam1.put("report_for", mReportFor)
            jsonParam1.put("club_id", mClubId)
            jsonParam1.put("report_user_id", mReportUserId)
            val jsonParam = JSONObject()
            jsonParam.put("PostReport", jsonParam1)
            Log.e("PostReport", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return aCaseStr
    }

    override fun onResponse(r: Response?) {
        AppDialogs.hideProgressDialog()
        if (r != null) {
            if (r.requestType!! == AppServices.API.report.hashCode()) {
                if (r.response!!.isSuccess) {
                }
            }
        }

    }

    fun validate(): Boolean {
        mContent = mQueryED!!.text.toString()
        if (mContent.isNullOrEmpty()) {
            AppDialogs.showToastDialog(mContext, "Enter message")
            return false
        }
        return true;
    }


    /* @SuppressLint("StaticFieldLeak")
     inner class ImageCompressor :
         AsyncTask<String?, Void?, Boolean?>() {
         protected override fun onPreExecute() {
             super.onPreExecute()

             AppDialogs.showProgressDialog(mContext)

             Log.d(
                 "Video compression",
                 "Start video compression"
             )
         }

         override fun doInBackground(vararg params: String?): Boolean? {

             val outPath: String = (Environment.getExternalStorageDirectory()
                 .toString() + File.separator
                     + APP_DIR
                     + COMPRESSED_IMAGES_DIR)
             // Log.d("Out file Name", outPath)

             val filePath = Compressor(mContext)
                 .setQuality(75)
                 .setCompressFormat(Bitmap.CompressFormat.WEBP)
                 .setDestinationDirectoryPath(outPath)
                 .compressToFile(File(params[0]!!));
             //  val filePath: String = SiliCompressor.with(mContext).compress(params[0], File(outPath))
             mCompressedImageFile.add(filePath)
             return true
             //return MediaController.getInstance().convertVideo(params[0], outPath)
         }

         override fun onPostExecute(compressed: Boolean?) {
             super.onPostExecute(compressed)
             AppDialogs.hideProgressDialog()
             if (compressed!!) {
                 if (mCompressedImageFile.size == mPostImageFile.size)
                     report()
             }
         }
     }
 */

}

