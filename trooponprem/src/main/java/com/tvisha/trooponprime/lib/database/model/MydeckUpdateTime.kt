package com.tvisha.trooponprime.lib.database.model

data class MydeckUpdateTime (
    var mydeck_folder_updated_at:String ="",
    var mydeck_folder_tags_updated_at:String ="",
    var mydeck_folder_comments_updated_at:String ="",
    var mydeck_files_updated_at:String ="",
    var mydeck_file_tags_updated_at:String ="",
    var mydeck_file_comments_updated_at:String =""
)