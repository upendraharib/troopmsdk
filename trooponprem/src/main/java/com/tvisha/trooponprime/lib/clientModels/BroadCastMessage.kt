package com.tvisha.trooponprime.lib.clientModels

data class BroadCastMessage(
    var message_id:Long = 0,
    var title :String = "",
    var message:String = "",
    var attachment:String="",
    var created_at:String="",
    var updated_at:String="",
)
