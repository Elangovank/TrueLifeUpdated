package com.truelife.app.model

import androidx.annotation.NonNull
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.gson.annotations.SerializedName
import com.truelife.http.Response
import java.io.Serializable


/**
 * Created by Elango on 12/12/19.
 **/

class Chat : Response(), Serializable {

    @Entity(tableName = "chat_list")
    class ChatList : Response() {

        @Ignore
        @SerializedName("chat_data")
        var mData = ArrayList<ChatList>()

        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        @ColumnInfo(name = "id")
        @NonNull
        var id: String? = null

        @NonNull
        @ColumnInfo(name = "user_id")
        @SerializedName("user_id")
        var user_id: String? = null

        @NonNull
        @ColumnInfo(name = "user_name")
        @SerializedName("user_name")
        var user_name: String? = null

        @NonNull
        @ColumnInfo(name = "gender")
        @SerializedName("gender")
        var gender: String? = null

        @NonNull
        @ColumnInfo(name = "profile_image")
        @SerializedName("profile_image")
        var profile_image: String? = null

        @NonNull
        @ColumnInfo(name = "message")
        @SerializedName("message")
        var message: String? = null

        @NonNull
        @ColumnInfo(name = "attachments")
        @SerializedName("attachments")
        var attachments: String? = null

        @NonNull
        @ColumnInfo(name = "unread_count")
        @SerializedName("unread_count")
        var unread_count: String? = null

        @NonNull
        @ColumnInfo(name = "created")
        @SerializedName("created")
        var created: String? = null

        @NonNull
        @ColumnInfo(name = "is_online")
        @SerializedName("is_online")
        var online: String? = null

        @ColumnInfo(name = "is_read")
        @SerializedName("is_read")
        var read: Int? = null

        @NonNull
        @ColumnInfo(name = "is_blocked")
        @SerializedName("is_blocked")
        var blocked: String? = null

        @NonNull
        @ColumnInfo(name = "user_blocked")
        @SerializedName("user_blocked")
        var user_blocked: String? = null

        @NonNull
        @ColumnInfo(name = "last_seen")
        @SerializedName("last_seen")
        var last_seen: String? = null

        var typing = false
        var checked = false
        var updated = false

    }

    class FriendsList : Response() {

        @SerializedName("friends_list")
        var mData = ArrayList<FriendsList>()

        @SerializedName("id")
        var id: String? = null

        @SerializedName("fullname")
        var fullname: String? = null

        @SerializedName("gender")
        var gender: String? = null

        @SerializedName("profile_image")
        var profile_image: String? = null

        @SerializedName("total_page")
        var total_page: String? = null

        @SerializedName("is_blocked")
        var is_blocked: String? = null

        @SerializedName("is_online")
        var is_online: String? = null

        @SerializedName("last_seen")
        var last_seen: String? = null
    }

    class ContactsList : Response() {
        var id: String? = null
        var name: String? = null
        var profile_image: String? = null
        var numbers = ArrayList<String>()
    }

    @Entity(tableName = "chat")
    class Chats : Response() {

        @Ignore
        @SerializedName("total_page")
        var total_page: String? = null

        @Ignore
        @SerializedName("is_blocked")
        var is_blocked: String? = null

        @Ignore
        @SerializedName("is_active")
        var is_active = 0

        @Ignore
        @SerializedName("is_online")
        var is_online: String? = null

        @Ignore
        @SerializedName("last_seen")
        var last_seen: String? = null

        // List

        @Ignore
        @SerializedName("chat_messages")
        var mData = ArrayList<Chats>()

        @Ignore
        @SerializedName("chat_history")
        var mSocketData = ArrayList<Chats>()

        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        @ColumnInfo(name = "id")
        @NonNull
        var messageId: String? = null

        @NonNull
        @ColumnInfo(name = "from")
        @SerializedName("FromUserid")
        var messagefrom: String? = null

        @NonNull
        @ColumnInfo(name = "message")
        @SerializedName("messagecontent")
        var message: String? = null

        @NonNull
        @ColumnInfo(name = "is_read")
        @SerializedName("is_read")
        var read = "0"

        @NonNull
        @ColumnInfo(name = "is_delivered")
        @SerializedName("is_delivered")
        var delivered = "0"

        @NonNull
        @ColumnInfo(name = "is_sent")
        @SerializedName("is_sent")
        var sent = "0"

        @Ignore
        @SerializedName("image_url")
        var imageUrl = ArrayList<String>()

        @Ignore
        @SerializedName("video_url")
        var videoUrl = ArrayList<String>()

        @Ignore
        @SerializedName("video_thumbnail")
        var videoThumbUrl = ArrayList<String>()

        @Ignore
        @SerializedName("document_url")
        var documentUrl = ArrayList<String>()

        @Ignore
        @SerializedName("audio_url")
        var audioUrl = ArrayList<String>()

        @NonNull
        @ColumnInfo(name = "created")
        @SerializedName("created")
        var createdAt: String? = null

        @NonNull
        @ColumnInfo(name = "updated")
        @SerializedName("updated")
        var updatedAt: String? = null

        @NonNull
        @ColumnInfo(name = "type")
        var type = ""


        var messageType = ""
        var message_avatar = ""
        var message_status = ""
        var message_to = ""
        var attachment = ""

        var user_id = ""
        var profile_image = ""
        var gender = ""
        var unread_count: Long = 0


        var chatDateheader = 0

        var isSelectedStatus = false

    }

    class FeedInfo {
        var myFeedImageList = ArrayList<String>()
    }

    @Dao
    interface ChatListDAO {

        @Query("SELECT * FROM chat_list")
        fun getAllList(): List<ChatList>

        @Insert
        fun insert(mData: ArrayList<ChatList>)

        @Delete
        fun delete(chat: ChatList)

        @Query("DELETE FROM chat_list")
        fun truncateTable()

        @Update
        fun update(chat: ChatList)
    }

    @Dao
    interface ChatDAO {

        @RawQuery
        fun getAllList(query: SimpleSQLiteQuery): List<Chats>

        @Query("SELECT COUNT(id) FROM chat WHERE id = :id")
        fun getCount(id: String): Int


        @Insert
        fun insert(mData: ArrayList<Chats>)

        @Delete
        fun delete(chat: Chats)

        @Query("DELETE FROM chat")
        fun truncateTable()

        @Update
        fun update(chat: Chats)
    }

}
