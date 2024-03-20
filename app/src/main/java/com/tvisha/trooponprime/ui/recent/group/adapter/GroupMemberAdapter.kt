package com.tvisha.trooponprime.ui.recent.group.adapter

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.ContactItemAdapterBinding
import com.tvisha.trooponprime.lib.database.model.GroupMembersModel
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper
import com.tvisha.trooponprime.ui.recent.contacts.adapter.ContactAdapter
import com.tvisha.trooponprime.ui.recent.conversation.ChatActivity

class GroupMemberAdapter(val context: Context, var handler : Handler): RecyclerView.Adapter<GroupMemberAdapter.ViewHolder>() {
    private var contactList:ArrayList<GroupMembersModel> = ArrayList()
    fun setList(list: ArrayList<GroupMembersModel>){
        contactList = list
        notifyDataSetChanged()
    }
    inner class ViewHolder(val dataBinding: ContactItemAdapterBinding): RecyclerView.ViewHolder(dataBinding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.user_item_layout,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var model = contactList[position]
        Glide.with(context).load(model.member_profile_pic)
            .circleCrop()
            .placeholder(ContextCompat.getDrawable(context, R.drawable.default_user)).error(
                ContextCompat.getDrawable(context, R.drawable.default_user)).into(holder.dataBinding.ivEntityProfile)
        holder.dataBinding.tvEntityName.text = model.member_name
        holder.dataBinding.tvEntityMobile.visibility =View.VISIBLE
        holder.dataBinding.tvEntityMobile.text =Helper.getMemberRoleText(model.member_role)
        holder.dataBinding.tvEntityMobile.setTextColor(if (model.member_role==ConstantValues.GroupValues.ADMIN.toInt())
            ContextCompat.getColor(context,android.R.color.holo_green_dark) else if (model.member_role==ConstantValues.GroupValues.MODERATOR.toInt())
            ContextCompat.getColor(context,android.R.color.holo_orange_light) else ContextCompat.getColor(context,R.color.group_member_color))

        holder.dataBinding.root.setOnClickListener {
            handler.obtainMessage(1,model).sendToTarget()
        }
        holder.dataBinding.tvAddContact.visibility =  View.GONE
        
    }

    override fun getItemCount(): Int {
        return contactList.size
    }
}