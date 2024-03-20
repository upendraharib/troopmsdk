package com.tvisha.trooponprime.ui.recent.group

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.GroupProfileLayoutBinding
import com.tvisha.trooponprime.lib.clientModels.MessengerGroupModel
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.ui.recent.conversation.ChatActivity
import com.tvisha.trooponprime.ui.recent.fragment.MediaFragment
import com.tvisha.trooponprime.ui.recent.group.fragment.GroupInfoFragment
import com.tvisha.trooponprime.ui.recent.group.fragment.GroupMemberFragment
import com.tvisha.trooponprime.ui.recent.user.fragment.InfoFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class GroupProfileActivity:AppCompatActivity(), TabLayout.OnTabSelectedListener,
    ViewPager.OnPageChangeListener {
    var groupId:String = ""
    var fromChat:Boolean = false
    lateinit var dataBinding:GroupProfileLayoutBinding
    var groupData : MessengerGroupModel? = null
    companion object{
        var SELF_USER_ROLE : Int = 0
        var SELF_USER_STATUS : Int = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.group_profile_layout)
        groupId = intent.getStringExtra("entity_id")!!
        fromChat = intent.getBooleanExtra("fromChat",false)

        if (groupId.isNullOrEmpty()){
            finish()
        }
        setView()
    }
    private fun setView(){
        groupData = troopClient.fetchGroupDetails(groupId)
        troopClient.fetchSelfGroupProfile(groupId).observe(this, Observer {
            if (it!=null){
                SELF_USER_ROLE = it.member_role
                SELF_USER_STATUS = it.member_status
                /*if (SELF_USER_ROLE==ConstantValues.GroupValues.ADMIN.toInt()){
                    ivDeleteGroup.visibility = View.VISIBLE
                }else{
                    ivDeleteGroup.visibility = View.GONE
                }*/
                dataBinding.tvAddMember.visibility = if (SELF_USER_ROLE==ConstantValues.GroupValues.ADMIN.toInt() || SELF_USER_ROLE==ConstantValues.GroupValues.MODERATOR.toInt()) View.VISIBLE else View.GONE
                if (SELF_USER_ROLE==ConstantValues.GroupValues.ADMIN.toInt() || SELF_USER_ROLE==ConstantValues.GroupValues.MODERATOR.toInt()){
                    dataBinding.tvProfileName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.edit, 0)
                    dataBinding.ivGroupProfilePic.isClickable = true
                    dataBinding.ivGroupEditProfile.visibility = View.VISIBLE
                }else{
                    dataBinding.tvProfileName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    dataBinding.ivGroupProfilePic.isClickable = false
                    dataBinding.ivGroupEditProfile.visibility =View.GONE
                }
                Glide.with(this).load(groupData!!.group_avatar!!).circleCrop().error(ContextCompat.getDrawable(this,R.drawable.default_user)).into(dataBinding.ivGroupProfilePic)
            }
        })
        dataBinding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        dataBinding.tvProfileName.text = groupData!!.group_name!!
        //Glide.with(this).load(groupData!!.group_avatar!!).circleCrop().error(ContextCompat.getDrawable(this,R.drawable.default_user)).into(ivGroupProfilePic)
        dataBinding.tvProfileAbout.text ="About : "+ groupData!!.group_description!!
        /*tabLayout.getTabAt(0)!!.text = "Info"
        tabLayout.getTabAt(1)!!.text = "Media"
        tabLayout.getTabAt(2)!!.text = "Group Members"*/
        dataBinding.fabChat.setOnClickListener {
            if (fromChat){
                onBackPressedDispatcher.onBackPressed()
            }else{
                var intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("entity_name",groupData!!.group_name!!)
                intent.putExtra("entity_id", groupData!!.group_id.toString())
                intent.putExtra("entity_type", ConstantValues.Entity.GROUP)
                intent.putExtra("entity_uid", "0")
                startActivity(intent)
                finish()
            }
        }
        var adapter = SimpleFragmentPagerAdapter(this,supportFragmentManager)
        dataBinding.viewPager.adapter = adapter
        dataBinding.tabLayout.setupWithViewPager(dataBinding.viewPager)
        dataBinding.tabLayout.isSmoothScrollingEnabled = true
        dataBinding.tabLayout.setOnTabSelectedListener(this)
        dataBinding.viewPager.addOnPageChangeListener(this)
       /* ivExitGroup.setOnClickListener {
            confirmationDialog(0)
        }
        ivDeleteGroup.setOnClickListener {
            confirmationDialog(1)
        }*/
        dataBinding.tvAddMember.setOnClickListener {
            var intent = Intent(this,AddGroupMemberActivity::class.java)
            intent.putExtra("group_id",groupId)
            startActivity(intent)
        }
        dataBinding.tvProfileName.setOnClickListener {
            groupNameUpdateDialog()
        }
        dataBinding.ivGroupEditProfile.setOnClickListener {
            updateGroupAvatar("https://cdn2.vectorstock.com/i/1000x1000/26/66/profile-icon-member-society-group-avatar-vector-18572666.jpg")
        }
    }
    fun confirmationDialog(i: Int) {
        AlertDialog.Builder(this)
            .setTitle(if (i==0) "Exit From Group" else "Delete Group")
            .setMessage("Are you sure you want to "+if (i==0) "exit from group " else "delete group")
            .setIcon(if (i==0) R.drawable.log_out else R.drawable.delete_red)
            .setPositiveButton(android.R.string.yes,
                DialogInterface.OnClickListener { dialog, whichButton ->
                    exitOrDeleteGroup(i)
                    dialog.dismiss()
                })
            .setNegativeButton(android.R.string.no, DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
            }).show()
    }
    private fun groupNameUpdateDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update group name")
        val input = EditText(this)
        input.setText(groupData!!.group_name)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setPositiveButton(
            "OK"
        ) { dialog, which ->
            var m_Text = input.text.toString()
            if (m_Text.trim().isNotEmpty() && m_Text.trim()!=groupData!!.group_name){
                dialog.dismiss()
                updateGroupName(m_Text)
            }
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }

        builder.show()
    }
    private fun updateGroupName(newName:String){
        GlobalScope.launch {
            var response = troopClient.updateGroupName(groupId,newName)
            Log.e("response==> ",response.toString())
        }

    }
    private fun updateGroupAvatar(groupAvatar:String){
        GlobalScope.launch {
            var response = troopClient.updateGroupAvatar(groupId,groupAvatar)
            Log.e("response==> ",response.toString())
        }
    }
    private fun exitOrDeleteGroup(i: Int) {
        if (i==0){
            GlobalScope.launch {
                var response = troopClient.exitGroup(groupId)
                if (response.optBoolean("success")){
                    finish()
                }
            }
        }else if (i==1){
            GlobalScope.launch {
                var response = troopClient.deleteGroup(groupId)
                if (response.optBoolean("success")){
                    finish()
                }
            }
        }
    }

    private val tabTitles = arrayOf("Info", "Media", "Group Members")
    inner class SimpleFragmentPagerAdapter(private val mContext: Context, fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!) {
        var fragment: Fragment? = null
        override fun getPageTitle(position: Int): CharSequence? {
            return tabTitles[position]
        }
        // This determines the fragment for each tab
        override fun getItem(position: Int): Fragment {
            return if (position == 0) {
                fragment = GroupInfoFragment(groupId,groupData!!)
                fragment!!
            } else if (position == 1) {
                fragment = MediaFragment(groupId,"",ConstantValues.Entity.GROUP,groupData!!.group_name!!)
                fragment!!
            }else if (position==2){
                fragment = GroupMemberFragment(groupId)
                fragment!!
            }
            else {
                fragment = InfoFragment(groupId,"")
                fragment!!
            }
        }

        // This determines the number of tabs
        override fun getCount(): Int {
            return 3
        }

    }

    override fun onTabSelected(tab: TabLayout.Tab?) {

    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
    }
}