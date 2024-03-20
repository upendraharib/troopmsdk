package com.tvisha.trooponprime.lib.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "chat", indices = [Index(value = ["message_id", "sender_id", "receiver_id", "workspace_id", "is_group"])])
internal data class Messenger(
    @PrimaryKey(autoGenerate = true)
    var ID: Long = 0L,
    @ColumnInfo(name = "message_id", defaultValue = "0")
    var message_id: Long = 0,
    @ColumnInfo(name = "sender_id", defaultValue = "0")
    var sender_id: Long = 0,
    @ColumnInfo(name = "receiver_id", defaultValue = "0")
    var receiver_id: Long = 0,
    @ColumnInfo(name = "message")
    var message: String? = null,
    @ColumnInfo(name = "attachment")
    var attachment: String? = null,
    @ColumnInfo(name = "caption")
    var caption: String? = null,
    @ColumnInfo(name = "preview_link")
    var preview_link: String? = null,
    @ColumnInfo(name = "is_group", defaultValue = "0")
    var is_group: Int = 0,
    @ColumnInfo(name = "is_reply", defaultValue = "0")
    var is_reply: Int = 0,
    @ColumnInfo(name = "is_forward", defaultValue = "0")
    var is_forward: Int = 0,
    @ColumnInfo(name = "is_forkout", defaultValue = "0")
    var is_forkout: Int = 0,
    @ColumnInfo(name = "original_message_id", defaultValue = "0")
    var original_message_id: Long = 0,
    @ColumnInfo(name = "original_message")
    var original_message: String? = null,
    @ColumnInfo(name = "is_sync", defaultValue = "0")
    var is_sync: Int = 0,
    @ColumnInfo(name = "is_read", defaultValue = "0")
    var is_read: Int = 0,
    @ColumnInfo(name = "is_delivered", defaultValue = "0")
    var is_delivered: Int = 0,
    @ColumnInfo(name = "message_type", defaultValue = "0")
    var message_type: Int = 0,
    @ColumnInfo(name = "status", defaultValue = "1")
    var status: Int = 1,
    @ColumnInfo(name = "message_memory")
    var message_memory: String? =  null,
    @ColumnInfo(name = "is_edited", defaultValue = "0")
    var is_edited: Int = 0,
    @ColumnInfo(name = "edited_at", defaultValue = "CURRENT_TIMESTAMP")
    var edited_at: String? = null,
    @ColumnInfo(name = "attachment_downloaded", defaultValue = "0")
    var attachment_downloaded: Int = 0,
    @ColumnInfo(name = "attachment_path")
    var attachment_path: String? = null,
    @ColumnInfo(name = "local_attachment_path")
    var local_attachment_path: String? = null,
    @ColumnInfo(name = "conversation_reference_id")
    var conversation_reference_id:String? = null,


    @ColumnInfo(name = "workspace_id")
    var workspace_id: String? = null,
    @ColumnInfo(name = "message_hash")
    var message_hash: String? = null,
    @ColumnInfo(name = "read_receipt_users")
    var read_receipt_users: String? = null,
    @ColumnInfo(name = "is_read_receipt", defaultValue = "0")
    var is_read_receipt: Int = 0,
    @ColumnInfo(name = "read_receipt_status", defaultValue = "0")
    var read_receipt_status: Int = 0,
    @ColumnInfo(name = "is_flag", defaultValue = "0")
    var is_flag: Int = 0,
    @ColumnInfo(name = "is_respond_later", defaultValue = "0")
    var is_respond_later: Int = 0,
    @ColumnInfo(name = "isMine", defaultValue = "0")
    var isMine: Int = 0,
    @ColumnInfo(name = "isSelected",defaultValue = "0")
    var isSelected: Int = 0,
    @ColumnInfo(name = "isHeader",defaultValue = "0")
    var isHeader: Int = 0,
    @ColumnInfo(name = "isBackground",defaultValue = "0")
    var isBackground: Int = 0,
    @ColumnInfo(name = "progrssvalue")
    var progrssvalue: String? = null,
    @ColumnInfo(name = "sender_name")
    var sender_name: String? = null,
    @ColumnInfo(name = "chetHeaderTime")
    var chetHeaderTime: String? = null,
    @ColumnInfo(name = "reply_message_id",defaultValue = "0")
    var reply_message_id: Int = 0,
    @ColumnInfo(name = "reply_message_status", defaultValue = "0")
    var reply_message_status: Int = 0,
    @ColumnInfo(name = "reply_sender_id")
    var reply_sender_id: String? = null,
    @ColumnInfo(name = "reply_receiver_id")
    var reply_receiver_id: String? = null,
    @ColumnInfo(name = "reply_message_type", defaultValue = "0")
    var reply_message_type: Int = 0,
    @ColumnInfo(name = "reply_message")
    var reply_message: String? = null,
    @ColumnInfo(name = "reply_attachment")
    var reply_attachment: String? = null,
    @ColumnInfo(name = "reply_caption")
    var reply_caption: String? = null,
    @ColumnInfo(name = "reply_created_at")
    var reply_created_at: String? = null,
    @ColumnInfo(name = "reply_sender_name")
    var reply_sender_name: String? =  null,
    @ColumnInfo(name = "member_name")
    var member_name: String? =  null,
    @ColumnInfo(name = "user_avatar")
    var user_avatar: String? =  null,
    @ColumnInfo(name = "user_label")
    var user_label: String? =  null,
    @ColumnInfo(name = "chatTimeHeaderLabel")
    var chatTimeHeaderLabel: String? =  null,
    @ColumnInfo(name = "isSameMember", defaultValue = "0")
    var isSameMember: Int = 0,
    @ColumnInfo(name = "uploadProgress")
    var uploadProgress: String? =  null,
    @ColumnInfo(name = "downloadPrgress")
    var downloadPrgress: String? =  null,
    @ColumnInfo(name = "audio_state", defaultValue = "0")
    var audio_state: Int = 0,
    @ColumnInfo(name = "audioProgress", defaultValue = "0")
    var audioProgress: Int = 0,
    @ColumnInfo(name = "mediaProgrssTime")
    var mediaProgrssTime: String? = null,
    @ColumnInfo(name = "audioTotalProgress", defaultValue = "0")
    var audioTotalProgress: Int = 0,
    @ColumnInfo(name = "mediaTime")
    var mediaTime: String? = null,
    @ColumnInfo(name = "is_trumpet", defaultValue = "0")
    var is_trumpet :Int= 0,
    @ColumnInfo(name = "is_like", defaultValue = "0")
    var is_like :Int= 0,
    @ColumnInfo(name = "pin", defaultValue = "0")
    var pin :Int = 0,
    @ColumnInfo(name = "pinned_by", defaultValue = "0")
    var pinned_by : Long = 0,
    @ColumnInfo(name = "pinned_at")
    var pinned_at: String? = null,
    @ColumnInfo(name = "sender_uid")
    var sender_uid:String ="",
    @ColumnInfo(name = "receiver_uid")
    var receiver_uid:String ="",
    @ColumnInfo(name = "is_recommended", defaultValue = "0")
    var is_recommended: Int = 0,
    @ColumnInfo(name = "is_room", defaultValue = "0")
    var is_room: Int = 0,


    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    var created_at: String? = null,
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    var updated_at: String? = null,
) : Serializable {

    /*data class Separator(private val letter: Char) : ClapMessenger(letter.toUpperCase().toString())*/

    /*internal constructor(album: List<Messenger>?) : this() {
        if (album != null) {
            listItems = album
        }
    }*/

}
