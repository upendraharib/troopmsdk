package com.tvisha.trooponprime.ui.recent.group.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.GroupInfoLayoutBinding
import com.tvisha.trooponprime.lib.clientModels.MessengerGroupModel
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper
import com.tvisha.trooponprime.ui.recent.group.GroupProfileActivity

class GroupInfoFragment(var groupId:String,var messengerGroupModel: MessengerGroupModel):Fragment() {
    lateinit var dataBinding :GroupInfoLayoutBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBinding = DataBindingUtil.inflate(inflater,R.layout.group_info_layout,container,false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setView()
    }
    private fun setView(){
        var userName = troopClient.fetchUserName(messengerGroupModel.created_by!!)
        var userAvatar = troopClient.fetchUserAvatar(messengerGroupModel.created_by!!)
        Glide.with(requireActivity()).load(userAvatar)
            .circleCrop()
            .placeholder(ContextCompat.getDrawable(requireActivity(),R.drawable.default_user))
            .error(ContextCompat.getDrawable(requireActivity(),R.drawable.default_user))
            .into(dataBinding.ivEntityProfile)
        dataBinding.tvEntityName.text = userName
        dataBinding.tvCreateTime.text = Helper.getRecentListMessagesDateTime(messengerGroupModel.created_at!!)

        dataBinding.tvDeleteGroup.visibility = if (GroupProfileActivity.SELF_USER_ROLE== ConstantValues.GroupValues.ADMIN.toInt()) View.VISIBLE else View.GONE

        dataBinding.tvDeleteGroup.setOnClickListener {
            (requireActivity() as GroupProfileActivity).confirmationDialog(1)
        }
        dataBinding.tvExitFromGroup.setOnClickListener {
            (requireActivity() as GroupProfileActivity).confirmationDialog(0)
        }
    }
}