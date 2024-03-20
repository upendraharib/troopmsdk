package com.tvisha.trooponprime.ui.recent.adapter

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.tvisha.trooponprime.MyApplication
import com.tvisha.trooponprime.ui.recent.conversation.ChatActivity
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.RecentListItemAdapterBinding
import com.tvisha.trooponprime.lib.clientModels.RecentMessageList
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper

class RecentListAdapter(var context: Context,var handler:Handler):RecyclerView.Adapter<RecentListAdapter.ViewHolder>() {
    var recentList :ArrayList<RecentMessageList> = ArrayList()
    var selectionMode : Boolean = false
    fun setTheRecentList(list: ArrayList<RecentMessageList>){
        recentList = list
        notifyDataSetChanged()
    }
    inner class ViewHolder(var binding: RecentListItemAdapterBinding):RecyclerView.ViewHolder(binding.root){

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.recent_list_item,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvEntityName.text = recentList[position].entity_name
        holder.binding.tvMessageTime.text = Helper.getRecentListMessagesDateTime(Helper.utcToLocalTime(recentList[position].message_time))
        holder.binding.tvUnreadCount.text = recentList[position].unread_messages_count.toString()
        holder.binding.tvUnreadCount.visibility = if (recentList[position].unread_messages_count>0) View.VISIBLE else View.GONE
        Glide.with(context).load(recentList[position].entity_avatar)
            .circleCrop()
            .placeholder(ContextCompat.getDrawable(context,if (recentList[position].entity==ConstantValues.Entity.GROUP) R.drawable.default_group_pic else R.drawable.default_user)).error(
                ContextCompat.getDrawable(context,if (recentList[position].entity==ConstantValues.Entity.GROUP) R.drawable.default_group_pic else R.drawable.default_user)).into(holder.binding.ivEntityProfile)
        holder.binding.ivGroup.visibility = if (recentList[position].entity==ConstantValues.Entity.GROUP) View.VISIBLE else View.GONE

        holder.binding.tvGroupMemberName.visibility = if (recentList[position].entity==ConstantValues.Entity.GROUP && !Helper.isSeverMessage(recentList[position].message_type)) View.VISIBLE else View.GONE
        holder.binding.tvGroupMemberName.text = recentList[position].sender_name+" : "
        holder.binding.ivAttachment.visibility = if (recentList[position].message_type==ConstantValues.MessageTypes.ATTACHMENT && recentList[position].message_status!=ConstantValues.MessageStatus.RECALLED ) View.VISIBLE else View.GONE
        holder.binding.root.setOnClickListener {
            if (selectionMode){
                recentList[position].isSelected = if (recentList[position].isSelected==1) 0 else 1
                notifyDataSetChanged()
                selectionMode = true
                handler.obtainMessage(1).sendToTarget()
            }else {
                var intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("entity_name", recentList[position].entity_name)
                intent.putExtra("entity_id", recentList[position].entity_id.toString())
                intent.putExtra("entity_type", recentList[position].entity)
                intent.putExtra("entity_uid", recentList[position].entity_uid)
                context.startActivity(intent)
            }
        }
        var icon = R.drawable.ic_msg_unsent
        var message:String = recentList[position].message!!
        if (recentList[position].message_status==ConstantValues.MessageStatus.RECALLED){
            icon = R.drawable.ic_msg_recall
            message = if (recentList[position].is_sender==1) "You recalled this message" else if (recentList[position].entity==ConstantValues.Entity.USER) "This message was recalled" else recentList[position].sender_name+" was recalled this message"
        }else {
            when (recentList[position].message_type) {
                ConstantValues.MessageTypes.TEXT_MESSAGE->{
                    message = recentList[position].message!!
                }
                ConstantValues.MessageTypes.ATTACHMENT->{
                    message = "Attachment"
                }
                ConstantValues.MessageTypes.CONTACT_MESSAGE->{
                    message = "Contact"
                }
                ConstantValues.MessageTypes.LOCATION_MESSAGE->{
                    message = "Location"
                }
                ConstantValues.MessageTypes.AUDIO_MESSAGE->{
                    message = "Audio"
                }
                else ->{
                    if (Helper.isSeverMessage(recentList[position].message_type)){
                        message = Helper.isGroupUpdatedMessage(recentList[position].message_type,if (recentList[position].is_sender==1) "You" else recentList[position].sender_name!!,recentList[position].message,recentList[position].is_sender==1,MyApplication.LOGINUSERTMID,recentList[position].sender_id.toString())
                    }
                }
            }
            when(recentList[position].message_status){
                ConstantValues.MessageStatus.READ->{
                    icon = R.drawable.ic_msg_read
                }
                ConstantValues.MessageStatus.DELIVERED->{
                    icon = R.drawable.ic_msg_delivered
                }
                ConstantValues.MessageStatus.SENT->{
                    icon = R.drawable.ic_msg_sent
                }

            }
        }
        holder.binding.tvMessage.text = message
        holder.binding.ivMessageStatus.visibility = if (recentList[position].message_status==ConstantValues.MessageStatus.RECALLED || (recentList[position].is_sender==1 && recentList[position].message_status!=ConstantValues.MessageStatus.DELETE && !Helper.isSeverMessage(recentList[position].message_type))) View.VISIBLE else View.GONE
        holder.binding.ivMessageStatus.setImageResource(icon)
        holder.binding.ivChecked.isChecked = if (recentList[position].isSelected==1) true else false
        holder.binding.ivChecked.visibility = if (recentList[position].isSelected==1) View.VISIBLE else View.GONE
        holder.binding.root.setOnLongClickListener {
        recentList[position].isSelected = if (recentList[position].isSelected==1) 0 else 1
            notifyDataSetChanged()
            selectionMode = true
            handler.obtainMessage(1).sendToTarget()
            false
        }
        holder.binding.tvMessage.setTextColor(ContextCompat.getColor(context,R.color.message_color))
        if (recentList[position].isTyping==1){
            holder.binding.ivAttachment.visibility = View.GONE
            holder.binding.ivMessageStatus.visibility = View.GONE
            holder.binding.tvGroupMemberName.visibility = View.GONE
            holder.binding.tvMessage.text = recentList[position].typingData
            holder.binding.tvMessage.setTextColor(ContextCompat.getColor(context,R.color.teal_200))
        }

        //holder.binding.ivMoreOptions.setImageResource(if (recentList[position].isArchive==1) R.drawable.unarchive_ic_icon else R.drawable.archive_icon)
        /*holder.binding.ivMoreOptions.setOnClickListener {
            handler.obtainMessage(1,recentList[position]).sendToTarget()
        }*/
     }

    override fun getItemCount(): Int {
        return recentList.size
    }

    fun getSelectedList(): ArrayList<RecentMessageList> {
        var list : ArrayList<RecentMessageList> = ArrayList()
        for (i in 0 until recentList.size){
            if (recentList[i].isSelected==1){
                list.add(recentList[i])
            }
        }
        return list
    }

    fun unselectAllChats() {
        if (recentList!=null && recentList.size>0){
            for (i in 0 until  recentList.size){
                if (recentList[i].isSelected==1){
                    recentList[i].isSelected = 0
                }
            }
        }
        selectionMode = false
        notifyDataSetChanged()
    }
}