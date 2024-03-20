package com.tvisha.trooponprime.lib.database.model

data class MessageMap(
    var ID:Long=0,
    var message_id:Long=0,
    var message:String?=null,
    var attachment:String? =null,
    var message_type:Int = 0,
    var sender_id:Long=0,
    var local_attachment_path:String?=null,
    var attachment_downloaded:Int = 0,
)