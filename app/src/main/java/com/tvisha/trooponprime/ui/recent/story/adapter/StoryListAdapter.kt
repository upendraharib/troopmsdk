package com.tvisha.trooponprime.ui.recent.story.adapter

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.solver.widgets.Helper
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.ContactItemAdapterBinding
import com.tvisha.trooponprime.databinding.StoryListItemAdapterBinding
import com.tvisha.trooponprime.lib.clientModels.StoryModel
import com.tvisha.trooponprime.lib.utils.ConstantValues

class StoryListAdapter(var context:Context,var handler: Handler):RecyclerView.Adapter<StoryListAdapter.ViewHolder>() {
    var storyList:ArrayList<StoryModel> = ArrayList()
    fun setList(list: ArrayList<StoryModel>){
        storyList = list
        Log.e("data==> ",""+storyList.size)
        notifyDataSetChanged()
    }
    inner class ViewHolder(val dataBinding: StoryListItemAdapterBinding):RecyclerView.ViewHolder(dataBinding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.story_user_item,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var model = storyList[position]
        Glide.with(context).load(model.profile_pic).circleCrop().placeholder(R.drawable.default_user).into(holder.dataBinding.ivEntityProfile)
        holder.dataBinding.tvEntityName.text = model.user_name!!
        holder.dataBinding.tvStatusTime.text = com.tvisha.trooponprime.lib.utils.Helper.getConversationMessagesTime(model.created_at)
        holder.dataBinding.ivRead.visibility = if (model.seen==ConstantValues.StoryStatus.VIEWED) View.GONE else View.VISIBLE
        holder.dataBinding.ivRead.setOnClickListener {
            handler.obtainMessage(1,model).sendToTarget()
        }
        holder.dataBinding.ivDelete.setOnClickListener {
            handler.obtainMessage(2,model).sendToTarget()
        }
    }

    override fun getItemCount(): Int {
        Log.e("size==> ",""+storyList.size)
        return storyList.size
    }
}