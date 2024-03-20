package com.tvisha.trooponprime.ui.recent.login

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.tvisha.trooponprime.MyApplication
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.constants.Constants
import com.tvisha.trooponprime.databinding.LoginLayoutBinding
import com.tvisha.trooponprime.lib.utils.Helper
import com.tvisha.trooponprime.ui.recent.MainActivity

class LoginActivity:Activity() {
    lateinit var dataBinding:LoginLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.login_layout)
        if (!Constants.checkContactPermissions(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(Constants.CONTATCT_PERMISSION,Constants.CONTACT_PERMISSION_CALLBACK)
            }
        }
        setView()
    }
    fun setView(){
        dataBinding.buttonSubmit.setOnClickListener {
            if (dataBinding.editTextUid.text.toString().isNullOrEmpty()){
                dataBinding.textInputLayoutUid.error = "User id cannot be empty"
                return@setOnClickListener
            }
            if (dataBinding.editTextUid.text!!.toString().length<3){
                dataBinding.textInputLayoutUid.error = "User id is minimum 4 character"
                return@setOnClickListener
            }
            if (dataBinding.editTextMobile.text.toString().isNullOrEmpty()){
                dataBinding.textInputLayoutMobile.error = "Mobile number cannot be empty"
                return@setOnClickListener
            }
            if (!Helper.validMobileNumber(dataBinding.editTextMobile.text!!.toString())){
                dataBinding.textInputLayoutMobile.error = "Please enter mobile number"
                return@setOnClickListener
            }
            if (dataBinding.editTextName.text.toString().isNullOrEmpty()){
                dataBinding.textInputLayoutUserName.error = "Enter user name"
                return@setOnClickListener
            }
            if (dataBinding.editTextCountryCode.text.toString().isNullOrEmpty()){
                dataBinding.textInputLayoutCountryCode.error = "Enter country code"
                return@setOnClickListener
            }
            login()
        }
    }
    private fun login(){
        MyApplication.LOGINUSERUID = dataBinding.editTextUid.text.toString()
        val edit = MyApplication.appPreferences!!.edit()
        edit.putBoolean(Constants.SharedPreferenceConstant.LOGIN_APP_STATUS,true)
        edit.putString(Constants.SharedPreferenceConstant.UID,dataBinding.editTextUid.text.toString())
        edit.putString(Constants.SharedPreferenceConstant.MOBILE,dataBinding.editTextMobile.text.toString())
        edit.putString(Constants.SharedPreferenceConstant.COUNTRY_CODE,dataBinding.editTextCountryCode.text.toString())
        edit.putString(Constants.SharedPreferenceConstant.NAME,dataBinding.editTextName.text.toString())
        edit.apply()
        val app = application as MyApplication
        app.listenClient()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
}