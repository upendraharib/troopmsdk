package com.tvisha.trooponprime.lib.clientModels

data class RecentMessageList(
    var id : Long = 0,
    var message_id : Long = 0,
    var sender_id : Long = 0,
    var message :String?= null,
    var message_type : Int = 0,
    var message_status :Int = 0,
    var is_sender :Int = 0,
    var unread_messages_count:Int = 0,
    var sender_name:String? = null,
    var entity_id:Long = 0,
    var entity_uid:String?=null,
    var entity :Int = 0,
    var entity_name:String?=null,
    var entity_avatar :String?=null,
    var message_time:String?=null,
    var isArchive:Int = 0,
    var isSelected:Int = 0,
    var isTyping : Int = 0,
    var typingData:String = "",
)
