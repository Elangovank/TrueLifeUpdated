package com.truelife.app.fragment.chat.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.ChatProfile.ClubMedia
import java.util.*

/**
 * Created by mahalingam on 25-04-2017.
 */
class TLChatProfileMediaAdapter(
    private var myContext: FragmentActivity,
    private val myMap: TreeMap<String, ArrayList<ClubMedia>>,
    aType: Int
) : RecyclerView.Adapter<TLChatProfileMediaAdapter.ViewHolder>() {
    private var splitcount = 0
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(myContext)
            .inflate(R.layout.club_by_you_first_image_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(
        aHolder: ViewHolder,
        position: Int
    ) {
        val title = myMap.keys.toTypedArray()[position]
        if (position < myMap.size && position != 0) {
            if (myMap.keys.toTypedArray()[position] == myMap.keys.toTypedArray()[position - 1]) {
                aHolder.itemTitle.visibility = View.GONE
            } else {
                aHolder.itemTitle.visibility = View.VISIBLE
            }
        }
        if (position == 0) {
            aHolder.itemTitle.visibility = View.VISIBLE
        }
        aHolder.itemTitle.text = title
        val myMedia = ArrayList(myMap[title]!!)
        val mySection = TLChatProfileImageAdapter(myContext, myMedia)
        aHolder.recycler_view_list.adapter = mySection
    }

    fun updateAdapter(
        myContext: FragmentActivity,
        myNewsList: TreeMap<String, ArrayList<ClubMedia>>?
    ) {
        try {
            this.myContext = myContext
            myMap.putAll(myNewsList!!)
            //this.myMap = myNewsList;
            notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return myMap.size
    }

    interface ClickListener {
        fun onClick(view: View?, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    inner class ViewHolder(aView: View) :
        RecyclerView.ViewHolder(aView) {
        var itemTitle: TextView
        var recycler_view_list: RecyclerView

        init {
            itemTitle = aView.findViewById(R.id.itemTitle)
            recycler_view_list = aView.findViewById(R.id.recycler_view_list)
            recycler_view_list.setHasFixedSize(true)
            val myRV_LayoutManager = GridLayoutManager(myContext, splitcount)
            recycler_view_list.layoutManager = myRV_LayoutManager
            recycler_view_list.isNestedScrollingEnabled = true
        }
    }

    init {
        splitcount = aType
    }
}