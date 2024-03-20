package com.tvisha.trooponprime.lib.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messenger_pinned_user")
internal data class MessengerPinnedUser(
    @PrimaryKey(autoGenerate = true)
    var ID:Long=0L,
    @ColumnInfo(name = "entity_id", defaultValue = "0")
    var entity_id:Int =0,
    @ColumnInfo(name = "entity_type",defaultValue = "0")
    var entity_type:Int=0,
    @ColumnInfo(name = "created_at",defaultValue = "CURRENT_TIMESTAMP")
    var created_at:String?=null,
    @ColumnInfo(name = "updated_at" , defaultValue = "CURRENT_TIMESTAMP")
    var updated_at: String? = null
)
