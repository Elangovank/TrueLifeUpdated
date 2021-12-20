package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response
import java.io.Serializable


class UserList : Response(), Serializable {

    @SerializedName("blocked_list")
    var blockedList: List<UList>? = null

    @SerializedName("followers_list")
    var followers_list: List<UList>? = null

    @SerializedName("friends_list")
    var friends_list: List<UList>? = null

    @SerializedName("following_list")
    var following_list: List<UList>? = null


    @SerializedName("total_page")
    var totalPage: String? = null

    class UList {

        @SerializedName("first_name")
        var firstName: String? = null

        @SerializedName("fullname")
        var fullname: String? = null

        @SerializedName("gender")
        var gender: String? = null

        @SerializedName("id")
        var id: String? = null

        @SerializedName("last_name")
        var lastName: String? = null

        @SerializedName("profile_image")
        var profileImage: String? = null

        @SerializedName("is_follow")
        var is_follow: String? = null

        @SerializedName("is_friend")
        var is_friend   : String? = null

        @SerializedName("is_online")
        var is_online: String? = null

        @SerializedName("is_blocked")
        var is_blocked: String? = null

        @SerializedName("user_blocked")
        var user_blocked: String? = null

        @SerializedName("last_seen")
        var last_seen: String? = null
    }
}