package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response
import java.io.Serializable


class PublicFeedModel : Response(), Serializable {
    @SerializedName("club_info")
    var clubInfo: List<ClubInfo>? =
        null
    @SerializedName("feed_list")
    var feedList: List<FeedList>? =
        null
    @SerializedName("is_friend")
    var isFriend: Int? = null
    @SerializedName("user_data")
    var userData: List<UserDatum>? =
        null

    class UserDatum : Serializable {
        @SerializedName("user_id")
        var userId: String? = null
        @SerializedName("user_name")
        var userName: String? = null

    }

    class ClubInfo : Serializable {
        @SerializedName("club_id")
        var clubId: String? = null
        @SerializedName("club_image")
        var clubImage: String? = null
        @SerializedName("club_name")
        var clubName: String? = null
        @SerializedName("total_members")
        var totalMembers: Int? = null

    }

    class ClubList : Serializable {
        @SerializedName("admin_id")
        var adminId: String? = null
        @SerializedName("club_administrator")
        var clubAdministrator: String? = null
        @SerializedName("club_for")
        var clubFor: String? = null
        @SerializedName("club_image")
        var clubImage: String? = null
        @SerializedName("club_media")
        var clubMedia: List<ClubMedium>? = null
        @SerializedName("club_members_info")
        var clubMembersInfo: List<ClubMembersInfo>? = null
        @SerializedName("club_name")
        var clubName: String? = null
        @SerializedName("club_rights")
        var clubRights: ClubRights? = null
        @SerializedName("club_type")
        var clubType: String? = null
        @SerializedName("id")
        var id: String? = null
        @SerializedName("total_members")
        var totalMembers: Int? = null

    }

    class ClubMedium : Serializable {
        @SerializedName("created")
        var created: String? = null
        @SerializedName("Icon")
        var icon: String? = null
        @SerializedName("id")
        var id: String? = null
        @SerializedName("media_type")
        var mediaType: String? = null
        @SerializedName("Mini")
        var mini: String? = null
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

    class FeedList : Response(), Serializable {

        @SerializedName("feed_list")
        var data: ArrayList<FeedList>? = null
        @SerializedName("isProfile")
        var isProfile: String? = "no"

        @SerializedName("club_id")
        var clubId: String? = null
        @SerializedName("club_image")
        var clubImage: String? = null
        @SerializedName("club_list")
        var clubList: ClubList? = null
        @SerializedName("club_name")
        var clubName: String? = null
        @SerializedName("comment")
        var comment: String? = null
        @SerializedName("content")
        var content: String? = null
        @SerializedName("gender")
        var gender: String? = null
        @SerializedName("id")
        var id: String? = null
        @SerializedName("is_follow")
        var isFollow: String? = null
        @SerializedName("is_friend")
        var isFriend: String? = null
        @SerializedName("is_shared_post")
        var isSharedPost: Int? = null
        @SerializedName("is_user_like")
        var isUserLike: String? = null
        @SerializedName("is_user_share")
        var isUserShare: String? = null
        @SerializedName("like")
        var like: String? = null
        @SerializedName("media")
        var media: List<Medium>? =
            null
        @SerializedName("media_type")
        var mediaType: String? = null
        @SerializedName("original_post_club_id")
        var originalPostClubId: String? = null
        @SerializedName("original_post_club_image")
        var originalPostClubImage: String? = null
        @SerializedName("original_post_club_name")
        var originalPostClubName: String? = null
        @SerializedName("original_post_time")
        var originalPostTime: String? = null
        @SerializedName("original_post_user")
        var originalPostUser: String? = null
        @SerializedName("original_post_user_gender")
        var originalPostUserGender: String? = null
        @SerializedName("original_post_user_id")
        var originalPostUserId: String? = null
        @SerializedName("original_post_user_image")
        var originalPostUserImage: String? = null
        @SerializedName("posted_time")
        var postedTime: String? = null
        @SerializedName("posting")
        var posting: String? = null
        @SerializedName("privacy")
        var mPrivacy: ArrayList<Privacy>? = arrayListOf()
        @SerializedName("share")
        var share: String? = null
        @SerializedName("share_post_content")
        var sharePostContent: String? = null
        @SerializedName("total_comments")
        var totalComments: String? = null
        @SerializedName("total_likes")
        var totalLikes: String? = null
        @SerializedName("total_shares")
        var totalShares: String? = null
        @SerializedName("user_id")
        var userId: String? = null
        @SerializedName("user_image")
        var userImage: String? = null
        @SerializedName("username")
        var username: String? = null

        @SerializedName("source")
        var source: String? = null

    }

    class Medium : Serializable {
        @SerializedName("Icon")
        var icon: String? = null
        @SerializedName("media_type")
        var mediaType: String? = null
        @SerializedName("Mini")
        var mini: String? = null
        @SerializedName("Original")
        var original: String? = null
        @SerializedName("Original_height")
        var originalHeight: String? = null
        @SerializedName("Original_width")
        var originalWidth: String? = null
        @SerializedName("Thumb")
        var thumb: String? = null

    }

    class Privacy : Serializable {
        @SerializedName("comment_on_frnds_posts")
        var commentOnFrndsPosts: String? = ""
        @SerializedName("comment_on_public")
        var commentOnPublic: String = ""
        @SerializedName("id")
        var id: String? = ""
        @SerializedName("user_id")
        var userId: String? = ""
        @SerializedName("view_your_posts_frnds")
        var viewYourPostsFrnds: String? = ""
        @SerializedName("view_your_profile")
        var viewYourProfile: String? = ""

    }
}