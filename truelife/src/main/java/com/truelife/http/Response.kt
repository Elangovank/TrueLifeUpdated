package com.truelife.http

import androidx.room.Ignore
import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by Elango on 28/01/19.
 **/

open class Response : Serializable {

    @Ignore
    @SerializedName("response")
    var response: Response? = null

    @Ignore
    @SerializedName("response_code")
    var responseCode: Int? = null

    @SerializedName("total_pages")
    var totalPages: Int = 0

    @Ignore
    @SerializedName("response_msg")
    var responseMessage: String? = null

    @Ignore
    @SerializedName("is_user_active")
    var isUserActive: String? = null
    /**
     * unique integer number for the request
     */
    @SerializedName("a1")
    var requestType: Int? = null




    @SerializedName("a2")
    var extraOutput: String? = null

    /**
     * @return true if the response gets success id
     */
    val isSuccess: Boolean
        get() = responseCode == 1

}
