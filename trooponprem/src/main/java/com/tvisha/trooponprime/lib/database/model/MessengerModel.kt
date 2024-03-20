package com.tvisha.trooponprime.lib.database.model

data class MessengerModel(
    var ID:Long= 0,
    var message_id:Long = 0,
    var message_type:Int = 0,
    var sender_id:Long = 0,
    var receiver_id:Long = 0,
    var is_group:Int = 0,
    var status:Int = 0,
)
