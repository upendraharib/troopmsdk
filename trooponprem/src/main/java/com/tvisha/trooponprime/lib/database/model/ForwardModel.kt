package com.tvisha.trooponprime.lib.database.model

data class ForwardModel(
    var entity_id:Long = 0,
    var entity:Int = 0,
    var entity_name:String?=null,
    var entity_uid:String?=null,
    var entity_avatar:String?=null,
    var message_time:String?=null,
    var isSelected:Int = 0,

)
