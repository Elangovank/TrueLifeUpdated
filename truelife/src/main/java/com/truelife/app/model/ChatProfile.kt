package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response
import java.io.Serializable
import java.util.*

/**
 * Created by Elango on 10-02-2020.
 */
class ChatProfile : Response(), Serializable {


    @SerializedName("media_info")
    var mData: ChatProfile? = null

    @SerializedName("id")
    var id: String? = null

    @SerializedName("first_name")
    var first_name: String? = null

    @SerializedName("last_name")
    var last_name: String? = null

    @SerializedName("fullname")
    var fullname: String? = null

    @SerializedName("email_id")
    var emailId: String? = null

    @SerializedName("mobile_number")
    var phonoNo: String? = null

    @SerializedName("gender")
    var gender: String? = null

    @SerializedName("profile_image")
    var profile_image: String? = null

    @SerializedName("cover_photo")
    var cover_photo: String? = null

    @SerializedName("status")
    var status: String? = null

    @SerializedName("more_photos")
    var more_photos: String? = null

    @SerializedName("more_docs")
    var more_docs: String? = null

    @SerializedName("img_info")
    var img_info = ArrayList<Images>()

    @SerializedName("doc_info")
    var doc_info = ArrayList<Documents>()

    inner class Images {

        @SerializedName("id")
        var id = ""

        @SerializedName("url")
        var url = ""

        @SerializedName("file_type")
        var file_type = ""

        @SerializedName("video_thumbnail")
        var video_thumbnail = ""
    }

    inner class Documents {

        @SerializedName("id")
        var id = ""

        @SerializedName("url")
        var url = ""

    }

    class ClubMedia : Response(), Serializable {

        @SerializedName("img_info")
        var mData = ArrayList<ClubMedia>()

        @SerializedName("total_page")
        var total_page = ""

        @SerializedName("id")
        var id = ""

        @SerializedName("url")
        var url = ""

        @SerializedName("file_type")
        var file_type = ""

        @SerializedName("video_thumbnail")
        var thumb = ""

        @SerializedName("created")
        var created = ""

        @SerializedName("created_time")
        var created_time = ""
    }
}