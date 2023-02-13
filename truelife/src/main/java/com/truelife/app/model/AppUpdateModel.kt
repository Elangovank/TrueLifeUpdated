package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response

class AppUpdateModel : Response() {


    @SerializedName("version_code")
    var versionCode: String? = null

    @SerializedName("is_update")
    var isUpdate: String? = null
}