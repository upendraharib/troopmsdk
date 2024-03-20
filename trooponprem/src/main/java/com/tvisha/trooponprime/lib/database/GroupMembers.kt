package com.tvisha.trooponprime.lib.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "group_member", indices = [Index(value = ["group_id","user_id"])])
internal data class GroupMembers(
    @PrimaryKey(autoGenerate = true)
    var ID : Long = 0,
    @ColumnInfo(name = "group_id", defaultValue = "0")
    var group_id:Long = 0,
    @ColumnInfo(name = "user_id", defaultValue = "0")
    var user_id:Long = 0,
    @ColumnInfo(name = "user_status", defaultValue = "0")
    var user_status:Int = 0,
    @ColumnInfo(name = "user_role", defaultValue = "0")
    var user_role:Int = 0,

    /*@ColumnInfo(name = "workspace_id")
    var workspace_id:String? = null,
    @ColumnInfo(name = "member_color")
    var member_color:String? = null,
    @ColumnInfo(name = "topic_user_role")
    var topic_user_role:String? = null,*/

    @ColumnInfo(name = "created_at",defaultValue = "CURRENT_TIMESTAMP")
    var created_at:String?=null,
    @ColumnInfo(name = "updated_at" , defaultValue = "CURRENT_TIMESTAMP")
    var updated_at: String? = null,

)

