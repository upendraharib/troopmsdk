package com.tvisha.trooponprime.lib.database.model

import org.junit.Ignore
import java.io.Serializable

data class ContactModel(
    @set:Ignore
    var entity_id:Long = 0,
    @set:Ignore
    var entity_type:Int = 1,
    @set:Ignore
    var name:String? = null,
    @set:Ignore
    var email:String?=null,
    @set:Ignore
    var mobile:String?=null,
    @set:Ignore
    var is_favourite:Int = 0,
    @set:Ignore
    var is_muted:Int = 0,
    @set:Ignore
    var user_avatar:String? =null,
    var orange:Int = 0,
    @set:Ignore
    var workspace_id:String? = null,
    @set:Ignore
    var isSelected:Int = 0,
    @set:Ignore
    var isAdminClicked:Int = 0,
    @set:Ignore
    var isModeratorClicked:Int = 0,
    @set:Ignore
    var user_role:Int = 0,
    @set:Ignore
    var clickable:Int = 0
):Serializable
