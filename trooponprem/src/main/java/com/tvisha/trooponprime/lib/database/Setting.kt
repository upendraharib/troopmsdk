package com.tvisha.trooponprime.lib.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "troop_profile_settings", indices = [Index(value =["settings_type"])])
internal data class Setting(@PrimaryKey(autoGenerate = true)
                   var ID:Long=0L,
                   @ColumnInfo(name = "workspace_id")
                   var workspace_id:String?=null,
                   @ColumnInfo(name = "settings_type", defaultValue = "0")
                   var settings_type: Int = 0,
                   @ColumnInfo(name = "type")
                   var type:String="",
                   @ColumnInfo(name = "value")
                   var value:String = "",
                   @ColumnInfo(name = "status", defaultValue = "0")
                   var status:Int =0,
                   @ColumnInfo(name = "attached_file")
                   var attached_file :String = "",
                   @ColumnInfo(name = "created_at",defaultValue = "CURRENT_TIMESTAMP")
                   var created_at:String="",
                   @ColumnInfo(name = "updated_at" , defaultValue = "CURRENT_TIMESTAMP")
                   var updated_at: String = "",)
