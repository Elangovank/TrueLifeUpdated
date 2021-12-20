package com.truelife.app.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import com.truelife.R


class YoutubePalyerActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {
    private val RECOVERY_REQUEST = 1
    lateinit var youTubeView: YouTubePlayerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_palyer)
        youTubeView =  findViewById(R.id.youtube_view);
        youTubeView.initialize(getString(R.string.youtube_api_key), this);
    }


    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider?,
        player: YouTubePlayer,
        wasRestored: Boolean
    ) {
        if (!wasRestored) {
            player.cueVideo(intent.getStringExtra("id")!!)
        }
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider?,
        errorReason: YouTubeInitializationResult
    ) {
        if (errorReason.isUserRecoverableError) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show()
        } else {
            val error =
                String.format(getString(R.string.player_error), errorReason.toString())
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == RECOVERY_REQUEST) { // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(getString(R.string.youtube_api_key), this)
        }
    }

    protected fun getYouTubePlayerProvider(): YouTubePlayer.Provider {
        return youTubeView
    }
}
