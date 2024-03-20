package com.tvisha.trooponprime.lib.database.model

data class CommonGroups(
    var group_id:Long = 0,
    var group_name:String= "",
    var members_count:Int = 0,
    var group_avatar:String="",
    var group_type:Int=0,
    var is_orange_group:Int=0
)
