package com.truelife.chat.adapters.messaging.holders

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import  com.truelife.R
import com.truelife.chat.adapters.messaging.holders.base.BaseHolder
import com.truelife.chat.model.realms.GroupEvent
import com.truelife.chat.model.realms.Message
import com.truelife.chat.model.realms.User

 class GroupEventHolder(context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvGroupEvent: TextView = itemView.findViewById(R.id.tv_group_event)

     fun bind(message: Message,user: User){
         tvGroupEvent.text = GroupEvent.extractString(message.content, user.group.users)
     }


}