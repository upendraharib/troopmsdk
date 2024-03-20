package com.tvisha.trooponprime.lib.listeneres

import org.json.JSONObject

interface ClientCallBackListener {
    fun clientConnected()
    fun clientDisConnected()
    fun tmError(jsonObject: JSONObject)
    fun loginResponse(jsonObject: JSONObject)
    fun messageSaved(jsonObject: JSONObject)
    fun typing(jsonObject: JSONObject)
    fun userPresenceResponse(jsonObject: JSONObject)
    fun reportUserResponse(jsonObject: JSONObject)
    fun blockUserResponse(jsonObject: JSONObject)
    fun likeUnLikeMessageResponse(jsonObject: JSONObject)
    fun reportMessageResponse(jsonObject: JSONObject)
    fun deleteChatResponse(jsonObject: JSONObject)
    fun recallResponse(jsonObject: JSONObject)
    fun deleteResponse(jsonObject: JSONObject)
    fun pushNotification(jsonObject: JSONObject)
    fun loadingMessages(status:Boolean)


    fun tmCallError(jsonObject: JSONObject)
    fun onCallRequest(jsonObject: JSONObject)
    fun onCallPermission(jsonObject: JSONObject)
    fun onCallParticipantStatusUpdated(jsonObject: JSONObject)
    fun onEndCall(jsonObject: JSONObject)
    fun onCallNewParticipants(jsonObject: JSONObject)
    fun onLeaveCall(jsonObject: JSONObject)
    fun onCallUserVideoStatusUpdated(jsonObject: JSONObject)
    fun onMuteCallAudio(jsonObject: JSONObject)
    fun onMuteCallVideo(jsonObject: JSONObject)
    fun selfStream(jsonObject: JSONObject)
    fun userStream(jsonObject: JSONObject)
    fun onSelfVideoMute(jsonObject: JSONObject)
    fun onSelfAudioMute(jsonObject: JSONObject)
    fun hostAudioMuteRequest(jsonObject: JSONObject)
    fun hostVideoMuteRequest(jsonObject: JSONObject)

}