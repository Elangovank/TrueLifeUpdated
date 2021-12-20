package com.truelife.app.model

import com.google.gson.annotations.SerializedName
import com.truelife.http.Response
import java.io.Serializable

class ComentsListModel : Response(), Serializable {
    @SerializedName("comments_list")
    var commentsList: ArrayList<CommentsList>? = ArrayList()

    class CommentsList : Serializable {
        @SerializedName("comment")
        var comment: String? = ""
        @SerializedName("comment_date")
        var commentDate: String? = ""
        @SerializedName("gender")
        var gender: String? = ""
        @SerializedName("id")
        var id: String? = ""
        @SerializedName("is_user_like")
        var isUserLike: String? = ""
        @SerializedName("like_count")
        var likeCount: String? = ""
        @SerializedName("comment_count")
        var comment_count: String? = ""
        @SerializedName("sub_comments")
        var subComments: List<Any>? = null
        @SerializedName("user_id")
        var userId: String? = ""
        @SerializedName("user_image")
        var userImage: String? = ""
        @SerializedName("user_name")
        var userName: String? = ""

    }
}