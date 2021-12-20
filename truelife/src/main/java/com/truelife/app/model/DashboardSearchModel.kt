package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response
import java.io.Serializable

class DashboardSearchModel : Serializable, Response() {
    @SerializedName("search_info")
    var searchInfo: SearchInfo? = null


    class SearchInfo : Serializable {
        @SerializedName("club_details")
        var clubDetails: ArrayList<ClubDetail>? = ArrayList()
        @SerializedName("clubs_feeds_count")
        var clubsFeedsCount: Long? = null
        @SerializedName("friends_details")
        var friendsDetails: ArrayList<FriendsDetail>? = ArrayList()
        @SerializedName("friends_feeds_count")
        var friendsFeedsCount: Long? = null
        @SerializedName("more_clubs")
        var moreClubs: Long? = null
        @SerializedName("more_friends")
        var moreFriends: Long? = null
        @SerializedName("peoples_count")
        var peoplesCount: Long? = null
        @SerializedName("public_feeds_count")
        var publicFeedsCount: Long? = null

    }

    class ClubDetail : Serializable {
        @SerializedName("admin_id")
        var adminId: String? = null
        @SerializedName("club_administrator")
        var clubAdministrator: String? = null
        @SerializedName("club_for")
        var clubFor: String? = null
        @SerializedName("club_image")
        var clubImage: String? = null
        @SerializedName("club_media")
        var clubMedia: List<ClubMedium>? =
            null
        @SerializedName("club_members_info")
        var clubMembersInfo: List<ClubMembersInfo>? =
            null
        @SerializedName("club_name")
        var clubName: String? = null
        @SerializedName("club_rights")
        var clubRights: ClubRights? = null
        @SerializedName("club_type")
        var clubType: String? = null
        @SerializedName("id")
        var id: String? = null
        @SerializedName("is_member_status")
        var isMemberStatus: String? = null
        @SerializedName("total_members")
        var totalMembers: String? = null

    }

    class ClubMedium : Serializable {
        @SerializedName("created")
        var created: String? = null
        @SerializedName("id")
        var id: String? = null
        @SerializedName("media_type")
        var mediaType: String? = null
        @SerializedName("Original")
        var original: String? = null
        @SerializedName("Thumb")
        var thumb: String? = null

    }

    class ClubMembersInfo : Serializable {
        @SerializedName("first_name")
        var firstName: String? = null
        @SerializedName("last_name")
        var lastName: String? = null
        @SerializedName("profile_image")
        var profileImage: String? = null
        @SerializedName("user_id")
        var userId: String? = null

    }

    class ClubRights : Serializable {
        @SerializedName("comment")
        var comment: String? = null
        @SerializedName("like")
        var like: String? = null
        @SerializedName("maximum_member")
        var maximumMember: String? = null
        @SerializedName("member_visibility")
        var memberVisibility: String? = null
        @SerializedName("posting")
        var posting: String? = null
        @SerializedName("rule")
        var rule: String? = null
        @SerializedName("share")
        var share: String? = null

    }



    class FriendsDetail : Serializable {
        @SerializedName("friend_id")
        var friendId: String? = null
        @SerializedName("friend_name")
        var friendName: String? = null
        @SerializedName("profile_image")
        var profileImage: String? = null

    }
}