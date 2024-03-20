package com.tvisha.trooponprime.ui.recent.group

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.tvisha.trooponprime.MyApplication
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.CreateGroupLayoutBinding
import com.tvisha.trooponprime.lib.clientModels.CreateGroupMembers
import com.tvisha.trooponprime.lib.clientModels.UserClientModel
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.ui.recent.group.adapter.GroupUserPicAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddGroupMemberActivity:AppCompatActivity() {
    private var groupUserPicAdapter : GroupUserPicAdapter? = null
    var groupId :String= ""
    lateinit var dataBinding:CreateGroupLayoutBinding
    companion object{
        var selectedUserList : ArrayList<UserClientModel> = ArrayList()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.create_group_layout)
        groupId = intent.getStringExtra("group_id")!!
        setView()
    }
    private fun setView(){
        dataBinding.userList.layoutManager = LinearLayoutManager(this)
        groupUserPicAdapter = GroupUserPicAdapter(this,handler)
        dataBinding.userList.adapter = groupUserPicAdapter
        Thread {
            var list = MyApplication.troopClient.fetchNonGroupUserList(groupId) as ArrayList<UserClientModel>
            runOnUiThread {
                groupUserPicAdapter!!.setList(list as ArrayList<UserClientModel>)
            }
        }.start()
        dataBinding.tvNext.setOnClickListener {
            if (groupUserPicAdapter!!.getSelectedList().size>0){
                selectedUserList = groupUserPicAdapter!!.getSelectedList()
                callBackResults()
            }else{
                Toast.makeText(this,"Please select at least on contact ", Toast.LENGTH_SHORT).show()
            }
        }
        dataBinding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    private fun callBackResults(){
        var listOfUsers : ArrayList<CreateGroupMembers> = ArrayList()
        for (i in 0 until selectedUserList.size){
            var groupMembers = CreateGroupMembers()
            groupMembers.name = selectedUserList[i].entity_name!!
            groupMembers.role = ConstantValues.GroupValues.MEMBER.toInt()
            groupMembers.status = 1
            groupMembers.user_id = selectedUserList[i].entity_id
            listOfUsers.add(groupMembers)
        }

        GlobalScope.launch {
            var response = troopClient.addGroupMembers(groupId,listOfUsers)
            Log.e("response==> ",""+response.toString())
            if (response!=null && response.optBoolean("success")){
                finish()
            }else{
                runOnUiThread {
                    Toast.makeText(this@AddGroupMemberActivity,response.optString("message"),Toast.LENGTH_SHORT).show()
                }
            }
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