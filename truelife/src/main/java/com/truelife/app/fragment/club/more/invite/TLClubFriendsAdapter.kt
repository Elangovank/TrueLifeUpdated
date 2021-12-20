package com.truelife.app.fragment.club.more.invite

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.Club
import com.truelife.util.Utility
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

/**
 * Created by Elango on 19-02-2020.
 */

class TLClubFriendsAdapter(
    var mContext: Context,
    var mData: ArrayList<Club.FriendList>,
    var mCallback: Callback,
    var isMore: Boolean,
    var isRemove: Boolean
) : RecyclerView.Adapter<TLClubFriendsAdapter.ItemViewHolder>(), Filterable {

    var mFilterData = ArrayList<Club.FriendList>()
    private var aFilteredList = ArrayList<Club.FriendList>()

    init {
        this.mFilterData = mData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.inflate_remove_member_list_item, parent, false)
        return ItemViewHolder(itemView)
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val data = mData[position]

        holder.mName.text = data.fullname

        if (data.profile_image!!.isNotEmpty())
            Utility.loadImage(data.profile_image, holder.mImage)
        else Utility.loadPlaceHolder(mContext, data.gender!!, holder.mImage)

        if (isRemove) {
            holder.mAdd.isEnabled = data.is_member_status == "1"
            if (data.is_member_status == "1") {
                holder.mAdd.setBackgroundResource(R.drawable.bg_round_button_black)
                holder.mAdd.text = "Remove"
            } else {
                holder.mAdd.text = "Removed"
                holder.mAdd.setBackgroundResource(R.drawable.bg_round_button_grey)
            }
        } else {
            holder.mAdd.isEnabled = data.is_member_status == "0"
            if (data.is_member_status == "0") {
                holder.mAdd.setBackgroundResource(R.drawable.bg_round_button_black)
                holder.mAdd.text = "Add"
            } else {
                holder.mAdd.text = "Added"
                holder.mAdd.setBackgroundResource(R.drawable.bg_round_button_grey)
            }
        }

        holder.mAdd.setOnClickListener {
            mCallback.info(data.id!!)
        }
    }

    override fun getItemCount(): Int {
        return when {
            mData.isEmpty() -> mData.size
            isMore -> mData.size
            else -> if (mData.size > 4) 4 else mData.size
        }
    }

    open class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mName = view.findViewById<TextView>(R.id.inflate_menu_list_name)!!
        var mAdd = view.findViewById<Button>(R.id.inflate_menu_right_arrow)!!
        var mImage = view.findViewById<CircleImageView>(R.id.inflate_menu_list_icon)!!

    }

    interface Callback {
        fun info(id: String)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val aText = constraint!!.toString().toLowerCase()
                if (aText.isEmpty())
                    mData = mFilterData
                else {
                    aFilteredList.clear()
                    for (data in mFilterData) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (data.fullname!!.toLowerCase().contains(aText))
                            aFilteredList.add(data)
                    }
                    mData = aFilteredList
                }

                val filterResults = FilterResults()
                filterResults.values = mData
                filterResults.count = mData.size
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mData = results!!.values as ArrayList<Club.FriendList>
                notifyDataSetChanged()
            }
        }
    }
}
