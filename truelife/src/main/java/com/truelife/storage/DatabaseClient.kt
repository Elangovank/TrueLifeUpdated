package com.truelife.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.truelife.BuildConfig
import com.truelife.app.model.Chat

/**
 * Created by Elango on 11/02/2020.
 **/

@Database(entities = [Chat.ChatList::class, Chat.Chats::class], version = BuildConfig.VERSION_CODE, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {
    abstract fun chatlist(): Chat.ChatListDAO
    abstract fun chat(): Chat.ChatDAO
}

class DatabaseClient(var mContext: Context) {

    fun getAppDatabase(): AppDataBase? {
        return Room.databaseBuilder(mContext, AppDataBase::class.java, "TrueLife")
            .allowMainThreadQueries()
            .build()
    }
}
