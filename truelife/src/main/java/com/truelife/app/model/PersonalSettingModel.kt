package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response

class PersonalSettingModel : Response() {

    @SerializedName("address_details")
    var setting: PersonalSettingModel? = null


    @SerializedName("id")
    var id: String? = null

    @SerializedName("user_id")
    var userId: String? = null

    @SerializedName("door_no")
    var doorNo: String? = null

    @SerializedName("street_name")
    var streetName: String? = null

    @SerializedName("address")
    var address: String? = null

    @SerializedName("home_town")
    var homeTown: String? = null

    @SerializedName("pincode")
    var pinCode: String? = null

    @SerializedName("country_id")
    var countryId: String? = null

    @SerializedName("country_name")
    var countryName: String? = null

    @SerializedName("state_id")
    var stateId: String? = null

    @SerializedName("state_name")
    var stateName: String? = null

    @SerializedName("current_city_id")
    var currentCityId: String? = null

    @SerializedName("current_city_name")
    var currentCityName: String? = null

}

