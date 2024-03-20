package com.tvisha.trooponprime.lib.database.model

data class DownloadedAttachmentModel(
    var message_id:Long=0,
    var is_downloaded:Int=0,
    var attachment:String="",
    var ID:Long=0
)
