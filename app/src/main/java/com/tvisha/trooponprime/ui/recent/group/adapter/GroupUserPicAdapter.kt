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
import com.tvisha.trooponprime.lib.clientModels.UserClientModel

class GroupUserPicAdapter(val context: Context, var handler : Handler): RecyclerView.Adapter<GroupUserPicAdapter.ViewHolder>() {
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
            .placeholder(ContextCompat.getDrawable(context, R.drawable.default_user)).error(
                ContextCompat.getDrawable(context, R.drawable.default_user)).into(holder.dataBinding.ivEntityProfile)
        holder.dataBinding.tvEntityName.text = model.entity_name
        holder.dataBinding.cbSelectBoc.isChecked = if (contactList[position].isSelected==1) true else false
        holder.dataBinding.tvEntityMobile.text =model.mobile
        holder.dataBinding.cbSelectBoc.visibility = View.VISIBLE
        holder.dataBinding.root.setOnClickListener {
            contactList[position].isSelected = if (contactList[position].isSelected==1) 0 else 1
            notifyDataSetChanged()
        }

        //holder.dataBinding.tvAddContact.visibility = if (model.isAlreadyUser!=1) View.VISIBLE else View.GONE
        /*holder.dataBinding.tvAddContact.setOnClickListener {
            handler.obtainMessage(1,model).sendToTarget()
        }*/
    }

    override fun getItemCount(): Int {
        return contactList.size
    }
    fun getSelectedList():ArrayList<UserClientModel>{
        var list :ArrayList<UserClientModel> = ArrayList()
        for (i in 0 until contactList.size){
            if (contactList[i].isSelected==1){
                list.add(contactList[i])
            }
        }
        return list
    }
}