package com.tvisha.trooponprime.ui.recent.conversation.adapter

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.constants.Constants
import com.tvisha.trooponprime.lib.clientModels.ConversationModel
import com.tvisha.trooponprime.lib.clientModels.MessageData
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper
import com.tvisha.trooponprime.ui.recent.conversation.viewholder.chatviewholders.*
import com.tvisha.trooponprime.ui.recent.preview.AttachmentViewActivity

class ConversationAdapter(var context: Context,var handler:Handler): PagingDataAdapter<ConversationModel, RecyclerView.ViewHolder>(
    diffCallback
) {
    private var messageList:ArrayList<ConversationModel> = ArrayList()
    fun setList(list:ArrayList<ConversationModel>){
        messageList = list
    }
    var selectionMode:Boolean =false
    var isLong_click:Boolean =false
    companion object{
        val diffCallback = object : DiffUtil.ItemCallback<ConversationModel>() {
            override fun areItemsTheSame(oldItem: ConversationModel, newItem: ConversationModel): Boolean {
                return if (oldItem is ConversationModel && newItem is ConversationModel) {
                    oldItem.message_id == newItem.message_id
                } else {
                    oldItem == newItem
                }
            }

            /**
             * Note that in kotlin, == checking on data classes compares all contents, but in Java,
             * typically you'll implement Object#equals, and use it to compare object contents.
             */
            override fun areContentsTheSame(oldItem: ConversationModel, newItem: ConversationModel): Boolean {
                return oldItem == newItem

            }

        }
    }
    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.registerAdapterDataObserver(observer)
    }
    override fun setStateRestorationPolicy(strategy: RecyclerView.Adapter.StateRestorationPolicy) {
        super.setStateRestorationPolicy(strategy)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when(viewType){
            Constants.ChatLayoutViewType.CHAT_MINE_TEXT-> {
                return ChatMineText(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.con_text_message_mine_item, parent, false),context)
            }
            Constants.ChatLayoutViewType.CHAT_OTHER_TEXT-> {
                return ChatOtherText(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.con_text_message_other_item, parent, false),context)
            }
            Constants.ChatLayoutViewType.CHAT_MINE_ATTACHMENT-> {
                return ChatMineAttachment(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.con_attacchment_message_mine_item, parent, false),context)
            }
            Constants.ChatLayoutViewType.CHAT_OTHER_ATTACHMENT-> {
                return ChatOtherAttachment(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.con_attacchment_message_other_item, parent, false),context)
            }
            Constants.ChatLayoutViewType.CHAT_MINE_CONTACT-> {
                return ChatMineContact(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.con_contact_message_mine_item, parent, false),context)
            }
            Constants.ChatLayoutViewType.CHAT_OTHER_CONTACT-> {
                return ChatOtherContact(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.con_contact_message_other_item, parent, false),context)
            }
            Constants.ChatLayoutViewType.CHAT_MINE_AUDIO-> {
                return ChatMineAudio(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.con_audio_message_mine_item, parent, false),context)
            }
            Constants.ChatLayoutViewType.CHAT_OTHER_AUDIO-> {
                return ChatOtherAudio(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.con_audio_message_other_item, parent, false),context)
            }
            Constants.ChatLayoutViewType.CHAT_MINE_LOCATION-> {
                return ChatMineLocation(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.con_location_message_mine_item, parent, false),context)
            }
            Constants.ChatLayoutViewType.CHAT_OTHER_LOCATION-> {
                return ChatOtherLocation(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.con_location_message_other_item, parent, false),context)
            }
            Constants.ChatLayoutViewType.CHAT_GROUP_NOTIFICATION-> {
                return ChatGroupNotification(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.con_group_notification, parent, false))
            }

        }
        return ChatMineText(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.con_text_message_mine_item, parent, false),context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(snapshot().items[position].message_type){
            ConstantValues.MessageTypes.TEXT_MESSAGE->{
                if (snapshot().items[position].is_sender==1){
                    holder as ChatMineText
                    holder.bindData(snapshot().items[position])
                    holder.binding.root.setOnLongClickListener {
                        onLongClickListener(position,snapshot().items[position])
                        false
                    }
                    holder.binding.root.setOnClickListener {
                        onClickListener(position)
                    }
                }else {
                    holder as ChatOtherText
                    holder.bindData(snapshot().items[position])
                    holder.binding.root.setOnLongClickListener {
                        onLongClickListener(position,snapshot().items[position])
                        false
                    }
                    holder.binding.root.setOnClickListener {
                        onClickListener(position)
                    }
                }
            }
            ConstantValues.MessageTypes.ATTACHMENT->{
                if (snapshot().items[position].is_sender==1){
                    holder as ChatMineAttachment
                    holder.bindData(snapshot().items[position])
                    holder.binding.root.setOnLongClickListener {
                        onLongClickListener(position,snapshot().items[position])
                        false
                    }
                    holder.binding.root.setOnClickListener {
                        onClickListener(position)
                    }
                    holder.binding.ivAttachment.setOnClickListener {
                        openPreview(snapshot().items[position])
                    }
                }else {
                    holder as ChatOtherAttachment
                    holder.bindData(snapshot().items[position])
                    holder.binding.root.setOnLongClickListener {
                        onLongClickListener(position,snapshot().items[position])
                        false
                    }
                    holder.binding.root.setOnClickListener {
                        onClickListener(position)
                    }
                    holder.binding.ivAttachment.setOnClickListener {
                        openPreview(snapshot().items[position])
                    }
                }
            }
            ConstantValues.MessageTypes.CONTACT_MESSAGE->{
                if (snapshot().items[position].is_sender==1){
                    holder as ChatMineContact
                    holder.bindData(snapshot().items[position])
                    holder.binding.root.setOnLongClickListener {
                        onLongClickListener(position,snapshot().items[position])
                        false
                    }
                    holder.binding.root.setOnClickListener {
                        onClickListener(position)
                    }
                }else {
                    holder as ChatOtherContact
                    holder.bindData(snapshot().items[position])
                    holder.binding.root.setOnLongClickListener {
                        onLongClickListener(position,snapshot().items[position])
                        false
                    }
                    holder.binding.root.setOnClickListener {
                        onClickListener(position)
                    }
                }
            }
            ConstantValues.MessageTypes.AUDIO_MESSAGE->{
                if (snapshot().items[position].is_sender==1){
                    holder as ChatMineAudio
                    holder.bindData(snapshot().items[position])
                    holder.binding.root.setOnLongClickListener {
                        onLongClickListener(position,snapshot().items[position])
                        false
                    }
                    holder.binding.root.setOnClickListener {
                        onClickListener(position)
                    }
                    holder.binding.clAudioView.setOnClickListener {
                        openPreview(snapshot().items[position])
                    }
                }else {
                    holder as ChatOtherAudio
                    holder.bindData(snapshot().items[position])
                    holder.binding.root.setOnLongClickListener {
                        onLongClickListener(position,snapshot().items[position])
                        false
                    }
                    holder.binding.root.setOnClickListener {
                        onClickListener(position)
                    }
                    holder.binding.clAudioView.setOnClickListener {
                        openPreview(snapshot().items[position])
                    }
                }
            }
            ConstantValues.MessageTypes.LOCATION_MESSAGE->{
                if (snapshot().items[position].is_sender==1){
                    holder as ChatMineLocation
                    holder.bindData(snapshot().items[position])
                    holder.binding.root.setOnLongClickListener {
                        onLongClickListener(position,snapshot().items[position])
                        false
                    }
                    holder.binding.root.setOnClickListener {
                        onClickListener(position)
                    }
                }else {
                    holder as ChatOtherLocation
                    holder.bindData(snapshot().items[position])
                    holder.binding.root.setOnLongClickListener {
                        onLongClickListener(position,snapshot().items[position])
                        false
                    }
                    holder.binding.root.setOnClickListener {
                        onClickListener(position)
                    }
                }
            }
            else->{
                if (Helper.isSeverMessage(snapshot().items[position].message_type)){
                    holder as ChatGroupNotification
                    holder.bindData(snapshot().items[position])
                }
            }
        }
    }

    private fun openPreview(conversationModel: ConversationModel) {
        handler.obtainMessage(2,conversationModel).sendToTarget()
    }

    fun onLongClickListener(position: Int,messageData:ConversationModel){
        selectMessage(position)
    }
    fun onClickListener(position: Int){
        if (selectionMode){
            selectMessage(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position!=-1 && getItem(position)!=null) {
            return if (snapshot().items[position].message_type == ConstantValues.MessageTypes.TEXT_MESSAGE) {
                return if (snapshot().items[position].is_sender == 1) Constants.ChatLayoutViewType.CHAT_MINE_TEXT else Constants.ChatLayoutViewType.CHAT_OTHER_TEXT
            } else if (snapshot().items[position].message_type == ConstantValues.MessageTypes.ATTACHMENT) {
                return if (snapshot().items[position].is_sender == 1) Constants.ChatLayoutViewType.CHAT_MINE_ATTACHMENT else Constants.ChatLayoutViewType.CHAT_OTHER_ATTACHMENT
            }else if (snapshot().items[position].message_type == ConstantValues.MessageTypes.CONTACT_MESSAGE) {
                return if (snapshot().items[position].is_sender == 1) Constants.ChatLayoutViewType.CHAT_MINE_CONTACT else Constants.ChatLayoutViewType.CHAT_OTHER_CONTACT
            }else if (snapshot().items[position].message_type == ConstantValues.MessageTypes.LOCATION_MESSAGE) {
                return if (snapshot().items[position].is_sender == 1) Constants.ChatLayoutViewType.CHAT_MINE_LOCATION else Constants.ChatLayoutViewType.CHAT_OTHER_LOCATION
            } else if (snapshot().items[position].message_type == ConstantValues.MessageTypes.AUDIO_MESSAGE) {
                return if (snapshot().items[position].is_sender == 1) Constants.ChatLayoutViewType.CHAT_MINE_AUDIO else Constants.ChatLayoutViewType.CHAT_OTHER_AUDIO
            }
            else if (Helper.isSeverMessage(snapshot().items[position].message_type)){
                return Constants.ChatLayoutViewType.CHAT_GROUP_NOTIFICATION
            } else {
                return if (snapshot().items[position].is_sender == 1) Constants.ChatLayoutViewType.CHAT_MINE_TEXT else Constants.ChatLayoutViewType.CHAT_OTHER_TEXT
            }
        }
        return -1
    }
    /*override fun getItemCount(): Int {
        return messageList.size
    }*/
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        //recyclerView.scrollToPosition(0)
    }
    public fun selectMessage(position: Int) {
        try {

            if (position != -1) {
                var message = getItem(position)
                if (message!= null  && (message.message_type < 12
                            || message.message_type == ConstantValues.MessageTypes.META_LINK
                            || message.message_type == ConstantValues.MessageTypes.AUDIO_MESSAGE)
                ) {
                    snapshot().items[position].isSelected = if (message.isSelected==1) 0 else 1
                    val count: Int = isAnySelected()
                    if (count == 1) {
                        selectionMode = true
                        handleClipboard(count)
                    } else if (count > 1) {
                        selectionMode = true
                        if (snapshot().items.get(position)
                                .status != ConstantValues.MessageStatus.RECALLED
                        ) {
                            handleClipboard(count)
                        }
                    } else {
                        //handler.sendEmptyMessage(Values.SingleUserChat.CHAT_SELECTION_MODE_INACTIVE)
                        selectionMode = false
                        isMessageLongClick(false)
                    }
                    handler.obtainMessage(1).sendToTarget()
                    notifyDataSetChanged()
                }
            }
        } catch (e: Exception) {
            //Helper.printExceptions(e)
        }
    }
    fun isAnySelected(): Int {
        var count = 0
        try {
            selectMessages = ArrayList()
            for (i in snapshot().items.indices) {
                if (snapshot().items[i].isSelected==1) {
                    selectMessages.add(snapshot().items[i].ID)
                    if (/*snapshot().items.get(i) != null && snapshot().items.get(i).getAlbum()
                            .size() > 0
                    */false) {
                        //count = count + snapshot().items.get(i).getAlbum().size()
                    } else {
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            //Helper.printExceptions(e)
        }
        return count
    }
    var selectedMessagesList : ArrayList<ConversationModel> = ArrayList()
    fun getSelectedMessages():ArrayList<ConversationModel>{
        selectedMessagesList = ArrayList()
        for (i in snapshot().items.indices) {
            if (snapshot().items[i].isSelected==1) {
                selectedMessagesList.add(snapshot().items[i])
            }
        }
        return selectedMessagesList
    }
    var selectMessages :ArrayList<Long> = ArrayList()
    fun unSelectAllMessage() {
        try {
            for (i in snapshot().items.indices) {
                snapshot().items[i].isSelected = 0
            }
            notifyDataSetChanged()
            selectionMode = false
        } catch (e: Exception) {
            //Helper.printExceptions(e)
        }
    }
    fun unSelectAllMessage(postion: Int) {
        try {
            for (i in snapshot().items.indices) {
                snapshot().items[i].isSelected = 0
            }
            snapshot().items[postion].isSelected = 1
            selectionMode = false
            notifyDataSetChanged()
        } catch (e: Exception) {
            //Helper.printExceptions(e)
        }
    }
    fun handleClipboard(selectedCount: Int) {
        try {
            if (selectedCount > 0) {
                if (checkIsTextMessage()) {
                    //handler.sendEmptyMessage(Values.SingleUserChat.CHAT_ENABLE_COPY)
                } else {
                    //handler.sendEmptyMessage(Values.SingleUserChat.CHAT_DISABLE_COPY)
                }
            } else {
                //handler.sendEmptyMessage(Values.SingleUserChat.CHAT_DISABLE_COPY)
            }
        } catch (e: Exception) {
            //Helper.printExceptions(e)
        }
    }
    fun isMessageLongClick(stats:Boolean){
        isLong_click = stats
    }
    fun checkIsTextMessage(): Boolean {
        try {
            for (model in snapshot().items) {
                if (model.isSelected==1 && (model.message_type!= ConstantValues.MessageTypes.TEXT_MESSAGE
                            || model.status == ConstantValues.MessageStatus.RECALLED) && model.message_type!= ConstantValues.MessageTypes.META_LINK) {
                    return false
                }
            }
        } catch (e: Exception) {
            //Helper.printExceptions(e)
        }
        return true
    }

    fun isRecallMessage(): Boolean {
        try {
            for (model in snapshot().items) {
                if (model.message_id>0 && model.isSelected==1 && (model.status!=ConstantValues.MessageStatus.RECALLED && model.status!=ConstantValues.MessageStatus.DELETE)) {
                    if (!Helper.timeCalculate(
                            model.created_at!!,
                            5
                        )
                    ) {
                        return false
                    }
                    return true
                }
            }
        } catch (e: Exception) {
            //Helper.printExceptions(e)
        }
        return true
    }
    fun isForwardMessage(): Boolean {
        try {
            for (model in snapshot().items) {
                if (model.message_id>0 && model.isSelected==1 && !Helper.isSeverMessage (model.message_type) && (model.status!=ConstantValues.MessageStatus.RECALLED && model.status!=ConstantValues.MessageStatus.DELETE)) {
                    return true
                }
            }
        } catch (e: Exception) {
            //Helper.printExceptions(e)
        }
        return true
    }
}