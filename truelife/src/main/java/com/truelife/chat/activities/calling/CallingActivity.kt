package com.truelife.chat.activities.calling

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.*
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.truelife.R
import com.truelife.TLApplication
import com.truelife.chat.activities.BaseActivity
import com.truelife.chat.activities.calling.event.CallingStateEvent
import com.truelife.chat.activities.calling.event.CallingViewState
import com.truelife.chat.activities.calling.model.CallType
import com.truelife.chat.activities.calling.model.CallingState
import com.truelife.chat.activities.calling.model.EngineConfig
import com.truelife.chat.common.extensions.unBindServiceSafely
import com.truelife.chat.extensions.finishAndRemoveTaskCompat
import com.truelife.chat.model.ImageItem
import com.truelife.chat.model.constants.FireCallDirection
import com.truelife.chat.model.realms.FireCall
import com.truelife.chat.model.realms.User
import com.truelife.chat.services.CallingService
import com.truelife.chat.utils.RealmHelper
import com.truelife.chat.utils.network.GroupManager


import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_phone_call.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import com.truelife.chat.utils.IntentUtils
import com.truelife.chat.utils.Util
import com.truelife.chat.utils.network.FireManager
import com.truelife.chat.utils.network.FireManager.Companion.checkAndDownloadUserPhoto
import com.truelife.chat.utils.network.FireManager.Companion.fetchUserByUid

class CallingActivity : BaseActivity(), ServiceConnection {
    private lateinit var imgUser: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvCallType: TextView
    private lateinit var btnAnswer: ImageView
    private lateinit var btnReject: ImageView
    private lateinit var btnHangup: ImageView
    private lateinit var btnSpeaker: ImageButton
    private lateinit var btnMic: ImageButton
    private lateinit var btnVideo: ImageButton
    private lateinit var constraint: ConstraintLayout
    private lateinit var btnFlipCamera: ImageButton
    private lateinit var bottomHolder: ImageView
    lateinit var localViewGroup: FrameLayout
    private lateinit var layFlipCamera: LinearLayout
    private lateinit var laySpeaker: LinearLayout
    private lateinit var layVideo: LinearLayout
    private lateinit var layMic: LinearLayout
    private lateinit var layBtnRoot: LinearLayout

    private var dx = 0f
    private var dy = 0f
    private var localViewGroupWidth = 0
    private var localViewGroupHeight = 0
    private var rootWidth = 0
    private var rootHeight = 0

    private var lastX = 0f
    private var lastY = 0f

    private var callDirection = 0
    private var callType: CallType = CallType.VOICE
    private var user: User? = null
    private var uid: String? = null
    private var phoneNumber: String? = null
    private var mCallId: String? = null


    private var callingServiceInterface: CallingService.CallingServiceInterface? = null

    private var videoUids = hashMapOf<Int, Pair<SurfaceView, Boolean>>()
    var localSurfaceView: SurfaceView? = null
    private var action = IntentUtils.NOTIFICATION_ACTION_NONE
    private val groupManager = GroupManager()
    private var broadcastReceiver: BroadcastReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenOnFlags()
        setContentView(R.layout.activity_phone_call)

        initViews()
        bindService()

        uid = intent.getStringExtra(IntentUtils.UID)
        phoneNumber = intent.getStringExtra(IntentUtils.PHONE)
        callType = CallType.fromInt(intent.getIntExtra(IntentUtils.CALL_TYPE, CallType.VOICE.value))

        callDirection = intent.getIntExtra(IntentUtils.CALL_DIRECTION, -1)
        mCallId = intent.getStringExtra(IntentUtils.CALL_ID)
        action = intent.getIntExtra(IntentUtils.CALL_ACTION_TYPE, IntentUtils.NOTIFICATION_ACTION_NONE)

        if (isVideoCall()) {
            user_name_toolbar_chat_activity.text = "Video Call"
        } else {
            user_name_toolbar_chat_activity.text = "Voice Call"
        }

        tvCallType.text = getCallTypeText()
        user = RealmHelper.getInstance().getUser(uid)
        if (user != null) {
            user?.let { user ->
                tvUsername.text = user.properUserName
                tvStatus.setText(R.string.connecting)
                tvStatus.setTextColor(Color.parseColor("#20b66e"))
                //load the full user image if it's exists
                if (user.userLocalPhoto != null) {
                    Glide.with(this).load(user.userLocalPhoto).into(imgUser)
                    //otherwise load the thumbImg
                } else {
                    Glide.with(this).load(user.thumbImg).into(imgUser)
                }
            }
        } else {
            //if the user is not exists in local database we will set the name as phoneNumber
            if (phoneNumber != null)
                tvUsername.text = phoneNumber

            //fetch the user info and save it
            uid?.let { uid ->
                if (callType.isGroupCall()) {
                    groupManager.fetchAndCreateGroup(uid).subscribe({ user ->
                        if (user != null) {
                            RealmHelper.getInstance().updateUserObjectForCall(uid, mCallId)
                            tvUsername.text = user.properUserName
                        }
                    }, { error ->
                    }).addTo(disposables)
                } else {
                    fetchUserByUid(uid)
                            .subscribe({ user ->
                                if (user != null) {
                                    RealmHelper.getInstance().updateUserObjectForCall(uid, mCallId)
                                    tvUsername.text = user.properUserName
                                }
                            }, { throwable ->
                            }).addTo(disposables)
                }
            }
        }

        hideOrShowButtons(callDirection == FireCallDirection.OUTGOING)
        btnAnswer.setOnClickListener { setStateEvent(CallingStateEvent.AnswerIncoming) }
        btnReject.setOnClickListener { setStateEvent(CallingStateEvent.RejectIncoming) }
        btnHangup.setOnClickListener { setStateEvent(CallingStateEvent.EndCall) }
        btnSpeaker.setOnClickListener { setStateEvent(CallingStateEvent.SpeakerClicked) }
        btnMic.setOnClickListener { setStateEvent(CallingStateEvent.MicClicked) }
        btnFlipCamera.setOnClickListener { setStateEvent(CallingStateEvent.FlipCameraClicked) }
        btnVideo.setOnClickListener { setStateEvent(CallingStateEvent.BtnVideoClicked) }


        //hide or show bottom view with the buttons
       /* constraint.setOnClickListener(View.OnClickListener {
            if (rtcEngine()?.connectionState != CONNECTION_STATE_CONNECTED) return@OnClickListener
            if (isVideoCall()) {
                val hide = bottomHolder.isVisible
                hideOrShowTopBottomHolders(hide)
            }
        })*/


        //fetch the remote user's photo
        if (user != null) {
            checkAndDownloadUserPhoto(user).subscribe({ imageItem: ImageItem? ->
                if (imageItem?.photo != null) {
                    Glide.with(this@CallingActivity).load(imageItem.photo).into(imgUser)
                }
            }
            ) { throwable: Throwable? -> }.addTo(disposables)
        }

        localViewGroup.isVisible = isVideoCall()
        updateUI()

        calculateViewsSizes()

        localViewGroup.setOnTouchListener { view, event ->
            handleLvgTouchEvent(event, view)
        }

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                endCall()
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver!!, IntentFilter(IntentUtils.ACTION_FINISH_CALLING_ACTIVITY))
    }

    private fun calculateViewsSizes() {
        constraint.doOnPreDraw {
            rootWidth = it.width
            rootHeight = it.height
        }
        localViewGroup.doOnPreDraw {
            localViewGroupWidth = it.width
            localViewGroupHeight = it.height
        }
    }

    private fun handleLvgTouchEvent(event: MotionEvent, view: View): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                dx = view!!.x - event.rawX
                dy = view!!.getY() - event.rawY;

            }
            MotionEvent.ACTION_MOVE -> {

                var x = event.rawX + dx

                val y = event.rawY + dy

                if (x < 0 || x > rootWidth - localViewGroupWidth) {
                    if (y >= 0 && y < rootHeight - localViewGroupHeight) {
                        view!!.animate()
                                .y(y)
                                .setDuration(0)
                                .start()
                    }

                } else if (y > 0 && y > rootHeight - localViewGroupHeight) {
                    if (x >= 0 && x > rootWidth - localViewGroupWidth) {
                        view!!.animate()
                                .x(x)
                                .setDuration(0)
                                .start()
                    }

                } else {
                    view!!.animate()
                            .x(x)
                            .y(y)
                            .setDuration(0)
                            .start()
                }


            }
            MotionEvent.ACTION_UP -> {
                lastX = view.x
                lastY = view.y
            }
            else -> {
                false
            }
        }
        return true
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { newIntent ->
            when (val action = newIntent.extras?.getInt(IntentUtils.CALL_ACTION_TYPE)) {
                IntentUtils.NOTIFICATION_ACTION_HANGUP -> {
                    setStateEvent(CallingStateEvent.EndCall)
                }

                IntentUtils.NOTIFICATION_ACTION_ANSWER -> {
                    RealmHelper.getInstance().getFireCall(mCallId)?.let { fireCall ->
                        setStateEvent(CallingStateEvent.StartCall(fireCall, true))
                    }
                }
                else -> {

                }
            }
        }

    }

    private fun getCallTypeText(): String {
        return when (callType) {
            CallType.VIDEO -> String.format(getString(R.string.fireapp_video_call), getString(R.string.app_name))
            CallType.CONFERENCE_VIDEO -> String.format(getString(R.string.fireapp_conference_video_call), getString(R.string.app_name))
            CallType.CONFERENCE_VOICE -> String.format(getString(R.string.fireapp_conference_voice_call), getString(R.string.app_name))
            else -> String.format(getString(R.string.fireapp_voice_call), getString(R.string.app_name))
        }
    }


    private fun setCallingState(callingState: CallingState) {
        when (callingState) {
            CallingState.INITIATING -> {
                hideOrShowButtons(true)
                tvStatus.text = getString(R.string.initiating)
                tvStatus.setTextColor(Color.parseColor("#2FBFE3"))
            }
            CallingState.CONNECTING -> {
                tvStatus.text = getString(R.string.connecting)
                tvStatus.setTextColor(Color.parseColor("#2FBFE3"))
            }
            CallingState.CONNECTED -> {
                tvStatus.text = getString(R.string.waiting_for_answer)
                tvStatus.setTextColor(Color.parseColor("#2FBFE3"))
            }
            CallingState.FAILED -> {
                tvStatus.text = getString(R.string.failed)
                tvStatus.setTextColor(Color.parseColor("#d42525"))
            }
            CallingState.RECONNECTING -> {
                tvStatus.text = getString(R.string.reconnecting)
                tvStatus.setTextColor(Color.parseColor("#d42525"))
            }
            CallingState.ANSWERED -> {
                tvStatus.text = getString(R.string.answered)
                tvStatus.setTextColor(Color.parseColor("#20b66e"))
            }
            else -> {
                tvStatus.text = ""
            }
        }
    }

    private fun hideOrShowTopBottomHolders(setHidden: Boolean) {
        val constraint1 = ConstraintSet()
        constraint1.clone(this, R.layout.activity_phone_call)
        val constraint2 = ConstraintSet()
        constraint2.clone(this, R.layout.activity_phone_call_alt)
        val transition = AutoTransition()
        transition.duration = 200
        TransitionManager.beginDelayedTransition(constraint, transition)
        val constraintSet = if (setHidden) constraint2 else constraint1
        constraintSet.applyTo(constraint)
        if (lastX != 0f) {
            localViewGroup.x = lastX
        }
        if (lastY != 0f) {
            localViewGroup.y = lastY
        }
        if (!setHidden) {
            updateUI()
            btnHangup.isVisible = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateState(callingViewState: CallingViewState) {
        when (callingViewState) {

            is CallingViewState.DisableSpeaker -> {
                disableSpeaker()
            }

            is CallingViewState.EnableSpeaker -> {
                enableSpeaker()
            }

            is CallingViewState.UpdateDuration -> {
                tvStatus.text = Util.formatCallTime(callingViewState.duration.toInt())
                tvStatus.setTextColor(Color.parseColor("#20b66e"))
            }

            is CallingViewState.PauseLocalVideo -> {
                pauseLocalVideo()
            }

            is CallingViewState.ResumeLocalVideo -> {
                resumeLocalVideo()
            }

            is CallingViewState.MicMuted -> {
                val isMicMuted = callingViewState.setMuted
                setMicBg(btnMic, isMicMuted)
                setIconBg(btnMic, isMicMuted)
            }
            is CallingViewState.FlipCamera -> {
                val isFlip = callingViewState.isFlip
                setFlipCameraIcon(btnFlipCamera, isFlip)
            }

            is CallingViewState.SetupRemoteViewForUid -> {
                renderRemoteVideo(callingViewState.uid)
            }

            is CallingViewState.RemoveRemoteViewForUid -> {
                removeRemoteView(callingViewState.uid)
            }

            is CallingViewState.MuteOrUnmuteRemoteViewForUid -> {
                muteOrUnMuteRemoteView(callingViewState.uid, callingViewState.mute)
            }

            is CallingViewState.SetupLocalView -> {
                addLocalView()
            }

            is CallingViewState.UpdateCallingState -> {
                setCallingState(callingViewState.callingState)
            }

            is CallingViewState.OnCallEstablished -> {
                volumeControlStream = AudioManager.STREAM_VOICE_CALL
                hideOrShowButtons(true)
                updateUI()
            }

            is CallingViewState.CallEnded -> {
                endCall()
            }



            is CallingViewState.JoinChannelSuccess -> {
                volumeControlStream = AudioManager.STREAM_VOICE_CALL
                hideOrShowButtons(true)
                if (isVideoCall())
                    setVideoStuff()
                updateUI()
            }

            is CallingViewState.HideRemoteViews -> {
                remote_view.isVisible = false
            }

            is CallingViewState.HideAnswerButtons -> {
                hideOrShowButtons(true)
            }

            is CallingViewState.SetupRemoteViewWithSurfaceView -> {
                addRemoteView(callingViewState.uid, callingViewState.surfaceView)
            }

        }
    }


    private fun disableSpeaker() {
        setSpeakerIconBg(btnSpeaker, false)
    }

    private fun enableSpeaker() {
        setSpeakerIconBg(btnSpeaker, true)
    }

    private fun pauseLocalVideo() {
        setVideoIcon(btnVideo, false)
        removeLocalView()
        btnFlipCamera.visibility = View.GONE
        layFlipCamera.visibility = View.GONE
        btnSpeaker.visibility = View.VISIBLE
        laySpeaker.visibility = View.VISIBLE
    }

    private fun resumeLocalVideo() {
        setVideoIcon(btnVideo, true)
        localViewGroup.isVisible = true
        if (localSurfaceView != null && localSurfaceView?.parent == null) {
            localViewGroup.addView(localSurfaceView)
        }

        btnSpeaker.visibility = View.VISIBLE
        laySpeaker.visibility = View.VISIBLE
        btnFlipCamera.visibility = View.VISIBLE
        layFlipCamera.visibility = View.VISIBLE
    }

    private fun setVideoStuff() {


        val videoOffered = isVideoCall()
        if (!videoOffered) {
            btnVideo.visibility = View.GONE
            layVideo.visibility = View.GONE
            btnFlipCamera.visibility = View.GONE
            layFlipCamera.visibility = View.GONE
            val fireAppVoiceCall = String.format(getString(R.string.fireapp_voice_call), getString(R.string.app_name))
            tvCallType.text = fireAppVoiceCall
        } else {
            enableSpeaker()
            btnSpeaker.visibility = View.GONE
            laySpeaker.visibility = View.GONE
            val fireAppVideoCall = String.format(getString(R.string.fireapp_video_call), getString(R.string.app_name))
            tvCallType.text = fireAppVideoCall
            setIconBg(btnVideo, true)
        }
    }

    private fun isVideoCall(): Boolean = callType == CallType.VIDEO || callType == CallType.CONFERENCE_VIDEO


    //hide or show buttons depending on call direction (incoming,outgoing)
    @SuppressLint("RestrictedApi")
    private fun hideOrShowButtons(showHangup: Boolean) {
        if (showHangup) {
            btnReject.visibility = View.GONE
            btnAnswer.visibility = View.GONE
            btnHangup.visibility = View.VISIBLE
            layBtnRoot.visibility = View.VISIBLE
        } else {
            btnReject.visibility = View.VISIBLE
            btnAnswer.visibility = View.VISIBLE
            btnHangup.visibility = View.GONE
            layBtnRoot.visibility = View.GONE
        }
    }


    private fun endCall() {
        volumeControlStream = AudioManager.STREAM_SYSTEM
        preview(false, null, config()?.mUid ?: 0)
        finishAndRemoveTaskCompat()
    }

    private fun initViews() {
        imgUser = findViewById(R.id.img_user)
        tvUsername = findViewById(R.id.tv_username)
        tvStatus = findViewById(R.id.tv_status)
        btnHangup = findViewById(R.id.btn_hangup_in_call)
        btnSpeaker = findViewById(R.id.btn_speaker)
        btnMic = findViewById(R.id.btn_mic)
        btnVideo = findViewById(R.id.btn_video)
        btnAnswer = findViewById(R.id.btn_answer)
        btnReject = findViewById(R.id.btn_reject)
        constraint = findViewById(R.id.constraint)
        tvCallType = findViewById(R.id.tv_call_type)
        btnFlipCamera = findViewById(R.id.btn_flip_camera)
        layBtnRoot = findViewById(R.id.lay_btn_root)
        layMic = findViewById(R.id.lay_mic)
        laySpeaker = findViewById(R.id.lay_speaker)
        layVideo = findViewById(R.id.lay_video)
        layFlipCamera = findViewById(R.id.lay_flip_camera)
        bottomHolder = findViewById(R.id.bottom_holder)
        localViewGroup = findViewById(R.id.local_view)
    }


    override fun enablePresence(): Boolean {
        return false
    }


    //we are using onWindowFocusChanged because on Samsung Devices the onPause will called when we will acquire the PowerLock
//which will cause a Flicker
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        setStateEvent(CallingStateEvent.OnWindowFocusChanged(hasFocus))
    }

    public override fun onStop() {
        super.onStop()
        setStateEvent(CallingStateEvent.OnStop)
        EventBus.getDefault().unregister(this)
    }


    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        setStateEvent(CallingStateEvent.OnStart)

    }


    private fun addLocalView() {

        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        localSurfaceView = surfaceView
      //  localSurfaceView?.setBackgroundColor(this.resources.getColor(R.color.transulcent_toolbar))
        preview(true, localSurfaceView, 0)


        //if the local view has no parent(not added to the layout yet) then add it
        if (localSurfaceView?.parent == null) {
            localSurfaceView?.setZOrderMediaOverlay(true)
            localSurfaceView?.setZOrderOnTop(true)
            localViewGroup.addView(localSurfaceView)
        }


        setIconBg(btnVideo, true)
        localViewGroup.isVisible = true
        btnFlipCamera.isVisible = true
        btnVideo.isVisible = true
        btnSpeaker.visibility = View.GONE
        laySpeaker.visibility = View.GONE
    }


    private fun removeLocalView() {
        localViewGroup.removeAllViews()
        localViewGroup.isVisible = false
    }


    private fun removeRemoteView(uid: Int) {
        videoUids.remove(uid)
        remote_view.removeItem(uid)

        if (videoUids.isEmpty()) {
            remote_view.isVisible = false
        }


    }

    private fun muteOrUnMuteRemoteView(uid: Int, setMuted: Boolean) {

        videoUids[uid]?.let { pair ->
            videoUids[uid] = pair.copy(second = setMuted)
            if (setMuted) {
                remote_view.removeItem(uid)
            } else {
                remote_view.addItem(uid, pair.first)
            }
        }
    }

    private fun updateUI() {

        if (isVideoCall()) {
            btnSpeaker.visibility = View.VISIBLE
            laySpeaker.visibility = View.VISIBLE
            btnFlipCamera.visibility = View.VISIBLE
            layFlipCamera.visibility = View.VISIBLE
            btnVideo.visibility = View.VISIBLE
            layVideo.visibility = View.VISIBLE

        } else {
            btnSpeaker.visibility = View.VISIBLE
            laySpeaker.visibility = View.VISIBLE
            btnFlipCamera.visibility = View.GONE
            layFlipCamera.visibility = View.GONE
            btnVideo.visibility = View.GONE
            layVideo.visibility = View.GONE

        }

    }

    private fun setVideoIcon(view: View, show: Boolean) {
          if (show) btnVideo.setImageResource( R.drawable.ic_video_call_on )else btnVideo.setImageResource( R.drawable.ic_video_off)

    }

    private fun setFlipCameraIcon(view: View, show: Boolean) {
          if (show) btnFlipCamera.setImageResource( R.drawable.ic_camera_front )else btnFlipCamera.setImageResource( R.drawable.ic_video_back)

    }
    //this will change button background when it's active
    private fun setIconBg(view: View, show: Boolean) {
      //  if (show) btnMic.setImageResource( R.drawable.ic_call_speaker )else btnMic.setImageResource( R.drawable.ic_no_speaker)
      //  if (show) view.background = ContextCompat.getDrawable(this, R.drawable.inactive_icon_bg) else view.background = ContextCompat.getDrawable(this, R.drawable.inactive_icon_bg)
    }

    private fun setSpeakerIconBg(view: View, show: Boolean) {
    //    if (show) btnSpeaker.setImageResource( R.drawable.ic_call_speaker )else btnSpeaker.setImageResource( R.drawable.ic_speaker_off)
        if (show) view.background = ContextCompat.getDrawable(this, R.drawable.active_icon_bg) else view.background = ContextCompat.getDrawable(this, R.drawable.inactive_icon_bg)
    }

    private fun setMicBg(view: View, show: Boolean) {
        if (show) btnMic.setImageResource( R.drawable.ic_mute_mic )else btnMic.setImageResource( R.drawable.ic_mic)
    }

    //these flags will make the screen turns on whenever a call has come
//also it will prevent the screen from auto turn off when the user the is making a call
    private fun setScreenOnFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
        if (CallingService::class.java.name == componentName.className) {
            callingServiceInterface = iBinder as CallingService.CallingServiceInterface




            when (action) {
                IntentUtils.ACTION_START_NEW_CALL -> {
                    val channel = UUID.randomUUID().toString()
                    val fireCall = FireCall(
                        FireManager.generateKey(), user, FireCallDirection.OUTGOING, System.currentTimeMillis(), phoneNumber
                            ?: "", isVideoCall(), callType.value, channel)

                    setStateEvent(CallingStateEvent.StartCall(fireCall, false))
                }
                IntentUtils.NOTIFICATION_ACTION_CLICK -> {

                    setStateEvent(CallingStateEvent.UpdateMe)
                }
                IntentUtils.NOTIFICATION_ACTION_HANGUP -> {
                    setStateEvent(CallingStateEvent.EndCall)
                }

                IntentUtils.NOTIFICATION_ACTION_ANSWER -> {
                    RealmHelper.getInstance().getFireCall(mCallId)?.let { fireCall ->
                        setStateEvent(CallingStateEvent.StartCall(fireCall, true))
                    }


                }

                IntentUtils.NOTIFICATION_ACTION_START_INCOMING -> {
                    setStateEvent(CallingStateEvent.UpdateMe)
                }

            }

        }
    }


    override fun onServiceDisconnected(componentName: ComponentName) {
        if (CallingService::class.java.name == componentName.className) {
            callingServiceInterface = null
        }
    }

    private fun bindService() {
        val serviceIntent = Intent(this, CallingService::class.java)
        startService(serviceIntent)
        applicationContext.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)
    }


    override fun onDestroy() {
        preview(false, null, config()?.mUid ?: 0)
        applicationContext.unBindServiceSafely(this)

        broadcastReceiver?.let { broadcastReceiver ->
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        }




        super.onDestroy()
    }


    private fun renderRemoteVideo(uid: Int) {

        if (videoUids.containsKey(uid))
            return

        val surfaceV = RtcEngine.CreateRendererView(applicationContext)


        surfaceV.setZOrderOnTop(true)
        surfaceV.setZOrderMediaOverlay(true)


        val setupRemoteVideo = rtcEngine()?.setupRemoteVideo(VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_FILL, uid))



        setStateEvent(CallingStateEvent.SurfaceViewAddedForUid(uid, surfaceV))
        videoUids[uid] = Pair(surfaceV, false)

        addRemoteView(uid, surfaceV)
        remote_view.isVisible = true

    }

    private fun addRemoteView(uid: Int, surfaceV: SurfaceView) {
        remote_view.addItem(uid, surfaceV)
        btnVideo.visibility = View.VISIBLE
        layVideo.visibility = View.VISIBLE
        btnFlipCamera.visibility = View.VISIBLE
        layFlipCamera.visibility = View.VISIBLE
    }


    private fun setStateEvent(stateEvent: CallingStateEvent) {
        callingServiceInterface?.setStateEvent(stateEvent)
    }

    private fun application(): TLApplication {
        return application as TLApplication
    }

    private fun rtcEngine(): RtcEngine? {
        return TLApplication.rtcEngine()
    }

    private fun config(): EngineConfig? {
        return TLApplication.config()
    }


    private fun preview(start: Boolean, view: SurfaceView?, uid: Int) {
        if (start) {
            rtcEngine()?.setupLocalVideo(VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid))
            rtcEngine()?.enableLocalVideo(true)
            rtcEngine()?.startPreview()
        } else {
            rtcEngine()?.stopPreview()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP -> {
                setStateEvent(CallingStateEvent.VolumeKeyPressed)
                false
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}

