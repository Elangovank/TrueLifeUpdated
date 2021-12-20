package com.truelife.chat.adapters.messaging

import com.truelife.chat.model.realms.RealmContact

interface ContactHolderInteraction {
    fun onMessageClick(contact:RealmContact)
    fun onAddContactClick(contact:RealmContact)
}