package com.tvisha.trooponprime.ui.recent.group.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.GroupMemberLayoutBinding
import com.tvisha.trooponprime.lib.database.model.GroupMembersModel
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.ui.recent.conversation.ChatActivity
import com.tvisha.trooponprime.ui.recent.group.GroupProfileActivity.Companion.SELF_USER_ROLE
import com.tvisha.trooponprime.ui.recent.group.GroupProfileActivity.Companion.SELF_USER_STATUS
import com.tvisha.trooponprime.ui.recent.group.adapter.GroupMemberAdapter
import com.tvisha.trooponprime.ui.recent.user.SelfUserProfileActivity
import com.tvisha.trooponprime.ui.recent.user.UserProfileActivity

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GroupMemberFragment(var groupId:String):Fragment() {
    lateinit var dataBinding: GroupMemberLayoutBinding
    var adapter : GroupMemberAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBinding  = DataBindingUtil.inflate(inflater,R.layout.group_member_layout,container,false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.rcvGroupMemberList.layoutManager = LinearLayoutManager(requireActivity())
        adapter = GroupMemberAdapter(requireActivity(),handler)
        dataBinding.rcvGroupMemberList.adapter = adapter
        setList()
    }
    private val handler = @SuppressLint("HandlerLeak")
    object :Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1->{
                    openBottomSheet(msg.obj as GroupMembersModel)
                }
            }
        }
    }
    private fun openBottomSheet(groupMembersModel: GroupMembersModel){
        if (troopClient.tmUserId()==groupMembersModel.member_id.toString()){
            return
        }
        var isBlockedUser= troopClient.checkIsUserIsBlock(groupMembersModel.member_id)
        if (isBlockedUser==1 && SELF_USER_STATUS!=ConstantValues.ACTIVE){
            return
        }

        var bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialogTheme)
        val inflater = requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val contentView = inflater.inflate(R.layout.group_member_options_layout, null)
        contentView.setBackgroundColor(ContextCompat.getColor(requireActivity(),android.R.color.transparent))
        bottomSheetDialog.setContentView(contentView)
        var tvOpenChat : TextView? = bottomSheetDialog!!.findViewById(R.id.tvOpenChat)
        var chatView : View? = bottomSheetDialog!!.findViewById(R.id.chatView)
        var tvOpenProfile : TextView? = bottomSheetDialog!!.findViewById(R.id.tvOpenProfile)
        var profileView : View? = bottomSheetDialog!!.findViewById(R.id.profileView)
        var tvMakeOrRemoveAdmin : TextView? = bottomSheetDialog!!.findViewById(R.id.tvMakeOrRemoveAdmin)
        var adminView : View? = bottomSheetDialog!!.findViewById(R.id.adminView)
        var tvCancel : TextView? = bottomSheetDialog!!.findViewById(R.id.tvCancel)
        var tvMakeOrRemoveModerator : TextView? = bottomSheetDialog!!.findViewById(R.id.tvMakeOrRemoveModerator)
        var moderatorView : View? = bottomSheetDialog!!.findViewById(R.id.moderatorView)
        var tvRemoveUser : TextView? = bottomSheetDialog!!.findViewById(R.id.tvRemoveUser)
        var tvUserName : TextView? = bottomSheetDialog!!.findViewById(R.id.tvUserName)
        tvUserName!!.text = if (groupMembersModel!!.member_name!=null) groupMembersModel.member_name else ""
        /*if (isBlockedUser==1){
            tvOpenChat!!.visibility = View.GONE
            chatView!!.visibility = View.GONE
            tvOpenProfile!!.visibility= View.GONE
            profileView!!.visibility= View.GONE
        }*/

        tvMakeOrRemoveAdmin!!.visibility = View.GONE
        adminView!!.visibility = View.GONE
        tvMakeOrRemoveModerator!!.visibility = View.GONE
        moderatorView!!.visibility = View.GONE
        tvRemoveUser!!.visibility = View.GONE
        if (SELF_USER_STATUS!=ConstantValues.ACTIVE){
            tvMakeOrRemoveAdmin!!.visibility = View.GONE
            adminView!!.visibility = View.GONE
            tvMakeOrRemoveModerator!!.visibility = View.GONE
            moderatorView!!.visibility = View.GONE
            tvRemoveUser!!.visibility = View.GONE
        }else if (SELF_USER_ROLE==ConstantValues.GroupValues.ADMIN.toInt()){
            tvMakeOrRemoveAdmin!!.visibility = View.VISIBLE
            adminView!!.visibility = View.VISIBLE
            tvMakeOrRemoveModerator!!.visibility = View.VISIBLE
            moderatorView!!.visibility = View.VISIBLE
            tvRemoveUser!!.visibility = View.VISIBLE
        }
        tvMakeOrRemoveAdmin!!.text  = if (groupMembersModel.member_role==ConstantValues.GroupValues.ADMIN.toInt()) "Remove as a Group Admin " else "Make as Group Admin"
        tvMakeOrRemoveModerator!!.text  = if (groupMembersModel.member_role==ConstantValues.GroupValues.MODERATOR.toInt()) "Remove as a Group Moderator " else "Make as Group Moderator"

        tvMakeOrRemoveAdmin!!.setOnClickListener {
            bottomSheetDialog.dismiss()
            GlobalScope.launch {
                if (groupMembersModel.member_role==ConstantValues.GroupValues.ADMIN.toInt()){
                    var response = troopClient.removeGroupAdmin(groupId,groupMembersModel.member_id.toString())
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireActivity(),response.optString("message"),Toast.LENGTH_SHORT).show()
                    }
                }else{
                    var response = troopClient.makeGroupAdmin(groupId,groupMembersModel.member_id.toString())
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireActivity(),response.optString("message"),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        tvMakeOrRemoveModerator!!.setOnClickListener {
            bottomSheetDialog.dismiss()
            GlobalScope.launch {
                if (groupMembersModel.member_role==ConstantValues.GroupValues.MODERATOR.toInt()){
                    var response =troopClient.removeGroupModerator(groupId,groupMembersModel.member_id.toString())
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireActivity(),response.optString("message"),Toast.LENGTH_SHORT).show()
                    }
                }else{
                    var response =troopClient.makeGroupModerator(groupId,groupMembersModel.member_id.toString())
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireActivity(),response.optString("message"),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        tvRemoveUser!!.setOnClickListener {
            bottomSheetDialog.dismiss()
            GlobalScope.launch {
                var response =troopClient.removeGroupUser(groupId,groupMembersModel.member_id.toString())
                requireActivity().runOnUiThread {
                    Toast.makeText(requireActivity(),response.optString("message"),Toast.LENGTH_SHORT).show()
                }
            }
        }
        tvOpenChat!!.setOnClickListener {
            bottomSheetDialog.dismiss()
            var intent = Intent(requireActivity(), ChatActivity::class.java)
            intent.putExtra("entity_name",groupMembersModel.member_name)
            intent.putExtra("entity_id", groupMembersModel.member_id.toString())
            intent.putExtra("entity_type", ConstantValues.Entity.USER)
            intent.putExtra("entity_uid", groupMembersModel.member_uid)
            startActivity(intent)
        }
        tvOpenProfile!!.setOnClickListener {
            bottomSheetDialog.dismiss()
            var intent = requireActivity().intent
            if (troopClient.tmUserId()==groupMembersModel.member_id.toString()) {
                intent = Intent(requireActivity(), SelfUserProfileActivity::class.java)
            }else{
                intent = Intent(requireActivity(), UserProfileActivity::class.java)
            }
            intent.putExtra("entity_id",groupMembersModel.member_id.toString())
            intent.putExtra("entity_uid",groupMembersModel.member_uid)
            intent.putExtra("fromChat",false)
            startActivity(intent)
        }
        tvCancel!!.setOnClickListener {
            bottomSheetDialog!!.dismiss()
        }

        bottomSheetDialog.show()
    }
    fun setList(){
        var loginUserid = troopClient.tmUserId()
        troopClient.fetchGroupMembers(groupId).observe(requireActivity(), Observer {
            if (it.isNotEmpty()){
                adapter!!.setList(it as ArrayList<GroupMembersModel>)
            }
        })
    }
}