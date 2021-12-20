package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response

class ProfileSettingModel : Response() {

    @SerializedName("profile_info")
    var setting: ProfileSettingModel? = null

    @SerializedName("school_info")
    var schoolInfo: ArrayList<SchoolInfo>? = null

    @SerializedName("college_info")
    var collageInfo: ArrayList<CollageInfo>? = null

    @SerializedName("workplace_info")
    var workPlaceInfo: ArrayList<WorkPlaceInfo>? = null

    @SerializedName("user_id")
    var userId: String? = null

    @SerializedName("category")
    var category: String? = null

    @SerializedName("celebrity")
    var celebrity: String? = null

    @SerializedName("nationality")
    var nationality: String? = null

    @SerializedName("religion_beliefs")
    var religionBeliefs: String? = null

    @SerializedName("date_of_birth")
    var dateOfBirth: String? = null

    class SchoolInfo {
        @SerializedName("user_id")
        var userId: String? = null

        @SerializedName("id")
        var id: String? = null

        @SerializedName("school")
        var school: String? = null

        @SerializedName("school_from")
        var school_from: String? = null

        @SerializedName("school_to")
        var school_to: String? = null

        @SerializedName("location")
        var location: String? = null

    }

    class CollageInfo {
        @SerializedName("user_id")
        var userId: String? = null

        @SerializedName("id")
        var id: String? = null

        @SerializedName("college")
        var college: String? = null

        @SerializedName("college_from")
        var collegeFrom: String? = null

        @SerializedName("college_to")
        var collegeTo: String? = null

        @SerializedName("location")
        var location: String? = null

    }

    class WorkPlaceInfo {
        @SerializedName("user_id")
        var userId: String? = null

        @SerializedName("id")
        var id: String? = null

        @SerializedName("workplace")
        var workPlace: String? = null

        @SerializedName("workplace_from")
        var workplaceFrom: String? = null

        @SerializedName("workplace_to")
        var workplaceTo: String? = null

        @SerializedName("location")
        var location: String? = null
    }
}