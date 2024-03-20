package com.tvisha.trooponprime.lib.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "user",indices = [Index(value = ["user_id"])])
internal data class User(@PrimaryKey(autoGenerate = false)
                @ColumnInfo(name = "user_id", defaultValue = "0")
                var user_id: Long=0,
                @ColumnInfo(name = "uid")
                var uid: String="",
                @ColumnInfo(name = "name")
                var name: String?=null,
                @ColumnInfo(name="is_archived", defaultValue = "0")
                var is_archived:Int = 0,
                @ColumnInfo(name="is_blocked", defaultValue = "0")
                var is_blocked:Int = 0,
                @ColumnInfo(name="blocked_me", defaultValue = "0")
                var blocked_me:Int = 0,
                @ColumnInfo(name="mobile")
                var mobile : String?=null,
                @ColumnInfo(name="country_code")
                var country_code : String?=null,
                @ColumnInfo(name="profile_pic")
                var profile_pic:String?=null,
                @ColumnInfo(name = "about")
                var about:String?=null,
                @ColumnInfo(name = "last_message")
                var last_message:String?=null,
                @ColumnInfo(name = "unread_messages_count", defaultValue = "0")
                var unread_count : Int =0,
                @ColumnInfo(name = "last_message_local_id", defaultValue = "0")
                var last_message_local_id: Long = 0,
                @ColumnInfo(name = "last_message_id", defaultValue = "0")
                var last_message_message_id: Long = 0,
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
                @ColumnInfo(name = "user_status", defaultValue = "0")
                var user_status:Int = 0,

                /*@ColumnInfo(name = "workspace_id")
                var workspace_id:String?=null,
                @ColumnInfo(name = "email")
                var email: String?=null,
                @ColumnInfo(name="designation_id")
                var designation_id : String?=null,
                @ColumnInfo(name = "designation_name")
                var designation_name:String?=null,
                @ColumnInfo(name="company")
                var company:String?=null,
                @ColumnInfo(name="company_city")
                var company_city:String?=null,
                @ColumnInfo(name = "user_pin", defaultValue = "0")
                var user_pin :Int=0,
                @ColumnInfo(name = "is_self_profile", defaultValue = "0")
                var is_self_profile:Int = 0,

                @ColumnInfo(name = "user_role", defaultValue = "0")
                var user_role: Int = 0,
                @ColumnInfo(name = "is_favourite", defaultValue = "0")
                var is_favourite : Int = 0,
                @ColumnInfo(name="is_muted", defaultValue = "0")
                var is_muted : Int = 0,
                @ColumnInfo(name = "is_orange_member", defaultValue = "0")
                var is_orange_member : Int =0,
                @ColumnInfo(name="user_label")
                var user_label : String?=null,

                @ColumnInfo(name = "read_receipt_count", defaultValue = "0")
                var read_receipt_count : Int = 0,
                @ColumnInfo(name = "respond_later_count", defaultValue = "0")
                var respond_later_count: Int = 0,

                @ColumnInfo(name = "counts")
                var counts:String?=null,
                @ColumnInfo(name = "public_key")
                var pulic_key:String?=null,
                @ColumnInfo(name = "data")
                var data:String?=null,
                @ColumnInfo(name = "username")
                var username:String?=null,
                @ColumnInfo(name = "is_global_user", defaultValue = "0")
                var is_global_user: Int = 0,
                @ColumnInfo(name = "unit_id", defaultValue = "0")
                var unit_id: Int = 0,*/

                @ColumnInfo(name = "created_at",defaultValue = "TIMESTAMP")
                var created_at:String?=null,
                @ColumnInfo(name = "updated_at" , defaultValue = "TIMESTAMP")
                var updated_at: String?=null
):Serializable
