package com.tvisha.trooponprime.lib.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story")
internal data class Story(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id", defaultValue = "0")
    var id : Long = 0,
    @ColumnInfo(name = "user_id", defaultValue = "0")
    var user_id:Long = 0,
    @ColumnInfo(name = "type", defaultValue = "0")
    var type:Int =0,
    @ColumnInfo(name = "data")
    var data:String?=null,
    @ColumnInfo(name = "shared_to")
    var shared_to:String?= null,
    @ColumnInfo(name = "status")
    var status:Int= 0,
    @ColumnInfo(name = "created_at",defaultValue = "CURRENT_TIMESTAMP")
    var created_at:String?=null,
    @ColumnInfo(name = "updated_at" , defaultValue = "CURRENT_TIMESTAMP")
    var updated_at: String? = null,
)
