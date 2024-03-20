package com.tvisha.trooponprime.ui.recent.contacts

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.ContactsContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.MyApplication.Companion.usersContacts
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.UserlistActivityBinding
import com.tvisha.trooponprime.lib.clientModels.UserClientModel
import com.tvisha.trooponprime.ui.recent.contacts.adapter.ContactAdapter



class UserListActivity:AppCompatActivity() {
    var contactAdapter : ContactAdapter? = null
    lateinit var dataBinding:UserlistActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.userlist_activity)
        setView()
    }
    fun setView(){
        dataBinding.userList.layoutManager = LinearLayoutManager(this)
        contactAdapter = ContactAdapter(this,handler)
        dataBinding.userList.adapter = contactAdapter
        Thread {
            var list = troopClient.fetchContactList() as ArrayList<UserClientModel>
            Log.e("list==> ",""+Gson().toJson(list))
            //contactAdapter!!.setList(list as ArrayList<UserClientModel>)
            var otherList = usersContacts
            list.addAll(otherList)
            runOnUiThread {
                contactAdapter!!.setList(list as ArrayList<UserClientModel>)
            }

        }.start()
        dataBinding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    private val handler = @SuppressLint("HandlerLeak")
    object :Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what){
                1->{
                    var userClientModel = msg.obj as UserClientModel
                    inviteUser(userClientModel)
                    /*if (userClientModel!=null){
                        GlobalScope.launch {
                            var listOfContacts : ArrayList<Contact> = ArrayList()
                            var contact = Contact()
                            contact.name= userClientModel.entity_name!!
                            contact.mobile = userClientModel.mobile!!
                            contact.country_code="91"
                            listOfContacts.add(contact)
                            troopClient.saveContacts(listOfContacts)
                            Log.e("list==> "," called savecontact ")
                        }

                    }*/
                }
            }
        }
    }

    private fun inviteUser(userClientModel: UserClientModel) {
        val link = "https://play.google.com/store/apps/details?id=com.tvisha.troopmessenger&pli=1"

        val uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, link)
        intent.putExtra(Intent.EXTRA_TITLE, "TroopMessenger")

        startActivity(Intent.createChooser(intent, "Share Link"))
    }

    @SuppressLint("Range")
    private fun fetchContacts(existingContact:ArrayList<UserClientModel>):ArrayList<UserClientModel>{
        var list :ArrayList<UserClientModel> = ArrayList()
        val cr = contentResolver
        val cur = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                //var avatar = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    var userClientModel = UserClientModel()
                    val pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null)
                    var phoneNo = ""
                    while (pCur!!.moveToNext()) {
                         phoneNo = pCur!!.getString(pCur!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    }
                    if (notExists(phoneNo,existingContact)) {
                        userClientModel.isAlreadyUser = 0
                        userClientModel.entity = 1
                        userClientModel.mobile = phoneNo
                        userClientModel.entity_name = name
                        //userClientModel.entity_avatar = avatar
                        list.add(userClientModel)
                    }
                    pCur!!.close()
                }
            }
        }
        cur?.close()
        return list
    }

    private fun notExists(phoneNo: String, existingContact: java.util.ArrayList<UserClientModel>): Boolean {
        for (i in 0 until existingContact.size){
            if (existingContact[i].mobile==phoneNo){
                return true
            }
        }
        return true
    }
}