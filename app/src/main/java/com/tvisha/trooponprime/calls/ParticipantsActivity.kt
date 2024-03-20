package com.tvisha.trooponprime.calls

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.calls.adapter.ParticipantsAdapter
import com.tvisha.trooponprime.databinding.CallPraticipatnLayoutBinding
import com.tvisha.trooponprime.databinding.CallPreviewLayoutBinding
import com.tvisha.trooponprime.lib.database.model.GroupMembersModel
import com.tvisha.trooponprime.lib.database.model.ParticipantUsers
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper
import com.tvisha.trooponprime.ui.recent.conversation.ChatActivity
import com.tvisha.trooponprime.ui.recent.group.GroupProfileActivity
import com.tvisha.trooponprime.ui.recent.user.SelfUserProfileActivity
import com.tvisha.trooponprime.ui.recent.user.UserProfileActivity
import com.tvisha.trooponprime.ui.recent.viewmodel.RecentChatViewModel

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class ParticipantsActivity : AppCompatActivity() {
    private var participantsAdapter : ParticipantsAdapter? = null
    var callId:String = ""
    var participantsData : JSONObject = JSONObject()
    var viewModel: RecentChatViewModel? = null
    var mineHost = false
    var tmCallUserId = ""
    lateinit var dataBinding: CallPraticipatnLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.call_praticipatn_layout)
        callId = intent.getStringExtra("call_id")!!
        participantsData = Helper.stringToJsonObject(intent.getStringExtra("participants")!!)!!
        setView()
    }
    fun setView(){
        dataBinding.userList.layoutManager = LinearLayoutManager(this)
        tmCallUserId = troopClient.callTmUserId()
        participantsAdapter = ParticipantsAdapter(this,handler,tmCallUserId)
        dataBinding.userList.adapter = participantsAdapter
        mineHost = troopClient.mineCallHost()
        viewModel = ViewModelProviders.of(this)[RecentChatViewModel::class.java]
        /*troopClient.callUserStatus().observe(this, Observer {
            if (it!=null){
                Log.e("called---> ",""+it.size)
                if (it.isNotEmpty()){
                    participantsAdapter!!.setTheList(it)
                }
            }
        })*/

        var participantsListArray = troopClient.fetchParticipantList()
        participantsAdapter!!.setTheList(participantsListArray)

    }
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what){
                1->{
                    if (mineHost) {
                        openBottomSheet(msg.obj as ParticipantUsers)
                    }
                }
            }
        }
    }
    private fun openBottomSheet(participantUsers: ParticipantUsers){
        var isHost = participantUsers.isHost==1
        var isAudioAdded = participantUsers.audioAdded==1
        var isVideoMuted = participantUsers.isVideoMuted==1
        var isAudioMuted = participantUsers.isAudioMuted==1
        var isExternal = participantUsers.isExternalUser
        var isVideoAdded = participantUsers.videoAdded==1
        var callstatus =participantUsers.callStatus
        Log.e("call---> ",""+Gson().toJson(participantUsers))

        var bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val inflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val contentView = inflater.inflate(R.layout.call_user_options, null)
        contentView.setBackgroundColor(ContextCompat.getColor(this,android.R.color.transparent))
        bottomSheetDialog.setContentView(contentView)

        val mBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(contentView.parent as View)
        bottomSheetDialog!!.setOnShowListener {
            mBehavior.peekHeight = contentView.height //get the height dynamically
        }
        val tvRevokeHost = bottomSheetDialog!!.findViewById<TextView>(R.id.tvRevokeHost)
        if (isHost) {
            tvRevokeHost!!.text = "Revoke host"
        } else {
            tvRevokeHost!!.text = "Make host"
        }
        tvRevokeHost!!.setOnClickListener {
            bottomSheetDialog!!.dismiss()
            val host = if (isHost) 0 else 1
            if (!isHost) {
                troopClient.makeUserAsCallHost(participantUsers.user_id)
            }else{
                troopClient.removeUserAsCallHost(participantUsers.user_id)
            }
            /*if (callSocket != null && callSocket!!.connected()) {
                emitHostUpdate(host, contact.user_id.toString())
            } else {
                showToast(this,getString(R.string.Check_network_connection))
            }*/
        }
        val tvRemoveUser = bottomSheetDialog!!.findViewById<TextView>(R.id.tvRemoveUser)
        tvRemoveUser!!.setOnClickListener {
            bottomSheetDialog!!.dismiss()
            troopClient.removeUserFromCall(participantUsers.user_id)
            /*if (callSocket != null && callSocket!!.connected()) {
                emitRemoveUser(contact.user_id.toString())
            } else {
                showToast(this,getString(R.string.Check_network_connection))
            }*/
        }
        val tvPinUser = bottomSheetDialog!!.findViewById<TextView>(R.id.tvPinUser)

        val videoEnableCount: Int = 0//videoAddedCount()
        tvPinUser!!.visibility = View.GONE
        /*tvPinUser.text = if (isPinned) "Unpin user" else "Pin user"
        val finalIsPinned = isPinned
        tvPinUser.setOnClickListener {
            bottomSheetDialog!!.dismiss()
            updateTheUserPin(finalIsPinned, contact.user_id.toString())
        }*/
        val tvRequestUnMuteAudio = bottomSheetDialog!!.findViewById<TextView>(R.id.tvRequestUnMuteAudio)
        tvRequestUnMuteAudio!!.text = if (isAudioMuted) "Request to unmute audio" else "Mute Audio"
        tvRequestUnMuteAudio.visibility = if (isAudioAdded) View.VISIBLE else View.GONE
        tvRequestUnMuteAudio.setOnClickListener {
            bottomSheetDialog!!.dismiss()
            /*if (callSocket != null && callSocket!!.connected()) {
                emitAudioVideoMuteReuest(1, contact.user_id.toString(), finalIsAudioMuted)
            } else {
                showToast(this,getString(R.string.Check_network_connection))
            }*/
            if (isAudioMuted) {
                troopClient.audioUnMuteRequest(participantUsers.user_id)
            }else {
                troopClient.muteUserAudio(participantUsers.user_id)
            }
        }
        val tvRequestUnMuteVideo = bottomSheetDialog!!.findViewById<TextView>(R.id.tvRequestUnMuteVideo)

        tvRequestUnMuteVideo!!.text =
            if (isVideoMuted) "Request to unmute video" else "Mute Video"
        tvRequestUnMuteVideo!!.visibility = if (isVideoAdded) View.VISIBLE else View.GONE
        val tvRetryUser = bottomSheetDialog!!.findViewById<TextView>(R.id.tvRetryUser)

        tvRequestUnMuteVideo!!.setOnClickListener {
            bottomSheetDialog!!.dismiss()
            if (isVideoMuted) {
                troopClient.videoUnMuteRequest(participantUsers.user_id)
            }else {
                troopClient.muteUserVideo(participantUsers.user_id)
            }
            /*if (callSocket != null && callSocket!!.connected()) {
                emitAudioVideoMuteReuest(2, contact.user_id.toString(), finalIsVideoMuted)
            } else {
                showToast(this,getString(R.string.Check_network_connection))
            }*/
        }
        bottomSheetDialog!!.findViewById<View>(R.id.tvCancle)!!.setOnClickListener { bottomSheetDialog!!.dismiss() }
        tvRetryUser!!.setOnClickListener {
            bottomSheetDialog!!.dismiss()
            troopClient.retryUser(participantUsers.user_id)
        //emitRetryCall(contact)
        }
        if (callstatus == 0) {
            tvRemoveUser.visibility = View.VISIBLE
            tvRetryUser.visibility = View.GONE
            tvRequestUnMuteAudio.visibility = View.GONE
            tvRequestUnMuteVideo.visibility = View.GONE
            tvPinUser.visibility = View.GONE
            tvRevokeHost.visibility = View.GONE
        } else if (callstatus > 1) {
            tvRemoveUser.visibility = View.VISIBLE
            if (isExternal == 0) {
                tvRetryUser.visibility = View.VISIBLE
            } else {
                tvRetryUser.visibility = View.GONE
            }
            tvRequestUnMuteAudio.visibility = View.GONE
            tvRequestUnMuteVideo.visibility = View.GONE
            tvPinUser.visibility = View.GONE
            tvRevokeHost.visibility = View.GONE
        } else {
            tvRemoveUser.visibility = View.VISIBLE
            tvRetryUser.visibility = View.GONE
        }

        bottomSheetDialog.show()
    }
}