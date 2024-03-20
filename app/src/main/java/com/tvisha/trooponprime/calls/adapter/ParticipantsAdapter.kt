package com.tvisha.trooponprime.calls.adapter

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.solver.widgets.Helper
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.ParticipantsItemAdapterBinding
import com.tvisha.trooponprime.lib.database.model.ParticipantUsers
import com.tvisha.trooponprime.ui.recent.conversation.ChatActivity
import org.json.JSONArray

class ParticipantsAdapter(var context: Context,var handler: Handler,var tmCallUserId:String):RecyclerView.Adapter<ParticipantsAdapter.ViewHolder>() {
    inner class ViewHolder(var dataBinding: ParticipantsItemAdapterBinding):RecyclerView.ViewHolder(dataBinding.root)

    var participants : ArrayList<ParticipantUsers> = ArrayList()
    fun setTheList(list: ArrayList<ParticipantUsers>){
        participants = list
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.call_participants_item_layout,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var model = participants[position]
        Glide.with(context).load(model.userpic)
            .circleCrop()
            .placeholder(ContextCompat.getDrawable(context, R.drawable.default_user)).error(
                ContextCompat.getDrawable(context, R.drawable.default_user)).into(holder.dataBinding.ivEntityProfile)
        holder.dataBinding.tvEntityName.text = model.name
        holder.dataBinding.tvEntityMobile.text = com.tvisha.trooponprime.lib.utils.Helper.getCallStatusText(model.callStatus)
        holder.dataBinding.root.setOnClickListener {
            if (participants[position].user_id!=tmCallUserId) {
                handler.obtainMessage(1, model).sendToTarget()
            }
        }
    }

    override fun getItemCount(): Int {
        return participants.size
    }
}