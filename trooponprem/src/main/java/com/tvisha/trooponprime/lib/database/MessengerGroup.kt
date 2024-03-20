package com.tvisha.trooponprime.lib.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_group", indices = [Index(value = ["group_id"])])
internal data class MessengerGroup(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "group_id", defaultValue = "0")
    var group_id: Long = 0,
    @ColumnInfo(name = "group_name")
    var group_name: String? = null,
    @ColumnInfo(name = "group_avatar")
    var group_avatar: String? = null,
    @ColumnInfo(name = "group_description")
    var group_description: String? = null,
    @ColumnInfo(name = "group_type", defaultValue = "0")
    var group_type: Int = 0,
    @ColumnInfo(name = "created_by", defaultValue = "0")
    var created_by: Long = 0,
    @ColumnInfo(name = "conversation_reference_id")
    var conversation_reference_id: String? = null,
    @ColumnInfo(name = "is_active", defaultValue = "0")
    var is_active: Int = 0,
    @ColumnInfo(name = "last_message_local_id", defaultValue = "0")
    var last_message_local_id: Long = 0,
    @ColumnInfo(name = "last_message_id", defaultValue = "0")
    var last_message_message_id: Long = 0,
    @ColumnInfo(name = "last_message")
    var last_message: String? = null,
    @ColumnInfo(name = "last_message_type", defaultValue = "0")
    var last_message_type:Int= 0,
    @ColumnInfo(name = "last_message_status", defaultValue = "0")
    var last_message_status:Int= 0,
    @ColumnInfo(name = "last_message_by_me", defaultValue = "0")
    var last_message_by_me:Int= 0,
    @ColumnInfo(name = "last_message_sender_id", defaultValue = "0")
    var last_message_sender_id:Long= 0,
    @ColumnInfo(name = "last_message_time",defaultValue = "TIMESTAMP")
    var last_message_time:String?=null,
    @ColumnInfo(name = "last_message_sender_name")
    var last_message_sender_name:String?= null,
    @ColumnInfo(name = "unread_messages_count", defaultValue = "0")
    var unread_count: Int = 0,
    @ColumnInfo(name="is_archived", defaultValue = "0")
    var is_archived:Int = 0,

    /*@ColumnInfo(name = "workspace_id")
    var workspace_id: String? = null,
    @ColumnInfo(name = "is_orange_group", defaultValue = "0")
    var is_orange_group: Int = 0,
    @ColumnInfo(name = "is_favourite", defaultValue = "0")
    var is_favourite: Int = 0,
    @ColumnInfo(name = "is_muted", defaultValue = "0")
    var is_muted: Int = 0,
    @ColumnInfo(name = "read_receipt_count", defaultValue = "0")
    var read_receipt_count: Int = 0,
    @ColumnInfo(name = "respond_later_count", defaultValue = "0")
    var respond_later_count: Int = 0,
    @ColumnInfo(name = "counts", defaultValue = "")
    var counts: String? = null,
    @ColumnInfo(name = "public_key")
    var public_key: String? = null,
    @ColumnInfo(name = "private_key")
    var private_key: String? = null,
    @ColumnInfo(name = "created_by_tm_appointment_id", defaultValue = "0")
    var created_by_tm_appointment_id: Long = 0,
    @ColumnInfo(name = "group_status", defaultValue = "0")
    var group_status: Int = 0,
    @ColumnInfo(name = "topic_data")
    var topic_data: String? = null,*/

    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    var created_at: String? = null,
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    var updated_at: String? = null,

    )
