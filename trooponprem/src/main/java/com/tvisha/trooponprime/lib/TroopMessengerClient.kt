package com.tvisha.trooponprime.lib

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.SurfaceView
import android.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.google.gson.Gson
import com.tvisha.trooponprime.lib.api.BaseAPIService
import com.tvisha.trooponprime.lib.api.BaseRetrofitClient
import com.tvisha.trooponprime.lib.api.CallApiService
import com.tvisha.trooponprime.lib.api.CallRetrofitClient
import com.tvisha.trooponprime.lib.call.RoomClient
import com.tvisha.trooponprime.lib.clientModels.*
import com.tvisha.trooponprime.lib.database.TroopDataBase
import com.tvisha.trooponprime.lib.database.model.ForwardModel
import com.tvisha.trooponprime.lib.database.model.GroupMembersModel
import com.tvisha.trooponprime.lib.database.model.ParticipantUsers
import com.tvisha.trooponprime.lib.listeneres.CallBackListener
import com.tvisha.trooponprime.lib.listeneres.ClientCallBackListener
import com.tvisha.trooponprime.lib.socket.TroopMessenger
import com.tvisha.trooponprime.lib.socket.TroopMessengerCall
import com.tvisha.trooponprime.lib.socket.TroopSocketClient
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper
import com.tvisha.trooponprime.lib.utils.SharePreferenceConstants
import io.socket.client.Socket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.mediasoup.droid.MediasoupClient
import org.webrtc.EglBase
import org.webrtc.RTCUtils
import org.webrtc.RtpParameters
import org.webrtc.VideoCapturer

class TroopMessengerClient  {
    companion object{
        internal var sharedPreferences : SharedPreferences? = null
        internal var APPLICATION_TYPE:Int = ConstantValues.ONLY_CALLS
        internal var messengerSocket: Socket? = null
        internal var callSocket: Socket? = null
        internal var dataBase : TroopDataBase? = null
        lateinit var clientCallBackListener:ClientCallBackListener
        internal var bitRates: ArrayList<RtpParameters.Encoding> = ArrayList()
        internal var APPLICATION_TM_LOGIN_USER_ID :String = ""
        internal var CALL_ACCESS_TOKEN :String = ""
        internal var APPLICATION_LOGIN_U_ID :String = ""
        internal var APP_AUTHORIZATION_TOKEN :String = ""
        internal var APP_ACCESS_TOKEN : String = ""
        internal var APP_LOGIN_USER_MOBILE_NUMBER : String = ""
        internal var APP_LOGIN_USER_MOBILE_COUNTRY : String = ""
        internal var APP_LOGIN_USER_NAME : String = ""
        internal var CLIENT_IS_CONNECTED : Boolean = false
        internal var CALL_AUTHORIZATION_TOKEN : String = ""
        internal var CALL_TM_USER_ID : String= ""
        var context: Context? = null
        var currentScreen : Int = 1
        var convEntityId :String = ""
        var convEntityType:Int = 0
        internal var clientInitialized:Boolean = false
        internal var baseAPIService : BaseAPIService? = null
        internal var callAPIService : CallApiService? = null
        internal var ACTIVE_CALL_ID : String = ""
        internal var activeCallData : JSONObject = JSONObject()
        internal var ACTIVE_CALL_TYPE : Int = 0
        var participantUsersList:ArrayList<ParticipantUsers> = ArrayList()
        //var updateUserList = MutableLiveData<ArrayList<ParticipantUsers>>()
        var usbVideoCapture:VideoCapturer? = null
    }
    /*fun callUserStatus():MutableLiveData<ArrayList<ParticipantUsers>>{
        //return updateUserList
    }*/

    /**
     * this function is check user already login or not
     */
    fun isLoggedIn(): Boolean {
        if (sharedPreferences==null){
            return false
        }
        return if (APPLICATION_TYPE==ConstantValues.ONLY_CALLS) sharedPreferences!!.getBoolean(SharePreferenceConstants.CALL_SDK_REGISTER_STATUS,false) else sharedPreferences!!.getBoolean(SharePreferenceConstants.TM_USER_LOGIN_STATUS,false)
    }
    fun callTmUserId():String{
        return CALL_TM_USER_ID
    }
    fun tmUserId():String{
        if (sharedPreferences==null){
            return ""
        }
        return sharedPreferences!!.getString(SharePreferenceConstants.TM_LOGIN_USER_ID,"")!!
    }
    /**
     * @param applicationContext initialize the client
     */
    fun initializeTroop(applicationContext: Context){
        context = applicationContext
        clientInitialized = true
        baseAPIService = BaseRetrofitClient.instance
        callAPIService = CallRetrofitClient.instance
        sharedPreferences = context!!.getSharedPreferences(SharePreferenceConstants.SHARED_PREFERENCE_NAME,Context.MODE_PRIVATE)
        dataBase = TroopDataBase.getDataBaseInstance(context!!)
        //Log.e("database===> ",""+ dataBase!!.openHelper!!.readableDatabase!!.version.toString())
        //viewModel = DataBaseViewModel(repository)
        //startApplication()
        if (sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")==null || sharedPreferences!!.getString(
                SharePreferenceConstants.DEVICE_ID,"")!!.trim().isEmpty() || sharedPreferences!!.getString(
                SharePreferenceConstants.DEVICE_ID,"")!!.trim() == "null"
        ){
            ConstantValues.deviceId = Settings.System.getString(context!!.contentResolver, Settings.Secure.ANDROID_ID)!!
            sharedPreferences!!.edit().putString(
                SharePreferenceConstants.DEVICE_ID,
                ConstantValues.deviceId
            ).apply()
        }else{
            ConstantValues.deviceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")!!
        }
        bitRates.add(RTCUtils.genRtpEncodingParameters("r0", true, 1.0, 1, 512000, 200000, 15, 1,4.0,1L, false))
        bitRates.add(RTCUtils.genRtpEncodingParameters("r1", true, 1.0, 1, 1024000, 200000, 15, 2,2.0,2L,false))
        bitRates.add(RTCUtils.genRtpEncodingParameters("r2", true, 1.0, 1, 2048000, 200000, 15, 3,1.0,3L,false))
    }

    /**
     * to start the sdk to listen all data
     * @param uid [user unique id] required
     * @param name required
     * @param mobileNumber optional
     * @param countryCode optional
     */
    fun runTheClientSdk(uid:String,name:String,mobileNumber:String,countryCode:String,listeners: ClientCallBackListener){
        if (!clientInitialized){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","initialize the SDK first, you can call the initializeTroop method inside your application oncreate method ")
            listeners.tmError(jsonObject)
            return
        }
        clientCallBackListener = listeners
        if (!isLoggedIn()){
            GlobalScope.launch {
                if (APPLICATION_TYPE==ConstantValues.ONLY_CALLS){
                    TroopClient.callRegister(uid, name, mobileNumber, countryCode, listeners)
                }else {
                    TroopClient.register(uid, name, mobileNumber, countryCode, listeners)
                }
            }
        }else{
            startListening(listeners)
        }

    }
    fun startListening(listeners: ClientCallBackListener){
        if (!isLoggedIn()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message"," Please login first ")
            listeners.tmError(jsonObject)
            return
        }
        MediasoupClient.initialize(context)
        clientCallBackListener = listeners
        if (APPLICATION_TYPE==ConstantValues.ONLY_CALLS){
            if (sharedPreferences!!.getBoolean(SharePreferenceConstants.CALL_SDK_REGISTER_STATUS,false)) {
                TroopClient.callSocketInitialize()
            }
        }else {
            APPLICATION_TM_LOGIN_USER_ID =
                sharedPreferences!!.getString(SharePreferenceConstants.TM_LOGIN_USER_ID, "")!!
            APPLICATION_LOGIN_U_ID =
                sharedPreferences!!.getString(SharePreferenceConstants.TM_LOGIN_USER_UID, "")!!
            APP_LOGIN_USER_NAME =
                sharedPreferences!!.getString(SharePreferenceConstants.TM_LOGIN_USER_NAME, "")!!
            APP_LOGIN_USER_MOBILE_NUMBER =
                sharedPreferences!!.getString(SharePreferenceConstants.TM_LOGIN_USER_MOBILE, "")!!
            APP_LOGIN_USER_MOBILE_COUNTRY = sharedPreferences!!.getString(
                SharePreferenceConstants.TM_LOGIN_USER_COUNTRY_CODE,
                ""
            )!!
            APP_AUTHORIZATION_TOKEN =
                sharedPreferences!!.getString(SharePreferenceConstants.AUTHORIZATION_TOKEN, "")!!

            TroopClient.messengerSocketInitialize()
        }

    }

    /**
     * @param screen required
     * this status used to show notifications are not
     * 1/2/3 - 1 for recent list page screen, 2 for conversation page , 3 for other screens
     */
    fun setScreen(screen:Int,entityId: Long,entityType: Int,isRoom: Int){
        currentScreen = screen
        if (currentScreen==ConstantValues.CurrentScreen.CONVERSATION) {
            convEntityId = entityId.toString()
            convEntityType = entityType
            if (!convEntityId.isNullOrEmpty()){
                TroopMessenger.fetchAndUpdateReadStatus(convEntityId, convEntityType,isRoom)
            }
            if (entityType==ConstantValues.Entity.USER){
                fetchUserPresence(entityId,entityType)
            }
        }
    }
    /**
     * Text Message
     * @param messageData
     * required data is [receiver_id,is_group,is_room,message,conversation_reference_id,receiver_uid,sender_uid]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     */
    fun sendTextMessage(messageData:MessageData){
        TroopMessenger.sendTextMessage(messageData, clientCallBackListener)
    }

    /**
     * Text ForkOut Message
     * @param messageData  list of MessageData
     * required data is [receiver_id,is_group,is_room,message,conversation_reference_id,receiver_uid,sender_uid]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     */
    fun sendForkOutTextMessages(messageData:List<MessageData>){
        TroopMessenger.sendForkOutTextMessage(messageData, clientCallBackListener)
    }

    /**
     * Attachment Message
     * @param messageData attachment message
     * required data is [receiver_id,is_group,is_room,attachment,caption,preview_link,conversation_reference_id,receiver_uid,sender_uid]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     */
    fun sendAttachmentMessage(messageData: MessageData){
        TroopMessenger.sendAttachmentMessage(messageData, clientCallBackListener)
    }

    /**
     * ForkOut Attachment
     * @param messageData list of MessageData
     * required data is [receiver_id,is_group,is_room,attachment,caption,preview_link,conversation_reference_id,receiver_uid,sender_uid]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     */
    fun sendForkOutAttachmentMessages(messageData: List<MessageData>){
        TroopMessenger.sendForkOutAttachments(messageData, clientCallBackListener)
    }

    /**
     * Audio Message
     * @param messageData
     * required data is [receiver_id,is_group,is_room,attachment,preview_link,conversation_reference_id,receiver_uid,sender_uid]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     */
    fun sendAudioMessage(messageData: MessageData){
        TroopMessenger.sendAudioMessage(messageData, clientCallBackListener)
    }

    /**
     * Audio ForkOut Message
     * @param messageData  list of MessageData
     * required data is [receiver_id,is_group,is_room,attachment,preview_link,conversation_reference_id,receiver_uid,sender_uid]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     */
    fun sendForkOutAudioMessages(messageData: List<MessageData>){
        TroopMessenger.sendForkOutAudioMessages(messageData, clientCallBackListener)
    }
    /**
     * Location Message
     * @param messageData
     * required data is [receiver_id,is_group,is_room,location_latitude,location_longitude,location_address,location_name,conversation_reference_id,receiver_uid,sender_uid]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     */
    fun sendLocationMessage(messageData: MessageData){
        TroopMessenger.sendLocationMessage(messageData, clientCallBackListener)
    }

    /**
     * Location ForkOut Message
     * @param messageData  list of MessageData
     * required data is [receiver_id,is_group,is_room,location_latitude,location_longitude,location_address,location_name,conversation_reference_id,receiver_uid,sender_uid]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     */
    fun sendForkOutLocationMessages(messageData: List<MessageData>){
        TroopMessenger.sendForkOutLocationMessages(messageData, clientCallBackListener)
    }

    /**
     * Contact Message
     * @param messageData
     * required data is [receiver_id,is_group,is_room,contact_name,contact_number,conversation_reference_id,receiver_uid,sender_uid]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     */
    fun sendContactMessage(messageData: MessageData){
        TroopMessenger.sendContactMessage(messageData, clientCallBackListener)
    }

    /**
     * Contact ForkOut Message
     * @param messageData
     * required data is [receiver_id,is_group,is_room,contact_name,contact_number,conversation_reference_id,receiver_uid,sender_uid]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     */
    fun sendForkOutContactMessages(messageData: List<MessageData>){
        TroopMessenger.sendForkOutContactMessages(messageData, clientCallBackListener)
    }

    /**
     * Activity Message
     * @param messageData
     * required data is [receiver_id,is_group,is_room,message,conversation_reference_id,receiver_uid,sender_uid]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     */
    fun sendActivityMessage(messageData: MessageData){
        TroopMessenger.sendActivityMessage(messageData, clientCallBackListener)
    }

    /**
     * Activity ForkOut Message
     * @param messageData
     * required data is [receiver_id,is_group,is_room,message,conversation_reference_id,receiver_uid,sender_uid]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     */
    fun sendForkOutActivityMessages(messageData: List<MessageData>){
        TroopMessenger.sendForkOutActivityMessages(messageData, clientCallBackListener)
    }

    /**
     * Reply Text Message
     * @param messageData
     * required data is [receiver_id,is_group,is_room,message,conversation_reference_id,receiver_uid,sender_uid,message_id]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     * message_id is original message_id [reply selected message_id]
     */
    fun sendReplyTextMessage(messageData: MessageData){
        TroopMessenger.sendTextReplyMessage(messageData, clientCallBackListener)
    }

    /**
     * Reply Attachment Message
     * @param messageData attachment message
     * required data is [receiver_id,is_group,is_room,attachment,caption,preview_link,conversation_reference_id,receiver_uid,sender_uid,message_id]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     * message_id is original message_id [reply selected message_id]
     */
    fun sendReplyAttachmentMessage(messageData: MessageData){
        TroopMessenger.sendAttachmentReplyMessage(messageData, clientCallBackListener)
    }

    /**
     * Contact Message
     * @param messageData
     * required data is [receiver_id,is_group,is_room,contact_name,contact_number,conversation_reference_id,receiver_uid,sender_uid,message_id]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     * message_id is original message_id [reply selected message_id]
     */
    fun sendReplyContactMessage(messageData: MessageData){
        TroopMessenger.sendContactReplyMessage(messageData, clientCallBackListener)
    }

    /**
     * Location Message
     * @param messageData
     * required data is [receiver_id,is_group,is_room,location_latitude,location_longitude,location_address,location_name,conversation_reference_id,receiver_uid,sender_uid,message_id]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     * message_id is original message_id [reply selected message_id]
     */
    fun sendReplyLocationMessage(messageData: MessageData){
        TroopMessenger.sendLocationReplyMessage(messageData, clientCallBackListener)
    }

    /**
     * Audio Message
     * @param messageData
     * required data is [receiver_id,is_group,is_room,attachment,preview_link,conversation_reference_id,receiver_uid,sender_uid,message_id]
     * is_group is 1 for group and 0 for user
     * is_room is 0/1
     * message_id is original message_id [reply selected message_id]
     */
    fun sendReplyAudioMessage(messageData: MessageData){
        TroopMessenger.sendAudioReplyMessage(messageData, clientCallBackListener)
    }

    /**
     * Forward Message
     * @param messageIds list of messageIds
     * @param forwardList list of Forward [receiver_id,entity]
     * required receiver_id,entity and messageIds
     * entity is 1 for user and 2 for group
     */
    fun sendForwardMessages(messageIds:List<String>,forwardList:List<Forward>){
        TroopMessenger.sendForwardMessages(messageIds,forwardList, clientCallBackListener)
    }

    /**
     * RecentList Live Data
     * recent page live  user/group list data
     */
    fun fetchRecentList():LiveData<List<RecentMessageList>>{
        return TroopClient.fetchRecentList()
    }

    /**
     * Recent page archived user/group list
     */
    fun fetchArchiveRecentList():LiveData<List<RecentMessageList>>{
        return TroopClient.fetchArchiveList()
    }

    /**
     * List of Contacts
     */
    fun fetchContactList():List<UserClientModel>{
        return TroopClient.fetchContactList()
    }

    /**
     * Forward user/group list
     */
    fun fetchForwardList():LiveData<List<ForwardModel>>{
        return TroopClient.fetchForwardList()
    }

    /**
     * Conversation data
     * @param entityId required if open conversation is group then group_id else user_id
     * @param entity 1 for user 2 for group
     * @param viewModel for observe the live data by pagination to smooth to load the conversation by paging
     * you call this method inside lifecycle to observe updated data
     */
    fun fetchConversation(entityId:String,entity:Int,viewModel:ViewModel): kotlinx.coroutines.flow.Flow<PagingData<ConversationModel>> {
        return TroopClient.fetchConversation(APPLICATION_TM_LOGIN_USER_ID,entityId,entity,viewModel)
    }

    /**
     * Recent list broadcast message
     * live data it will return latest one message only you call this method inside lifecycle to observe updated data
     */
    fun fetchLiveRecentBroadcastMessage():LiveData<List<BroadCastMessage>>{
        return TroopClient.fetchLiveRecentBroadCastMessage()
    }

    /**
     * Recent list broadcast message
     * returns latest broadcast message only
     */
    fun fetchRecentBroadcastMessage():BroadCastMessage{
        return TroopClient.fetchRecentBroadCastMessage()
    }

    /**
     * Broadcast message
     * returns live broad cast message you call this method inside lifecycle to observe updated data
     */
    fun fetchBroadCastMessages():LiveData<List<BroadCastMessage>>{
        return TroopClient.fetchBroadCastMessages()
    }

    /**
     * Archive chats to send the server and update in DB
     * required list of chats [entity_id,entity_type,is_archived (1 for archive | 0 for unarchive)]
     */
    fun archiveChats(chats: List<Chats>):JSONObject{
        return TroopSocketClient.archivedChats(chats)
    }
    /**
     * UnArchive chats to send the server and update in DB
     * required list of chats [entity_id,entity_type,is_archived (1 for archive | 0 for unarchive)]
     */
    fun unArchiveChats(chats: List<Chats>):JSONObject{
        return TroopSocketClient.unArchivedChats(chats)
    }

    //group
    /**
     * get group information
     * @param groupId required
     */
    fun fetchGroupDetails(groupId:String):MessengerGroupModel{
        return TroopClient.fetchGroupDetails(groupId)
    }
    /**
     * get live list of group members
     * @param groupId required
     * you can call this method inside lifecycle observer for update data
     */
    fun fetchGroupMembers(groupId: String):LiveData<List<GroupMembersModel>>{
        return TroopClient.fetchGroupMembers(groupId)
    }

    /**
     * API call for group creation, call this method in coroutines
     * @param groupCreateModel
     * required group_name (not empty),list of groupMembers (required member_id[not 0/empty])
     */
    suspend fun createGroup(groupCreateModel: GroupCreateModel):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.createGroup(
                groupCreateModel.group_name,
                groupCreateModel.group_avatar,
                groupCreateModel.group_description,
                groupCreateModel.conversation_reference_id,
                Helper.stringToJsonArray(Gson().toJson(groupCreateModel.groupMembers))!!
            )
        }else{
            return networkLostReturnObject()
        }
    }

    /**
     * API call for update Group Name, call this method in coroutines
     * @param groupId required it is not empty and must be greater than 0
     * @param groupName required it is not empty
     */
    suspend fun updateGroupName(groupId: String,groupName:String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.updateGroupName(groupId, groupName)
        }else{
            return networkLostReturnObject()
        }
    }

    /**
     * API call for update Group profile pic, call this method in coroutines
     * @param groupId required it is not empty and must be greater than 0
     * @param groupAvatar required
     */
    suspend fun updateGroupAvatar(groupId: String,groupAvatar:String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.updateGroupAvtar(groupAvatar, groupId)
        }else{
            return networkLostReturnObject()
        }
    }

    /**
     * API call for update Group member to Group admin role, call this method in coroutines
     * @param groupId required it is not empty and must be greater than 0
     * @param memberId required it is not empty and must be greater than 0
     */
    suspend fun makeGroupAdmin(groupId: String,memberId:String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.makeGroupAdmin(groupId, memberId)
        }else{
            return networkLostReturnObject()
        }
    }

    /**
     * API call for update Group member to Group moderator role, call this method in coroutines
     * @param groupId required it is not empty and must be greater than 0
     * @param memberId required it is not empty and must be greater than 0
     */
    suspend fun makeGroupModerator(groupId: String,memberId: String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.makeGroupModerator(groupId, memberId)
        }else{
            return networkLostReturnObject()
        }
    }
    /**
     * API call for update Group admin to Group member role, call this method in coroutines
     * @param groupId required it is not empty and must be greater than 0
     * @param memberId required it is not empty and must be greater than 0
     */
    suspend fun removeGroupAdmin(groupId: String,memberId: String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.removeGroupAdmin(groupId, memberId)
        }else{
            return networkLostReturnObject()
        }
    }
    /**
     * API call for update Group moderator to Group member role, call this method in coroutines
     * @param groupId required it is not empty and must be greater than 0
     * @param memberId required it is not empty and must be greater than 0
     */
    suspend fun removeGroupModerator(groupId: String,memberId: String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.removeGroupModerator(groupId, memberId)
        }else{
            return networkLostReturnObject()
        }
    }
    /**
     * API call for update Group remove the user from group, call this method in coroutines
     * @param groupId required it is not empty and must be greater than 0
     * @param memberId required it is not empty and must be greater than 0
     */
    suspend fun removeGroupUser(groupId: String,memberId: String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.removeGroupUser(groupId, memberId)
        }else{
            return networkLostReturnObject()
        }
    }

    /**
     * API call for delete the group , call this method in coroutines
     * @param groupId required it is not empty and must be greater than 0
     */
    suspend fun deleteGroup(groupId: String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.deleteGroup(groupId)
        }else{
            return networkLostReturnObject()
        }
    }

    /**
     * API call for exit from group , call this method in coroutines
     * @param groupId required it is not empty and must be greater than 0
     */
    suspend fun exitGroup(groupId: String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.exitGroup(groupId)
        }else{
            return networkLostReturnObject()
        }
    }
    /**
     * API call for add new members to Group , call this method in coroutines
     * @param groupId required it is not empty and must be greater than 0
     * @param groupMembers list of groupMembers (required member_id[not 0/empty])
     */
    suspend fun addGroupMembers(groupId: String,groupMembers: List<CreateGroupMembers>):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.addGroupMembers(
                groupId,
                Helper.stringToJsonArray(Gson().toJson(groupMembers))!!
            )
        }else{
            return networkLostReturnObject()
        }
    }

    /**
     * DB get the Group avatar
     * @param groupId required it is not empty and must be greater than 0
     */
    fun fetchGroupAvatar(groupId:String):String{
        return TroopClient.fetchGroupAvatar(groupId)
    }
    /**
     * DB update login user about
     * @param text
     */
    fun updateAboutMe(text: String):JSONObject{
        return TroopSocketClient.updateUserAbout(text)
    }

    /**
     * DB get selected messages text
     * @param id list of local ids
     */
    fun fetchCopiedMessages(id: List<Long>):List<String>{
        return TroopClient.fetchCopiedMessages(id)
    }

    /**
     * fetch the message actions like copy,delete,recall,edit [true/false]
     */
    fun getMessageActions(id: List<Long>):JSONObject{
        return TroopClient.getMessageActionsForMessage(id)
    }

    /**
     * DB get user profile data
     * @param uid required it is not empty
     */
    fun fetchUserProfileByUid(uid: String):UserClientModel{
        return TroopClient.fetchProfileByUid(uid)
    }

    /**
     * DB get user profile data
     * @param userId required it is not empty and greater than 0
     */
    fun fetchUserProfileByUserId(userId: String):UserClientModel{
        return TroopClient.fetchUserProfile(userId)
    }
    /**
     * DB get user profile pic
     * @param userId required it is not empty and greater than 0
     */
    fun fetchUserAvatar(userId: String):String{
        return TroopClient.fetchUserAvatar(userId)
    }
    /**
     * DB get user UserName pic
     * @param userId required it is not empty and greater than 0
     */
    fun fetchUserName(userId: String):String{
        return TroopClient.fetchUserName(userId)
    }
    /**
     * DB get list of users
     */
    fun fetchUsersListForCreatingGroup():List<UserClientModel>{
        return TroopClient.fetchUsersListForCreatingGroup()
    }
    /**
     * API call for update the self user profile pic , call this method in coroutines
     * @param userAvatar
     */
    suspend fun updateUserProfilePic(userAvatar:String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.updateUserProfilePic(userAvatar)
        }else{
            return networkLostReturnObject()
        }
    }
    /**
     * API call for add new contact to server , call this method in coroutines
     * @param uid required user unique id not empty
     * @param name required name is not empty
     */
    suspend fun addToNewContact(uid: String,name: String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.addContact(uid, name)
        }else{
            return networkLostReturnObject()
        }
    }

    /**
     * API call for save multiple contacts to server , call this method in coroutines
     * @param contacts list of contacts required [name must be not empty and mobile_number must be not empty and valid mobile number]
     */
    suspend fun saveContacts(contacts:List<Contact>):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.saveContacts(Helper.stringToJsonArray(Gson().toJson(contacts))!!)
        }else{
            return networkLostReturnObject()
        }
    }
    /**
     * DB save contact
     * @param entityId required it is not empty
     * @param name required it is not empty
     */
    fun saveContact(entityId: String,name: String):JSONObject{
        return TroopClient.saveContact(name,entityId)
    }

    //story
    /**
     * DB and event emit TEXT STORY
     * @param text required it is not empty
     * @param bgCode required it is not empty
     * @param fontName required it is not empty
     */
    fun addTextStory(text:String,bgCode:String,fontName:String):JSONObject{
        return TroopSocketClient.addTextStory(text,bgCode,fontName)
    }
    /**
     * DB and event emit IMAGE STORY
     * @param url required it is not empty
     */
    fun addImageStory(url:String):JSONObject{
        return TroopSocketClient.addImageStory(url)
    }
    /**
     * DB and event emit VIDEO STORY
     * @param url required it is not empty
     */
    fun addVideoStory(url:String):JSONObject{
        return TroopSocketClient.addVideoStory(url)
    }
    /**
     * DB fetchAll my self stories
     */
    fun fetchMyStories():LiveData<List<com.tvisha.trooponprime.lib.clientModels.StoryModel>>{
        return TroopClient.fetchMyStories()
    }
    /**
     * DB fetch All others stories
     */
    fun fetchOthersStories():LiveData<List<com.tvisha.trooponprime.lib.clientModels.StoryModel>>{
        return TroopClient.fetchOtherStories()
    }
    /**
     * DB fetch All stories by userid
     * @param userId must be greater than 0
     */
    fun fetchStoriesByUserId(userId: Long):LiveData<List<com.tvisha.trooponprime.lib.clientModels.StoryModel>>{
        return TroopClient.fetchStoriesByUserId(userId.toString())
    }
    /**
     * DB and event emit STORY VIEWED
     * @param id required it is greater than 0
     */
    fun storyViewed(id:Long){
        TroopSocketClient.storyViewed(id)
    }
    /**
     * DB and event emit DELETE STORY
     * @param id list of ids required each id greater than 0
     */
    fun deleteStories(id: List<Long>):JSONObject{
        return TroopSocketClient.deleteStory(id)
    }
    //room
    /**
     * API call for creating new ROOM , call this method in coroutines
     * @param roomCreateModel
     * required roomName is not empty
     */
    suspend fun createRoom(roomCreateModel: RoomCreateModel):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.createRoom(
                roomCreateModel.room_name,
                roomCreateModel.room_description,
                roomCreateModel.room_avatar,
                roomCreateModel.conversation_reference_id
            )
        }else{
            return networkLostReturnObject()
        }
    }
    /**
     * API call for update ROOM name , call this method in coroutines
     * @param roomId required it is not empty and grater than 0
     * @param roomName required roomName is not empty
     */
    suspend fun updateRoomName(roomId:String,roomName:String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.updateRoomName(roomId, roomName)
        }else{
            return networkLostReturnObject()
        }
    }
    /**
     * API call for update ROOM avatar , call this method in coroutines
     * @param roomId required it is not empty and grater than 0
     * @param roomAvatar required roomName is not empty
     */
    suspend fun updateRoomAvatar(roomId: String,roomAvatar:String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.updateRoomAvatar(roomId, roomAvatar)
        }else{
            return networkLostReturnObject()
        }
    }
    /**
     * API call for delete ROOM  , call this method in coroutines
     * @param roomId required it is not empty and grater than 0
     */
    suspend fun deleteRoom(roomId: String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.deleteRoom(roomId)
        }else{
            return networkLostReturnObject()
        }
    }
    /**
     * API call for exit ROOM  , call this method in coroutines
     * @param roomId required it is not empty and grater than 0
     */
    suspend fun exitRoom(roomId: String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.exitRoom(roomId)
        }else{
            return networkLostReturnObject()
        }
    }
    /**
     * API call for get the ROOM details , call this method in coroutines
     * @param roomId required it is not empty and grater than 0
     */
    suspend fun fetchRoomDetails(roomId: String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.fetchRoomDetails(roomId)
        }else{
            return networkLostReturnObject()
        }
    }
    /**
     * API call for join ROOM  , call this method in coroutines
     * @param roomId required it is not empty and grater than 0
     */
    suspend fun joinRoom(roomId: String):JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.joinRoom(roomId)
        }else{
            return networkLostReturnObject()
        }
    }

    /**
     * API call for get all ROOM list , call this method in coroutines
     */
    suspend fun roomList():JSONObject{
        if (context==null){
            return initializeSDKReturnObject()
        }
        if (Helper.getConnectivityStatus(context!!)) {
            return TroopClient.fetchRoomList()
        }else{
            return networkLostReturnObject()
        }
    }

    /**
     * Event emit
     * get the user status online/offline/last seen status
     * @param userId not 0
     * @param entityType 1 for user 2 for group
     */
    private fun fetchUserPresence(userId: Long,entityType: Int){
        if (entityType==ConstantValues.Entity.USER) {
            TroopClient.userPresence(userId.toString(), clientCallBackListener)
        }
    }
    /**
     * DB
     * update the attachment download status
     * @param messageId required grater than 0
     * @param attachmentPath required file stored path not empty
     */
    fun updateAttachmentDownloaded(messageId:Long,attachmentPath:String){
        TroopClient.attachmentDownloaded(messageId,attachmentPath)
    }

    /**
     * DB
     * update the aws attachment path to db
     * @param localId required grater than 0
     * @param awsPath required file stored path not empty
     */
    fun updateAwsFilePath(localId:Long,awsPath:String){
        TroopClient.updateAwsFilePath(localId,awsPath)
    }
    /**
     * Event emit REPORT USER
     * @param entityId required greater than 0
     * @param entityType required 1 for user and 2 for group
     * @param reason
     */
    fun reportUser(entityId: Long,entityType:Int,reason:String){
        TroopSocketClient.reportUser(entityId,entityType,reason, clientCallBackListener)
    }
    /**
     * Event emit UNBLOCK the USER
     * @param entityId required greater than 0
     */
    fun unBlockUser(entityId: Long){
        TroopSocketClient.unblockUser(entityId, clientCallBackListener)
    }
    /**
     * Event emit BLOCK the USER
     * @param entityId required greater than 0
     */
    fun blockUser(entityId: Long){
        TroopSocketClient.blockUser(entityId, clientCallBackListener)
    }
    /**
     * check user is blocked or not
     * @param entityId required greater than 0
     */
    fun checkIsUserIsBlock(entityId: Long):Int{
        return TroopClient.checkUserIsBlock(entityId)
    }
    /**
     * Event emit UNLIKE the MESSAGE
     * @param messageId required greater than 0
     */
    fun unLikeMessage(messageId: Long){
        TroopSocketClient.unLikeMessage(messageId, clientCallBackListener)
    }
    /**
     * Event emit LIKE the MESSAGE
     * @param messageId required greater than 0
     */
    fun likeMessage(messageId: Long){
        TroopSocketClient.likeMessage(messageId , clientCallBackListener)
    }
    /**
     * Event emit REPORT the MESSAGE
     * @param messageId required greater than 0
     * @param message
     */
    fun reportMessage(messageId: Long,message:String){
        TroopSocketClient.reportMessage(messageId,message, clientCallBackListener)
    }
    /**
     * user status text like online/offline/last seen/dnd
     * @param status required not empty [status_1,status_2,status_3]
     */
    fun getStatusText(status:String):String{
        return TroopClient.getStatusText(status)
    }
    /**
     * Event emit DELETE the USER/GROUP chats
     * @param chats required not empty of list
     * in chats entityId is not empty and not as 0, entityType is 1 for user 2 for group
     */
    fun deleteChats(chats: List<Chats>){
        TroopSocketClient.deleteChats(chats, clientCallBackListener)
    }

    /**
     * DB get non group members for particular group
     * @param groupId required it not empty and grater than 0
     */
    fun fetchNonGroupUserList(groupId: String):List<UserClientModel>{
        return TroopClient.fetchNonGroupUserList(groupId)
    }
    /**
     * Event emit RECALL messages
     * @param messageIds required not empty of list
     * @param entityId is not empty and not as 0
     * @param entityType is 1 for user 2 for group
     */
    fun recallMessages(messageIds: List<String>, entityId: String, entityType: Int,isRoom: Int){
        TroopMessenger.recallMessage(messageIds,entityId,entityType,isRoom, clientCallBackListener)
    }
    /**
     * Event emit EDIT messages
     * @param messageIds required not empty of list
     * @param entityId is not empty and not as 0
     * @param entityType is 1 for user 2 for group
     * @param isRoom is 0 or 1
     * @param message edited message it cannot be empty
     */
    fun editMessage(messageIds: Long, entityId: String, entityType: Int,isRoom:Int,message:String){
        TroopMessenger.editMessage(messageIds,entityId,entityType,isRoom,message,clientCallBackListener)
    }
    /**
     * Event emit DELETE messages
     * @param localIds required not empty of list
     * @param entityId is not empty and not as 0
     * @param entityType is 1 for user 2 for group
     */
    fun deleteMessages(localIds: List<Long>,entityId: String,entityType: Int,isRoom: Int){
        TroopMessenger.deleteMessages(localIds,entityId,entityType,isRoom,clientCallBackListener)
    }
    /**
     * DB get all active members count in a group
     * @param groupId is not empty and not as 0
     */
    fun fetchGroupActiveMemberCount(groupId:Long):List<Int>{
        return TroopClient.fetchGroupActiveMemberCount(groupId.toString())
    }
    fun setTypingStatus(entityId: String, entityType: Int,status: Int) {
        TroopMessenger.setTyping(entityId,entityType,status)
    }
    fun fetchSelfGroupProfile(groupId: String):LiveData<GroupMembersModel>{
        return TroopClient.fetchSelfGroupMemberData(groupId)
    }
    private fun networkLostReturnObject():JSONObject{
        var jsonObject = JSONObject()
        jsonObject.put("success",false)
        jsonObject.put("message",ConstantValues.CHECK_NETWORK_ERROR_MESSAGE)
        return jsonObject
    }
    fun initializeSDKReturnObject():JSONObject{
        var jsonObject = JSONObject()
        jsonObject.put("success",false)
        jsonObject.put("message","SDK is not initialized")
        return jsonObject
    }
    fun logout(){
        TroopClient.logout()
    }
    fun fetchUserMedia(userId: String):LiveData<List<MediaModel>>{
        return TroopClient.fetchUserMedia(userId)
    }
    fun fetchGroupMedia(userId: String):LiveData<List<MediaModel>>{
        return TroopClient.fetchGroupMedia(userId)
    }
    /**
     * initiating call
     * @param participantsData list of Participant data
     * @param callType 1 for screen share 2 video call 3 audio call
     * @param handler for getting callback response
     */
    fun initiateCall(uids:List<String>,callType:Int,listener: CallBackListener){
        if (callSocket!=null && !callSocket!!.connected()){
            clientCallBackListener.tmError(networkLostReturnObject())
            return
        }
        if (uids.isEmpty()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message"," users can not be empty ")
            clientCallBackListener.tmError(jsonObject)
            return
        }
        if (!ACTIVE_CALL_ID.isNullOrEmpty()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message"," You cannot make another call ")
            clientCallBackListener.tmError(jsonObject)
            return
        }
        return TroopMessengerCall.initiateCall(uids,callType, listener)
    }

    /**
     * add new participants to exising calls
     * @param uId list of string should not be empty
     */
    fun addNewParticipantToCall(uId: List<String>){
        TroopMessengerCall.addUsersToCall(uId)
    }

    /**
     * when user accept the call
     * @param handler getting callback response
     */
    fun callAccept(listenr: CallBackListener){
        TroopMessengerCall.acceptCall(listenr)
    }
    /**
     * when user reject the call
     */
    fun rejectCall(){
        TroopMessengerCall.rejectCall()
    }
    /**
     * end the current call
     */
    fun endCall(){
        TroopMessengerCall.endCall()
    }

    /**
     * add audio / video track after user accept the call
     */
    fun addStreams(){
        TroopClient.addCallStreams()
    }

    fun fetchEglBaseContext():EglBase{
        return if (RoomClient.rootEglBase!=null) {
            RoomClient.rootEglBase!!
        }else{
            EglBase.create()
        }
    }

    fun fetchParticipantList():ArrayList<ParticipantUsers>{
        return TroopMessengerCall.fetchParticipants()
    }

    /**
     * you can create and pass the video capture for rending for self and other participants
     * mainly using this method for TV application no need for mobile app
     */
    fun setVideoCapture(capturer: VideoCapturer){
        usbVideoCapture = capturer
    }

    /**
     * checking weather mine is host of this call or not
     */
    fun mineCallHost():Boolean{
        return TroopMessengerCall.checkSelfHost()
    }

    /**
     * mute the self audio in call
     */
    fun selfMuteAudio(){
        TroopMessengerCall.muteMyTrack(ConstantValues.CallKinds.KIND_AUDIO)
    }
    /**
     * mute the self video in call
     */
    fun selfMuteVideo(){
        TroopMessengerCall.muteMyTrack(ConstantValues.CallKinds.KIND_VIDEO)
    }
    /**
     * un mute the self audio in call
     */
    fun selfUnMuteAudio(){
        TroopMessengerCall.unMuteMyTrack(ConstantValues.CallKinds.KIND_AUDIO)
    }
    /**
     * un mute the self video in call
     */
    fun selfUnMuteVideo(){
        TroopMessengerCall.unMuteMyTrack(ConstantValues.CallKinds.KIND_VIDEO)
    }
    /**
     * mute the particular call participants audio
     * @param userId should not be empty
     */
    fun muteUserAudio(userId: String){
        TroopMessengerCall.hostMuteAudio(userId)
    }
    /**
     * mute the particular call participants video
     * @param userId should not be empty
     */
    fun muteUserVideo(userId: String){
        TroopMessengerCall.hostMuteVideo(userId)
    }
    /**
     * un mute the audio request for particular call participant
     * @param userId should not be empty
     */
    fun audioUnMuteRequest(userId: String){
        TroopMessengerCall.hostUnMuteAudioVideoRequest(userId,1)
    }
    /**
     * un mute the video request for particular call participant
     * @param userId should not be empty
     */
    fun videoUnMuteRequest(userId: String){
        TroopMessengerCall.hostUnMuteAudioVideoRequest(userId,2)
    }
    /**
     * remove the particular participant from call
     * @param userId should not be empty
     */
    fun removeUserFromCall(userId: String){
        TroopMessengerCall.removeUser(userId)
    }
    /**
     * switch the camera
     */
    fun switchCamera(){
        TroopMessengerCall.switchCamera()
    }
    /**
     * add the video to existing audio call
     */
    fun switchAudioCallToVideoCall(){
        TroopMessengerCall.switchToVideoCall()
    }
    /**
     * make particular participant as host
     * @param userId should not be empty
     */
    fun makeUserAsCallHost(userId: String){
        TroopMessengerCall.updateHost(1,userId)
    }
    /**
     * remove particular participant as host
     * @param userId should not be empty
     */
    fun removeUserAsCallHost(userId: String){
        TroopMessengerCall.updateHost(0,userId)
    }
    /**
     * retry the participant
     * @param userId should not be empty
     */
    fun retryUser(userId: String){
        TroopMessengerCall.retryUser(userId)
    }
    /**
     * accept the participant join call user by host
     * @param userId should not be empty
     */
    fun acceptJoinCallPermission(userId: String){
        TroopMessengerCall.acceptRejectTheJoinCallPermission(userId,1)
    }
    /**
     * reject the participant join call user by host
     * @param userId should not be empty
     */
    fun rejectJoinCallPermission(userId: String){
        TroopMessengerCall.acceptRejectTheJoinCallPermission(userId,0)
    }
    /**
     * check weather call is active or not
     * @param callId should not be empty
     */
    fun isCallActive(callId:String):Boolean{
        return TroopMessengerCall.isCallActive(callId)
    }
}

