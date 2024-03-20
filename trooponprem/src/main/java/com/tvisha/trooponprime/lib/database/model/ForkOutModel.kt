package com.tvisha.trooponprime.lib.database.model

import java.io.Serializable

data class ForkOutModel(
    var entity_type : Int = 0,
    var is_favourite : Int = 0,
    var group_type : Int = 0,
    var is_muted : Int = 0,
    var status : Int = 0,
    var entity_id:Long = 0,
    var entity_name:String?=null,
    var search_name:String?=null,
    var entity_avatar:String?=null,
    var created_at:String?=null,
    var orange:Int = 0,
    var user_count:Int = 0,
    var type:Int = 0,
    var isSelected:Int = 0
):Serializable
