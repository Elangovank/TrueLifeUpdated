package com.truelife.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.squareup.picasso.Picasso
import com.truelife.R
import com.truelife.app.activity.YoutubePalyerActivity
import com.truelife.app.activity.otp.TLOTPActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.chat.profile.TLChatProfile
import com.truelife.app.model.PublicFeedModel
import com.truelife.app.viewer.TLWebViewView
import com.truelife.base.TLFragmentManager
import droidninja.filepicker.PickerManager.title
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.experimental.and


/**
 * Created by Elango on 31/01/19.
 **/

object Helper {
    var DATE_FORMAT_FROM_SERVER = "yyyy-MM-dd hh:mm:ss"
    var DATE_FORMAT_FROM_SERVER_1 = "MMM dd, yyyy @ hh:mm a"


    fun getTimeZone(): ArrayList<String>? {
        val timezones = TimeZone.getAvailableIDs()
        val list = ArrayList<String>()

        for (i in timezones.indices) {
            val zone = TimeZone.getTimeZone(timezones[i]).getDisplayName(true, TimeZone.SHORT)
            val name = timezones[i]

            val out = String.format("(%s) %s", zone, name)
            list.add(out)
        }
        return list
    }

    fun isValidEmail(target: String): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    fun isValidMobileNo(target: String): Boolean {
        return !TextUtils.isEmpty(target) && target.length == 10
    }

    /**
     * @param context Context
     */
    fun navigateAppSetting(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }

    /**
     * @param context Context
     */
    fun navigatePlaystore(context: Context) {
        val appPackageName = context.packageName
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun GenerateEncrptedUrl(
        mBaseUrl: String,
        mCase: String
    ): String {
        Log.e("Url",mBaseUrl + URLEncoder.encode(
            getAuthString().toString(),
            "utf-8"
        ) + "&case=" + URLEncoder.encode(mCase, "utf-8"))
        return mBaseUrl + URLEncoder.encode(
            getAuthString().toString(),
            "utf-8"
        ) + "&case=" + URLEncoder.encode(mCase, "utf-8")


    }


    fun getAuthString(): String? {
      //  Log.e("JSON", "sdggsgdsdgsdg")
        val aTimeStamp: String = DateUtil.getCurrentDTForamat(DATE_FORMAT_FROM_SERVER)!!
        var aAuthStr = " "
        try {
            val jsonParam2 = JSONObject()
            jsonParam2.put("user_email", "eid_android_user")
            jsonParam2.put("password_digest", Base64.encodeToString(convertToHex(hashString("androiduser$aTimeStamp"))!!.toByteArray(), 0))
            jsonParam2.put("timestamp", aTimeStamp)
            val jsonParam = JSONObject()
            jsonParam.put("auth2", jsonParam2)
            aAuthStr = jsonParam.toString()
        } catch (e: NoSuchAlgorithmException) {
            Log.e("JSON", "NoSuchAlgorithmException")
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            Log.e("JSON", "UnsupportedEncodingException")
            e.printStackTrace()
        } catch (e: JSONException) {
            Log.e("JSON", "JSONException")
            e.printStackTrace()
        } catch (e: ArrayIndexOutOfBoundsException) {
            Log.e("JSON", "ArrayIndexOutOfBoundsException")
        }
        return aAuthStr
    }

    private fun convertToHex(data: ByteArray): String? {
        val buf = StringBuilder()
        try {
            for (b in data) {
                var halfbyte: Int = b.toInt() ushr 4 and 0x0F
                var two_halfs = 0
                do {
                    buf.append(if (0 <= halfbyte && halfbyte <= 9) ('0'.toInt() + halfbyte).toChar() else ('a'.toInt() + (halfbyte - 10)).toChar())
                    halfbyte = (b and 0x0F).toInt()
                } while (two_halfs++ < 1)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return buf.toString()
    }

    private fun hashString(input: String): ByteArray {
        var sha1hash = ByteArray(0)
        try {
            val md = MessageDigest.getInstance("SHA-1")
            md.update(input.toByteArray(charset("iso-8859-1")), 0, input.length)
            sha1hash = md.digest()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return sha1hash
    }

    fun postTimeConvertFormat(aDate: String?): String? {
        var output = ""
        try {
            val aFormat =
                SimpleDateFormat(DATE_FORMAT_FROM_SERVER)
            val diff =
                SimpleDateFormat(DATE_FORMAT_FROM_SERVER_1)
            var timestamp: Date? = null
            timestamp = aFormat.parse(aDate)
            output = diff.format(timestamp)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return output
    }

    fun getDisplayChatGroupMonthName(aDisplayDate: String?): String? {
        val formatrecived = SimpleDateFormat("yyyy-MM-dd")
        val formarwanted = SimpleDateFormat("MMM dd, yyyy")
        var recived: Date? = null
        try {
            recived = formatrecived.parse(aDisplayDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return formarwanted.format(recived)
    }

    fun navigateChatProfile(mContext: FragmentActivity, id: String?) {
        if (Utility.isInternetAvailable(mContext)) {
            val aBundle = Bundle()
            aBundle.putString("ChatUserId", id)
            TLFragmentManager(mContext).addContent(
                TLChatProfile(),
                TLChatProfile.TAG,
                aBundle
            )
        }
    }

    fun navigateOTPScreen(mContext: FragmentActivity, msg: String, reqCode:Int) {
        if (Utility.isInternetAvailable(mContext)) {
            val intent = Intent(mContext, TLOTPActivity::class.java)
            intent.putExtra("msg", msg)
            mContext.startActivityForResult(intent, reqCode)
        }
    }

    fun isYoutubeUrl(youTubeURl: String): Boolean {
        val pattern: Regex =
            "^(http(s)?:\\/\\/)?((w){3}.)?((m){1}.)?youtu(be|.be)?(\\.com)?\\/.+".toRegex()

        return youTubeURl.trim().matches(pattern)
    }

}


