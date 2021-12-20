package com.truelife.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.LikeList
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by Elango on 12-03-2019.
 */

class LikeListAdapter(
    var mContext: Context,
    var mItems: ArrayList<LikeList>,
    var mCallback: Callback
) :
    RecyclerView.Adapter<LikeListAdapter.ItemViewHolder>(), Filterable {

    var aFilteredList = ArrayList<LikeList>()
    var aData = mItems

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.inflate_like_list_item, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

        val item = mItems[position]
        holder.mName.text = item.fullname

        if (!item.profile_image.isNullOrEmpty())
            item.profile_image?.let { Utility.loadImage(it, holder.mProfilePic) }
        holder.mName.setOnClickListener {
            mCallback.info(position, item.id!!)
        }
        holder.mProfilePic.setOnClickListener {
            mCallback.info(position, item.id!!)
        }
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    open class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mProfilePic = view.findViewById(R.id.inflate_menu_list_icon) as CircleImageView
        var mName = view.findViewById<TextView>(R.id.inflate_single_item_text)!!
    }

    interface Callback {
        fun info(position: Int, text: String)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val aText = constraint!!.toString().toLowerCase()
                mItems = if (aText.isEmpty()) aData
                else {
                    aFilteredList.clear()
                    for (data in aData) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (data.fullname!!.toLowerCase()
                                .startsWith(aText) || data.fullname!!.toLowerCase().contains(
                                aText
                            )
                        )
                            aFilteredList.add(data)
                    }
                    aFilteredList
                }

                val filterResults = FilterResults()
                filterResults.values = mItems
                filterResults.count = mItems.size
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mItems = results!!.values as ArrayList<LikeList>
                notifyDataSetChanged()
            }

        }
    }
}
