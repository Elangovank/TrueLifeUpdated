package com.truelife.app.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.rd.PageIndicatorView
import com.truelife.R
import com.truelife.app.VideoPreviewActivity
import com.truelife.app.model.Chat
import com.truelife.app.model.PublicFeedModel
import com.truelife.app.touchimageview.TLExtendedViewPager
import com.truelife.app.touchimageview.TLTouchImageView
import com.truelife.base.BaseActivity
import com.truelife.util.TLProgressDialog
import com.truelife.util.Utility.loadImage
import kotlinx.android.synthetic.main.activity_image_preview.*


class ImagePreview : BaseActivity() {
    private var myViewPager: TLExtendedViewPager? = null
    private var myFeedImageArray: ArrayList<String>? = ArrayList<String>()
    private var myMediaArray: ArrayList<String>? = ArrayList<String>()
    private var myFeedMediaList: Chat.FeedInfo? = null
    private var myPagerPosition = 0
    private val myMoreIMGBTN: ImageButton? = null
    private val myAsynTask: AsynTask? = null
    lateinit var mMedia: PublicFeedModel.FeedList
    private var myCategoryNameStr: String? = ""
    private val myTypeSTR = ""
    // pager Indicator
    private var myPageIndicator: PageIndicatorView? = null
    private var myView: View? = null
    private var myPageTotalShow: TextView? = null
    private var myRootLAY: LinearLayout? = null
    private var myCloseBtn: ImageButton? = null
    private var myMediaTypeStr: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)
        classAndWidgetInitialize()
    }

    override fun clickListener() {
        inflate_alert_close_button.setOnClickListener {
            finish()
        }
    }

    override fun init() {
    }

    private fun classAndWidgetInitialize() {
        try {
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            myViewPager = findViewById(R.id.screen_gallery_view_VWPGR)

            // Initialise pager Indicator
            myPageIndicator = findViewById(R.id.screen_gallery_view_VWIND)
            myPageTotalShow = findViewById(R.id.page_count)
            myCloseBtn = findViewById(R.id.inflate_alert_close_button)
            myRootLAY = findViewById(R.id.screen_main_LAY)
            galleryData

            clickListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // Getting position of image tap in gallery by user
    private val galleryData: Unit
        get() { // Getting position of image tap in gallery by user
            val i = intent
            mMedia =
                i.getSerializableExtra("media") as PublicFeedModel.FeedList
            myPagerPosition = i.getIntExtra("focus", 0)

            for (i in mMedia.media!!.indices) {
                myFeedImageArray!!.add(mMedia.media!![i].thumb!!)
                myMediaArray!!.add(mMedia.media!![i].mediaType!!)
            }

            //  loadInfo();
            if (myFeedImageArray!!.size > 0 && myMediaArray!!.size > 0) {
                loadValues(myFeedImageArray, myMediaArray)
            }

        }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


    private inner class ImagePagerAdapter(
        private val myStrings: ArrayList<String>?
    ) : PagerAdapter() {
        private val aInflater =
            myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getItemPosition(`object`: Any): Int {
            return super.getItemPosition(`object`)
        }

        override fun getCount(): Int {
            return myStrings!!.size
        }

        private fun getGallerryInfo(position: Int): String {
            return myStrings!![position]
        }

        override fun instantiateItem(
            view: ViewGroup,
            position: Int
        ): View { // Inflator layout and its belongings
            val aImageLayout =
                aInflater.inflate(R.layout.layout_inflate_gallery_image_pager, view, false)
            val imageView: TLTouchImageView = aImageLayout.findViewById(R.id.imageViewTouch)
            val aProgressBar =
                aImageLayout.findViewById<ProgressBar>(R.id.layout_inflate_gallery_image_pager_PG)
            val aVideoPLayBtn =
                aImageLayout.findViewById<ImageView>(R.id.video_play_IMG_one)
            imageView.visibility = View.VISIBLE
            Glide.with(myContext).load(getGallerryInfo(position)).into(imageView);
            if (myMediaArray!![position] == "image") {
                imageView.isEnabled = true
                aVideoPLayBtn.visibility = View.GONE
            } else {
                imageView.isEnabled = false
                aVideoPLayBtn.visibility = View.VISIBLE
            }
            loadImage(getGallerryInfo(position), imageView)
            aVideoPLayBtn.setOnClickListener {

                val intent =
                    Intent(this@ImagePreview, VideoPreviewActivity::class.java)
                intent.putExtra("url", mMedia.media!![position].original)
                startActivity(intent)
            }
            (view as ViewPager).addView(aImageLayout, 0)
            return aImageLayout
        }

        override fun destroyItem(
            container: ViewGroup,
            position: Int,
            `object`: Any
        ) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(
            view: View,
            `object`: Any
        ): Boolean {
            return view === `object`
        }

    }

    private inner class AsynTask :
        AsyncTask<Void?, Void?, Void?>() {
        // Declaration for List and progress dialog
        private var myProgressDialog: TLProgressDialog? = null

        override fun onPreExecute() {
            super.onPreExecute()
            // Showing progress when getting data
            myProgressDialog = TLProgressDialog(myContext)
            // myProgressDialog.setMessage(PLEASE_WAIT);
            myProgressDialog!!.setCanceledOnTouchOutside(false)
            myProgressDialog!!.setOnCancelListener { myAsynTask!!.cancel(true) }
            myProgressDialog!!.show()
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            if (myProgressDialog!!.isShowing) myProgressDialog!!.dismiss()
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            return null
        }
    }

    private fun loadValues(
        aFeedImageArray: ArrayList<String>?,
        myMediaArray: ArrayList<String>?
    ) {
        myFeedImageArray = aFeedImageArray
        myViewPager!!.adapter = ImagePagerAdapter(aFeedImageArray)
        myPageIndicator!!.setViewPager(myViewPager)
        myViewPager!!.currentItem = myPagerPosition
        myPageIndicator!!.selection = myPagerPosition
        myViewPager!!.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                myPageTotalShow!!.text =
                    (position + 1).toString() + " of " + myFeedImageArray!!.size
            }

            override fun onPageSelected(position: Int) {
                myPagerPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

    }

    override fun onDestroy() {
        super.onDestroy()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    companion object {
        val TAG = ImagePreview::class.java.simpleName
    }
}