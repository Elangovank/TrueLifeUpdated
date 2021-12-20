package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response

class AccountSettingModel : Response() {

    @SerializedName("account_settings")
    var accountSetting: AccountSettingModel? = null


    @SerializedName("first_name")
    var firstName: String? = null

    @SerializedName("user_id")
    var userId: String? = null

    @SerializedName("last_name")
    var lastName: String? = null

    @SerializedName("email")
    var email: String? = null

    @SerializedName("username")
    var userName: String? = null

    @SerializedName("mobile_number")
    var mobileNumber: String? = null

    @SerializedName("gender")
    var gender: String? = null

}