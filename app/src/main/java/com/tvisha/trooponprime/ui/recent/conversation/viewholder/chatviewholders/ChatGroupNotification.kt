package com.tvisha.trooponprime.ui.recent.conversation.viewholder.chatviewholders

import androidx.recyclerview.widget.RecyclerView
import com.tvisha.trooponprime.MyApplication
import com.tvisha.trooponprime.databinding.ConGroupNotificationBinding
import com.tvisha.trooponprime.lib.clientModels.ConversationModel
import com.tvisha.trooponprime.lib.utils.Helper

class ChatGroupNotification(var binding:ConGroupNotificationBinding):RecyclerView.ViewHolder(binding.root) {
    fun bindData(messageData: ConversationModel){
        binding.chatGroupNotificationLabel.text = Helper.isGroupUpdatedMessage(messageData.message_type,if (messageData.sender_id.toString()==MyApplication.LOGINUSERTMID)"You" else messageData.member_name!!,messageData.message,messageData.is_sender==1,MyApplication.LOGINUSERTMID,messageData.sender_id.toString())
    }
}