package com.tvisha.trooponprime.ui.recent.group

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.ui.recent.group.adapter.GroupPicUserAdapter
import com.tvisha.trooponprime.constants.Constants
import com.tvisha.trooponprime.databinding.NewCreateGroupLayoutBinding
import com.tvisha.trooponprime.lib.clientModels.GroupCreateModel
import com.tvisha.trooponprime.lib.clientModels.CreateGroupMembers
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class CreateGroupActivity:AppCompatActivity() {
    var adapter : GroupPicUserAdapter? = null
    lateinit var dataBinding : NewCreateGroupLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.new_create_group_layout)
        setView()
    }
    private fun setView(){
        dataBinding.userList.layoutManager = GridLayoutManager(this,4)
        adapter = GroupPicUserAdapter(this)
        dataBinding.userList.adapter = adapter
        adapter!!.setList(CreateGroupPicUserActivity.selectedUserList)

        dataBinding.tvNext.setOnClickListener {
            if (dataBinding.etGroupName.text.toString().trim().isNullOrEmpty()){
                Toast.makeText(this,"Group name is required",Toast.LENGTH_SHORT).show()
            }
            createGroup()
        }
        dataBinding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        dataBinding.ivPicGroup.setOnClickListener {
            openBottomSheet()
        }
    }
    private fun openBottomSheet(){
        var bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val contentView = inflater.inflate(R.layout.attachment_picker_layout, null)
        contentView.setBackgroundColor(ContextCompat.getColor(this,android.R.color.transparent))
        bottomSheetDialog.setContentView(contentView)


        var tvOpenCamera : TextView? = bottomSheetDialog!!.findViewById(R.id.tvOpenCamera)
        var tvOpenGallery : TextView? = bottomSheetDialog!!.findViewById(R.id.tvOpenGallery)
        var tvOpenDoc : TextView? = bottomSheetDialog!!.findViewById(R.id.tvOpenDoc)
        var tvCancel : TextView? = bottomSheetDialog!!.findViewById(R.id.tvCancel)

        tvCancel!!.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        tvOpenCamera!!.setOnClickListener {
            bottomSheetDialog.dismiss()
            if (Constants.checkCameraStoragePermissions(this)){
                openCamera()
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(Constants.CAMERA_PERMISSION,Constants.CAMERA_PERMISSION_CALLBACK)
                }
            }
        }
        tvOpenGallery!!.setOnClickListener {
            bottomSheetDialog.dismiss()
            if (Constants.checkStoragePermissions(this)){
                openGallery()
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(Constants.STORAGE_PERMISSION,Constants.STORAGE_PERMISSION_CALLBACK)
                }
            }
        }
        tvOpenDoc!!.setOnClickListener {
            bottomSheetDialog.dismiss()
            if (Constants.checkStoragePermissions(this)){
                openDoc()
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(Constants.STORAGE_PERMISSION,Constants.STORAGE_PERMISSION_CALLBACK)
                }
            }
        }

        bottomSheetDialog.show()

    }
    var currentImageUri: Uri? = null
    private fun openCamera(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        currentImageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        resultLauncher.launch(cameraIntent)
    }
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Uri? = result.data!!.data
            if (data!=null){
                Log.e("data===> ", " from gallery $data")
                var path =  Helper.getRealPathFromURI(this,data)
                Log.e("data===> ", " from gallery $path")
                Glide.with(this).load(path).placeholder(ContextCompat.getDrawable(this,
                    R.drawable.default_user
                )).error(R.drawable.default_user).into(dataBinding.ivPicGroup)
            }else if (currentImageUri!=null){
                //val bitmap = BitmapFactory.decodeStream(contentResolver?.openInputStream(currentImageUri!!))
                Log.e("data===> ", " from gallery "+(currentImageUri!=null)+"   "+ File(currentImageUri!!.path).path)
                Glide.with(this).load(currentImageUri).placeholder(ContextCompat.getDrawable(this,
                    R.drawable.default_user
                )).error(R.drawable.default_user).into(dataBinding.ivPicGroup)
            }

            //doSomeOperations()
        }
    }

    private fun openGallery(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }
    private fun openDoc(){
        val intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }
    private fun createGroup(){
        var listOfGroupMembers : ArrayList<CreateGroupMembers> = ArrayList()
        for (i in 0 until CreateGroupPicUserActivity.selectedUserList.size){
            var groupMembersModel = CreateGroupMembers()
            groupMembersModel.user_id = CreateGroupPicUserActivity.selectedUserList[i].entity_id
            groupMembersModel.name = CreateGroupPicUserActivity.selectedUserList[i].entity_name!!
            groupMembersModel.role = ConstantValues.GroupValues.MEMBER.toInt()
            groupMembersModel.status = ConstantValues.ACTIVE
            listOfGroupMembers.add(groupMembersModel)
        }
        var groupCreateModel = GroupCreateModel(dataBinding.etGroupName.text.toString(),"",dataBinding.etGroupDescription.text.toString(),listOfGroupMembers,"")
        Log.e("response===> ",""+Gson().toJson(groupCreateModel))
        GlobalScope.launch {
            var response = troopClient.createGroup(groupCreateModel)
            Log.e("response===> ",""+response.toString())
            if (response.optBoolean("success")){
                setResult(Activity.RESULT_OK)
                finish()
            }
        }

    }

}