package com.truelife.app

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.truelife.R
import com.truelife.app.constants.TLConstant
import com.truelife.app.listeners.Feedistener
import com.truelife.app.model.PublicFeedModel.FeedList
import com.truelife.base.TLFragmentManager
import com.truelife.storage.LocalStorageSP

class VideoPlayerRecyclerView : RecyclerView {
    private enum class VolumeState {
        ON, OFF
    }

    // ui

    // private var isVideoPlay = false
    private var thumbnail: ImageView? = null
    private var replayButton: RelativeLayout? = null
    private var volumeControl: ImageView? = null
    private var progressBar: ProgressBar? = null
    private var viewHolderParent: View? = null
    private var frameLayout: FrameLayout? = null
    private var videoSurfaceView: PlayerView? = null
    private var videoPlayer: SimpleExoPlayer? = null
    private var singleImage: ImageView? = null
    private var likeLay: LinearLayout? = null

    // vars
    private var mediaObjects: ArrayList<FeedList> = ArrayList()
    private var mCallback: Feedistener? = null
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    var mContext: Context? = null
    private var playPosition = -1
    public var isVideoViewAdded = false
    private var requestManager: RequestManager? = null

    // controlling playback state
    private var volumeState: VolumeState? = null
    private var mFragActicvity: FragmentActivity? = null
    private var mFragmentManager: TLFragmentManager? = null
    private var targetPosition: Int? = null

    // private var scrollDirection:String? = "up"

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context)
    }

    fun getVisiblePercent(v: View): Int {
        return if (v.isShown) {
            val r = Rect()
            v.getGlobalVisibleRect(r)
            val sVisible = r.width() * r.height().toDouble()
            val sTotal = v.width * v.height.toDouble()
            (100 * sVisible / sTotal).toInt()
        } else {
            -1
        }
    }

    private fun init(context: Context) {
        this.mContext = context.applicationContext
        val display =
            (getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y
        videoSurfaceView = PlayerView(context)
        videoSurfaceView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory: TrackSelection.Factory =
            AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        // 2. Create the player
        videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        // Bind the player to the view.
        videoSurfaceView!!.useController = false
        videoSurfaceView!!.player = videoPlayer
        setVolumeControl(VolumeState.ON)


        addOnScrollListener(object : OnScrollListener() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                try {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == SCROLL_STATE_IDLE) {
                        if (thumbnail != null) { // show the old thumbnail
                            thumbnail!!.visibility = View.VISIBLE
                            volumeControl!!.alpha = 0f
                            pausePlayer()
                        }
                        // There's a special case when the end of the list has been reached.
                        // Need to handle that with this bit of logic
                        //  isVideoPlay = isVisible(videoSurfaceView)
                        //Log.e("video Visibility", bb.toString())
                        if (!recyclerView.canScrollVertically(1)) {
                            playVideo(true)
                        } else {
                            playVideo(false)
                        }
                        Log.d(
                            TAG,
                            "onScrollStateChanged: called."
                        )

                    }
                } catch (e: Exception) {
                }
            }

            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)

                if ((layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == playPosition) {
                    pausePlayer()
                }

                Log.e(
                    "visible item",
                    (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition().toString()
                )
            }
        })
        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {}
            override fun onChildViewDetachedFromWindow(view: View) {
                if (viewHolderParent != null && viewHolderParent == view) {
                    resetVideoView()
                }
            }
        })





        videoPlayer!!.addListener(object : Player.EventListener {
            override fun onTimelineChanged(
                timeline: Timeline,
                manifest: Any?,
                reason: Int
            ) {
            }

            override fun onTracksChanged(
                trackGroups: TrackGroupArray,
                trackSelections: TrackSelectionArray
            ) {
            }

            override fun onLoadingChanged(isLoading: Boolean) {}

            override fun onPlayerStateChanged(
                playWhenReady: Boolean,
                playbackState: Int
            ) {
                LocalStorageSP.put(mContext!!, TLConstant.Video_Play_State, true)
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        replayButton!!.visibility = View.GONE
                        Log.e(
                            TAG,
                            "onPlayerStateChanged: Buffering video."
                        )
                        if (progressBar != null) {
                            progressBar!!.visibility = View.VISIBLE
                        }
                    }
                    Player.STATE_ENDED -> {
                        Log.d(
                            TAG,
                            "onPlayerStateChanged: Video ended."
                        )
                        //videoPlayer!!.seekTo(0)
                        replayButton!!.visibility = View.VISIBLE
                    }
                    Player.STATE_IDLE -> {
                        replayButton!!.visibility = View.GONE
                    }
                    Player.STATE_READY -> {
                        replayButton!!.visibility = View.GONE

                        LocalStorageSP.put(mContext!!, TLConstant.Video_Play_State, false)
                        Log.e(
                            TAG,
                            "onPlayerStateChanged: Ready to play."
                        )
                        if (progressBar != null) {
                            progressBar!!.visibility = View.GONE
                        }
                        if (!isVideoViewAdded) {
                            animateVolumeControl()
                            addVideoView()
                        }
                    }
                    else -> {
                        replayButton!!.visibility = View.GONE
                    }
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {}
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
            override fun onPlayerError(error: ExoPlaybackException) {}
            override fun onPositionDiscontinuity(reason: Int) {}
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
            override fun onSeekProcessed() {}
        })

        // this.scrollToPosition(0)
    }


    fun initFullscreenDialog() {
        var custom_dialog: Dialog? = null
        custom_dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)


    }

    fun isVisible(view: View?): Boolean {
        if (view == null) {
            return false
        }
        if (!view.isShown) {
            return false
        }
        val actualPosition = Rect()


        if (!view.getLocalVisibleRect(actualPosition) || actualPosition.height() < view.height) {
            return false
        } else {
            return true
        }
        /* val screen = Rect(0, 0, Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels)
         return actualPosition.intersect(screen)*/
    }


    fun playVideo(isEndOfList: Boolean) {

        frameLayout = null
        if (!isEndOfList) {
            var startPosition =
                (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()

            var endPosition =
                (layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()
            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1
            }
            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return
            }
            // if there is more than 1 list-item on the screen
            if (startPosition != endPosition) {
                targetPosition =
                    (layoutManager as LinearLayoutManager?)!!.findFirstCompletelyVisibleItemPosition()
                if (targetPosition == -1) {
                    targetPosition =
                        (layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                }
                if (targetPosition == -1) {
                    targetPosition = startPosition
                }
            } else
                targetPosition = startPosition
            /*  targetPosition = if (startPosition != endPosition) {
                  val startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition)
                  val endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition)
                  if (startPositionVideoHeight > endPositionVideoHeight
                  ) startPosition else endPosition
              } else {
                  startPosition
              }*/
        } else {
            targetPosition = mediaObjects.size - 1
        }

        val currentPosition =
            targetPosition!! - (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        val child = getChildAt(currentPosition) ?: return
        val holder = child.tag as VideoPlayerViewHolder
        removeVideoView(videoSurfaceView)
        frameLayout = holder.itemView.findViewById(R.id.media_container)

        Log.e("Target position", targetPosition.toString())
        Log.e("play position", playPosition.toString())
        Log.e("Current  position", currentPosition.toString())
        Log.e(
            "Act Current  position",
            (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition().toString()
        )
        Log.e("Visible percent", getVisiblePercent(frameLayout!!).toString())

        var value = 0
        if (holder.isVideo) {
            value = getVisiblePercent(frameLayout!!)
        } else {
            value = 0
        }
        if (holder.isVideo && (value > 90)) {
            // video is already playing so return
            if (targetPosition == playPosition) {
                startPlayer()
                return
            } else {
                // set the position of the list-item that is to be played
                playPosition = targetPosition!!
                if (videoSurfaceView == null) {
                    return
                }
                // remove any old surface views from previously playing videos
                videoSurfaceView!!.visibility = View.INVISIBLE

                replayButton = holder.replay
                thumbnail = holder.thumbnail
                progressBar = holder.progressBar
                volumeControl = holder.volumeControl
                viewHolderParent = holder.itemView
                likeLay = holder.likeLay
                requestManager = holder.requestManager
                frameLayout = holder.itemView.findViewById(R.id.media_container)
                videoSurfaceView!!.player = videoPlayer
                volumeControl!!.setOnClickListener(videoViewClickListener)
                singleImage = holder.singleImage
                /*frameLayout!!.setOnClickListener(previewListner)
            singleImage!!.setOnClickListener { previewImageListner }*/

                val dataSourceFactory: DataSource.Factory =
                    DefaultDataSourceFactory(
                        context,
                        Util.getUserAgent(
                            context,
                            "RecyclerView VideoPlayer"
                        )
                    )
                val mediaUrl = mediaObjects[targetPosition!!].media!![0].original
                if (mediaUrl != null) {
                    val videoSource: MediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(
                            Uri.parse(
                                mediaUrl.replace(
                                    "upload/",
                                    "upload/q_50/",
                                    true
                                )
                            )
                        )
                    videoPlayer!!.prepare(videoSource)
                    videoPlayer!!.playWhenReady = true
                }

                holder.replay.setOnClickListener {
                    if (mediaUrl != null) {

                        holder.switchToHD.setBackgroundResource(R.drawable.bg_circle_gradient_hd_black)
                        val videoSource: MediaSource =
                            ExtractorMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(
                                    Uri.parse(
                                        mediaUrl.replace(
                                            "upload/",
                                            "upload/q_50/",
                                            true
                                        )
                                    )
                                )
                        videoPlayer!!.prepare(videoSource)
                        videoPlayer!!.playWhenReady = true
                    }
                }

                holder.switchToHD.setOnClickListener {
                    progressBar!!.visibility = View.VISIBLE
                    holder.isHD = !holder.isHD
                    val currentPos = videoPlayer!!.currentPosition
                    if (mediaUrl != null) {
                        if (!holder.isHD) {
                            holder.switchToHD.setBackgroundResource(R.drawable.bg_circle_gradient_hd_black)
                            val videoSource: MediaSource =
                                ExtractorMediaSource.Factory(dataSourceFactory)
                                    .createMediaSource(
                                        Uri.parse(
                                            mediaUrl.replace(
                                                "upload/",
                                                "upload/q_50/",
                                                true
                                            )
                                        )
                                    )

                            videoPlayer!!.prepare(videoSource)
                            videoPlayer!!.playWhenReady = true
                            videoPlayer!!.seekTo(currentPos)
                        } else {
                            holder.switchToHD.setBackgroundResource(R.drawable.bg_circle_gradient_hd)
                            val videoSource: MediaSource =
                                ExtractorMediaSource.Factory(dataSourceFactory)
                                    .createMediaSource(Uri.parse(mediaUrl))
                            videoPlayer!!.prepare(videoSource)
                            videoPlayer!!.playWhenReady = true
                            videoPlayer!!.seekTo(currentPos)
                        }
                    }
                }
            }
        } else {
            try {
                removeVideoView(videoSurfaceView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val videoViewClickListener =
        OnClickListener {
            toggleVolume()
        }

    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     * @param playPosition
     * @return
     */
    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at =
            playPosition - (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        Log.d(
            TAG,
            "getVisibleVideoSurfaceHeight: at: $at"
        )
        val child = getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)
        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }

    // Remove the old player
    private fun removeVideoView(videoView: PlayerView?) {

        val parent = videoView!!.parent as? ViewGroup
        if (parent == null)
            return
        val index = parent.indexOfChild(videoView)
        if (index >= 0) {
            parent.removeViewAt(index)
            isVideoViewAdded = false
            viewHolderParent!!.setOnClickListener(null)
        }
    }

    private fun addVideoView() {

        frameLayout!!.addView(videoSurfaceView)

        isVideoViewAdded = true
        videoSurfaceView!!.requestFocus()
        videoSurfaceView!!.visibility = View.VISIBLE
        videoSurfaceView!!.alpha = 1f
        thumbnail!!.visibility = View.GONE
        volumeControl!!.alpha = 1f
    }

    public fun resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView)
            playPosition = -2
            videoSurfaceView!!.visibility = View.INVISIBLE
            thumbnail!!.visibility = View.VISIBLE
            volumeControl!!.alpha = 0f
        }
    }

    fun releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer!!.release()
            videoPlayer = null
        }
        viewHolderParent = null
    }

    fun pausePlayer() {
        videoPlayer!!.setPlayWhenReady(false);
        videoPlayer!!.getPlaybackState();
    }

    fun setPlayPostion(pos: Long) {
        return videoPlayer!!.seekTo(pos)
    }

    fun getPlayPosition(): Long {
        return videoPlayer!!.currentPosition
    }

    fun startPlayer() {
        volumeControl!!.alpha = 1f
        videoPlayer!!.setPlayWhenReady(true);
        videoPlayer!!.getPlaybackState();
    }

    private fun toggleVolume() {
        if (videoPlayer != null) {
            if (volumeState == VolumeState.OFF) {
                Log.d(
                    TAG,
                    "togglePlaybackState: enabling volume."
                )
                setVolumeControl(VolumeState.ON)
            } else if (volumeState == VolumeState.ON) {
                Log.d(
                    TAG,
                    "togglePlaybackState: disabling volume."
                )
                setVolumeControl(VolumeState.OFF)
            }
        }
    }

    private fun setVolumeControl(state: VolumeState) {
        volumeState = state
        if (state == VolumeState.OFF) {
            videoPlayer!!.volume = 0f
            animateVolumeControl()
        } else if (state == VolumeState.ON) {
            videoPlayer!!.volume = 1f
            animateVolumeControl()
        }
    }

    /*
    * todo change image
    * */
    private fun animateVolumeControl() {
        if (volumeControl != null) {
            volumeControl!!.bringToFront()
            if (volumeState == VolumeState.OFF) {
                requestManager!!.load(android.R.drawable.ic_lock_silent_mode)
                    .into(volumeControl!!)
            } else if (volumeState == VolumeState.ON) {
                requestManager!!.load(android.R.drawable.ic_lock_silent_mode_off)
                    .into(volumeControl!!)
            }
        }
    }

    fun setMediaObjects(mediaObjects: ArrayList<FeedList>) {
        this.mediaObjects = mediaObjects
    }

    fun setFragmentActivity(aFragActicvity: FragmentActivity, aFragmentManager: TLFragmentManager) {
        this.mFragActicvity = aFragActicvity
        this.mFragmentManager = aFragmentManager
    }

    fun setInterface(aCallBack: Feedistener) {
        this.mCallback = aCallBack
    }

    companion object {
        private const val TAG = "VideoPlayerRecyclerView"
    }


}