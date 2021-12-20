package com.truelife.app.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.truelife.BuildConfig
import com.truelife.R
import com.truelife.app.activity.feedpost.adapter.TLFeedPostImageListAdapter
import com.truelife.http.MyHttpEntity
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.FileCompression
import com.truelife.util.Helper
import com.truelife.util.Utility
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
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


class ReportDailogFragment : DialogFragment() {

    var myPostId = ""
    var myClubId = ""
    var myReportUserId = ""
    var myReportFor = ""
    var myFromScreenName = ""
    private lateinit var myMenuRecyclerAdapter: TLFeedPostImageListAdapter
    var mQueryED: EditText? = null
    var mImageRecycleView: RecyclerView? = null


    private var myCloseImgBtn: ImageButton? = null
    private val list: List<String> = ArrayList()
    private var mySaveBtn: Button? = null
    private var myPostImageTXT: TextView? = null
    private var myRecyclerView: RecyclerView? = null
    private var myPath = ArrayList<String>()
    private val filePaths: ArrayList<String>? = ArrayList()
    private var myPostImageFile = ArrayList<File>()
    private var myPostThumbFile = ArrayList<File>()
    private var myRootLayout: RelativeLayout? = null
    private var myPostContentEdt: EditText? = null
    private val handler = Handler()
    private var myProgressLAY: RelativeLayout? = null



    private lateinit var progressBar: ProgressBar
    private lateinit var progressTV: TextView

    // From Club More
    private lateinit var mHeader: LinearLayout
    private lateinit var mMainLayout: LinearLayout
    private lateinit var mOtherLayout: RelativeLayout
    private lateinit var mClose: ImageView
    private lateinit var mImageButton: TextView

    private var myHeader: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_report_dailog, container, false)

        myCloseImgBtn =
            view.findViewById<View>(R.id.close_club_button) as ImageButton
        myPostImageTXT =
            view.findViewById<View>(R.id.fragment_club_in_post_photo_TXT) as TextView
        myRecyclerView =
            view.findViewById<View>(R.id.fragment_club_in_report_post_RCY) as RecyclerView
        mySaveBtn =
            view.findViewById<View>(R.id.send_feed_report_btn) as Button
        myPostContentEdt =
            view.findViewById<View>(R.id.fragment_feed_post_thoughts_Edt) as EditText
        myRootLayout =
            view.findViewById<View>(R.id.fragment_crate_club_root_layout) as RelativeLayout
        myProgressLAY =
            view.findViewById<View>(R.id.progress_lay) as RelativeLayout
        myHeader =
            view.findViewById<View>(R.id.fragment_feed_report_header) as TextView

        mImageRecycleView = view.findViewById(R.id.fragment_club_in_report_post_RCY) as RecyclerView

        mQueryED = view.findViewById(R.id.fragment_feed_post_thoughts_Edt) as EditText

        progressBar = view.findViewById<ProgressBar>(R.id.circularProgressbar)
        progressTV = view.findViewById<TextView>(R.id.tv)

        mHeader = view.findViewById(R.id.report)
        mMainLayout = view.findViewById(R.id.main_layout)
        mOtherLayout = view.findViewById(R.id.other)
        mClose = view.findViewById(R.id.close_button)
        mImageButton = view.findViewById(R.id.fragment_club_in_post_photo)

        if (getArguments() != null) {
            val mArgs = arguments

            myFromScreenName = mArgs!!.getString("FromScreen")!!
            when (myFromScreenName) {
                "settingList" -> {
                    myPostId = "0"
                    myClubId = "0"
                    myReportUserId = "0"
                    myReportFor = "setting"
                }
                "ClubInMore" -> {
                    myPostId = "0"
                    myClubId = mArgs.getString("club_id")!!
                    myReportUserId = "0"
                    myReportFor = "club"

                    mOtherLayout.visibility = View.GONE
                    myPostImageTXT!!.visibility = View.GONE
                    mHeader.visibility = View.VISIBLE
                    mImageButton.visibility = View.VISIBLE
                    mMainLayout.setBackgroundColor(Utility.getColor(this.activity!!, R.color.black))
                    myPostImageTXT!!.setTextColor(Utility.getColor(this.activity!!, R.color.white))

                }
                "feed_list" -> {
                    myPostId = mArgs.getString("post_id")!!
                    myClubId = "0"
                    myReportUserId = "0"
                    myReportFor = "feed"
                }
                "profile" -> {
                    myHeader!!.text = getResources().getString(R.string.report_user_label)
                    myPostId = "0"
                    myClubId = "0"
                    myReportUserId = mArgs.getString("user_id")!!
                    myReportFor = "profile"
                }
            }
        }
        setListeners()

        return view
    }


    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.getWindow()!!.setLayout(width, height)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
            try {
                myPath = ArrayList(
                    data!!.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)
                )
                myRecyclerView!!.setVisibility(View.VISIBLE)
                try {
                    myPostImageFile =
                        ArrayList() // store image url for send API
                    myPostThumbFile = ArrayList()
                    for (i in myPath.indices) {
                        myPostImageFile.add( FileCompression.compressImage(activity!! ,File(myPath[i])))
                        myPostImageFile.add(File(myPath[i]))
                        Log.e("image$i", myPostImageFile[i].toString())
                    }
                    setRecylceview()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setRecylceview() {
        myMenuRecyclerAdapter = TLFeedPostImageListAdapter(this.activity!!, myPath)
        val horizontalLayoutManagaer =
            LinearLayoutManager(this.activity, LinearLayoutManager.HORIZONTAL, false)
        mImageRecycleView!!.setLayoutManager(horizontalLayoutManagaer)
        mImageRecycleView!!.setAdapter(myMenuRecyclerAdapter)
    }


    private fun setListeners() {
        mySaveBtn!!.setOnClickListener {
            if (!mQueryED!!.text.isEmpty() || !myPath.isEmpty()) {
                submitReport()
            } else {
                AppDialogs.showToastlong(
                    this.activity!!.applicationContext,
                    getString(R.string.label_report_screen_alert)
                )
            }
        }
        myCloseImgBtn!!.setOnClickListener { dismiss() }
        mClose.setOnClickListener { dismiss() }

        myPostImageTXT!!.setOnClickListener {
            initPhoto()
        }

        mImageButton.setOnClickListener {
            initPhoto()
        }
    }

    private fun initPhoto() {
        FilePickerBuilder.instance.setMaxCount(5)
            .setSelectedFiles(filePaths!!)
            .setActivityTheme(R.style.FilePickerTheme)
            .enableVideoPicker(true)
            .enableCameraSupport(true)
            .showGifs(false)
            .showFolderView(true)
            .enableImagePicker(true)
            .setCameraPlaceholder(R.drawable.ic_action_camera)
            .pickPhoto(this@ReportDailogFragment)
    }


    fun submitReport() {

        if (validate()) {
            report()
        }
    }

    var mContent: String = ""
    fun validate(): Boolean {
        mContent = mQueryED!!.text.toString()
        if (mContent.isNullOrEmpty()) {
            AppDialogs.showToastDialog(this.activity!!.applicationContext, "Enter message")
            return false
        }
        return true;
    }


    @SuppressLint("SetTextI18n")
    fun report() {

        this.activity!!.runOnUiThread(java.lang.Runnable {
            myProgressLAY!!.visibility = View.VISIBLE
        })


        val result = Helper.GenerateEncrptedUrl(BuildConfig.API_URL, getReportAsString())
        var httpclient: HttpClient? = DefaultHttpClient()
        val httppost = HttpPost(result)


        val multipartEntity: MultipartEntityBuilder = MultipartEntityBuilder.create()

        if (myPostImageFile.size > 0) {
            var aThumb = 0
            for (i in myPostImageFile.indices) {
                val ImagepathURL: String = myPostImageFile.get(i).toString()
                multipartEntity.addBinaryBody("media$i", myPostImageFile.get(i))
                Log.e("media$i", myPostImageFile.get(i).toString())
                if (ImagepathURL != null || ImagepathURL != "null") {
                    if (ImagepathURL.substring(ImagepathURL.lastIndexOf(".")).toLowerCase() != ".jpeg" ||
                        ImagepathURL.substring(ImagepathURL.lastIndexOf(".")).toLowerCase() != ".jpg" ||
                        ImagepathURL.substring(ImagepathURL.lastIndexOf(".")).toLowerCase() != ".png"
                    ) {
                        val uri =
                            Uri.fromFile(File(myPostImageFile.get(i).toString()))
                        val thumb = ThumbnailUtils.createVideoThumbnail(
                            uri.path,
                            MediaStore.Video.Thumbnails.MINI_KIND
                        )
                        val filesDir = this.activity!!.filesDir
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

        httppost.entity = MyHttpEntity(multipartEntity.build(), MyHttpEntity.ProgressListener {
            this.activity!!.runOnUiThread {
                progressTV.setText(it.toInt().toString() + "%")
                progressBar.setProgress(it.toInt())
            }
            Log.e("Progress --> ", it.toString())
        })
        Thread({
            //Do some Network Request
            val httpResponse = httpclient!!.execute(httppost)
            val httpEntity = httpResponse.entity

            var aJsonResponse = EntityUtils.toString(httpEntity)
            Log.e("Report Response --> ", aJsonResponse)
            this.activity!!.runOnUiThread({


                if (aJsonResponse != "") { //  TLHelper.hideDialog(myProgressDialog);
                    try {
                        var aJsonObject = JSONObject(aJsonResponse)
                        AppDialogs.hideProgressBarCounting()
                        if (aJsonObject.has("response")) {
                            var response_msg: JSONObject = aJsonObject.getJSONObject("response")
                            var ss = response_msg.getString("response_msg")
                            var responsecode = response_msg.getString("response_code")

                            if (responsecode.equals("1")) {
                                Toast.makeText(
                                    this.activity,
                                    "Reported successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismiss()
                            } else {
                                Toast.makeText(
                                    this.activity,
                                    ss,
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismiss()
                            }


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

            val user = LocalStorageSP.getLoginUser(this.activity)
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", user!!.mUserId)
            jsonParam1.put("post_id", myPostId)
            jsonParam1.put("report", "3")
            jsonParam1.put("type", "users")
            jsonParam1.put("content", mContent)
            jsonParam1.put("report_for", myReportFor)
            jsonParam1.put("club_id", myClubId)
            jsonParam1.put("report_user_id", myReportUserId)
            val jsonParam = JSONObject()
            jsonParam.put("PostReport", jsonParam1)
            Log.e("PostReport", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return aCaseStr
    }
}
