package com.truelife.chat.utils.network

import com.truelife.chat.model.realms.User
import com.truelife.chat.utils.RealmHelper
import io.reactivex.Observable

object UserByIdsDataSource {
    fun getUsersByIds(uids: List<String>): Observable<MutableList<User>> {
        val observersList = arrayListOf<Observable<User?>>()

        for (uid in uids) {
            if (uid != FireManager.uid) {
                val user = RealmHelper.getInstance().getUser(uid)
                if (user != null) {
                    observersList.add(Observable.just(user))
                } else {
                    observersList.add(FireManager.fetchUserByUid(uid).toObservable())
                }

            }
        }


        return Observable.merge(observersList).toList().toObservable().map {
            it.filterNotNull().toMutableList()
        }

    }
}