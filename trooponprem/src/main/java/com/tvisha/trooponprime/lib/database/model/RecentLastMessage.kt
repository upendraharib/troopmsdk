package com.tvisha.trooponprime.lib.database.model

data class RecentLastMessage(
    var id:Long=0,
    var message_id :Long = 0,
    var member_name:String? = null,
    var message_status:Int=0,
    var is_sender:Int=0,
    var entity:Int=0,
    var unread_messages_count:Int=0,
    var contact_id:String?= null,
    var sender_id:Long= 0,
    var receiver_id : Long = 0,
    var entity_id :Long = 0,
    var message :String?= null,
    var message_type:Int = 0,
    var created_at :String?= null,
    var member_id:Long = 0,
)
