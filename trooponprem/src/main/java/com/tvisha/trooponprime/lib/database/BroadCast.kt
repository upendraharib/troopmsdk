package com.tvisha.trooponprime.lib.database

import androidx.room.*

@Entity(tableName = "broadcast",indices = [Index(value = ["message_id"])])
internal data class BroadCast(
    @PrimaryKey(autoGenerate = true)
    var ID:Long=0L,
    @ColumnInfo(name = "message_id", defaultValue = "0")
    var message_id:Long = 0,
    @ColumnInfo(name = "title")
    var title:String? = null,
    @ColumnInfo(name = "message")
    var message:String? = null,
    @ColumnInfo(name = "attachment")
    var attachment:String? = null,
    @ColumnInfo(name = "created_at",defaultValue = "TIMESTAMP")
    var created_at:String?=null,
    @ColumnInfo(name = "updated_at" , defaultValue = "TIMESTAMP")
    var updated_at: String?=null
)
