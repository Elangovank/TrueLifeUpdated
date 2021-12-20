package com.truelife.app.fragment.chat.profile

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.google.gson.Gson
import com.truelife.R
import com.truelife.app.fragment.chat.message.TLFeedDetailsVideo
import com.truelife.app.fragment.chat.message.TLScreenFeedPhotoView
import com.truelife.app.model.ChatProfile.Images
import com.truelife.base.TLFragmentManager
import com.truelife.util.Utility.loadImage
import java.util.*

/**
 * Created by mahalingam on 01-09-2017.
 * TLClubByAdapter
 */
class TLMsgTotalProfileImageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private var myContext: FragmentActivity? = null
    private var myClubInfoList: ArrayList<Images>? = null
    private var myUserProfileImageList: ArrayList<String>? = null
    private var myFragmentManager: TLFragmentManager? = null
    var myImageCreatedList =
        ArrayList<String>()
    private val myImageListonly =
        ArrayList<String>()
    private val myVideoListOnly =
        ArrayList<String>()
    val aMeadiaType = ArrayList<String>()
    private lateinit var myCheckedFriend: BooleanArray
    private var myScreenType = ""

    constructor(
        context: FragmentActivity?,
        aclubInfoList: ArrayList<Images>?,
        aImageCreatedList: ArrayList<String>,
        aScreenType: String
    ) {
        try {
            myContext = context
            myClubInfoList = aclubInfoList
            myImageCreatedList = aImageCreatedList
            myFragmentManager = TLFragmentManager(myContext!!)
            myScreenType = aScreenType
            //  myImageLoader.init(ImageLoaderConfiguration.createDefault(myContext));
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    constructor(
        context: FragmentActivity?,
        imageInfos: ArrayList<String>?,
        user_profile: String
    ) {
        try {
            myContext = context
            myUserProfileImageList = imageInfos
            myFragmentManager = TLFragmentManager(context!!)
            myScreenType = user_profile
            //  myImageLoader.init(ImageLoaderConfiguration.createDefault(myContext));
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder { // Context context = holder.itemView.getContext();
        var aItemView: View? = null
        aItemView = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_inflate_user_profile_msg_profile_image_list_item,
            parent,
            false
        )
        return ItemViewHolder(
            aItemView
        )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        try {
            val aContent =
                holder as ItemViewHolder
            //  holder.myclubName.setText(aClubObject.getFirst_name());
            aContent.myclubName.visibility = View.GONE
            // myOption = TLHelper.displayClubOption(myContext);
//   myImageLoader.displayImage(aClubObject.getClub_image(), holder.myCropImage, myOption);
            if (myScreenType == "User_Profile") {
                val aClubObject = myUserProfileImageList!![position]
                if (!aClubObject.isEmpty()) { // drawableManager.fetchDrawableOnThread( aClubObject.getUrl(),aContent.myCropImage );
                    loadImage(aClubObject, aContent.myCropImage)
                } else {
                    aContent.myCropImage.setImageResource(R.drawable.new_feed_image_place_holder)
                }
                aContent.myCropImage.setOnClickListener {
                    myImageListonly.clear()
                    aMeadiaType.clear()
                    for (i in myUserProfileImageList!!.indices) {
                        aMeadiaType.add("image")
                        myImageListonly.add(myUserProfileImageList!![i])
                    }
                    val aBundle = Bundle()
                    val gson = Gson()
                    val jsonMediaType = gson.toJson(aMeadiaType)
                    val json = gson.toJson(myImageListonly)
                    aBundle.putString("feed_images", json)
                    aBundle.putString("feed_images_Type", jsonMediaType)
                    aBundle.putInt("Position", position)
                    myFragmentManager!!.addContent(
                        TLScreenFeedPhotoView(),
                        TLScreenFeedPhotoView.TAG,
                        aBundle
                    )
                }
            } else {
                val aClubObject = myClubInfoList!![position]

                if (aClubObject.file_type == "image") {
                    aContent.myVideoIMG.visibility = View.GONE
                    aContent.myCropImage.visibility = View.VISIBLE
                    loadImage(aClubObject.url, aContent.myCropImage)
                } else {
                    aContent.myVideoIMG.visibility = View.VISIBLE
                    if (aClubObject.video_thumbnail.isNotEmpty()) {
                        loadImage(aClubObject.video_thumbnail, aContent.myCropImage)
                    } else {
                        aContent.myCropImage.setImageResource(R.drawable.new_feed_image_place_holder)
                    }
                }

                aContent.myCropImage.setOnClickListener {
                    myImageListonly.clear()
                    aMeadiaType.clear()
                    for (i in myClubInfoList!!.indices) {
                        aMeadiaType.add(myClubInfoList!![i].file_type)
                        if (myClubInfoList!![i].file_type == "image") {
                            myImageListonly.add(java.lang.String.valueOf(myClubInfoList!![i].url))
                        } else {
                            myImageListonly.add(java.lang.String.valueOf(myClubInfoList!![i].video_thumbnail))
                            myVideoListOnly.add(java.lang.String.valueOf(myClubInfoList!![i].url))
                        }
                    }

                    if (aClubObject.file_type == "image") {
                        val aBundle = Bundle()
                        val gson = Gson()
                        val jsonMediaType = gson.toJson(aMeadiaType)
                        val json = gson.toJson(myImageListonly)
                        aBundle.putString("feed_images", json)
                        aBundle.putString("feed_images_Type", jsonMediaType)
                        aBundle.putInt("Position", position)
                        myFragmentManager!!.addContent(
                            TLScreenFeedPhotoView(),
                            TLScreenFeedPhotoView.TAG,
                            aBundle
                        )
                        // myImageListonly.clear();
                    } else {
                        val aBundle = Bundle()
                        aBundle.putString("Video_url", aClubObject.url)
                        myFragmentManager!!.addContent(
                            TLFeedDetailsVideo(),
                            TLFeedDetailsVideo.TAG,
                            aBundle
                        )
                    }
                }
            }
            // setListener(holder, aClubObject);
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateAdapter(
        myContext: FragmentActivity?,
        myNewsList: ArrayList<Images>?
    ) {
        try {
            this.myContext = myContext
            myClubInfoList = myNewsList
            notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateAdpter(aClubforList: ArrayList<Images>) {
        try {
            myClubInfoList = aClubforList
            myCheckedFriend = BooleanArray(aClubforList.size)
            for (i in myCheckedFriend.indices) {
                myCheckedFriend[i] = false
            }
            // notifyDataSetChanged();
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return if (myScreenType == "User_Profile") myUserProfileImageList!!.size else myClubInfoList!!.size
    }

    /*
         * ItemViewHolder
         * */
    inner class ItemViewHolder internal constructor(view: View) :
        RecyclerView.ViewHolder(view) {
        var myclubName: TextView
        var myCropImage: ImageView
        var myVideoIMG: ImageView
        var myClubLayout: RelativeLayout

        init {
            myclubName = view.findViewById<View>(R.id.layout_club_name) as TextView
            myCropImage =
                view.findViewById<View>(R.id.layout_club_image) as ImageView
            myVideoIMG =
                view.findViewById<View>(R.id.layout_inflate_feed_details_video_play_IMG) as ImageView
            myClubLayout =
                view.findViewById<View>(R.id.layout_club_linear_layout) as RelativeLayout
        }
    }

    /**
     * interface onclick
     */
    interface ClickListener {
        fun onClick(view: View?, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    /**
     * RecyclerTouchListener
     */
    class RecyclerTouchListener(
        context: Context?,
        recyclerView: RecyclerView,
        private val clickListener: ClickListener?
    ) : OnItemTouchListener {
        // GestureDetector handling singleClick or longClick
        private val gestureDetector: GestureDetector

        override fun onInterceptTouchEvent(
            rv: RecyclerView,
            e: MotionEvent
        ): Boolean {
            val child = rv.findChildViewUnder(e.x, e.y)
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child))
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

        init {
            gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    val child =
                        recyclerView.findChildViewUnder(e.x, e.y)
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child))
                    }
                }
            })
        }
    }

    companion object {
        var TAG = TLMsgTotalProfileImageAdapter::class.java.simpleName
    }
}