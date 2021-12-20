package com.truelife.app.fragment.chat.profile

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.truelife.R
import com.truelife.app.model.ChatProfile.Documents
import com.truelife.app.viewer.TLDocFileViewer
import com.truelife.base.TLFragmentManager
import com.truelife.util.TLDownloadPdf
import java.util.*

/**
 * Created by mahalingam on 01-09-2017.
 * TLClubByAdapter
 */
class TLMsgProfileIDocuAdapter(
    context: FragmentActivity?,
    aclubInfoList: ArrayList<Documents>?
) : RecyclerView.Adapter<TLMsgProfileIDocuAdapter.ItemViewHolder>() {
    private var myContext: FragmentActivity? = null
    private var myClubInfoList: ArrayList<Documents>? = null
    private var myFragmentManager: TLFragmentManager? = null
    private var myDownloadPDF: TLDownloadPdf? = null
    private lateinit var myCheckedFriend: BooleanArray
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder { // Context context = holder.itemView.getContext();
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_single_card, parent, false)
        return ItemViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int
    ) {
        try {
            val aClubObject = myClubInfoList!![position]
            val filename = aClubObject.url.substring(aClubObject.url.lastIndexOf("/") + 1)
            holder.myclubName.text = filename

            holder.myCropImage.setImageResource(R.drawable.msg_file_placeholder)

            holder.myCropImage.setOnClickListener {
                try {
                    val aBundle = Bundle()
                    aBundle.putString("FILE_PATH", aClubObject.url)
                    myFragmentManager!!.addContent(
                        TLDocFileViewer(),
                        TLDocFileViewer.TAG,
                        aBundle
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // setListener(holder, aClubObject);
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateAdapter(
        myContext: FragmentActivity?,
        myNewsList: ArrayList<Documents>?
    ) {
        try {
            this.myContext = myContext
            myClubInfoList = myNewsList
            notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateAdpter(aClubforList: ArrayList<Documents>) {
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

    /*
     * ItemViewHolder
     * */
    inner class ItemViewHolder internal constructor(view: View) :
        RecyclerView.ViewHolder(view) {
        var myclubName: TextView
        var myCropImage: ImageView
        var mDocLAY: LinearLayout

        init {
            myclubName = view.findViewById(R.id.doc_name)
            myCropImage = view.findViewById(R.id.doc_image)
            mDocLAY = view.findViewById(R.id.doc_list_Image_LAY)
            mDocLAY.visibility = View.VISIBLE
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
        var TAG = TLMsgProfileIDocuAdapter::class.java.simpleName
    }

    init {
        try {
            myContext = context
            myClubInfoList = aclubInfoList
            myFragmentManager = TLFragmentManager(myContext!!)
            myDownloadPDF = TLDownloadPdf(myContext)
            //  myImageLoader.init(ImageLoaderConfiguration.createDefault(myContext));
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}