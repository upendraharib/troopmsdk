package com.tvisha.trooponprime.ui.recent.contacts

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.tvisha.trooponprime.MyApplication
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.ContactPicLayoutBinding
import com.tvisha.trooponprime.lib.clientModels.UserClientModel
import com.tvisha.trooponprime.ui.recent.contacts.adapter.MobileContactAdapter

import org.json.JSONObject

class ContactPicActivity:AppCompatActivity() {
    var adapter : MobileContactAdapter?= null
    lateinit var dataBinding : ContactPicLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.contact_pic_layout)
        setView()
    }
    private fun setView(){
        dataBinding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        dataBinding.contactList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        adapter = MobileContactAdapter(this, handler)
        dataBinding.contactList.adapter = adapter

        adapter!!.setList(MyApplication.deviceContacts)
    }
    private val handler = @SuppressLint("HandlerLeak")
    object :Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1->{
                    var userClientModel = msg.obj as UserClientModel
                    sendContact(userClientModel)
                }
            }
        }
    }

    private fun sendContact(userClientModel: UserClientModel) {
        val intent: Intent = Intent()
        var jsonObject = JSONObject()
        jsonObject.put("contact_name",userClientModel.entity_name)
        jsonObject.put("contact_number",userClientModel.mobile)
        intent.putExtra("data", jsonObject.toString())
        Log.e("contact--->"," data "+jsonObject.toString())
        setResult(RESULT_OK,intent)
        finish()
    }
}