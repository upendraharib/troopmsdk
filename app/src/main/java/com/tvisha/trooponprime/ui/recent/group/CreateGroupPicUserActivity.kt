package com.tvisha.trooponprime.ui.recent.group

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.CreateGroupLayoutBinding
import com.tvisha.trooponprime.ui.recent.group.adapter.GroupUserPicAdapter
import com.tvisha.trooponprime.lib.clientModels.UserClientModel


class CreateGroupPicUserActivity:AppCompatActivity() {
    private var groupUserPicAdapter : GroupUserPicAdapter? = null
    lateinit var dataBinding:CreateGroupLayoutBinding
    companion object{
        var selectedUserList : ArrayList<UserClientModel> = ArrayList()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.create_group_layout)
        setView()
    }
    private fun setView(){
        dataBinding.userList.layoutManager = LinearLayoutManager(this)
        groupUserPicAdapter = GroupUserPicAdapter(this,handler)
        dataBinding.userList.adapter = groupUserPicAdapter
        Thread {
            var list = troopClient.fetchUsersListForCreatingGroup() as ArrayList<UserClientModel>
            runOnUiThread {
                groupUserPicAdapter!!.setList(list as ArrayList<UserClientModel>)
            }
        }.start()
        dataBinding.tvNext.setOnClickListener {
            if (groupUserPicAdapter!!.getSelectedList().size>0){
                selectedUserList = groupUserPicAdapter!!.getSelectedList()
                resultLauncher.launch(Intent(this, CreateGroupActivity::class.java))
            }else{
                Toast.makeText(this,"Please select at least on contact ",Toast.LENGTH_SHORT).show()
            }
        }
        dataBinding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            finish()
        }
    }
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what){
                1->{

                }
            }
        }
    }
}
