package com.tvisha.trooponprime.lib.database.model

import java.io.Serializable

data class ParticipantUsers(
    var isExternalUser:Int=0,
    var email  : String? = null,
    var name  : String? = null,
    var userpic  : String? = null,
    var isHost  :Int = 0,
    var isOrangeMember :Int = 0,
    var meetingUserStatus:Int = 0,
    var user_id:String = "",
    var uid:String = "",
    var user_status:Int = 0,
    var isSelected:Int = 0,
    var callStatus:Int = 0,
    var videoAdded:Int = 0,
    var audioAdded:Int = 0,
    var isVideoMuted:Int = 0,
    var isAudioMuted:Int = 0,
    var streamType:Int = 0,
    var isPin:Int = 0,
    var screenShareActive:Int = 0,
    var jointlyCodeActive:Int = 0,
    var callConnectionStatus:String = "",
    var confStatus:Int = 0,
    var clickable:Int = 1,
    var platForm:Int = 1
):Serializable
