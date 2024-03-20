package com.tvisha.trooponprime.calls

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.util.Rational
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.tvisha.trooponprime.MyApplication
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.CallPreviewLayoutBinding
import com.tvisha.trooponprime.lib.call.RoomClient
import com.tvisha.trooponprime.lib.listeneres.CallBackListener
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper
import org.json.JSONObject


class CallPreviewActivity : AppCompatActivity(), MyApplication.CallListeners {
    private var isInPipMode = false
    var isMineCall:Boolean = false
    var callType:Int = 0
    var initiatorName:String = ""
    var participatnsData:JSONObject = JSONObject()
    var callId:String = ""
    var initiatorId:String = ""
    lateinit var dataBinding:CallPreviewLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.call_preview_layout)
        isMineCall = intent.getBooleanExtra("isMineCall",false)!!
        callType = intent.getIntExtra("call_type",ConstantValues.NewCallTypes.AUDIO_CALL)
        participatnsData = Helper.stringToJsonObject(intent.getStringExtra("participants"))!!
        callId = intent.getStringExtra("call_id")!!
        initiatorId = intent.getStringExtra("initiator_id")!!
        var app = application as MyApplication
        app.setCallsListeners(this)
        setView()
    }
    fun setView(){
        dataBinding.clParentView.setBackgroundColor(if (isMineCall) ContextCompat.getColor(this,R.color.conv_mine_bg_color) else ContextCompat.getColor(this,R.color.conv_other_bg_color))
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
        dataBinding.ivAccept.visibility = if (isMineCall) View.GONE else View.VISIBLE
        dataBinding.ivReject.visibility = if (isMineCall) View.GONE else View.VISIBLE
        dataBinding.ivEndCall.visibility = if (isMineCall) View.VISIBLE else View.GONE

        dataBinding.tvCallType.text = callTypeName



        if (participatnsData.has(initiatorId)){
            initiatorName = participatnsData.optJSONObject(initiatorId)!!.optString("name")
        }

        dataBinding.tvCallingName.text = initiatorName

        dataBinding.ivAccept.setOnClickListener {
            troopClient.callAccept(object :CallBackListener{
                override fun onSuccess(jsonObjet: JSONObject) {
                    moveToCall(jsonObjet)
                }

                override fun onFailure(jsonObjet: JSONObject) {

                }
            })

        }
        dataBinding.ivReject.setOnClickListener {
            troopClient.rejectCall()
        }
        dataBinding.ivEndCall.setOnClickListener {
            troopClient.endCall()
        }
    }
    private var callBackListener = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1->{
                    var jsonObject = msg.obj as JSONObject
                    moveToCall(jsonObject)
                }
            }
        }
    }
    fun moveToCall(jsonObject: JSONObject){
        if (jsonObject.optBoolean("success")){
            var intent = Intent(this,CallActivity::class.java)
            intent.putExtra("call_id",callId)
            intent.putExtra("call_type",callType)
            intent.putExtra("participants",jsonObject.optString("participants"))
            intent.putExtra("isMineCall",isMineCall)
            startActivity(intent)
            finishAndRemoveTask()
        }
    }
    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            togglePipMode()
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

    override fun callUserStatus(jsonObject: JSONObject) {

    }

    override fun callUserPermission(jsonObject: JSONObject) {
        Log.e("callpermission==> "," user "+jsonObject.toString())
        if (jsonObject.optInt("permission")==1){
            var intent = Intent(this,CallActivity::class.java)
            intent.putExtra("call_id",callId)
            intent.putExtra("call_type",callType)
            intent.putExtra("participants",participatnsData.toString())
            intent.putExtra("isMineCall",isMineCall)
            startActivity(intent)
            finishAndRemoveTask()
        }
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

    }

    override fun userStream(jsonObject: JSONObject) {

    }

    override fun hostMuteVideo(jsonObject: JSONObject) {

    }

    override fun hostMuteAudio(jsonObject: JSONObject) {
    }

    override fun hostRequestUnMuteAudio(jsonObject: JSONObject) {
    }

    override fun hostRequestUnMuteVideo(jsonObject: JSONObject) {

    }
}