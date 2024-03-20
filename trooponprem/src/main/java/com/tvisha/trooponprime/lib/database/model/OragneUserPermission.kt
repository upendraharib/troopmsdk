package com.tvisha.trooponprime.lib.database.model

data class OragneUserPermission(
    var permission : String? = null,
    var default_permission : String? =null,
    var user_id:Int = 0,
    var type:Int = 0
)
