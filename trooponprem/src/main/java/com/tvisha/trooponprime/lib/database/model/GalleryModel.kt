package com.tvisha.trooponprime.lib.database.model

import android.graphics.Bitmap
import java.io.Serializable

data class GalleryModel(
    var ID: Long =0,
    var message_id:Long = 0,
    var caption:String?  =null,
    var message:String?  =null,
    var is_group : Int = 0,
    var receiver_id:Long = 0,
    var sender_id:Long = 0,
    var created_at:String? =null,
    var attachment:String? =null,
    var is_downloaded : Int = 0,
    var progress:String? = null,
    var thumb:Any? = null,
    var status:Int = 0,
    var is_sync : Int = 0,
    var message_type : Int = 0,
    var is_respond_later : Int = 0,
    var is_flag : Int = 0,
    var pin : Int = 0,
    var is_trumpet : Int = 0,
    var is_selected : Int= 0,
    var is_edited : Int= 0,
    var edited_at : String?=null,
    var editFileName:String?=null,
    var fileExtension:String?=null,
    var tags:String?=null,
    var file_name:String?=null,
    var comment:String? =null,
    var message_memory:Double=0.0
)
    :Serializable
