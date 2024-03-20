package com.tvisha.trooponprime.lib.database.model

data class GroupMembersModel(
    var member_role:Int=0,
    var member_status:Int=0,
    var member_id:Long=0,
    var member_uid:String?=null,
    var member_profile_pic:String?=null,
    var member_name:String?=null,

)