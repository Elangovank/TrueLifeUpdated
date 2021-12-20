package com.truelife.chat.adapters.messaging.holders

import android.content.Context
import android.view.View
import com.aghajari.emojiview.view.AXEmojiTextView
import  com.truelife.R
import com.truelife.chat.adapters.messaging.holders.base.BaseReceivedHolder
import com.truelife.chat.model.realms.Message
import com.truelife.chat.model.realms.User

// received message with type text
class ReceivedTextHolder(context: Context, itemView: View) : BaseReceivedHolder(context,itemView) {

    private var tvMessageContent: AXEmojiTextView = itemView.findViewById(R.id.tv_message_content)

    override fun bind(message: Message,user: User) {
        super.bind(message,user)
        tvMessageContent.text = message.content
    }


}