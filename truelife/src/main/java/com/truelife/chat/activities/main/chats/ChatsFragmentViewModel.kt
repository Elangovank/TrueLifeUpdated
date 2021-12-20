package com.truelife.chat.activities.main.chats

import com.truelife.chat.common.DisposableViewModel
import com.truelife.chat.model.realms.User
import com.truelife.chat.utils.TimeHelper
import com.truelife.chat.utils.network.FireManager
import io.reactivex.rxkotlin.addTo
import java.util.*

class ChatsFragmentViewModel : DisposableViewModel() {
    private val currentDownloads = mutableListOf<String>()


    fun fetchUserImage(pos: Int, user: User) {
        if (user.isBroadcastBool)return
        if (TimeHelper.canFetchUserImage(Date().time, user.lastTimeFetchedImage) && !currentDownloads.contains(user.uid)) {
            currentDownloads.add(user.uid)
            FireManager.checkAndDownloadUserThumbImg(user).subscribe({image ->

            }, { throwable ->

            }).addTo(disposables)
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

}