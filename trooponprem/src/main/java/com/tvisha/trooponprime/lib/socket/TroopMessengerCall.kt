package com.tvisha.trooponprime.lib.socket

import android.util.Log
import com.tvisha.trooponprime.lib.TroopClient
import com.tvisha.trooponprime.lib.TroopMessengerClient
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.ACTIVE_CALL_ID
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.ACTIVE_CALL_TYPE
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.CALL_TM_USER_ID
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.activeCallData
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.callSocket
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.clientCallBackListener
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.participantUsersList

import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.usbVideoCapture
import com.tvisha.trooponprime.lib.call.RoomClient
import com.tvisha.trooponprime.lib.clientModels.CallParticipantData
import com.tvisha.trooponprime.lib.database.model.ParticipantUsers
import com.tvisha.trooponprime.lib.listeneres.CallBackListener
import com.tvisha.trooponprime.lib.listeneres.ClientCallBackListener
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.ConstantValues.TAG
import com.tvisha.trooponprime.lib.utils.Helper
import com.tvisha.trooponprime.lib.utils.SocketEvents
import io.socket.client.Ack
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.CameraVideoCapturer

internal object TroopMessengerCall {
    fun initiateCall(uids:List<String>, callType:Int,listener: CallBackListener){
        TroopClient.ioScope.launch {
            Log.e("sending==> ",""+uids.toString().replace("[","").replace("]","").replace(" ",""))
            var response = TroopMessengerClient.callAPIService!!.fetchUsersByUid(ConstantValues.CLIENT_VERSION,uids.toString().replace(" ","").replace("[","").replace("]","")).await()
            if (response.isSuccessful){
                var responseObject = Helper.stringToJsonObject(response.body())
                Log.e("responseObject==> ",""+responseObject.toString())
                if (responseObject!=null && responseObject.optBoolean("success")){
                    var callObjet = JSONObject()
                    var users = responseObject.optJSONObject("users")
                    Log.e("responseObject==> ",""+ CALL_TM_USER_ID+"   "+(!users!!.has(CALL_TM_USER_ID)))
                    if (!users.has(CALL_TM_USER_ID)){
                        return@launch
                    }
                    var participantUsers  = JSONObject()
                    var selfParticipants = JSONObject()
                    selfParticipants.put("video_active", if(callType==ConstantValues.NewCallTypes.VIDEO_CALL) 1 else 0)
                    selfParticipants.put("audio_active", if(callType==ConstantValues.NewCallTypes.VIDEO_CALL || callType==ConstantValues.NewCallTypes.AUDIO_CALL) 1 else 0)
                    selfParticipants.put("is_host",1)
                    selfParticipants.put("user_id",CALL_TM_USER_ID)
                    selfParticipants.put("status",ConstantValues.CallUserStatus.CALL_USER_STATUS_ACTIVE)
                    selfParticipants.put("video_muted",if(callType==ConstantValues.NewCallTypes.VIDEO_CALL || callType==ConstantValues.NewCallTypes.AUDIO_CALL) 0 else 1)
                    selfParticipants.put("audio_muted",if(callType==ConstantValues.NewCallTypes.VIDEO_CALL || callType==ConstantValues.NewCallTypes.AUDIO_CALL) 0 else 1)
                    selfParticipants.put("screen_share_active",0)
                    selfParticipants.put("control_screen",0)
                    selfParticipants.put("platform",ConstantValues.CallPlatForm.PLATFORM_ANDROID)
                    selfParticipants.put("jointly_code_active",0)
                    selfParticipants.put("uid",users.optJSONObject(TroopMessengerClient.CALL_TM_USER_ID)!!.optString("uid"))
                    selfParticipants.put("name",users.optJSONObject(TroopMessengerClient.CALL_TM_USER_ID)!!.optString("name"))
                    selfParticipants.put("avatar",users.optJSONObject(TroopMessengerClient.CALL_TM_USER_ID)!!.optString("avatar"))
                    participantUsers.put(CALL_TM_USER_ID,selfParticipants)
                    val iterator = users.keys()
                    while (iterator.hasNext()){
                        var key = iterator.next()
                        if (key!=CALL_TM_USER_ID) {
                            var selfParticipants = JSONObject()
                            selfParticipants.put("video_active", if (callType == ConstantValues.NewCallTypes.VIDEO_CALL) 1 else 0)
                            selfParticipants.put("audio_active",if (callType == ConstantValues.NewCallTypes.VIDEO_CALL || callType == ConstantValues.NewCallTypes.AUDIO_CALL) 1 else 0)
                            selfParticipants.put("is_host", 0)
                            selfParticipants.put("user_id",key)
                            selfParticipants.put("status", ConstantValues.CallUserStatus.CALL_USER_STATUS_REQUEST_PENDING)
                            selfParticipants.put("video_muted", if (callType == ConstantValues.NewCallTypes.VIDEO_CALL || callType == ConstantValues.NewCallTypes.AUDIO_CALL) 0 else 1)
                            selfParticipants.put("audio_muted", if (callType == ConstantValues.NewCallTypes.VIDEO_CALL || callType == ConstantValues.NewCallTypes.AUDIO_CALL) 0 else 1)
                            selfParticipants.put("screen_share_active", 0)
                            selfParticipants.put("control_screen", 0)
                            selfParticipants.put("platform", ConstantValues.CallPlatForm.PLATFORM_ANDROID)
                            selfParticipants.put("jointly_code_active", 0)
                            selfParticipants.put("uid", users.optJSONObject(key)!!.optString("uid"))
                            selfParticipants.put("name", users.optJSONObject(key)!!.optString("name"))
                            selfParticipants.put("avatar", users.optJSONObject(key)!!.optString("avatar"))
                            participantUsers.put(key,selfParticipants)
                        }
                    }
                    callObjet.put("user_id",CALL_TM_USER_ID)
                    callObjet.put("participants",participantUsers)
                    callObjet.put("call_type",callType)
                    callSocket!!.emit(SocketEvents.INITIATE_CALL,callObjet,Ack{args ->
                        if (args[0]!=null){
                            var callBackObject = JSONObject()
                            callBackObject.put("success",false)
                            callBackObject.put("message",args[0].toString())
                            listener.onFailure(callBackObject)
                            return@Ack
                        }else if (args[1]!=null){
                            Log.e("callback===> "," initiate 1 "+args[1].toString())
                            if (RoomClient.isAlreadyStarted){
                                RoomClient.release(false)
                            }
                            Log.e("callback===> "," initiate "+args[1].toString())
                            var jsonObject = args[1] as JSONObject
                            jsonObject.put("participants",participantUsers.toString())
                            jsonObject.put("initiator_id", CALL_TM_USER_ID)
                            jsonObject.put("call_type", callType)
                            listener.onSuccess(jsonObject)
                            jsonObject.put("participants",participantUsers)
                            ACTIVE_CALL_ID = jsonObject.optString("call_id")
                            ACTIVE_CALL_TYPE = jsonObject.optInt("call_type")
                            activeCallData  = jsonObject
                            addParticipantsIntoList()
                            Log.e(ConstantValues.TAG,"  initiate  "+(callSocket==null)+"   "+RoomClient.isAlreadyStarted)
                            if (!RoomClient.isAlreadyStarted){
                                RoomClient.initialize(CALL_TM_USER_ID, ACTIVE_CALL_ID, ACTIVE_CALL_TYPE,false,true,
                                    callType==ConstantValues.NewCallTypes.VIDEO_CALL
                                )
                            }
                        }

                        //RoomClient.initialize(TroopMessengerClient.CALL_TM_USER_ID,)
                    })

                }

            }
        }


        /*jsonObject.put("user_id",TroopMessengerClient.CALL_TM_USER_ID)
        var participantsData : List<ParticipantUsers> = ArrayList()
        var data = Helper.stringToJsonArray(Gson().toJson(participantsData))
        jsonObject.put("participants",data)
        jsonObject.put("call_type",callType)
        TroopMessengerClient.callSocket!!.emit(SocketEvents.INITIATE_CALL)*/
    }

    fun rejectCall() {
        if (!ACTIVE_CALL_ID.isNullOrEmpty()) {
            if (callSocket != null && callSocket!!.connected()) {
                var endCallObject = JSONObject()
                endCallObject.put("call_id", ACTIVE_CALL_ID)
                endCallObject.put("permission", 0)
                endCallObject.put("user_id", CALL_TM_USER_ID)
                callSocket!!.emit(SocketEvents.CALL_PERMISSION, endCallObject, Ack { args ->
                    Log.e("callback===> ", " permission reject " + args[0].toString())
                    ACTIVE_CALL_ID = ""
                    activeCallData = JSONObject()
                    usbVideoCapture=null
                    participantUsersList = ArrayList()
                })

            }
        }
        if (RoomClient.isAlreadyStarted){
            RoomClient.release(false)
        }
    }
    fun acceptCall(listener: CallBackListener){
        if (!ACTIVE_CALL_ID.isNullOrEmpty()) {
            if (callSocket != null && callSocket!!.connected()) {
                var endCallObject = JSONObject()
                endCallObject.put("call_id", ACTIVE_CALL_ID)
                endCallObject.put("permission", 1)
                endCallObject.put("user_id", CALL_TM_USER_ID)
                callSocket!!.emit(SocketEvents.CALL_PERMISSION, endCallObject, Ack { args ->
                    Log.e("callback===> ", " permission accept " + args[0].toString())
                    var jsonObject = args[0] as JSONObject
                    listener.onSuccess(jsonObject)
                    if (jsonObject.optBoolean("success")) {
                        activeCallData.put("participants",jsonObject.optJSONObject("participants"))
                    }
                })

            }
        }
    }
    fun endCall(){
        if (!ACTIVE_CALL_ID.isNullOrEmpty()) {
            if (callSocket != null && callSocket!!.connected()) {
                var endCallObject = JSONObject()
                endCallObject.put("call_id", ACTIVE_CALL_ID)
                endCallObject.put("user_id", CALL_TM_USER_ID)
                callSocket!!.emit(SocketEvents.END_CALL, endCallObject, Ack { args ->
                    Log.e("callback===> ", " permission accept " + args[0].toString())
                    ACTIVE_CALL_ID = ""
                    activeCallData = JSONObject()
                    usbVideoCapture = null
                    participantUsersList = ArrayList()
                })
            }
        }
        if (RoomClient.isAlreadyStarted){
            RoomClient.release(false)
        }
    }
    fun addUsersToCall(uids: List<String>){
        TroopClient.ioScope.launch {
            Log.e("adding==> ", "" + uids.toString().replace("[", "").replace("]", "").replace(" ", ""))
            var response = TroopMessengerClient.callAPIService!!.fetchUsersByUid(ConstantValues.CLIENT_VERSION, uids.toString().replace(" ", "").replace("[", "").replace("]", "")).await()
            if (response.isSuccessful) {
                var responseObject = Helper.stringToJsonObject(response.body())
                Log.e("responseObject==> ", "" + responseObject.toString())
                if (responseObject != null && responseObject.optBoolean("success")) {
                    var participantUsers  = JSONObject()
                    var users = responseObject.optJSONObject("users")
                    val iterator = users.keys()
                    while (iterator.hasNext()){
                        var key = iterator.next()
                        if (key!=CALL_TM_USER_ID && !activeCallData.optJSONObject("participants")!!.has(key)) {
                            var selfParticipants = JSONObject()
                            selfParticipants.put("video_active", if (ACTIVE_CALL_TYPE == ConstantValues.NewCallTypes.VIDEO_CALL) 1 else 0)
                            selfParticipants.put("audio_active",if (ACTIVE_CALL_TYPE == ConstantValues.NewCallTypes.VIDEO_CALL || ACTIVE_CALL_TYPE == ConstantValues.NewCallTypes.AUDIO_CALL) 1 else 0)
                            selfParticipants.put("is_host", 0)
                            selfParticipants.put("user_id",key)
                            selfParticipants.put("status", ConstantValues.CallUserStatus.CALL_USER_STATUS_REQUEST_PENDING)
                            selfParticipants.put("video_muted", if (ACTIVE_CALL_TYPE == ConstantValues.NewCallTypes.VIDEO_CALL || ACTIVE_CALL_TYPE == ConstantValues.NewCallTypes.AUDIO_CALL) 0 else 1)
                            selfParticipants.put("audio_muted", if (ACTIVE_CALL_TYPE == ConstantValues.NewCallTypes.VIDEO_CALL || ACTIVE_CALL_TYPE == ConstantValues.NewCallTypes.AUDIO_CALL) 0 else 1)
                            selfParticipants.put("screen_share_active", 0)
                            selfParticipants.put("control_screen", 0)
                            selfParticipants.put("platform", ConstantValues.CallPlatForm.PLATFORM_ANDROID)
                            selfParticipants.put("jointly_code_active", 0)
                            selfParticipants.put("uid", users.optJSONObject(key)!!.optString("uid"))
                            selfParticipants.put("name", users.optJSONObject(key)!!.optString("name"))
                            selfParticipants.put("avatar", users.optJSONObject(key)!!.optString("avatar"))
                            participantUsers.put(key,selfParticipants)
                        }
                    }
                    if (users!=null && users.length()>0 && participantUsers.length()>0){

                        var jsonObject = JSONObject()
                        jsonObject.put("call_id", ACTIVE_CALL_ID)
                        jsonObject.put("participants",participantUsers)
                        callSocket!!.emit(SocketEvents.ADD_CALL_USER,jsonObject, Ack { args ->
                            if (args.isNotEmpty()) {
                                for (element in args) {
                                    if (element !=null) {
                                        var jsonObject  = element as JSONObject
                                        if (jsonObject.has("participants")){
                                            var participants = jsonObject.optJSONObject("participants")
                                            addNewParticipants(jsonObject)
                                        }
                                        Log.e(TAG, " adduser==> callback " + element.toString())
                                    }
                                }
                            }
                        })
                    }
                }
            }
        }
    }
    fun muteMyTrack(kind:String){
        if (RoomClient.localAudioTrack==null && kind==ConstantValues.CallKinds.KIND_AUDIO){
            addAudioStream()
            return
        }
        if (RoomClient.localVideoTrack==null && kind==ConstantValues.CallKinds.KIND_VIDEO){
            switchToVideoCall()
            return
        }

        var jsonObject = JSONObject()
        if (kind==ConstantValues.CallKinds.KIND_AUDIO){
            if (!RoomClient.localAudioTrack!!.enabled()){
                unMuteMyTrack(kind)
                return
            }
            activeCallData.optJSONObject("participants")!!.optJSONObject(CALL_TM_USER_ID)!!.put("audio_muted",1)
            RoomClient.localAudioTrack!!.setEnabled(false)
            jsonObject.put("call_id", ACTIVE_CALL_ID)
            jsonObject.put("audio_muted",1)
            jsonObject.put("user_id", CALL_TM_USER_ID)
            callSocket!!.emit(SocketEvents.MUTE_CALL_AUDIO,jsonObject, Ack { args ->

            })
        }else if (kind==ConstantValues.CallKinds.KIND_VIDEO){
            if (!RoomClient.localVideoTrack!!.enabled()){
                unMuteMyTrack(kind)
                return
            }
            activeCallData.optJSONObject("participants")!!.optJSONObject(CALL_TM_USER_ID)!!.put("video_muted",1)
            RoomClient.localVideoTrack!!.setEnabled(false)

            jsonObject.put("call_id", ACTIVE_CALL_ID)
            jsonObject.put("video_muted",1)
            jsonObject.put("user_id", CALL_TM_USER_ID)
            callSocket!!.emit(SocketEvents.MUTE_CALL_VIDEO,jsonObject, Ack { args ->

            })
        }
        updateParticipants(jsonObject,if (kind==ConstantValues.CallKinds.KIND_VIDEO) "video_mute" else "audio_mute")
    }
    private fun addAudioStream(){
        RoomClient.addAudio = true
        RoomClient.setCallType(ConstantValues.NewCallTypes.AUDIO_CALL)
        ACTIVE_CALL_TYPE = ConstantValues.NewCallTypes.AUDIO_CALL
        RoomClient.addAudioStream()
        var jsonObject = JSONObject()
        jsonObject.put("call_id", ACTIVE_CALL_ID)
        jsonObject.put("call_type", ACTIVE_CALL_TYPE)
        jsonObject.put("user_id", CALL_TM_USER_ID)
        callSocket!!.emit(SocketEvents.CALL_STREAM_REQUEST,jsonObject, Ack {

        })
        updateParticipants(jsonObject, "audio_add")
    }
    fun unMuteMyTrack(kind:String){

        var jsonObject = JSONObject()
        if (kind==ConstantValues.CallKinds.KIND_AUDIO){
            activeCallData.optJSONObject("participants")!!.optJSONObject(CALL_TM_USER_ID)!!.put("audio_muted",0)
            RoomClient.localAudioTrack!!.setEnabled(true)
            jsonObject.put("call_id", ACTIVE_CALL_ID)
            jsonObject.put("audio_muted",0)
            jsonObject.put("user_id", CALL_TM_USER_ID)
            callSocket!!.emit(SocketEvents.MUTE_CALL_AUDIO,jsonObject, Ack { args ->

            })
        }else if (kind==ConstantValues.CallKinds.KIND_VIDEO){
            activeCallData.optJSONObject("participants")!!.optJSONObject(CALL_TM_USER_ID)!!.put("video_muted",0)
            RoomClient.localAudioTrack!!.setEnabled(true)
            jsonObject.put("call_id", ACTIVE_CALL_ID)
            jsonObject.put("video_muted",0)
            jsonObject.put("user_id", CALL_TM_USER_ID)
            callSocket!!.emit(SocketEvents.MUTE_CALL_VIDEO,jsonObject, Ack { args ->

            })
        }
        updateParticipants(jsonObject,if (kind==ConstantValues.CallKinds.KIND_VIDEO) "video_mute" else "audio_mute")
    }
    fun switchCamera(){

        if (RoomClient.localVideoTrack!=null){
            val cameraVideoCapture = RoomClient.videoCapturer as CameraVideoCapturer
            if (cameraVideoCapture!=null){
                cameraVideoCapture.switchCamera(null)
            }
        }
    }
    fun switchToVideoCall(){
        if (callSocket!=null && !callSocket!!.connected()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Check network connection!")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        if (RoomClient.localAudioTrack==null && RoomClient.localVideoTrack==null){
            RoomClient.addVideo = true
            RoomClient.setCallType(ConstantValues.NewCallTypes.VIDEO_CALL)
            ACTIVE_CALL_TYPE = ConstantValues.NewCallTypes.VIDEO_CALL
            RoomClient.addVideoStream()
            var jsonObject = JSONObject()
            jsonObject.put("call_id", ACTIVE_CALL_ID)
            jsonObject.put("call_type", ACTIVE_CALL_TYPE)
            jsonObject.put("user_id", CALL_TM_USER_ID)
            callSocket!!.emit(SocketEvents.CALL_STREAM_REQUEST,jsonObject, Ack {

            })
            updateParticipants(jsonObject, "video_add")
        } else
        if (activeCallData.optJSONObject("participants")!!.optJSONObject(CALL_TM_USER_ID)!!.optInt("video_active")==0){
            var jsonObject = JSONObject()
            jsonObject.put("call_id", ACTIVE_CALL_ID)
            jsonObject.put("video_active",1)
            jsonObject.put("user_id", CALL_TM_USER_ID)
            callSocket!!.emit(SocketEvents.CALL_USER_VIDEO_STATUS_UPDATED,jsonObject, Ack {  })
            activeCallData.optJSONObject("participants")!!.optJSONObject(CALL_TM_USER_ID)!!.put("video_active",1)
            RoomClient.addVideo =true
            RoomClient.addVideoStream()
        }
    }
    fun hostMuteAudio(userId:String){
        if (callSocket!=null && !callSocket!!.connected()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Check network connection!")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        if (userId.isNullOrEmpty()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Invalid data user_id ")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        var jsonObject = JSONObject()
        jsonObject.put("call_id", ACTIVE_CALL_ID)
        jsonObject.put("mute_audio",1)
        jsonObject.put("user_id", userId)
        callSocket!!.emit(SocketEvents.HOST_MUTE_AUDIO,jsonObject, Ack {  })
        if (activeCallData!=null && activeCallData.has("participants") && activeCallData.optJSONObject("participants")!=null && activeCallData.optJSONObject("participants")!!.has(userId)){
            activeCallData.optJSONObject("participants")!!.optJSONObject(userId)!!.put("audio_muted",1)
            jsonObject.put("audio_muted",1)
            updateParticipants(jsonObject,"audio_mute")
        }
    }
    fun hostMuteVideo(userId:String){
        if (callSocket!=null && !callSocket!!.connected()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Check network connection!")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        if (userId.isNullOrEmpty()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Invalid data user_id ")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        var jsonObject = JSONObject()
        jsonObject.put("call_id", ACTIVE_CALL_ID)
        jsonObject.put("mute_video",1)
        jsonObject.put("user_id", userId)
        callSocket!!.emit(SocketEvents.HOST_MUTE_VIDEO,jsonObject, Ack {  })
        if (activeCallData!=null && activeCallData.has("participants") && activeCallData.optJSONObject("participants")!=null && activeCallData.optJSONObject("participants")!!.has(userId)){
            activeCallData.optJSONObject("participants")!!.optJSONObject(userId)!!.put("video_muted",1)
            jsonObject.put("video_muted",1)
            updateParticipants(jsonObject,"video_mute")
        }
    }
    fun hostUnMuteAudioVideoRequest(userId: String,type:Int){
        if (callSocket!=null && !callSocket!!.connected()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Check network connection!")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        if (userId.isNullOrEmpty()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Invalid data user_id ")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        var jsonObject = JSONObject()
        jsonObject.put("call_id", ACTIVE_CALL_ID)
        jsonObject.put("type",type)
        jsonObject.put("user_id", userId)
        callSocket!!.emit(SocketEvents.CALL_MUTE_REQUEST,jsonObject, Ack {  })
    }
    fun removeUser(userId: String){
        if (callSocket!=null && !callSocket!!.connected()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Check network connection!")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        if (userId.isNullOrEmpty()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Invalid data user_id ")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        var jsonObject = JSONObject()
        jsonObject.put("call_id", ACTIVE_CALL_ID)
        jsonObject.put("user_id", userId)
        callSocket!!.emit(SocketEvents.CALL_REMOVE_USER,jsonObject, Ack {  })
        if (activeCallData!=null && activeCallData.has("participants") && activeCallData.optJSONObject("participants")!=null && activeCallData.optJSONObject("participants")!!.has(userId)){
            activeCallData.optJSONObject("participants")!!.remove(userId)
            updateParticipants(jsonObject,"remove")
        }
    }
    fun updateHost(status:Int,userId: String){
        if (callSocket!=null && !callSocket!!.connected()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Check network connection!")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        if (userId.isNullOrEmpty()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Invalid data user_id ")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        var jsonObject = JSONObject()
        jsonObject.put("call_id", ACTIVE_CALL_ID)
        jsonObject.put("is_host",status)
        jsonObject.put("user_id", userId)
        callSocket!!.emit(SocketEvents.CALL_UPDATE_HOST_UPDATED,jsonObject, Ack {  })
        if (activeCallData!=null && activeCallData.has("participants") && activeCallData.optJSONObject("participants")!=null && activeCallData.optJSONObject("participants")!!.has(userId)){
            activeCallData.optJSONObject("participants")!!.optJSONObject(userId)!!.put("is_host",status)
            updateParticipants(jsonObject,"host_update")
        }
    }
    fun retryUser(userId: String){
        if (callSocket!=null && !callSocket!!.connected()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Check network connection!")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        if (userId.isNullOrEmpty()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Invalid data user_id ")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        var jsonObject = JSONObject()
        jsonObject.put("call_id", ACTIVE_CALL_ID)
        jsonObject.put("user_id", userId)
        callSocket!!.emit(SocketEvents.CALL_USER_RETRY,jsonObject, Ack {args->
            if (args != null) {
                val objects = args as Array<Any>
                if (objects != null && objects.isNotEmpty()) {
                    if (objects[0] != null && objects[0] != "null") {
                        var jsonObject = JSONObject()
                        jsonObject.put("success",false)
                        jsonObject.put("message",args[0].toString())
                        clientCallBackListener.tmCallError(jsonObject)
                        return@Ack
                    }

                }
            }
        })
    }
    fun acceptRejectTheJoinCallPermission(userId: String,permission:Int){
        if (callSocket!=null && !callSocket!!.connected()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Check network connection!")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        if (userId.isNullOrEmpty()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Invalid data user_id ")
            clientCallBackListener.tmCallError(jsonObject)
            return
        }
        var jsonObject = JSONObject()
        jsonObject.put("call_id", ACTIVE_CALL_ID)
        jsonObject.put("permission",permission)
        jsonObject.put("user_id", userId)
        callSocket!!.emit(SocketEvents.JOIN_CALL_PERMISSION,jsonObject, Ack { args ->
        })
    }
    fun isCallActive(callId:String):Boolean{
        var status = false
        var jsonObject = JSONObject()
        jsonObject.put("call_id",callId)
        callSocket!!.emit(SocketEvents.GET_CALL_DATA,callId, Ack { args ->
            if (args.size>0){
                for (element in args) {
                    if (element!=null){
                        var jsonObject = element as JSONObject
                        if (jsonObject.optBoolean("success")){
                            status = true
                            if (callId== ACTIVE_CALL_ID) {
                                activeCallData = jsonObject.optJSONObject("data")
                            }
                        }
                    }
                }
            }
        })
        return status
    }

    fun fetchParticipants(): ArrayList<ParticipantUsers> {
        return participantUsersList
    }
    fun addParticipantsIntoList(){
        if (activeCallData!=null && activeCallData.has("participants")){
            val participantData = activeCallData.optJSONObject("participants")
            val iterator = participantData.keys()
            while (iterator.hasNext()){
                var key = iterator.next()
                var userObject = participantData!!.optJSONObject(key)
                var participantUsers = ParticipantUsers()
                participantUsers.callStatus =  userObject!!.optInt("status")
                participantUsers.videoAdded = userObject.optInt("video_active")
                participantUsers.audioAdded = userObject.optInt("audio_active")
                participantUsers.isHost = userObject.optInt("is_host")
                participantUsers.user_id = userObject.optString("user_id")
                participantUsers.callStatus = userObject.optInt("status")
                participantUsers.isVideoMuted = userObject.optInt("video_muted")
                participantUsers.isAudioMuted = userObject.optInt("audio_muted")
                participantUsers.screenShareActive = userObject.optInt("screen_share_active")
                participantUsers.platForm = userObject.optInt("platform")
                participantUsers.jointlyCodeActive = userObject.optInt("jointly_code_active")
                participantUsers.uid = userObject.optString("uid")
                participantUsers.name = userObject.optString("name")
                participantUsers.userpic = userObject.optString("avatar")
                participantUsersList.add(participantUsers)
            }
        }
        //updateUserList.postValue(participantUsersList)
    }
    fun addNewParticipants(jsonObject: JSONObject){
        if (jsonObject!=null && jsonObject.has("participants")){
            val participantData = jsonObject.optJSONObject("participants")
            val iterator = participantData.keys()
            while (iterator.hasNext()){
                var key = iterator.next()
                var userObject = participantData!!.optJSONObject(key)
                var participantUsers = ParticipantUsers()
                participantUsers.callStatus =  userObject!!.optInt("status")
                participantUsers.videoAdded = userObject.optInt("video_active")
                participantUsers.audioAdded = userObject.optInt("audio_active")
                participantUsers.isHost = userObject.optInt("is_host")
                participantUsers.user_id = userObject.optString("user_id")
                participantUsers.callStatus = userObject.optInt("status")
                participantUsers.isVideoMuted = userObject.optInt("video_muted")
                participantUsers.isAudioMuted = userObject.optInt("audio_muted")
                participantUsers.screenShareActive = userObject.optInt("screen_share_active")
                participantUsers.platForm = userObject.optInt("platform")
                participantUsers.jointlyCodeActive = userObject.optInt("jointly_code_active")
                participantUsers.uid = userObject.optString("uid")
                participantUsers.name = userObject.optString("name")
                participantUsers.userpic = userObject.optString("avatar")
                participantUsersList.add(participantUsers)
            }
        }
        //updateUserList.postValue(participantUsersList)
    }
    fun updateParticipants(jsonObject: JSONObject, type:String){
        var user_id = jsonObject.optString("user_id")
        Log.e(TAG," update participants "+jsonObject.toString())
        if (participantUsersList.isNotEmpty()){
            for (i in 0 until participantUsersList.size){
                if (participantUsersList[i].user_id==user_id){
                    when(type){
                        "video_add" ->{
                            participantUsersList[i].audioAdded = 1
                            participantUsersList[i].videoAdded = 1
                        }
                        "audio_add" ->{
                            participantUsersList[i].audioAdded = 1
                        }
                        "audio_mute" -> {
                            participantUsersList[i].isAudioMuted = jsonObject.optInt("audio_muted")
                        }
                        "video_mute" -> {
                            participantUsersList[i].isVideoMuted = jsonObject.optInt("video_muted")
                        }
                        "video_status_update"->{
                            participantUsersList[i].videoAdded = jsonObject.optInt("video_active")
                            participantUsersList[i].isVideoMuted = 0

                        }
                        "user_status_update" -> {
                            participantUsersList[i].callStatus = jsonObject.optInt("status")
                        }
                        "host_update" ->{
                            participantUsersList[i].isHost = jsonObject.optInt("is_host")
                        }
                        "remove"->{
                            participantUsersList.removeAt(i)
                        }
                    }
                    break
                }
            }
            //updateUserList.postValue(participantUsersList)

        }
    }

    fun checkSelfHost() :Boolean{
        if (activeCallData!=null && activeCallData.has("participants")){
            if (activeCallData.optJSONObject("participants")!!.has(CALL_TM_USER_ID) && activeCallData.optJSONObject("participants")!!.optJSONObject(CALL_TM_USER_ID)!=null){
                return activeCallData.optJSONObject("participants")!!.optJSONObject(CALL_TM_USER_ID)!!.optInt("is_host")==1
            }
        }
        return false
    }
}