package com.tvisha.trooponprime.lib.database.model

internal data class StoryModel(
    var ID:Long=0,
    var user_id:Long=0,
    var name:String="",
    var user_avatar:String="",
    var type:Int= 0,
    var data:String="",
    var seen :Int=0,
    var created_at :String=""
)
