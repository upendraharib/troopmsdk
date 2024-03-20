package com.tvisha.trooponprime.ui.recent.forward

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.CreateGroupLayoutBinding
import com.tvisha.trooponprime.lib.clientModels.Forward
import com.tvisha.trooponprime.lib.database.model.ForwardModel
import com.tvisha.trooponprime.ui.recent.forward.adapter.ForwardUserAdapter

import org.json.JSONArray
import org.json.JSONObject

class ForwardUserListActivity : AppCompatActivity() {
    var adapter: ForwardUserAdapter? = null
    var messageList : String = ""
    lateinit var dataBinding:CreateGroupLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.create_group_layout)
        messageList = intent.getStringExtra("data")!!
        setView()
    }
    fun setView(){
        dataBinding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        dataBinding.tvCreateGroup.text = "Forward Message"
        dataBinding.tvSend.visibility = View.VISIBLE
        dataBinding.tvNext.visibility = View.GONE
        dataBinding.userList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        adapter = ForwardUserAdapter(this, handler)
        dataBinding.userList.adapter = adapter
        setlist()
        dataBinding.tvSend.setOnClickListener {
            var list = adapter!!.getSelectedList()
            Log.e("listsize==> ",""+list.size)
            if (list.isEmpty()){
                Toast.makeText(this,"Please select at least on contact!",Toast.LENGTH_SHORT).show()
            }else {
                sendResultsBack(list)
            }
        }
    }
    private fun sendResultsBack(list: ArrayList<ForwardModel>) {
        var forwardArray :ArrayList<Forward> = ArrayList()
        var length = list.size
        for (i in 0 until length){
            var jsonObject = Forward()
            jsonObject.receiver_id = list[i].entity_id.toString()
            jsonObject.entity = list[i].entity
            jsonObject.entity_uid = list[i].entity_uid
            forwardArray.add(jsonObject)
        }
        intent.putExtra("forward",Gson().toJson(forwardArray))
        intent.putExtra("message_id",messageList)

        setResult(Activity.RESULT_OK,intent)
        finish()
    }
    fun setlist(){
        troopClient.fetchForwardList().observe(this, Observer {
            if (it.isNotEmpty()){
                adapter!!.setList(it as ArrayList<ForwardModel>)
            }
        })
    }
    var handler = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1->{

                }
            }
        }
    }

}