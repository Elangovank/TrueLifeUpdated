package com.truelife.app.fragment.club.media

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.Club
import java.util.*

/**
 * Created by Elango on 21-02-2020.
 */

class TLClubMediaHeaderAdapter(
    private val mContext: FragmentActivity,
    private val mMediaMap: TreeMap<String, ArrayList<Club.Media>>
) : RecyclerView.Adapter<TLClubMediaHeaderAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(mContext)
            .inflate(R.layout.club_by_you_first_image_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(
        aHolder: ViewHolder,
        position: Int
    ) {
        val title = mMediaMap.keys.toTypedArray()[position]
        aHolder.mTitle.text = title
        val myMedia = ArrayList(mMediaMap[title]!!)
        val mySection = TLClubMediaItemsAdapter(mContext, myMedia)
        aHolder.mMediaRecycler.adapter = mySection
    }

    override fun getItemCount(): Int {
        return mMediaMap.size
    }

    interface ClickListener {
        fun onClick(view: View?, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    inner class ViewHolder constructor(aView: View) :
        RecyclerView.ViewHolder(aView) {
        var mTitle: TextView = aView.findViewById(R.id.itemTitle)
        var mMediaRecycler: RecyclerView = aView.findViewById(R.id.recycler_view_list)

        init {
            mMediaRecycler.setHasFixedSize(true)
            mMediaRecycler.layoutManager = LinearLayoutManager(
                mContext,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            mMediaRecycler.isNestedScrollingEnabled = true
        }
    }

}