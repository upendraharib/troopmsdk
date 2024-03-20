package com.tvisha.trooponprime.ui.recent

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.tvisha.trooponprime.MyApplication
import com.tvisha.trooponprime.MyApplication.Companion.appPreferences
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.constants.Constants
import com.tvisha.trooponprime.databinding.ActivityMainBinding
import com.tvisha.trooponprime.lib.clientModels.Chats
import com.tvisha.trooponprime.lib.clientModels.RecentMessageList
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.ui.recent.adapter.RecentListAdapter
import com.tvisha.trooponprime.ui.recent.archive.ArchiveChatActivity
import com.tvisha.trooponprime.ui.recent.contacts.UserListActivity
import com.tvisha.trooponprime.ui.recent.group.CreateGroupPicUserActivity
import com.tvisha.trooponprime.ui.recent.login.LoginActivity
import com.tvisha.trooponprime.ui.recent.story.StoryListActivity
import com.tvisha.trooponprime.ui.recent.viewmodel.RecentChatViewModel
import org.json.JSONObject


class MainActivity : AppCompatActivity(), MyApplication.ClientListeners/*, ClientCallBackListener*/ {
    //private val apiViewModel : ApiViewModel by inject()
    var viewModel: RecentChatViewModel? = null
    lateinit var dataBinding : ActivityMainBinding
    var recentListAdapter: RecentListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding =DataBindingUtil.setContentView(this,R.layout.activity_main)
        if (appPreferences!!.getBoolean(Constants.SharedPreferenceConstant.LOGIN_APP_STATUS,false)) {
            viewModel = ViewModelProviders.of(this)[RecentChatViewModel::class.java]
            if (!Constants.checkContactPermissions(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        Constants.CONTATCT_PERMISSION,
                        Constants.CONTACT_PERMISSION_CALLBACK
                    )
                }
            }
            setView()
            if (!Constants.checkNotificationPermissions(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        Constants.NOTIFICATION_PERMISSION,
                        Constants.NOTIFICATION_PERMISSION_CALLBACK
                    )
                }
            }
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
        }else{
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        if (troopClient.isLoggedIn()){
            troopClient.setScreen(ConstantValues.CurrentScreen.RECENT,0,1,0)
        }
        var app = application as MyApplication
        app.setListener(this)

        super.onResume()
    }

    override fun onPause() {
        if (troopClient.isLoggedIn()){
            troopClient.setScreen(ConstantValues.CurrentScreen.OTHER,0,1,0)
        }
        super.onPause()
    }
    override fun onBackPressed() {
        if (dataBinding.clNormalActionBar!=null && dataBinding.clNormalActionBar.visibility!=View.VISIBLE){
            recentListAdapter!!.unselectAllChats()
            openActionBar()
            return
        }else{
            super.onBackPressed()
        }
    }
    var isAllVisible = false
    private fun setView(){
        dataBinding.recentList.layoutManager = LinearLayoutManager(this)
        recentListAdapter = RecentListAdapter(this,adapterHandelr)
        dataBinding.recentList.adapter = recentListAdapter
        dataBinding.addFab.shrink()
        dataBinding.addFab.setOnClickListener {
            if (isAllVisible){
                closeActions()
            }else {
                openActions()
            }
        }
        dataBinding.addPersonFab.setOnClickListener {
            closeActions()
            if (Constants.checkContactPermissions(this)) {
                startActivity(Intent(this, UserListActivity::class.java))
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(Constants.CONTATCT_PERMISSION,Constants.CONTACT_PERMISSION_CALLBACK)
                }
            }

        }
        dataBinding.addGroupFab.setOnClickListener {
            closeActions()
            startActivity(Intent(this, CreateGroupPicUserActivity::class.java))
        }
        dataBinding.addStory.setOnClickListener {
            closeActions()
            startActivity(Intent(this, StoryListActivity::class.java))
        }
        setList()
        dataBinding.ivLogout.setOnClickListener {
            confirmDialog()
        }
        dataBinding.ivArchive.setOnClickListener {
            startActivity(Intent(this,ArchiveChatActivity::class.java))
        }
    }
    private fun deleteChats(recentMessageList: ArrayList<RecentMessageList>){
        var deleteChats : ArrayList<Chats> = ArrayList()
        for (i in 0 until recentMessageList.size){
            deleteChats.add(Chats(recentMessageList[i].entity_id.toString(),recentMessageList[i].entity,0))
        }
        troopClient.deleteChats(deleteChats)

    }
    private fun archiveUnArchiveChat(recentMessageList: ArrayList<RecentMessageList>) {
        var archiveChats : ArrayList<Chats> = ArrayList()
        for (i in 0 until recentMessageList.size){
            if (recentMessageList[i].isArchive==0) {
                archiveChats.add(
                    Chats(
                        recentMessageList[i].entity_id.toString(),
                        recentMessageList[i].entity,
                        1
                    )
                )
            }
        }
        if (archiveChats.isNotEmpty()){
            troopClient.archiveChats(archiveChats)
        }
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

    private fun confirmDialog(){
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setIcon(R.drawable.log_out)
            .setPositiveButton(android.R.string.yes,
                DialogInterface.OnClickListener { dialog, whichButton ->
                    dialog.dismiss()
                    troopClient.logout()
                    startActivity(Intent(this,LoginActivity::class.java))
                    finish()
                })
            .setNegativeButton(android.R.string.no, DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
            }).show()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                Constants.CONTACT_PERMISSION_CALLBACK -> {
                    //startActivity(Intent(this, UserListActivity::class.java))
                }
            }
        }
    }
    private fun openActions(){
        isAllVisible = true
        dataBinding.addPersonFab.visibility = View.VISIBLE
        dataBinding.addPersonFabText.visibility = View.VISIBLE
        dataBinding.addGroupFab.visibility = View.VISIBLE
        dataBinding.addGroupFabText.visibility = View.VISIBLE
        dataBinding.addStory.visibility = View.VISIBLE
        dataBinding.addStoryFabText.visibility = View.VISIBLE
        dataBinding.addFab.extend()
    }
    private fun closeActions(){
        isAllVisible = false
        dataBinding.addPersonFab.visibility = View.GONE
        dataBinding.addPersonFabText.visibility = View.GONE
        dataBinding.addGroupFab.visibility = View.GONE
        dataBinding.addGroupFabText.visibility = View.GONE
        dataBinding.addStory.visibility = View.GONE
        dataBinding.addStoryFabText.visibility = View.GONE
        dataBinding.addFab.shrink()
    }
    var recentMessageList:ArrayList<RecentMessageList> = ArrayList()
    var archiveList:ArrayList<RecentMessageList> = ArrayList()
    private fun setList(){
        troopClient.fetchRecentList().observe(this, Observer {
            if (it!=null){
                recentMessageList = it as ArrayList<RecentMessageList>
                recentMessageList.addAll(archiveList)
                recentListAdapter!!.setTheRecentList(recentMessageList)
            }
            syncing(false)
        })
        //fetchArchiveList()
    }
    private fun fetchArchiveList(){
        troopClient.fetchArchiveRecentList().observe(this, Observer {
            //recentMessageList.removeAll(archiveList)
            archiveList = it as ArrayList<RecentMessageList>
            recentMessageList.addAll(archiveList)
            recentListAdapter!!.setTheRecentList(recentMessageList)
        })
    }

    override fun typing(jsonObject: JSONObject) {

        if (jsonObject!=null){
            var listSize = recentMessageList.size
            for (i in 0 until listSize) {
                Log.e("SDk==> ", " typing main "+recentMessageList[i].entity_id+"   "+recentMessageList[i].entity+"   "+((jsonObject.optLong("user_id") == recentMessageList[i].entity_id && jsonObject.optInt("is_group")==(recentMessageList[i].entity-1))))
                if (jsonObject.optLong("user_id") == recentMessageList[i].entity_id && jsonObject.optInt("is_group")==(recentMessageList[i].entity-1)) {
                    if (jsonObject.optInt("typing")==1){
                        recentMessageList[i].isTyping = 1
                        recentMessageList[i].typingData = if (jsonObject.optInt("is_group")==1) jsonObject.optString("typing_user_name")+" typing..." else  "typing..."
                    }else{
                        recentMessageList[i].isTyping = 0
                        recentMessageList[i].typingData = ""
                    }
                    runOnUiThread {
                        recentListAdapter!!.notifyItemChanged(i,recentMessageList[i])
                    }
                    break
                }
            }
        }
    }

    override fun userStatus(jsonObject: JSONObject) {

    }

    override fun syncing(status: Boolean) {
        if (status){
            if (mProgressDialog==null || (mProgressDialog!=null && !mProgressDialog!!.isShowing)) {
                runOnUiThread {
                    showDialog()
                }
            }
        }else{
            runOnUiThread {
                if (mProgressDialog!=null && mProgressDialog!!.isShowing){
                    mProgressDialog!!.dismiss()
                }
            }

        }
    }
    var mProgressDialog: ProgressDialog? = null
    fun showDialog(){
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setTitle("TroopSDK INITIALIZING")
        mProgressDialog!!.setMessage("Please wait we are fetching your data")
        mProgressDialog!!.show()
    }
    /*override fun clientConnected() {

    }
    override fun tmError(jsonObject: JSONObject) {
        Log.e("tmerror==> ",jsonObject.toString())
    }

    override fun messageSaved(jsonObject: JSONObject) {

    }

    override fun userPresenceResponse(jsonObject: JSONObject) {

    }

    override fun reportUserResponse(jsonObject: JSONObject) {

    }

    override fun blockUserResponse(jsonObject: JSONObject) {

    }

    override fun likeUnLikeMessageResponse(jsonObject: JSONObject) {

    }

    override fun reportMessageResponse(jsonObject: JSONObject) {

    }

    override fun deleteChatResponse(jsonObject: JSONObject) {

    }

    override fun recallResponse(jsonObject: JSONObject) {

    }

    override fun deleteResponse(jsonObject: JSONObject) {

    }

    override fun loginResponse(jsonObject: JSONObject) {
        Log.e("Callback ", "loginResponse=> ${jsonObject.toString()}")
    }*/

}

