package com.truelife.app.fragment.chat.friends

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.truelife.R
import com.truelife.app.model.Chat
import com.truelife.util.Utility
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

/**
 * Created by Elango on 24-12-2019.
 */

class TLFriendsListAdapter(
    var mContext: Context,
    var mData: ArrayList<Chat.FriendsList>,
    var mCallback: Callback
) : RecyclerView.Adapter<TLFriendsListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.inflate_chat_list_item, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val data = mData[position]

        holder.mName.text = data.fullname
        holder.mMessage.visibility = View.GONE

        if (data.profile_image!!.isNotEmpty())
            Utility.loadImage(data.profile_image!!, holder.mImage)
        else Utility.loadPlaceHolder(mContext, data.gender!!, holder.mImage)

        holder.mMainLayout.setOnClickListener {
            mCallback.info(position)
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

    interface Callback {
        fun info(position: Int)
    }
}
