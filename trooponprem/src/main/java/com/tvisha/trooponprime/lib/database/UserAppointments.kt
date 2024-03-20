package com.tvisha.trooponprime.lib.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_appointment")
internal data class UserAppointments(
    @PrimaryKey(autoGenerate = true)
    var ID :Long = 0,
    @ColumnInfo(name = "tm_appointment_id", defaultValue = "0")
    var tm_appointment_id:Long = 0,
    @ColumnInfo(name = "appointment_id" , defaultValue = "0")
    var appointment_id :Long = 0,
    @ColumnInfo(name = "appointment_name")
    var appointment_name:String? = null,
    @ColumnInfo(name = "user_id", defaultValue = "0")
    var user_id:Long = 0,
    @ColumnInfo(name="data")
    var data:String? = null,
    @ColumnInfo(name = "created_at",defaultValue = "CURRENT_TIMESTAMP")
    var created_at:String?=null,
    @ColumnInfo(name = "updated_at" , defaultValue = "CURRENT_TIMESTAMP")
    var updated_at: String? = null
)
