package com.truelife.app.model

import com.google.gson.annotations.SerializedName

class MediaTypeModel {

    @SerializedName("id")
    var id: String? = null

    @SerializedName("path")
    var path: String? = null

    @SerializedName("resolution")
    var resolution: String? = null

    @SerializedName("media_type")
    var media_type: String? = null

}