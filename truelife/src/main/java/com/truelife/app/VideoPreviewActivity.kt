package com.truelife.app

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection.Factory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.truelife.R
import com.truelife.app.fragment.chat.message.TLFeedDetailsVideo
import com.truelife.base.BaseActivity
import kotlinx.android.synthetic.main.activity_video_preview.*

class VideoPreviewActivity : BaseActivity() {
    private var myVideoURL: String? = ""
    private var myCloseImgBtn: ImageButton? = null

    // EXo player
    private var mPlayer: SimpleExoPlayer? = null
    private var mProgressBar: ProgressBar? = null
    var mPlayerView: SimpleExoPlayerView? = null
    var mPos: Long = 0
    var mAction = ""
    var isSeekDone = false
 // lateinit var power : PowerManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        System.gc()
      //.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "TAG")!!
       // power= myContext.getSystemService(POWER_SERVICE) as PowerManager
       // power.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakeup:mytag").acquire()

        mPlayerView = findViewById(R.id.exo_videoPlayer)
        mProgressBar = findViewById(R.id.progressBar)
        myVideoURL = intent.getStringExtra("url")!!
        if (intent.hasExtra("action"))
            mAction = intent.getStringExtra("action")!!
        if (intent.hasExtra("play_postion"))
            mPos = intent.getLongExtra("play_postion", 0)!!
        getPlayer(myVideoURL)
        back_arrow.setOnClickListener {
            try {

                val intent = Intent(mAction)
                val bundle = Bundle()
                bundle.putLong(mAction, mPlayer!!.currentPosition)
                intent.putExtras(bundle)
                LocalBroadcastManager.getInstance(myContext).sendBroadcast(intent)


                /*val intent = Intent(TLConstant.VIDEO_SEEK_POSITION)
                intent.action = TLConstant.VIDEO_SEEK_POSITION
                intent.setPackage(myContext.packageName)
                intent.putExtra(TLConstant.VIDEO_SEEK_POSITION, mPlayer!!.currentPosition)
                sendBroadcast(intent)*/
            } catch (e: Exception) {
                print("exception: " + e.toString())
            }
            finish()
        }
    }

    override fun clickListener() {

    }

    override fun init() {

    }


    private fun getPlayer(videoURL: String?) { // URL of the video to stream

        val mainHandler = Handler()
        mPlayerView!!.visibility = View.VISIBLE
        /* A TrackSelector that selects tracks provided by the MediaSource to be consumed by each of the available Renderers.
	  A TrackSelector is injected when the player is created. */
        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory: TrackSelection.Factory =
            Factory(bandwidthMeter)
        val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        // Create the player with previously created TrackSelector
        mPlayer = ExoPlayerFactory.newSimpleInstance(myContext, trackSelector)
        // Load the default controller
        mPlayerView!!.useController = true
        mPlayerView!!.requestFocus()
        // Load the SimpleExoPlayerView with the created player
        mPlayer!!.setSeekParameters(SeekParameters.CLOSEST_SYNC)
        mPlayerView!!.player = mPlayer
        // Measures bandwidth during playback. Can be null if not required.
        val defaultBandwidthMeter = DefaultBandwidthMeter()
        // Produces DataSource instances through which media data is loaded.


        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(
                myContext,
                Util.getUserAgent(myContext, "MyAppName"),
                defaultBandwidthMeter
            )
        // Produces Extractor instances for parsing the media data.
        val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()
        // This is the MediaSource representing the media to be played.
        val videoSource: MediaSource = ExtractorMediaSource(
            Uri.parse(videoURL),
            dataSourceFactory,
            extractorsFactory,
            null,
            null
        )

        // Prepare the player with the source.
        mPlayer!!.prepare(videoSource)
        // Autoplay the video when the player is ready
        mPlayer!!.playWhenReady = true

        mPlayer!!.addListener(object : Player.EventListener {

            override fun onSeekProcessed() {
                mProgressBar!!.visibility = View.GONE
            }



           /* override fun onPlayerError(error: ExoPlaybackException?) {

                mProgressBar!!.visibility = View.GONE
            }
*/
            override fun onLoadingChanged(isLoading: Boolean) {
           //    power.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakeup:mytag").acquire()
                if (isLoading)
                    mProgressBar!!.visibility = View.VISIBLE
                else mProgressBar!!.visibility = View.GONE

            }

            override fun onPositionDiscontinuity(reason: Int) {

            }

            override fun onRepeatModeChanged(repeatMode: Int) {

            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

            }

          /*  override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

            }*/

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {


                    Player.STATE_BUFFERING -> {

                        Log.e(
                            TAG,
                            "onPlayerStateChanged: Buffering video."
                        )
                        if (mProgressBar != null) {
                            mProgressBar!!.visibility = View.VISIBLE
                        }
                    }
                    Player.STATE_ENDED -> {
                        Log.d(
                            TAG,
                            "onPlayerStateChanged: Video ended."
                        )


                    }
                    Player.STATE_IDLE -> {

                    }
                    Player.STATE_READY -> {
                        if (!isSeekDone) {
                            if (mPos != null && mPos > 0)
                                mPlayer!!.seekTo(mPos)
                            isSeekDone = true
                        }
                        if (mProgressBar != null)
                            mProgressBar!!.visibility = View.GONE
                    }
                }
            }

        })
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            myContext.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            myContext.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mPlayer!!.release()
        this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        Log.d(TAG, "TLFeedDetailsVideo onDestroyView")
    }

    override fun onPause() {
        Log.d(TAG, "onPause called")
        if (mPlayer != null) {
            mPlayer!!.playWhenReady = false
            mPlayer!!.playbackState
        }
        super.onPause()
    }


    override fun onBackPressed() {
        back_arrow.performClick()
    }

    override fun onResume() {
        super.onResume()
        if (mPlayer != null) {
            mPlayer!!.playWhenReady = true
            mPlayer!!.playbackState
        }
        Log.d(TAG, "onResume called")
    }

    companion object {
        @JvmField
        var TAG = TLFeedDetailsVideo::class.java.simpleName
    }
}