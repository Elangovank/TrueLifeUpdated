package com.truelife.http

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.truelife.util.AppDialogs
import com.truelife.util.Utility
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.Type
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.exitProcess


/**
 * Created by Elango on 28/01/19.
 */

class JsonRestClient
/**
 * @param c   Context of Application
 * @param m   Request method [Request.Method].Method.POST / others from Request.Method.*
 * @param url Url of the request
 * @param rt  Unique request type from constants. This param may be deprecated in future release.
 */
    (c: Context, m: Int, url: String, rt: Int) : VolleyClient(c, m, url, rt) {

    /**
     * Default is 50 seconds
     *
     * @param timeout Set time out in seconds
     */
    override fun setTimeout(timeout: Int) {
        this.timeout = timeout * 1000
    }

    /**
     * @param key   Header name / key
     * @param value Header value
     */
    fun header(key: String, value: String) {
        Log.i("Headers --> ", "$key-----> $value")
        mHeaders[key] = value
    }


    /**
     * Executes the url request that is preset in constructor
     *
     * @param l            Response Listener which is implemented in the activity
     * @param responseType Response type from one of model default is [Response]
     */
    @Throws(Exception::class)
    fun execute(l: ResponseListener?, json: JSONObject, responseType: Type) {
        this.l = l
        this.responseType = responseType

        try {
            if (Utility.isInternetAvailable(c)) {
                val hurlStack = object : HurlStack() {
                    @Throws(IOException::class)
                    override fun createConnection(url: URL): HttpURLConnection {
                        val connection = super.createConnection(url)
                        Log.i("VolleyClient", "Connection created")
                        return connection
                    }
                }

                val queue = Volley.newRequestQueue(c, hurlStack)

                Log.i("Body ---> ", json.toString())

                val jsonObjectRequest = object : JsonObjectRequest(url, json,
                    Response.Listener { response ->
                        try {
                            if (l != null) {
                                try {
                                    val r = parseResponse(response.toString(), responseType)
                                   // Toast.makeText(c,response.toString(),Toast.LENGTH_LONG).show()
                                    if (r!!.response!!.isUserActive != null) {
                                        if (!r.response!!.isUserActive.equals("1")) {
                                            AppDialogs.customOkAction(
                                                c!!,
                                                null,
                                                "User Not Active",
                                                null,
                                                object : AppDialogs.ConfirmListener {
                                                    override fun yes() {
                                                        AppDialogs.hideProgressDialog()
                                                       // exitProcess(0)
                                                    }

                                                },
                                                false
                                            )
                                            return@Listener
                                        }
                                    }

                                    r.requestType = requestType
                                    r.extraOutput = extraOutput

                                    l.onResponse(r)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                Log.w("RestClient", "Response received but not listened.")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, Response.ErrorListener { error ->
                        try {
                            if (l != null) {
                                val r = Response()
                                r.responseCode = error.networkResponse.statusCode
                                r.responseMessage = String.format("%s\n%s", r.responseCode, error.toString())
                                r.requestType = requestType
                                l.onResponse(r)
                            } else {
                                Log.w("RestClient", "Response received but not listened.")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                ) {

                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        return mHeaders
                    }
                }
                jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                    timeout,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
                queue.add(jsonObjectRequest)
            } else {
                throw ConnectException("No network access detected")
            }
        } catch (e: Exception) {
            Log.e("RestClint", "Exception-------------------\n")
            throw e
        }
    }
}
