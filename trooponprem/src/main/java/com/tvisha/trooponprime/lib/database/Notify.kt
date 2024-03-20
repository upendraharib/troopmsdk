package com.tvisha.trooponprime.lib.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "notify", indices = [Index(value = ["notify_id","workspace_id"])])
internal data class Notify(@PrimaryKey(autoGenerate = true)
                  var ID:Long=0L,
                  @ColumnInfo(name="workspace_id")
                  var workspace_id:String?=null,
                  @ColumnInfo(name = "notify_id", defaultValue = "0")
                  var notify_id:Long = 0,
                  @ColumnInfo(name = "sender_id", defaultValue = "0")
                  var sender_id:Long = 0,
                  @ColumnInfo(name = "receiver_id")
                  var receiver_id:String? = null,
                  @ColumnInfo(name = "message")
                  var message :String?= null,
                  @ColumnInfo(name = "attachment")
                  var attachment:String? = null,
                  @ColumnInfo(name = "sender_name")
                  var sender_name:String? = null,
                  @ColumnInfo(name = "sender_avatar")
                  var sender_avatar:String? = null,
                  @ColumnInfo(name = "priority", defaultValue = "0")
                  var priority:Int = 0,
                  @ColumnInfo(name = "is_read", defaultValue = "0")
                  var is_read : Int = 0,
                  @ColumnInfo(name = "status", defaultValue = "0")
                  var status:Int = 0,
                  @ColumnInfo(name = "created_at",defaultValue = "CURRENT_TIMESTAMP")
                  var created_at:String?=null,
                  @ColumnInfo(name = "updated_at" , defaultValue = "CURRENT_TIMESTAMP")
                  var updated_at: String? = null,
                  @ColumnInfo(name = "isMine", defaultValue = "0")
                  var isMine: Int = 0,
                  @ColumnInfo(name = "isSelected",defaultValue = "0")
                  var isSelected: Int = 0,
                  @ColumnInfo(name = "isHeader",defaultValue = "0")
                  var isHeader: Int = 0,
                  @ColumnInfo(name = "isBackground",defaultValue = "0")
                  var isBackground: Int = 0,
                  @ColumnInfo(name = "chatTimeHeaderLable")
                  var chatTimeHeaderLable: String? =  null,
                  @ColumnInfo(name = "isSameMember", defaultValue = "0")
                  var isSameMember: Int = 0,
                  @ColumnInfo(name = "progrssvalue")
                  var progrssvalue: String? = null

)
