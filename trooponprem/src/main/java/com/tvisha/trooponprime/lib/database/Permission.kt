package com.tvisha.trooponprime.lib.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "permission", indices = [Index(value = ["workspace_id","user_id"])])
internal data class Permission(@PrimaryKey(autoGenerate = true)
                      var ID:Long=0L,
                      @ColumnInfo(name = "workspace_id")
                      var workspace_id:String? =null,
                      @ColumnInfo(name = "user_id")
                      var user_id:String? =null,
                      @ColumnInfo(name = "type",defaultValue = "0")
                      var type : Int = 0,
                      @ColumnInfo(name = "permission")
                      var permission: String? = null,
                      @ColumnInfo(name = "created_at",defaultValue = "CURRENT_TIMESTAMP")
                      var created_at:String?=null,
                      @ColumnInfo(name = "updated_at" , defaultValue = "CURRENT_TIMESTAMP")
                      var updated_at: String? = null,
)
