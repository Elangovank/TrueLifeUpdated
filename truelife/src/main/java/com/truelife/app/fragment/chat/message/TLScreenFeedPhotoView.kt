package com.truelife.app.fragment.chat.message

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rd.PageIndicatorView
import com.truelife.R
import com.truelife.app.model.Chat.FeedInfo
import com.truelife.app.touchimageview.TLExtendedViewPager
import com.truelife.app.touchimageview.TLTouchImageView
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager
import com.truelife.util.TLProgressDialog
import com.truelife.util.Utility
import com.truelife.util.Utility.loadImage
import java.util.*

class TLScreenFeedPhotoView : BaseFragment() {
    private var myViewPager: TLExtendedViewPager? = null
    private var myContext: FragmentActivity? = null
    private var myFeedImageArray: ArrayList<String>? = null
    private var myMediaArray: ArrayList<String>? = null
    private var myFeedMediaList: FeedInfo? = null
    private var myPagerPosition = 0
    private val myMoreIMGBTN: ImageButton? = null
    private val myAsynTask: AsynTask? = null
    private var myCategoryNameStr: String? = ""
    private val myTypeSTR = ""
    // pager Indicator
//    private var myPageIndicator: PageIndicatorView? = null
    private val IMAGE_SHARE = 1
    private val IMAGE_SAVE = 2
    private var myView: View? = null
    private var myPageTotalShow: TextView? = null
    private val downX = 0f
    private val downY = 0f
    private val upX = 0f
    private val upY = 0f
    private val min_distance = 100f
    private var myFragmentManager: TLFragmentManager? = null
    private var myRootLAY: LinearLayout? = null
    private var myCloseBtn: ImageButton? = null
    private var myMediaTypeStr: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.screen_feed_photos_view, container, false)
        classAndWidgetInitialize(myView)
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        return myView
    }

    private fun classAndWidgetInitialize(aView: View?) {
        try {
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            myContext = activity
            myViewPager = aView!!.findViewById(R.id.screen_gallery_view_VWPGR)
            myFragmentManager = TLFragmentManager(myContext!!)
            // Initialise pager Indicator
//            myPageIndicator = aView.findViewById(R.id.screen_gallery_view_VWIND)
            myPageTotalShow = aView.findViewById(R.id.page_count)
            myCloseBtn = aView.findViewById(R.id.inflate_alert_close_button)
            myRootLAY = aView.findViewById(R.id.screen_content_load_LAY)
            /*myDragLAY = (SwipeBackLayout) aView.findViewById(R.id.swipeBackLayout_photo_view);
            myDragLAY.setDragEdge(SwipeBackLayout.DragEdge.TOP);*/
/* myMoreIMGBTN = (ImageButton) myContext.findViewById(R.id.layout_title_bar_wit_back_btn_IMGBTN_more);
            myMoreIMGBTN.setVisibility(View.VISIBLE);*/galleryData
            clickListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun clickListener() {
        myCloseBtn!!.setOnClickListener { myFragmentManager!!.onBackPress() }
    }//  loadInfo();

    // Getting position of image tap in gallery by user
    private val galleryData: Unit
        private get() { // Getting position of image tap in gallery by user
            val aBundle = arguments
            if (aBundle != null) {
                myPagerPosition = aBundle.getInt("Position")
                myCategoryNameStr = aBundle.getString("feed_images")
                myMediaTypeStr = aBundle.getString("feed_images_Type")
                val myMedia = aBundle.getString("feed_media")
                myFeedMediaList = Gson().fromJson<FeedInfo>(
                    myMedia,
                    object : TypeToken<FeedInfo?>() {}.type
                )
                myFeedImageArray = Gson().fromJson<ArrayList<String>>(
                    myCategoryNameStr,
                    object : TypeToken<ArrayList<String?>?>() {}.type
                )
                myMediaArray = Gson().fromJson<ArrayList<String>>(
                    myMediaTypeStr,
                    object : TypeToken<ArrayList<String?>?>() {}.type
                )
                //  loadInfo();
                if (myFeedImageArray!!.size > 0 && myMediaArray!!.size > 0) {
                    loadValues(myFeedImageArray, myMediaArray)
                }
            }
            myPageTotalShow!!.text = (myPagerPosition + 1).toString() + " of " + myFeedImageArray!!.size
        }

    override fun onBackPressed() {}
    override fun init(view: View) {}
    override fun initBundle() {}
    override fun onResumeFragment() {}
    private inner class ImagePagerAdapter(
        private val myStrings: ArrayList<String>?,
        myMediaArray: ArrayList<String>?,
        aContext: FragmentActivity?
    ) : PagerAdapter() {
        private val aInflater =
            activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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
            //  Glide.with(myContext).load( getGallerryInfo(position)).into(imageView);
            if (myMediaArray!![position] == "image") {
                imageView.isEnabled = true
                aVideoPLayBtn.visibility = View.GONE
            } else {
                imageView.isEnabled = false
                aVideoPLayBtn.visibility = View.VISIBLE
            }
            loadImage(getGallerryInfo(position), imageView,R.drawable.image_placeholder)
            aVideoPLayBtn.setOnClickListener {
                if (myMediaArray!![position] == "video") {
                    val aBundle = Bundle()
                    if (myFeedMediaList != null) aBundle.putString(
                        "Video_url",
                        myFeedMediaList!!.myFeedImageList[position]
                    ) else aBundle.putString("Video_url", getGallerryInfo(position))
                    myFragmentManager!!.addContent(
                        TLFeedDetailsVideo(),
                        TLFeedDetailsVideo.TAG,
                        aBundle
                    )
                }
            }
            //  }
// RTCHelper.loadImage(getActivity(), aString.getContentUrl(),
// imageView, LOAD_UNI_IMG_LOADER);
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
            myProgressDialog = TLProgressDialog(activity)
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
        myViewPager!!.adapter = ImagePagerAdapter(aFeedImageArray, myMediaArray, activity)
//        myPageIndicator!!.setViewPager(myViewPager)
        myViewPager!!.currentItem = myPagerPosition
//        myPageIndicator!!.selection = myPagerPosition
        myViewPager!!.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                myPageTotalShow!!.text = (position + 1).toString() + " of " + myFeedImageArray!!.size
            }

            override fun onPageSelected(position: Int) {
                myPagerPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    companion object {
        val TAG = TLScreenFeedPhotoView::class.java.simpleName
    }
}