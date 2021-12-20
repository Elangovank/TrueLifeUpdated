package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response
import java.io.Serializable

class LocationModel : Response(), Serializable {

    @SerializedName("location_list")
    var mLocation: ArrayList<LocationModel>? = null;

    @SerializedName("id")
    var id: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("code")
    var code: String? = null


}
