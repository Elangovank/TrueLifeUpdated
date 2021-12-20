package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response
import java.io.Serializable

class ClubListShareModel : Response(),Serializable {
    @SerializedName("club_list")
    var clubList: List<ClubList>? =
        null
    @SerializedName("max_last_updated_time")
    var maxLastUpdatedTime: String? = null

    class ClubList : Serializable {
        @SerializedName("club_admin")
        var clubAdmin: String? = null
        @SerializedName("club_image")
        var clubImage: String? = null
        @SerializedName("club_name")
        var clubName: String? = null
        @SerializedName("club_posting_right")
        var clubPostingRight: String? = null
        @SerializedName("id")
        var id: String? = null
        @SerializedName("total_members")
        var totalMembers: String? = null

        var isChecked = false

    }
}