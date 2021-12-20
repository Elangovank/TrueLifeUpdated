package com.truelife.app.fragment.chat.message

import android.annotation.SuppressLint
import android.content.Context.POWER_SERVICE
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import androidx.fragment.app.FragmentActivity
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.truelife.R
import com.truelife.base.BaseFragment
import com.truelife.base.TLFragmentManager

/**
 * Created by Aravindh on 01-04-2017.
 * TLMenuList
 */
class TLFeedDetailsVideo : BaseFragment() {
    private var myContext: FragmentActivity? = null
    private var myFragmentManager: TLFragmentManager? = null
    private var myVideoURL: String? = ""
    private var myCloseImgBtn: ImageButton? = null
    // EXo player
    private var mPlayer: SimpleExoPlayer? = null
    var mPlayerView: SimpleExoPlayerView? = null
    @SuppressLint("InvalidWakeLockTag")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the square_border for this fragment
        val aView =
            inflater.inflate(R.layout.fragment_feed_details_video, container, false)
        initializeClassAndWidgets(aView)
        // loadValues();
        values

        val power : PowerManager = activity!!.getSystemService(POWER_SERVICE) as PowerManager//.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "TAG")!!
        power.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "TAG"
        ).acquire()
        //  setListeners();
        clickListeners()
        // Hide status bar
// getActivity().getTheme().applyStyle(R.style.VideoPlayTheme, true);
//  myAsyncTaskRunner = new AsyncTaskRunner();
//  myAsyncTaskRunner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        // switchOrientation(myContext);
        return aView
    }

    private fun getPlayer(videoURL: String?) { // URL of the video to stream
//String videoURL = "https://www.youtube.com/watch?v=3tmd-ClpJxA";
// Handler for the video player
        val mainHandler = Handler()
        mPlayerView!!.visibility = View.VISIBLE
        /* A TrackSelector that selects tracks provided by the MediaSource to be consumed by each of the available Renderers.
	  A TrackSelector is injected when the player is created. */
        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory: TrackSelection.Factory =
            AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        // Create the player with previously created TrackSelector
        mPlayer = ExoPlayerFactory.newSimpleInstance(context!!, trackSelector)
        // Load the default controller
        mPlayerView!!.useController = true
        mPlayerView!!.requestFocus()
        // Load the SimpleExoPlayerView with the created player
        mPlayerView!!.player = mPlayer
        // Measures bandwidth during playback. Can be null if not required.
        val defaultBandwidthMeter = DefaultBandwidthMeter()
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context!!, "MyAppName"),
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
    }

    private fun clickListeners() {
        myCloseImgBtn!!.setOnClickListener { myFragmentManager!!.onBackPress() }
    }

    private fun initializeClassAndWidgets(aView: View) {
        try {
            myContext = activity
            myFragmentManager = TLFragmentManager(myContext!!)
            mPlayerView = aView.findViewById(R.id.exo_videoPlayer)
            myCloseImgBtn = aView.findViewById(R.id.inflate_alert_close_button)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val values: Unit
        get() {
            try {
                val aBundle = arguments
                if (aBundle != null) {
                    myVideoURL = aBundle.getString("Video_url")
                    getPlayer(myVideoURL)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            myContext!!.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            myContext!!.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPlayer!!.release()
        activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //  getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
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

    override fun onResume() {
        super.onResume()
        if (mPlayer != null) {
            mPlayer!!.playWhenReady = true
            mPlayer!!.playbackState
        }
        Log.d(TAG, "onResume called")
    }

    override fun onBackPressed() {}
    override fun onResumeFragment() {}
    override fun init(view: View) {}
    override fun initBundle() {}
    override fun clickListener() {}

    companion object {
        @JvmField
        var TAG = TLFeedDetailsVideo::class.java.simpleName
    }
}