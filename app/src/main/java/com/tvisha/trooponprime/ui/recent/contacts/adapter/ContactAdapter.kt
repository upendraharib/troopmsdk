package com.tvisha.trooponprime.ui.recent.contacts.adapter

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
import com.tvisha.trooponprime.ui.recent.conversation.ChatActivity
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.ContactItemAdapterBinding
import com.tvisha.trooponprime.lib.clientModels.UserClientModel
import com.tvisha.trooponprime.lib.utils.ConstantValues

class ContactAdapter(val context: Context,var handler :Handler): RecyclerView.Adapter<ContactAdapter.ViewHolder>() {
    private var contactList:ArrayList<UserClientModel> = ArrayList()
    fun setList(list:ArrayList<UserClientModel>){
        contactList = list
        notifyDataSetChanged()
    }
    inner class ViewHolder(val dataBinding: ContactItemAdapterBinding): RecyclerView.ViewHolder(dataBinding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.user_item_layout,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var model = contactList[position]
        Glide.with(context).load(model.entity_avatar)
            .circleCrop()
            .placeholder(ContextCompat.getDrawable(context,R.drawable.default_user)).error(ContextCompat.getDrawable(context,R.drawable.default_user)).into(holder.dataBinding.ivEntityProfile)
        holder.dataBinding.tvEntityName.text = model.entity_name
        holder.dataBinding.tvEntityMobile.text =model.mobile
        holder.dataBinding.root.setOnClickListener {
            if (model.isAlreadyUser==1) {
                var intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("entity_name",model.entity_name)
                intent.putExtra("entity_id", model.entity_id.toString())
                intent.putExtra("entity_type", ConstantValues.Entity.USER)
                intent.putExtra("entity_uid", model.uid)
                context.startActivity(intent)
            }else{

            }
        }
        holder.dataBinding.tvAddContact.visibility = if (model.isAlreadyUser!=1) View.VISIBLE else View.GONE
        holder.dataBinding.tvAddContact.setOnClickListener {
            handler.obtainMessage(1,model).sendToTarget()
        }
    }
    override fun getItemCount(): Int {
        return contactList.size
    }
}