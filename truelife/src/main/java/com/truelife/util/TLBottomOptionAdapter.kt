package com.truelife.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R

/**
 * Created by Elango on 11-12-2019.
 */

class TLBottomOptionAdapter(
    var mContext: Context,
    var mItems: Array<String>,
    var mCallback: Callback
) :
    RecyclerView.Adapter<TLBottomOptionAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.inflate_bottom_choice, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.mName.text = mItems[position]

        if (position == mItems.size - 1)
            holder.mView.visibility = View.GONE
        else holder.mView.visibility = View.VISIBLE

        holder.mName.setOnClickListener {

            AppDialogs.hideBottomDialog()
            mCallback.info(position, mItems[position])
        }
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    open class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mName = view.findViewById<TextView>(R.id.inflate_single_item_text)!!
        var mView = view.findViewById<View>(R.id.inflate_single_item_view)!!
    }

    interface Callback {
        fun info(option: Int, text: String)
    }
}
