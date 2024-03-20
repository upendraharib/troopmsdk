package com.tvisha.trooponprime.lib.clientModels

data class StoryModel(
    var id:Long = 0,
    var user_id:Long = 0,
    var type:Int = 0,
    var data:String?=null,
    var shared_to:String?=null,
    var seen:Int = 0,
    var created_at:String?=null,
    var updated_at:String?=null,
    var profile_pic:String?=null,
    var user_name:String?=null,

)