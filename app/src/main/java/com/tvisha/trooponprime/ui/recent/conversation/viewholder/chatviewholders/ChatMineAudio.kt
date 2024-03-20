package com.tvisha.trooponprime.ui.recent.conversation.viewholder.chatviewholders

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.ConAudioMessageMineItemBinding
import com.tvisha.trooponprime.lib.clientModels.ConversationModel
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper

class ChatMineAudio(var binding: ConAudioMessageMineItemBinding,var context: Context):RecyclerView.ViewHolder(binding.root) {
    fun bindData(message: ConversationModel){
        binding.clReply.clReplyHolder.visibility = if (message.is_reply==1) View.VISIBLE else View.GONE
        binding.txtMsg.text ="" //message.message!!
        binding.messageTimeView.timeStamp.text = Helper.getConversationMessagesTime(Helper.utcToLocalTime(message.created_at!!))
        binding.messageTimeView.msgSentStatus.visibility = View.VISIBLE
        binding.messageTimeView.msgSentStatus.setImageResource(if (message.is_read== ConstantValues.ACTIVE) R.drawable.ic_msg_read else if (message.is_delivered== ConstantValues.ACTIVE) R.drawable.ic_msg_delivered else if (message.is_sync== ConstantValues.ACTIVE) R.drawable.ic_msg_sent else R.drawable.ic_msg_unsent)
        if (message.status== ConstantValues.MessageStatus.MESSAGE_RECALLED){
            binding.messageTimeView.msgSentStatus.visibility = View.GONE
            binding.txtMsg.text = if (message.is_sender==1) "You recalled this message" else if (message.is_group== ConstantValues.Entity.IS_USER) "This message was recalled" else "${message.member_name} was recalled"
        }else{
            setAudioMessage(message)
        }
        if (message.is_reply==1){
            setReplyMessage(message)
        }

        setSelection(message.isSelected)
    }
    private fun setSelection(status:Int){
        if (status==1) {
            binding.root.setBackgroundColor(ContextCompat.getColor(context as Activity, android.R.color.darker_gray))
        } else {
            binding.root.setBackgroundColor(ContextCompat.getColor(context as Activity, android.R.color.transparent))
        }
    }
    private fun setAudioMessage(message: ConversationModel) {
        binding.clAudio.audioProgress.progress =  0
        binding.clAudio.tvAudioTime.text = "01:40"
        if (message.status==ConstantValues.ACTIVE && (message.is_forkout==1 || message.is_forward==1 || message.is_edited==1)){
            binding.moreFeatures.clMoreFeatures.visibility = View.VISIBLE
            binding.moreFeatures.tvEdited.visibility = if (message.is_edited==1) View.VISIBLE else View.GONE
            if (message.is_edited==1 && !message.edited_at.isNullOrEmpty()){
                binding.moreFeatures.tvEdited.text = Helper.getConversationMessagesTime(Helper.utcToLocalTime(message.edited_at))
            }
            binding.moreFeatures.ivFlag.visibility = View.GONE
            binding.moreFeatures.ivForkOut.visibility = if (message.is_forkout==1) View.VISIBLE else View.GONE
            binding.moreFeatures.ivForward.visibility = if (message.is_forward==1) View.VISIBLE else View.GONE
        }else{
            binding.moreFeatures.clMoreFeatures.visibility = View.GONE
        }
    }

    private fun setReplyMessage(message: ConversationModel){
        var jsonObject = Helper.stringToJsonObject(message.original_message)!!
        jsonObject.put("message",Helper.stringToJsonObject(jsonObject.optString("message")))
        binding.clReply.ivReplyAttachment.visibility = View.GONE
        if (jsonObject!=null) {
            binding.clReply.tvReplyUserName.text = if (jsonObject.has("member_name")) jsonObject.optString("member_name") else jsonObject.optString("sender_name")
            var replyMessage = jsonObject.optString("message")
            var replyAttachment :Int = 0
            when(jsonObject.optInt("message_type")){
                ConstantValues.MessageTypes.LOCATION_MESSAGE->{
                    replyMessage = jsonObject.optJSONObject("message").optString("location_name")
                    replyAttachment = R.drawable.location_shape
                }

                ConstantValues.MessageTypes.CONTACT_MESSAGE->{
                    replyMessage = jsonObject.optJSONObject("message").optString("contact_name")+" \n "+jsonObject.optJSONObject("message").optString("contact_address")
                    replyAttachment = R.drawable.default_user
                }

                ConstantValues.MessageTypes.AUDIO_MESSAGE->{
                    replyMessage = "Audio Message"
                    replyAttachment = android.R.drawable.presence_audio_online
                }

                ConstantValues.MessageTypes.ATTACHMENT->{
                    replyMessage = "Attachment"
                    replyAttachment = R.drawable.attachment_pin_icon
                }
            }
            binding.clReply.repliedMessage.text = replyMessage
            if (replyAttachment>0){
                binding.clReply.ivReplyAttachment.visibility = View.VISIBLE
                binding.clReply.ivReplyAttachment.setImageResource(replyAttachment)
            }
        }else{
            binding.clReply.clReplyHolder.visibility = View.GONE
        }
    }
}