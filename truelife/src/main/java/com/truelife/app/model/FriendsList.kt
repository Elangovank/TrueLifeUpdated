package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response

class FriendsList : Response() {

    @SerializedName("total_page")
    var total_page: String? = null

    @SerializedName("friends_list")
    var mData = ArrayList<FriendsList>()

    @SerializedName("id")
    var id: String? = null

    @SerializedName("first_name")
    var first_name: String? = null

    @SerializedName("gender")
    var gender: String? = null

    @SerializedName("profile_image")
    var profile_image: String? = null

    @SerializedName("last_name")
    var last_name: String? = null

    @SerializedName("fullname")
    var fullname: String? = null

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