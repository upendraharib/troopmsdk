package com.tvisha.trooponprime.lib.clientModels

data class CreateGroupMembers(
    var user_id:Long = 0,
    var name:String = "",
    var role:Int = 0,
    var status:Int = 0,
)
