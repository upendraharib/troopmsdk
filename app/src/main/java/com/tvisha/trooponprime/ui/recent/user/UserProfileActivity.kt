package com.tvisha.trooponprime.ui.recent.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.databinding.UserProfileInfoLayoutBinding
import com.tvisha.trooponprime.lib.clientModels.UserClientModel
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.ui.recent.conversation.ChatActivity
import com.tvisha.trooponprime.ui.recent.fragment.MediaFragment
import com.tvisha.trooponprime.ui.recent.user.fragment.CommonGroupFragment
import com.tvisha.trooponprime.ui.recent.user.fragment.InfoFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class UserProfileActivity:AppCompatActivity(), TabLayout.OnTabSelectedListener,
    ViewPager.OnPageChangeListener {
    var userId:String = ""
    var userUid:String = ""
    var selfUserData : UserClientModel? = null
    var fromChat:Boolean = false
    lateinit var dataBinding:UserProfileInfoLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.user_profile_info_layout)
        userId = intent.getStringExtra("entity_id")!!
        userUid = intent.getStringExtra("entity_uid")!!
        fromChat = intent.getBooleanExtra("fromChat",false)

        if (userId.isNullOrEmpty()){
            finish()
        }
        setView()
    }
    private fun setView(){
        selfUserData = troopClient.fetchUserProfileByUserId(userId)
        dataBinding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        dataBinding.ivEditProfile.visibility = View.GONE
        dataBinding.tvProfileName.text = selfUserData!!.entity_name
        showProfile()
        dataBinding.tvProfileAbout.text = "About : "+ selfUserData!!.about!!
        dataBinding.tvProfileName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        dataBinding.fabChat.setOnClickListener {
            if (fromChat){
                onBackPressedDispatcher.onBackPressed()
            }else{
                var intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("entity_name",selfUserData!!.entity_name)
                intent.putExtra("entity_id", selfUserData!!.entity_id.toString())
                intent.putExtra("entity_type", ConstantValues.Entity.USER)
                intent.putExtra("entity_uid", selfUserData!!.uid)
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
    }
    private fun showProfile(){
        Glide.with(this).load(selfUserData!!.entity_avatar).circleCrop().error(ContextCompat.getDrawable(this,R.drawable.default_user)).into(dataBinding.ivProfilePic)
    }
    private val tabTitles = arrayOf("Info", "Media")
    inner class SimpleFragmentPagerAdapter(private val mContext: Context, fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!) {
        var fragment: Fragment? = null
        override fun getPageTitle(position: Int): CharSequence? {
            return tabTitles[position]
        }
        // This determines the fragment for each tab
        override fun getItem(position: Int): Fragment {
            return if (position == 0) {
                fragment = InfoFragment(userId,userUid)
                fragment!!
            } else if (position == 1) {
                fragment = MediaFragment(userId,userUid,ConstantValues.Entity.USER,selfUserData!!.entity_name!!)
                fragment!!
            }/*else if (position==2){
                fragment = CommonGroupFragment(userId,userUid)
                fragment!!
            }*/
            else {
                fragment = InfoFragment(userId,userUid)
                fragment!!
            }
        }

        // This determines the number of tabs
        override fun getCount(): Int {
           return 2
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