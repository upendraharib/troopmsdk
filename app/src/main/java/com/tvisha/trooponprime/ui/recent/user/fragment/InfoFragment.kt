package com.tvisha.trooponprime.ui.recent.user.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.UserInfoLayoutBinding
import com.tvisha.trooponprime.lib.clientModels.UserClientModel
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper


class InfoFragment(var entityId:String,var entityUid:String):Fragment() {
    lateinit var dataBinding : UserInfoLayoutBinding
    var userData: UserClientModel? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBinding = DataBindingUtil.inflate(inflater,R.layout.user_info_layout,container,false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setView()
    }

    private fun setView() {
        //userData = troopClient.fetchUserProfileByUserId(entityId)

        dataBinding.swBlockUnBlock.setOnCheckedChangeListener { compoundButton, b ->
            Log.e("called===> ",""+b)
            if (b){
                troopClient.blockUser(entityId.toLong())
            }else{
                troopClient.unBlockUser(entityId.toLong())
            }
        }

        dataBinding.swBlockUnBlock.isChecked = troopClient.checkIsUserIsBlock(entityId.toLong())==1
        dataBinding.tvReportUser.setOnClickListener {
            dataBinding.clReportView.visibility = View.VISIBLE
            dataBinding.tvReportUser.visibility = View.GONE
        }
        dataBinding.done.setOnClickListener {
            troopClient.reportUser(entityId.toLong(),ConstantValues.Entity.USER,dataBinding.etReportData.text.toString())
            dataBinding.cancel.callOnClick()
        }
        dataBinding.cancel.setOnClickListener {
            dataBinding.etReportData.text.clear()
            dataBinding.clReportView.visibility = View.GONE
            dataBinding.tvReportUser.visibility = View.VISIBLE
        }
    }
    /*private fun openDialog(){
       var dialog = Dialog(requireActivity())
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(R.layout.report_user_layout)
        dialog!!.window?.attributes!!.width = ((Helper.getDeviceMetrics(requireActivity()).widthPixels.times(0.9)).toInt()!!)
    }*/
}