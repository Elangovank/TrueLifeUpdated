package com.truelife.app.fragment.chat.profile

import android.content.Context
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.truelife.R
import com.truelife.app.model.ChatProfile.Images
import com.truelife.base.TLFragmentManager
import com.truelife.util.Utility.loadImage
import java.util.*

/**
 * Created by mahalingam on 01-09-2017.
 * TLClubByAdapter
 */
class TLMsgProfileImageAdapter(
    context: FragmentActivity?,
    aclubInfoList: ArrayList<Images>?
) : RecyclerView.Adapter<TLMsgProfileImageAdapter.ItemViewHolder>() {
    private var myContext: FragmentActivity? = null
    private var myClubInfoList: ArrayList<Images>? = null
    private var myFragmentManager: TLFragmentManager? = null
    private lateinit var myCheckedFriend: BooleanArray
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder { // Context context = holder.itemView.getContext();
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_inflate_clubbyyou_member_item, parent, false)
        return ItemViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int
    ) {
        try {
            val context = holder.itemView.context
            val aClubObject = myClubInfoList!![position]
            //  holder.myclubName.setText(aClubObject.getFirst_name());
            holder.myclubName.visibility = View.GONE
            // myOption = TLHelper.displayClubOption(myContext);
//   myImageLoader.displayImage(aClubObject.getClub_image(), holder.myCropImage, myOption);
            if (aClubObject.url != "") {
                loadImage(aClubObject.url, holder.myCropImage)
            } else {
                holder.myCropImage.setImageResource(R.drawable.msg_file_placeholder)
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
        return myClubInfoList!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    /*
     * ItemViewHolder
     * */
    inner class ItemViewHolder internal constructor(view: View) :
        RecyclerView.ViewHolder(view) {
        var myclubName: TextView
        var myCropImage: ImageView
        var myClubLayout: CardView

        init {
            myclubName = view.findViewById<View>(R.id.layout_club_name) as TextView
            myCropImage =
                view.findViewById<View>(R.id.layout_club_image) as ImageView
            myClubLayout =
                view.findViewById<View>(R.id.layout_club_linear_layout) as CardView
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
        var TAG = TLMsgProfileImageAdapter::class.java.simpleName
    }

    init {
        try {
            myContext = context
            myClubInfoList = aclubInfoList
            myFragmentManager = TLFragmentManager(myContext!!)
            //  myImageLoader.init(ImageLoaderConfiguration.createDefault(myContext));
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}