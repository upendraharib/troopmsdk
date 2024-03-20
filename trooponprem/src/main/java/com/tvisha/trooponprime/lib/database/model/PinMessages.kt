package com.tvisha.trooponprime.lib.database.model

data class PinMessages(
    var ID:Long = 0,
    var workspaceid:String?=null,
    var pinnedUserName:String? = null,
    var pinnedBy:Long =0,
    var messageID:Long = 0,
    var messageType:Int = 0,
    var message:String? =null,
    var caption:String? =null,
    var attachment:String? =null,
    var pinnedAt:String? = null,
    var senderId:Long = 0,
    var receiverId:Long = 0,
    var isGroup:Int = 0,
    var isDownloaded:Int = 0,
    var filePath:String? = null
)
