package com.tvisha.trooponprime.lib.clientModels

data class MessengerGroupModel(
    var group_id:Long=0,
    var created_by:String?= null,
    var group_name:String?=null,
    var group_avatar:String?=null,
    var group_description:String?=null,
    var created_at:String?=null,
    var conversation_reference_id:String?=null,
    var is_active:Int=0,
    var is_archived:Int=0,

    )
