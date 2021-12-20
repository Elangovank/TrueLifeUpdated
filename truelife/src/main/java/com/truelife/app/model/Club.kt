package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response
import java.io.Serializable


class Club : Response(), Serializable {

    @SerializedName("club_list")
    var data: ArrayList<Clubs>? = null

    @SerializedName("total_page")
    var totaPage: String = ""

    class Clubs : Response() {

        @SerializedName("club_list")
        var clubs: Clubs? = null

        @SerializedName("club_rights")
        var clubRights: ClubRights? = null

        @SerializedName("club_visibility")
        var clubVisibility: String? = null

        @SerializedName("club_members_info")
        var clubMembers: ArrayList<ClubMember>? = null

        @SerializedName("club_media")
        var mClubMedia: ArrayList<Media>? = null

        @SerializedName("id")
        var id: String? = null

        @SerializedName("admin_id")
        var adminId: String? = null


        @SerializedName("club_name")
        var clubName: String? = null

        @SerializedName("club_type")
        var clubType: String? = null

        @SerializedName("club_for")
        var clubFor: String? = null

        @SerializedName("club_admin")
        var clubAdmin: String? = null

        @SerializedName("club_administrator")
        var clubAdministrator: String? = null

        @SerializedName("is_friend_with_admin")
        var is_friend_with_admin = 0

        @SerializedName("club_image")
        var profileImage: String? = null

        @SerializedName("total_members")
        var totalMembers: String? = null

        @SerializedName("club_posting_right")
        var clubPostingRight: String? = null

        var isSelected: Boolean = false

        @SerializedName("is_member_status")
        var is_member_status: String? = null

        @SerializedName("admin_email")
        var admin_email = ""

        @SerializedName("notification")
        var notification = ""
    }


    class ClubMember {

        @SerializedName("user_id")
        var userId: String? = null

        @SerializedName("first_name")
        var firstName: String? = null

        @SerializedName("last_name")
        var lastName: String? = null

        @SerializedName("profile_image")
        var profileImage: String? = null

        @SerializedName("gender")
        var gender: String? = null
    }

    class ClubRights {

        @SerializedName("posting")
        var posting: String? = null

        @SerializedName("like")
        var like: String? = null

        @SerializedName("share")
        var share: String? = null

        @SerializedName("comment")
        var comment: String? = null

        @SerializedName("rule")
        var rule: String? = null

        @SerializedName("member_visibility")
        var memberVisibility: String? = null

        @SerializedName("maximum_member")
        var maximum_member: String? = null

    }

    class PublicCategory : Response() {

        @SerializedName("public_count")
        val mCategory: PublicCategory? = null

        @SerializedName("street_user_count")
        val mStreetCount = 0

        @SerializedName("area_user_count")
        val mAreaCount = 0

        @SerializedName("city_user_count")
        val mCityCount = 0

        @SerializedName("state_user_count")
        val mStateCount = 0

        @SerializedName("country_user_count")
        val mCountryCount = 0

    }


    class FriendList : Response() {

        @SerializedName("total_page")
        val total_page: String? = null

        @SerializedName("friends_list")
        val mList = ArrayList<FriendList>()

        @SerializedName("id")
        val id: String? = null

        @SerializedName("fullname")
        val fullname: String? = null

        @SerializedName("gender")
        val gender: String? = null

        @SerializedName("profile_image")
        val profile_image: String? = null

        @SerializedName("is_member_status")
        var is_member_status: String? = null

        var isSelected = false

    }

    class Media : Response() {

        @SerializedName("id")
        val id: String? = null

        @SerializedName("Original")
        val original: String? = null

        @SerializedName("Icon")
        val icon: String? = null

        @SerializedName("Thumb")
        val thumb: String? = null

        @SerializedName("Mini")
        val mini: String? = null

        @SerializedName("media_type")
        val media_type: String? = null

        @SerializedName("created")
        val created: String? = null

    }
}