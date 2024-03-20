package com.tvisha.trooponprime.ui.recent.archive

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.tvisha.trooponprime.MyApplication
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.ArchiveChatLayoutBinding
import com.tvisha.trooponprime.lib.clientModels.Chats
import com.tvisha.trooponprime.lib.clientModels.RecentMessageList
import com.tvisha.trooponprime.ui.recent.adapter.RecentListAdapter
import com.tvisha.trooponprime.ui.recent.viewmodel.RecentChatViewModel


class ArchiveChatActivity:AppCompatActivity() {
    var viewModel: RecentChatViewModel? = null
    var recentListAdapter: RecentListAdapter? = null
    lateinit var dataBinding:ArchiveChatLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.archive_chat_layout)
        viewModel = ViewModelProviders.of(this)[RecentChatViewModel::class.java]
        setView()
    }
    fun setView(){
        dataBinding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        dataBinding.ivArchiveUnArchive.setOnClickListener {
            var listCount = recentListAdapter!!.getSelectedList()
            archiveUnArchiveChat(listCount)
            recentListAdapter!!.unselectAllChats()
            openActionBar()
        }
        dataBinding.ivDeleteChats.setOnClickListener {
            var listCount = recentListAdapter!!.getSelectedList()
            deleteChats(listCount)
            recentListAdapter!!.unselectAllChats()
            openActionBar()
        }
        dataBinding.recentList.layoutManager = LinearLayoutManager(this)
        recentListAdapter = RecentListAdapter(this,adapterHandelr)
        dataBinding.recentList.adapter = recentListAdapter
        setList()
    }
    private fun setList(){
        MyApplication.troopClient.fetchArchiveRecentList().observe(this, Observer {
            if (it!=null){

                recentListAdapter!!.setTheRecentList(it as ArrayList<RecentMessageList>)
            }

        })
    }
    private fun deleteChats(recentMessageList: ArrayList<RecentMessageList>){
        var deleteChats : ArrayList<Chats> = ArrayList()
        for (i in 0 until recentMessageList.size){
            deleteChats.add(Chats(recentMessageList[i].entity_id.toString(),recentMessageList[i].entity,0))
        }
        MyApplication.troopClient.deleteChats(deleteChats)

    }
    private val adapterHandelr = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1-> {
                    openActionBar()
                    //archiveUnArchiveChat(msg.obj as RecentMessageList)
                }
            }
        }
    }
    private fun openActionBar(){
        var listCount = recentListAdapter!!.getSelectedList()
        if (listCount.isEmpty()){
            dataBinding.clNormalActionBar.visibility = View.VISIBLE
            dataBinding.clOptionsActionBar.visibility = View.GONE
        }else{
            dataBinding.clNormalActionBar.visibility = View.GONE
            dataBinding.clOptionsActionBar.visibility = View.VISIBLE
            dataBinding.tvSelectedChats.text = "Selected "+listCount.size
        }

    }
    private fun archiveUnArchiveChat(recentMessageList: ArrayList<RecentMessageList>) {
        var unArchiveChats : ArrayList<Chats> = ArrayList()
        for (i in 0 until recentMessageList.size){
            if (recentMessageList[i].isArchive==1){
                unArchiveChats.add(Chats(recentMessageList[i].entity_id.toString(),recentMessageList[i].entity,0))
            }
        }
        if (unArchiveChats.isNotEmpty()){
            MyApplication.troopClient.unArchiveChats(unArchiveChats)
        }
    }

}