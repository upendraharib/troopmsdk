package com.tvisha.trooponprime.lib.clientModels

data class MessageData(
    var receiver_id:String ="", //required for all types of messages
    var is_group:Int = -1, //required for all types of messages
    var is_room : Int = 0, //required for all types of messages
    var message:String = "", //required for all types of messages
    var conversation_reference_id:String= "",
    var receiver_uid:String="",
    var sender_uid:String="",
    var attachment:String="",// required for attachment/audio message
    var local_attachment_path:String="",// required for attachment/audio message
    var caption:String="",// required for attachment message
    var preview_link :String = "",// required for attachment/audio message
    var contact_name:String="",// required for contact message
    var contact_number:String="",// required for contact message
    var location_latitude:String="",// required for location message
    var location_longitude:String="",// required for location message
    var location_address :String="",// required for location message
    var location_name:String= "",// required for location message
    var message_id : Long = 0, // required for reply message

)
