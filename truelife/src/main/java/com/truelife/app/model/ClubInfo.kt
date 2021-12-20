package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response


class ClubInfo : Response() {

    @SerializedName("club_list")
    var data: ArrayList<ClubInfo>? = null

    @SerializedName("id")
    var id: String? = null

    @SerializedName("club_name")
    var clubName: String? = null

    @SerializedName("club_admin")
    var clubAdmin: String? = null

    @SerializedName("club_image")
    var profileImage: String? = null

    @SerializedName("total_members")
    var totalMembers: String? = null

    @SerializedName("club_posting_right")
    var clubPostingRight: String? = null


    var isSelected: Boolean = false
}