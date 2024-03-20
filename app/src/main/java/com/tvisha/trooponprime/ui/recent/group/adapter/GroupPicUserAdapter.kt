package com.tvisha.trooponprime.ui.recent.group.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tvisha.trooponprime.ui.recent.conversation.ChatActivity
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.GroupPickedItemAdapterBinding
import com.tvisha.trooponprime.lib.clientModels.UserClientModel

class GroupPicUserAdapter(val context: Context): RecyclerView.Adapter<GroupPicUserAdapter.ViewHolder>() {
    private var contactList:ArrayList<UserClientModel> = ArrayList()
    fun setList(list:ArrayList<UserClientModel>){
        contactList = list
        notifyDataSetChanged()
    }
    inner class ViewHolder(val dataBinding: GroupPickedItemAdapterBinding): RecyclerView.ViewHolder(dataBinding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.group_picked_item,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var model = contactList[position]
        Glide.with(context).load(model.entity_avatar)
            .circleCrop()
            .placeholder(ContextCompat.getDrawable(context, R.drawable.default_user)).error(
                ContextCompat.getDrawable(context, R.drawable.default_user)).into(holder.dataBinding.ivEntityProfile)
        holder.dataBinding.tvEntityName.text = model.entity_name
        holder.dataBinding.root.setOnClickListener {
            //context.startActivity(Intent(context, ChatActivity::class.java))
        }


        //holder.dataBinding.tvAddContact.visibility = if (model.isAlreadyUser!=1) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

}