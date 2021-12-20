package com.truelife.app.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.truelife.BuildConfig
import com.truelife.FeedMenuClickListener
import com.truelife.R
import com.truelife.api.AppServices
import com.truelife.app.VideoPlayerRecyclerAdapter
import com.truelife.app.VideoPlayerRecyclerView
import com.truelife.app.activity.FeedEditActivity
import com.truelife.app.activity.ProfileActivity
import com.truelife.app.activity.TLDashboardActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.fragment.feed.FragmentLifeCycleInterface
import com.truelife.app.listeners.Feedistener
import com.truelife.app.model.LikeList
import com.truelife.app.model.PublicFeedModel
import com.truelife.app.model.User
import com.truelife.base.BaseFragment
import com.truelife.http.Response
import com.truelife.http.ResponseListener
import com.truelife.storage.LocalStorageSP
import com.truelife.util.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI


class TLFriendsFeedFragment : BaseFragment(), ResponseListener,
    FragmentLifeCycleInterface, Feedistener, FeedClickListener, FeedMenuClickListener {
    lateinit var mRecyclerView: VideoPlayerRecyclerView
    var mView: View? = null
    lateinit var mContext: FragmentActivity
    lateinit var mList: ArrayList<PublicFeedModel.FeedList>
    lateinit var mAdapter: VideoPlayerRecyclerAdapter
    private var mSwipeRefresh: SwipeRefreshLayout? = null
    var mWebSocketClient: WebSocketClient? = null
    lateinit var mUser: User
    var mPage: Int = 1
    var mTotalPages = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tlfriends_feed, container, false)
        mRecyclerView = view.findViewById(R.id.publicRv)
        mSwipeRefresh = view.findViewById(R.id.swipeRefereshPublic)
        mView = view
        mContext = activity!!
        init(mView!!)
        LocalBroadcastManager.getInstance(mContext)
            .registerReceiver(
                myBroadcastReceiver,
                IntentFilter(TLConstant.VIDEO_SEEK_POSITION_FRIENDS)
            )
        return view
    }


    override fun onResume() {
        super.onResume()
        // AppDialogs.showToastDialog(mContext,"Friends Resume")


        LocalStorageSP.put(mContext, TLConstant.SourceType, "2")
        initSocket()
        if ((mRecyclerView.isVideoViewAdded)) {
            mRecyclerView.startPlayer()

        }
        /*if( LocalStorageSP.get(mContext, TLConstant.SourceType, "0")!!.equals("2")){
            mRecyclerView.smoothScrollBy(0,1)
            AppDialogs.showToastDialog(mContext,"Friends Resume scroll")
        }*/

    }


    private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            try {
                if (intent.action.equals(TLConstant.VIDEO_SEEK_POSITION_FRIENDS)) {
                    val bundle = intent.extras
                    if (bundle!!.containsKey(TLConstant.VIDEO_SEEK_POSITION_FRIENDS)) {
                        var pos: Long = 0
                        pos = bundle.getLong(TLConstant.VIDEO_SEEK_POSITION_FRIENDS, 0)
                        try {
                            mRecyclerView.setPlayPostion(pos)
                        } catch (e: Exception) {
                            print(e.toString())
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun initSocket() {
        val uri = URI(BuildConfig.SOCKET_URL)
        //var isFound = false

        mWebSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake) {
                Log.i("Socket onOpen --> ", handshakedata.toString())
            }

            override fun onMessage(message: String) {


                /*   if (mWebSocketClient == null || !mWebSocketClient!!.connection.isOpen)
                       return
               */
                // Log.i("Friends Socket  --> ", message)

                //  if (message.isNotEmpty() && mList.isNotEmpty()) {


                // Update User Messages
                val obj = JSONObject(message)
                if (obj.has("feed_list")) {


                    val mValue =
                        Gson().fromJson(message, PublicFeedModel.FeedList::class.java)

                    if (!mValue.data.isNullOrEmpty()) {


                        if (mValue.data!![0].source.equals("2") && !mValue.data!![0].source.isNullOrEmpty()) {

                            for (i in mList.indices) {
                                if (mList[i].id.equals(mValue.data!![0].id)) {

                                    val oldlike = mList[i].isUserLike
                                    mList.removeAt(i)
                                    mValue.data!![0].isUserLike = oldlike
                                    mList.add(i, mValue.data!![0])

                                    mContext.runOnUiThread {
                                        mRecyclerView.setMediaObjects(mList)
                                        mAdapter.notifyDataSetChanged()

                                    }
                                    // isFound = true
                                    break
                                } else {
                                    if (i == (mList.size - 1)) {
                                        mList.add(0, mValue.data!![0])
                                        mContext.runOnUiThread {
                                            mRecyclerView.setMediaObjects(mList)
                                            mAdapter.notifyDataSetChanged()
                                            mRecyclerView.smoothScrollBy(0, 2)

                                        }
                                    }
                                }
                            }

                            /* if (!isFound) {
                                 Log.e("isFound", "found")
                                 mList.add(0, mValue.data!![0])
                             }*/


                        }
                    }

                    //   initSocket()
                }
//}
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                Log.i("Socket onClose --> ", reason)
            }

            override fun onError(ex: Exception) {
                Log.i("Socket onError --> ", ex.printStackTrace().toString())
            }
        }

        mWebSocketClient!!.connect()


    }

    private fun disconnectSocket() {
        if (mWebSocketClient != null && mWebSocketClient!!.connection.isOpen) {
            mWebSocketClient!!.connection.close()
            mWebSocketClient!!.close()
        }
    }


    override fun onBackPressed() {

    }

    override fun onResumeFragment() {

    }

    fun loadData() {

        val mCase = getFeedCaseInfo(
            "2", mUser.mUserId!!,
            mUser.mCountryId!!, mUser.mStateId!!, mUser.mCurrentCityId!!, mPage.toString(), "0", ""
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            mContext, this,
            result,
            Request.Method.POST,
            AppServices.API.publicFeed,
            PublicFeedModel::class.java
        )
    }


    private fun getFeedCaseInfo(
        Type: String,
        aUserId: String,
        aCountryId: String,
        aStateId: String,
        aCurrentCityId: String,
        aPage: String,
        aSearchUserId: String,
        aPincode: String
    ): String? {
        var aCaseStr = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("source", Type)
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("country_id", aCountryId)
            jsonParam1.put("state_id", aStateId)
            jsonParam1.put("current_city_id", aCurrentCityId)
            jsonParam1.put("page", aPage)
            jsonParam1.put("search_user_id", aSearchUserId)
            jsonParam1.put("pincode", aPincode)
            val jsonParam = JSONObject()
            jsonParam.put("NewsFeeds", jsonParam1)
            Log.e("NewsFeeds", "" + jsonParam.toString())
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
            Log.e("NewsFeeds", " $aCaseStr")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    override fun init(view: View) {
        val layoutManager = LinearLayoutManager(this.context)
        mList = ArrayList()
        mUser = LocalStorageSP.getLoginUser(mContext);
        mRecyclerView.setLayoutManager(layoutManager)
        mRecyclerView.setMediaObjects(mList)
        mRecyclerView.setInterface(this)
        mAdapter = VideoPlayerRecyclerAdapter(mList, initGlide()!!, mContext, this, "", "2")
        mRecyclerView.setAdapter(mAdapter)
        AppDialogs.swipeRefColor(mContext, mSwipeRefresh!!)
        mSwipeRefresh!!.setOnRefreshListener {
            mList.clear()
            mTotalPages = 0
            mPage = 1
            loadData()
        }

        val listener = object : EndlessRecyclerViewScrollListener(layoutManager, 5) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                try {
                    if (mPage < mTotalPages) {
                        mAdapter.showBottomLoading()
                        mPage++
                        loadData()
                    } else {
                        mAdapter.stopBottomLoading()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

        mRecyclerView.addOnScrollListener(listener)

        loadData()
    }

    override fun initBundle() {

    }

    override fun clickListener() {

    }

    private fun initGlide(): RequestManager? {
        val options: RequestOptions = RequestOptions()
            .placeholder(R.drawable.bg_border_view_white)
            .error(R.drawable.bg_border_view_white)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
        return Glide.with(this)
            .setDefaultRequestOptions(options)
    }

    override fun onPause() {
        super.onPause()
        mRecyclerView.pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRecyclerView.releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mRecyclerView.releasePlayer()
        disconnectSocket()
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(myBroadcastReceiver)
    }

    override fun onResponse(r: Response?) {

        AppDialogs.hideProgressDialog()
        mSwipeRefresh!!.isRefreshing = false
        if (r != null) {
            if (r.requestType!! == AppServices.API.publicFeed.hashCode()) {
                if (r.response!!.isSuccess) {
                    mTotalPages = r.totalPages
                    mList.addAll((r as PublicFeedModel).feedList!!)
                    mRecyclerView.setMediaObjects(mList)
                    if (mPage == 1) {
                        mAdapter.notifyDataSetChanged()
                        mRecyclerView.smoothScrollBy(0, 1)
                    } else {
                        mAdapter.notifyItemChanged(mList.size - 10)
                    }

                    /* if(mPage==1) {
                         if( LocalStorageSP.get(mContext, TLConstant.SourceType, "0")!!.equals("2")){
                             mRecyclerView.smoothScrollBy(0,2)
                         }*/
                    // mRecyclerView.smoothScrollBy(0,2)
                    // }
                } else AppDialogs.showToastDialog(mContext, r.response!!.responseMessage!!)
            } else if (r.requestType!! == AppServices.API.likedetails.hashCode()) {
                if (r.response!!.isSuccess) {
                    val value = r as LikeList

                    AppDialogs.showLikeList(
                        mContext,
                        "",
                        value.mDataList,
                        object : LikeListAdapter.Callback {
                            override fun info(position: Int, text: String) {

                                val aIntent =
                                    Intent(mContext, ProfileActivity::class.java).putExtra(
                                        "userid",
                                        text
                                    )
                                mContext.startActivity(aIntent)
                            }

                        },
                        true,
                        true
                    )


                }
            }
        } else AppDialogs.customOkAction(
            mContext,
            null,
            TLConstant.SERVER_NOT_REACH,
            null,
            null,
            false
        )
    }

    override fun onFragmentInPagerResume() {
        try {
            if (mRecyclerView.isVideoViewAdded)
                mRecyclerView.startPlayer()
        } catch (e: Exception) {
        }
    }

    override fun onFragmentInPagerPause() {
        try {
            mRecyclerView.pausePlayer()
        } catch (e: Exception) {
        }
    }

    override fun volumeStateChange(isMute: Boolean) {
        mAdapter.changeVolumeState(isMute)
    }

    private fun postLikeString(
        aUserId: String,
        aPostId: String,
        aLikeStatus: String,
        aSourceType: String,
        aLikeType: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("post_id", aPostId)
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("like", aLikeStatus)
            jsonParam1.put("like_type", aLikeType)
            jsonParam1.put("share", "0")
            //   if(aLikeType.equals( "1" )){
            jsonParam1.put("level", "")
            jsonParam1.put("source", aSourceType)
            //  }
            val jsonParam = JSONObject()
            jsonParam.put("LikeShare", jsonParam1)
            Log.e("PostLike", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    override fun like_details(position: Int) {
        AppDialogs.showProgressDialog(mContext)
        AppServices.postLikeDetailsString(mList[position].id!!, mContext, this, mUser.mUserId!!)
    }

    override fun like_click(position: Int, isLike: Boolean) {
        var like: String = ""
        if (isLike)
            like = "1"
        else
            like = "0"


        val mCase = postLikeString(
            mUser.mUserId!!,
            mList[position].id!!,
            like,
            "", "1"
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            mContext, this,
            result,
            Request.Method.POST,
            AppServices.API.publicFeed,
            PublicFeedModel::class.java
        )
    }

    override fun share_click(position: Int, imgView: ImageView) {
        (mContext as TLDashboardActivity).showShareMenu(mList[position], imgView)
    }

    override fun share_click(position: Int) {

    }

    /* override fun share_click(position: Int) {
         (mContext as TLDashboardActivity).showShareMenu(mList[position])
     }
 */
    override fun comment_click(position: Int) {
        (mContext as TLDashboardActivity).openFeedDetail(mList[position])
    }

    override fun menu_click(position: Int) {

        (mContext as TLDashboardActivity).showFeedMenu(this, mList[position])
    }

    override fun video_preview(
        position: Int,
        isRecycler: Boolean,
        isVideo: Boolean,
        focus_pos: Int
    ) {
        if (isVideo)
            (mContext as TLDashboardActivity).openVideoPreview(
                mList[position].media!![0].original,
                mRecyclerView.getPlayPosition(), TLConstant.VIDEO_SEEK_POSITION_FRIENDS
            )
        else
            (mContext as TLDashboardActivity).openImagePreview(mList[position], focus_pos)
    }

    override fun EditThisPost(feedList: PublicFeedModel.FeedList) {

        val intent = Intent(mContext, FeedEditActivity::class.java)
        intent.putExtra("feed", feedList)
        startActivity(intent)

    }

    override fun HideThisPost(feedList: PublicFeedModel.FeedList) {
        val mCase = getFeedHidePostCaseString(
            mUser.mUserId!!,
            feedList.id!!
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            mContext, object : ResponseListener {
                override fun onResponse(r: Response?) {
                    if (r != null) {
                        if (r.response!!.isSuccess) {
                            mList.remove(feedList)
                            mAdapter.update(mList)
                            mRecyclerView.smoothScrollBy(0, 1)
                            Toast.makeText(mContext, "Post successfully hidden", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            },
            result,
            Request.Method.POST,
            AppServices.API.hide_post,
            Response::class.java
        )


    }

    override fun DeleteThisPost(feedList: PublicFeedModel.FeedList) {

        AppDialogs.customDoubleAction(
            mContext,
            "",
            "Are you sure want to delete this post?",
            "Yes",
            "No",
            object : AppDialogs.OptionListener {
                override fun no() {

                }

                override fun yes() {
                    val mCase = getFeedDeletePostCaseString(
                        feedList.id!!
                    )
                    val result =
                        Helper.GenerateEncrptedUrl(
                            BuildConfig.API_URL,
                            mCase!!
                        )
                    Log.e("URL", result)
                    AppServices.execute(
                        mContext, object : ResponseListener {
                            override fun onResponse(r: Response?) {
                                if (r != null) {
                                    if (r.response!!.isSuccess) {

                                        mList.remove(feedList)
                                        mAdapter.update(mList)
                                        mRecyclerView.smoothScrollBy(0, 1)
                                        Toast.makeText(
                                            mContext,
                                            "Post successfully deleted",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        },
                        result,
                        Request.Method.POST,
                        AppServices.API.delete_post,
                        Response::class.java
                    )

                }

            },
            false,
            false
        )

    }

    override fun ReportThisPost(feedList: PublicFeedModel.FeedList) {

        /*val intent = Intent(mContext, ReportProblemActivity::class.java)
        intent.putExtra("post_id", feedList.id)
        intent.putExtra("FromScreen", "feed_list")
        startActivity(intent)*/

        val fm = mContext.supportFragmentManager
        val dialogFragment = ReportDailogFragment()
        val args: Bundle? = Bundle()
        args?.putString("post_id", feedList.id);
        args?.putString("FromScreen", "feed_list");
        dialogFragment.setArguments(args)
        dialogFragment.show(fm, "ReportDailogFragment")

    }

    override fun BlockThisPost(feedList: PublicFeedModel.FeedList) {

        val mCase = getSettingBlockFriendMemberString(
            mUser.mUserId!!,
            feedList.userId!!,
            "1"
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            mContext, object : ResponseListener {
                override fun onResponse(r: Response?) {
                    if (r != null) {
                        if (r.response!!.isSuccess) {
                            Toast.makeText(
                                mContext,
                                r.response!!.responseMessage,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            },
            result,
            Request.Method.POST,
            AppServices.API.hide_post,
            Response::class.java
        )

    }

    override fun FollowThisPost(feedList: PublicFeedModel.FeedList) {
        val status = if (feedList.isFollow.equals("0"))
            "1"
        else "0"

        val mCase = follow_unfollow_case(
            mUser.mUserId!!,
            feedList.userId!!,
            status
        )
        val result =
            Helper.GenerateEncrptedUrl(
                BuildConfig.API_URL,
                mCase!!
            )
        Log.e("URL", result)
        AppServices.execute(
            mContext, object : ResponseListener {
                override fun onResponse(r: Response?) {
                    if (r != null) {
                        if (r.response!!.isSuccess) {
                            Toast.makeText(
                                mContext,
                                r.response!!.responseMessage,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            },
            result,
            Request.Method.POST,
            AppServices.API.hide_post,
            Response::class.java
        )
    }


    private fun follow_unfollow_case(
        aUserId: String,
        aProfileUserId: String,
        aStatus: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("user_id", aUserId)
            jsonParam1.put("follows_id", aProfileUserId)
            jsonParam1.put("follow_status", aStatus)

            val jsonParam = JSONObject()
            jsonParam.put("FollowUnfollow", jsonParam1)
            Log.e("FollowUnfollow", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun getSettingBlockFriendMemberString(
        aLoginUserId: String,
        aFriendId: String,
        aBlockStatus: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", aLoginUserId)
            jsonParam1.put("friend_id", aFriendId)
            jsonParam1.put("block_status", aBlockStatus)
            val jsonParam = JSONObject()
            jsonParam.put("BlockFriend", jsonParam1)
            Log.e("BlockFriend", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun getFeedHidePostCaseString(
        aUserId: String,
        aPostId: String
    ): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("login_user_id", aUserId)
            jsonParam1.put("post_id", aPostId)
            val jsonParam = JSONObject()
            jsonParam.put("HidePost", jsonParam1)
            Log.e("HidePost", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }


    private fun getFeedDeletePostCaseString(aPostId: String): String? {
        var aCaseStr: String? = " "
        try {
            val jsonParam1 = JSONObject()
            jsonParam1.put("post_id", aPostId)
            val jsonParam = JSONObject()
            jsonParam.put("DeletePost", jsonParam1)
            Log.e("DeletePost", " $jsonParam")
            aCaseStr = Base64.encodeToString(jsonParam.toString().toByteArray(), 0)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return aCaseStr
    }

    companion object {
        var TAG: String = TLFriendsFeedFragment::class.java.simpleName

        class MyAsyncTask internal constructor(
            context: Context,
            aList: ArrayList<PublicFeedModel.FeedList>,
            aadapter: VideoPlayerRecyclerAdapter,
            aRec: VideoPlayerRecyclerView,
            msg: String
        ) : AsyncTask<String, String, String?>() {

            private var resp: String? = null

            private var isFound = false

            private var mList = aList

            private var mAdapter = aadapter

            private var mRecyclerView = aRec

            private var message = msg
            override fun onPreExecute() {

            }

            override fun doInBackground(vararg params: String?): String? {

                val obj = JSONObject(message)
                if (obj.has("feed_list")) {


                    val mValue =
                        Gson().fromJson(message, PublicFeedModel.FeedList::class.java)

                    if (!mValue.data.isNullOrEmpty()) {


                        if (mValue.data!![0].source.equals("2") || !mValue.data!![0].source.isNullOrEmpty()) {


                            for (i in mList.indices) {
                                if (mList[i].id.equals(mValue.data!![0].id)) {
                                    // Log.e("Inside For loop", "for loop")
                                    val oldlike = mList[i].isUserLike
                                    mList.removeAt(i)
                                    mValue.data!![0].isUserLike = oldlike
                                    mList.add(i, mValue.data!![0])
                                    // isFound = true
                                    break
                                } else {
                                    mList.add(0, mValue.data!![0])
                                    break
                                }
                            }

                            //  if (!isFound) {
                            //  Log.e("isFound", "found")

                            var bo = false
                            mList.forEach {


                            }


                        }
                    }

                }


                //   Log.e("Asyn",params.get(0)!!)
                return resp
            }


            override fun onPostExecute(result: String?) {
                mRecyclerView.setMediaObjects(mList)
                mAdapter.notifyDataSetChanged()


            }

            override fun onProgressUpdate(vararg text: String?) {


            }
        }
    }
}
