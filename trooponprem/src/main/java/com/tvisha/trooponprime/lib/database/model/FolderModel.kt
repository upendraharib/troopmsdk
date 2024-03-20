package com.tvisha.troopmessenger.FileDeck.Model

import java.io.Serializable

data class FolderModel(
    var ancestor : Int = 0,
    var descendant : Int = 0,
    var number_of_descendants:Int =0,
    var folder_id:Int=0,
    var folder_type:Int=0,
    var is_downloaded:Int=0,
    var workspace_id:String?=null,
    var folder_name:String?=null,
    var created_at:String?=null,
    @JvmField
    var tabId:Int=0

):Serializable
