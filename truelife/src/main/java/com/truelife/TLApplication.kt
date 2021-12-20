package com.truelife

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDex
import com.aghajari.emojiview.AXEmojiManager
import com.aghajari.emojiview.emoji.iosprovider.AXIOSEmojiProvider
import com.cloudinary.android.MediaManager
import com.evernote.android.job.JobManager

import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.gms.ads.MobileAds
import com.truelife.chat.activities.calling.model.AGEventHandler
import com.truelife.chat.activities.calling.model.EngineConfig
import com.truelife.chat.activities.calling.model.MyEngineEventHandler
import com.truelife.chat.job.FireJobCreator
import com.truelife.chat.utils.MyMigration
import com.truelife.chat.utils.SharedPreferencesManager
import io.agora.rtc.Constants
import io.agora.rtc.RtcEngine
import io.realm.Realm
import io.realm.RealmConfiguration
import java.lang.Exception
import java.lang.RuntimeException

public class TLApplication : Application(), ActivityLifecycleCallbacks {

    companion object {

        private var INSTANCE: TLApplication? = null
        private var mApp: TLApplication? = null
        private var currentChatId = ""
        private var chatActivityVisible = false
        private var phoneCallActivityVisible = false
        private var baseActivityVisible = false
        private var isCallActive = false

        fun context(): Context? {
            return mApp?.getApplicationContext()
        }

           fun isChatActivityVisible(): Boolean {
            return chatActivityVisible
        }

        fun getCurrentChatId(): String? {
            return currentChatId
        }

        private var hasMovedToForeground = false

        fun isHasMovedToForeground(): Boolean {
            return hasMovedToForeground
        }

        private var activityReferences = 0
        private var isActivityChangingConfigurations = false

        fun chatActivityResumed(chatId: String) {
            chatActivityVisible = true
            currentChatId = chatId
        }

        fun chatActivityPaused() {
            chatActivityVisible = false
            currentChatId = ""
        }

        fun isPhoneCallActivityVisible(): Boolean {
            return phoneCallActivityVisible
        }

        fun phoneCallActivityResumed() {
            phoneCallActivityVisible = true
        }

        fun phoneCallActivityPaused() {
            phoneCallActivityVisible = false
        }


        fun isBaseActivityVisible(): Boolean {
            return baseActivityVisible
        }

        fun baseActivityResumed() {
            baseActivityVisible = true
        }

        fun baseActivityPaused() {
            baseActivityVisible = false
        }


        fun setCallActive(mCallActive: Boolean) {
            isCallActive = mCallActive
        }

        fun isIsCallActive(): Boolean {
            return isCallActive
        }


        private var mRtcEngine: RtcEngine? = null
        private var mConfig: EngineConfig? = null
        private var mEventHandler: MyEngineEventHandler? = null

        fun rtcEngine(): RtcEngine? {
            return mRtcEngine
        }

        fun config(): EngineConfig? {
            return mConfig
        }


        fun addEventHandler(handler: AGEventHandler?) {
            mEventHandler?.addEventHandler(handler)
        }

        fun removeEventHandler(handler: AGEventHandler?) {
            mEventHandler?.removeEventHandler(handler)
        }

    }


        override fun onCreate() {
            super.onCreate()
            MediaManager.init(this)
            //add support for vector drawables on older APIs
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
            //init realm
            Realm.init(this)
            //init set realm configs
            val realmConfiguration = RealmConfiguration.Builder()
                .schemaVersion(MyMigration.SCHEMA_VERSION.toLong())
                .migration(MyMigration())
                .build()
            Realm.setDefaultConfiguration(realmConfiguration)
            //init shared prefs manager
            SharedPreferencesManager.init(this)
            //init evernote job
            JobManager.create(this).addJobCreator(FireJobCreator())
            initEmojiKeyboard()


            //initialize ads for faster loading in first time
            if (resources.getBoolean(R.bool.are_ads_enabled)) MobileAds.initialize(this)
            registerActivityLifecycleCallbacks(this)
            createRtcEngine()
            mApp = this
        }

        private fun initEmojiKeyboard() {
            AXEmojiManager.install(this, AXIOSEmojiProvider(this))
            val bgColor = ContextCompat.getColor(this, R.color.bgColor)
            val accentColor = ContextCompat.getColor(this, R.color.colorAccent)
            AXEmojiManager.getEmojiViewTheme().footerBackgroundColor = bgColor
            AXEmojiManager.getEmojiViewTheme().categoryColor = bgColor
            AXEmojiManager.getEmojiViewTheme().backgroundColor = bgColor
            AXEmojiManager.getEmojiViewTheme().selectedColor = accentColor
            AXEmojiManager.getStickerViewTheme().categoryColor = bgColor
            AXEmojiManager.getStickerViewTheme().backgroundColor = bgColor
            AXEmojiManager.getStickerViewTheme().selectedColor = accentColor
        }

        fun context(): Context? {
            return mApp?.getApplicationContext()
        }

        //to run multi dex
        override fun attachBaseContext(base: Context?) {
            super.attachBaseContext(base)
            MultiDex.install(this)
        }

        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {
            hasMovedToForeground =
                if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                    true
                } else {
                    false
                }
        }

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {
            isActivityChangingConfigurations = activity.isChangingConfigurations
            if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                // App enters background
                SharedPreferencesManager.setLastActive(System.currentTimeMillis())
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}


        private fun createRtcEngine() {
            val context = applicationContext
            val appId = context.getString(R.string.agora_app_id)
            if (TextUtils.isEmpty(appId)) {
                throw RuntimeException("NEED TO use your App ID, get your own ID at https://dashboard.agora.io/")
            }
            mEventHandler = MyEngineEventHandler()
            mRtcEngine = try {
                // Creates an RtcEngine instance
                RtcEngine.create(context, appId, mEventHandler)
            } catch (e: Exception) {
                throw RuntimeException(
                    """
                    NEED TO check rtc sdk init fatal error
                    ${Log.getStackTraceString(e)}
                    """.trimIndent()
                )
            }

            /*
          Sets the channel profile of the Agora RtcEngine.
          The Agora RtcEngine differentiates channel profiles and applies different optimization
          algorithms accordingly. For example, it prioritizes smoothness and low latency for a
          video call, and prioritizes video quality for a video broadcast.
         */mRtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)


//        /*
//          Enables the onAudioVolumeIndication callback at a set time interval to report on which
//          users are speaking and the speakers' volume.
//          Once this method is enabled, the SDK returns the volume indication in the
//          onAudioVolumeIndication callback at the set time interval, regardless of whether any user
//          is speaking in the channel.
//         */
//        mRtcEngine.enableAudioVolumeIndication(200, 3, false);
            mConfig = EngineConfig()
        }

}
