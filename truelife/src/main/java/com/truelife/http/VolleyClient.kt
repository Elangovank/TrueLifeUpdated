package com.truelife.http

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.util.*

/**
 * Created by Elango on 28/01/19.
 **/

abstract class VolleyClient
/**
 * @param c   Context of Application
 * @param method   Request method [Request.Method].Method.POST / others from Request.Method.*
 * @param url Url of the request
 * @param requestType  Unique request type from constants. This param may be deprecated in future release.
 */
internal constructor(
    internal var c: Context?,
    internal var method: Int,
    internal var url: String,
    internal var requestType: Int
) : com.android.volley.Response.ErrorListener {

    // Expremental
    var extraOutput: String? = null
    internal var timeout = 50000 // 50 seconds - time out
    internal var mHeaders: MutableMap<String, String> = HashMap()
    internal var mParams: MutableMap<String, String> = HashMap()
    internal var l: ResponseListener? = null
    internal var responseType: Type = Response::class.java

    init {
        Log.i("Url", url)
    }

    override fun onErrorResponse(error: VolleyError) {
        Log.e("VolleyClient", "ErrorResponse----:  " + error.message)
        if (l != null) {
            val r = Response()
            r.requestType = requestType
            r.responseCode = 404
            r.responseMessage = "Failure! Could not load data"
            //r.customMessage = "Server is not reachable"
            r.extraOutput = extraOutput
            l!!.onResponse(r)
        }
    }

    /**
     * Default is 50 seconds
     *
     * @param timeout Set time out in seconds
     */
    open fun setTimeout(timeout: Int) {
        this.timeout = timeout * 1000
    }

    /**
     * @param key   Param name / key
     * @param value Param value
     */
    fun addParam(key: String, value: String) {
        mParams.put(key, value)
        Log.i("Params --> ", "$key-----> $value")

    }

    /**
     * @param key   Header name / key
     * @param value Header value
     */
    fun addHeader(key: String, value: String) {
        Log.i("Headers --> ", "$key-----> $value")
        mHeaders.put(key, value)
    }

    /**
     * All Collection
     *
     * @param headers Map of Params
     */
    internal fun addHeaders(headers: Map<String, String>) {
        this.mHeaders.putAll(headers)
    }

    companion object {

        /**
         * @param rs    Response String
         * @param model [Type] of Sub class of [Response]
         * @return object of @param model
         */
        @Synchronized
        internal fun parseResponse(rs: String, model: Type): Response? {
            try {

                Log.i("Response", "Response-----------\n$rs")

                val builder = GsonBuilder().serializeNulls()
                val gson = builder.create()
                // converting or parsing the content
                return gson.fromJson<Response>(rs, model)

            } catch (e: JsonParseException) {
                Log.e("Response", "Exception ----------------------------------------------------")
                e.printStackTrace()
                return null
            } catch (e: IllegalStateException) {
                Log.e("Response", "Exception ----------------------------------------------------")
                e.printStackTrace()
                return null
            } catch (e: Exception) {
                Log.e("Response", "Exception ----------------------------------------------------")
                e.printStackTrace()
                return null
            }

        }
    }
}
