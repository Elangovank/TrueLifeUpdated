package com.truelife.api

import android.content.Context
import android.util.Base64
import android.util.Log
import com.android.volley.Request
import com.truelife.BuildConfig
import com.truelife.app.model.LikeList
import com.truelife.app.model.LocationModel
import com.truelife.app.model.PublicFeedModel
import com.truelife.app.model.User
import com.truelife.http.JsonRestClient
import com.truelife.http.ResponseListener
import com.truelife.http.RestClient
import com.truelife.storage.LocalStorageSP
import com.truelife.util.AppDialogs
import com.truelife.util.Helper
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.lang.reflect.Type


/**
 * Created by Elango on 05/12/19.
 **/

class AppServices {

    object API {

        // No Prefix
        fun constructUrl(urlKey: String): String {
            return String.format("%s%s", BuildConfig.API_URL, urlKey)
        }

        // With Prefix
        fun constructSuffixUrl(c: Context, urlKey: String): String {
            val isProvider = LocalStorageSP[c, "is_provider", false]!!
            val SUFFIX = String.format("api/v1/%s/", if (isProvider) "providers" else "patients")
            return String.format("%s%s%s", BuildConfig.API_URL, SUFFIX, urlKey)
        }

        // WebSocket URL
        fun constructSocketUrl(c: Context): String {
            return String.format(
                "%s%s",
                BuildConfig.SOCKET_URL,
                String.format("cable?token=%s", LocalStorageSP[c, "auth_key", ""]!!)
            )
        }

        // API Case's

        // Login Screen
        val login = "login"
        val gcmRegister = "gcmregister"
        val countryCode = "countrycode"
        val country = "country"
        val otpVerfication = "otpVerfication"
        val forgotPass = "forgotPass"
        val resetPassword = "passwords/send_email"
        val logout = "logout"
        val report = "report"
        val deleteAccount = "deleteAccount"
        val chatList = "ChatUsersList"
        val friendList = "FriendsList"
        val searchNumber = "ChatByNumber"
        val searchFriend = "SearchFriend"
        val register = "register"
        val publicFeed = "public_feed"
        val club_suggestion = "club_suggestion"
        val blockedlist = "blockedlist"
        val blockOrUnblocklist = "blockorUnblocklist"
        val followerslist = "followerslist"
        val clublist = "clublist"
        val setting = "setting"
        val perosnalsetting = "personalsetting"
        val profilesetting = "profilesetting"
        val profilesettingsubmit = "profilesettingsubmit"
        val accountsetting = "accountsetting"
        val savepersonalsetting = "savepersonalsetting"
        val privacysettingsave = "privacysettingsave"
        val chat = "ChatMessage"
        val saveaccountdetails = "saveaccountdetails"
        val savepassworddetails = "savepassworddetails"
        val notificationlist = "notificationlist"
        val addfriend = "addfriend"
        val AddFollow = "AddFollow"
        val clubinvite = "clubinvite"
        val profile = "profile"
        val updateProfile = "UpdateProfile"
        val removeChatList = "removeChatList"
        val blockFriend = "BlockFriend"
        val clearChat = "Clear chat"
        val deleteChat = "Delete Chat"
        val sendTextMsg = "sendTextMsg"
        val OnlineOfflineStatus = "OnlineOfflineStatus"
        val clubfriendsList = "clubfriendsList"
        val feedpost = "feedpost"
        val viewProfile = "ViewMessengerProfile"
        val ClubMedia = "ClubMedia"
        val commentList = "CommentsList"
        val post_comment = "CommentsList"
        val like = "like"
        val hide_post = "hide_post"
        val delete_post = "delete_post"
        val clubdetails = "clubdetails"
        val ClubPublicCategory = "ClubPublicCategory"
        val ClubPublicMembers = "ClubPublicMembers"
        val Invites = "Invites"
        val AddClubMember = "AddClubMember"
        val ClubRequest = "ClubRequest"
        val RemoveClubMember = "RemoveClubMember"
        val ChangeAdministrator = "ChangeAdministrator"
        val ClubListShare = "ClubListShareModel"
        val feeddetails = "feeddetails"
        val PhotosList = "PhotosList"
        val OTPRequest = "OTPRequest"
        val OTPVerify = "OTPVerify"
        val AcceptClubRequest = "AcceptClubRequest"
        val likedetails = "likedetails"
    }

    private interface ApiInterface {

        // ******* Update Family Member ******* //

        @Multipart
        @POST
        fun updateMember(
            @Url url: String,
            @HeaderMap mHeader: HashMap<String, String>,
            @Part file: MultipartBody.Part?,
            @QueryMap mParam: HashMap<String, String>
        ): Call<ResponseBody>

        @POST
        fun updateMember(
            @Url url: String,
            @HeaderMap mHeader: HashMap<String, String>,
            @QueryMap mParam: HashMap<String, String>
        ): Call<ResponseBody>

    }


    companion object {

        private var retrofit: Retrofit? = null

        private fun fillCommons(c: Context, client: RestClient) {
            client.addHeader("Authorization", LocalStorageSP[c, "auth_key", ""]!!)
        }

        private fun fillCommons(c: Context, client: JsonRestClient) {
            client.header("Authorization", LocalStorageSP[c, "auth_key", ""]!!)
            client.header("Content-Type", "application/json")
        }

        private fun getClient(): Retrofit {

            val okHttpClient = OkHttpClient.Builder().build()

            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.API_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit as Retrofit
        }


        fun execute(c: Context, data: String, method: Int, case: String, model: Type): Boolean {
            try {
                // Generating Req
                val obj = JSONObject()

                val client = JsonRestClient(c, method, data, case.hashCode())
                client.header("Content-Type", "application/json; charset=utf-8")
                client.execute(c as ResponseListener, obj, model)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }

        fun execute(
            c: Context,
            listener: ResponseListener,
            data: String,
            method: Int,
            case: String,
            model: Type
        ): Boolean {
            try {
                // Generating Req
                val obj = JSONObject()

                val client = JsonRestClient(c, method, data, case.hashCode())
                client.header("Content-Type", "application/json; charset=utf-8")
                client.execute(listener, obj, model)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }

        fun login(c: Context, data: String): Boolean {
            try {
                // Generating Req
                val obj = JSONObject()

                val client = JsonRestClient(c, Request.Method.POST, data, API.login.hashCode())
                client.header("Content-Type", "application/json; charset=utf-8")
                //client.add("Content-Type", "application/json; charset=utf-8")
                client.execute(c as ResponseListener, obj, User::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }

        fun forgotpass(c: Context, data: String): Boolean {
            try {
                // Generating Req
                val obj = JSONObject()

                val client = JsonRestClient(c, Request.Method.POST, data, API.forgotPass.hashCode())
                client.header("Content-Type", "application/json; charset=utf-8")
                client.execute(c as ResponseListener, obj, User::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }

        fun getCountryStateCityApi(c: Context, data: String) {
            try {
                val obj = JSONObject()
                val client = JsonRestClient(c, Request.Method.POST, data, API.country.hashCode())
                client.header("Content-Type", "application/json; charset=utf-8")
                client.execute(c as ResponseListener, obj, LocationModel::class.java)
            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
        fun postLikeDetailsString(
            aUserId: String,
            aPostId: String
        ): String? {
            var aCaseStr: String? = " "
            try {
                val jsonParam1 = JSONObject()
                jsonParam1.put("post_id", aPostId)
                jsonParam1.put("user_id", aUserId)

                val jsonParam = JSONObject()
                jsonParam.put("LikeList", jsonParam1)
                Log.e("LikeList", " $jsonParam")
                aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return aCaseStr
        }

        fun postLikeDetailsString(
            postId: String,
            mContext: Context,
            listener: ResponseListener,
            userid: String
        ) {
            AppDialogs.showProgressDialog(mContext)
            val mCase = postLikeDetailsString(
                userid,
                postId
            )
            val result =
                Helper.GenerateEncrptedUrl(
                    BuildConfig.API_URL,
                    mCase!!
                )
            Log.e("Like URL", result)
            execute(
                mContext, listener,
                result,
                Request.Method.POST,
                API.likedetails,
                LikeList::class.java
            )
        }
    }


}