package com.truelife.app.fragment.chat.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.constants.TLConstant
import com.truelife.app.model.Chat
import com.truelife.util.Utility
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

/**
 * Created by Elango on 26-12-2019.
 */

class TLContactsAdapter(
    var mContext: Context,
    var mData: ArrayList<Chat.ContactsList>,
    var mCallback: Callback
) : RecyclerView.Adapter<TLContactsAdapter.ItemViewHolder>(), Filterable {

    private var aFilteredList = ArrayList<Chat.ContactsList>()
    private var aData = ArrayList<Chat.ContactsList>()

    init {
        this.aData = mData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.inflate_chat_list_item, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val data = mData[position]

        holder.mName.text = data.name
        holder.mMessage.visibility = View.GONE

        if (data.profile_image != null && data.profile_image!!.isNotEmpty())
            Utility.loadImage(data.profile_image!!, holder.mImage)
        else Utility.loadPlaceHolder(mContext, TLConstant.MALE, holder.mImage)

        if (data.numbers.isNotEmpty()) {
            holder.mMainLayout.setOnClickListener {
                mCallback.info(data)
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    open class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mMainLayout = view.findViewById<LinearLayout>(R.id.chat_member_LAY)!!
        var mName = view.findViewById<TextView>(R.id.chat_list_item_TXT_name)!!
        var mMessage = view.findViewById<TextView>(R.id.chat_list_item_TXT_msg)!!
        var mImage = view.findViewById<CircleImageView>(R.id.chat_list_item_IMG_profile)!!
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val aText = constraint!!.toString().toLowerCase()
                if (aText.isEmpty())
                    mData = aData
                else {
                    aFilteredList.clear()
                    for (data in aData) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (data.name!!.toLowerCase().contains(aText))
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
                mData = results!!.values as ArrayList<Chat.ContactsList>
                notifyDataSetChanged()
            }

        }
    }

    interface Callback {
        fun info(data: Chat.ContactsList)
    }
}
