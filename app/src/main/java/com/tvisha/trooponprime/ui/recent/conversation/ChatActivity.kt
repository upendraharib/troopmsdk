package com.tvisha.trooponprime.ui.recent.conversation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tvisha.troopim.activity.chat.singleChat.MessageSwipe.MessageSwipeController
import com.tvisha.troopim.activity.chat.singleChat.MessageSwipe.SwipeControllerActions
import com.tvisha.trooponprime.MyApplication
import com.tvisha.trooponprime.MyApplication.Companion.LOGINUSERUID
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.calls.CallPreviewActivity
import com.tvisha.trooponprime.constants.Constants
import com.tvisha.trooponprime.databinding.ChatActivityBinding
import com.tvisha.trooponprime.lib.clientModels.*
import com.tvisha.trooponprime.lib.listeneres.CallBackListener
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper
import com.tvisha.trooponprime.ui.recent.contacts.ContactPicActivity
import com.tvisha.trooponprime.ui.recent.conversation.adapter.ConversationAdapter
import com.tvisha.trooponprime.ui.recent.forkout.ForkOutActivity
import com.tvisha.trooponprime.ui.recent.forward.ForwardUserListActivity
import com.tvisha.trooponprime.ui.recent.group.GroupProfileActivity
import com.tvisha.trooponprime.ui.recent.location.LocationPickerActivity
import com.tvisha.trooponprime.ui.recent.preview.AttachmentViewActivity
import com.tvisha.trooponprime.ui.recent.user.SelfUserProfileActivity
import com.tvisha.trooponprime.ui.recent.user.UserProfileActivity
import com.tvisha.trooponprime.ui.recent.viewmodel.RecentChatViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject


class ChatActivity :AppCompatActivity(), MyApplication.ClientListeners {
    var adapter: ConversationAdapter? = null
    var entityId:String=""
    var entityUid:String = ""
    var entityName:String= ""
    var entityType:Int = 0
    var isRoom : Int = 0
    var viewModel: RecentChatViewModel? = null
    var mTyping: Boolean = true
    private val mTypingHandler = Handler()
    private val TYPING_TIMER_LENGTH = 3000
    var popupWindow: PopupWindow? = null
    var selectedReplyMessage:ConversationModel? = null
    var entityContact : UserClientModel? = null
    var groupData : MessengerGroupModel? = null
    var profilePic :String = ""
    var forkOutData:JSONArray = JSONArray()
    var userStatusText: String = ""
    lateinit var dataBinding:ChatActivityBinding
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getIntent().replaceExtras(intent!!)
        recreate()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.chat_activity)
        viewModel = ViewModelProviders.of(this)[RecentChatViewModel::class.java]

        entityId = intent.getStringExtra("entity_id")!!
        entityUid = intent.getStringExtra("entity_uid")!!
        entityType = intent.getIntExtra("entity_type",0)
        entityName = intent.getStringExtra("entity_name")!!
        Log.e("data===> ", "$entityId   $entityUid   $entityType   $entityName")
        setView()
    }
    private fun setView(){
        if (entityType==ConstantValues.Entity.USER){
            entityContact = troopClient.fetchUserProfileByUserId(entityId)
            profilePic = entityContact!!.entity_avatar!!
            dataBinding.ivEntity.visibility = View.GONE
            userStatusText = "offline"
        }else {
             groupData = troopClient.fetchGroupDetails(entityId)
            profilePic = groupData!!.group_avatar!!
            dataBinding.ivEntity.visibility = View.VISIBLE
            var count  = troopClient.fetchGroupActiveMemberCount(entityId.toLong()).size
            userStatusText = if (count<=1) "$count member" else "$count members"
        }
        dataBinding.tvUserStatus.text = userStatusText
        dataBinding.tvEntityName.text = entityName
        showProfile()

        dataBinding.conversationList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true)
        adapter = ConversationAdapter(this,adapterHandler)
        dataBinding.conversationList.itemAnimator = null
        dataBinding.conversationList.adapter = adapter
        val messageSwipeController = MessageSwipeController(this, object : SwipeControllerActions {
            override fun showReplyUI(position: Int) {
                setReplyMessageData(adapter!!.snapshot().items[position])
            }
        })
        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper.attachToRecyclerView(dataBinding.conversationList)
        lifecycleScope.launch(Dispatchers.IO){
            troopClient.fetchConversation(entityId,entityType,viewModel!!).collectLatest {
                if (it!=null){
                   // Log.e("data---> ",""+Gson().toJson(it))
                    withContext(Dispatchers.Main){
                        adapter!!.submitData(it)
                        adapter!!.refresh()

                        dataBinding.conversationList.itemAnimator= null
                    }
                }
            }
        }
        lifecycleScope.launch {
            adapter!!.loadStateFlow.distinctUntilChangedBy {
                it.refresh
            }.collectLatest {
                var list = adapter!!.snapshot().items
            }
        }
        adapter!!.addLoadStateListener { combinedLoadStates ->
            var list = adapter!!.snapshot().items
            if (combinedLoadStates.append.endOfPaginationReached) {
                //loadMoreMessagesFromServer
            }
        }
        dataBinding.etMessage.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.isNotEmpty()) {
                    setTyping(1)
                } else {
                    setTyping(0)
                }
                mTyping = true
                mTypingHandler.removeCallbacks(onTypingTimeout)
                mTypingHandler.postDelayed(
                    onTypingTimeout,
                    TYPING_TIMER_LENGTH.toLong()
                )
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        dataBinding.ivSend.setOnClickListener {
            if (!dataBinding.etMessage.text.toString().trim().isNullOrEmpty()){
                if (forkOutData!=null && forkOutData.length()>0){
                    var messageDataList : ArrayList<MessageData> = ArrayList()
                    for (i in 0 until forkOutData.length()){
                        var messageData = MessageData()
                        messageData.message = dataBinding.etMessage.text.toString()
                        messageData.is_group = forkOutData.optJSONObject(i).optInt("entity")-1
                        messageData.receiver_id = forkOutData.optJSONObject(i).optString("receiver_id")
                        messageData.is_room = 0
                        messageData.receiver_uid = forkOutData.optJSONObject(i).optString("entity_uid")
                        messageData.sender_uid = LOGINUSERUID
                        messageDataList.add(messageData)
                    }
                    if (messageDataList.size>0){
                        troopClient.sendForkOutTextMessages(messageDataList)
                        dataBinding.ivForkOut.callOnClick()
                    }
                }else
                if (dataBinding.replyMessageHolder!=null && dataBinding.replyMessageHolder.visibility==View.VISIBLE){
                    var messageData = MessageData()
                    messageData.message = dataBinding.etMessage.text.toString()
                    messageData.is_group = entityType-1
                    messageData.receiver_id = entityId
                    messageData.is_room = 0
                    messageData.receiver_uid = entityUid
                    messageData.sender_uid = LOGINUSERUID
                    messageData.message_id = selectedReplyMessage!!.message_id
                    troopClient.sendReplyTextMessage(messageData)

                }else {
                    var messageData = MessageData()
                    messageData.message = dataBinding.etMessage.text.toString()
                    messageData.is_group = entityType - 1
                    messageData.receiver_id = entityId
                    messageData.is_room = 0
                    messageData.receiver_uid = entityUid
                    messageData.sender_uid = LOGINUSERUID
                    troopClient.sendTextMessage(messageData)
                }
                clearText()
                dataBinding.replyItemClose.callOnClick()
            }

        }
        dataBinding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        dataBinding.clHeadBar.setOnClickListener {
            var intent = intent
            if (entityType==ConstantValues.Entity.USER) {
                if (troopClient.tmUserId()==entityId) {
                    intent = Intent(this,SelfUserProfileActivity::class.java)
                }else{
                    intent = Intent(this,UserProfileActivity::class.java)
                }
            }else if (entityType==ConstantValues.Entity.GROUP){
                intent = Intent(this,GroupProfileActivity::class.java)
            }
            intent.putExtra("entity_id",entityId)
            intent.putExtra("entity_uid",entityUid)
            intent.putExtra("fromChat",true)
            startActivity(intent)
        }
        dataBinding.ivAttachment.setOnClickListener {
            openBottomSheet()
        }
        dataBinding.ivForkOut.setOnClickListener {
            if (forkOutData!=null && forkOutData.length()>0){
                forkOutData = JSONArray()
                dataBinding.ivForkOut.setImageResource(R.drawable.forkout_in_active)
            }else {
                openForkOut()
            }
        }
        dataBinding.ivAudioCall.setOnClickListener {
            if (Constants.checkCallPermission(this)) {
                var userids: ArrayList<String> = ArrayList()
                userids.add(LOGINUSERUID)
                userids.add(entityUid)
                troopClient.initiateCall(userids, ConstantValues.NewCallTypes.AUDIO_CALL,object :CallBackListener{
                    override fun onFailure(jsonObjet: JSONObject) {
                        runOnUiThread {
                            Toast.makeText(this@ChatActivity,jsonObjet.optString("message"),Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onSuccess(jsonObjet: JSONObject) {

                        if (jsonObjet.has("call_id")){
                            openCallPreview(jsonObjet)
                        }
                    }
                })
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(Constants.CALL_PERMISSION_LIST,Constants.CALL_PERMISSION_CALLBACK)
                }
            }
        }
        dataBinding.ivVideoCall.setOnClickListener {
            if (Constants.checkCallPermission(this)) {
                var userids: ArrayList<String> = ArrayList()
                userids.add(LOGINUSERUID)
                userids.add(entityUid)
                troopClient.initiateCall(userids, ConstantValues.NewCallTypes.VIDEO_CALL,object :CallBackListener{
                    override fun onFailure(jsonObjet: JSONObject) {
                        runOnUiThread {
                            Toast.makeText(this@ChatActivity,jsonObjet.optString("message"),Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onSuccess(jsonObjet: JSONObject) {

                        if (jsonObjet.has("call_id")){
                            openCallPreview(jsonObjet)
                        }
                    }
                })
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(Constants.CALL_PERMISSION_LIST,Constants.CALL_PERMISSION_CALLBACK)
                }
            }
        }
    }
    var callBack = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1->{
                    var callBackObject = msg.obj as JSONObject

                    if (callBackObject.has("call_id")){
                        openCallPreview(callBackObject)
                    }
                }
                0->{
                    var errorObject = msg.obj as JSONObject
                    runOnUiThread {
                        Toast.makeText(this@ChatActivity,errorObject.optString("message"),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun openCallPreview(jsonObject: JSONObject){
        var intent = Intent(this,CallPreviewActivity::class.java)
        intent.putExtra("call_id",jsonObject.optString("call_id"))
        intent.putExtra("participants",jsonObject.optString("participants"))
        intent.putExtra("call_type",jsonObject.optInt("call_type"))
        intent.putExtra("isMineCall",true)
        intent.putExtra("initiator_id",jsonObject.optString("initiator_id"))
        startActivity(intent)
    }
    private fun showProfile() {
        Glide.with(this).load(profilePic).circleCrop().placeholder(ContextCompat.getDrawable(this,if (entityType==ConstantValues.Entity.USER) R.drawable.default_user else R.drawable.default_group_pic)).into(dataBinding.ivEntityPic)
    }

    private fun setReplyMessageData(conversationModel: ConversationModel) {
        if (forkOutData!=null && forkOutData.length()>0){
            dataBinding.ivForkOut.callOnClick()
        }
        selectedReplyMessage = conversationModel
        dataBinding.replyMessageHolder.visibility = View.VISIBLE
        dataBinding.replyMessage.text = if (conversationModel.message!=null) conversationModel.message!! else  if (conversationModel.message_type==ConstantValues.MessageTypes.ATTACHMENT) "Attachment" else if (conversationModel.message_type==ConstantValues.MessageTypes.AUDIO_MESSAGE) "Audio" else if (conversationModel.message_type==ConstantValues.MessageTypes.CONTACT_MESSAGE) "contact" else if (conversationModel.message_type==ConstantValues.MessageTypes.LOCATION_MESSAGE) "location" else if (conversationModel.message_type==ConstantValues.MessageTypes.AUDIO_MESSAGE) "Audio" else ""
        if (conversationModel.message_type==ConstantValues.MessageTypes.ATTACHMENT) {
            Glide.with(this).load(conversationModel.attachment).into(dataBinding.replyItemMessage)
        }
        dataBinding.replyItemClose.setOnClickListener {
            dataBinding.replyMessageHolder.visibility = View.GONE
            selectedReplyMessage = null
        }

    }

    private val adapterHandler = @SuppressLint("HandlerLeak")
    object :Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1->{
                    checkAndShowOptions()
                }
                2->{
                    openPreviewPage(msg.obj as ConversationModel)
                }
            }
        }
    }
    fun openPreviewPage(conversationModel: ConversationModel){
        var intent = Intent(this, AttachmentViewActivity::class.java)
        intent.putExtra("entity_id",entityId)
        intent.putExtra("entity_name",entityName)
        intent.putExtra("entity_type",entityType)
        intent.putExtra("path",conversationModel.attachment)
        intent.putExtra("sender_name",conversationModel.member_name)
        intent.putExtra("created_at",conversationModel.created_at)
        startActivity(intent)
    }

    private fun checkAndShowOptions() {
        var selectedMessages = adapter!!.getSelectedMessages()
        if (selectedMessages.size>0){
            dataBinding.ivMoreOptions.visibility = View.VISIBLE
            dataBinding.ivAudioCall.visibility = View.GONE
            dataBinding.ivVideoCall.visibility = View.GONE
        }else{
            dataBinding.ivMoreOptions.visibility = View.GONE
            dataBinding.ivAudioCall.visibility = View.VISIBLE
            dataBinding.ivVideoCall.visibility = View.VISIBLE
        }
        dataBinding.ivMoreOptions.setOnClickListener {
            showMoreOptionWindow(dataBinding.ivMoreOptions,selectedMessages)
        }
    }

    private fun showMoreOptionWindow(v: ImageView?, selectedSize: ArrayList<ConversationModel>) {
        popupWindow = PopupWindow(this@ChatActivity)
        val layout: View = layoutInflater.inflate(R.layout.moreoptions, null)
        popupWindow!!.contentView = layout
        popupWindow!!.height = ListPopupWindow.WRAP_CONTENT
        popupWindow!!.width = ListPopupWindow.WRAP_CONTENT
        // Closes the popup window when touch outside of it - when looses focus
        popupWindow!!.isOutsideTouchable = true
        //popupWindow!!.isFocusable = true
        popupWindow!!.update()
        // Show anchored to button
        popupWindow!!.setBackgroundDrawable(BitmapDrawable())
        popupWindow!!.showAsDropDown(v)

        val tvCopy = layout.findViewById<TextView>(R.id.tvCopy)
        val tvRecall = layout.findViewById<TextView>(R.id.tvRecall)
        val tvDelete = layout.findViewById<TextView>(R.id.tvDelete)
        val tvForward = layout.findViewById<TextView>(R.id.tvForward)
        val tvShare = layout.findViewById<TextView>(R.id.tvShare)
        val tvLike = layout.findViewById<TextView>(R.id.tvLike)
        val tvReport = layout.findViewById<TextView>(R.id.tvReport)
        val tvEdit = layout.findViewById<TextView>(R.id.tvEdit)

        var isTextMessage = adapter!!.checkIsTextMessage()
        tvCopy.alpha = if (isTextMessage) 1f else 0.5f
        var isRecallMessage = adapter!!.isRecallMessage()
        tvRecall.alpha = if (isRecallMessage) 1f else 0.5f
        tvEdit.alpha = if (isRecallMessage && isTextMessage && selectedSize.size==1) 1f else 0.5f
        var isForwardMessage = adapter!!.isForwardMessage()
        tvForward.alpha  = if (isForwardMessage) 1f else 0.5f
        tvShare.alpha = if (selectedSize.size>1) 0.5f else 1f
        tvLike.alpha = if (selectedSize.size==1) 1f else 0.5f
        tvReport.alpha = if (selectedSize.size==1) 1f else 0.5f
        if (tvLike.alpha>0.5f){
            tvLike.text = if (selectedSize[0].is_like==1) "UnLike" else "Like"
        }
        tvCopy.setOnClickListener {
            if (tvCopy.alpha>0.5f) {
                popupWindow!!.dismiss()
                var localIds: ArrayList<Long> = ArrayList()
                var size = selectedSize.size
                for (i in 0 until size) {
                    localIds.add(selectedSize[i].ID)
                }
                troopClient.fetchCopiedMessages(localIds)
            }
        }
        tvRecall.setOnClickListener {
            if (tvRecall.alpha>0.5f) {
                popupWindow!!.dismiss()
                recallMessages(selectedSize)
                adapter!!.unSelectAllMessage()
            }
        }
        tvDelete.setOnClickListener {
            if (tvDelete.alpha>0.5f) {
                popupWindow!!.dismiss()
                deleteMessages(selectedSize)
                adapter!!.unSelectAllMessage()
            }
        }
        tvForward.setOnClickListener {
            if (tvForward.alpha>0.5f) {
                popupWindow!!.dismiss()
                forwardMessages(selectedSize)
                adapter!!.unSelectAllMessage()

            }
        }
        tvShare.setOnClickListener {
            popupWindow!!.dismiss()
        }
        tvLike.setOnClickListener {
            if (tvLike.alpha>0.5f) {
                popupWindow!!.dismiss()
                likeOrDisLikeMessage(selectedSize[0].message_id,selectedSize[0].is_like)
                adapter!!.unSelectAllMessage()
            }
        }
        tvReport.setOnClickListener {
            if (tvReport.alpha>0.5f){
                popupWindow!!.dismiss()
                reportTheMessage(selectedSize[0].message_id)
                adapter!!.unSelectAllMessage()
            }
        }
        tvEdit.setOnClickListener {
            if (tvEdit.alpha>0.5f){
                popupWindow!!.dismiss()
                editMessage(selectedSize[0].message_id)
                adapter!!.unSelectAllMessage()
            }
        }
    }

    private fun editMessage(messageId: Long) {
        troopClient.editMessage(messageId,entityId,entityType,0,"Edited This Message")
    }

    private fun reportTheMessage(messageId: Long){
        troopClient.reportMessage(messageId,"")
    }
    private fun likeOrDisLikeMessage(messageId:Long,isLike:Int) {
        if (isLike==0) {
            troopClient.likeMessage(messageId)
        }else if (isLike==1) {
            troopClient.unLikeMessage(messageId)
        }
    }

    private fun deleteMessages(selectedSize:ArrayList<ConversationModel>){
        var localIds : ArrayList<Long> = ArrayList()
        var size = selectedSize.size
        for (i in 0 until  size){
            localIds.add(selectedSize[i].ID)
        }
        troopClient.deleteMessages(localIds,entityId,entityType,isRoom)
    }
    private fun recallMessages(selectedSize:ArrayList<ConversationModel>){
        var localIds : ArrayList<String> = ArrayList()
        var size = selectedSize.size
        for (i in 0 until  size){
            localIds.add(selectedSize[i].message_id.toString())
        }
        troopClient.recallMessages(localIds,entityId,entityType,isRoom)
    }

    private fun forwardMessages(selectedSize: ArrayList<ConversationModel>){
        if (forkOutData!=null && forkOutData.length()>0){
            dataBinding.ivForkOut.callOnClick()
        }
        var intent = Intent(this,ForwardUserListActivity::class.java)
        var listOfString: ArrayList<String> = ArrayList()
        for (i in 0 until selectedSize.size){
            if (selectedSize[i].message_id>0){
                listOfString.add(selectedSize[i].message_id.toString())
            }
        }
        intent.putExtra("data",Gson().toJson(listOfString))
        resultForwardLauncher.launch(intent)

    }
    private val resultForwardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data!!
            if (data!=null){
                var jsonObject = data.getStringExtra("forward")!!
                var messageids = data.getStringExtra("message_id")
                val typeToken = object : TypeToken<ArrayList<String>>() {}.type
                val typeTokenForward = object : TypeToken<ArrayList<Forward>>() {}.type
                var messageList:ArrayList<String> = Gson().fromJson(messageids, typeToken)
                var forwardList:ArrayList<Forward> = Gson().fromJson(jsonObject, typeTokenForward)
                Thread {
                    troopClient.sendForwardMessages(messageList, forwardList)
                }.start()
            }

            //doSomeOperations()
        }
    }
    private fun openForkOut(){
        var intent = Intent(this,ForkOutActivity::class.java)
        resultForkOutLauncher.launch(intent)
    }
    private val resultForkOutLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data!!
            if (data!=null){
                forkOutData = Helper.stringToJsonArray(data.getStringExtra("forkout")!!)!!
                dataBinding.ivForkOut.setImageResource(R.drawable.forkout_active)
            }
            //doSomeOperations()
        }
    }
    override fun onResume() {
        troopClient.setScreen(ConstantValues.CurrentScreen.CONVERSATION,entityId.toLong(),entityType,isRoom)
        var app = application as MyApplication
        app.setListener(this)
        super.onResume()
    }

    override fun onPause() {
        troopClient.setScreen(ConstantValues.CurrentScreen.OTHER,entityId.toLong(),entityType,isRoom)
        super.onPause()
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
        var tvPicContact : TextView? = bottomSheetDialog!!.findViewById(R.id.tvPicContact)
        var tvPicLocation : TextView? = bottomSheetDialog!!.findViewById(R.id.tvPicLocation)
        var tvAudio : TextView? = bottomSheetDialog!!.findViewById(R.id.tvAudio)

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
        tvPicContact!!.setOnClickListener {
            bottomSheetDialog!!.dismiss()
            if (Constants.checkContactPermissions(this)){
                openContacts()
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(Constants.CONTATCT_PERMISSION,Constants.CONTACT_PERMISSION_CALLBACK)
                }
            }
        }

        tvPicLocation!!.setOnClickListener {
            bottomSheetDialog!!.dismiss()
            if (Constants.checkLocationPermissions(this)){
                openLocation()
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(Constants.LOCATION_PERMISSION,Constants.LOCATION_PERMISSION_CALLBACK)
                }
            }
        }
        tvAudio!!.setOnClickListener {
            bottomSheetDialog!!.dismiss()
            sendVoiceMessage()
        }
        bottomSheetDialog.show()

    }
    var currentImageUri:Uri? = null
    var attachmentType = 0
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
                currentImageUri = data
            }else if (currentImageUri!=null){
                //val bitmap = BitmapFactory.decodeStream(contentResolver?.openInputStream(currentImageUri!!))
            }
            if (currentImageUri.toString().contains("image")){
                attachmentType = 0
            }else if (currentImageUri.toString().contains("video")){
                attachmentType = 1
            }else{
                attachmentType = 2
            }

            var messageData = MessageData()
            if (forkOutData!=null && forkOutData.length()>0){
                var messageDataList : ArrayList<MessageData> = ArrayList()
                for (i in 0 until forkOutData.length()){
                    var messageData = MessageData()
                    messageData.message = ""
                    messageData.attachment = if (attachmentType==0) "https://media.troopmessenger.com/desktop/attachments/1709291206289_ZwFxy/TroopMessenger_1709291206198.png" else if(attachmentType==1) "https://media.troopmessenger.com/android/attachments/1688392484170966/23_07_03_09_54_29.mp4" else "https://view.officeapps.live.com/op/embed.aspx?src=https://media.troopmessenger.com/desktop/attachments/1702360272784_zfdrJ/TM_Changes_Additions.docx"
                    messageData.caption = dataBinding.etMessage.text.toString()
                    messageData.is_group = forkOutData.optJSONObject(i).optInt("entity")-1
                    messageData.receiver_id = forkOutData.optJSONObject(i).optString("receiver_id")
                    messageData.is_room = 0
                    messageData.receiver_uid = forkOutData.optJSONObject(i).optString("entity_uid")
                    messageData.sender_uid = LOGINUSERUID
                    messageDataList.add(messageData)
                }
                if (messageDataList.size>0){
                    troopClient.sendForkOutAttachmentMessages(messageDataList)
                    dataBinding.ivForkOut.callOnClick()
                }
            }else
            if (dataBinding.replyMessageHolder!=null && dataBinding.replyMessageHolder.visibility==View.VISIBLE){
                messageData.caption = dataBinding.etMessage.text.toString()
                messageData.message = ""
                messageData.attachment = if (attachmentType==0) "https://media.troopmessenger.com/desktop/attachments/1709291206289_ZwFxy/TroopMessenger_1709291206198.png" else if(attachmentType==1) "https://media.troopmessenger.com/android/attachments/1688392484170966/23_07_03_09_54_29.mp4" else "https://view.officeapps.live.com/op/embed.aspx?src=https://media.troopmessenger.com/desktop/attachments/1702360272784_zfdrJ/TM_Changes_Additions.docx"
                messageData.is_group = entityType-1
                messageData.receiver_id = entityId
                messageData.is_room = 0
                messageData.receiver_uid = entityUid
                messageData.sender_uid = LOGINUSERUID
                messageData.message_id = selectedReplyMessage!!.message_id
                troopClient.sendReplyAttachmentMessage(messageData)
                dataBinding.replyItemClose.callOnClick()
            }else {
                messageData.message = ""
                messageData.caption = "Test Message"
                messageData.attachment = if (attachmentType==0) "https://media.troopmessenger.com/desktop/attachments/1709291206289_ZwFxy/TroopMessenger_1709291206198.png" else if(attachmentType==1) "https://media.troopmessenger.com/android/attachments/1688392484170966/23_07_03_09_54_29.mp4" else "https://view.officeapps.live.com/op/embed.aspx?src=https://media.troopmessenger.com/desktop/attachments/1702360272784_zfdrJ/TM_Changes_Additions.docx"
                messageData.is_group = entityType - 1
                messageData.receiver_id = entityId
                messageData.is_room = 0
                messageData.receiver_uid = entityUid
                messageData.sender_uid = LOGINUSERUID
                troopClient.sendAttachmentMessage(messageData)
            }

            //doSomeOperations()
        }
    }

    private fun openGallery(){
        val intent = Intent()
        intent.type = "image/* video/*";
        intent.action = Intent.ACTION_GET_CONTENT
        attachmentType = 0
        resultLauncher.launch(intent)
    }
    private fun openDoc(){
        val intent = Intent()
        intent.type = "*/*"
        attachmentType = 2
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }
    private fun openContacts(){
        val intent = Intent(this,ContactPicActivity::class.java)
        resultContactLauncher.launch(intent)
    }
    var resultContactLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data!!
            if (data!=null){
                var jsonObject = Helper.stringToJsonObject(data.getStringExtra("data")!!)!!
                if (forkOutData!=null && forkOutData.length()>0){
                    var messageDataList : ArrayList<MessageData> = ArrayList()
                    for (i in 0 until forkOutData.length()){
                        var messageData = MessageData()
                        messageData.message = ""
                        messageData.contact_number = jsonObject.optString("contact_number")
                        messageData.contact_name = jsonObject.optString("contact_name")
                        messageData.is_group = forkOutData.optJSONObject(i).optInt("entity")-1
                        messageData.receiver_id = forkOutData.optJSONObject(i).optString("receiver_id")
                        messageData.is_room = 0
                        messageData.receiver_uid = forkOutData.optJSONObject(i).optString("entity_uid")
                        messageData.sender_uid = LOGINUSERUID
                        messageDataList.add(messageData)
                    }
                    if (messageDataList.size>0){
                        troopClient.sendForkOutContactMessages(messageDataList)
                        dataBinding.ivForkOut.callOnClick()
                    }
                }else
                if (jsonObject!=null && jsonObject.has("contact_number") && !jsonObject.optString("contact_number").isNullOrEmpty()){
                    var messageData = MessageData()
                    if (dataBinding.replyMessageHolder!=null && dataBinding.replyMessageHolder.visibility==View.VISIBLE){
                        messageData.message = ""
                        messageData.contact_number = jsonObject.optString("contact_number")
                        messageData.contact_name = jsonObject.optString("contact_name")
                        messageData.is_group = entityType-1
                        messageData.receiver_id = entityId
                        messageData.is_room = 0
                        messageData.receiver_uid = entityUid
                        messageData.sender_uid = LOGINUSERUID
                        messageData.message_id = selectedReplyMessage!!.message_id
                        troopClient.sendReplyContactMessage(messageData)
                        dataBinding.replyItemClose.callOnClick()
                    }else {
                        messageData.message = ""
                        messageData.is_group = entityType - 1
                        messageData.receiver_id = entityId
                        messageData.is_room = 0
                        messageData.receiver_uid = entityUid
                        messageData.sender_uid = LOGINUSERUID
                        messageData.contact_number = jsonObject.optString("contact_number")
                        messageData.contact_name = jsonObject.optString("contact_name")
                        troopClient.sendContactMessage(messageData)
                    }
                }
            }

            //doSomeOperations()
        }
    }
    private fun sendVoiceMessage(){
        var messageData = MessageData()
        if (forkOutData!=null && forkOutData.length()>0){
            var messageDataList : ArrayList<MessageData> = ArrayList()
            for (i in 0 until forkOutData.length()){
                var messageData = MessageData()
                messageData.message = ""
                messageData.attachment = "https://media.troopmessenger.com/desktop/attachments/1710152722810_WMaNn/file_example_MP3_700KB.mp3"
                messageData.caption = ""
                messageData.is_group = forkOutData.optJSONObject(i).optInt("entity")-1
                messageData.receiver_id = forkOutData.optJSONObject(i).optString("receiver_id")
                messageData.is_room = 0
                messageData.receiver_uid = forkOutData.optJSONObject(i).optString("entity_uid")
                messageData.sender_uid = LOGINUSERUID
                messageDataList.add(messageData)
            }
            if (messageDataList.size>0){
                troopClient.sendForkOutAudioMessages(messageDataList)
                dataBinding.ivForkOut.callOnClick()
            }
        }else
        if (dataBinding.replyMessageHolder!=null && dataBinding.replyMessageHolder.visibility==View.VISIBLE){
            messageData.message = ""
            messageData.caption = ""
            messageData.attachment = "https://media.troopmessenger.com/desktop/attachments/1710152722810_WMaNn/file_example_MP3_700KB.mp3"
            messageData.is_group = entityType-1
            messageData.receiver_id = entityId
            messageData.is_room = 0
            messageData.receiver_uid = entityUid
            messageData.sender_uid = LOGINUSERUID
            messageData.message_id = selectedReplyMessage!!.message_id
            troopClient.sendReplyAudioMessage(messageData)
            dataBinding.replyItemClose.callOnClick()
        }else {
            messageData.message = ""
            messageData.caption = ""
            messageData.attachment = "https://media.troopmessenger.com/desktop/attachments/1710152722810_WMaNn/file_example_MP3_700KB.mp3"
            messageData.is_group = entityType - 1
            messageData.receiver_id = entityId
            messageData.is_room = 0
            messageData.receiver_uid = entityUid
            messageData.sender_uid = LOGINUSERUID
            troopClient.sendAudioMessage(messageData)
        }
    }
    private fun openLocation(){
        val intent = Intent(this,LocationPickerActivity::class.java)
        resultLocationLauncher.launch(intent)
    }
    var resultLocationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data!!
            if (data!=null){
                //var jsonObject = Helper.stringToJsonObject(data.getStringExtra("data")!!)!!
                val address: Address = data.getParcelableExtra<Address>("address")!!
                if (address == null) {
                    Toast.makeText(this,"Location pick failed Please try again",Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                var locationName = ""
                locationName = address.locality+" - "+address.adminArea
                var locationObject = JSONObject()
                locationObject.put("location_latitude",address.latitude)
                locationObject.put("location_longitude",address.longitude)
                locationObject.put("location_address",address.getAddressLine(0))
                locationObject.put("location_name",locationName)
                Log.e("contact--->"," data "+(locationObject.toString())+"   "+forkOutData.toString())
                if (locationObject!=null && locationObject.has("location_latitude") && !locationObject.optString("location_latitude").isNullOrEmpty()){
                    if (forkOutData!=null && forkOutData.length()>0){
                        var messageDataList : ArrayList<MessageData> = ArrayList()
                        for (i in 0 until forkOutData.length()){
                            var messageData = MessageData()
                            messageData.message = ""
                            messageData.location_name = locationObject.optString("location_name")
                            messageData.location_address = locationObject.optString("location_address")
                            messageData.location_latitude =
                                locationObject.optString("location_latitude")
                            messageData.location_longitude =
                                locationObject.optString("location_longitude")
                            messageData.is_group = forkOutData.optJSONObject(i).optInt("entity")-1
                            messageData.receiver_id = forkOutData.optJSONObject(i).optString("receiver_id")
                            messageData.is_room = 0
                            messageData.receiver_uid = forkOutData.optJSONObject(i).optString("entity_uid")
                            messageData.sender_uid = LOGINUSERUID
                            messageDataList.add(messageData)
                        }
                        if (messageDataList.size>0){
                            troopClient.sendForkOutLocationMessages(messageDataList)
                            dataBinding.ivForkOut.callOnClick()
                        }
                    }else
                    if (dataBinding.replyMessageHolder!=null && dataBinding.replyMessageHolder.visibility==View.VISIBLE){
                        var messageData = MessageData()
                        messageData.message = ""
                        messageData.location_name = locationObject.optString("location_name")
                        messageData.location_address = locationObject.optString("location_address")
                        messageData.location_latitude =
                            locationObject.optString("location_latitude")
                        messageData.location_longitude =
                            locationObject.optString("location_longitude")
                        messageData.is_group = entityType-1
                        messageData.receiver_id = entityId
                        messageData.is_room = 0
                        messageData.receiver_uid = entityUid
                        messageData.sender_uid = LOGINUSERUID
                        messageData.message_id = selectedReplyMessage!!.message_id
                        troopClient.sendReplyLocationMessage(messageData)
                        dataBinding.replyItemClose.callOnClick()
                    }else {
                        var messageData = MessageData()
                        messageData.message = ""
                        messageData.is_group = entityType - 1
                        messageData.receiver_id = entityId
                        messageData.is_room = 0
                        messageData.receiver_uid = entityUid
                        messageData.sender_uid = LOGINUSERUID
                        messageData.location_name = locationObject.optString("location_name")
                        messageData.location_address = locationObject.optString("location_address")
                        messageData.location_latitude =
                            locationObject.optString("location_latitude")
                        messageData.location_longitude =
                            locationObject.optString("location_longitude")
                        troopClient.sendLocationMessage(messageData)
                    }
                }
            }

            //doSomeOperations()
        }
    }
    private fun clearText(){
        dataBinding.etMessage.text.clear()
    }
    private fun setTyping(status: Int) {
        troopClient.setTypingStatus(entityId,entityType,status)
    }
    private val onTypingTimeout = kotlinx.coroutines.Runnable {
        try {
            if (!mTyping) return@Runnable
            mTyping = false
            setTyping(0)
        } catch (e: Exception) {
            //Helper.printExceptions(e)
        }
    }

    override fun userStatus(jsonObject: JSONObject) {
        if (jsonObject!=null){
            if (jsonObject.optString("user_id") == entityId && entityType==ConstantValues.Entity.USER) {
                if (jsonObject.optString("last_seen").isNullOrEmpty()){
                    userStatusText = jsonObject.optString("last_seen")
                }else{
                    userStatusText = jsonObject.optString("status")
                }
            }
            runOnUiThread {
                dataBinding.tvUserStatus.text = userStatusText
            }
        }
    }
    override fun typing(jsonObject: JSONObject) {
        if (jsonObject!=null){
            if (jsonObject.optString("user_id") == entityId && jsonObject.optInt("is_group")==(entityType-1)) {
                var typingText = ""
                if (jsonObject.optInt("typing")==1){
                    typingText = if (jsonObject.optInt("is_group")==1) jsonObject.optString("typing_user_name")+" typing..." else  "typing..."
                }else{
                    typingText = userStatusText
                }
                runOnUiThread {
                    dataBinding.tvUserStatus.text = typingText
                }

            }
        }
    }

    override fun syncing(status: Boolean) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                Constants.CALL_PERMISSION_CALLBACK->{
                    dataBinding.ivAudioCall.callOnClick()
                }
            }
        }
    }
}