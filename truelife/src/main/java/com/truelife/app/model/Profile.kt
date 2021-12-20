package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response
import java.io.Serializable

/**
 * Created by Elango on 27/01/19.
 **/

class Profile : Response(), Serializable {

    @SerializedName("user_details")
    val userdetails: Profile? = null

    @SerializedName("chat_tone")
    val chatTone: String? = null

    @SerializedName("country_id")
    val countryId: String? = null

    @SerializedName("current_city_id")
    val currentCityId: String? = null

    @SerializedName("education")
    val education: String? = null

    @SerializedName("first_name")
    val firstName: String? = null

    @SerializedName("fullname")
    val fullname: String? = null

    @SerializedName("gender")
    val gender: String? = null

    @SerializedName("home_town")
    val homeTown: String? = null

    @SerializedName("is_messenger_mobile_verified")
    val isMessengerMobileVerified: String? = null

    @SerializedName("is_mobile_verified")
    val isMobileVerified: String? = null

    @SerializedName("last_login")
    val lastLogin: String? = null

    @SerializedName("last_name")
    val lastName: String? = null

    @SerializedName("mobile_number")
    val mobileNumber: String? = null

    @SerializedName("pincode")
    val mPincode: String? = null

    @SerializedName("profession")
    val profession: String? = null

    @SerializedName("profile_icon")
    val profileIcon: String? = null

    @SerializedName("profile_image")
    val profileImage: String? = null

    @SerializedName("profile_mini")
    val profileMini: String? = null

    @SerializedName("state_id")
    val stateId: String? = null

    @SerializedName("status")
    val status: String? = null

    @SerializedName("street_name")
    val streetName: String? = null

    @SerializedName("unread_count")
    val unreadCount: String? = null

    @SerializedName("id")
    val userId: String? = null

    @SerializedName("cover_photo")
    val coverPhoto: String? = null

    @SerializedName("lives_now")
    val livesNow: String? = null


    @SerializedName("total_friends")
    val totalFriends: String? = null

    @SerializedName("frds_more")
    val friendsMore: String? = null


    @SerializedName("total_followers")
    val totalFollowers: String? = null

    @SerializedName("followers_more")
    val followersMore: String? = null

    @SerializedName("total_follows")
    val totalFollows: String? = null

    @SerializedName("following_more")
    val followingMore: String? = null

    @SerializedName("total_photos")
    val totalPhotos: String? = null

    @SerializedName("photos_more")
    val photosMore: String? = null

    @SerializedName("is_follow")
    var isFollow: String? = null

    @SerializedName("is_friend")
    var is_friend: String? = null

    @SerializedName("is_blocked")
    val isBlocked: String? = null

    @SerializedName("user_blocked")
    val userBlocked: String? = null

    @SerializedName("friends_details")
    val friendsDetails: ArrayList<FriendsDetails>? = null

    @SerializedName("followers_details")
    val followersDetails: ArrayList<FollowersDetails>? = null

    @SerializedName("following_details")
    val followingDetails: ArrayList<FollowingDetails>? = null

    @SerializedName("photos_list")
    val photoList: ArrayList<PhotoList>? = null

    @SerializedName("college_info")
    val collegeInfo: ArrayList<CollegeInfo>? = null

    @SerializedName("school_info")
    val schoolInfo: ArrayList<SchoolInfo>? = null

    @SerializedName("privacy")
    val privacy: Privacy? = null


    class FriendsDetails {

        @SerializedName("id")
        val id: String? = null

        @SerializedName("user_name")
        val userName: String? = null

        @SerializedName("profile")
        val profile: String? = null

        @SerializedName("gender")
        val gender: String? = null
    }

    class FollowersDetails {

        @SerializedName("id")
        val id: String? = null

        @SerializedName("user_name")
        val userName: String? = null

        @SerializedName("profile")
        val profile: String? = null

        @SerializedName("gender")
        val gender: String? = null
    }

    class FollowingDetails {

        @SerializedName("id")
        val id: String? = null

        @SerializedName("user_name")
        val userName: String? = null

        @SerializedName("profile")
        val profile: String? = null

        @SerializedName("gender")
        val gender: String? = null
    }

    class PhotoList {

        @SerializedName("photos")
        val photos: String? = null

    }

    class CollegeInfo {

        @SerializedName("id")
        val id: String? = null

        @SerializedName("user_id")
        val userId: String? = null

        @SerializedName("college")
        val college: String? = null

        @SerializedName("college_from")
        val collegeFrom: String? = null

        @SerializedName("college_to")
        val collegeTo: String? = null

        @SerializedName("location")
        val location: String? = null
    }

    class SchoolInfo {

        @SerializedName("id")
        val id: String? = null

        @SerializedName("user_id")
        val userId: String? = null

        @SerializedName("school")
        val school: String? = null

        @SerializedName("school_from")
        val schoolFrom: String? = null

        @SerializedName("school_to")
        val schoolTo: String? = null

        @SerializedName("location")
        val location: String? = null
    }

    class Privacy {

        @SerializedName("id")
        val id: String? = null

        @SerializedName("user_id")
        val userId: String? = null

        @SerializedName("view_your_profile")
        val viewYourProfile: String? = null

        @SerializedName("view_your_posts_frnds")
        val viewYourPostsFrnds: String? = null

        @SerializedName("comment_on_public")
        val commentOnPublic: String? = null

        @SerializedName("comment_on_frnds_posts")
        val commentOnFrndsPosts: String? = null

    }

    class Photos : Response(), Serializable {

        @SerializedName("total_page")
        var total_page = ""

        @SerializedName("user_photos")
        var mData = ArrayList<Photos>()

        @SerializedName("id")
        var id = ""

        @SerializedName("Thumb")
        var thumb = ""

        @SerializedName("Original")
        var original = ""
    }
}
