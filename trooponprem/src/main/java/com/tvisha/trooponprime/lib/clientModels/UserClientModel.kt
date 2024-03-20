package com.tvisha.trooponprime.lib.clientModels

data class UserClientModel(
    var entity_id:Long = 0,
    var uid:String? = null,
    var entity:Int = 0,
    var entity_name:String? = null,
    var entity_avatar:String? = null,
    var mobile:String ? = null,
    var country_code:String? = null,
    var about:String ? = null,
    var isAlreadyUser:Int = 0,
    var isSelected : Int = 0,

)
