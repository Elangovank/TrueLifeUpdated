package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response
import java.io.Serializable

/**
 * Created by Kishore on 10/12/19.
 **/

class User : Response(), Serializable {

    @SerializedName("Userdetails")
    var mUserdetails: User? = null

    @SerializedName("chat_tone")
    val mChatTone: String? = null

    @SerializedName("country_id")
    val mCountryId: String? = null

    @SerializedName("current_city_id")
    val mCurrentCityId: String? = null

    @SerializedName("education")
    val mEducation: String? = null

    @SerializedName("first_name")
    val mFirstName: String? = null

    @SerializedName("fullname")
    val mFullname: String? = null

    @SerializedName("gender")
    val mGender: String? = null

    @SerializedName("home_town")
    val mHomeTown: String? = null

    @SerializedName("country_code")
    val countryCode: String? = null

    @SerializedName("is_messenger_mobile_verified")
    var mIsMessengerMobileVerified: String? = null

    @SerializedName("is_mobile_verified")
    var mIsMobileVerified: String? = null

    @SerializedName("last_login")
    val mLastLogin: String? = null

    @SerializedName("last_name")
    val mLastName: String? = null

    @SerializedName("mobile_number")
    val mMobileNumber: String? = null

    @SerializedName("pincode")
    val mPincode: String? = null

    @SerializedName("profession")
    val mProfession: String? = null

    @SerializedName("profile_icon")
    val mProfileIcon: String? = null

    @SerializedName("profile_image")
    val mProfileImage: String? = null

    @SerializedName("profile_mini")
    val mProfileMini: String? = null

    @SerializedName("state_id")
    val mStateId: String? = null

    @SerializedName("status")
    var mStatus: String? = null

    @SerializedName("street_name")
    val mStreetName: String? = null

    @SerializedName("unread_count")
    val mUnreadCount: String? = null

    @SerializedName("user_id")
    val mUserId: String? = null

}
