package com.tvisha.trooponprime.lib.database.model

data class NotificationData(
    var dummy_group_by:Long = 0,
    var entity_type:Int = 0,
    var is_downloaded : Int = 0,
    var attachment:String?= null,
    var workspace_id:String?=null,
    var message_hash:String?=null,
    var status:Int = 0,
    var id : Long = 0,
    var message_type:Int =0,
    var message:String?= null,
    var created_at:String?=null,
    var is_read:Int = 0,
    var sender_id:Long=0,
    var is_group:Int = 0,
    var group_type:Int = 0,
    var user_role:Int = 0,
    var receiver_id:Long = 0,
    var sender_name:String?=null,
    var sender_profile_pic:String?=null,
    var user_status:Int = 0,
    var company_name:String?=null,
    var receiver_name:String?=null,
    var receiver_profile_pic:String?=null,
    var is_entity_muted:Int = 0,
    var is_group_active:Int = 0
)
