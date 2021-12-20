package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response

class Setting : Response() {

    @SerializedName("privacy_info")
    var setting: Setting? = null


    @SerializedName("id")
    var id: String? = null

    @SerializedName("user_id")
    var userId: String? = null

    @SerializedName("view_your_profile")
    var viewYourProfile: String? = null

    @SerializedName("view_your_posts_frnds")
    var viewYourPostsFrnds: String? = null

    @SerializedName("comment_on_public")
    var commentOnPublic: String? = null

    @SerializedName("comment_on_frnds_posts")
    var commentOnFrndsPosts: String? = null

}

