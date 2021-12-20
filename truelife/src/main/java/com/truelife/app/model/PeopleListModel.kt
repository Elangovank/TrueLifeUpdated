package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response
import java.io.Serializable

class PeopleListModel : Serializable, Response() {
    @SerializedName("peoples_list")
    var peoplesList: ArrayList<PeoplesList>? = ArrayList()

    @SerializedName("total_page")
    var totalPage: Int = 0

    class PeoplesList : Serializable {
        @SerializedName("city_name")
        var cityName: String? = null
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

    }
}