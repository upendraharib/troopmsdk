package com.tvisha.trooponprime.ui.recent.forkout.adapter

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
import com.tvisha.trooponprime.lib.database.model.ForwardModel
import com.tvisha.trooponprime.ui.recent.forward.adapter.ForwardUserAdapter

class ForkOutAdapter (val context: Context, var handler : Handler): RecyclerView.Adapter<ForkOutAdapter.ViewHolder>() {
    private var contactList:ArrayList<ForwardModel> = ArrayList()
    fun setList(list:ArrayList<ForwardModel>){
        contactList = list
        notifyDataSetChanged()
    }
    inner class ViewHolder(val dataBinding: ContactItemAdapterBinding): RecyclerView.ViewHolder(dataBinding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.user_item_layout,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var model = contactList[position]
        holder.dataBinding.cbSelectBoc.visibility = View.VISIBLE
        Glide.with(context).load(model.entity_avatar)
            .circleCrop()
            .placeholder(ContextCompat.getDrawable(context, R.drawable.default_user)).error(
                ContextCompat.getDrawable(context, R.drawable.default_user)).into(holder.dataBinding.ivEntityProfile)
        holder.dataBinding.tvEntityName.text = model.entity_name
        holder.dataBinding.tvEntityMobile.visibility = View.GONE//model.mobile
        holder.dataBinding.cbSelectBoc.isChecked = if (contactList[position].isSelected==1) true else false
        /*holder.dataBinding.root.setOnClickListener {
            //handler.obtainMessage(1,model).sendToTarget()
        }*/
        holder.dataBinding.root.setOnClickListener {
            contactList[position].isSelected = if (contactList[position].isSelected==1) 0 else 1
            notifyDataSetChanged()
        }
        holder.dataBinding.tvAddContact.visibility =  View.GONE
    }
    override fun getItemCount(): Int {
        return contactList.size
    }

    fun getSelectedList():ArrayList<ForwardModel>{
        var list :ArrayList<ForwardModel> = ArrayList()
        for (i in 0 until contactList.size){
            if (contactList[i].isSelected==1){
                list.add(contactList[i])
            }
        }
        return list
    }
}