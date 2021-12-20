package com.truelife.chat.adapters.messaging

import com.truelife.chat.model.realms.Message

interface AudibleInteraction {
    fun onSeek(message:Message,progress:Int,max:Int)
}