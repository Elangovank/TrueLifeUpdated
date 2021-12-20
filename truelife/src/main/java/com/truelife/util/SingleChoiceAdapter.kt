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

/**
 * Created by Elango on 12-03-2019.
 */

class SingleChoiceAdapter(
    var mContext: Context,
    var mItems: ArrayList<String>,
    var mCallback: Callback
) :
    RecyclerView.Adapter<SingleChoiceAdapter.ItemViewHolder>(), Filterable {

    var aFilteredList = ArrayList<String>()
    var aData = mItems

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.inflate_single_choice_item, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.mName.text = mItems[position]
        holder.mName.setOnClickListener {
            mCallback.info(position, mItems[position])
        }
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    open class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
                        if (data.toLowerCase().startsWith(aText) || data.toLowerCase().contains(
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
                mItems = results!!.values as ArrayList<String>
                notifyDataSetChanged()
            }

        }
    }
}
