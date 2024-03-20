package com.tvisha.trooponprime.lib.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "contact",indices = [Index(value = ["uid"])])
internal data class Contact (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id", defaultValue = "0")
    var id:Long=0L,
    @ColumnInfo(name = "name")
    var name:String?=null,
    @ColumnInfo(name="uid")
    var uid:String?=null,
    @ColumnInfo(name = "created_at",defaultValue = "TIMESTAMP")
    var created_at:String?=null,
    @ColumnInfo(name = "updated_at" , defaultValue = "TIMESTAMP")
    var updated_at: String?=null
)