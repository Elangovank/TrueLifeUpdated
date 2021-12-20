package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response

class LikeList : Response() {



    @SerializedName("Like")
    var mDataList = ArrayList<LikeList>()

    @SerializedName("user_id")
    var id: String? = null

    @SerializedName("user_name")
    var fullname: String? = null



    @SerializedName("user_image")
    var profile_image: String? = null

    /*   @SerializedName("last_name")
     var last_name: String? = null

     @SerializedName("first_name")
    var first_name: String? = null

     @SerializedName("gender")
     var gender: String? = null

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
 */

}