package com.tvisha.trooponprime.lib.clientModels

data class CallParticipantData(
    var video_active:Int = 0,
    var audio_active:Int = 0,
    var is_host:Int = 0,
    var status:Int = 0,
    var video_muted:Int = 0,
    var audio_muted:Int = 0,
    var screen_share_active:Int = 0,
    var control_screen:Int = 0,
    var platform:Int = 0,
    var jointly_code_active:Int = 0,
    var user_id:Long = 0,
    var uid:String? = null,

)
