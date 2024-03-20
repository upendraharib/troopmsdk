package com.tvisha.trooponprime.lib.clientModels

data class GroupCreateModel(
    var group_name:String="",
    var group_avatar:String="",
    var group_description:String="",
    var groupMembers:List<CreateGroupMembers>,
    var conversation_reference_id:String = "",
)
