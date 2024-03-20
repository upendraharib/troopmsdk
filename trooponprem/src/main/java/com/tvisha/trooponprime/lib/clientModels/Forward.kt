package com.tvisha.trooponprime.lib.clientModels

data class Forward(
    var receiver_id:String?=null,
    var entity:Int = 0, //required if it is group 2 then 1
    var entity_uid:String?=null,
)
