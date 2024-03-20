package com.tvisha.trooponprime.ui.recent.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.MediaLayoutBinding
import com.tvisha.trooponprime.lib.clientModels.ConversationModel
import com.tvisha.trooponprime.lib.clientModels.MediaModel
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.ui.recent.adapter.MediaAdapter
import com.tvisha.trooponprime.ui.recent.preview.AttachmentViewActivity

class MediaFragment(var userId:String,var userUid:String,var entityType:Int,var entityName:String):Fragment() {
    lateinit var dataBinding: MediaLayoutBinding
    var adapter : MediaAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBinding = DataBindingUtil.inflate(inflater,R.layout.media_layout,container,false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setView()
    }
    private fun setView(){
        dataBinding.rcvMedia.layoutManager = GridLayoutManager(requireActivity(),3)
        adapter = MediaAdapter(requireActivity(),adapterHandler)
        dataBinding.rcvMedia.adapter = adapter
        setList()
    }
    private val adapterHandler = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1->{
                    openPreviewPage(msg.obj as MediaModel)
                }
            }
        }
    }
    private fun setList(){
        if (entityType==ConstantValues.Entity.USER) {
            troopClient.fetchUserMedia(userId).observe(requireActivity(), Observer {
                if (it!=null){
                    adapter!!.setTheList(it as ArrayList<MediaModel>)
                }
            })
        }else {
            troopClient.fetchGroupMedia(userId).observe(requireActivity(), Observer {
                if (it!=null){
                    adapter!!.setTheList(it as ArrayList<MediaModel>)
                }
            })
        }
    }
    fun openPreviewPage(mediaModel: MediaModel){
        var intent = Intent(requireActivity(), AttachmentViewActivity::class.java)
        intent.putExtra("entity_id",userUid)
        intent.putExtra("entity_name",entityName)
        intent.putExtra("entity_type",entityType)
        intent.putExtra("path",mediaModel.attachment)
        intent.putExtra("sender_name",mediaModel.sender_name)
        intent.putExtra("created_at",mediaModel.created_at)
        startActivity(intent)
    }
}