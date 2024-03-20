package com.tvisha.trooponprime.lib.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "group_message_status", indices = [Index(value = ["workspace_id","group_id","user_id","message_id"])])
internal data class GroupMemberStatus(
    @PrimaryKey(autoGenerate = true)
    var ID:Long=0L,
    @ColumnInfo(name = "workspace_id")
    var workspace_id:String="",
    @ColumnInfo(name = "group_id", defaultValue = "0")
    var group_id:Int =0,
    @ColumnInfo(name = "user_id", defaultValue = "0")
    var user_id:Int =0,
    @ColumnInfo(name = "message_id", defaultValue = "0")
    var user_status:Int =0,
    @ColumnInfo(name = "is_read", defaultValue = "0")
    var user_role:Int =0,
    @ColumnInfo(name = "is_delivered", defaultValue = "0")
    var is_delivered:Int=0,
    @ColumnInfo(name = "read_at",defaultValue = "CURRENT_TIMESTAMP")
    var read_at:String?=null,
    @ColumnInfo(name = "delivered_at",defaultValue = "CURRENT_TIMESTAMP")
    var delivered_at:String?=null,
    @ColumnInfo(name = "created_at",defaultValue = "CURRENT_TIMESTAMP")
    var created_at:String?=null,
    @ColumnInfo(name = "updated_at" , defaultValue = "CURRENT_TIMESTAMP")
    var updated_at: String? = null
)