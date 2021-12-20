package com.truelife.app.fragment.chat.chatlist

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.eyalbira.loadingdots.LoadingDots
import com.truelife.R
import com.truelife.app.model.Chat
import com.truelife.util.Utility
import de.hdodenhof.circleimageview.CircleImageView
import org.apache.commons.lang3.StringEscapeUtils
import java.util.*

/**
 * Created by Elango on 12-12-2019.
 */

class TLChatListAdapter(
    var mContext: Context,
    var mChatData: ArrayList<Chat.ChatList>,
    var mCallback: Callback
) : RecyclerView.Adapter<TLChatListAdapter.ItemViewHolder>(), Filterable {

    private var aFilteredList = ArrayList<Chat.ChatList>()
    private var aChatData = ArrayList<Chat.ChatList>()
    private var mEnableDelete = false

    init {
        this.aChatData = mChatData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.inflate_chat_list_item, parent, false)
        return ItemViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val data = mChatData[position]
        holder.mName.text = data.user_name

        if (data.attachments!!.isEmpty())
            holder.mMessage.text = StringEscapeUtils.unescapeJava(data.message)
        else holder.mMessage.text = Utility.getMediaType(data.attachments!!)

        if (data.unread_count!!.isEmpty() || data.unread_count!! == "0")
            holder.mUnReadCount.visibility = View.GONE
        else {
            holder.mUnReadCount.visibility = View.VISIBLE
            holder.mUnReadCount.text = data.unread_count
        }

        if (data.online!! == "0" || data.blocked == "1" || data.user_blocked == "1")
            holder.mStatus.visibility = View.GONE
        else
            holder.mStatus.visibility = View.VISIBLE

        if (data.profile_image!!.isNotEmpty())
            Utility.loadImage(data.profile_image!!, holder.mImage)
        else Utility.loadPlaceHolder(mContext, data.gender!!, holder.mImage)

        if (data.typing) {
            Handler().postDelayed({
                data.typing = false
                holder.mDots.visibility = View.GONE
            }, 1000)
            holder.mDots.visibility = View.VISIBLE
        }

        holder.mMainLayout.setOnClickListener {
            if (mEnableDelete) {
                holder.mCheckBox.isChecked = !data.checked
                data.checked = holder.mCheckBox.isChecked
                mCallback.delete()
            } else mCallback.info(position)
        }

        // Delete Chat

        if (mEnableDelete)
            holder.mCheckBox.visibility = View.VISIBLE
        else holder.mCheckBox.visibility = View.GONE

        holder.mCheckBox.isChecked = data.checked

        holder.mCheckBox.setOnClickListener {
            data.checked = holder.mCheckBox.isChecked
            mCallback.delete()
        }
    }

    override fun getItemCount(): Int {
        return mChatData.size
    }

    open class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mMainLayout = view.findViewById<LinearLayout>(R.id.chat_member_LAY)!!
        var mName = view.findViewById<TextView>(R.id.chat_list_item_TXT_name)!!
        var mMessage = view.findViewById<TextView>(R.id.chat_list_item_TXT_msg)!!
        var mUnReadCount = view.findViewById<TextView>(R.id.chat_list_item_TXT_unread)!!
        var mStatus = view.findViewById<TextView>(R.id.message_status)!!
        var mImage = view.findViewById<CircleImageView>(R.id.chat_list_item_IMG_profile)!!
        var mCheckBox = view.findViewById<CheckBox>(R.id.multiple_list_item_check_RD_button)!!
        var mDots = view.findViewById<LoadingDots>(R.id.chat_list_item_loading)!!

    }

    fun enableDelete(enable: Boolean) {
        mEnableDelete = enable
        notifyDataSetChanged()
    }

    interface Callback {
        fun info(position: Int)
        fun delete()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val aText = constraint!!.toString().toLowerCase()
                if (aText.isEmpty())
                    mChatData = aChatData
                else {
                    aFilteredList.clear()
                    for (data in aChatData) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (data.user_name!!.toLowerCase().contains(aText) || data.message!!.toLowerCase().contains(aText))
                            aFilteredList.add(data)
                    }
                    mChatData = aFilteredList
                }

                val filterResults = FilterResults()
                filterResults.values = mChatData
                filterResults.count = mChatData.size
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mChatData = results!!.values as ArrayList<Chat.ChatList>
                notifyDataSetChanged()
            }

        }
    }
}
