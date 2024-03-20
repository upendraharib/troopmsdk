package com.tvisha.trooponprime.lib

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.gson.Gson
import com.iq.atoms.data.base.ResponseResult
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APPLICATION_LOGIN_U_ID
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APPLICATION_TM_LOGIN_USER_ID
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APP_ACCESS_TOKEN
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APP_AUTHORIZATION_TOKEN
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APP_LOGIN_USER_MOBILE_COUNTRY
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APP_LOGIN_USER_MOBILE_NUMBER
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APP_LOGIN_USER_NAME
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.CALL_ACCESS_TOKEN
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.CALL_AUTHORIZATION_TOKEN
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.CALL_TM_USER_ID
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.CLIENT_IS_CONNECTED
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.baseAPIService
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.callAPIService
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.callSocket
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.clientCallBackListener
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.clientInitialized
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.dataBase
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.messengerSocket
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.sharedPreferences
import com.tvisha.trooponprime.lib.call.RoomClient
import com.tvisha.trooponprime.lib.clientModels.*
import com.tvisha.trooponprime.lib.clientModels.StoryModel
import com.tvisha.trooponprime.lib.database.*
import com.tvisha.trooponprime.lib.database.Contact
import com.tvisha.trooponprime.lib.database.model.*
import com.tvisha.trooponprime.lib.listeneres.ClientCallBackListener
import com.tvisha.trooponprime.lib.listeneres.SocketCallbackListener
import com.tvisha.trooponprime.lib.socket.TroopMessenger
import com.tvisha.trooponprime.lib.socket.TroopSocketClient
import com.tvisha.trooponprime.lib.utils.*
import com.tvisha.trooponprime.lib.utils.ConstantValues.TAG
import com.tvisha.trooponprime.lib.utils.ConstantValues.deviceId
import com.tvisha.trooponprime.lib.utils.Encryption
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.internal.wait
import org.json.JSONArray
import org.json.JSONObject


internal object TroopClient : SocketCallbackListener {
    val viewModelJob = Job()
    val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)
    val uiScope = CoroutineScope(Dispatchers.Main)

    fun logout(){
        destroyMessengerSocket()
        destroyCallSocket()
        var fcmToken = sharedPreferences!!.getString(SharePreferenceConstants.FCM_TOKEN,"")!!
        sharedPreferences!!.edit().clear().apply()
        APP_ACCESS_TOKEN = ""
        APPLICATION_TM_LOGIN_USER_ID = ""
        APP_AUTHORIZATION_TOKEN = ""
        APPLICATION_LOGIN_U_ID = ""
        APP_LOGIN_USER_MOBILE_NUMBER = ""
        APP_LOGIN_USER_NAME = ""
        APP_LOGIN_USER_MOBILE_COUNTRY = ""
        CLIENT_IS_CONNECTED = false
        TroopMessengerClient.CALL_AUTHORIZATION_TOKEN =""
        TroopMessengerClient.CALL_TM_USER_ID = ""
        TroopMessengerClient.convEntityId = ""
        TroopMessengerClient.convEntityType= 0
        GlobalScope.launch {
            deleteFcm(fcmToken)
        }
    }
    open fun messengerSocketInitialize(){
        if (messengerSocket==null){
            val opts = IO.Options()
            opts.reconnection = true
            opts.transports = arrayOf(WebSocket.NAME)
            opts.query = "user_id=$APPLICATION_TM_LOGIN_USER_ID&token=${ConstantValues.TOKEN}&platform=${ConstantValues.PLATFORM_ANDROID}&checksum=_zxtyuIopQwRt&at=$APP_AUTHORIZATION_TOKEN&uid=$APPLICATION_LOGIN_U_ID&m=$APP_LOGIN_USER_MOBILE_NUMBER&v=2"
            //opts.auth = singletonMap("access_token", APP_AUTHORIZATION_TOKEN)
            messengerSocket = IO.socket(ConstantValues.API_BASE_URL,opts)
            if (!messengerSocket!!.connected()){
                messengerSocket!!.connect()
            }
            //TroopSocketClient.setMessengerListener(this)
            TroopSocketClient.setSocketCallBackListener(this)
            TroopSocketClient.callOnMessengerSocketListeners(messengerSocket!!)
            callSocketInitialize()
        }
    }
    fun callSocketInitialize(){

        if (!sharedPreferences!!.getBoolean(SharePreferenceConstants.CALL_SDK_REGISTER_STATUS,false)){
            GlobalScope.launch {
                callRegister(APPLICATION_LOGIN_U_ID, APP_LOGIN_USER_NAME,APP_LOGIN_USER_MOBILE_NUMBER,
                    APP_LOGIN_USER_MOBILE_COUNTRY, clientCallBackListener)
            }
            return
        }

        CALL_AUTHORIZATION_TOKEN = sharedPreferences!!.getString(SharePreferenceConstants.CALL_AUTHORIZATION_TOKEN,"")!!
        CALL_TM_USER_ID = sharedPreferences!!.getString(SharePreferenceConstants.CALL_TM_USERID,"")!!
        if (callSocket==null){
            val opts = IO.Options()
            opts.reconnection = true
            opts.transports = arrayOf(WebSocket.NAME)
            opts.query = "access_token=$CALL_AUTHORIZATION_TOKEN&platform=${ConstantValues.CALL_PLATFORM_ANDROID}&v=2"
            //opts.auth = singletonMap("access_token",APP_AUTHORIZATION_TOKEN)
            callSocket = IO.socket(ConstantValues.CALL_API_BASE_URL,opts)
            if (!callSocket!!.connected()){
                callSocket!!.connect()
            }

            TroopSocketClient.callOnCallSocketListeners(callSocket!!)
        }
    }
    open fun destroyMessengerSocket(){
        TroopSocketClient.stopOnMessengerSocketListeners(messengerSocket!!)
        messengerSocket= null
    }
    open fun destroyCallSocket(){
        TroopSocketClient.stopOnCallSocketListeners(callSocket!!)
        callSocket=null
    }
    var isRegisterIsInProcessing = false
    suspend fun register(uid:String,name:String,mobileNumber:String,countryCode:String,listener: ClientCallBackListener){
        try {
            isRegisterIsInProcessing = true
            listener.loadingMessages(true)
            var response = baseAPIService!!.registerTm(ConstantValues.CLIENT_VERSION,
                ConstantValues.TOKEN,
                uid,
                name,
                mobileNumber,
                countryCode
            ).await()
            if (response != null && response.isSuccessful) {
                var jsonObject = Helper.stringToJsonObject(response.body()!!)
                if (jsonObject != null && jsonObject.optBoolean("success")) {
                    var edit = sharedPreferences!!.edit()
                    edit.putBoolean(SharePreferenceConstants.TM_USER_LOGIN_STATUS, true).apply()
                    edit.putString(
                        SharePreferenceConstants.TM_LOGIN_USER_ID,
                        jsonObject.optString("tm_user_id")
                    ).apply()
                    edit.putString(SharePreferenceConstants.TM_LOGIN_USER_NAME, name)
                    edit.putString(SharePreferenceConstants.TM_LOGIN_USER_MOBILE, mobileNumber)
                    edit.putString(SharePreferenceConstants.TM_LOGIN_USER_COUNTRY_CODE, countryCode)
                    edit.putString(SharePreferenceConstants.TM_LOGIN_USER_UID, uid)
                    edit.putString(SharePreferenceConstants.AUTHORIZATION_TOKEN, jsonObject.optString("authorization_token"))
                    APP_LOGIN_USER_MOBILE_NUMBER = mobileNumber
                    APPLICATION_LOGIN_U_ID = uid
                    APPLICATION_TM_LOGIN_USER_ID = jsonObject.optString("tm_user_id")
                    APP_LOGIN_USER_NAME = name
                    APP_AUTHORIZATION_TOKEN = jsonObject.optString("authorization_token")
                    edit.apply()
                    //syncContacts()
                    syncMessengerContacts()
                    var jsonObject = JSONObject()
                    jsonObject.put("success", true)
                    jsonObject.put("message", "Registered successfully")
                    listener.loginResponse(jsonObject)
                    messengerSocketInitialize()
                } else {
                    var jsonObject = JSONObject()
                    jsonObject.put("success", false)
                    jsonObject.put("message", response.message())
                    listener.loginResponse(jsonObject)
                }
            } else {
                var jsonObject = JSONObject()
                jsonObject.put("success", false)
                jsonObject.put("message", ConstantValues.SOMETHING_WENT_WRON_ERROR_MESSAGE)
                listener.loginResponse(jsonObject)
            }
        }catch (e:Exception){
            e.printStackTrace()
            var jsonObject = JSONObject()
            jsonObject.put("success", false)
            jsonObject.put("message", ConstantValues.SOMETHING_WENT_WRON_ERROR_MESSAGE)
            listener.loginResponse(jsonObject)
        }
    }
    suspend fun callRegister(uid:String,name:String,mobileNumber:String,countryCode:String,listener: ClientCallBackListener){
        try {
            isRegisterIsInProcessing = true
            var response = callAPIService!!.registerTm(ConstantValues.CLIENT_VERSION,
                ConstantValues.CALL_TOKEN,
                uid,
                name,
                mobileNumber,
                countryCode, "",""
            ).await()
            if (response != null && response.isSuccessful) {
                var jsonObject = Helper.stringToJsonObject(response.body()!!)
                if (jsonObject != null && jsonObject.optBoolean("success")) {
                    var edit = sharedPreferences!!.edit()
                    edit.putString(SharePreferenceConstants.CALL_TM_USERID,jsonObject.optJSONObject("data")!!.optString("tm_user_id"))
                    edit.putBoolean(SharePreferenceConstants.CALL_SDK_REGISTER_STATUS,true)
                    edit.putString(SharePreferenceConstants.CALL_AUTHORIZATION_TOKEN,jsonObject.optJSONObject("data")!!.optString("authorization_token"))
                    edit.apply()
                    callSocketInitialize()
                } else {
                    Log.e("call--> ", " success false "+response.body().toString())
                }
            } else {
                Log.e("call--> ", " somthing ")
            }
        }catch (e:Exception){
            Helper.printExceptions(e)
        }
    }

    private suspend fun syncContacts() {
        var result = baseAPIService!!.syncContacts(ConstantValues.CLIENT_VERSION,
            APPLICATION_TM_LOGIN_USER_ID,
            APP_AUTHORIZATION_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,"[]",deviceId).await()
        if (result.isSuccessful && result!=null){
            var response = Helper.stringToJsonObject(result.body()!!)!!
            if (response.optBoolean("success")){
                if (response.has("contacts") && response.optJSONArray("contacts")!!.length()>0){
                    val contactList : ArrayList<Contact> = ArrayList()
                    val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
                    var arrayLength =response.optJSONArray("contacts")!!.length()
                    for (i in 0 until arrayLength) {
                        var contactObject = response.optJSONArray("contacts")!!.optJSONObject(i)
                        var contact = Contact()
                        contact.id = contactObject.optLong("id")
                        contact.name = contactObject.optString("name")
                        contact.uid = contactObject.optString("uid")
                        contact.created_at= updatedAtTime!!
                        contact.updated_at = updatedAtTime!!
                        contactList.add(contact)
                    }
                    dataBase!!.contactDAO.insertBulkContacts(contactList)
                }
                if (response.has("deleted_contact_names") && response.optJSONArray("deleted_contact_names")!!.length()>0){
                    var deleteContactLength =response.optJSONArray("deleted_contact_names")!!.length()
                    for (i in 0 until deleteContactLength){
                        dataBase!!.userDAO.updateUserName(response.optJSONArray("deleted_contact_names")!!.optJSONObject(i).optString("user_id"),response.optJSONArray("deleted_contact_names")!!.optJSONObject(i).optString("name"))
                    }
                }
            }
        }
    }
    private suspend fun syncMessengerContacts() {
        var result = baseAPIService!!.fetchMessengerContacts(ConstantValues.CLIENT_VERSION,
            APPLICATION_TM_LOGIN_USER_ID,
            APP_AUTHORIZATION_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,deviceId).await()
        clientCallBackListener.loadingMessages(true)
        if (result.isSuccessful && result!=null){
            var response = Helper.stringToJsonObject(result.body()!!)!!
            var groupMembersArray = JSONArray()
            if (response.optBoolean("success")){
                var userList :ArrayList<User> = ArrayList()
                var groupList :ArrayList<MessengerGroup> = ArrayList()
                var groupMemberList :ArrayList<GroupMembers> = ArrayList()
                var responseData = response.optJSONArray("data")
                if (responseData!=null && responseData.length()>0){
                    var lengthOfData = responseData.length()
                    for (i in 0 until lengthOfData){
                        var jsonObject = responseData.optJSONObject(i)
                        if (jsonObject.optInt("entity_type")==ConstantValues.Entity.USER){
                            var user = User()
                            user.uid = jsonObject.optString("uid")
                            user.user_id = jsonObject.optLong("entity_id")
                            user.name = jsonObject.optString("entity_name")
                            user.is_archived = jsonObject.optInt("is_archived")
                            user.is_blocked = jsonObject.optInt("is_blocked")
                            user.blocked_me = jsonObject.optInt("blocked_me")
                            user.mobile = jsonObject.optString("mobile")
                            user.country_code = jsonObject.optString("country_code")
                            user.profile_pic = jsonObject.optString("profile_pic")
                            user.about = jsonObject.optString("about")
                            userList.add(user)
                        }else if (jsonObject.optInt("entity_type")==ConstantValues.Entity.GROUP){
                            var messengerGroup = MessengerGroup()
                            messengerGroup.group_id = jsonObject.optLong("entity_id")
                            messengerGroup.group_type =jsonObject.optInt("group_type")
                            messengerGroup.group_name = jsonObject.optString("entity_name")
                            messengerGroup.group_avatar = jsonObject.optString("entity_avatar")
                            messengerGroup.created_by = jsonObject.optLong("group_admin")
                            messengerGroup.is_active = 1
                            messengerGroup.group_description = jsonObject.optString("group_description")
                            messengerGroup.created_at = jsonObject.optString("created_at")
                            messengerGroup.is_archived = jsonObject.optInt("is_archived")
                            groupList.add(messengerGroup)
                            var groupMemberLength =jsonObject.optJSONArray("group_members")!!.length()

                            for (gm in 0 until groupMemberLength){
                                var groupMemberObject = jsonObject.optJSONArray("group_members")!!.optJSONObject(gm)
                                Log.e("members===> ",""+groupMemberObject.toString())
                                var groupMembers = GroupMembers()
                                groupMembers.group_id = jsonObject.optLong("entity_id")
                                groupMembers.user_id = groupMemberObject.optLong("member_id")
                                groupMembers.user_status = groupMemberObject.optInt("member_status")
                                groupMembers.user_role = groupMemberObject.optInt("member_role")
                                groupMembersArray.put(groupMemberObject)
                                groupMemberList.add(groupMembers)
                            }
                        }
                    }
                    if (userList.isNotEmpty()){
                        dataBase!!.userDAO.insertAllUsers(userList)
                    }
                    if (groupList.isNotEmpty()){
                        dataBase!!.messengerGroupDAO.insertBulkMessengerGroup(groupList)
                    }
                    if (groupMemberList.isNotEmpty()){
                        dataBase!!.groupMembersDAO.insertAllData(groupMemberList)
                    }
                    bulckAddGroupMembers(groupMembersArray)
                }
                fetchAllMessages()
                fetchAllStories()
                //fetchAllBroadCastMessages()
            }
        }
    }
    private suspend fun fetchAllMessages(){
        ioScope.launch {
            clientCallBackListener.loadingMessages(true)
            var responseResult = baseAPIService!!.fetchAllMessages(ConstantValues.CLIENT_VERSION,APPLICATION_TM_LOGIN_USER_ID,APP_AUTHORIZATION_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,ConstantValues.deviceId).await()
            uiScope.launch {
                if (responseResult.isSuccessful) {
                    var response = Helper.stringToJsonObject(responseResult.body()!!)
                    if (response != null && response.optBoolean("success")) {
                        if (response.has("messages") && response.optJSONArray("messages")!!.length()>0){
                            TroopSocketClient.messageInsertOrUpdate(response.optJSONArray("messages")!!,ConstantValues.SocketOn.SYNC)
                        }
                    }
                }else{
                    //ResponseResult.Error(response.message())
                    isRegisterIsInProcessing = false
                }

            }
        }
    }
    private suspend fun fetchAllStories(){
        ioScope.launch {
            var responseResult = baseAPIService!!.fetchAllStories(ConstantValues.CLIENT_VERSION,APPLICATION_TM_LOGIN_USER_ID,APP_AUTHORIZATION_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,ConstantValues.deviceId).await()
            if (responseResult.isSuccessful && responseResult!=null) {
                var response = Helper.stringToJsonObject(responseResult.body()!!)!!
                if (response!=null && response.optBoolean("success")){
                    var storiesData = response.optJSONArray("data")
                    var storyList:ArrayList<Story> = ArrayList()
                    var storyLength = storiesData.length()
                    for (i in 0 until storyLength){
                        var jsonObject = storiesData.optJSONObject(i)
                        var story = Gson().fromJson(jsonObject.toString(),Story::class.java)
                        //story.status = 1
                        story.status =  if (jsonObject.optInt("seen")==1) ConstantValues.StoryStatus.VIEWED else if (jsonObject.optString("seen").trim().isNullOrEmpty()) ConstantValues.StoryStatus.VIEWED else ConstantValues.StoryStatus.UNSEEN
                        storyList.add(story)
                    }
                    if (storyList.isNotEmpty()){
                        dataBase!!.storyDAO.insertBulkStories(storyList)
                    }
                }
            }
        }

    }
    private suspend fun fetchAllBroadCastMessages(){
        ioScope.launch {
            var responseResult = baseAPIService!!.fetchAllBroadCastMessages(ConstantValues.CLIENT_VERSION,APPLICATION_TM_LOGIN_USER_ID,ConstantValues.TOKEN,APP_AUTHORIZATION_TOKEN).await()
            if (responseResult.isSuccessful && responseResult!=null) {
                var response = Helper.stringToJsonObject(responseResult.body()!!)!!
                if (response!=null && response.optBoolean("success")){
                    var broadCastDataLength = response.optJSONArray("data")!!.length()
                    var broadCastMessageList:ArrayList<BroadCast> = ArrayList()
                    for (i in 0 until broadCastDataLength){
                        broadCastMessageList.add(Gson().fromJson(response.optJSONArray("data")!!.optJSONObject(i).toString(),BroadCast::class.java))
                    }
                    if (broadCastMessageList.isNotEmpty()){
                        dataBase!!.broadCastDAO.insertBulkBroadCast(broadCastMessageList)
                    }
                }
            }
        }
    }
    internal suspend fun saveContacts(contacts:JSONArray):JSONObject{
        var result = baseAPIService!!.syncContacts(ConstantValues.CLIENT_VERSION,APPLICATION_TM_LOGIN_USER_ID, APP_AUTHORIZATION_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,contacts.toString(),deviceId).await()
        if (result.isSuccessful && result!=null){
            var response = Helper.stringToJsonObject(result.body()!!)!!
            if (response.optBoolean("success")){
                if (response.has("contacts") && response.optJSONArray("contacts")!!.length()>0){
                    val contactList : ArrayList<Contact> = ArrayList()
                    val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
                    var contactLength = response.optJSONArray("contacts")!!.length()
                    Log.e("data===> "," contacts "+response.optJSONArray("contacts").toString())
                    for (i in 0 until contactLength) {
                        var contactObject = response.optJSONArray("contacts")!!.optJSONObject(i)
                        var contact = Contact()
                        contact.id = contactObject.optLong("id")
                        contact.name = contactObject.optString("name")
                        contact.uid = contactObject.optString("uid")
                        contact.created_at= updatedAtTime!!
                        contact.updated_at = updatedAtTime!!
                        contactList.add(contact)
                    }
                    dataBase!!.contactDAO.insertBulkContacts(contactList)
                }
                if (response.has("deleted_contact_names") && response.optJSONArray("deleted_contact_names")!!.length()>0){
                    var deleteContactNameLength = response.optJSONArray("deleted_contact_names")!!.length()
                    for (i in 0 until deleteContactNameLength){
                        dataBase!!.userDAO.updateUserName(response.optJSONArray("deleted_contact_names")!!.optJSONObject(i).optString("user_id"),response.optJSONArray("deleted_contact_names")!!.optJSONObject(i).optString("name"))
                    }
                }
            }
        }
        return JSONObject()
    }
    internal suspend fun fetchChatList(conversationUserId:String){
        ioScope.launch {
            var result = baseAPIService!!.fetchChatList(ConstantValues.CLIENT_VERSION,messengerSocket!!.id(),APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,conversationUserId).await()
            uiScope.launch {
                if (result.isSuccessful && result!=null) {
                    var response = Helper.stringToJsonObject(result.body()!!)!!

                }
            }
        }


    }
    internal suspend fun createGroup(groupName:String,groupAvatar:String,groupDescription: String,conversationUserId:String,members: JSONArray):JSONObject{
        return try {
            var responseObject = JSONObject()
            if (groupName.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Group Name is required")
                return responseObject
            }
            if (members.length()==0){
                responseObject.put("success",false)
                responseObject.put("message","Group members are required")
                return responseObject
            }
            var response = baseAPIService!!.createGroup(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,groupName,groupDescription,groupAvatar,members.toString(),ConstantValues.PLATFORM_ANDROID,conversationUserId).await()
            if (response.isSuccessful && response!=null) {
                responseObject.put("success",true)
                return responseObject
            }else{
                responseObject.put("success",true)
                responseObject.put("message",response.message())
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun updateGroupName(groupId:String,groupName: String):JSONObject{
        return try {
            var responseObject = JSONObject()
            if (groupName.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid Group Name")
                return responseObject
            }
            if (groupId.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid Group")
                return responseObject
            }
            var response = baseAPIService!!.updateGroupName(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                ConstantValues.GroupUpdatePayLoads.PAYLOAD_CHANGE_NAME,groupId,APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,groupName,ConstantValues.PLATFORM_ANDROID).await()
            if (response.isSuccessful && response!=null) {
                return responseObject
            }else{
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun updateGroupAvtar(groupAvatar: String,groupId: String):JSONObject{
        return try {
            var responseObject = JSONObject()
            if (groupId.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid Group")
                return responseObject
            }
            var response = baseAPIService!!.updateGroupAvatar(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                ConstantValues.GroupUpdatePayLoads.PAYLOAD_CHANGE_AVATAR,groupId,APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,groupAvatar,ConstantValues.PLATFORM_ANDROID).await()
            if (response.isSuccessful && response!=null) {
                return responseObject
            }else{
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun makeGroupAdmin(groupId: String,memberId:String):JSONObject{
        var responseObject = JSONObject()
        if (groupId.isNullOrEmpty()){
            responseObject.put("success",false)
            responseObject.put("message","Invalid Group")
            return responseObject
        }
        if (memberId.isNullOrEmpty()){
            responseObject.put("success",false)
            responseObject.put("message","Invalid member")
            return responseObject
        }
        return updateGroupMemberRole(groupId,memberId,ConstantValues.GroupValues.ADMIN)
    }
    internal suspend fun makeGroupModerator(groupId: String,memberId:String):JSONObject{
        var responseObject = JSONObject()
        if (groupId.isNullOrEmpty()){
            responseObject.put("success",false)
            responseObject.put("message","Invalid Group")
            return responseObject
        }
        if (memberId.isNullOrEmpty()){
            responseObject.put("success",false)
            responseObject.put("message","Invalid member")
            return responseObject
        }
        return updateGroupMemberRole(groupId,memberId,ConstantValues.GroupValues.MODERATOR)
    }
    internal suspend fun removeGroupAdmin(groupId: String,memberId:String):JSONObject{
        var responseObject = JSONObject()
        if (groupId.isNullOrEmpty()){
            responseObject.put("success",false)
            responseObject.put("message","Invalid Group")
            return responseObject
        }
        if (memberId.isNullOrEmpty()){
            responseObject.put("success",false)
            responseObject.put("message","Invalid member")
            return responseObject
        }
        return updateGroupMemberRole(groupId,memberId,ConstantValues.GroupValues.MEMBER)
    }
    internal suspend fun removeGroupModerator(groupId: String,memberId:String):JSONObject{
        var responseObject = JSONObject()
        if (groupId.isNullOrEmpty()){
            responseObject.put("success",false)
            responseObject.put("message","Invalid Group")
            return responseObject
        }
        if (memberId.isNullOrEmpty()){
            responseObject.put("success",false)
            responseObject.put("message","Invalid member")
            return responseObject
        }
        return updateGroupMemberRole(groupId,memberId,ConstantValues.GroupValues.MEMBER)
    }
    private suspend fun updateGroupMemberRole(groupId: String,memberId: String,memberRole:String):JSONObject{
        return try {
            var response = baseAPIService!!.updateGroupUserRole(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                ConstantValues.GroupUpdatePayLoads.PAYLOAD_CHANGE_ROLE,groupId,memberId,APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,memberRole,ConstantValues.PLATFORM_ANDROID).await()
            if (response.isSuccessful && response!=null) {
                return Helper.stringToJsonObject(response.body())!!
            }else{
                var responseObject = JSONObject()
                responseObject.put("success",false)
                responseObject.put("message",ConstantValues.SOMETHING_WENT_WRON_ERROR_MESSAGE)
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun removeGroupUser(groupId: String,memberId: String):JSONObject{
        return try {
            var responseObject = JSONObject()
            if (groupId.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid Group")
                return responseObject
            }
            if (memberId.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid member")
                return responseObject
            }
            var response = baseAPIService!!.removeGroupUser(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                ConstantValues.GroupUpdatePayLoads.PAYLOAD_REMOVE_MEMBERS,groupId,APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,memberId,ConstantValues.PLATFORM_ANDROID).await()
            if (response.isSuccessful && response!=null) {
                responseObject.put("success",true)
                responseObject.put("message","Removed user successful")
                return responseObject
            }else{
                responseObject.put("success",false)
                responseObject.put("message",ConstantValues.SOMETHING_WENT_WRON_ERROR_MESSAGE)
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun deleteGroup(groupId: String):JSONObject{
        return try {
            var responseObject = JSONObject()
            if (groupId.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid Group")
                return responseObject
            }

            var response = baseAPIService!!.deleteGroup(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                ConstantValues.GroupUpdatePayLoads.PAYLOAD_GROUP_DELETE,groupId,APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,"1",ConstantValues.PLATFORM_ANDROID).await()
            if (response.isSuccessful && response!=null) {
                responseObject.put("success",true)
                responseObject.put("message","Deleted group successfully")
                return responseObject
            }else{
                responseObject.put("success",false)
                responseObject.put("message",ConstantValues.SOMETHING_WENT_WRON_ERROR_MESSAGE)
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun exitGroup(groupId: String):JSONObject{
        return try {
            var responseObject = JSONObject()
            if (groupId.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid Group")
                return responseObject
            }
            var response = baseAPIService!!.exitGroup(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                ConstantValues.GroupUpdatePayLoads.PAYLOAD_GROUP_EXIT,groupId,APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID).await()
            if (response.isSuccessful && response!=null) {
                responseObject.put("success",true)
                responseObject.put("message","Exited from group successfully")
                return responseObject
            }else{
                responseObject.put("success",false)
                responseObject.put("message",ConstantValues.SOMETHING_WENT_WRON_ERROR_MESSAGE)
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun addGroupMembers(groupId: String,members: JSONArray):JSONObject{
        return try {
            var responseObject = JSONObject()
            if (groupId.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid Group")
                return responseObject
            }
            if (members.length()==0){
                responseObject.put("success",false)
                responseObject.put("message","Group members are required")
                return responseObject
            }
            var response = baseAPIService!!.addGroupMembers(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                groupId,APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,members,ConstantValues.PLATFORM_ANDROID).await()
            if (response.isSuccessful && response!=null) {
                var responseObject = Helper.stringToJsonObject(response.body()!!)!!
                return responseObject
            }else{
                responseObject.put("success",false)
                responseObject.put("message",ConstantValues.SOMETHING_WENT_WRON_ERROR_MESSAGE)
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun createRoom(roomName:String,roomDescription: String,roomAvatar:String,conversationReferenceId:String):JSONObject{
        return try {
            var responseObject = JSONObject()
            if (roomName.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Room Name is required")
                return responseObject
            }
            if (roomName.length>ConstantValues.MAX_GROUP_NAME_CHARACTERS){
                responseObject.put("success",false)
                responseObject.put("message","Maximum ${ConstantValues.MAX_GROUP_NAME_CHARACTERS} characters are allowed for Group Name")
                return responseObject
            }

            var responseResult = baseAPIService!!.createRoom(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,conversationReferenceId,roomName,roomDescription,roomAvatar).await()
            if (responseResult.isSuccessful && responseResult!=null) {
                var response = Helper.stringToJsonObject(responseResult.body()!!)!!
                if (response!=null && response.optBoolean("success")){

                }
                //ResponseResult.Success(response.body()!!)
                return JSONObject()
            }else{
                return JSONObject()
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun updateRoomName(roomId:String,roomName: String):JSONObject{
        return try {
            var responseObject = JSONObject()
            if (roomId.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid Room")
                return responseObject
            }
            if (roomName.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Room Name is required")
                return responseObject
            }
            if (roomName.length>ConstantValues.MAX_GROUP_NAME_CHARACTERS){
                responseObject.put("success",false)
                responseObject.put("message","Maximum ${ConstantValues.MAX_GROUP_NAME_CHARACTERS} characters are allowed for Group Name")
                return responseObject
            }
            var response = baseAPIService!!.updateRoomName(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,ConstantValues.RoomPayloads.ROOM_NAME_CHANGE,roomName,roomId).await()
            if (response.isSuccessful && response!=null) {
                return responseObject
            }else{
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun updateRoomAvatar(roomId:String,roomAvatar: String):JSONObject{
        return try {

            var responseObject = JSONObject()
            if (roomId.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid Room")
                return responseObject
            }

            var response = baseAPIService!!.updateRoomAvatar(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,ConstantValues.RoomPayloads.ROOM_AVATAR_CHANGE,roomId,roomAvatar).await()
            if (response.isSuccessful && response!=null) {
                return responseObject
            }else{
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun deleteRoom(roomId:String):JSONObject{
        return try {
            var responseObject = JSONObject()
            if (roomId.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid Room")
                return responseObject
            }

            var response = baseAPIService!!.deleteRoom(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,ConstantValues.RoomPayloads.ROOM_DELETE,roomId).await()
            if (response.isSuccessful && response!=null) {
                return responseObject
            }else{
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun exitRoom(roomId:String):JSONObject{
        return try {

            var responseObject = JSONObject()
            if (roomId.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid Room")
                return responseObject
            }

            var response = baseAPIService!!.exitRoom(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,ConstantValues.RoomPayloads.ROOM_EXIT,roomId).await()
            if (response.isSuccessful && response!=null) {
                return responseObject
            }else{
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun fetchRoomDetails(roomId:String):JSONObject{
        return try {

            var responseObject = JSONObject()
            if (roomId.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid Room")
                return responseObject
            }

            var response = baseAPIService!!.fetchRoomDetails(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,roomId).await()
            if (response.isSuccessful && response!=null) {
                return responseObject
            }else{
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun joinRoom(roomId:String):JSONObject{
        return try {

            var responseObject = JSONObject()
            if (roomId.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid Room")
                return responseObject
            }

            var response = baseAPIService!!.joinRoom(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,ConstantValues.RoomPayloads.JOIN_ROOM,roomId,APPLICATION_TM_LOGIN_USER_ID).await()
            if (response.isSuccessful && response!=null) {
                return responseObject
            }else{
                return responseObject
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }
    internal suspend fun fetchRoomList():JSONObject{
        return try {
            var response = baseAPIService!!.fetchRooms(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,APP_ACCESS_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID).await()
            if (response.isSuccessful && response!=null) {
                return Helper.stringToJsonObject(response.body()!!)!!
            }else{
                return JSONObject()
            }
        }catch (e:Exception){
            return JSONObject()
        }
    }

    internal suspend fun updateFcm(fcmToken:String):ResponseResult<String>{
        return try {
            var response = baseAPIService!!.updateFCMToken(ConstantValues.CLIENT_VERSION,APPLICATION_TM_LOGIN_USER_ID,ConstantValues.TOKEN,"1",fcmToken).await()
            if (response.isSuccessful && response!=null) {
                ResponseResult.Success(response.body()!!)
            }else{

                ResponseResult.Error(response.message())
            }
        }catch (e:Exception){
            ResponseResult.Error("")
        }
    }
    internal suspend fun deleteFcm(fcmToken:String):ResponseResult<String>{
        return try {
            var response = baseAPIService!!.deleteFCMToken(ConstantValues.CLIENT_VERSION,APPLICATION_TM_LOGIN_USER_ID,ConstantValues.TOKEN,"0",fcmToken).await()
            if (response.isSuccessful && response!=null) {
                ResponseResult.Success(response.body()!!)
            }else{

                ResponseResult.Error(response.message())
            }
        }catch (e:Exception){
            ResponseResult.Error("")
        }
    }
    internal suspend fun updateUserProfilePic(profilePic:String):JSONObject{
        return try {
            var response = baseAPIService!!.updateProfilePic(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,ConstantValues.TOKEN,APP_AUTHORIZATION_TOKEN,APP_ACCESS_TOKEN,profilePic).await()
            if (response.isSuccessful && response!=null) {
                var responses = Helper.stringToJsonObject(response.body()!!)
                if (responses!=null && responses.optBoolean("success")){
                    dataBase!!.userDAO.updateUserProfileAvatar(APPLICATION_TM_LOGIN_USER_ID,profilePic,ConstantValues.fetchCurrentTimeInUTC())
                }
                return Helper.stringToJsonObject(response.body()!!)!!
                //ResponseResult.Success(response.body()!!)
            }else{
                return Helper.stringToJsonObject(response.body()!!)!!
                //ResponseResult.Error(response.message())
            }
        }catch (e:Exception){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message",ConstantValues.SOMETHING_WENT_WRON_ERROR_MESSAGE)
            return jsonObject
            //ResponseResult.Error("")
        }
    }
    internal suspend fun fetchUserProfileBuUid(initiatorUid:String):ResponseResult<String>{
        return try {
            var response = baseAPIService!!.fetchUserProfilePicByUid(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                initiatorUid,ConstantValues.TOKEN,APP_AUTHORIZATION_TOKEN,APP_ACCESS_TOKEN).await()
            if (response.isSuccessful && response!=null) {
                ResponseResult.Success(response.body()!!)
            }else{

                ResponseResult.Error(response.message())
            }
        }catch (e:Exception){
            ResponseResult.Error("")
        }
    }

    internal suspend fun addContact(uid:String,name: String):JSONObject{
        var responseObject = JSONObject()
        return try {
            if (uid.isNullOrEmpty() || name.isNullOrEmpty()){
                responseObject.put("success",false)
                responseObject.put("message","Invalid data")
                return responseObject
            }
            var response = baseAPIService!!.addNewContact(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,APP_AUTHORIZATION_TOKEN,APP_ACCESS_TOKEN,ConstantValues.TOKEN,uid,name,deviceId).await()
            if (response.isSuccessful && response!=null) {
                var responseData = Helper.stringToJsonObject(response.body()!!)!!
                if (responseData.optBoolean("success")){
                    var contact = Contact()
                    contact.name = name
                    contact.uid = uid
                    contact.created_at = ConstantValues.fetchCurrentTimeInUTC()
                    dataBase!!.contactDAO.insertContact(contact)
                    if (responseData.has("user")){
                        var userObject = responseData.optJSONObject("user")
                        if (dataBase!!.userDAO.checkUserExists(userObject!!.optString("user_id"))<=0){
                            var user = Gson().fromJson(userObject!!.toString(),User::class.java)
                            user.updated_at = ConstantValues.fetchCurrentTimeInUTC()
                            dataBase!!.userDAO.insert(user)
                        }
                    }
                }
                responseObject.put("success",true)
                return responseObject
            }else{
                responseObject.put("success",false)
                responseObject.put("message",response.message())
                return responseObject
            }
        }catch (e:Exception){
            responseObject.put("success",false)
            responseObject.put("message",ConstantValues.SOMETHING_WENT_WRON_ERROR_MESSAGE)
            return responseObject
        }
    }
    //database queries
    fun saveContact(contactName:String,entityId:String):JSONObject{
        if (contactName.isNullOrEmpty()){
            var jsonObject = JSONObject()
            jsonObject.put("success",false)
            jsonObject.put("message","Contact Name is required")
            return jsonObject
        }
        dataBase!!.userDAO.updateUserName(entityId,contactName)
        var uid = dataBase!!.userDAO.fetchUid(entityId)
        if (!uid.isNullOrEmpty()){
            var contact = Contact()
            contact.uid = uid.toString()
            contact.name = contactName
            dataBase!!.contactDAO.insertContact(contact)
        }
        var jsonObject = JSONObject()
        jsonObject.put("success",true)
        jsonObject.put("message","Contact Saved")
        return jsonObject
    }
    fun fetchRecentList():LiveData<List<RecentMessageList>>{
        var query = "SELECT 0 as isTyping,'' as typingData,0 as isSelected,0 as isArchive,last_message_local_id as id,last_message_id as message_id,last_message as message,last_message_type as message_type,last_message_status as message_status,last_message_by_me as is_sender, unread_messages_count,last_message_sender_name as sender_name,last_message_sender_id as  sender_id,user_id as entity_id,1 as entity,name as entity_name,uid as entity_uid,profile_pic as entity_avatar,last_message_time as message_time FROM user WHERE last_message_id !=0 AND is_archived = 0 UNION ALL SELECT 0 as isTyping,'' as typingData,0 as isSelected,0 as isArchive,last_message_local_id as id,last_message_id as message_id,last_message as message,last_message_type as message_type,last_message_status as message_status,last_message_by_me as is_sender, unread_messages_count,last_message_sender_name as sender_name,last_message_sender_id as sender_id,group_id as entity_id,2 as entity,group_name as entity_name,0 as entity_uid,group_avatar as entity_avatar,last_message_time as message_time FROM chat_group WHERE last_message_id !=0 AND is_active = 1 AND is_archived = 0 ORDER BY message_time DESC"
        //var query = "SELECT last_message_local_id as id,last_message_id as message_id,last_message as message,last_message_type as message_type,last_message_status as message_status,last_message_by_me as is_sender, unread_messages_count,last_message_sender_name as sender_name,last_message_sender_id as  sender_id,user_id as entity_id,1 as entity,name as entity_name,uid as entity_uid,profile_pic as entity_avatar,last_message_time as message_time FROM user WHERE last_message_local_id!=0 AND is_archived = 0 UNION SELECT last_message_local_id as id,last_message_id as message_id,last_message as message,last_message_type as message_type,last_message_status as message_status,last_message_by_me as is_sender, unread_messages_count,last_message_sender_name as sender_name,last_message_sender_id as sender_id,group_id as entity_id,2 as entity,group_name as entity_name,group_avatar as entity_avatar,last_message_time as message_time,0 as entity_uid FROM chat_group WHERE last_message_local_id!=0 AND is_active = 1 AND is_archived = 0 ORDER BY message_time DESC"
        return dataBase!!.messengerDAO.fetchRecentMessageList(SimpleSQLiteQuery(query))
    }
    fun fetchArchiveList():LiveData<List<RecentMessageList>>{
        var query = "SELECT 0 as isTyping,'' as typingData,0 as isSelected,1 as isArchive,last_message_local_id as id,last_message_id as message_id,last_message as message,last_message_type as message_type,last_message_status as message_status,last_message_by_me as is_sender, unread_messages_count,last_message_sender_name as sender_name,last_message_sender_id as  sender_id,user_id as entity_id,1 as entity,name as entity_name,uid as entity_uid,profile_pic as entity_avatar,last_message_time as message_time FROM user WHERE last_message_id !=0 AND is_archived = 1 UNION ALL SELECT 0 as isTyping,'' as typingData,0 as isSelected,1 as isArchive,last_message_local_id as id,last_message_id as message_id,last_message as message,last_message_type as message_type,last_message_status as message_status,last_message_by_me as is_sender, unread_messages_count,last_message_sender_name as sender_name,last_message_sender_id as sender_id,group_id as entity_id,2 as entity,group_name as entity_name,0 as entity_uid,group_avatar as entity_avatar,last_message_time as message_time FROM chat_group WHERE last_message_id !=0 AND is_active = 1 AND is_archived = 1 ORDER BY message_time DESC"
        //var query = "SELECT last_message_local_id as id,last_message_id as message_id,last_message as message,last_message_type as message_type,last_message_status as message_status,last_message_by_me as is_sender, unread_messages_count,last_message_sender_name as sender_name,last_message_sender_id as  sender_id,user_id as entity_id,1 as entity,name as entity_name,uid as entity_uid,profile_pic as entity_avatar,last_message_time as message_time FROM user WHERE  last_message_local_id!=0 AND is_archived = 1 UNION SELECT last_message_local_id as id,last_message_id as message_id,last_message as message,last_message_type as message_type,last_message_status as message_status,last_message_by_me as is_sender, unread_messages_count,last_message_sender_name as sender_name,last_message_sender_id as sender_id,group_id as entity_id,2 as entity,group_name as entity_name,group_avatar as entity_avatar,last_message_time as message_time,0 as entity_uid FROM chat_group WHERE  last_message_local_id!=0 AND is_active = 1 AND is_archived = 1 ORDER BY message_time DESC"
        return dataBase!!.messengerDAO.fetchArchivedList(SimpleSQLiteQuery(query))
    }
    fun fetchContactList():List<UserClientModel>{
        return dataBase!!.userDAO.fetchContacts()
    }
    fun fetchForwardList():LiveData<List<ForwardModel>>{
        var query = "SELECT 0 as isSelected,user_id as entity_id,1 as entity,uid as entity_uid,name as entity_name,profile_pic as entity_avatar,last_message_time as message_time FROM user as u WHERE u.last_message_local_id !=0 AND u.is_blocked = 0 UNION ALL SELECT 0 as isSelected,group_id as entity_id,2 as entity,'0' as entity_uid,group_name as entity_name,group_avatar as entity_avatar,last_message_time as message_time FROM chat_group as g WHERE g.last_message_local_id!=0 AND g.is_active = 1 ORDER BY message_time DESC"
        return dataBase!!.messengerDAO.fetchForwardList(SimpleSQLiteQuery(query))
    }
    fun fetchConversation(userId:String,entityId:String,entity:Int,viewModel:ViewModel): Flow<PagingData<ConversationModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = 1000,
                prefetchDistance = 50,
                enablePlaceholders = false,
                initialLoadSize = 1000,
                maxSize = 200000
            )
        ) {
            var query = ""
            if (entity==ConstantValues.Entity.USER || entity==ConstantValues.Entity.IS_USER){
                query = "SELECT m.is_edited,m.edited_at,m.is_like,0 as isSelected,isMine as is_sender,m.local_attachment_path,m.ID,LOWER(replace(m.attachment,rtrim(m.attachment,replace(m.attachment,'.','')),'')) as attachment_extension,m.original_message,m.is_forkout,m.is_forward,m.preview_link,m.status,m.message_id,CASE WHEN status = 2 THEN '' ELSE m.message END as message,m.sender_id,m.receiver_id,m.message_type,m.is_reply,m.original_message_id,m.attachment,m.is_read,m.is_delivered,m.is_sync,m.caption,m.is_group,m.created_at,m.attachment_downloaded,m.attachment_path,'' as member_name FROM chat as m WHERE (m.sender_id=$userId AND receiver_id=$entityId AND m.is_group=0) or (m.sender_id = $entityId AND m.receiver_id=$userId AND m.is_group=0) GROUP BY m.ID HAVING m.status IN (1,2) ORDER BY m.created_at DESC,m.message_id DESC"
            }else if (entity==ConstantValues.Entity.GROUP || entity==ConstantValues.Entity.IS_GROUP){
                query = "SELECT m.is_edited,m.edited_at,m.is_like,0 as isSelected,isMine as is_sender,m.ID,m.local_attachment_path,LOWER(replace(m.attachment,rtrim(m.attachment,replace(m.attachment,'.','')),'')) as attachment_extension,m.original_message,m.is_forkout,m.is_forward,m.preview_link,m.status,m.message_id,CASE WHEN status = 2 THEN '' ELSE m.message END as message,m.sender_id,m.receiver_id,m.message_type,m.is_reply,m.original_message_id,m.attachment,m.is_read,m.is_delivered,m.is_sync,m.caption,m.is_group,m.created_at,m.attachment_downloaded,m.attachment_path,u.name as member_name FROM chat as m LEFT JOIN user as u ON m.sender_id = u.user_id LEFT JOIN group_member as mgm ON mgm.group_id=m.receiver_id AND mgm.user_id = m.sender_id WHERE (m.receiver_id=$entityId AND m.is_group=1) or (m.sender_id = $userId AND m.receiver_id=$entityId AND m.is_group=1) GROUP BY m.ID HAVING m.status IN (1,2) ORDER BY m.created_at DESC,m.message_id DESC"
            }
             dataBase!!.messengerDAO.fetchConversationMessages(SimpleSQLiteQuery(query))
        }.flow.map { pagingData ->
            pagingData.map { cheese -> cheese }
        }.cachedIn(viewModel!!.viewModelScope).flowOn(Dispatchers.IO)
    }
    fun fetchLiveRecentBroadCastMessage():LiveData<List<BroadCastMessage>>{
        return dataBase!!.broadCastDAO.fetchLiveRecentBroadCastMessage()
    }
    fun fetchRecentBroadCastMessage():BroadCastMessage{
        return dataBase!!.broadCastDAO.fetchRecentBroadCastMessage()
    }
    fun fetchBroadCastMessages():LiveData<List<BroadCastMessage>>{
        return dataBase!!.broadCastDAO.fetchAllBroadCastMessages()
    }
    fun fetchGroupDetails(groupId:String):MessengerGroupModel{
        return dataBase!!.messengerGroupDAO.fetchGroupData(groupId)
    }
    fun fetchGroupMembers(groupId: String):LiveData<List<GroupMembersModel>>{
        return dataBase!!.groupMembersDAO.fetchGroupMemberList(groupId)
    }
    fun fetchSelfGroupMemberData(groupId: String):LiveData<GroupMembersModel>{
        return dataBase!!.groupMembersDAO.fetchGroupMember(groupId, APPLICATION_TM_LOGIN_USER_ID)
    }
    fun fetchGroupActiveMemberCount(groupId: String):List<Int>{
        return dataBase!!.groupMembersDAO.fetchGroupActiveMemberCount(groupId)
    }


    override fun syncData() {
        if (isRegisterIsInProcessing){
            return
        }
        var query = "SELECT MAX(IFNULL(updated_at,'')) AS updated_at FROM chat  UNION ALL SELECT MAX(IFNULL(updated_at,'')) AS updated_at FROM user UNION ALL SELECT MAX(IFNULL(updated_at,'')) AS updated_at FROM chat_group UNION ALL SELECT MAX(IFNULL(updated_at,'')) AS updated_at FROM group_member UNION ALL SELECT MAX(IFNULL(updated_at,'')) AS updated_at FROM contact UNION ALL SELECT MAX(IFNULL(updated_at,'')) AS updated_at FROM story UNION ALL SELECT MAX(IFNULL(updated_at,'')) AS updated_at FROM broadcast ORDER BY updated_at  DESC LIMIT 1 "
        //Log.e("query==> ",""+query)
        var time = dataBase!!.messengerDAO.fetchCurrentTime(SimpleSQLiteQuery(query))
        if (time.isNullOrEmpty()){
            time = ConstantValues.fetchCurrentTimeInUTC()
        }
        if(time.contains(".")){
            time = time.substring(0,time.indexOf("."))
        }
        var lastMessageId = dataBase!!.messengerDAO.fetchLastMessageId()
        ioScope.launch {
            syncOfflineReadMessages()
            syncMessengerUpdatedContacts(time)
            syncServerMessages(time,lastMessageId)
            syncLocalMessages()
            syncLocallyDeletedMessages()
            syncStories(time)
            syncLocallyViewedStories()
            syncLocallyDeletedStories()
            syncLocallyArchivedChats()
            syncLocallyUnarchivedChats()
            syncLocallyBlockedUsers()
            syncLocallyUnBlockedUsers()
            //syncDeactivatedUsers(time)
            //syncBroadCastMessages()
            syncUndeliveredStatusMessages()
            TroopSocketClient.updateRecentUserGroupMessage()
        }
    }

    private fun syncOfflineReadMessages() {
        var offlineUnArchivedChats = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.TM_OFFLINE_READ_MESSAGES,"[]"))
        if (offlineUnArchivedChats!=null && offlineUnArchivedChats.length()>0){
            for (i in 0 until offlineUnArchivedChats.length()) {
                var messageObject = offlineUnArchivedChats.optJSONObject(i)
                var jsonObject = JSONObject()
                if (offlineUnArchivedChats.optJSONObject(i)!=null && offlineUnArchivedChats.optJSONObject(i).has("message_id") && offlineUnArchivedChats.optJSONObject(i).optJSONArray("message_id").length()>0) {
                    jsonObject.put("access_token", APP_ACCESS_TOKEN)
                    jsonObject.put(
                        "message_id",
                        offlineUnArchivedChats.optJSONObject(i).optJSONArray("message_id")
                    )
                    jsonObject.put(
                        "is_group",
                        offlineUnArchivedChats.optJSONObject(i).optInt("is_group")
                    )
                    jsonObject.put(
                        "is_room",
                        offlineUnArchivedChats.optJSONObject(i).optInt("is_room")
                    )
                    TroopMessenger.sendMessageReadStatus(jsonObject)
                }
            }
            sharedPreferences!!.edit().putString(SharePreferenceConstants.TM_OFFLINE_READ_MESSAGES,"[]").apply()
            /*messengerSocket!!.emit(SocketEvents.MESSAGE_READ,jsonObject, Ack { args ->
                if (args[0]!=null){
                    sharedPreferences!!.edit().putString(SharePreferenceConstants.TM_OFFLINE_UNARCHIVED_CHATS,"[]").apply()
                }
            })*/
        }
    }

    private suspend fun syncServerMessages(time:String,lastMessageId:Long){
        ioScope.launch {
            var responseResult = baseAPIService!!.fetchOfflineMessages(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,
                APP_ACCESS_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,
                deviceId,time,lastMessageId.toString()).await()
            if (responseResult.isSuccessful) {
                var response = Helper.stringToJsonObject(responseResult.body()!!)
                if (response != null && response.optBoolean("success")) {
                    if (response.has("messages") && response.optJSONArray("messages")!!.length()>0){
                        TroopSocketClient.messageInsertOrUpdate(response.optJSONArray("messages")!!,ConstantValues.SocketOn.SYNC)
                    }
                }
            }else{
                //ResponseResult.Error(response.message())
            }
        }
    }
    private suspend fun syncMessengerUpdatedContacts(time:String){
        ioScope.launch {
            var responseResult = baseAPIService!!.fetchUpdateMessengerContacts(ConstantValues.CLIENT_VERSION,
                if (messengerSocket != null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,
                APP_ACCESS_TOKEN,
                ConstantValues.TOKEN,
                ConstantValues.PLATFORM_ANDROID,
                deviceId,
                time
            ).await()
            //uiScope.launch {
                if (responseResult.isSuccessful) {
                    var response = Helper.stringToJsonObject(responseResult.body()!!)
                    if (response!=null && response.optBoolean("success")){
                        Log.e("data===> ",""+response.toString())
                        if (response.optJSONObject("data")!!.has("users") && response.optJSONObject("data")!!.optJSONArray("users")!!.length()>0){
                            var userList = ArrayList<User>()
                            var userLength = response.optJSONObject("data")!!.optJSONArray("users")!!.length()
                            for (i in  0 until userLength) {
                                var userObject = response.optJSONObject("data")!!.optJSONArray("users")!!.optJSONObject(i)
                                var updateTime = ConstantValues.fetchCurrentTimeInUTC()
                                var user = User()//Gson().fromJson(userObject.toString(),User::class.java)
                                user.updated_at = updateTime
                                if (dataBase!!.userDAO.checkUserExists(userObject.optString("entity_id"))>0){
                                    user = dataBase!!.userDAO.fetchUser(userObject.optString("entity_id"))
                                }
                                user.uid = userObject.optString("uid")
                                user.user_id = userObject.optLong("entity_id")
                                user.created_at = updateTime
                                user.updated_at = updateTime
                                user.name = userObject.optString("entity_name")
                                user.is_archived = userObject.optInt("is_archived")
                                user.is_blocked = userObject.optInt("is_blocked")
                                user.blocked_me = userObject.optInt("blocked_me")
                                user.mobile=  userObject.optString("mobile")
                                user.country_code = userObject.optString("country_code")
                                user.profile_pic = userObject.optString("profile_pic")
                                user.about = userObject.optString("about")
                                userList.add(user)
                            }
                            dataBase!!.userDAO.insertAllUsers(userList)
                        }
                        var groupLength = response.optJSONObject("data")!!.optJSONArray("groups")!!.length()
                        if (response.optJSONObject("data")!!.has("groups") && groupLength>0){
                            var groupList = ArrayList<MessengerGroup>()
                            for (i in  0 until groupLength) {
                                var groupObject = response.optJSONObject("data")!!.optJSONArray("groups")!!.optJSONObject(i)
                                var updateTime = ConstantValues.fetchCurrentTimeInUTC()
                                var messengerGroup = MessengerGroup() //Gson().fromJson(groupObject.toString(),MessengerGroup::class.java)
                                if (dataBase!!.messengerGroupDAO.checkGroupExists(groupObject.optLong("entity_id"))>0){
                                    messengerGroup = dataBase!!.messengerGroupDAO.fetchingGroupData(groupObject.optString("entity_id"))
                                }
                                messengerGroup.group_id = groupObject.optLong("entity_id")
                                messengerGroup.group_type =groupObject.optInt("group_type")
                                messengerGroup.group_name = groupObject.optString("group_name")
                                messengerGroup.group_avatar = groupObject.optString("entity_avatar")
                                messengerGroup.created_by = groupObject.optLong("group_admin")
                                messengerGroup.is_active = groupObject.optInt("is_active")
                                messengerGroup.group_description = groupObject.optString("group_description")
                                messengerGroup.created_at = groupObject.optString("created_at")
                                messengerGroup.is_archived = groupObject.optInt("is_archived")
                                messengerGroup.updated_at = updateTime
                                groupList.add(messengerGroup)
                            }
                            dataBase!!.messengerGroupDAO.insertBulkMessengerGroup(groupList)
                        }
                        var groupMembersArray= JSONArray()
                        if (response.optJSONObject("data")!!.has("group_members") && response.optJSONObject("data")!!.optJSONArray("group_members")!!.length()>0){
                            var groupMemberList = ArrayList<GroupMembers>()
                            var groupMemberLength = response.optJSONObject("data")!!.optJSONArray("group_members")!!.length()
                            groupMembersArray = response.optJSONObject("data")!!.optJSONArray("group_members")
                            for (i in  0 until groupMemberLength) {
                                var updateTime = ConstantValues.fetchCurrentTimeInUTC()
                                var groupMembers = GroupMembers()
                                var groupMemberObject = response.optJSONObject("data")!!.optJSONArray("group_members")!!.optJSONObject(i)
                                if (dataBase!!.groupMembersDAO.checkGroupUserExists(groupMemberObject.optString("group_id"),groupMemberObject.optString("user_id"))>0){
                                    groupMembers = dataBase!!.groupMembersDAO.fetchGroupUser(groupMemberObject.optString("group_id"),groupMemberObject.optString("user_id"))
                                }
                                groupMembers.group_id = groupMemberObject.optLong("group_id")
                                groupMembers.user_id = if (groupMemberObject.has("user_id")) groupMemberObject.optLong("user_id") else groupMemberObject.optLong("member_id")
                                groupMembers.user_role = if (groupMemberObject.has("user_role")) groupMemberObject.optInt("user_role") else groupMemberObject.optInt("member_role")
                                groupMembers.user_status = if (groupMemberObject.has("user_status")) groupMemberObject.optInt("user_status") else groupMemberObject.optInt("member_status")
                                groupMembers.updated_at = updateTime
                                groupMemberList.add(groupMembers)
                            }
                            bulckAddGroupMembers(groupMembersArray)
                            dataBase!!.groupMembersDAO.insertAllData(groupMemberList)
                        }
                    }
                }
            //}
        }
    }

    internal fun bulckAddGroupMembers(groupMembers: JSONArray) {
        var userIds:ArrayList<Long> = ArrayList()
        var memberObject = JSONObject()
        if (groupMembers!=null && groupMembers.length()>0){
            for (i in 0 until groupMembers.length()){
                var groupMemberObject = groupMembers.optJSONObject(i)
                var userId =  if (groupMemberObject.has("user_id")) groupMemberObject.optLong("user_id") else groupMemberObject.optLong("member_id")
                userIds.add(userId)
                memberObject.put(userId.toString(),groupMemberObject)
            }
        }

        var existingUsers = dataBase!!.userDAO.fetchNonUsers(userIds)
        var nonExistingUsers = userIds.minus(existingUsers.toHashSet())
        var nonUserList : ArrayList<User> = ArrayList()

        if (nonExistingUsers!=null && nonExistingUsers.isNotEmpty()){
            for (i in nonExistingUsers.indices){
                if (memberObject.has(nonExistingUsers[i].toString())){
                    var jsonObject = memberObject.optJSONObject(nonExistingUsers[i].toString())
                    var user = User()
                    user.uid = jsonObject!!.optString("member_uid")
                    user.name = jsonObject.optString("member_name")
                    user.user_id = nonExistingUsers[i]
                    user.mobile = jsonObject.optString("member_mobile")
                    user.profile_pic = jsonObject.optString("member_profile_pic")
                    user.country_code = jsonObject.optString("member_country_code")
                    nonUserList.add(user)
                }
            }
        }
        if (nonUserList!=null && nonUserList.size>0){
            dataBase!!.userDAO.insertAllUsers(nonUserList)
        }
    }

    private fun syncLocalMessages(){
        var deviceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")
        var unSentMessages = dataBase!!.messengerDAO.fetchUnSentMessages()
        var messagesList = unSentMessages.size
        var unsentArray = JSONArray()
        for (i in 0 until messagesList){
            var entityType = unSentMessages[i].is_group+1
            var referenceId = deviceId+unSentMessages[i].id
            unSentMessages[i].access_token = APP_ACCESS_TOKEN
            unSentMessages[i].reference_id = referenceId
            var message:String = ""
            if (!unSentMessages[i].message.isNullOrEmpty()){
                message = unSentMessages[i].message!!
                unSentMessages[i].message = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,unSentMessages[i].message!!,unSentMessages[i].sender_id.toString(),entityType,referenceId,unSentMessages[i].sender_id.toString(),unSentMessages[i].receiver_id.toString())
            }
            if (!unSentMessages[i].attachment.isNullOrEmpty()){
                unSentMessages[i].attachment = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,unSentMessages[i].attachment!!,unSentMessages[i].sender_id.toString(),entityType,referenceId,unSentMessages[i].sender_id.toString(),unSentMessages[i].receiver_id.toString())
            }
            if (!unSentMessages[i].caption.isNullOrEmpty()){
                unSentMessages[i].caption = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,unSentMessages[i].caption!!,unSentMessages[i].sender_id.toString(),entityType,referenceId,unSentMessages[i].sender_id.toString(),unSentMessages[i].receiver_id.toString())
            }
            var jsonObject = Helper.stringToJsonObject(Gson().toJson(unSentMessages[i]))
            jsonObject!!.put("version",ConstantValues.CLIENT_VERSION.toInt())
            /*if (unSentMessages[i].message_type==ConstantValues.MessageTypes.CONTACT_MESSAGE){
                var contactObject = Helper.stringToJsonObject(message)!!
                jsonObject!!.put("contact_name",contactObject.optString("contact_name"))
                jsonObject.put("contact_number",contactObject.optString("contact_number"))
            }else if (unSentMessages[i].message_type==ConstantValues.MessageTypes.LOCATION_MESSAGE){
                var locationObject = Helper.stringToJsonObject(message)!!
                jsonObject!!.put("location_latitude",locationObject.optString("location_latitude"))
                jsonObject.put("location_longitude",locationObject.optString("location_longitude"))
                jsonObject.put("location_address",locationObject.optString("location_address"))
                jsonObject.put("location_name",locationObject.optString("location_name"))
            }*/
            unsentArray.put(jsonObject)
        }
        messengerSocket!!.emit(SocketEvents.SYNC_OFFLINE_MESSAGES,unsentArray)
    }
    private fun syncLocallyDeletedMessages(){
        var offlineDeletedMessages = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.OFFLINE_DELETED_MESSAGES,"[]"))
        if (offlineDeletedMessages!=null && offlineDeletedMessages.length()>0) {
            var messagesLength = offlineDeletedMessages.length()
            for (i in 0 until messagesLength) {
                var jsonObject = JSONObject()
                jsonObject.put("access_token",APP_ACCESS_TOKEN)
                jsonObject.put("message_id",offlineDeletedMessages.optJSONObject(i).optString("message_id"))
                jsonObject.put("is_room",0)
                jsonObject.put("entity_id",offlineDeletedMessages.optJSONObject(i).optString("entity_id"))
                jsonObject.put("entity_type",offlineDeletedMessages.optJSONObject(i).optInt("entity_type"))
                messengerSocket!!.emit(SocketEvents.DELETE_MESSAGE,jsonObject,Ack{args ->
                    if (args[0]!=null){
                        //dataBase!!.messengerDAO.deleteServerMessages(JSONObject(args[0]))
                        sharedPreferences!!.edit().putString(SharePreferenceConstants.OFFLINE_DELETED_MESSAGES,"[]").apply()
                        TroopSocketClient.updateRecentUserGroupMessage()
                    }
                })
            }
        }
    }
    private suspend fun syncStories(recentTime:String):ResponseResult<String>{
        return try {
            var responseResults = baseAPIService!!.fetchUpdatedStories(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,APP_AUTHORIZATION_TOKEN,APP_ACCESS_TOKEN,ConstantValues.TOKEN,ConstantValues.PLATFORM_ANDROID,deviceId,recentTime).await()
            if (responseResults.isSuccessful && responseResults!=null) {
                var response = Helper.stringToJsonObject(responseResults.body()!!)!!
                if (response!=null && response.optBoolean("success")){
                    var storiesData = response.optJSONArray("data")
                    var storyList:ArrayList<Story> = ArrayList()
                    var storyLength = storiesData.length()
                    for (i in 0 until storyLength){
                        var jsonObject = storiesData!!.optJSONObject(i)
                        var story = Gson().fromJson(jsonObject.toString(),Story::class.java)
                        //story.status = 1
                        story.status =  if (jsonObject.optInt("seen")==1) ConstantValues.StoryStatus.VIEWED else if (jsonObject.optString("seen").trim().isNullOrEmpty()) ConstantValues.StoryStatus.VIEWED else ConstantValues.StoryStatus.UNSEEN
                        storyList.add(story)
                    }
                    if (storyList.isNotEmpty()){
                        dataBase!!.storyDAO.insertBulkStories(storyList)
                    }
                }
                ResponseResult.Success(responseResults.body()!!)
            }else{
                ResponseResult.Error(responseResults.message())
            }
        }catch (e:Exception){
            ResponseResult.Error("")
        }
    }
    private fun syncLocallyViewedStories(){
        var offlineViewedStories = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.OFFLINE_VIEWED_STORIES,"[]"))
        if (offlineViewedStories!=null && offlineViewedStories.length()>0) {
            var numberOfStories = offlineViewedStories.length()
            for (i in 0 until numberOfStories){
                var jsonObject = JSONObject()
                jsonObject.put("access_token", APP_ACCESS_TOKEN)
                jsonObject.put("id",offlineViewedStories.optJSONObject(i).optString("id"))
                jsonObject.put("user_id",offlineViewedStories.optJSONObject(i).optString("user_id"))
                jsonObject.put("seen_at",offlineViewedStories.optJSONObject(i).optString("seen_at"))
                jsonObject.put("name",APP_LOGIN_USER_NAME)
                messengerSocket!!.emit(SocketEvents.VIEW_STORY,jsonObject,Ack{args ->
                    if (args[0]!=null){
                        sharedPreferences!!.edit().putString(SharePreferenceConstants.OFFLINE_VIEWED_STORIES,"[]").apply()
                    }
                })
            }
        }
    }
    private fun syncLocallyDeletedStories(){
        var offlineDeletedStories = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.TM_OFFLINE_DELETED_STORIES,"[]"))
        if (offlineDeletedStories!=null && offlineDeletedStories.length()>0){
            var jsonObject = JSONObject()
            jsonObject.put("access_token", APP_ACCESS_TOKEN)
            jsonObject.put("id",offlineDeletedStories)
            messengerSocket!!.emit(SocketEvents.DELETE_STORY,jsonObject, Ack { args ->
                if (args[0]!=null){
                    sharedPreferences!!.edit().putString(SharePreferenceConstants.TM_OFFLINE_DELETED_STORIES,"[]").apply()
                }
            })
        }
    }
    private fun syncLocallyArchivedChats(){
        var offlineArchivedChats = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.TM_OFFLINE_ARCHIVED_CHATS,"[]"))
        if (offlineArchivedChats!=null && offlineArchivedChats.length()>0){
            var jsonObject = JSONObject()
            jsonObject.put("access_token", APP_ACCESS_TOKEN)
            jsonObject.put("chats",offlineArchivedChats)
            messengerSocket!!.emit(SocketEvents.ARCHIVED_CHAT,jsonObject, Ack { args ->
                if (args[0]!=null){
                    sharedPreferences!!.edit().putString(SharePreferenceConstants.TM_OFFLINE_ARCHIVED_CHATS,"[]").apply()
                }
            })
        }
    }
    private fun syncLocallyUnarchivedChats(){
        var offlineUnArchivedChats = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.TM_OFFLINE_UNARCHIVED_CHATS,"[]"))
        if (offlineUnArchivedChats!=null && offlineUnArchivedChats.length()>0){
            var jsonObject = JSONObject()
            jsonObject.put("access_token", APP_ACCESS_TOKEN)
            jsonObject.put("chats",offlineUnArchivedChats)
            messengerSocket!!.emit(SocketEvents.UNARCHIVED_CHAT,jsonObject, Ack { args ->
                if (args[0]!=null){
                    sharedPreferences!!.edit().putString(SharePreferenceConstants.TM_OFFLINE_UNARCHIVED_CHATS,"[]").apply()
                }
            })
        }
    }
    private fun syncLocallyBlockedUsers(){
        var offlineBlockedUsers = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.TM_OFFLINE_BLOCKED_USERS,"[]"))
        if (offlineBlockedUsers!=null && offlineBlockedUsers.length()>0){
            var jsonObject = JSONObject()
            jsonObject.put("access_token", APP_ACCESS_TOKEN)
            jsonObject.put("entity_id",offlineBlockedUsers)
            messengerSocket!!.emit(SocketEvents.BLOCK_USER,jsonObject, Ack { args ->
                if (args[0]!=null){
                    sharedPreferences!!.edit().putString(SharePreferenceConstants.TM_OFFLINE_BLOCKED_USERS,"[]").apply()
                }
            })
        }
    }
    private fun syncLocallyUnBlockedUsers(){
        var offlineUnBlockedUsers = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.TM_OFFLINE_UNBLOCKED_USERS,"[]"))
        if (offlineUnBlockedUsers!=null && offlineUnBlockedUsers.length()>0){
            var jsonObject = JSONObject()
            jsonObject.put("access_token", APP_ACCESS_TOKEN)
            jsonObject.put("entity_id",offlineUnBlockedUsers)
            messengerSocket!!.emit(SocketEvents.UNBLOCK_USER,jsonObject, Ack { args ->
                if (args[0]!=null){
                    sharedPreferences!!.edit().putString(SharePreferenceConstants.TM_OFFLINE_UNBLOCKED_USERS,"[]").apply()
                }
            })
        }
    }
    private suspend fun syncDeactivatedUsers(recentTime: String){
        var responseResult = baseAPIService!!.fetchDeactivatedUsers(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
            APPLICATION_TM_LOGIN_USER_ID,ConstantValues.TOKEN,APP_AUTHORIZATION_TOKEN,APP_ACCESS_TOKEN,recentTime).await()
        if (responseResult.isSuccessful && responseResult!=null) {
            var response = Helper.stringToJsonObject(responseResult.body()!!)!!
            if (response!=null && response.optBoolean("success")){
                var userIds = response.optJSONArray("user_id")
                if (userIds!=null && userIds.length()>0) {
                    deactivatedUsers(userIds)
                }
            }
        }
    }
    private fun deactivatedUsers(userId: JSONArray) {
        val userids = Helper.toStringArray(userId)
        val uids = dataBase!!.userDAO.fetchUidByUserIds(userids)
        dataBase!!.userDAO.deleteUsers(userids)
        dataBase!!.storyDAO.deleteUserStories(userids)
        dataBase!!.contactDAO.deleteContacts(uids)
        dataBase!!.messengerGroupDAO.deleteAdminGroup(userids)
        val groupIds = dataBase!!.messengerGroupDAO.fetchAdminGroupIds(userids)
        dataBase!!.groupMembersDAO.deleteUsersFromGroup(groupIds)
        dataBase!!.messengerDAO.deleteUserMessages(userids)
        dataBase!!.messengerDAO.deleteGroupMessages(groupIds)
    }
    private suspend fun syncBroadCastMessages(){
        var recentTime = dataBase!!.broadCastDAO.fetchLastRecordTime()
        ioScope.launch {
            var responseResult = baseAPIService!!.fetchUpdatedBroadCastMessages(ConstantValues.CLIENT_VERSION,if (messengerSocket!=null && messengerSocket!!.connected()) messengerSocket!!.id() else "",
                APPLICATION_TM_LOGIN_USER_ID,ConstantValues.TOKEN,APP_AUTHORIZATION_TOKEN,APP_ACCESS_TOKEN,recentTime).await()
            if (responseResult.isSuccessful && responseResult!=null) {
                val response = Helper.stringToJsonObject(responseResult.body()!!)
                if (response!=null && response.optBoolean("success")){
                    if (response.has("data") && response.optJSONArray("data").length()>0) {
                        var data = response.optJSONArray("data")
                    }
                }
            }
        }

    }
    private fun syncUndeliveredStatusMessages(){
        var undeliveredStatusMessages = dataBase!!.messengerDAO.fetchUndeliveredMessages(
            APPLICATION_TM_LOGIN_USER_ID)
        if (undeliveredStatusMessages!=null && undeliveredStatusMessages.isNotEmpty()){
            var jsonObject = JSONObject()
            jsonObject.put("access_token", APP_ACCESS_TOKEN)
            //for (i in 0 until undeliveredStatusMessages.size) {
                jsonObject.put(
                    "message_id",
                    Helper.stringToJsonArray(Gson().toJson(undeliveredStatusMessages))
                )
                messengerSocket!!.emit(SocketEvents.MESSAGE_DELIVERED, jsonObject)
                dataBase!!.messengerDAO!!.updateMessageDeliveredStatus(
                    undeliveredStatusMessages,
                    ConstantValues.fetchCurrentTimeInUTC()
                )
            //}
        }
    }
    fun fetchMyStories():LiveData<List<StoryModel>>{
        dataBase!!.storyDAO.deleteExpiredStories()
        return dataBase!!.storyDAO.fetchMyStories(APPLICATION_TM_LOGIN_USER_ID)
    }
    fun fetchOtherStories():LiveData<List<StoryModel>> {
        return dataBase!!.storyDAO.fetchOtherStories(APPLICATION_TM_LOGIN_USER_ID)
    }
    fun fetchStoriesByUserId(userId: String):LiveData<List<StoryModel>> {
        if (userId== APPLICATION_TM_LOGIN_USER_ID){
            return dataBase!!.storyDAO.fetchMyStories(userId)
        }else {
            return dataBase!!.storyDAO.fetchStoriesByUserId(userId)
        }
    }
    fun fetchCopiedMessages(id:List<Long>):List<String>{
        return dataBase!!.messengerDAO.fetchCopiedMessages(id)
    }
    fun getMessageActionsForMessage(id: List<Long>):JSONObject{
        var jsonObject = JSONObject()
        jsonObject.put("copy",false)
        jsonObject.put("edit",false)
        jsonObject.put("delete",false)
        jsonObject.put("recall",false)
        if (id.isEmpty()){
            return jsonObject
        }
        var messages = dataBase!!.messengerDAO.fetchMessagesForMessageActions(id)

        var messagesLength = messages.size
        var sentMessages:ArrayList<Long> = ArrayList()
        var receiverMessages :ArrayList<Long> = ArrayList()
        var textMessages : ArrayList<Long> = ArrayList()
        var attachmentMessages : ArrayList<Long> = ArrayList()
        var otherMessages : ArrayList<Long> = ArrayList()
        var inActiveMessages : ArrayList<Long> = ArrayList()
        var nonNotificationMessage = arrayOf("0","1","2","3","23")
        var isNonNotificationMessage = false
        for (i in 0 until messagesLength){
            if (messages[i].sender_id.toString()== APPLICATION_TM_LOGIN_USER_ID){
                sentMessages.add(messages[i].ID)
            }else {
                receiverMessages.add(messages[i].ID)
            }
            if (messages[i].message_type==ConstantValues.MessageTypes.TEXT_MESSAGE){
                textMessages.add(messages[i].ID)
            }else if (messages[i].message_type==ConstantValues.MessageTypes.ATTACHMENT || messages[i].message_type==ConstantValues.MessageTypes.AUDIO_MESSAGE){
                attachmentMessages.add(messages[i].ID)
            }else{
                otherMessages.add(messages[i].ID)
            }
            if (messages[i].status!=1){
                inActiveMessages.add(messages[i].ID)
            }
            if (nonNotificationMessage.contains(messages[i].ID.toString())){
                isNonNotificationMessage = true
            }
        }
        if (textMessages.isNotEmpty() && attachmentMessages.isEmpty() && otherMessages.isEmpty() && inActiveMessages.isEmpty() && !isNonNotificationMessage){
            jsonObject.put("copy",true)
        }
        if (textMessages.size==1 && attachmentMessages.isEmpty() && otherMessages.isEmpty() && inActiveMessages.isEmpty()&& sentMessages.isNotEmpty() && sentMessages.size==1 && receiverMessages.isEmpty() && !isNonNotificationMessage){
            jsonObject.put("edit",true)
        }
        if (!isNonNotificationMessage){
            jsonObject.put("delete",true)
        }
        if ((textMessages.isNotEmpty() || attachmentMessages.isNotEmpty() || otherMessages.isNotEmpty())
            && inActiveMessages.isEmpty() && sentMessages.isNotEmpty() && sentMessages.size==id.size && receiverMessages.isEmpty() && !isNonNotificationMessage){
            jsonObject.put("recall",true)
        }
        return jsonObject
    }
    fun fetchProfileByUid(uid: String):UserClientModel{
        return dataBase!!.userDAO.fetchProfileByUid(uid)
    }
    fun fetchUserProfile(userId: String):UserClientModel{
        return dataBase!!.userDAO.fetchUserProfile(userId)
    }
    fun fetchUserAvatar(userId: String):String{
        return dataBase!!.userDAO.fetchUserAvatar(userId)
    }
    fun fetchUserName(userId: String):String{
        return dataBase!!.userDAO.fetchUserName(userId)
    }
    fun parseCallLogs(callLogs:List<Long>){

    }
    fun userPresence(userId: String,listener: ClientCallBackListener){
        var callBackObjet =JSONObject()
        callBackObjet.put("user_id",userId)
        if (dataBase!!.userDAO.checkIsUserIsBlocked(userId)==1){
            callBackObjet.put("success",false)
            callBackObjet.put("status","offline")
            callBackObjet.put("last_seen","")

            listener.userPresenceResponse(callBackObjet)
        }
        if (CLIENT_IS_CONNECTED){
            var jsonObject = JSONObject()
            jsonObject.put("access_token", APP_ACCESS_TOKEN)
            jsonObject.put("user_id",userId)
            jsonObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
            Log.e("userpresense==> "," sent "+jsonObject.toString())
            messengerSocket!!.emit(SocketEvents.USER_PRESENCE,jsonObject, Ack {args->
                Log.e("userpresense==> "," callbac k"+args.size)
                if (args[0]!=null){
                    Log.e("userpresense==> "," callback"+args[0].toString())
                    var responseObject = args[0] as JSONObject
                    responseObject.put("user_id",userId)
                    if (!responseObject.optString("last_seen").isNullOrEmpty()){
                        responseObject.put("last_seen",Helper.utcToLocalTime(responseObject.optString("last_seen")))
                    }
                    listener.userPresenceResponse(responseObject)
                }else if (args[1]!=null){
                    Log.e("userpresense==> ",""+args[1].toString())
                }
            })
        }else {
            callBackObjet.put("success", false)
            callBackObjet.put("status", "Connection to server is not yet established!")
            callBackObjet.put("last_seen", "")
            listener.userPresenceResponse(callBackObjet)
        }
    }
    fun attachmentDownloaded(messageId:Long,attachmentPath:String){
        dataBase!!.messengerDAO.updateAttachmentDownloaded(attachmentPath,messageId)
    }
    fun updateAwsFilePath(localId:Long,awsPath:String){
        dataBase!!.messengerDAO.updateAwsPath(awsPath,localId)
    }
    fun getStatusText(status:String):String{
        when (status){
            "status_1"->{
                return "online"
            }
            "status_2"->{
                return "do not disturb"
            }
            "status_3"->{
                return "away"
            }
        }
        return ""
    }
    fun fetchNonGroupUserList(groupId:String):List<UserClientModel>{
        return dataBase!!.userDAO.fetchNonGroupUserList(APPLICATION_TM_LOGIN_USER_ID,groupId)
    }
    fun fetchGroupAvatar(groupId: String):String{
        return dataBase!!.messengerGroupDAO.fetchGroupAvatar(groupId)
    }
    fun fetchUsersListForCreatingGroup():List<UserClientModel>{
        return dataBase!!.userDAO.fetchUsersListForCreatingGroup(APPLICATION_TM_LOGIN_USER_ID)
    }
    fun fetchMessageStatusText(isRead:Int,isDelivered:Int):String{
        return if (isRead==1){
            ConstantValues.MessageStatusText.READ
        }else if (isDelivered==1){
            ConstantValues.MessageStatusText.DELIVERED
        }else if (isDelivered==2){
            ConstantValues.MessageStatusText.PENDING
        }else{
            ConstantValues.MessageStatusText.SENT
        }
    }

    fun checkUserIsBlock(entityId: Long): Int {
        return dataBase!!.userDAO.checkIsUserIsBlocked(entityId.toString())
    }
    fun fetchUserMedia(userId: String):LiveData<List<MediaModel>>{
        return dataBase!!.messengerDAO.fetchUserMedia(userId, APPLICATION_TM_LOGIN_USER_ID)
    }
    fun fetchGroupMedia(userId: String):LiveData<List<MediaModel>>{
        return dataBase!!.messengerDAO.fetchGroupMedia(userId)
    }
    fun addCallStreams(){
        if (RoomClient.localAudioTrack==null && RoomClient.mMediasoupDevice!=null && RoomClient.mMediasoupDevice!!.isLoaded) {
            Log.e("called===> "," add ")
            RoomClient.peerConnectionAndStream(false)
        }else{
            Log.e("called===> "," add "+(RoomClient.localAudioTrack==null)+"   "+(RoomClient.mMediasoupDevice!=null))
        }
    }
}
