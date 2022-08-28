package com.truelife.app.constants

import android.Manifest
import java.util.regex.Pattern

/**
 * Created by Elango on 28/01/19.
 **/

object TLConstant {


     val URL_PATTERN: Pattern = Pattern.compile(
        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
        Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
    )
    val POST_TEXT_LIMIT = 350
    val IMAGE_FILE_SIZE_LIMIT_3MB = 3072
    val IMAGE_FILE_SIZE_LIMIT_5MB = 5120
    val IMAGE_FILE_SIZE_LIMIT_8MB = 8192

    val IMAGE_QUALITY_80 = 80
    val IMAGE_QUALITY_70 = 70
    val IMAGE_QUALITY_60 = 60
    val TEMP_DIR = "/Temp/"
    val APP_DIR = "Truelife"
    val COMPRESSED_IMAGES_DIR = "/Images/"
    internal var PREF_FILE_NAME = "DocApp_Patient"

    var SERVER_NOT_REACH = "Server Not Reachable"

    // App Screen Titles
    var DASHBOARD_TITLE = "Dashboard"
    var GALLERY_TITLE = "Gallery"
    var ABOUT_TITLE = "About Us"
    var CONTACT_TITLE = "Contact Us"
    var ISO_TITLE = "ISO"

    var FEED = 0
    var NOTIFICATION = 1
    var ADD = 2
    var CHAT = 3
    var SETTINGS = 4

    var LASTADDEDITEM = -1
    var CHAT_LIST = 0
    var CHAT_FRIENDS = 1
    var CHAT_CONTACTS = 2
    var CHAT_WIFI = 3

    var MALE = "1"
    var FEMALE = "2"

    var IMAGE = 1
    var VIDEO = 2
    var AUDIO = 3
    var FILES = 4

    // Date/Time Formats
    var DD_MM_YY = "dd-MM-yyyy" // 06-12-1993
    var YY_MM_DD = "yyyy-MM-dd" // 1993-12-06
    var YY_MM_DD_SLASH = "yyyy/MM/dd" // 1993/12/06
    var DD_MMM_YY = "dd MMM yyyy" // 06 Dec 1993
    var MMM_YYYY = "MMM yyyy" // Dec 1993
    var DD_MMM_YY_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" // 2019-03-07T16:00:00.000-05:00
    var HH_MM = "HH:mm" // 20:30
    var HH_MM_AA = "hh:mm aa" // 10:30 AM

    var DATE_FORMAT_FROM_SERVER = "yyyy-MM-dd hh:mm:ss"
    var DATE_FORMAT_FROM_SERVER_1 = "MMM dd, yyyy @ hh:mm aa"
    var DATE_FORMAT_FROM_SERVER_CURRENT_DAY = " @ hh:mm a"

    var CAMERA = Manifest.permission.CAMERA
    var WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    var READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    var RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
    var MODIFY_AUDIO_SETTINGS = Manifest.permission.MODIFY_AUDIO_SETTINGS
//    var CONTACTS = Manifest.permission.READ_CONTACTS

    // 1 - PublicFeed, 2 - Friends feed, 3 - Club feed
    var SourceType: String = "SourceType"
    var Video_Play_State: String = "VideoPlayState"
    var SelectedClubList: String = "selectedclublist"
    var VIDEO_SEEK_POSITION_PUBLIC: String = "video_seek_position_public"
    var VIDEO_SEEK_POSITION_FRIENDS: String = "video_seek_position_friends"
    var VIDEO_SEEK_POSITION_CLUB: String = "video_seek_position_club"
    var VIDEO_SEEK_POSITION_PROFILE: String = "video_seek_position_profile"
    var VIDEO_SEEK_POSITION_CLUB_DETAIL: String = "video_seek_position_club_detail"
    var VIDEO_SEEK_POSITION_SEARCH: String = "video_seek_position_search"

    // Push Notification
    var PUSH_FEED_TYPE = "share"
    var PUSH_CHAT_TYPE = "chat_message"
    var PUSH_DASHBOARD = "dashboard"

    const val SELECTED_COUNTRY_CODE ="SELECTED_COUNTRY_CODE"
    const val SELECTED_MOBILE_NUMBER ="SELECTED_MOBILE_NUMBER"
}
