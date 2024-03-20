package com.tvisha.trooponprime.ui.recent.adapter

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
import com.tvisha.trooponprime.databinding.MediaItemAdapterBinding
import com.tvisha.trooponprime.databinding.MediaLayoutBinding
import com.tvisha.trooponprime.lib.clientModels.MediaModel
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper

class MediaAdapter(var context:Context,var handler:Handler):RecyclerView.Adapter<MediaAdapter.ViewHolder>() {
    private var mediaList:ArrayList<MediaModel> = ArrayList()
    fun setTheList(list:ArrayList<MediaModel>){
        mediaList = list
        notifyDataSetChanged()
    }
    inner class ViewHolder(var dataBinding: MediaItemAdapterBinding):RecyclerView.ViewHolder(dataBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.media_layout_item,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var attachmentType = Helper.getFileIconPath(mediaList[position].attachment)
        if (attachmentType== ConstantValues.Gallery.GALLERY_IMAGE || attachmentType== ConstantValues.Gallery.GALLERY_VIDEO) {
            Glide.with(context).load(mediaList[position].attachment)
                .placeholder(ContextCompat.getDrawable(context, android.R.drawable.ic_menu_gallery))
                .error(ContextCompat.getDrawable(context, android.R.drawable.ic_menu_gallery))
                .into(holder.dataBinding.ivMediaImage)
        }else{
            var icon = Helper.getAttachmentIcon(attachmentType)
            Glide.with(context).load(icon)
                .placeholder(ContextCompat.getDrawable(context, android.R.drawable.ic_menu_gallery))
                .error(ContextCompat.getDrawable(context, android.R.drawable.ic_menu_gallery))
                .into(holder.dataBinding.ivMediaImage)
        }
        holder.dataBinding.tvFileName.text = Helper.getFileNameWithExt(mediaList[position].attachment!!)
        holder.dataBinding.tvCreatedAt.text =Helper.getRecentListMessagesDateTime(mediaList[position].created_at!!)
        holder.dataBinding.tvSenderName.text = mediaList[position].sender_name!!
        holder.dataBinding.ivDownload.visibility = if (mediaList[position].attachment_downloaded==1) View.GONE else View.VISIBLE
        holder.dataBinding.root.setOnClickListener {
            handler.obtainMessage(1,mediaList[position]).sendToTarget()
        }
    }
    override fun getItemCount(): Int {
        return mediaList.size
    }
}