package com.tvisha.trooponprime.lib.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan")
internal data class Plan(@PrimaryKey(autoGenerate = true)
                var ID:Long=0L,
                @ColumnInfo(name = "workspace_id")
                var workspace_id:String? = null,
                @ColumnInfo(name = "plan_type",defaultValue = "0")
                var plan_type:Int = 0,
                @ColumnInfo(name = "plan")
                var plan:String? =null,
                @ColumnInfo(name = "valid_from",defaultValue = "CURRENT_TIMESTAMP")
                var valid_from:String?=null,
                @ColumnInfo(name = "valid_to" , defaultValue = "CURRENT_TIMESTAMP")
                var valid_to: String? = null,
                @ColumnInfo(name = "created_at",defaultValue = "CURRENT_TIMESTAMP")
                var created_at:String?=null,
                @ColumnInfo(name = "updated_at" , defaultValue = "CURRENT_TIMESTAMP")
                var updated_at: String? = null,
)
