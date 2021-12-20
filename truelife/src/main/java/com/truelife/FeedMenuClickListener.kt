package com.truelife

import com.truelife.app.model.PublicFeedModel

interface FeedMenuClickListener {

    fun EditThisPost(feedList: PublicFeedModel.FeedList)
    fun HideThisPost(feedList: PublicFeedModel.FeedList)
    fun DeleteThisPost(feedList: PublicFeedModel.FeedList)
    fun ReportThisPost(feedList: PublicFeedModel.FeedList)
    fun BlockThisPost(feedList: PublicFeedModel.FeedList)
    fun FollowThisPost(feedList: PublicFeedModel.FeedList)
}