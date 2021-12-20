package com.truelife.app.fragment.notification.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response

class NotificationModel : Response() {

    @SerializedName("notification_info")
    var notification: ArrayList<NotificationModel>? = null

    @SerializedName("accept_request_info")
    var acceptRequestInfo: ArrayList<AcceptRequestInfo>? = null

    @SerializedName("invites_notification_info")
    var invitesNotificationInfo: ArrayList<InvitesNotificationInfo>? = null

    @SerializedName("club_accept_info")
    var clubAcceptInfo: ArrayList<ClubAcceptInfo>? = null

    @SerializedName("club_request_info")
    var clubRequestInfo: ArrayList<ClubRequestInfo>? = null

    @SerializedName("friend_request_info")
    var friendRequestInfo: ArrayList<FriendRequestInfo>? = null

    @SerializedName("id")
    var id: String? = null

    @SerializedName("user_id")
    var userId: String? = null

    @SerializedName("username")
    var userName: String? = null

    @SerializedName("user_image")
    var userImage: String? = null


    @SerializedName("post_id")
    var postId: String? = null

    @SerializedName("type")
    var type: String? = null


    var invitationId: String? = null

    var clubName: String? = null

    var clubAdmin: String? = null

    var clubId: String? = null


    @SerializedName("notification")
    var notificationText: String? = null

    @SerializedName("created")
    var created: String? = null

    @SerializedName("modified")
    var modified: String? = null

    class AcceptRequestInfo {

        @SerializedName("id")
        var id: String? = null

        @SerializedName("user_id")
        var user_id: String? = null

        @SerializedName("username")
        var userName: String? = null

        @SerializedName("user_image")
        var userImage: String? = null

        @SerializedName("notification")
        var notification: String? = null

        @SerializedName("created")
        var created: String? = null
    }


    class InvitesNotificationInfo {

        @SerializedName("invite_id")
        var inviteId: String? = null

        @SerializedName("user_id")
        var user_id: String? = null

        @SerializedName("username")
        var userName: String? = null

        @SerializedName("user_image")
        var userImage: String? = null

        @SerializedName("club_id")
        var clubId: String? = null

        @SerializedName("club_name")
        var clubName: String? = null

        @SerializedName("club_admin")
        var clubAdmin: String? = null

        @SerializedName("type")
        var type: String? = null


        @SerializedName("notification")
        var notification: String? = null

        @SerializedName("created")
        var created: String? = null

        @SerializedName("modified")
        var modified: String? = null
    }


    class ClubAcceptInfo {

        @SerializedName("id")
        var id: String? = null

        @SerializedName("user_id")
        var user_id: String? = null

        @SerializedName("username")
        var userName: String? = null

        @SerializedName("user_image")
        var userImage: String? = null

        @SerializedName("club_id")
        var clubId: String? = null

        @SerializedName("type")
        var type: String? = null

        @SerializedName("notification")
        var notification: String? = null

        @SerializedName("created")
        var created: String? = null

        @SerializedName("modified")
        var modified: String? = null
    }


    class ClubRequestInfo {

        @SerializedName("id")
        var id: String? = null

        @SerializedName("user_id")
        var user_id: String? = null

        @SerializedName("username")
        var userName: String? = null

        @SerializedName("user_image")
        var userImage: String? = null

        @SerializedName("club_id")
        var clubId: String? = null

        @SerializedName("type")
        var type: String? = null

        @SerializedName("notification")
        var notification: String? = null

        @SerializedName("created")
        var created: String? = null

        @SerializedName("modified")
        var modified: String? = null
    }

    class FriendRequestInfo {

        @SerializedName("id")
        var id: String? = null

        @SerializedName("user_id")
        var user_id: String? = null

        @SerializedName("username")
        var userName: String? = null

        @SerializedName("user_image")
        var userImage: String? = null

        @SerializedName("notification")
        var notification: String? = null

        @SerializedName("created")
        var created: String? = null
    }
}