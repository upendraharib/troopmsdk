package com.tvisha.trooponprime.lib.clientModels

data class MediaModel(
    var message_id:Long = 0,
    var attachment:String?= null,
    var message_type:Int = 1,
    var preview_link:String?= null,
    var attachment_downloaded:Int=0,
        var created_at:String? = null,
    var ID:Long=  0,
    var sender_name:String?= null,
)