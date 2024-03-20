package com.tvisha.trooponprime.lib.socket

import android.util.Log
import com.google.gson.Gson
import com.tvisha.trooponprime.lib.TroopClient
import com.tvisha.trooponprime.lib.TroopMessengerClient
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APPLICATION_LOGIN_U_ID
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APPLICATION_TM_LOGIN_USER_ID
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APP_ACCESS_TOKEN
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APP_LOGIN_USER_NAME
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.CLIENT_IS_CONNECTED
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.dataBase
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.messengerSocket
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.sharedPreferences
import com.tvisha.trooponprime.lib.clientModels.Forward
import com.tvisha.trooponprime.lib.clientModels.MessageData
import com.tvisha.trooponprime.lib.database.Messenger
import com.tvisha.trooponprime.lib.database.model.MessageMap
import com.tvisha.trooponprime.lib.listeneres.ClientCallBackListener
import com.tvisha.trooponprime.lib.utils.*
import com.tvisha.trooponprime.lib.utils.ConstantValues.TAG
import io.socket.client.Ack
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TroopMessenger {
    fun sendTextMessage(data:MessageData,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid Receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid Receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==-1 || (data.is_group!=0 && data.is_group!=1)){
            callBackObject.put("message","Value fot is_group not specified")
            listener.tmError(callBackObject)
            return
        }
        if (data.message.isNullOrEmpty()){
            callBackObject.put("message","Message cannot be empty")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==0){
            if (dataBase!!.userDAO.checkIsUserIsBlocked(data.receiver_uid)==1){
                return
            }
        }
        setTyping(data.receiver_id,data.is_group,0)
        var currentTime = ConstantValues.fetchCurrentTimeInUTC()
        var messenger = Messenger()
        messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
        messenger.receiver_id = data.receiver_id.toLong()
        messenger.message = data.message
        messenger.message_type = ConstantValues.MessageTypes.TEXT_MESSAGE
        messenger.attachment = ""
        messenger.caption = ""
        messenger.is_group = data.is_group
        messenger.message_id = 0
        messenger.is_sync = 0
        messenger.is_read = 0
        messenger.is_reply = 0
        messenger.original_message_id = 0
        messenger.original_message = "{}"
        messenger.is_delivered = 0
        messenger.status = 1
        messenger.is_forward  = 0
        messenger.is_flag = 0
        messenger.is_forkout = 0
        messenger.conversation_reference_id = data.conversation_reference_id
        messenger.created_at = currentTime
        messenger.updated_at = currentTime
        messenger.sender_uid = data.sender_uid
        messenger.receiver_uid =data.receiver_uid
        messenger.is_room = data.is_room
        messenger.sender_name = APP_LOGIN_USER_NAME
        messenger.isMine = 1
        dataBase!!.messengerDAO.insertMessage(messenger)
        var localId = dataBase!!.messengerDAO.fetchLastLocalId()
        if (data.is_group == 0 ){
            dataBase!!.userDAO.updateUserRecentMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.TEXT_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }else{
            dataBase!!.messengerGroupDAO.updateRecentGroupMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.TEXT_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }
        var referenceId = TroopMessengerClient.sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
        var messageObject = Gson().toJson(messenger)
        listener.messageSaved(JSONObject(messageObject))
        if (CLIENT_IS_CONNECTED){
            var encryptedMessage = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.message,data.receiver_id,data.is_group+1,referenceId,
                APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("message_type",ConstantValues.MessageTypes.TEXT_MESSAGE)
            sendObject.put("receiver_id",data.receiver_id)
            sendObject.put("is_group",data.is_group)
            sendObject.put("is_room",data.is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message",encryptedMessage)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data.conversation_reference_id)
            sendObject.put("receiver_uid",data.receiver_uid)
            sendObject.put("sender_uid",data.sender_uid)
            sendObject.put("entity",data.is_group+1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION)
            messengerSocket!!.emit(SocketEvents.SEND_MESSAGE,sendObject)
            //messengerSocket!!.once("tm_error",data)
        }else{
            newMessageVerifyChatArchive(data.receiver_id,data.is_group+1)
        }
    }
    fun sendForkOutTextMessage(data: List<MessageData>,listener:ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.isEmpty()){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        var lengthOfList =  data.size
        var forkOutArray =JSONArray()
        for (i in 0 until lengthOfList){
            var currentTime = ConstantValues.fetchCurrentTimeInUTC()
            var messenger = Messenger()
            messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
            messenger.receiver_id = data[i].receiver_id.toLong()
            messenger.message = data[i].message
            messenger.message_type = ConstantValues.MessageTypes.TEXT_MESSAGE
            messenger.attachment = ""
            messenger.preview_link = ""
            messenger.caption = ""
            messenger.is_group = data[i].is_group
            messenger.message_id = 0
            messenger.is_sync = 0
            messenger.is_read = 0
            messenger.is_reply = 0
            messenger.original_message_id = 0
            messenger.original_message = "{}"
            messenger.is_delivered = 0
            messenger.status = 1
            messenger.is_forward  = 0
            messenger.is_flag = 0
            messenger.is_forkout = 1
            messenger.conversation_reference_id = data[i].conversation_reference_id
            messenger.created_at = currentTime
            messenger.updated_at = currentTime
            messenger.sender_uid = data[i].sender_uid
            messenger.receiver_uid =data[i].receiver_uid
            messenger.is_room = data[i].is_room
            messenger.sender_name = APP_LOGIN_USER_NAME
            messenger.isMine = 1
            dataBase!!.messengerDAO.insertMessage(messenger)
            var localId = dataBase!!.messengerDAO.fetchLastLocalId()
            var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
            //var messageObject = Gson().toJson(messenger)
            //listener.messageSaved(JSONObject(messageObject))
            var encryptedMessage = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data[i].message,data[i].receiver_id,data[i].is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data[i].receiver_id)
            //var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.caption,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("receiver_id",data[i].receiver_id)
            sendObject.put("is_group",data[i].is_group)
            sendObject.put("is_room",data[i].is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message",encryptedMessage)
            //sendObject.put("attachment",encryptedAttachment)
            //sendObject.put("caption",encryptedCaption)
            //sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data[i].conversation_reference_id)
            sendObject.put("receiver_uid",data[i].receiver_uid)
            sendObject.put("sender_uid",data[i].sender_uid)
            sendObject.put("entity",data[i].is_group+1)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message_type",ConstantValues.MessageTypes.TEXT_MESSAGE)
            sendObject.put("is_forkout",1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION)
            forkOutArray.put(sendObject)
        }
        TroopSocketClient.updateRecentUserGroupMessage()
        messengerSocket!!.emit(SocketEvents.FORK_OUT,forkOutArray)
    }
    fun sendAttachmentMessage(data: MessageData,listener:ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid Receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid Receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==-1 || (data.is_group!=0 && data.is_group!=1)){
            callBackObject.put("message","Value fot is_group not specified")
            listener.tmError(callBackObject)
            return
        }
        if (data.attachment!!.isNullOrEmpty()){
            callBackObject.put("message","Attachment cannot be empty")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==0){
            if (dataBase!!.userDAO.checkIsUserIsBlocked(data.receiver_uid)==1){
                return
            }
        }
        setTyping(data.receiver_id,data.is_group,0)
        var currentTime = ConstantValues.fetchCurrentTimeInUTC()
        var messenger = Messenger()
        messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
        messenger.receiver_id = data.receiver_id.toLong()
        messenger.message = ""
        messenger.message_type = ConstantValues.MessageTypes.ATTACHMENT
        messenger.attachment = data.attachment
        messenger.preview_link = data.preview_link
        if (!data.local_attachment_path.isNullOrEmpty()){
            messenger.local_attachment_path = data.local_attachment_path
            messenger.attachment_downloaded = 1
        }
        messenger.caption = data.caption
        messenger.is_group = data.is_group
        messenger.message_id = 0
        messenger.is_sync = 0
        messenger.is_read = 0
        messenger.is_reply = 0
        messenger.original_message_id = 0
        messenger.original_message = "{}"
        messenger.is_delivered = 0
        messenger.status = 1
        messenger.is_forward  = 0
        messenger.is_flag = 0
        messenger.is_forkout = 0
        messenger.conversation_reference_id = data.conversation_reference_id
        messenger.created_at = currentTime
        messenger.updated_at = currentTime
        messenger.sender_uid = data.sender_uid
        messenger.receiver_uid =data.receiver_uid
        messenger.is_room = data.is_room
        messenger.sender_name = APP_LOGIN_USER_NAME
        messenger.isMine = 1
        dataBase!!.messengerDAO.insertMessage(messenger)
        var localId = dataBase!!.messengerDAO.fetchLastLocalId()
        if (data.is_group == 0 ){
            dataBase!!.userDAO.updateUserRecentMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.ATTACHMENT,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }else{
            dataBase!!.messengerGroupDAO.updateRecentGroupMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.ATTACHMENT,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }
        var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
        var messageObject = Gson().toJson(messenger)
        listener.messageSaved(JSONObject(messageObject))
        if (CLIENT_IS_CONNECTED){
            var encryptedAttachment = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.attachment,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.caption,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("receiver_id",data.receiver_id)
            sendObject.put("is_group",data.is_group)
            sendObject.put("is_room",data.is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message","")
            sendObject.put("attachment",encryptedAttachment)
            sendObject.put("caption",encryptedCaption)
            sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data.conversation_reference_id)
            sendObject.put("receiver_uid",data.receiver_uid)
            sendObject.put("sender_uid",data.sender_uid)
            sendObject.put("entity",data.is_group+1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION)
            messengerSocket!!.emit(SocketEvents.SEND_ATTACHMENT,sendObject)
            //messengerSocket!!.once("tm_error",data)
        }else{
            newMessageVerifyChatArchive(data.receiver_id,data.is_group+1)
        }
    }
    fun sendForkOutAttachments(data: List<MessageData>,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.isEmpty()){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data[0].attachment!!.isNullOrEmpty()){
            callBackObject.put("message","Attachment cannot be empty")
            listener.tmError(callBackObject)
            return
        }
        var lengthOfList =  data.size
        var forkOutArray =JSONArray()
        for (i in 0 until lengthOfList){
            var currentTime = ConstantValues.fetchCurrentTimeInUTC()
            var messenger = Messenger()
            messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
            messenger.receiver_id = data[i].receiver_id.toLong()
            messenger.message = ""
            messenger.message_type = ConstantValues.MessageTypes.ATTACHMENT
            messenger.attachment = data[i].attachment
            messenger.preview_link = data[i].preview_link
            messenger.caption = data[i].caption
            messenger.is_group = data[i].is_group
            if (!data[i].local_attachment_path.isNullOrEmpty()){
                messenger.local_attachment_path = data[i].local_attachment_path
                messenger.attachment_downloaded = 1
            }
            messenger.message_id = 0
            messenger.is_sync = 0
            messenger.is_read = 0
            messenger.is_reply = 0
            messenger.original_message_id = 0
            messenger.original_message = "{}"
            messenger.is_delivered = 0
            messenger.status = 1
            messenger.is_forward  = 0
            messenger.is_flag = 0
            messenger.is_forkout = 1
            messenger.conversation_reference_id = data[i].conversation_reference_id
            messenger.created_at = currentTime
            messenger.updated_at = currentTime
            messenger.sender_uid = data[i].sender_uid
            messenger.receiver_uid =data[i].receiver_uid
            messenger.is_room = data[i].is_room
            messenger.sender_name = APP_LOGIN_USER_NAME
            messenger.isMine = 1
            dataBase!!.messengerDAO.insertMessage(messenger)
            var localId = dataBase!!.messengerDAO.fetchLastLocalId()
            var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
            //var messageObject = Gson().toJson(messenger)
            //listener.messageSaved(JSONObject(messageObject))
            var encryptedAttachment = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data[i].attachment,data[i].receiver_id,data[i].is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data[i].receiver_id)
            var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data[i].caption,data[i].receiver_id,data[i].is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data[i].receiver_id)
            var sendObject = JSONObject()
            sendObject.put("receiver_id",data[i].receiver_id)
            sendObject.put("is_group",data[i].is_group)
            sendObject.put("is_room",data[i].is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message","")
            sendObject.put("attachment",encryptedAttachment)
            sendObject.put("caption",encryptedCaption)
            //sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data[i].conversation_reference_id)
            sendObject.put("receiver_uid",data[i].receiver_uid)
            sendObject.put("sender_uid",data[i].sender_uid)
            sendObject.put("entity",data[i].is_group+1)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message_type",ConstantValues.MessageTypes.ATTACHMENT)
            sendObject.put("is_forkout",1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION)
            forkOutArray.put(sendObject)
        }
        TroopSocketClient.updateRecentUserGroupMessage()
        messengerSocket!!.emit(SocketEvents.FORK_OUT,forkOutArray)
    }
    fun sendAudioMessage(data: MessageData,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid Receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid Receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==-1 || (data.is_group!=0 && data.is_group!=1)){
            callBackObject.put("message","Value fot is_group not specified")
            listener.tmError(callBackObject)
            return
        }
        if (data.attachment!!.isNullOrEmpty()){
            callBackObject.put("message","Attachment cannot be empty")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==0){
            if (dataBase!!.userDAO.checkIsUserIsBlocked(data.receiver_uid)==1){
                return
            }
        }
        setTyping(data.receiver_id,data.is_group,0)
        var currentTime = ConstantValues.fetchCurrentTimeInUTC()
        var messenger = Messenger()
        messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
        messenger.receiver_id = data.receiver_id.toLong()
        messenger.message = ""
        messenger.message_type = ConstantValues.MessageTypes.AUDIO_MESSAGE
        messenger.attachment = data.attachment
        if (!data.local_attachment_path.isNullOrEmpty()){
            messenger.local_attachment_path = data.local_attachment_path
            messenger.attachment_downloaded = 1
        }
        messenger.preview_link = data.preview_link
        messenger.caption = data.caption
        messenger.is_group = data.is_group
        messenger.message_id = 0
        messenger.is_sync = 0
        messenger.is_read = 0
        messenger.is_reply = 0
        messenger.original_message_id = 0
        messenger.original_message = "{}"
        messenger.is_delivered = 0
        messenger.status = 1
        messenger.is_forward  = 0
        messenger.is_flag = 0
        messenger.is_forkout = 0
        messenger.conversation_reference_id = data.conversation_reference_id
        messenger.created_at = currentTime
        messenger.updated_at = currentTime
        messenger.sender_uid = data.sender_uid
        messenger.receiver_uid =data.receiver_uid
        messenger.is_room = data.is_room
        messenger.sender_name = APP_LOGIN_USER_NAME
        messenger.isMine = 1
        dataBase!!.messengerDAO.insertMessage(messenger)
        var localId = dataBase!!.messengerDAO.fetchLastLocalId()
        if (data.is_group == 0 ){
            dataBase!!.userDAO.updateUserRecentMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.AUDIO_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }else{
            dataBase!!.messengerGroupDAO.updateRecentGroupMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.AUDIO_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }
        var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
        var messageObject = Gson().toJson(messenger)
        listener.messageSaved(JSONObject(messageObject))
        if (CLIENT_IS_CONNECTED){
            var encryptedAttachment = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.attachment,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            //var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.caption,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("receiver_id",data.receiver_id)
            sendObject.put("is_group",data.is_group)
            sendObject.put("is_room",data.is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message","")
            sendObject.put("attachment",encryptedAttachment)
            //sendObject.put("caption",encryptedCaption)
            //sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data.conversation_reference_id)
            sendObject.put("receiver_uid",data.receiver_uid)
            sendObject.put("sender_uid",data.sender_uid)
            sendObject.put("entity",data.is_group+1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION)
            messengerSocket!!.emit(SocketEvents.SEND_AUDIO_MESSAGE,sendObject)
            //messengerSocket!!.once("tm_error",data)
        }else{
            newMessageVerifyChatArchive(data.receiver_id,data.is_group+1)
        }
    }
    fun sendForkOutAudioMessages(data: List<MessageData>, listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.isEmpty()){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data[0].attachment!!.isNullOrEmpty()){
            callBackObject.put("message","Attachment cannot be empty")
            listener.tmError(callBackObject)
            return
        }
        var lengthOfList =  data.size
        var forkOutArray =JSONArray()
        for (i in 0 until lengthOfList){
            var currentTime = ConstantValues.fetchCurrentTimeInUTC()
            var messenger = Messenger()
            messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
            messenger.receiver_id = data[i].receiver_id.toLong()
            messenger.message = ""
            messenger.message_type = ConstantValues.MessageTypes.AUDIO_MESSAGE
            messenger.attachment = data[i].attachment
            messenger.preview_link = data[i].preview_link
            messenger.local_attachment_path = data[i].local_attachment_path
            messenger.attachment_downloaded = 1
            messenger.caption = data[i].caption
            messenger.is_group = data[i].is_group
            messenger.message_id = 0
            messenger.is_sync = 0
            messenger.is_read = 0
            messenger.is_reply = 0
            messenger.original_message_id = 0
            messenger.original_message = "{}"
            messenger.is_delivered = 0
            messenger.status = 1
            messenger.is_forward  = 0
            messenger.is_flag = 0
            messenger.is_forkout = 1
            messenger.conversation_reference_id = data[i].conversation_reference_id
            messenger.created_at = currentTime
            messenger.updated_at = currentTime
            messenger.sender_uid = data[i].sender_uid
            messenger.receiver_uid =data[i].receiver_uid
            messenger.is_room = data[i].is_room
            messenger.sender_name = APP_LOGIN_USER_NAME
            messenger.isMine = 1
            dataBase!!.messengerDAO.insertMessage(messenger)
            var localId = dataBase!!.messengerDAO.fetchLastLocalId()
            var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
            //var messageObject = Gson().toJson(messenger)
            //listener.messageSaved(JSONObject(messageObject))
            var encryptedAttachment = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data[i].attachment,data[i].receiver_id,data[i].is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data[i].receiver_id)
            var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data[i].caption,data[i].receiver_id,data[i].is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data[i].receiver_id)
            var sendObject = JSONObject()
            sendObject.put("receiver_id",data[i].receiver_id)
            sendObject.put("is_group",data[i].is_group)
            sendObject.put("is_room",data[i].is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message","")
            sendObject.put("attachment",encryptedAttachment)
            sendObject.put("caption",encryptedCaption)
            //sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data[i].conversation_reference_id)
            sendObject.put("receiver_uid",data[i].receiver_uid)
            sendObject.put("sender_uid",data[i].sender_uid)
            sendObject.put("entity",data[i].is_group+1)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message_type",ConstantValues.MessageTypes.AUDIO_MESSAGE)
            sendObject.put("is_forkout",1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION)
            forkOutArray.put(sendObject)
        }
        TroopSocketClient.updateRecentUserGroupMessage()
        messengerSocket!!.emit(SocketEvents.FORK_OUT,forkOutArray)
    }
    fun sendContactMessage(data: MessageData,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid Receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid Receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==-1 || (data.is_group!=0 && data.is_group!=1)){
            callBackObject.put("message","Value fot is_group not specified")
            listener.tmError(callBackObject)
            return
        }
        if (data.contact_name!!.isNullOrEmpty()){
            callBackObject.put("message","Value for contact_name is required")
            listener.tmError(callBackObject)
            return
        }
        if (data.contact_number!!.isNullOrEmpty()){
            callBackObject.put("message","Value for contact_number is required")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==0){
            if (dataBase!!.userDAO.checkIsUserIsBlocked(data.receiver_uid)==1){
                return
            }
        }
        var contactObject = JSONObject()
        contactObject.put("contact_name",data.contact_name)
        contactObject.put("contact_number",data.contact_number)
        setTyping(data.receiver_id,data.is_group,0)
        var currentTime = ConstantValues.fetchCurrentTimeInUTC()
        var messenger = Messenger()
        messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
        messenger.receiver_id = data.receiver_id.toLong()
        messenger.message = contactObject.toString()
        messenger.message_type = ConstantValues.MessageTypes.CONTACT_MESSAGE
        messenger.attachment = ""
        messenger.preview_link = ""
        messenger.caption = ""
        messenger.is_group = data.is_group
        messenger.message_id = 0
        messenger.is_sync = 0
        messenger.is_read = 0
        messenger.is_reply = 0
        messenger.original_message_id = 0
        messenger.original_message = "{}"
        messenger.is_delivered = 0
        messenger.status = 1
        messenger.is_forward  = 0
        messenger.is_flag = 0
        messenger.is_forkout = 0
        messenger.conversation_reference_id = data.conversation_reference_id
        messenger.created_at = currentTime
        messenger.updated_at = currentTime
        messenger.sender_uid = data.sender_uid
        messenger.receiver_uid =data.receiver_uid
        messenger.is_room = data.is_room
        messenger.sender_name = APP_LOGIN_USER_NAME
        messenger.isMine = 1
        dataBase!!.messengerDAO.insertMessage(messenger)
        var localId = dataBase!!.messengerDAO.fetchLastLocalId()
        if (data.is_group == 0 ){
            dataBase!!.userDAO.updateUserRecentMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.CONTACT_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }else{
            dataBase!!.messengerGroupDAO.updateRecentGroupMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.CONTACT_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }
        var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
        var messageObject = Gson().toJson(messenger)
        listener.messageSaved(JSONObject(messageObject))
        if (CLIENT_IS_CONNECTED){
            var encryptedMessage = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,messenger!!.message!!,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            //var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.caption,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("receiver_id",data.receiver_id)
            sendObject.put("is_group",data.is_group)
            sendObject.put("is_room",data.is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message",encryptedMessage)
            //sendObject.put("attachment",encryptedAttachment)
            //sendObject.put("caption",encryptedCaption)
            //sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            /*sendObject.put("contact_name",data.contact_name)
            sendObject.put("contact_number",data.contact_number)*/
            sendObject.put("conversation_reference_id",data.conversation_reference_id)
            sendObject.put("receiver_uid",data.receiver_uid)
            sendObject.put("sender_uid",data.sender_uid)
            sendObject.put("entity",data.is_group+1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION)
            messengerSocket!!.emit(SocketEvents.SEND_CONTACT_V2,sendObject)
            //messengerSocket!!.once("tm_error",data)
        }else{
            newMessageVerifyChatArchive(data.receiver_id,data.is_group+1)
        }
    }
    fun sendForkOutContactMessages(data: List<MessageData>, listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.isEmpty()){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data[0].contact_name!!.isNullOrEmpty()){
            callBackObject.put("message","Value for contact_name is required")
            listener.tmError(callBackObject)
            return
        }
        if (data[0].contact_number!!.isNullOrEmpty()){
            callBackObject.put("message","Value for contact_number is required")
            listener.tmError(callBackObject)
            return
        }
        var lengthOfList =  data.size
        var forkOutArray =JSONArray()

        for (i in 0 until lengthOfList){
            var contactObject = JSONObject()
            contactObject.put("contact_name",data[i].contact_name)
            contactObject.put("contact_number",data[i].contact_number)
            var currentTime = ConstantValues.fetchCurrentTimeInUTC()
            var messenger = Messenger()
            messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
            messenger.receiver_id = data[i].receiver_id.toLong()
            messenger.message = contactObject.toString()
            messenger.message_type = ConstantValues.MessageTypes.CONTACT_MESSAGE
            messenger.attachment = ""
            messenger.preview_link = ""
            messenger.caption = ""
            messenger.is_group = data[i].is_group
            messenger.message_id = 0
            messenger.is_sync = 0
            messenger.is_read = 0
            messenger.is_reply = 0
            messenger.original_message_id = 0
            messenger.original_message = "{}"
            messenger.is_delivered = 0
            messenger.status = 1
            messenger.is_forward  = 0
            messenger.is_flag = 0
            messenger.is_forkout = 1
            messenger.conversation_reference_id = data[i].conversation_reference_id
            messenger.created_at = currentTime
            messenger.updated_at = currentTime
            messenger.sender_uid = data[i].sender_uid
            messenger.receiver_uid =data[i].receiver_uid
            messenger.is_room = data[i].is_room
            messenger.sender_name = APP_LOGIN_USER_NAME
            messenger.isMine = 1
            dataBase!!.messengerDAO.insertMessage(messenger)
            var localId = dataBase!!.messengerDAO.fetchLastLocalId()
            var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
            //var messageObject = Gson().toJson(messenger)
            //listener.messageSaved(JSONObject(messageObject))
            var encryptedMessage = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,messenger!!.message!!,data[i].receiver_id,data[i].is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data[i].receiver_id)
            //var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.caption,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("receiver_id",data[i].receiver_id)
            sendObject.put("is_group",data[i].is_group)
            sendObject.put("is_room",data[i].is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message",encryptedMessage)
            /*sendObject.put("contact_name",data[i].contact_name)
            sendObject.put("contact_number",data[i].contact_number)*/
            //sendObject.put("attachment",encryptedAttachment)
            //sendObject.put("caption",encryptedCaption)
            //sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data[i].conversation_reference_id)
            sendObject.put("receiver_uid",data[i].receiver_uid)
            sendObject.put("sender_uid",data[i].sender_uid)
            sendObject.put("entity",data[i].is_group+1)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message_type",ConstantValues.MessageTypes.CONTACT_MESSAGE)
            sendObject.put("is_forkout",1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
            forkOutArray.put(sendObject)
        }
        TroopSocketClient.updateRecentUserGroupMessage()
        messengerSocket!!.emit(SocketEvents.FORK_OUT,forkOutArray)
    }
    fun sendLocationMessage(data: MessageData,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid Receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid Receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==-1 || (data.is_group!=0 && data.is_group!=1)){
            callBackObject.put("message","Value fot is_group not specified")
            listener.tmError(callBackObject)
            return
        }
        if (data.location_longitude!!.isNullOrEmpty()){
            callBackObject.put("message","Value for location_longitude is required")
            listener.tmError(callBackObject)
            return
        }
        if (data.location_latitude!!.isNullOrEmpty()){
            callBackObject.put("message","Value for location_latitude is required")
            listener.tmError(callBackObject)
            return
        }
        if (data.location_address!!.isNullOrEmpty()){
            callBackObject.put("message","Value for location_address is required")
            listener.tmError(callBackObject)
            return
        }
        if (data.location_name!!.isNullOrEmpty()){
            callBackObject.put("message","Value for location_name is required")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==0){
            if (dataBase!!.userDAO.checkIsUserIsBlocked(data.receiver_uid)==1){
                return
            }
        }
        var locationObject = JSONObject()
        locationObject.put("location_latitude",data.location_latitude)
        locationObject.put("location_longitude",data.location_longitude)
        locationObject.put("location_address",data.location_address)
        locationObject.put("location_name",data.location_name)
        setTyping(data.receiver_id,data.is_group,0)
        var currentTime = ConstantValues.fetchCurrentTimeInUTC()
        var messenger = Messenger()
        messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
        messenger.receiver_id = data.receiver_id.toLong()
        messenger.message = locationObject.toString()
        messenger.message_type = ConstantValues.MessageTypes.LOCATION_MESSAGE
        messenger.attachment = ""
        messenger.preview_link = ""
        messenger.caption = ""
        messenger.is_group = data.is_group
        messenger.message_id = 0
        messenger.is_sync = 0
        messenger.is_read = 0
        messenger.is_reply = 0
        messenger.original_message_id = 0
        messenger.original_message = "{}"
        messenger.is_delivered = 0
        messenger.status = 1
        messenger.is_forward  = 0
        messenger.is_flag = 0
        messenger.is_forkout = 0
        messenger.conversation_reference_id = data.conversation_reference_id
        messenger.created_at = currentTime
        messenger.updated_at = currentTime
        messenger.sender_uid = data.sender_uid
        messenger.receiver_uid =data.receiver_uid
        messenger.is_room = data.is_room
        messenger.sender_name = APP_LOGIN_USER_NAME
        messenger.isMine = 1
        dataBase!!.messengerDAO.insertMessage(messenger)
        var localId = dataBase!!.messengerDAO.fetchLastLocalId()
        if (data.is_group == 0 ){
            dataBase!!.userDAO.updateUserRecentMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.LOCATION_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }else{
            dataBase!!.messengerGroupDAO.updateRecentGroupMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.LOCATION_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }
        var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
        var messageObject = Gson().toJson(messenger)
        listener.messageSaved(JSONObject(messageObject))
        if (CLIENT_IS_CONNECTED){
            var encryptedMessage = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,messenger!!.message!!,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            //var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.caption,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("receiver_id",data.receiver_id)
            sendObject.put("is_group",data.is_group)
            sendObject.put("is_room",data.is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message",encryptedMessage)
            //sendObject.put("attachment",encryptedAttachment)
            //sendObject.put("caption",encryptedCaption)
            //sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data.conversation_reference_id)
            sendObject.put("receiver_uid",data.receiver_uid)
            sendObject.put("sender_uid",data.sender_uid)
            sendObject.put("entity",data.is_group+1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
            /*sendObject.put("location_latitude",data.location_latitude)
            sendObject.put("location_longitude",data.location_longitude)
            sendObject.put("location_address",data.location_address)
            sendObject.put("location_name",data.location_name)*/
            messengerSocket!!.emit(SocketEvents.SEND_LOCATION_V2,sendObject)
            //messengerSocket!!.once("tm_error",data)
        }else{
            newMessageVerifyChatArchive(data.receiver_id,data.is_group+1)
        }
    }
    fun sendForkOutLocationMessages(data: List<MessageData>, listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.isEmpty()){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data[0].location_longitude!!.isNullOrEmpty()){
            callBackObject.put("message","Value for location_longitude is required")
            listener.tmError(callBackObject)
            return
        }
        if (data[0].location_latitude!!.isNullOrEmpty()){
            callBackObject.put("message","Value for location_latitude is required")
            listener.tmError(callBackObject)
            return
        }
        var lengthOfList =  data.size
        var forkOutArray =JSONArray()

        for (i in 0 until lengthOfList){
            var locationObject = JSONObject()
            locationObject.put("location_latitude",data[i].location_latitude)
            locationObject.put("location_longitude",data[i].location_longitude)
            locationObject.put("location_address",data[i].location_address)
            locationObject.put("location_name",data[i].location_name)
            var currentTime = ConstantValues.fetchCurrentTimeInUTC()
            var messenger = Messenger()
            messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
            messenger.receiver_id = data[i].receiver_id.toLong()
            messenger.message = locationObject.toString()
            messenger.message_type = ConstantValues.MessageTypes.LOCATION_MESSAGE
            messenger.attachment = ""
            messenger.preview_link = ""
            messenger.caption = ""
            messenger.is_group = data[i].is_group
            messenger.message_id = 0
            messenger.is_sync = 0
            messenger.is_read = 0
            messenger.is_reply = 0
            messenger.original_message_id = 0
            messenger.original_message = "{}"
            messenger.is_delivered = 0
            messenger.status = 1
            messenger.is_forward  = 0
            messenger.is_flag = 0
            messenger.is_forkout = 1
            messenger.conversation_reference_id = data[i].conversation_reference_id
            messenger.created_at = currentTime
            messenger.updated_at = currentTime
            messenger.sender_uid = data[i].sender_uid
            messenger.receiver_uid =data[i].receiver_uid
            messenger.is_room = data[i].is_room
            messenger.sender_name = APP_LOGIN_USER_NAME
            messenger.isMine = 1
            dataBase!!.messengerDAO.insertMessage(messenger)
            var localId = dataBase!!.messengerDAO.fetchLastLocalId()
            var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
            //var messageObject = Gson().toJson(messenger)
            //listener.messageSaved(JSONObject(messageObject))
            var encryptedMessage = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,messenger.message!!,data[i].receiver_id,data[i].is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data[i].receiver_id)
            //var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.caption,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("receiver_id",data[i].receiver_id)
            sendObject.put("is_group",data[i].is_group)
            sendObject.put("is_room",data[i].is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message",encryptedMessage)
            /*sendObject.put("location_latitude",data[i].location_latitude)
            sendObject.put("location_longitude",data[i].location_longitude)
            sendObject.put("location_address",data[i].location_address)
            sendObject.put("location_name",data[i].location_name)*/
            //sendObject.put("attachment",encryptedAttachment)
            //sendObject.put("caption",encryptedCaption)
            //sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data[i].conversation_reference_id)
            sendObject.put("receiver_uid",data[i].receiver_uid)
            sendObject.put("sender_uid",data[i].sender_uid)
            sendObject.put("entity",data[i].is_group+1)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message_type",ConstantValues.MessageTypes.LOCATION_MESSAGE)
            sendObject.put("is_forkout",1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
            forkOutArray.put(sendObject)
        }
        TroopSocketClient.updateRecentUserGroupMessage()
        messengerSocket!!.emit(SocketEvents.FORK_OUT,forkOutArray)
    }
    fun sendActivityMessage(data: MessageData,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (!CLIENT_IS_CONNECTED){
            callBackObject.put("message","Connection to server is not yet established!")
            listener.tmError(callBackObject)
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid Receiver")
            listener.tmError(callBackObject)
        }
        if (data.is_group==-1){
            callBackObject.put("message","Invalid value specified for is_group")
            listener.tmError(callBackObject)
        }
        if (data.message.isNullOrEmpty()){
            callBackObject.put("message","Message cannot be empty")
            listener.tmError(callBackObject)
        }
        var messageObject = JSONObject()
        if (data.message.contains("{") && data.message.contains("}")){
            messageObject = Helper.stringToJsonObject(data.message)!!
        }else{
            callBackObject.put("message","Message must be JSONObject")
            listener.tmError(callBackObject)
        }
        var sendActivityObject = JSONObject()
        sendActivityObject.put("receiver_id",data.receiver_id)
        sendActivityObject.put("is_group",data.is_group)
        sendActivityObject.put("is_room",data.is_room)
        sendActivityObject.put("access_token", APP_ACCESS_TOKEN)
        sendActivityObject.put("message",messageObject)
        sendActivityObject.put("conversation_reference_id",data.conversation_reference_id)
        sendActivityObject.put("receiver_uid",data.receiver_uid)
        sendActivityObject.put("sender_uid",data.sender_uid)
        sendActivityObject.put("entity",data.is_group+1)
        sendActivityObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
        messengerSocket!!.emit(SocketEvents.SEND_ACTIVITY_MESSAGE,sendActivityObject)
    }
    fun sendForkOutActivityMessages(data: List<MessageData>, listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.isEmpty()){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data[0].message.isNullOrEmpty()){
            callBackObject.put("message","Message cannot be empty")
            listener.tmError(callBackObject)
        }
        var messageObject = JSONObject()
        if (data[0].message.contains("{") && data[0].message.contains("}")){
            messageObject = Helper.stringToJsonObject(data[0].message)!!
        }else{
            callBackObject.put("message","Message must be JSONObject")
            listener.tmError(callBackObject)
        }
        var lengthOfList =  data.size
        var forkOutArray =JSONArray()
        for (i in 0 until lengthOfList){
            var currentTime = ConstantValues.fetchCurrentTimeInUTC()
            var messenger = Messenger()
            messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
            messenger.receiver_id = data[i].receiver_id.toLong()
            messenger.message = messageObject.toString()
            messenger.message_type = ConstantValues.MessageTypes.ACTIVITY
            messenger.attachment = ""
            messenger.preview_link = ""
            messenger.caption = ""
            messenger.is_group = data[i].is_group
            messenger.message_id = 0
            messenger.is_sync = 0
            messenger.is_read = 0
            messenger.is_reply = 0
            messenger.original_message_id = 0
            messenger.original_message = "{}"
            messenger.is_delivered = 0
            messenger.status = 1
            messenger.is_forward  = 0
            messenger.is_flag = 0
            messenger.is_forkout = 1
            messenger.conversation_reference_id = data[i].conversation_reference_id
            messenger.created_at = currentTime
            messenger.updated_at = currentTime
            messenger.sender_uid = data[i].sender_uid
            messenger.receiver_uid =data[i].receiver_uid
            messenger.is_room = data[i].is_room
            messenger.sender_name = APP_LOGIN_USER_NAME
            messenger.isMine = 1
            dataBase!!.messengerDAO.insertMessage(messenger)
            var localId = dataBase!!.messengerDAO.fetchLastLocalId()
            var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
            //var messageObject = Gson().toJson(messenger)
            //listener.messageSaved(JSONObject(messageObject))
            var encryptedMessage = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data[i].message,data[i].receiver_id,data[i].is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data[i].receiver_id)
            //var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.caption,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("receiver_id",data[i].receiver_id)
            sendObject.put("is_group",data[i].is_group)
            sendObject.put("is_room",data[i].is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message",encryptedMessage)
            //sendObject.put("attachment",encryptedAttachment)
            //sendObject.put("caption",encryptedCaption)
            //sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data[i].conversation_reference_id)
            sendObject.put("receiver_uid",data[i].receiver_uid)
            sendObject.put("sender_uid",data[i].sender_uid)
            sendObject.put("entity",data[i].is_group+1)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message_type",ConstantValues.MessageTypes.ACTIVITY)
            sendObject.put("is_forkout",1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
            forkOutArray.put(sendObject)
        }
        TroopSocketClient.updateRecentUserGroupMessage()
        messengerSocket!!.emit(SocketEvents.FORK_OUT,forkOutArray)
    }
    fun sendTextReplyMessage(data: MessageData,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.message_id<=0){
            callBackObject.put("message","Invalid Message")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==-1){
            callBackObject.put("message","Value for is_group not specified")
            listener.tmError(callBackObject)
            return
        }
        if (data.message.isNullOrEmpty()){
            callBackObject.put("message","Message cannot be empty")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==0){
            if (dataBase!!.userDAO.checkIsUserIsBlocked(data.receiver_uid)==1){
                return
            }
        }
        setTyping(data.receiver_id,data.is_group+1,0)
        var currentTime = ConstantValues.fetchCurrentTimeInUTC()
        var originalMessageData = dataBase!!.messengerDAO.fetchReplyOriginalMessageDetails(data.message_id)

        var messenger = Messenger()
        messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
        messenger.receiver_id = data.receiver_id.toLong()
        messenger.message = data.message
        messenger.message_type = ConstantValues.MessageTypes.TEXT_MESSAGE
        messenger.attachment = ""
        messenger.preview_link = ""
        messenger.caption = ""
        messenger.is_group = data.is_group
        messenger.message_id = 0
        messenger.is_sync = 0
        messenger.is_read = 0
        messenger.is_reply = 1
        messenger.original_message_id = data.message_id
        messenger.original_message = Gson().toJson(originalMessageData)
        messenger.is_delivered = 0
        messenger.status = 1
        messenger.is_forward  = 0
        messenger.is_flag = 0
        messenger.is_forkout = 0
        messenger.conversation_reference_id = data.conversation_reference_id
        messenger.created_at = currentTime
        messenger.updated_at = currentTime
        messenger.sender_uid = data.sender_uid
        messenger.receiver_uid =data.receiver_uid
        messenger.is_room = data.is_room
        messenger.sender_name = APP_LOGIN_USER_NAME
        messenger.isMine = 1
        dataBase!!.messengerDAO.insertMessage(messenger)
        var localId = dataBase!!.messengerDAO.fetchLastLocalId()
        if (data.is_group == 0 ){
            dataBase!!.userDAO.updateUserRecentMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.TEXT_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }else{
            dataBase!!.messengerGroupDAO.updateRecentGroupMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.TEXT_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }
        var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
        var messageObject = Gson().toJson(messenger)
        listener.messageSaved(JSONObject(messageObject))
        if (CLIENT_IS_CONNECTED){
            var encryptedMessage = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.message,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            //var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.caption,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("status",1)
            sendObject.put("message_type",ConstantValues.MessageTypes.TEXT_MESSAGE)
            sendObject.put("receiver_id",data.receiver_id)
            sendObject.put("is_group",data.is_group)
            sendObject.put("is_room",data.is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message",encryptedMessage)
            sendObject.put("message_id",data.message_id)
            //sendObject.put("attachment",encryptedAttachment)
            //sendObject.put("caption",encryptedCaption)
            //sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data.conversation_reference_id)
            sendObject.put("receiver_uid",data.receiver_uid)
            sendObject.put("sender_uid",data.sender_uid)
            sendObject.put("entity",data.is_group+1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
            messengerSocket!!.emit(SocketEvents.REPLY_MESSAGE,sendObject)
            //messengerSocket!!.once("tm_error",data)
        }else{
            newMessageVerifyChatArchive(data.receiver_id,data.is_group+1)
        }
    }
    fun sendAttachmentReplyMessage(data: MessageData,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.message_id<=0){
            callBackObject.put("message","Invalid Message")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==-1){
            callBackObject.put("message","Value for is_group not specified")
            listener.tmError(callBackObject)
            return
        }
        if (data.attachment.isNullOrEmpty()){
            callBackObject.put("message","attachment cannot be empty")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==0){
            if (dataBase!!.userDAO.checkIsUserIsBlocked(data.receiver_uid)==1){
                return
            }
        }
        setTyping(data.receiver_id,data.is_group+1,0)
        var currentTime = ConstantValues.fetchCurrentTimeInUTC()
        var originalMessageData = dataBase!!.messengerDAO.fetchReplyOriginalMessageDetails(data.message_id)

        var messenger = Messenger()
        messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
        messenger.receiver_id = data.receiver_id.toLong()
        messenger.message = ""
        messenger.message_type = ConstantValues.MessageTypes.ATTACHMENT
        messenger.attachment = data.attachment
        messenger.preview_link = data.preview_link
        if (!data.local_attachment_path.isNullOrEmpty()){
            messenger.local_attachment_path = data.local_attachment_path
            messenger.attachment_downloaded = 1
        }
        messenger.caption = data.caption
        messenger.is_group = data.is_group
        messenger.message_id = 0
        messenger.is_sync = 0
        messenger.is_read = 0
        messenger.is_reply = 1
        messenger.original_message_id = data.message_id
        messenger.original_message = Gson().toJson(originalMessageData)
        messenger.is_delivered = 0
        messenger.status = 1
        messenger.is_forward  = 0
        messenger.is_flag = 0
        messenger.is_forkout = 0
        messenger.conversation_reference_id = data.conversation_reference_id
        messenger.created_at = currentTime
        messenger.updated_at = currentTime
        messenger.sender_uid = data.sender_uid
        messenger.receiver_uid =data.receiver_uid
        messenger.is_room = data.is_room
        messenger.sender_name = APP_LOGIN_USER_NAME
        messenger.isMine = 1
        dataBase!!.messengerDAO.insertMessage(messenger)
        var localId = dataBase!!.messengerDAO.fetchLastLocalId()
        if (data.is_group == 0 ){
            dataBase!!.userDAO.updateUserRecentMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.ATTACHMENT,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }else{
            dataBase!!.messengerGroupDAO.updateRecentGroupMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.ATTACHMENT,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }
        var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
        var messageObject = Gson().toJson(messenger)
        listener.messageSaved(JSONObject(messageObject))
        if (CLIENT_IS_CONNECTED){
            var encryptedAttachment = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.attachment,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.caption,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("status",1)
            sendObject.put("message_type",ConstantValues.MessageTypes.ATTACHMENT)
            sendObject.put("receiver_id",data.receiver_id)
            sendObject.put("is_group",data.is_group)
            sendObject.put("is_room",data.is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message","")
            sendObject.put("message_id",data.message_id)
            sendObject.put("attachment",encryptedAttachment)
            sendObject.put("caption",encryptedCaption)
            sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data.conversation_reference_id)
            sendObject.put("receiver_uid",data.receiver_uid)
            sendObject.put("sender_uid",data.sender_uid)
            sendObject.put("entity",data.is_group+1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
            messengerSocket!!.emit(SocketEvents.REPLY_MESSAGE,sendObject)
            //messengerSocket!!.once("tm_error",data)
        }else{
            newMessageVerifyChatArchive(data.receiver_id,data.is_group+1)
        }
    }
    fun sendContactReplyMessage(data: MessageData,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.message_id<=0){
            callBackObject.put("message","Invalid Message")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==-1){
            callBackObject.put("message","Value for is_group not specified")
            listener.tmError(callBackObject)
            return
        }
        if (data.contact_name.isNullOrEmpty()){
            callBackObject.put("message","Value for contact_name is required")
            listener.tmError(callBackObject)
            return
        }
        if (data.contact_number.isNullOrEmpty()){
            callBackObject.put("message","Value for contact_number is required")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==0){
            if (dataBase!!.userDAO.checkIsUserIsBlocked(data.receiver_uid)==1){
                return
            }
        }
        var contactObject = JSONObject()
        contactObject.put("contact_name",data.contact_name)
        contactObject.put("contact_number",data.contact_number)
        setTyping(data.receiver_id,data.is_group+1,0)
        var currentTime = ConstantValues.fetchCurrentTimeInUTC()
        var originalMessageData = dataBase!!.messengerDAO.fetchReplyOriginalMessageDetails(data.message_id)

        var messenger = Messenger()
        messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
        messenger.receiver_id = data.receiver_id.toLong()
        messenger.message = contactObject.toString()
        messenger.message_type = ConstantValues.MessageTypes.CONTACT_MESSAGE
        messenger.attachment = ""
        messenger.preview_link = ""
        messenger.caption = ""
        messenger.is_group = data.is_group
        messenger.message_id = 0
        messenger.is_sync = 0
        messenger.is_read = 0
        messenger.is_reply = 1
        messenger.original_message_id = data.message_id
        messenger.original_message = Gson().toJson(originalMessageData)
        messenger.is_delivered = 0
        messenger.status = 1
        messenger.is_forward  = 0
        messenger.is_flag = 0
        messenger.is_forkout = 0
        messenger.conversation_reference_id = data.conversation_reference_id
        messenger.created_at = currentTime
        messenger.updated_at = currentTime
        messenger.sender_uid = data.sender_uid
        messenger.receiver_uid =data.receiver_uid
        messenger.is_room = data.is_room
        messenger.sender_name = APP_LOGIN_USER_NAME
        messenger.isMine = 1
        dataBase!!.messengerDAO.insertMessage(messenger)
        var localId = dataBase!!.messengerDAO.fetchLastLocalId()
        if (data.is_group == 0 ){
            dataBase!!.userDAO.updateUserRecentMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.CONTACT_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }else{
            dataBase!!.messengerGroupDAO.updateRecentGroupMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.CONTACT_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }
        var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
        var messageObject = Gson().toJson(messenger)
        listener.messageSaved(JSONObject(messageObject))
        if (CLIENT_IS_CONNECTED){
            var encryptedMessage = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,messenger.message!!,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            //var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.caption,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("status",1)
            sendObject.put("message_type",ConstantValues.MessageTypes.CONTACT_MESSAGE)
            sendObject.put("receiver_id",data.receiver_id)
            sendObject.put("is_group",data.is_group)
            sendObject.put("is_room",data.is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message",encryptedMessage)
            sendObject.put("message_id",data.message_id)
            //sendObject.put("attachment",encryptedAttachment)
            //sendObject.put("caption",encryptedCaption)
            //sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data.conversation_reference_id)
            sendObject.put("receiver_uid",data.receiver_uid)
            sendObject.put("sender_uid",data.sender_uid)
            sendObject.put("entity",data.is_group+1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
            messengerSocket!!.emit(SocketEvents.REPLY_MESSAGE,sendObject)
            //messengerSocket!!.once("tm_error",data)
        }else{
            newMessageVerifyChatArchive(data.receiver_id,data.is_group+1)
        }
    }
    fun sendLocationReplyMessage(data: MessageData,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.message_id<=0){
            callBackObject.put("message","Invalid Message")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==-1){
            callBackObject.put("message","Value for is_group not specified")
            listener.tmError(callBackObject)
            return
        }
        if (data.location_latitude.isNullOrEmpty()){
            callBackObject.put("message","Value for location_latitude is required")
            listener.tmError(callBackObject)
            return
        }
        if (data.location_longitude.isNullOrEmpty()){
            callBackObject.put("message","Value for location_longitude is required")
            listener.tmError(callBackObject)
            return
        }
        if (data.location_address.isNullOrEmpty()){
            callBackObject.put("message","Value for location_address is required")
            listener.tmError(callBackObject)
            return
        }
        if (data.location_name.isNullOrEmpty()){
            callBackObject.put("message","Value for location_name is required")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==0){
            if (dataBase!!.userDAO.checkIsUserIsBlocked(data.receiver_uid)==1){
                return
            }
        }
        var locatpionObject = JSONObject()
        locatpionObject.put("location_latitude",data.location_latitude)
        locatpionObject.put("location_longitude",data.location_longitude)
        locatpionObject.put("location_address",data.location_address)
        locatpionObject.put("location_name",data.location_name)
        setTyping(data.receiver_id,data.is_group+1,0)
        var currentTime = ConstantValues.fetchCurrentTimeInUTC()
        var originalMessageData = dataBase!!.messengerDAO.fetchReplyOriginalMessageDetails(data.message_id)

        var messenger = Messenger()
        messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
        messenger.receiver_id = data.receiver_id.toLong()
        messenger.message = locatpionObject.toString()
        messenger.message_type = ConstantValues.MessageTypes.LOCATION_MESSAGE
        messenger.attachment = ""
        messenger.preview_link = ""
        messenger.caption = ""
        messenger.is_group = data.is_group
        messenger.message_id = 0
        messenger.is_sync = 0
        messenger.is_read = 0
        messenger.is_reply = 1
        messenger.original_message_id = data.message_id
        messenger.original_message = Gson().toJson(originalMessageData)
        messenger.is_delivered = 0
        messenger.status = 1
        messenger.is_forward  = 0
        messenger.is_flag = 0
        messenger.is_forkout = 0
        messenger.conversation_reference_id = data.conversation_reference_id
        messenger.created_at = currentTime
        messenger.updated_at = currentTime
        messenger.sender_uid = data.sender_uid
        messenger.receiver_uid =data.receiver_uid
        messenger.is_room = data.is_room
        messenger.sender_name = APP_LOGIN_USER_NAME
        messenger.isMine = 1
        dataBase!!.messengerDAO.insertMessage(messenger)
        var localId = dataBase!!.messengerDAO.fetchLastLocalId()
        if (data.is_group == 0 ){
            dataBase!!.userDAO.updateUserRecentMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.LOCATION_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }else{
            dataBase!!.messengerGroupDAO.updateRecentGroupMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.LOCATION_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }
        var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
        var messageObject = Gson().toJson(messenger)
        listener.messageSaved(JSONObject(messageObject))
        if (CLIENT_IS_CONNECTED){
            var encryptedMessage = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,messenger.message!!,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            //var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.caption,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("status",1)
            sendObject.put("message_type",ConstantValues.MessageTypes.LOCATION_MESSAGE)
            sendObject.put("receiver_id",data.receiver_id)
            sendObject.put("is_group",data.is_group)
            sendObject.put("is_room",data.is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message",encryptedMessage)
            sendObject.put("message_id",data.message_id)
            //sendObject.put("attachment",encryptedAttachment)
            //sendObject.put("caption",encryptedCaption)
            //sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data.conversation_reference_id)
            sendObject.put("receiver_uid",data.receiver_uid)
            sendObject.put("sender_uid",data.sender_uid)
            sendObject.put("entity",data.is_group+1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
            messengerSocket!!.emit(SocketEvents.REPLY_MESSAGE,sendObject)
            //messengerSocket!!.once("tm_error",data)
        }else{
            newMessageVerifyChatArchive(data.receiver_id,data.is_group+1)
        }
    }
    fun sendAudioReplyMessage(data: MessageData,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (data==null){
            callBackObject.put("message","Invalid Data")
            listener.tmError(callBackObject)
            return
        }
        if (data.message_id<=0){
            callBackObject.put("message","Invalid Message")
            listener.tmError(callBackObject)
            return
        }
        if (data.receiver_id.isNullOrEmpty()){
            callBackObject.put("message","Invalid receiver")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==-1){
            callBackObject.put("message","Value for is_group not specified")
            listener.tmError(callBackObject)
            return
        }
        if (data.attachment.isNullOrEmpty()){
            callBackObject.put("message","attachment cannot be empty")
            listener.tmError(callBackObject)
            return
        }
        if (data.is_group==0){
            if (dataBase!!.userDAO.checkIsUserIsBlocked(data.receiver_uid)==1){
                return
            }
        }
        setTyping(data.receiver_id,data.is_group+1,0)
        var currentTime = ConstantValues.fetchCurrentTimeInUTC()
        var originalMessageData = dataBase!!.messengerDAO.fetchReplyOriginalMessageDetails(data.message_id)

        var messenger = Messenger()
        messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
        messenger.receiver_id = data.receiver_id.toLong()
        messenger.message = ""
        messenger.message_type = ConstantValues.MessageTypes.AUDIO_MESSAGE
        messenger.attachment = data.attachment
        messenger.preview_link = data.preview_link
        if (!data.local_attachment_path.isNullOrEmpty()){
            messenger.local_attachment_path = data.local_attachment_path
            messenger.attachment_downloaded = 1
        }
        messenger.caption =""
        messenger.is_group = data.is_group
        messenger.message_id = 0
        messenger.is_sync = 0
        messenger.is_read = 0
        messenger.is_reply = 1
        messenger.original_message_id = data.message_id
        messenger.original_message = Gson().toJson(originalMessageData)
        messenger.is_delivered = 0
        messenger.status = 1
        messenger.is_forward  = 0
        messenger.is_flag = 0
        messenger.is_forkout = 0
        messenger.conversation_reference_id = data.conversation_reference_id
        messenger.created_at = currentTime
        messenger.updated_at = currentTime
        messenger.sender_uid = data.sender_uid
        messenger.receiver_uid =data.receiver_uid
        messenger.is_room = data.is_room
        messenger.sender_name = APP_LOGIN_USER_NAME
        messenger.isMine = 1
        dataBase!!.messengerDAO.insertMessage(messenger)
        var localId = dataBase!!.messengerDAO.fetchLastLocalId()
        if (data.is_group == 0 ){
            dataBase!!.userDAO.updateUserRecentMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.AUDIO_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }else{
            dataBase!!.messengerGroupDAO.updateRecentGroupMessage(localId,0,messenger.message!!,currentTime,ConstantValues.MessageTypes.AUDIO_MESSAGE,ConstantValues.MessageStatus.PENDING,messenger.receiver_id.toString(),
                APPLICATION_TM_LOGIN_USER_ID,1,
                APP_LOGIN_USER_NAME,
                APPLICATION_TM_LOGIN_USER_ID)
        }
        var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
        var messageObject = Gson().toJson(messenger)
        listener.messageSaved(JSONObject(messageObject))
        if (CLIENT_IS_CONNECTED){
            var encryptedAttachment = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.attachment,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            //var encryptedCaption = if (messenger.caption.isNullOrEmpty()) "" else Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,data.caption,data.receiver_id,data.is_group+1,referenceId, APPLICATION_TM_LOGIN_USER_ID,data.receiver_id)
            var sendObject = JSONObject()
            sendObject.put("status",1)
            sendObject.put("message_type",ConstantValues.MessageTypes.AUDIO_MESSAGE)
            sendObject.put("receiver_id",data.receiver_id)
            sendObject.put("is_group",data.is_group)
            sendObject.put("is_room",data.is_room)
            sendObject.put("access_token", APP_ACCESS_TOKEN)
            sendObject.put("message","")
            sendObject.put("message_id",data.message_id)
            sendObject.put("attachment",encryptedAttachment)
            //sendObject.put("caption",encryptedCaption)
            sendObject.put("preview_link",data.preview_link)
            sendObject.put("reference_id",referenceId)
            sendObject.put("conversation_reference_id",data.conversation_reference_id)
            sendObject.put("receiver_uid",data.receiver_uid)
            sendObject.put("sender_uid",data.sender_uid)
            sendObject.put("entity",data.is_group+1)
            sendObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
            messengerSocket!!.emit(SocketEvents.REPLY_MESSAGE,sendObject)
            //messengerSocket!!.once("tm_error",data)
        }else{
            newMessageVerifyChatArchive(data.receiver_id,data.is_group+1)
        }
    }
    fun sendForwardMessages(messageIds:List<String>,forwardList:List<Forward>,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (messageIds.isEmpty()){
            callBackObject.put("message","MessageId cannot be empty")
            listener.tmError(callBackObject)
            return
        }
        if (forwardList.isEmpty()){
            callBackObject.put("message","forwardTo cannot be empty")
            listener.tmError(callBackObject)
            return
        }
        var messages = dataBase!!.messengerDAO.fetchMessagesForForwarding(messageIds)
        Log.e("message===> ",""+Gson().toJson(messages))
        var messagesLength = messages.size
        var numberOfReceivers = forwardList.size
        for (m in 0 until messagesLength){
            for (r in 0 until numberOfReceivers){
                var forwardobject = JSONObject()
                var receiverId = forwardList[r].receiver_id
                var messenger = Messenger()
                messenger.sender_id = APPLICATION_TM_LOGIN_USER_ID.toLong()
                messenger.receiver_id = receiverId!!.toLong()
                messenger.message = messages[m].message
                messenger.message_type = messages[m].message_type
                messenger.attachment = messages[m].attachment
                messenger.is_group = forwardList[r].entity-1
                messenger.local_attachment_path = messages[m].local_attachment_path
                messenger.attachment_downloaded = messages[m].attachment_downloaded
                messenger.message_id = 0
                messenger.is_sync = 0
                messenger.is_read = 0
                messenger.is_reply = 0
                messenger.original_message = ""
                messenger.is_delivered = 0
                messenger.status = 1
                messenger.is_forward  = 1
                messenger.is_flag = 0
                messenger.is_forkout = 0
                messenger.conversation_reference_id = ""
                messenger.created_at = ConstantValues.fetchCurrentTimeInUTC()
                messenger.updated_at = ConstantValues.fetchCurrentTimeInUTC()
                messenger.sender_uid = APPLICATION_LOGIN_U_ID
                messenger.receiver_uid =forwardList[r].entity_uid!!
                messenger.is_room = 0
                messenger.sender_name = APP_LOGIN_USER_NAME
                messenger.isMine = 1
                var localId = dataBase!!.messengerDAO.fetchLastLocalId()


                var referenceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")+localId
                forwardobject.put("receiver_id",receiverId)
                forwardobject.put("reference_id",referenceId)
                forwardobject.put("sender_uid", APPLICATION_LOGIN_U_ID)
                forwardobject.put("receiver_uid",forwardList[r].entity_uid!!)
                forwardobject.put("is_forward",1)
                forwardobject.put("status",1)
                forwardobject.put("entity",forwardList[r].entity)
                forwardobject.put("message_type",messages[m].message_type)
                forwardobject.put("message",messages[m].message)
                forwardobject.put("attachment",messages[m].attachment)
                forwardobject.put("is_group",forwardList[r].entity-1)
                forwardobject.put("access_token", APP_ACCESS_TOKEN)
                forwardobject.put("conversation_reference_id","")
                forwardobject.put("version",ConstantValues.CLIENT_VERSION.toInt())

                if (!messages[m].message.isNullOrEmpty()){
                    forwardobject.put("message",Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,messages[m].message!!,receiverId!!,forwardList[r].entity,referenceId,
                        APPLICATION_TM_LOGIN_USER_ID,receiverId!!))
                }
                if (!messages[m].attachment.isNullOrEmpty()){
                    forwardobject.put("attachment",Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,messages[m].attachment!!,receiverId!!,forwardList[r].entity,referenceId,
                        APPLICATION_TM_LOGIN_USER_ID,receiverId!!))
                }
                /*if (messages[m].attachment.isNullOrEmpty()){
                    forwardobject.put("caption",Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,messages[m].,receiverId,forwardList[r].entity,referenceId,
                        APPLICATION_TM_LOGIN_USER_ID,receiverId))
                }*/
                if (messages[m].message_type==ConstantValues.MessageTypes.ATTACHMENT || messages[m].message_type==ConstantValues.MessageTypes.AUDIO_MESSAGE){
                    messengerSocket!!.emit(SocketEvents.SEND_ATTACHMENT,forwardobject)
                }else if (messages[m].message_type==ConstantValues.MessageTypes.LOCATION_MESSAGE){
                    messengerSocket!!.emit(SocketEvents.SEND_LOCATION_V2,forwardobject)
                }else if (messages[m].message_type==ConstantValues.MessageTypes.CONTACT_MESSAGE){
                    messengerSocket!!.emit(SocketEvents.SEND_CONTACT_V2,forwardobject)
                } else {
                    messengerSocket!!.emit(SocketEvents.SEND_MESSAGE,forwardobject)
                }
            }
        }
    }

    fun sendMessageDeliveryStatus(jsonObject: JSONObject){
        Log.e("delivered==> "," called sent "+jsonObject.toString())
        messengerSocket!!.emit(SocketEvents.MESSAGE_DELIVERED,jsonObject)
    }
    fun sendMessageReadStatus(jsonObject: JSONObject){
        Log.e(TAG,"readmesages===> "+jsonObject.toString())
        messengerSocket!!.emit(SocketEvents.MESSAGE_READ,jsonObject)
    }

    fun deleteMessages(localIds:List<Long>,entityId: String,entityType: Int,isRoom: Int,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (localIds.isEmpty()){
            callBackObject.put("success",false)
            callBackObject.put("message","At least one message is required")
            listener.deleteResponse(callBackObject)
            return
        }
        var ids = dataBase!!.messengerDAO.fetchMessagesIdsIdsFormLocalIds(localIds)
        var messageArrys = JSONArray()
        for (element in ids){
            messageArrys.put(element.toString())
        }
        dataBase!!.messengerDAO.deleteLocalMessages(localIds)
        if (ids.isNotEmpty()) {
            if (CLIENT_IS_CONNECTED) {
                    var jsonObject = JSONObject()
                    jsonObject.put("access_token", APP_ACCESS_TOKEN)
                    jsonObject.put("message_id", messageArrys)
                    jsonObject.put("is_room", isRoom)
                    jsonObject.put("is_group", entityType-1)
                    jsonObject.put("entity_id", entityId)
                    jsonObject.put("entity_type", entityType)
                    Log.e(TAG, " delete " + jsonObject.toString())
                    messengerSocket!!.emit(SocketEvents.DELETE_MESSAGE, jsonObject, Ack { args ->
                        Log.e(TAG, " delete callback " + jsonObject.toString())
                        if (args[0] != null) {
                            var responseObject = args[0] as JSONObject
                            if (responseObject.optBoolean("status")) {
                                dataBase!!.messengerDAO.deleteServerMessages(ids)
                                TroopSocketClient.updateRecentUserGroupMessage()
                                callBackObject.put("success", true)
                                callBackObject.put("id", Gson().toJson(ids))
                                listener.deleteResponse(callBackObject)
                            } else {
                                callBackObject.put("success", false)
                                callBackObject.put("message", responseObject.optString("message"))
                                listener.deleteResponse(callBackObject)
                            }

                        }
                    })

            } else {
                var offlineDeleteMessagesList = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.OFFLINE_DELETED_MESSAGES, "[]"))!!
                var jsonObject = JSONObject()
                jsonObject.put("entity_id",entityId)
                jsonObject.put("entity_type",entityType)
                jsonObject.put("message_id",Helper.stringToJsonArray(Gson().toJson(ids)))
                offlineDeleteMessagesList.put(jsonObject)
                sharedPreferences!!.edit().putString(SharePreferenceConstants.OFFLINE_DELETED_MESSAGES,offlineDeleteMessagesList.toString()).apply()
            }
        }

    }
    fun recallMessage(messageIds: List<String>,entityId:String,entityType:Int,isRoom: Int,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (messengerSocket!=null && !messengerSocket!!.connected()){
            callBackObject.put("success",false)
            callBackObject.put("message","Connection to server is not yet established!")
            listener.recallResponse(callBackObject)
            return
        }
        if (messageIds.isEmpty()){
            callBackObject.put("success",false)
            callBackObject.put("message","messageId cannot be empty")
            listener.recallResponse(callBackObject)
            return
        }
        var jsonArray = JSONArray()
        for (i in 0 until messageIds.size){
            jsonArray.put(messageIds[i])
        }

            var recallJSONObject = JSONObject()
            recallJSONObject.put("access_token", APP_ACCESS_TOKEN)
            recallJSONObject.put("message_id", jsonArray)
            recallJSONObject.put("is_room", isRoom)
            recallJSONObject.put("is_group", entityType-1)
            recallJSONObject.put("entity_id", entityId)
            recallJSONObject.put("entity_type", entityType)
            messengerSocket!!.emit(SocketEvents.RECALL_MESSAGE, recallJSONObject, Ack { args ->
                var responseObject = args[0] as JSONObject
                if (responseObject.optBoolean("status")) {
                    val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
                    var messageObject = JSONObject()
                    messageObject.put("name", APP_LOGIN_USER_NAME)
                    messageObject.put("user_id", APPLICATION_TM_LOGIN_USER_ID)
                    dataBase!!.messengerDAO.updateMessageRecall(
                        ConstantValues.MessageStatus.MESSAGE_RECALLED,
                        messageIds,
                        updatedAtTime!!,
                        messageObject.toString()
                    )
                    callBackObject.put("success", true)
                    callBackObject.put("message_id", Gson().toJson(messageIds))
                    listener.recallResponse(callBackObject)
                    TroopSocketClient.updateRecentUserGroupMessage()
                } else {
                    callBackObject.put("success", false)
                    callBackObject.put("message", responseObject.optString("message"))
                    listener.recallResponse(callBackObject)
                }
            })

    }
    fun editMessage(messageId:Long,entityId: String,entityType: Int,isRoom:Int,message:String,listener: ClientCallBackListener){
        var room = isRoom
        var callBackObject = JSONObject()
        if (!CLIENT_IS_CONNECTED){
            callBackObject.put("success",false)
            callBackObject.put("message","Connection to server is not yet established!")
            listener.recallResponse(callBackObject)
            return
        }
        if (messageId<=0 ){
            callBackObject.put("success",false)
            callBackObject.put("message","Invalid Message")
            listener.recallResponse(callBackObject)
            return
        }
        if (message.trim().isNullOrEmpty()){
            callBackObject.put("success",false)
            callBackObject.put("message","Message can not be empty")
            listener.recallResponse(callBackObject)
            return
        }
        if (entityId.trim().isNullOrEmpty()){
            callBackObject.put("success",false)
            callBackObject.put("message","Invalid User")
            listener.recallResponse(callBackObject)
            return
        }
        if (entityType<=0){
            callBackObject.put("success",false)
            callBackObject.put("message","Invalid User/Group type")
            listener.recallResponse(callBackObject)
            return
        }
        if (isRoom>1){
            callBackObject.put("success",false)
            callBackObject.put("message","Invalid value specified for is_room")
            listener.recallResponse(callBackObject)
            return
        }
        if (isRoom<0){
            room = 0
        }
        var encyData = Encryption.encryptMessage(Encryption.EncryptionType.STANDARD,message,entityId,entityType,"",
            APPLICATION_TM_LOGIN_USER_ID,entityId)
        var jsonObject = JSONObject()
        jsonObject.put("access_token", APP_ACCESS_TOKEN)
        jsonObject.put("message_id",messageId)
        jsonObject.put("is_room",room)
        jsonObject.put("message",encyData)
        jsonObject.put("is_group",entityType-1)
        messengerSocket!!.emit(SocketEvents.MESSAGE_EDIT,jsonObject, Ack {args ->
            if (args[0]!=null){
                var jsonObject = args[0] as JSONObject
                if (jsonObject.optBoolean("success")) {
                    TroopSocketClient.messageEdited(jsonObject.optJSONObject("data"))
                }
            }
        })

    }
    fun newMessageVerifyChatArchive(entityId: String, entityType: Int){
        var isUpdate = false
        var isArchived = if (entityType==ConstantValues.Entity.USER) dataBase!!.userDAO.checkIsArchived(entityId) else dataBase!!.messengerGroupDAO.isArchivedGroup(entityId)

        if (isArchived==1){
            var list : ArrayList<Long> = ArrayList()
            list.add(entityId.toLong())
            if (entityType==ConstantValues.Entity.USER){
                dataBase!!.userDAO.updateUserUnArchived(list)
            }else{
                dataBase!!.messengerGroupDAO.updateGroupUnArchived(list)
            }
            isUpdate = true
        }
        if (isUpdate){
            var listOfChats = JSONArray()
            var chatObject = JSONObject()
            chatObject.put("entity_id",entityId)
            chatObject.put("entity_type",entityType)
            listOfChats.put(chatObject)
            if (CLIENT_IS_CONNECTED){
                var jsonObject = JSONObject()
                jsonObject.put("access_token", APP_ACCESS_TOKEN)
                jsonObject.put("chats",listOfChats)
                messengerSocket!!.emit(SocketEvents.UNARCHIVED_CHAT,jsonObject)
            }else {
                var offlineUnArchivedChats = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.TM_OFFLINE_UNARCHIVED_CHATS,"[]"))!!
                offlineUnArchivedChats.put(chatObject)
                sharedPreferences!!.edit().putString(SharePreferenceConstants.TM_OFFLINE_UNARCHIVED_CHATS,offlineUnArchivedChats.toString()).apply()
            }
        }
    }
    fun setTyping(entityId: String,entityType: Int,tyingStatus:Int){
        if (entityId.isNullOrEmpty()){
            return
        }
        var jsonObject = JSONObject()
        jsonObject.put("typing",tyingStatus)
        jsonObject.put("receiver_id",entityId)
        jsonObject.put("typing_user_name", APP_LOGIN_USER_NAME)
        jsonObject.put("is_group",(entityType-1))
        jsonObject.put("access_token", APP_ACCESS_TOKEN)
        Log.e("typing---> ",""+jsonObject.toString())
        messengerSocket!!.emit(SocketEvents.USER_TYPING,jsonObject)
    }
    fun fetchAndUpdateReadStatus(convEntityId: String, convEntityType: Int,isRoom: Int) {
        var messageIds :ArrayList<Long> = ArrayList()
        if (convEntityType==ConstantValues.Entity.USER){
            messageIds = dataBase!!.messengerDAO.fetchUserUnReadMessages(convEntityId,
                APPLICATION_TM_LOGIN_USER_ID) as ArrayList<Long>
        }else if (convEntityType==ConstantValues.Entity.GROUP){
            messageIds = dataBase!!.messengerDAO.fetchGroupUnReadMessages(
                APPLICATION_TM_LOGIN_USER_ID,convEntityId) as ArrayList<Long>
        }
        if (messageIds.isNotEmpty()){
            dataBase!!.messengerDAO.updateMessageReadStatus(messageIds,ConstantValues.fetchCurrentTimeInUTC())
        }
        if (convEntityType==ConstantValues.Entity.USER) {
            dataBase!!.userDAO.updateUserUnreadCount(convEntityId,APPLICATION_TM_LOGIN_USER_ID,)
        }else if (convEntityType==ConstantValues.Entity.GROUP){
            dataBase!!.messengerGroupDAO.updateUnreadCount(APPLICATION_TM_LOGIN_USER_ID, convEntityId)
        }
        if (messengerSocket!=null && messengerSocket!!.connected() && messageIds.isNotEmpty()){
            for (i in 0 until messageIds.size) {
                if (messageIds[i]>0) {
                    var jsonObject = JSONObject()
                    jsonObject.put("access_token", APP_ACCESS_TOKEN)
                    jsonObject.put("message_id", messageIds[i])
                    jsonObject.put("is_group", convEntityType - 1)
                    jsonObject.put("is_room", isRoom)
                    sendMessageReadStatus(jsonObject)
                }
            }
        }else{
            var offlineUnArchivedChats = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.TM_OFFLINE_READ_MESSAGES,"[]"))
            var jsonObject = JSONObject()
            jsonObject.put("message_id", Helper.stringToJsonArray(Gson().toJson(messageIds)))
            jsonObject.put("is_group",convEntityType-1)
            jsonObject.put("is_room",isRoom)
            offlineUnArchivedChats!!.put(jsonObject)
            sharedPreferences!!.edit().putString(SharePreferenceConstants.TM_OFFLINE_READ_MESSAGES,offlineUnArchivedChats.toString()).apply()
        }

    }
}