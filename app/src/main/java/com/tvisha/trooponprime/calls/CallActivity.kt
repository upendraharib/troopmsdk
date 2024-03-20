package com.tvisha.trooponprime.calls

import android.app.Activity
import android.app.AlertDialog
import android.app.PictureInPictureParams
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.tvisha.trooponprime.MyApplication
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.CallLayoutBinding
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper
import com.tvisha.trooponprime.ui.recent.login.LoginActivity
import org.json.JSONObject
import org.webrtc.RendererCommon
import org.webrtc.VideoTrack


class CallActivity : AppCompatActivity(), MyApplication.CallListeners {
    private var isInPipMode = false
    var isMineCall:Boolean = false
    var callType:Int = 0
    var initiatorName:String = ""
    var participatnsData: JSONObject = JSONObject()
    var callId:String = ""
    var audioManager:AudioManager? = null
    var selfCallTmUserId = ""
    var pip_Builder: PictureInPictureParams.Builder? = null
    var deviceWidth = 0
    var deviceHeight = 0
    lateinit var dataBinding : CallLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.call_layout)
        isMineCall = intent.getBooleanExtra("isMineCall",false)!!
        callType = intent.getIntExtra("callType", ConstantValues.NewCallTypes.AUDIO_CALL)
        participatnsData = Helper.stringToJsonObject(intent.getStringExtra("participants"))!!
        callId = intent.getStringExtra("call_id")!!
        initializeAndSetAudio()
        var app = application as MyApplication
        app.setCallsListeners(this)
        selfCallTmUserId = troopClient.callTmUserId()
        val d: Display = windowManager
            .defaultDisplay
        val p = Point()
        d.getSize(p)
        val width: Int = p.x
        val height: Int = p.y
        val ratio = Rational(width, height)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pip_Builder = PictureInPictureParams.Builder()
            pip_Builder!!.setAspectRatio(ratio).build()
        }
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(displayMetrics)
        deviceHeight = displayMetrics.heightPixels
        deviceWidth = displayMetrics.widthPixels
        setView()
    }
    private fun enterPipModeIfPossible(): Boolean {
            if (isSystemPipEnabledAndAvailable()) {
                val aspectRatio = Rational(deviceWidth, deviceHeight)
                pip_Builder!!.setAspectRatio(aspectRatio).build()
                enterPictureInPictureMode(pip_Builder!!.build())
                return true
            }
        return false
    }
    private fun isSystemPipEnabledAndAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= 26 &&
                packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }
    fun initializeAndSetAudio(){
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager!!.mode = AudioManager.MODE_IN_COMMUNICATION
        if (audioManager != null) {
            /*if (Helper.bluetoothDeviceConnected && Helper.blueToothOn) {
                audioManager!!.isMicrophoneMute = false
                audioManager!!.isSpeakerphoneOn = false
                Thread {
                    try {
                        Thread.sleep(3000)
                        audioManager!!.startBluetoothSco()
                    } catch (e: java.lang.Exception) {
                        Helper.printExceptions(e)
                    }
                }.start()
            } else {*/
                if (audioManager!!.isWiredHeadsetOn) {
                    audioManager!!.isSpeakerphoneOn = false
                    audioManager!!.isSpeakerphoneOn = false
                } else audioManager!!.isSpeakerphoneOn = callType != ConstantValues.NewCallTypes.AUDIO_CALL
                audioManager!!.isMicrophoneMute = false
                audioManager!!.stopBluetoothSco()
            //}
        }
    }
    fun setView(){
        var callTypeName : String = "CallType : "
        when(callType){
            ConstantValues.NewCallTypes.AUDIO_CALL->{
                callTypeName+= "Audio call"
            }
            ConstantValues.NewCallTypes.VIDEO_CALL->{
                callTypeName+= "Video call"
            }
            ConstantValues.NewCallTypes.SCREEN_SHARE->{
                callTypeName+= "Screen share"
            }
        }
        dataBinding.ivAudioMute.setOnClickListener {
            troopClient.selfMuteAudio()
        }
        dataBinding.tvCallLabel.text = callTypeName
        dataBinding.ivSpeaker.setOnClickListener {

        }
        dataBinding.ivVideoMute.setOnClickListener {
            troopClient.selfMuteVideo()
        }
        dataBinding.ivCamSwitch.setOnClickListener {
            troopClient.switchCamera()
        }
        dataBinding.ivEndCall.setOnClickListener {
            troopClient.endCall()
        }
        initVideo()
        troopClient.addStreams()
        dataBinding.ivAddParticipants.setOnClickListener {
            var intent = Intent(this,ParticipantsActivity::class.java)
            intent.putExtra("call_id",callId)
            intent.putExtra("participants",participatnsData.toString())
            startActivity(intent)
            //enterPipModeIfPossible()
        }
    }
    private fun confirmDialog(audio:Boolean){
        AlertDialog.Builder(this)
            .setTitle("Un mute request")
            .setMessage("Host is requesting to un mute your "+if (audio) "audio" else "video")
            .setIcon(R.drawable.log_out)
            .setPositiveButton(android.R.string.yes,
                DialogInterface.OnClickListener { dialog, whichButton ->
                    dialog.dismiss()
                    if (audio) {
                        troopClient.selfUnMuteAudio()
                    }else{
                        troopClient.selfUnMuteVideo()
                    }
                })
            .setNegativeButton(android.R.string.no, DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
            }).show()
    }
    override fun onPause() {
        enterPipModeIfPossible()
        super.onPause()
    }
    fun initVideo(){
        dataBinding.remoteStream.init(troopClient.fetchEglBaseContext().eglBaseContext,null)
        dataBinding.selfStreamView.init(troopClient.fetchEglBaseContext().eglBaseContext,null)

        dataBinding.remoteStream.setZOrderOnTop(false)
        dataBinding.selfStreamView.setZOrderOnTop(true)

        dataBinding.remoteStream.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        dataBinding.selfStreamView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)

        dataBinding.remoteStream.setEnableHardwareScaler(true)
        dataBinding.selfStreamView.setEnableHardwareScaler(true)

        dataBinding.remoteStream.setZOrderMediaOverlay(true)


    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPipModeIfPossible()
        }
        super.onBackPressed()
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun togglePipMode() {
        if (isInPipMode) {
            // Exit PiP mode
            isInPipMode = false
            enterPipMode()
        } else {
            // Enter PiP mode
            isInPipMode = true
            exitPipMode()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun enterPipMode() {
        val aspectRatio = Rational(16, 9) // Aspect ratio of the PiP window
        val pipBuilder = PictureInPictureParams.Builder()
        pipBuilder.setAspectRatio(aspectRatio)
        enterPictureInPictureMode(pipBuilder.build())
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun exitPipMode() {
        // Exit PiP mode
        if (isInPictureInPictureMode) {
            isInPipMode = false
            onBackPressed() // This will trigger the onPictureInPictureModeChanged callback
        }
    }

    fun camSwitchOption(view: View) {}
    override fun callUserStatus(jsonObject: JSONObject) {

    }

    override fun callUserPermission(jsonObject: JSONObject) {

    }

    override fun callEndCall(jsonObject: JSONObject) {
        if (jsonObject.optString("call_id")==callId){
            finishAndRemoveTask()
        }
    }

    override fun callRemoveUser(jsonObject: JSONObject) {
        if (jsonObject.optString("call_id")==callId && jsonObject.optString("uid")==MyApplication.LOGINUSERUID){
            finishAndRemoveTask()
        }
    }

    override fun callLeaveCall(jsonObject: JSONObject) {
        if (jsonObject.optString("call_id")==callId && jsonObject.optString("uid")==MyApplication.LOGINUSERUID){
            finishAndRemoveTask()
        }
    }

    override fun muteUnMuteAudio(jsonObject: JSONObject) {

    }

    override fun muteUnMuteVideo(jsonObject: JSONObject) {

    }

    override fun selfStream(jsonObject: JSONObject) {
        if (jsonObject.has("stream") && jsonObject.optJSONObject("stream")!=null && jsonObject.optJSONObject("stream")!!.has(selfCallTmUserId)
            && jsonObject.optJSONObject("stream")!!.optJSONObject(selfCallTmUserId)!=null && jsonObject.optJSONObject("stream")!!.optJSONObject(selfCallTmUserId)!!.has("video")){
            var track = jsonObject.optJSONObject("stream")!!.optJSONObject(selfCallTmUserId)!!.opt("video") as VideoTrack
            track.addSink(dataBinding.selfStreamView)
        }

    }

    override fun userStream(jsonObject: JSONObject) {
        Log.e(ConstantValues.TAG," video status => "+jsonObject.toString())
        if (jsonObject.has("stream") && jsonObject.optJSONObject("stream")!=null && jsonObject.optJSONObject("stream")!!.has(jsonObject.optString("user_id"))
            && jsonObject.optJSONObject("stream")!!.optJSONObject(jsonObject.optString("user_id"))!=null && jsonObject.optJSONObject("stream")!!.optJSONObject(jsonObject.optString("user_id"))!!.has("video")){
            var track = jsonObject.optJSONObject("stream")!!.optJSONObject(jsonObject.optString("user_id"))!!.opt("video") as VideoTrack
            Log.e(ConstantValues.TAG," video status => called  "+jsonObject.toString())
            track.addSink(dataBinding.remoteStream)
        }
    }

    override fun hostMuteVideo(jsonObject: JSONObject) {
        dataBinding.ivVideoMute.setImageResource(R.drawable.ic_video_off)
    }

    override fun hostMuteAudio(jsonObject: JSONObject) {
        dataBinding.ivAudioMute.setImageResource(R.drawable.ic_audio_off)
    }

    override fun hostRequestUnMuteAudio(jsonObject: JSONObject) {
        confirmDialog(true)
    }

    override fun hostRequestUnMuteVideo(jsonObject: JSONObject) {
        confirmDialog(false)
    //ivVideoMute.alpha  = 1f
    }

    override fun onDestroy() {
        if (dataBinding.selfStreamView!=null){
            dataBinding.selfStreamView.release()
        }
        if (dataBinding.remoteStream!=null){
            dataBinding.remoteStream.release()
        }
        if (audioManager!=null){
            audioManager!!.mode = AudioManager.MODE_NORMAL
            audioManager!!.isSpeakerphoneOn = true
        }
        super.onDestroy()
    }

    override fun onUserLeaveHint() {
        enterPipModeIfPossible()
        super.onUserLeaveHint()
    }


}