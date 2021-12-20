package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response

class CreateClubFriendsList : Response() {

    @SerializedName("total_page")
    var total_page: String? = null

    @SerializedName("friends_list")
    var mData = ArrayList<FriendsList>()


    class FriendsList() {
        @SerializedName("isSelected")
        var isSelected: Boolean = false

        @SerializedName("id")
        var id: String? = null

        @SerializedName("first_name")
        var firstName: String? = null

        @SerializedName("lastName")
        var lastName: String? = null

        @SerializedName("fullname")
        var fullname: String? = null

        @SerializedName("profile_image")
        var profileimage: String? = null

        @SerializedName("gender")
        var gender: String? = null

        @SerializedName("is_member_status")
        var is_member_status: String? = null
    }
}