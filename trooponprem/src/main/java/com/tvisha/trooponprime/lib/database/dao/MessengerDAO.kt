package com.tvisha.trooponprime.lib.database.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.tvisha.trooponprime.lib.clientModels.ConversationModel
import com.tvisha.trooponprime.lib.clientModels.MediaModel
import com.tvisha.trooponprime.lib.clientModels.RecentMessageList
import com.tvisha.trooponprime.lib.database.GroupMembers
import com.tvisha.trooponprime.lib.database.Messenger
import com.tvisha.trooponprime.lib.database.MessengerGroup
import com.tvisha.trooponprime.lib.database.User
import com.tvisha.trooponprime.lib.database.model.*
import org.webrtc.CryptoOptions.Srtp

@Dao
internal interface MessengerDAO {
    @Insert
    fun insertMessage(messenger: Messenger)

    @Update
    fun updateMessage(messenger: Messenger)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBulkMessages(messages:List<Messenger>)

    /*@Query("SELECT message_id FROM chat WHERE message_id=:messageId AND workspace_id =:workspaceId")
    fun checkWorkspaceMessageExists(messageId:Long,workspaceId:String) : Long
    @Query("SELECT message_id FROM chat WHERE message_id=:messageId ")
    fun checkMessageExists(messageId:Long) : Long

    @Query("SELECT status FROM chat WHERE message_id =:messageId and workspace_id= :workspaceId")
    fun checkWorkspaceMessageStatus(messageId: Long,workspaceId: String):Int
    @Query("SELECT status FROM chat WHERE message_id =:messageId")
    fun checkMessageStatus(messageId: Long):Int

    @Query("UPDATE chat SET uploadProgress='',message_id=:messageId,is_sync=1,updated_at= :updatedAt,created_at =:createdAt WHERE ID= :deviceId AND workspace_id =:workspaceId")
    fun updateWorkspaceMessageSentStatus(deviceId:String,workspaceId: String,messageId: Int,updatedAt:String,createdAt:String)
    @Query("UPDATE chat SET uploadProgress='',message_id=:messageId,is_sync=1,updated_at= :updatedAt,created_at =:createdAt WHERE ID= :deviceId")
    fun updateMessageSentStatus(deviceId:String,messageId: Int,updatedAt:String,createdAt:String)

    @Query("UPDATE chat SET is_sync = 1,is_delivered = 1,updated_at =:updatedAt WHERE message_id=:messageId AND workspace_id = :workspaceId AND message_id > 0")
    fun updateWorkspaceMessageDeliveredStatus(messageId: Long,updatedAt: String,workspaceId: String)


    @Query("UPDATE chat SET is_sync = 1,is_delivered = 1,is_read =1,updated_at =:updatedAt WHERE message_id=:messageId AND workspace_id = :workspaceId AND message_id > 0")
    fun updateWorkspaceMessageReadStatus(messageId: Long,updatedAt: String,workspaceId: String)


    @Query("DELETE FROM chat WHERE receiver_id = :groupId AND workspace_id = :workspaceId AND is_group = 1 AND message_id<:messageId")
    fun deleteWorkspaceGroupAllMessagesByMessageId(groupId:Long,workspaceId: String,messageId: Long)
    @Query("DELETE FROM chat WHERE receiver_id = :groupId  AND is_group = 1 AND message_id<:messageId")
    fun deleteGroupAllMessagesByMessageId(groupId:Long,messageId: Long)

    @Query("DELETE FROM chat WHERE receiver_id = :groupId AND workspace_id = :workspaceId AND is_group = 1")
    fun deleteWorkspaceGroupAllMessages(groupId:Long,workspaceId: String)
    @Query("DELETE FROM chat WHERE receiver_id = :groupId  AND is_group = 1")
    fun deleteGroupAllMessages(groupId:Long)

    @Query("DELETE FROM chat WHERE receiver_id = :groupId AND workspace_id = :workspaceId AND message_type IN (0,2,3,24) AND is_group =1 AND message_id < :messageId")
    fun deleteWorkspaceGroupMessagesOnlyByMessageId(groupId: Long,workspaceId: String,messageId: Long)
    @Query("DELETE FROM chat WHERE receiver_id = :groupId AND message_type IN (0,2,3,24) AND is_group =1 AND message_id < :messageId")
    fun deleteGroupMessagesOnlyByMessageId(groupId: Long,messageId: Long)

    @Query("DELETE FROM chat WHERE receiver_id = :groupId AND workspace_id = :workspaceId AND message_type IN (0,2,3,24) AND is_group =1 ")
    fun deleteWorkspaceGroupMessagesOnly(groupId: Long,workspaceId: String)
    @Query("DELETE FROM chat WHERE receiver_id = :groupId AND message_type IN (0,2,3,24) AND is_group =1 ")
    fun deleteGroupMessagesOnly(groupId: Long)

    @Query("DELETE FROM chat WHERE receiver_id = :groupId AND workspace_id = :workspaceId AND message_type IN (1,23,26,28) AND is_group =1 AND message_id<:messageId")
    fun deleteWorkspaceGroupFilesOnlyByMessageId(groupId: Long,workspaceId: String,messageId: Long)
    @Query("DELETE FROM chat WHERE receiver_id = :groupId AND message_type IN (1,23,26,28) AND is_group =1 AND message_id<:messageId")
    fun deleteGroupFilesOnlyByMessageId(groupId: Long,messageId: Long)

    @Query("DELETE FROM chat WHERE receiver_id = :groupId AND workspace_id = :workspaceId AND message_type IN (1,23,26,28) AND is_group =1 ")
    fun deleteWorkspaceGroupFilesOnly(groupId: Long,workspaceId: String)
    @Query("DELETE FROM chat WHERE receiver_id = :groupId AND message_type IN (1,23,26,28) AND is_group =1 ")
    fun deleteGroupFilesOnly(groupId: Long)

    @Query("DELETE FROM chat WHERE (receiver_id = :entityId or sender_id=:entityId) AND workspace_id = :workspaceId AND is_group = 0")
    fun deleteWorkspaceUserAllMessages(entityId:Long,workspaceId: String)
    @Query("DELETE FROM chat WHERE (receiver_id = :entityId or sender_id=:entityId)  AND is_group = 0")
    fun deleteUserAllMessages(entityId:Long)

    @Query("DELETE FROM chat WHERE (receiver_id = :entityId or sender_id=:entityId) AND workspace_id = :workspaceId AND message_type IN (0,2,3,24) AND is_group =0")
    fun deleteWorkspaceUserMessagesOnly(entityId: Long,workspaceId: String)
    @Query("DELETE FROM chat WHERE (receiver_id = :entityId or sender_id=:entityId)  AND message_type IN (0,2,3,24) AND is_group =0")
    fun deleteUserMessagesOnly(entityId: Long)

    @Query("DELETE FROM chat WHERE (receiver_id = :entityId or sender_id=:entityId) AND workspace_id = :workspaceId AND message_type IN (1,23,26,28) AND is_group =0")
    fun deleteWorkspaceUserFilesOnly(entityId: Long,workspaceId: String)
    @Query("DELETE FROM chat WHERE (receiver_id = :entityId or sender_id=:entityId)  AND message_type IN (1,23,26,28) AND is_group =0")
    fun deleteUserFilesOnly(entityId: Long)

    @Query("UPDATE chat SET status = 0 , updated_at = :updatedAt WHERE message_id IN (:messageIds) AND workspace_id= :workspaceId")
    fun  messageWorkspaceDelete(messageIds:List<String>,workspaceId: String,updatedAt: String)
    @Query("UPDATE chat SET status = 0 , updated_at = :updatedAt WHERE message_id IN (:messageIds)")
    fun  messageDelete(messageIds:List<String>,updatedAt: String)

    @Query("UPDATE chat SET status = 0 , updated_at = :updatedAt WHERE ID IN (:ids) AND workspace_id= :workspaceId")
    fun  messageWorkspaceDeleteByLocalID(ids:List<Long>,workspaceId: String,updatedAt: String)
    @Query("UPDATE chat SET status = 0 , updated_at = :updatedAt WHERE ID IN (:ids) ")
    fun  messageDeleteByLocalID(ids:List<Long>,updatedAt: String)

    @Query("UPDATE chat SET status = 0 , updated_at = :updatedAt WHERE message_id IN (:ids) AND workspace_id= :workspaceId")
    fun  messageWorkspaceDeleteByMessageID(ids:List<Long>,workspaceId: String,updatedAt: String)
    @Query("UPDATE chat SET status = 0 , updated_at = :updatedAt WHERE message_id IN (:ids)")
    fun  messageDeleteByMessageID(ids:List<Long>,updatedAt: String)


    @Query("UPDATE chat SET status = 2 , message=:message,updated_at = :updatedAt WHERE message_id IN (:messageIds) AND workspace_id= :workspaceId")
    fun  messageWorkspaceRecall(messageIds:List<String>,workspaceId: String,updatedAt: String,message:String)
    @Query("UPDATE chat SET status = 2 , message=:message,updated_at = :updatedAt WHERE message_id IN (:messageIds)")
    fun  messageRecall(messageIds:List<String>,updatedAt: String,message:String)

    @Query("UPDATE chat SET read_receipt_status = 1,updated_at = :updatedAt WHERE message_id =:messageId AND workspace_id=:workspaceId ")
    fun updateWorkspaceMessageReadReceiptStatus(messageId: String,workspaceId: String,updatedAt: String)
    @Query("UPDATE chat SET read_receipt_status = 1,updated_at = :updatedAt WHERE message_id =:messageId")
    fun updateMessageReadReceiptStatus(messageId: String,updatedAt: String)

    @Query("SELECT count(message_id) as unread_read_receipt_count FROM chat WHERE ((sender_id!=:workspaceUserId AND receiver_id=:entityId) OR (sender_id=:entityId AND receiver_id=:workspaceUserId)) AND is_group =0 AND workspace_id = :workspaceId AND is_read_receipt = 1 AND read_receipt_status = 0 and status = 1")
    fun fetchWorkspaceUserUnreadReadReceiptCount(workspaceId: String,workspaceUserId :String,entityId:String) : Int
    @Query("SELECT count(message_id) as unread_read_receipt_count FROM chat WHERE ((sender_id!=:loginUserId AND receiver_id=:entityId) OR (sender_id=:entityId AND receiver_id=:loginUserId)) AND is_group =0 AND is_read_receipt = 1 AND read_receipt_status = 0 and status = 1")
    fun fetchUserUnreadReadReceiptCount(loginUserId :String,entityId:String) : Int

    @Query("SELECT count(message_id) as unread_read_receipt_count FROM chat WHERE sender_id != :workspaceUserId AND receiver_id =:entityId AND is_group = 1 AND workspace_id =:workspaceId AND is_read_receipt = 1 AND read_receipt_status = 0 AND status = 1")
    fun fetchWorkspaceGroupUnreadReadReceiptCount(workspaceId: String,workspaceUserId :String,entityId:String) : Int
    @Query("SELECT count(message_id) as unread_read_receipt_count FROM chat WHERE sender_id != :loginUserId AND receiver_id =:entityId AND is_group = 1  AND is_read_receipt = 1 AND read_receipt_status = 0 AND status = 1")
    fun fetchGroupUnreadReadReceiptCount(loginUserId: String,entityId:String) : Int

    @Query("UPDATE chat SET is_flag  = :flagStatus,updated_at =:updatedAt WHERE message_id =:messageId AND workspace_id =:workspaceId")
    fun updateWorkspaceMessageFlagStatus(flagStatus:Int,messageId: Long,workspaceId: String,updatedAt: String)
    @Query("UPDATE chat SET is_flag  = :flagStatus,updated_at =:updatedAt WHERE message_id =:messageId")
    fun updateMessageFlagStatus(flagStatus:Int,messageId: Long,updatedAt: String)

    @Query("UPDATE chat SET is_respond_later  = :respondLaterStatus,updated_at =:updatedAt WHERE message_id =:messageId AND workspace_id =:workspaceId")
    fun updateWorkspaceMessageRespondLaterStatus(respondLaterStatus:Int,messageId: Long,workspaceId: String,updatedAt: String)
    @Query("UPDATE chat SET is_respond_later  = :respondLaterStatus,updated_at =:updatedAt WHERE message_id =:messageId")
    fun updateMessageRespondLaterStatus(respondLaterStatus:Int,messageId: Long,updatedAt: String)

    @Query("UPDATE chat SET message =:message, is_edited =:isEdited,updated_at=:updatedAt,is_downloaded= 0,edited_at=:editedAt,attachment=:attachmentPath,message_type = :messageType WHERE message_id =:messageId AND workspace_id =:workspaceId")
    fun updateWorkspaceCodeSnippetMessageEdit(message:String,isEdited:Int,editedAt:String,updatedAt: String,attachmentPath :String,messageType:Int,messageId: Int,workspaceId: String)
    @Query("UPDATE chat SET message =:message, is_edited =:isEdited,updated_at=:updatedAt,is_downloaded= 0,edited_at=:editedAt,attachment=:attachmentPath,message_type = :messageType WHERE message_id =:messageId")
    fun updateCodeSnippetMessageEdit(message:String,isEdited:Int,editedAt:String,updatedAt: String,attachmentPath :String,messageType:Int,messageId: Int)

    @Query("UPDATE chat SET message =:message, is_edited =:isEdited,updated_at=:updatedAt,edited_at =:editedAt,message_type = :messageType,caption =:caption WHERE message_id =:messageId AND workspace_id =:workspaceId")
    fun updateWorkspaceMessageEdit(message:String,isEdited:Int,editedAt:String,updatedAt: String,messageType:Int,messageId: Int,workspaceId: String,caption:String)
    @Query("UPDATE chat SET message =:message, is_edited =:isEdited,updated_at=:updatedAt,edited_at =:editedAt,message_type = :messageType,caption =:caption WHERE message_id =:messageId")
    fun updateMessageEdit(message:String,isEdited:Int,editedAt:String,updatedAt: String,messageType:Int,messageId: Long,caption:String)

    @Query("UPDATE chat SET message =:message, is_edited =:isEdited,updated_at=:updatedAt,edited_at =:editedAt,message_type = :messageType,caption=:caption WHERE ID =:id AND workspace_id =:workspaceId")
    fun updateWorkspaceMessageEditWithLocalID(message:String,isEdited:Int,editedAt:String,updatedAt: String,messageType:Int,id: Long,workspaceId: String,caption:String)
    @Query("UPDATE chat SET message =:message, is_edited =:isEdited,updated_at=:updatedAt,edited_at =:editedAt,message_type = :messageType,caption=:caption WHERE ID =:id")
    fun updateMessageEditWithLocalID(message:String,isEdited:Int,editedAt:String,updatedAt: String,messageType:Int,id: Long,caption:String)

    @Query("SELECT message FROM chat WHERE message_id = :messageId AND workspace_id =:workspaceId AND message_type = 26")
    fun fetchWorkspaceCodeSnippetData(messageId: Long,workspaceId: String) :String
    @Query("SELECT message FROM chat WHERE message_id = :messageId AND workspace_id =:workspaceId AND message_type = 26")
    fun fetchCodeSnippetData(messageId: Long,workspaceId: String) :String

    @Query("UPDATE chat SET message = :message,is_downloaded = 1 WHERE workspace_id =:workspaceId AND message_id =:messageId")
    fun updateWorkspaceCodeSnippetMessage(message: String,workspaceId: String,messageId: Long)
    @Query("UPDATE chat SET message = :message,is_downloaded = 1 WHERE message_id =:messageId")
    fun updateCodeSnippetMessage(message: String,messageId: Long)

    @RawQuery(observedEntities = [Messenger::class])
    fun fetchMessages(query: SupportSQLiteQuery): PagingSource<Int, Messenger>

    @Query("SELECT count(message_id) AS count FROM chat WHERE (sender_id = :senderId AND receiver_id =:receiverId AND is_read != 1 ) AND workspace_id = :workspaceId AND status IN (1,2,3) AND is_group = 0 AND message_id != 0 ORDER BY message_id DESC")
    fun fetchWorkspaceUserUnreadCount(senderId : String,receiverId:String,workspaceId: String) :Int
    @Query("SELECT count(message_id) AS count FROM chat WHERE (sender_id = :senderId AND receiver_id =:receiverId AND is_read != 1 )  AND status IN (1,2,3) AND is_group = 0 AND message_id != 0 ORDER BY message_id DESC")
    fun fetchUserUnreadCount(senderId : String,receiverId:String) :Int

    @Query("SELECT count(message_id) AS count FROM chat WHERE (sender_id != :workspaceUserid AND receiver_id =:receiverId AND is_read != 1 ) AND workspace_id = :workspaceId AND status IN (1,2,3) AND is_group = 1 AND message_id != 0 ORDER BY message_id DESC")
    fun fetchWorkspaceGroupUnreadCount(workspaceUserid: String,receiverId: String,workspaceId: String) : Int
    @Query("SELECT count(message_id) AS count FROM chat WHERE (sender_id != :loginUserId AND receiver_id =:receiverId AND is_read != 1 )  AND status IN (1,2,3) AND is_group = 1 AND message_id != 0 ORDER BY message_id DESC")
    fun fetchGroupUnreadCount(loginUserId: String,receiverId: String) : Int

    @Query("SELECT count(message_id) AS count FROM chat WHERE ((sender_id != :workspaceUserId AND receiver_id = :receiverId) OR (sender_id = :receiverId and receiver_id = :workspaceUserId)) AND is_group = 0 AND workspace_id = :workspaceId AND is_respond_later =1 AND status = 1")
    fun fetchWorkspaceUserRespondLaterCount(workspaceUserId: String,receiverId: String,workspaceId: String): Int
    @Query("SELECT count(message_id) AS count FROM chat WHERE ((sender_id != :loginUserId AND receiver_id = :receiverId) OR (sender_id = :receiverId and receiver_id = :loginUserId)) AND is_group = 0  AND is_respond_later =1 AND status = 1")
    fun fetchUserRespondLaterCount(loginUserId: String,receiverId: String): Int

    @Query("SELECT count(message_id) AS count FROM chat WHERE sender_id !=:workspaceUserId AND receiver_id=:receiverId AND is_group = 1 AND workspace_id = :workspaceId AND is_respond_later =1 AND status = 1")
    fun fetchWorkspaceGroupRespondLaterCount(workspaceUserId: String,receiverId: String,workspaceId: String):Int
    @Query("SELECT count(message_id) AS count FROM chat WHERE sender_id !=:loginUserId AND receiver_id=:receiverId AND is_group = 1  AND is_respond_later =1 AND status = 1")
    fun fetchGroupRespondLaterCount(loginUserId: String,receiverId: String):Int

    @Query("SELECT count(message_id) AS count FROM chat WHERE ((sender_id != :workspaceUserid AND receiver_id = :receiverId) OR (sender_id = :receiverId and receiver_id = :workspaceUserid)) AND is_group = 0 AND workspace_id = :workspaceId AND is_read_receipt =1 AND read_receipt_status = 0 AND status = 1")
    fun fetchWorkspaceUserUnreadReceiptCount(workspaceUserid: String,receiverId: String,workspaceId: String): Int
    @Query("SELECT count(message_id) AS count FROM chat WHERE ((sender_id != :loginUserId AND receiver_id = :receiverId) OR (sender_id = :receiverId and receiver_id = :loginUserId)) AND is_group = 0  AND is_read_receipt =1 AND read_receipt_status = 0 AND status = 1")
    fun fetchUserUnreadReceiptCount(loginUserId: String,receiverId: String): Int

    @Query("SELECT count(message_id) AS count FROM chat WHERE sender_id !=:workspaceUserId AND receiver_id=:receiverId AND is_group = 1 AND workspace_id = :workspaceId AND is_read_receipt =1 AND read_receipt_status= 0 AND status = 1")
    fun fetchWorkspaceGroupUnreadReceiptCount(workspaceUserId: String,receiverId: String,workspaceId: String):Int
    @Query("SELECT count(message_id) AS count FROM chat WHERE sender_id !=:loginUserId AND receiver_id=:receiverId AND is_group = 1  AND is_read_receipt =1 AND read_receipt_status= 0 AND status = 1")
    fun fetchGroupUnreadReceiptCount(loginUserId: String,receiverId: String):Int*/

    /*@Query("SELECT CASE WHEN (m.is_group = 1) THEN (SELECT is_active FROM chat_group as mg INNER JOIN group_member AS mgm ON mgm.group_id = mg.group_id AND mgm.user_id = :workspaceUserid AND (mgm.user_status =1  AND mg.is_active = 1 ) WHERE mg.group_id = m.receiver_id AND mg.workspace_id = m.workspace_id) ELSE (SELECT user_status FROM user WHERE user_id = m.sender_id AND user.workspace_id = m.workspace_id) end AS is_active ,count(distinct(message_id)) AS count,u.is_orange_member AS orange_member,u.user_id AS entity_id FROM chat AS m INNER JOIN user AS u ON u.workspace_id = m.workspace_id AND u.user_id = m.sender_id where ((m.receiver_id = :workspaceUserid and m.sender_id != :workspaceUserid and m.is_group = 0 and m.is_read = 0 and m.status > 0) or (m.sender_id != :workspaceUserid and m.is_group = 1 and m.is_read = 0  and m.status > 0 )) AND m.workspace_id = :workspaceId group by is_active HAVING is_active=1 ")
    fun fetchWorkspaceAllUnreadCountByWorkspace(workspaceId: String,workspaceUserid: String) :Int
    @Query("SELECT CASE WHEN (m.is_group = 1) THEN (SELECT is_active FROM chat_group as mg INNER JOIN group_member AS mgm ON mgm.group_id = mg.group_id AND mgm.user_id = :loginUserId AND (mgm.user_status =1  AND mg.is_active = 1 ) WHERE mg.group_id = m.receiver_id AND mg.workspace_id = m.workspace_id) ELSE (SELECT user_status FROM user WHERE user_id = m.sender_id AND user.workspace_id = m.workspace_id) end AS is_active ,count(distinct(message_id)) AS count,u.is_orange_member AS orange_member,u.user_id AS entity_id FROM chat AS m INNER JOIN user AS u ON u.workspace_id = m.workspace_id AND u.user_id = m.sender_id where ((m.receiver_id = :loginUserId and m.sender_id != :loginUserId and m.is_group = 0 and m.is_read = 0 and m.status > 0) or (m.sender_id != :loginUserId and m.is_group = 1 and m.is_read = 0  and m.status > 0 ))  group by is_active HAVING is_active=1 ")
    fun fetchAllUnreadCount(loginUserId: String) :Int*/

    /*@RawQuery(observedEntities = [Messenger::class, User::class,MessengerGroup::class,GroupMembers::class])
    fun fetchRecentChatMessageList(query: SupportSQLiteQuery): LiveData<List<RecentList>>

    @Query("SELECT message_id FROM chat WHERE  sender_id!=:userId AND sender_id=:entityId AND receiver_id=:userId AND is_group = 0 AND workspace_id= :workspaceId AND is_read = 0 AND status IN (1,2,3) ")
    fun fetchWorkspaceUnreadUserMessagesList(entityId:String,userId:String,workspaceId: String):List<Long>
    @Query("SELECT message_id FROM chat WHERE  sender_id!=:userId AND sender_id=:entityId AND receiver_id=:userId AND is_group = 0  AND is_read = 0 AND status IN (1,2,3) ")
    fun fetchUnreadUserMessagesList(entityId:String,userId:String):List<Long>*/

    /*@Query("SELECT message_id FROM chat WHERE  sender_id=:entityId AND receiver_id=:userId AND is_group = 0 AND workspace_id= :workspaceId AND is_read = 0 AND status IN (1,2,3) ")
    fun fetchWorkspaceUnreadUserSelfMessagesList(entityId:String,userId:String,workspaceId: String):List<Long>
    @Query("SELECT message_id FROM chat WHERE  sender_id=:entityId AND receiver_id=:userId AND is_group = 0 AND is_read = 0 AND status IN (1,2,3) ")
    fun fetchUnreadUserSelfMessagesList(entityId:String,userId:String):List<Long>*/

   /* @Query("SELECT count(message_id) as count FROM chat WHERE  sender_id=:entityId AND receiver_id=:userId AND is_group = 0 AND workspace_id= :workspaceId AND is_read = 0 AND is_sync = 1 AND status IN (1,2,3) ")
    fun fetchWorkspaceUnreadUserMessagesCount(entityId:String,userId:String,workspaceId: String):Long
    @Query("SELECT count(message_id) as count FROM chat WHERE  sender_id=:entityId AND receiver_id=:userId AND is_group = 0  AND is_read = 0 AND is_sync = 1 AND status IN (1,2,3) ")
    fun fetchUnreadUserMessagesCount(entityId:String,userId:String):Long*/

    /*@Query("SELECT message_id FROM chat WHERE sender_id!=:userId AND receiver_id=:entityId AND is_group = 1 AND workspace_id= :workspaceId AND is_read = 0  AND is_sync = 1 AND status IN (1,2,3)")
    fun fetchWorkspaceUnreadGroupMessagesList(entityId:String,userId:String,workspaceId: String):List<Long>
    @Query("SELECT message_id FROM chat WHERE sender_id!=:userId AND receiver_id=:entityId AND is_group = 1 AND is_read = 0  AND is_sync = 1 AND status IN (1,2,3)")
    fun fetchUnreadGroupMessagesList(entityId:String,userId:String):List<Long>*/

    /*@Query("SELECT count(message_id) as count FROM chat WHERE sender_id!=:userId AND receiver_id=:entityId AND is_group = 1 AND workspace_id= :workspaceId AND is_read = 0 AND is_sync = 1 AND status IN (1,2,3)")
    fun fetchWorkspaceUnreadGroupMessagesCount(entityId:String,userId:String,workspaceId: String):Long
    @Query("SELECT count(message_id) as count FROM chat WHERE sender_id!=:userId AND receiver_id=:entityId AND is_group = 1 AND is_read = 0 AND is_sync = 1 AND status IN (1,2,3)")
    fun fetchUnreadGroupMessagesCount(entityId:String,userId:String):Long

    @Query("SELECT 0 as is_selected,m.pin as pin,m.is_trumpet,m.is_edited,m.edited_at,m.message_memory,m.caption,m.message,m.message_id,m.is_group,m.receiver_id,m.sender_id,m.created_at,m.id,m.attachment,m.is_downloaded,null as progress,null as thumb,m.status,m.is_sync,m.message_type,m.is_respond_later,m.is_flag  FROM chat as m where ((m.receiver_id =  :workspaceUserId and m.is_group = 0) or (m.sender_id != :workspaceUserId and m.is_group =1 and m.receiver_id in (select group_id from group_member where user_id = :workspaceUserId and user_status = 1 and workspace_id =  :workspaceId ))) and (m.message_type = 1 or m.message_type=28) and m.is_downloaded IN (:download) and m.status =:status and m.workspace_id = :workspaceId order by m.created_at ASC")
    fun fetchWorkspaceSelfMedia(workspaceId: String, workspaceUserId: String,status:Int,download:List<Int>):LiveData<List<GalleryModel>>
    @Query("SELECT 0 as is_selected,m.pin as pin,m.is_trumpet,m.is_edited,m.edited_at,m.message_memory,m.caption,m.message,m.message_id,m.is_group,m.receiver_id,m.sender_id,m.created_at,m.id,m.attachment,m.is_downloaded,null as progress,null as thumb,m.status,m.is_sync,m.message_type,m.is_respond_later,m.is_flag  FROM chat as m where ((m.receiver_id =  :loginUserId and m.is_group = 0) or (m.sender_id != :loginUserId and m.is_group =1 and m.receiver_id in (select group_id from group_member where user_id = :loginUserId and user_status = 1 ))) and (m.message_type = 1 or m.message_type=28) and m.is_downloaded IN (:download) and m.status =:status order by m.created_at ASC")
    fun fetchSelfMedia(loginUserId: String,status:Int,download:List<Int>):LiveData<List<GalleryModel>>*/

//    @Query("SELECT 0 as is_selected,m.pin as pin,m.is_trumpet,m.is_edited,m.edited_at,m.message_memory,m.caption,m.message,m.message_id,m.is_group,m.receiver_id,m.sender_id,m.created_at,m.id,m.attachment,m.is_downloaded,null as progress,null as thumb,m.status,m.is_sync,m.message_type,m.is_respond_later,m.is_flag FROM chat as m where ((m.receiver_id =  :workspaceUserId and m.is_group = 0) or (m.sender_id != :workspaceUserId and m.is_group=1 and m.receiver_id in (select group_id from group_member where user_id = :workspaceUserId and user_status = 1 and workspace_id = :workspaceId))) and (lower(m.message) glob '*https://*[a-zA-Z0-9].*' or lower(m.message) glob '*http://*[a-zA-Z0-9].*' or lower(m.message) glob '*www.*[a-z].*' or lower(m.caption) glob '*https://*[a-zA-Z0-9].*' or lower(m.caption) glob '*http://*[a-zA-Z0-9].*' or lower(m.caption) glob '*www.*[a-z].*') and m.status = :status  and m.workspace_id = :workspaceId order by m.created_at DESC")
//    fun fetchWorkspaceSelfLinks(workspaceId: String,workspaceUserId: String,status: Int):LiveData<List<GalleryModel>>
//    @Query("SELECT 0 as is_selected,m.pin as pin,m.is_trumpet,m.is_edited,m.edited_at,m.message_memory,m.caption,m.message,m.message_id,m.is_group,m.receiver_id,m.sender_id,m.created_at,m.id,m.attachment,m.is_downloaded,null as progress,null as thumb,m.status,m.is_sync,m.message_type,m.is_respond_later,m.is_flag FROM chat as m where ((m.receiver_id =  :loginUserId and m.is_group = 0) or (m.sender_id != :loginUserId and m.is_group=1 and m.receiver_id in (select group_id from group_member where user_id = :loginUserId and user_status = 1))) and (lower(m.message) glob '*https://*[a-zA-Z0-9].*' or lower(m.message) glob '*http://*[a-zA-Z0-9].*' or lower(m.message) glob '*www.*[a-z].*' or lower(m.caption) glob '*https://*[a-zA-Z0-9].*' or lower(m.caption) glob '*http://*[a-zA-Z0-9].*' or lower(m.caption) glob '*www.*[a-z].*') and m.status = :status  order by m.created_at DESC")
//    fun fetchSelfLinks(loginUserId: String,status: Int):LiveData<List<GalleryModel>>

    /*@Query("SELECT 0 as is_selected,m.pin as pin,m.is_trumpet,m.is_edited,m.edited_at,m.message_memory,m.caption,m.message,m.message_id,m.is_group,m.receiver_id,m.sender_id,m.created_at,m.id,m.attachment,m.is_downloaded,null as progress,null as thumb,m.status,m.is_sync,m.message_type,m.is_respond_later,m.is_flag FROM chat as m where ((m.sender_id =  :workspaceUserid and m.receiver_id = :entityId and m.is_group = 0) or (m.sender_id=:entityId AND m.receiver_id= :workspaceUserid AND m.is_group=0)) and (m.message_type = 1 or m.message_type=28) and m.is_downloaded IN (:download)  and m.status =:status AND m.is_group = 0 and m.workspace_id = :workspaceId order by m.created_at ASC")
    fun fetchWorkspaceUserMedia(workspaceId: String, workspaceUserid: String,entityId: String,status: Int,download: List<Int>):LiveData<List<GalleryModel>>
    @Query("SELECT 0 as is_selected,m.pin as pin,m.is_trumpet,m.is_edited,m.edited_at,m.message_memory,m.caption,m.message,m.message_id,m.is_group,m.receiver_id,m.sender_id,m.created_at,m.id,m.attachment,m.is_downloaded,null as progress,null as thumb,m.status,m.is_sync,m.message_type,m.is_respond_later,m.is_flag FROM chat as m where ((m.sender_id =  :loginUserId and m.receiver_id = :entityId and m.is_group = 0) or (m.sender_id=:entityId AND m.receiver_id= :loginUserId AND m.is_group=0)) and (m.message_type = 1 or m.message_type=28) and m.is_downloaded IN (:download)  and m.status =:status AND m.is_group = 0  order by m.created_at ASC")
    fun fetchUserMedia(loginUserId: String,entityId: String,status: Int,download: List<Int>):LiveData<List<GalleryModel>>*/

//    @Query("SELECT 0 as is_selected,m.pin as pin,m.is_trumpet,m.is_edited,m.edited_at,m.message_memory,m.caption,m.message,m.message_id,m.is_group,m.receiver_id,m.sender_id,m.created_at,m.id,m.attachment,m.is_downloaded,null as progress,null as thumb,m.status,m.is_sync,m.message_type,m.is_respond_later,m.is_flag FROM chat AS m WHERE ((m.receiver_id=:workspaceUserId AND m.sender_id=:entityId) OR (m.receiver_id=:entityId AND m.sender_id=:workspaceUserId)) AND (lower(m.message) glob '*https://*[a-zA-Z0-9].*' or lower(m.message) glob '*http://*[a-zA-Z0-9].*' or lower(m.message) glob '*www.*[a-z].*' or lower(m.caption) glob '*https://*[a-zA-Z0-9].*' or lower(m.caption) glob '*http://*[a-zA-Z0-9].*' or lower(m.caption) glob '*www.*[a-z].*') AND m.is_group = 0 AND m.workspace_id = :workspaceId AND m.status = :status ")
//    fun fetchWorkspaceUserLinks(workspaceId: String,workspaceUserId: String,entityId: String,status: Int):LiveData<List<GalleryModel>>
//    @Query("SELECT 0 as is_selected,m.pin as pin,m.is_trumpet,m.is_edited,m.edited_at,m.message_memory,m.caption,m.message,m.message_id,m.is_group,m.receiver_id,m.sender_id,m.created_at,m.id,m.attachment,m.is_downloaded,null as progress,null as thumb,m.status,m.is_sync,m.message_type,m.is_respond_later,m.is_flag FROM chat AS m WHERE ((m.receiver_id=:loginUserId AND m.sender_id=:entityId) OR (m.receiver_id=:entityId AND m.sender_id=:loginUserId)) AND (lower(m.message) glob '*https://*[a-zA-Z0-9].*' or lower(m.message) glob '*http://*[a-zA-Z0-9].*' or lower(m.message) glob '*www.*[a-z].*' or lower(m.caption) glob '*https://*[a-zA-Z0-9].*' or lower(m.caption) glob '*http://*[a-zA-Z0-9].*' or lower(m.caption) glob '*www.*[a-z].*') AND m.is_group = 0 AND m.status = :status ")
//    fun fetchUserLinks(loginUserId: String,entityId: String,status: Int):LiveData<List<GalleryModel>>

    /*@Query("SELECT 0 as is_selected,m.pin as pin,m.is_trumpet,m.is_edited,m.edited_at,m.message_memory,m.caption,m.message,m.message_id,m.is_group,m.receiver_id,m.sender_id,m.created_at,m.id,m.attachment,m.is_downloaded,null as progress,null as thumb,m.status,m.is_sync,m.message_type,m.is_respond_later,m.is_flag FROM chat AS m WHERE m.receiver_id=:entityId AND m.is_group = 1 AND m.is_downloaded IN (:download) AND (m.message_type =1 or m.message_type=28) AND m.workspace_id =:workspaceId AND m.status = :status")
    fun fetchWorkspaceGroupMedia(workspaceId: String, entityId: String,status: Int,download: List<Int>):LiveData<List<GalleryModel>>
    @Query("SELECT 0 as is_selected,m.pin as pin,m.is_trumpet,m.is_edited,m.edited_at,m.message_memory,m.caption,m.message,m.message_id,m.is_group,m.receiver_id,m.sender_id,m.created_at,m.id,m.attachment,m.is_downloaded,null as progress,null as thumb,m.status,m.is_sync,m.message_type,m.is_respond_later,m.is_flag FROM chat AS m WHERE m.receiver_id=:entityId AND m.is_group = 1 AND m.is_downloaded IN (:download) AND (m.message_type =1 or m.message_type=28)  AND m.status = :status")
    fun fetchGroupMedia(entityId: String,status: Int,download: List<Int>):LiveData<List<GalleryModel>>*/

//    @Query("SELECT 0 as is_selected,m.pin as pin,m.is_trumpet,m.is_edited,m.edited_at,m.message_memory,m.caption,m.message,m.message_id,m.is_group,m.receiver_id,m.sender_id,m.created_at,m.id,m.attachment,m.is_downloaded,null as progress,null as thumb,m.status,m.is_sync,m.message_type,m.is_respond_later,m.is_flag FROM chat as m where m.receiver_id=:entityId and (lower(m.message) glob '*https://*[a-zA-Z0-9].*' or lower(m.message) glob '*http://*[a-zA-Z0-9].*' or lower(m.message) glob '*www.*[a-z].*' or lower(m.caption) glob '*https://*[a-zA-Z0-9].*' or lower(m.caption) glob '*http://*[a-zA-Z0-9].*' or lower(m.caption) glob '*www.*[a-z].*') AND m.is_group = 1 AND m.status =:status and m.workspace_id = :workspaceId order by m.created_at DESC")
//    fun fetchWorkspaceGroupLinks(workspaceId: String,entityId: String,status: Int):LiveData<List<GalleryModel>>
//    @Query("SELECT 0 as is_selected,m.pin as pin,m.is_trumpet,m.is_edited,m.edited_at,m.message_memory,m.caption,m.message,m.message_id,m.is_group,m.receiver_id,m.sender_id,m.created_at,m.id,m.attachment,m.is_downloaded,null as progress,null as thumb,m.status,m.is_sync,m.message_type,m.is_respond_later,m.is_flag FROM chat as m where m.receiver_id=:entityId and (lower(m.message) glob '*https://*[a-zA-Z0-9].*' or lower(m.message) glob '*http://*[a-zA-Z0-9].*' or lower(m.message) glob '*www.*[a-z].*' or lower(m.caption) glob '*https://*[a-zA-Z0-9].*' or lower(m.caption) glob '*http://*[a-zA-Z0-9].*' or lower(m.caption) glob '*www.*[a-z].*') AND m.is_group = 1 AND m.status =:status  order by m.created_at DESC")
//    fun fetchGroupLinks(entityId: String,status: Int):LiveData<List<GalleryModel>>

    /*@Query("SELECT updated_at FROM chat ORDER BY updated_at DESC limit 1")
    fun fetchLastUpdatedTime():String

    @Query("SELECT ID FROM chat ORDER BY ID DESC limit 1")
    fun fetchLastId():Long
    @Query("SELECT * FROM chat WHERE message_id = :messageId AND workspace_id = :workspaceId")
    fun fetchWorkspaceMessageByMessageID(messageId: Long,workspaceId: String):Messenger

    @Query("SELECT * FROM chat WHERE message_id = :messageId")
    fun fetchMessageByMessageID(messageId: Long):Messenger
    @Query("SELECT * FROM chat WHERE ID = :id AND workspace_id = :workspaceId")
    fun fetchWorkspaceMessageByLocalID(id: Long,workspaceId: String):Messenger

    @Query("SELECT * FROM chat WHERE ID = :id")
    fun fetchMessageByLocalID(id: Long):Messenger
    @Query("UPDATE chat SET file_path =:filepath,status=1 WHERE ID=:id")
    fun updateMessageFilePath(filepath:String,id:Long)

    @Query("SELECT * FROM chat WHERE message_id = 0 AND is_sync=0 AND status!=0 AND sender_id=:userIds AND workspace_id=:workspaceId")
    fun fetchWorkspaceUnSentMessages(userIds:Long,workspaceId:String) :List<Messenger>
    @Query("SELECT * FROM chat WHERE message_id = 0 AND is_sync=0 AND status!=0 AND sender_id=:userIds")
    fun fetchUnSentMessages(userIds:Long) :List<Messenger>

    @Query("SELECT * FROM chat WHERE workspace_id = :workspaceId AND sender_id=:workspaceUserId AND is_group = :entityType AND receiver_id=:entityId ORDER BY ID DESC limit 1")
    fun fetchWorkspaceUseLastMessageAfterBurnout(workspaceId: String,workspaceUserId: String,entityId: String,entityType: Int):Messenger
    @Query("SELECT * FROM chat WHERE  sender_id=:loginUserId AND is_group = :entityType AND receiver_id=:entityId ORDER BY ID DESC limit 1")
    fun fetchUseLastMessageAfterBurnout(loginUserId: String,entityId: String,entityType: Int):Messenger

    @Query("UPDATE chat SET status = 0,updated_at =:updatedAt WHERE ((sender_id=:userId AND receiver_id=:workspaceUserId) OR (sender_id=:workspaceUserId AND receiver_id=:userId)) AND is_group = 0 AND workspace_id = :workspaceId AND status IN (3)")
    fun deleteWorkspaceBurnoutMessages(workspaceUserId: String,workspaceId: String,updatedAt: String,userId: String)
    @Query("UPDATE chat SET status = 0,updated_at =:updatedAt WHERE ((sender_id=:userId AND receiver_id=:loginUserId) OR (sender_id=:loginUserId AND receiver_id=:userId)) AND is_group = 0  AND status IN (3)")
    fun deleteBurnoutMessages(loginUserId: String,updatedAt: String,userId: String)

    @Query("UPDATE chat SET status = 0 WHERE workspace_id = :workspaceId AND status = 3")
    fun updateWorkspaceAllBurnoutMessageStatus(workspaceId: String)
    @Query("UPDATE chat SET status = 0 WHERE status = 3")
    fun updateAllBurnoutMessageStatus()*/

    /*@Query("UPDATE chat SET status = 0 WHERE workspace_id = :workspaceId AND status = 3 AND ((sender_id =:entityId AND receiver_id =:workspaceUserId) or (receiver_id =:entityId AND sender_id =:workspaceUserId)) AND is_group = 0")
    fun updateWorkspaceBurnoutMessage(entityId:String,workspaceId:String,workspaceUserId: String)
    @Query("UPDATE chat SET status = 0 WHERE  status = 3 AND ((sender_id =:entityId AND receiver_id =:loginUserId) or (receiver_id =:entityId AND sender_id =:loginUserId)) AND is_group = 0")
    fun updateBurnoutMessage(entityId:String,loginUserId: String)

    @Query("SELECT case WHEN m.message_id = 0 THEN \"L\" || m.id ELSE m.message_id END as dummy_group_by, CASE WHEN (m.is_group != 1) THEN 1 ELSE 2 end as entity_type,m.is_downloaded,m.attachment,m.workspace_id as workspace_id, m.message_hash as message_hash,m.status as status,m.attachment as attachment,m.message_id as id,m.message_type as message_type, m.message as message, m.created_at as created_at,m.is_read as is_read, m.sender_id as sender_id, m.is_group as is_group, mg.group_type,mgm.user_role,m.receiver_id as receiver_id, u.name as sender_name, u.user_avatar as sender_profile_pic,u.user_status as user_status ,w.workspace_name as company_name, CASE WHEN (m.is_group = 0) THEN u1.name ELSE mg.group_name end as receiver_name, CASE WHEN (m.is_group = 0) THEN u1.user_avatar ELSE mg.group_avatar end as receiver_profile_pic,IFNULL(CASE WHEN (m.is_group = 0) THEN (CASE WHEN (m.sender_id = u.user_id) THEN u.is_muted ELSE u1.is_muted end) ELSE mg.is_muted end,0) as is_entity_muted,CASE WHEN (m.is_group = 0) THEN 1 ELSE mg.is_active end as is_group_active FROM chat as m INNER JOIN workspace as w ON m.workspace_id = w.workspace_id  LEFT JOIN user as u ON m.sender_id = u.user_id AND m.workspace_id = u.workspace_id LEFT JOIN user as u1 ON m.receiver_id = u1.user_id AND m.workspace_id = u1.workspace_id AND m.is_group != 0 LEFT JOIN messenger_group as mg ON m.receiver_id = mg.group_id AND m.is_group = 1 LEFT JOIN group_member as mgm ON mg.group_id = mgm.group_id and mgm.user_id IN (:workspaceUserIds) and mg.workspace_id = mgm.workspace_id WHERE m.id IN (SELECT cm.id as id FROM chat as cm INNER JOIN (SELECT m.id as id,CASE WHEN (sender_id < receiver_id AND is_group != 1) THEN sender_id || '-' || receiver_id || '-' || '-user' WHEN (receiver_id < sender_id AND is_group != 1) THEN receiver_id || '-' || sender_id || '-' || '-user' ELSE sender_id || '-' || receiver_id || '-' || '-group' END as concat_id,MAX(created_at) as created_at FROM chat as m WHERE (m.sender_id NOT IN (:workspaceUserIds) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type =24 or m.message_type = 26) and m.status > 0 and m.workspace_id = :workspaceId) OR (m.receiver_id IN (:workspaceUserIds) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type =23 or m.message_type =24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0 and m.workspace_id = :workspaceId) OR (m.sender_id NOT IN (:workspaceUserIds) and m.is_group = 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type = 24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0 and workspace_id =  :workspaceId) GROUP BY concat_id,id ORDER BY created_at DESC) AS cmi ON cmi.id = cm.id) and (mgm.user_status = 1 or (m.is_group = 0 AND mgm.user_status IS NULL)) AND m.created_at> :conditionTime  GROUP BY dummy_group_by HAVING is_entity_muted = 0 AND is_group_active = 1 ORDER BY created_at DESC")
    fun fetchWorkspaceNotificationDataWithWorkspaceID(workspaceUserIds: List<String>,workspaceId: String,conditionTime: String):List<NotificationData>
    @Query("SELECT case WHEN m.message_id = 0 THEN \"L\" || m.id ELSE m.message_id END as dummy_group_by, CASE WHEN (m.is_group != 1) THEN 1 ELSE 2 end as entity_type,m.is_downloaded,m.attachment,m.workspace_id as workspace_id, m.message_hash as message_hash,m.status as status,m.attachment as attachment,m.message_id as id,m.message_type as message_type, m.message as message, m.created_at as created_at,m.is_read as is_read, m.sender_id as sender_id, m.is_group as is_group, mg.group_type,mgm.user_role,m.receiver_id as receiver_id, u.name as sender_name, u.user_avatar as sender_profile_pic,u.user_status as user_status ,w.workspace_name as company_name, CASE WHEN (m.is_group = 0) THEN u1.name ELSE mg.group_name end as receiver_name, CASE WHEN (m.is_group = 0) THEN u1.user_avatar ELSE mg.group_avatar end as receiver_profile_pic,IFNULL(CASE WHEN (m.is_group = 0) THEN (CASE WHEN (m.sender_id = u.user_id) THEN u.is_muted ELSE u1.is_muted end) ELSE mg.is_muted end,0) as is_entity_muted,CASE WHEN (m.is_group = 0) THEN 1 ELSE mg.is_active end as is_group_active FROM chat as m INNER JOIN workspace as w ON m.workspace_id = w.workspace_id  LEFT JOIN user as u ON m.sender_id = u.user_id AND m.workspace_id = u.workspace_id LEFT JOIN user as u1 ON m.receiver_id = u1.user_id AND m.workspace_id = u1.workspace_id AND m.is_group != 0 LEFT JOIN messenger_group as mg ON m.receiver_id = mg.group_id AND m.is_group = 1 LEFT JOIN group_member as mgm ON mg.group_id = mgm.group_id and mgm.user_id IN (:loginUserId) and mg.workspace_id = mgm.workspace_id WHERE m.id IN (SELECT cm.id as id FROM chat as cm INNER JOIN (SELECT m.id as id,CASE WHEN (sender_id < receiver_id AND is_group != 1) THEN sender_id || '-' || receiver_id || '-' || '-user' WHEN (receiver_id < sender_id AND is_group != 1) THEN receiver_id || '-' || sender_id || '-' || '-user' ELSE sender_id || '-' || receiver_id || '-' || '-group' END as concat_id,MAX(created_at) as created_at FROM chat as m WHERE (m.sender_id NOT IN (:loginUserId) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type =24 or m.message_type = 26) and m.status > 0 ) OR (m.receiver_id IN (:loginUserId) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type =23 or m.message_type =24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0 ) OR (m.sender_id NOT IN (:loginUserId) and m.is_group = 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type = 24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0 ) GROUP BY concat_id,id ORDER BY created_at DESC) AS cmi ON cmi.id = cm.id) and (mgm.user_status = 1 or (m.is_group = 0 AND mgm.user_status IS NULL)) AND m.created_at> :conditionTime  GROUP BY dummy_group_by HAVING is_entity_muted = 0 AND is_group_active = 1 ORDER BY created_at DESC")
    fun fetchNotificationData(loginUserId: List<String>,conditionTime: String):List<NotificationData>

    @Query("SELECT case WHEN m.message_id = 0 THEN \"L\" || m.id ELSE m.message_id END as dummy_group_by, CASE WHEN (m.is_group != 1) THEN 1 ELSE 2 end as entity_type,m.is_downloaded,m.attachment,m.workspace_id as workspace_id, m.message_hash as message_hash,m.status as status,m.attachment as attachment,m.message_id as id,m.message_type as message_type, m.message as message, m.created_at as created_at,m.is_read as is_read, m.sender_id as sender_id, m.is_group as is_group, mg.group_type,mgm.user_role,m.receiver_id as receiver_id, u.name as sender_name, u.user_avatar as sender_profile_pic,u.user_status as user_status ,w.workspace_name as company_name, CASE WHEN (m.is_group = 0) THEN u1.name ELSE mg.group_name end as receiver_name, CASE WHEN (m.is_group = 0) THEN u1.user_avatar ELSE mg.group_avatar end as receiver_profile_pic,IFNULL(CASE WHEN (m.is_group = 0) THEN (CASE WHEN (m.sender_id = u.user_id) THEN u.is_muted ELSE u1.is_muted end) ELSE mg.is_muted end,0) as is_entity_muted,CASE WHEN (m.is_group = 0) THEN 1 ELSE mg.is_active end as is_group_active FROM chat as m INNER JOIN workspace as w ON m.workspace_id = w.workspace_id  LEFT JOIN user as u ON m.sender_id = u.user_id AND m.workspace_id = u.workspace_id LEFT JOIN user as u1 ON m.receiver_id = u1.user_id AND m.workspace_id = u1.workspace_id AND m.is_group != 0 LEFT JOIN messenger_group as mg ON m.receiver_id = mg.group_id AND m.is_group = 1 LEFT JOIN group_member as mgm ON mg.group_id = mgm.group_id and mgm.user_id IN (:workspaceUserIds) and mg.workspace_id = mgm.workspace_id WHERE m.id IN (SELECT cm.id as id FROM chat as cm INNER JOIN (SELECT m.id as id,CASE WHEN (sender_id < receiver_id AND is_group != 1) THEN sender_id || '-' || receiver_id || '-' || '-user' WHEN (receiver_id < sender_id AND is_group != 1) THEN receiver_id || '-' || sender_id || '-' || '-user' ELSE sender_id || '-' || receiver_id || '-' || '-group' END as concat_id,MAX(created_at) as created_at FROM chat as m WHERE (m.sender_id NOT IN (:workspaceUserIds) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type =24 or m.message_type = 26) and m.status > 0 and m.workspace_id = :workspaceId) OR (m.receiver_id IN (:workspaceUserIds) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type =23 or m.message_type =24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0 and m.workspace_id = :workspaceId) OR (m.sender_id NOT IN (:workspaceUserIds) and m.is_group = 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type = 24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0 and workspace_id =  :workspaceId) GROUP BY concat_id,id ORDER BY created_at DESC) AS cmi ON cmi.id = cm.id) and (mgm.user_status = 1 or (m.is_group = 0 AND mgm.user_status IS NULL))   GROUP BY dummy_group_by HAVING is_entity_muted = 0 AND is_group_active = 1 ORDER BY created_at DESC")
    fun fetchWorkspaceNotificationDataWithWorkspaceIDWithoutCondition(workspaceUserIds: List<String>,workspaceId: String):List<NotificationData>
    @Query("SELECT case WHEN m.message_id = 0 THEN \"L\" || m.id ELSE m.message_id END as dummy_group_by, CASE WHEN (m.is_group != 1) THEN 1 ELSE 2 end as entity_type,m.is_downloaded,m.attachment,m.workspace_id as workspace_id, m.message_hash as message_hash,m.status as status,m.attachment as attachment,m.message_id as id,m.message_type as message_type, m.message as message, m.created_at as created_at,m.is_read as is_read, m.sender_id as sender_id, m.is_group as is_group, mg.group_type,mgm.user_role,m.receiver_id as receiver_id, u.name as sender_name, u.user_avatar as sender_profile_pic,u.user_status as user_status ,w.workspace_name as company_name, CASE WHEN (m.is_group = 0) THEN u1.name ELSE mg.group_name end as receiver_name, CASE WHEN (m.is_group = 0) THEN u1.user_avatar ELSE mg.group_avatar end as receiver_profile_pic,IFNULL(CASE WHEN (m.is_group = 0) THEN (CASE WHEN (m.sender_id = u.user_id) THEN u.is_muted ELSE u1.is_muted end) ELSE mg.is_muted end,0) as is_entity_muted,CASE WHEN (m.is_group = 0) THEN 1 ELSE mg.is_active end as is_group_active FROM chat as m INNER JOIN workspace as w ON m.workspace_id = w.workspace_id  LEFT JOIN user as u ON m.sender_id = u.user_id AND m.workspace_id = u.workspace_id LEFT JOIN user as u1 ON m.receiver_id = u1.user_id AND m.workspace_id = u1.workspace_id AND m.is_group != 0 LEFT JOIN messenger_group as mg ON m.receiver_id = mg.group_id AND m.is_group = 1 LEFT JOIN group_member as mgm ON mg.group_id = mgm.group_id and mgm.user_id IN (:loginUserId) and mg.workspace_id = mgm.workspace_id WHERE m.id IN (SELECT cm.id as id FROM chat as cm INNER JOIN (SELECT m.id as id,CASE WHEN (sender_id < receiver_id AND is_group != 1) THEN sender_id || '-' || receiver_id || '-' || '-user' WHEN (receiver_id < sender_id AND is_group != 1) THEN receiver_id || '-' || sender_id || '-' || '-user' ELSE sender_id || '-' || receiver_id || '-' || '-group' END as concat_id,MAX(created_at) as created_at FROM chat as m WHERE (m.sender_id NOT IN (:loginUserId) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type =24 or m.message_type = 26) and m.status > 0 ) OR (m.receiver_id IN (:loginUserId) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type =23 or m.message_type =24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0) OR (m.sender_id NOT IN (:loginUserId) and m.is_group = 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type = 24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0 ) GROUP BY concat_id,id ORDER BY created_at DESC) AS cmi ON cmi.id = cm.id) and (mgm.user_status = 1 or (m.is_group = 0 AND mgm.user_status IS NULL))   GROUP BY dummy_group_by HAVING is_entity_muted = 0 AND is_group_active = 1 ORDER BY created_at DESC")
    fun fetchNotificationDataWithWorkspaceIDWithoutCondition(loginUserId: List<String>):List<NotificationData>

    @Query("SELECT case WHEN m.message_id = 0 THEN \"L\" || m.id ELSE m.message_id END as dummy_group_by, CASE WHEN (m.is_group != 1) THEN 1 ELSE 2 end as entity_type,m.is_downloaded,m.attachment,m.workspace_id as workspace_id, m.message_hash as message_hash,m.status as status,m.attachment as attachment,m.message_id as id,m.message_type as message_type, m.message as message, m.created_at as created_at,m.is_read as is_read, m.sender_id as sender_id, m.is_group as is_group,mg.group_type,mgm.user_role, m.receiver_id as receiver_id, u.name as sender_name, u.user_avatar as sender_profile_pic,u.user_status as user_status,w.workspace_name as company_name, CASE WHEN (m.is_group = 0) THEN u1.name ELSE mg.group_name end as receiver_name, CASE WHEN (m.is_group = 0) THEN u1.user_avatar ELSE mg.group_avatar end as receiver_profile_pic,IFNULL(CASE WHEN (m.is_group = 0) THEN (CASE WHEN (m.sender_id = u.user_id) THEN u.is_muted ELSE u1.is_muted end) ELSE mg.is_muted end,0) as is_entity_muted,CASE WHEN (m.is_group = 0) THEN 1 ELSE mg.is_active end as is_group_active FROM chat as m INNER JOIN workspace as w ON m.workspace_id = w.workspace_id  LEFT JOIN user as u ON m.sender_id = u.user_id AND u.user_status = 1 AND m.workspace_id = u.workspace_id LEFT JOIN user as u1 ON m.receiver_id = u1.user_id AND u1.user_status = 1 AND m.workspace_id = u1.workspace_id AND m.is_group != 0 LEFT JOIN messenger_group as mg ON m.receiver_id = mg.group_id AND m.is_group = 1 LEFT JOIN group_member as mgm ON mg.group_id = mgm.group_id and mgm.user_id IN (:workspaceUserIds) and mg.workspace_id = mgm.workspace_id WHERE m.id IN (SELECT cm.id as id FROM chat as cm INNER JOIN (SELECT m.id as id,CASE WHEN (sender_id < receiver_id AND is_group != 1) THEN sender_id || '-' || receiver_id || '-' || '-user' WHEN (receiver_id < sender_id AND is_group != 1) THEN receiver_id || '-' || sender_id || '-' || '-user' ELSE sender_id || '-' || receiver_id || '-' || '-group' END as concat_id,MAX(created_at) as created_at FROM chat as m WHERE (m.sender_id NOT IN (:workspaceUserIds) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type = 24 or m.message_type = 26) and m.status > 0) OR (m.receiver_id IN (:workspaceUserIds) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5  or m.message_type = 23 or m.message_type =  24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0) OR (m.sender_id NOT IN (:workspaceUserIds) and m.is_group = 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type = 24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0)  GROUP BY concat_id,id ORDER BY created_at DESC) as cmi ON cmi.id = cm.id) and (mgm.user_status = 1 or (m.is_group = 0 AND mgm.user_status IS NULL)) AND m.created_at >:conditionTime GROUP BY dummy_group_by HAVING  is_entity_muted = 0 AND is_group_active = 1 ORDER BY created_at DESC")
    fun fetchWorkspaceNotification(workspaceUserIds: List<String>,conditionTime: String):List<NotificationData>
    @Query("SELECT case WHEN m.message_id = 0 THEN \"L\" || m.id ELSE m.message_id END as dummy_group_by, CASE WHEN (m.is_group != 1) THEN 1 ELSE 2 end as entity_type,m.is_downloaded,m.attachment,m.workspace_id as workspace_id, m.message_hash as message_hash,m.status as status,m.attachment as attachment,m.message_id as id,m.message_type as message_type, m.message as message, m.created_at as created_at,m.is_read as is_read, m.sender_id as sender_id, m.is_group as is_group,mg.group_type,mgm.user_role, m.receiver_id as receiver_id, u.name as sender_name, u.user_avatar as sender_profile_pic,u.user_status as user_status,w.workspace_name as company_name, CASE WHEN (m.is_group = 0) THEN u1.name ELSE mg.group_name end as receiver_name, CASE WHEN (m.is_group = 0) THEN u1.user_avatar ELSE mg.group_avatar end as receiver_profile_pic,IFNULL(CASE WHEN (m.is_group = 0) THEN (CASE WHEN (m.sender_id = u.user_id) THEN u.is_muted ELSE u1.is_muted end) ELSE mg.is_muted end,0) as is_entity_muted,CASE WHEN (m.is_group = 0) THEN 1 ELSE mg.is_active end as is_group_active FROM chat as m INNER JOIN workspace as w ON m.workspace_id = w.workspace_id  LEFT JOIN user as u ON m.sender_id = u.user_id AND u.user_status = 1 AND m.workspace_id = u.workspace_id LEFT JOIN user as u1 ON m.receiver_id = u1.user_id AND u1.user_status = 1 AND m.workspace_id = u1.workspace_id AND m.is_group != 0 LEFT JOIN messenger_group as mg ON m.receiver_id = mg.group_id AND m.is_group = 1 LEFT JOIN group_member as mgm ON mg.group_id = mgm.group_id and mgm.user_id IN (:loginUserId) and mg.workspace_id = mgm.workspace_id WHERE m.id IN (SELECT cm.id as id FROM chat as cm INNER JOIN (SELECT m.id as id,CASE WHEN (sender_id < receiver_id AND is_group != 1) THEN sender_id || '-' || receiver_id || '-' || '-user' WHEN (receiver_id < sender_id AND is_group != 1) THEN receiver_id || '-' || sender_id || '-' || '-user' ELSE sender_id || '-' || receiver_id || '-' || '-group' END as concat_id,MAX(created_at) as created_at FROM chat as m WHERE (m.sender_id NOT IN (:loginUserId) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type = 24 or m.message_type = 26) and m.status > 0) OR (m.receiver_id IN (:loginUserId) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5  or m.message_type = 23 or m.message_type =  24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0) OR (m.sender_id NOT IN (:loginUserId) and m.is_group = 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type = 24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0)  GROUP BY concat_id,id ORDER BY created_at DESC) as cmi ON cmi.id = cm.id) and (mgm.user_status = 1 or (m.is_group = 0 AND mgm.user_status IS NULL)) AND m.created_at >:conditionTime GROUP BY dummy_group_by HAVING  is_entity_muted = 0 AND is_group_active = 1 ORDER BY created_at DESC")
    fun fetchNotification(loginUserId: List<String>,conditionTime: String):List<NotificationData>

    @Query("SELECT case WHEN m.message_id = 0 THEN \"L\" || m.id ELSE m.message_id END as dummy_group_by, CASE WHEN (m.is_group != 1) THEN 1 ELSE 2 end as entity_type,m.is_downloaded,m.attachment,m.workspace_id as workspace_id, m.message_hash as message_hash,m.status as status,m.attachment as attachment,m.message_id as id,m.message_type as message_type, m.message as message, m.created_at as created_at,m.is_read as is_read, m.sender_id as sender_id, m.is_group as is_group,mg.group_type,mgm.user_role, m.receiver_id as receiver_id, u.name as sender_name, u.user_avatar as sender_profile_pic,u.user_status as user_status,w.workspace_name as company_name, CASE WHEN (m.is_group = 0) THEN u1.name ELSE mg.group_name end as receiver_name, CASE WHEN (m.is_group = 0) THEN u1.user_avatar ELSE mg.group_avatar end as receiver_profile_pic,IFNULL(CASE WHEN (m.is_group = 0) THEN (CASE WHEN (m.sender_id = u.user_id) THEN u.is_muted ELSE u1.is_muted end) ELSE mg.is_muted end,0) as is_entity_muted,CASE WHEN (m.is_group = 0) THEN 1 ELSE mg.is_active end as is_group_active FROM chat as m INNER JOIN workspace as w ON m.workspace_id = w.workspace_id  LEFT JOIN user as u ON m.sender_id = u.user_id AND u.user_status = 1 AND m.workspace_id = u.workspace_id LEFT JOIN user as u1 ON m.receiver_id = u1.user_id AND u1.user_status = 1 AND m.workspace_id = u1.workspace_id AND m.is_group != 0 LEFT JOIN messenger_group as mg ON m.receiver_id = mg.group_id AND m.is_group = 1 LEFT JOIN group_member as mgm ON mg.group_id = mgm.group_id and mgm.user_id IN (:workspaceUserIds) and mg.workspace_id = mgm.workspace_id WHERE m.id IN (SELECT cm.id as id FROM chat as cm INNER JOIN (SELECT m.id as id,CASE WHEN (sender_id < receiver_id AND is_group != 1) THEN sender_id || '-' || receiver_id || '-' || '-user' WHEN (receiver_id < sender_id AND is_group != 1) THEN receiver_id || '-' || sender_id || '-' || '-user' ELSE sender_id || '-' || receiver_id || '-' || '-group' END as concat_id,MAX(created_at) as created_at FROM chat as m WHERE (m.sender_id NOT IN (:workspaceUserIds) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type = 24 or m.message_type = 26) and m.status > 0) OR (m.receiver_id IN (:workspaceUserIds) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5  or m.message_type = 23 or m.message_type =  24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0) OR (m.sender_id NOT IN (:workspaceUserIds) and m.is_group = 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type = 24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0)  GROUP BY concat_id,id ORDER BY created_at DESC) as cmi ON cmi.id = cm.id) and (mgm.user_status = 1 or (m.is_group = 0 AND mgm.user_status IS NULL)) GROUP BY dummy_group_by HAVING  is_entity_muted = 0 AND is_group_active = 1 ORDER BY created_at DESC")
    fun fetchWorkspaceNotificationWithoutCondition(workspaceUserIds: List<String>):List<NotificationData>
    @Query("SELECT case WHEN m.message_id = 0 THEN \"L\" || m.id ELSE m.message_id END as dummy_group_by, CASE WHEN (m.is_group != 1) THEN 1 ELSE 2 end as entity_type,m.is_downloaded,m.attachment,m.workspace_id as workspace_id, m.message_hash as message_hash,m.status as status,m.attachment as attachment,m.message_id as id,m.message_type as message_type, m.message as message, m.created_at as created_at,m.is_read as is_read, m.sender_id as sender_id, m.is_group as is_group,mg.group_type,mgm.user_role, m.receiver_id as receiver_id, u.name as sender_name, u.user_avatar as sender_profile_pic,u.user_status as user_status,w.workspace_name as company_name, CASE WHEN (m.is_group = 0) THEN u1.name ELSE mg.group_name end as receiver_name, CASE WHEN (m.is_group = 0) THEN u1.user_avatar ELSE mg.group_avatar end as receiver_profile_pic,IFNULL(CASE WHEN (m.is_group = 0) THEN (CASE WHEN (m.sender_id = u.user_id) THEN u.is_muted ELSE u1.is_muted end) ELSE mg.is_muted end,0) as is_entity_muted,CASE WHEN (m.is_group = 0) THEN 1 ELSE mg.is_active end as is_group_active FROM chat as m INNER JOIN workspace as w ON m.workspace_id = w.workspace_id  LEFT JOIN user as u ON m.sender_id = u.user_id AND u.user_status = 1 AND m.workspace_id = u.workspace_id LEFT JOIN user as u1 ON m.receiver_id = u1.user_id AND u1.user_status = 1 AND m.workspace_id = u1.workspace_id AND m.is_group != 0 LEFT JOIN messenger_group as mg ON m.receiver_id = mg.group_id AND m.is_group = 1 LEFT JOIN group_member as mgm ON mg.group_id = mgm.group_id and mgm.user_id IN (:loginUserId) and mg.workspace_id = mgm.workspace_id WHERE m.id IN (SELECT cm.id as id FROM chat as cm INNER JOIN (SELECT m.id as id,CASE WHEN (sender_id < receiver_id AND is_group != 1) THEN sender_id || '-' || receiver_id || '-' || '-user' WHEN (receiver_id < sender_id AND is_group != 1) THEN receiver_id || '-' || sender_id || '-' || '-user' ELSE sender_id || '-' || receiver_id || '-' || '-group' END as concat_id,MAX(created_at) as created_at FROM chat as m WHERE (m.sender_id NOT IN (:loginUserId) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type = 24 or m.message_type = 26) and m.status > 0) OR (m.receiver_id IN (:loginUserId) and m.is_group != 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5  or m.message_type = 23 or m.message_type =  24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0) OR (m.sender_id NOT IN (:loginUserId) and m.is_group = 1 and m.is_read != 1 and m.message_id != 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type = 24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28) and m.status >0)  GROUP BY concat_id,id ORDER BY created_at DESC) as cmi ON cmi.id = cm.id) and (mgm.user_status = 1 or (m.is_group = 0 AND mgm.user_status IS NULL)) GROUP BY dummy_group_by HAVING  is_entity_muted = 0 AND is_group_active = 1 ORDER BY created_at DESC")
    fun fetchNotificationWithoutCondition(loginUserId: List<String>):List<NotificationData>*/

    /*@Query("select MAX(m.created_at) as created_at, IFNULL(CASE WHEN (m.is_group = 0) THEN (CASE WHEN (m.sender_id = u1.user_id) THEN u1.is_muted ELSE u2.is_muted end) ELSE mg.is_muted end,0) as muted FROM chat as m INNER JOIN user as u1 ON u1.user_id = m.sender_id and u1.workspace_id = m.workspace_id LEFT JOIN user as u2 ON u2.user_id = m.receiver_id and u2.workspace_id = m.workspace_id and m.is_group =0 LEFT JOIN messenger_group as mg ON mg.group_id = m.receiver_id AND mg.workspace_id = m.workspace_id AND m.is_group =1 where m.message_id != 0 and m.sender_id NOT IN (:userIds) and muted = 0 and (m.message_type < 5 or m.message_type = 23 or m.message_type =24 or m.message_type = 26 or m.message_type = 27 or m.message_type = 28)")
    fun fetchMaxMessageCreatedAt(userIds: List<String>):NotificationMaxDate
    @Query("UPDATE chat SET is_downloaded = 1, attachment=:filepath WHERE ID=:id AND workspace_id =:workspaceId")
    fun updateWorkspaceDownloadStatus(id: Long,filepath: String,workspaceId: String)*/

//    @Query("UPDATE chat SET is_downloaded = 1, attachment=:filepath WHERE ID=:id")
//    fun updateDownloadStatus(id: Long,filepath: String)
//    @Query("UPDATE chat SET is_downloaded = 1, attachment=:filepath WHERE message_id=:messageId AND workspace_id =:workspaceId")
//    fun updateWorkspaceDownloadStatusBymessageId(messageId: Long,filepath: String,workspaceId: String)
//
//    @Query("UPDATE chat SET is_downloaded = 1, attachment=:filepath WHERE message_id=:messageId")
//    fun updateDownloadStatusBymessageId(messageId: Long,filepath: String)
//    @Query("SELECT ID FROM chat WHERE message_type IN (1,23,26,25,28) AND status IN (1,3) AND is_downloaded!=1 limit 5 ")
//    fun fetchMessageForDownloading() :List<Long>
//
//    @Query("SELECT message_id FROM chat WHERE ((sender_id=:workspaceUserId AND receiver_id=:entityId) OR (sender_id=:entityId AND receiver_id=:workspaceUserId)) AND is_group = 0 AND status IN (1,2) AND workspace_id=:workspaceId AND message_id=:messageId")
//    fun checkWorkspaceUserReplyMessageExistsInDB(workspaceId: String,workspaceUserId:String,entityId: String,messageId:Long):Long
//    @Query("SELECT message_id FROM chat WHERE ((sender_id=:loginUserId AND receiver_id=:entityId) OR (sender_id=:entityId AND receiver_id=:loginUserId)) AND is_group = 0 AND status IN (1,2)  AND message_id=:messageId")
//    fun checkUserReplyMessageExistsInDB(loginUserId: String,entityId: String,messageId:Long):Long
//
//    @Query("SELECT message_id FROM chat WHERE receiver_id=:entityId AND is_group= 1  AND status IN (1,2) AND workspace_id = :workspaceId AND message_id =:messageId")
//    fun checkWorkspaceGroupReplyMessageExistsInDB(workspaceId: String,entityId: String,messageId: Long):Long
//    @Query("SELECT message_id FROM chat WHERE receiver_id=:entityId AND is_group= 1  AND status IN (1,2) AND  message_id =:messageId")
//    fun checkGroupReplyMessageExistsInDB(entityId: String,messageId: Long):Long
//
//    @Query("SELECT message_id FROM chat WHERE ((sender_id=:workspaceUserId AND receiver_id=:entityId) OR (sender_id=:entityId AND receiver_id=:workspaceUserId)) AND is_group=0 AND workspace_id=:workspaceId AND reply_message_type!=-1 ORDER BY message_id ASC limit 1")
//    fun fetchWorkspaceUserLastMessageID(entityId: String,workspaceUserId: String,workspaceId: String) : Long
//    @Query("SELECT message_id FROM chat WHERE ((sender_id=:loginUserId AND receiver_id=:entityId) OR (sender_id=:entityId AND receiver_id=:loginUserId)) AND is_group=0 AND reply_message_type!=-1 ORDER BY message_id ASC limit 1")
//    fun fetchUserLastMessageID(entityId: String,loginUserId: String) : Long
//
//    @Query("SELECT message_id FROM chat WHERE receiver_id=:entityId AND is_group=1 AND workspace_id=:workspaceId AND reply_message_type!=-1 ORDER BY message_id ASC limit 1")
//    fun fetchWorkspaceGroupLastMessageID(entityId: String,workspaceId: String):Long
//    @Query("SELECT message_id FROM chat WHERE receiver_id=:entityId AND is_group=1  AND reply_message_type!=-1 ORDER BY message_id ASC limit 1")
//    fun fetchGroupLastMessageID(entityId: String):Long
//
//    @Query("UPDATE chat SET file_path=:filepath,is_downloaded = 1 ,attachment=:attachmentPath WHERE message_id=:messageId AND workspace_id =:workspaceId")
//    fun updateWorkspaceAttachment(filepath: String,messageId: String,attachmentPath: String,workspaceId: String)
//    @Query("UPDATE chat SET file_path=:filepath,is_downloaded = 1 ,attachment=:attachmentPath WHERE message_id=:messageId")
//    fun updateAttachment(filepath: String,messageId: String,attachmentPath: String)
//
//    @Query("DELETE FROM chat WHERE workspace_id =:workspaceId")
//    fun removeWorkspaceMessages(workspaceId: String)
//    @Query("DELETE FROM chat")
//    fun removeMessages()
//
//    @Query("DELETE FROM chat WHERE workspace_id =:workspaceId AND message_type NOT IN (1,23,26,28)")
//    fun removeWorkspaceMessagesByOnlyMessages(workspaceId: String)
//    @Query("DELETE FROM chat WHERE  message_type NOT IN (1,23,26,28)")
//    fun removeMessagesByOnlyMessages()
//
//    @Query("DELETE FROM chat WHERE workspace_id =:workspaceId AND message_type IN (1,23,26,28)")
//    fun removeWorkspaceMessagesByFiles(workspaceId: String)
//    @Query("DELETE FROM chat WHERE  message_type IN (1,23,26,28)")
//    fun removeMessagesByFiles()
//
//    @Query("SELECT file_path FROM chat WHERE ID=:id")
//    fun fetchFilePath(id: Long): String
//    @Query("SELECT attachment FROM chat WHERE ID=:id")
//    fun fetchFilePathMine(id: Long): String
//
//    @Query("SELECT attachment FROM chat WHERE workspace_id = :workspaceId AND is_downloaded = 1")
//    fun fetchWorkspaceDownloadedFiles(workspaceId: String) :List<String>
//    @Query("SELECT attachment FROM chat WHERE  is_downloaded = 1")
//    fun fetchDownloadedFiles() :List<String>
//
//    @Query("SELECT attachment FROM chat WHERE workspace_id=:workspaceId AND message_type= 26 AND ID=:id AND is_downloaded=1")
//    fun fetchWorkspaceAttachment(workspaceId: String,id: Long):String
//    @Query("SELECT attachment FROM chat WHERE  message_type= 26 AND ID=:id AND is_downloaded=1")
//    fun fetchAttachment(id: Long):String
//
//    @Query("SELECT attachment FROM chat WHERE workspace_id = :workspaceId AND is_downloaded = 1 AND message_id < :messageId")
//    fun fetchWorkspaceDownloadedFilesWithCondition(workspaceId: String,messageId : String) :List<String>
//    @Query("SELECT attachment FROM chat WHERE is_downloaded = 1 AND message_id < :messageId")
//    fun fetchDownloadedFilesWithCondition(messageId : String) :List<String>
//
//    @Query("SELECT * FROM chat WHERE is_reply = 1 AND workspace_id =:workspaceId and message_type IN (0,1,2,3,23,24) AND is_group = 1 AND receiver_id=:entityId ")
//    fun fetchWorkspaceGroupReplyMessage(entityId: String,workspaceId: String) : List<Messenger>
//    @Query("SELECT * FROM chat WHERE is_reply = 1  and message_type IN (0,1,2,3,23,24) AND is_group = 1 AND receiver_id=:entityId ")
//    fun fetchGroupReplyMessage(entityId: String) : List<Messenger>
//
//    @Query("SELECT * FROM chat WHERE is_reply = 1 AND workspace_id =:workspaceId and message_type IN (0,1,2,3,23,24) AND is_group = 0 AND ((receiver_id=:entityId AND sender_id=:workspaceUserId) OR (receiver_id=:workspaceUserId AND sender_id=:entityId))")
//    fun fetchWorkspaceUserReplyMessage(entityId: String,workspaceId: String,workspaceUserId: String) : List<Messenger>
//    @Query("SELECT * FROM chat WHERE is_reply = 1  and message_type IN (0,1,2,3,23,24) AND is_group = 0 AND ((receiver_id=:entityId AND sender_id=:loginUserId) OR (receiver_id=:loginUserId AND sender_id=:entityId))")
//    fun fetchUserReplyMessage(entityId: String,loginUserId: String) : List<Messenger>
//
//    @Query("UPDATE chat SET original_message =:originalMessage WHERE message_id=:messageId AND is_group=:isGroup AND workspace_id =:workspaceId")
//    fun updateWorkspaceOriginalMessage(originalMessage:String,messageId: Long,isGroup:Int,workspaceId: String)
//    @Query("UPDATE chat SET original_message =:originalMessage WHERE message_id=:messageId AND is_group=:isGroup")
//    fun updateOriginalMessage(originalMessage:String,messageId: Long,isGroup:Int)
//
//    @Query("SELECT ID FROM chat WHERE message_id = :messageId AND workspace_id = :workspaceId")
//    fun fetchWorkspaceLocalIdFormMessageId(messageId: String,workspaceId: String) : Long
//    @Query("SELECT ID FROM chat WHERE message_id = :messageId")
//    fun fetchLocalIdFormMessageId(messageId: String) : Long
//
//    @Query("SELECT ID,message_id,attachment,is_downloaded FROM chat where ((sender_id = :workspaceUserId and receiver_id = :entityId) or (sender_id = :entityId and receiver_id= :workspaceUserId))  and workspace_id = :workspaceId  and is_group <> 1   and status IN (1) and message_id <> 0")
//    fun fetchWorkspaceUserDownloadedMessages(entityId: String,workspaceUserId: String,workspaceId: String):List<DownloadedAttachmentModel>
//    @Query("SELECT ID,message_id,attachment,is_downloaded FROM chat where ((sender_id = :loginUserId and receiver_id = :entityId) or (sender_id = :entityId and receiver_id= :loginUserId))   and is_group <> 1   and status IN (1) and message_id <> 0")
//    fun fetchUserDownloadedMessages(entityId: String,loginUserId: String):List<DownloadedAttachmentModel>
//
//    @Query("SELECT ID,message_id,attachment,is_downloaded FROM chat where receiver_id =:entityId and workspace_id = :workspaceId and is_group = 1  and status IN (1) and message_id <> 0")
//    fun fetchWorkspaceGroupDownloadedMessages(entityId: String,workspaceId: String):List<DownloadedAttachmentModel>
//    @Query("SELECT ID,message_id,attachment,is_downloaded FROM chat where receiver_id =:entityId  and is_group = 1  and status IN (1) and message_id <> 0")
//    fun fetchGroupDownloadedMessages(entityId: String):List<DownloadedAttachmentModel>
//
//    @Query("UPDATE chat SET message = :message WHERE message_id=:messageId AND workspace_id =:workspaceId")
//    fun updateWorkspaceMissedCallMessage(workspaceId: String,message:String,messageId: Long)
//    @Query("UPDATE chat SET message = :message WHERE message_id=:messageId")
//    fun updateMissedCallMessage(message:String,messageId: Long)
//
//    @Query("SELECT min(created_at) as created_at FROM chat limit 1")
//    fun fetchMinimumCreatedAt() : String
//    @Query("SELECT * FROM chat WHERE ((sender_id=:userId AND receiver_id=:receiverId) OR (sender_id=:receiverId AND receiver_id=:userId)) AND is_group=0 AND workspace_id=:workspaceId AND status!=0 ORDER BY created_at DESC limit 1")
//    fun fetchWorkspaceUserLastMessage(userId: String,receiverId: Long,workspaceId: String):Messenger
//
//    @Query("SELECT * FROM chat WHERE ((sender_id=:userId AND receiver_id=:receiverId) OR (sender_id=:receiverId AND receiver_id=:userId)) AND is_group=0 AND status!=0 ORDER BY created_at DESC limit 1")
//    fun fetchUserLastMessage(userId: String,receiverId: Long):Messenger
//
//    @Query("SELECT * FROM chat WHERE receiver_id=:receiverId AND is_group=1 AND workspace_id=:workspaceId AND status!=0 ORDER BY created_at DESC limit 1")
//    fun fetchWorkspaceGroupLastMessage(receiverId: Long,workspaceId: String):Messenger
//    @Query("SELECT * FROM chat WHERE receiver_id=:receiverId AND is_group=1  AND status!=0 ORDER BY created_at DESC limit 1")
//    fun fetchGroupLastMessage(receiverId: Long):Messenger
//
//    @Query("DELETE FROM chat WHERE :whereCondition AND workspace_id = :workspaceId")
//    fun deleteWorkspaceMessages(whereCondition:String,workspaceId: String)
//    @Query("DELETE FROM chat WHERE :whereCondition")
//    fun deleteMessages(whereCondition:String)
//
//    @Query("SELECT workspace_id FROM chat WHERE ID =:id")
//    fun fetchWorkspaceIdFromLocalID(id: Long):String
//
//    @Query("DELETE FROM chat WHERE ID=:id AND workspace_id=:workspaceId")
//    fun deleteWorkspaceMessageWhileSendingError(id:Long,workspaceId: String)
//    @Query("DELETE FROM chat WHERE ID=:id")
//    fun deleteMessageWhileSendingError(id:Long)
//
//    @Query("SELECT MAX(message_id) FROM chat WHERE workspace_id=:workspaceId")
//    fun fetchWorkspaceLastMessageID(workspaceId: String) : Long
//    @Query("SELECT MAX(message_id) FROM chat")
//    fun fetchLastMessageID() : Long

//    @Query("SELECT is_read FROM chat WHERE message_id=:messageId AND workspace_id=:workspaceId")
//    fun checkWorkspaceMessageIsUnread(messageId: String,workspaceId: String):Int
//    @Query("SELECT is_read FROM chat WHERE message_id=:messageId")
//    fun checkMessageIsUnread(messageId: String):Int
//    @Query("UPDATE chat SET attachment=:attachment,is_downloaded=0 WHERE workspace_id=:workspaceId AND message_id=:messageId")
//
//    fun updateWorkspaceMessageAttachment(attachment:String,workspaceId: String,messageId: Long)
//    @Query("UPDATE chat SET attachment=:attachment,is_downloaded=0 WHERE  message_id=:messageId")
//    fun updateMessageAttachment(attachment:String,messageId: Long)

    /*@Query("UPDATE chat SET pin = :pin,pinned_by=:userId,pinned_at=:createdAt,updated_at=:createdAt WHERE workspace_id=:workspaceId AND message_id=:messageId")
    fun updateWorkspacePinMessage(workspaceId: String,pin:Int,userId: String,messageId: Long,createdAt: String)
    @Query("UPDATE chat SET pin = :pin,pinned_by=:userId,pinned_at=:createdAt,updated_at=:createdAt WHERE  message_id=:messageId")
    fun updatePinMessage(pin:Int,userId: String,messageId: Long,createdAt: String)

    @Query("SELECT m.ID,u.name as pinnedUserName,m.caption as caption,m.message_id as messageID,m.message,m.message_type as messageType,m.attachment,m.is_group as isGroup,m.file_path as filePath,m.pinned_at as pinnedAt,m.pinned_by as pinnedBy,m.workspace_id as workspaceid,m.sender_id as senderId,m.receiver_id as receiverId,m.is_downloaded as isDownloaded FROM chat as m LEFT JOIN user as u ON u.workspace_id = m.workspace_id and u.user_id = m.pinned_by WHERE m.is_group=0 and m.status=1 and message_id>0 and m.pin=1 and m.workspace_id=:workspaceId and ((m.sender_id=:workspaceUserId and m.receiver_id=:entityId) or (m.sender_id=:entityId and m.receiver_id=:workspaceUserId)) ORDER BY m.pinned_at DESC")
    fun fetchWorkspaceUserPinnedMessages(workspaceId: String,workspaceUserId: String,entityId: String):PagingSource<Int,PinMessages>
    @Query("SELECT m.ID,u.name as pinnedUserName,m.caption as caption,m.message_id as messageID,m.message,m.message_type as messageType,m.attachment,m.is_group as isGroup,m.file_path as filePath,m.pinned_at as pinnedAt,m.pinned_by as pinnedBy,m.workspace_id as workspaceid,m.sender_id as senderId,m.receiver_id as receiverId,m.is_downloaded as isDownloaded FROM chat as m LEFT JOIN user as u ON u.workspace_id = m.workspace_id and u.user_id = m.pinned_by WHERE m.is_group=0 and m.status=1 and message_id>0 and m.pin=1  and ((m.sender_id=:loginUserId and m.receiver_id=:entityId) or (m.sender_id=:entityId and m.receiver_id=:loginUserId)) ORDER BY m.pinned_at DESC")
    fun fetchUserPinnedMessages(loginUserId: String,entityId: String):PagingSource<Int,PinMessages>

    @Query("SELECT m.ID,u.name as pinnedUserName,m.caption as caption,m.message_id as messageID,m.message,m.message_type as messageType,m.attachment,m.is_group as isGroup,m.file_path as filePath,m.pinned_at as pinnedAt,m.pinned_by as pinnedBy,m.workspace_id as workspaceid,m.sender_id as senderId,m.receiver_id as receiverId,m.is_downloaded as isDownloaded FROM chat as m LEFT JOIN user as u ON u.workspace_id = m.workspace_id and u.user_id = m.pinned_by WHERE m.is_group=0 and m.status=1 and message_id>0 and m.pin=1 and m.workspace_id=:workspaceId and ((m.sender_id=:workspaceUserId and m.receiver_id=:entityId) or (m.sender_id=:entityId and m.receiver_id=:workspaceUserId)) ORDER BY m.pinned_at DESC")
    fun fetchWorkspaceUserPinnedMessagesNew(workspaceId: String,workspaceUserId: String,entityId: String):LiveData<List<PinMessages>>
    @Query("SELECT m.ID,u.name as pinnedUserName,m.caption as caption,m.message_id as messageID,m.message,m.message_type as messageType,m.attachment,m.is_group as isGroup,m.file_path as filePath,m.pinned_at as pinnedAt,m.pinned_by as pinnedBy,m.workspace_id as workspaceid,m.sender_id as senderId,m.receiver_id as receiverId,m.is_downloaded as isDownloaded FROM chat as m LEFT JOIN user as u ON u.workspace_id = m.workspace_id and u.user_id = m.pinned_by WHERE m.is_group=0 and m.status=1 and message_id>0 and m.pin=1  and ((m.sender_id=:loginUserId and m.receiver_id=:entityId) or (m.sender_id=:entityId and m.receiver_id=:loginUserId)) ORDER BY m.pinned_at DESC")
    fun fetchUserPinnedMessagesNew(loginUserId: String,entityId: String):LiveData<List<PinMessages>>

    @Query("SELECT m.ID,u.name as pinnedUserName,m.caption as caption,m.message_id as messageID,m.message,m.message_type as messageType,m.attachment,m.is_group as isGroup,m.file_path as filePath,m.pinned_at as pinnedAt,m.pinned_by as pinnedBy,m.workspace_id as workspaceid,m.sender_id as senderId,m.receiver_id as receiverId,m.is_downloaded as isDownloaded FROM chat as m LEFT JOIN user as u ON u.workspace_id = m.workspace_id and u.user_id = m.pinned_by WHERE m.is_group=0 and m.status=1 and message_id>0 and m.pin=1 and m.workspace_id=:workspaceId and ((m.sender_id=:workspaceUserId and m.receiver_id=:entityId) or (m.sender_id=:entityId and m.receiver_id=:workspaceUserId)) ORDER BY m.pinned_at DESC limit 40 offset :offset")
    fun fetchWorkspaceUserPinnedMessage(workspaceId: String,workspaceUserId: String,entityId: String,offset:Int):List<PinMessages>
    @Query("SELECT m.ID,u.name as pinnedUserName,m.caption as caption,m.message_id as messageID,m.message,m.message_type as messageType,m.attachment,m.is_group as isGroup,m.file_path as filePath,m.pinned_at as pinnedAt,m.pinned_by as pinnedBy,m.workspace_id as workspaceid,m.sender_id as senderId,m.receiver_id as receiverId,m.is_downloaded as isDownloaded FROM chat as m LEFT JOIN user as u ON u.workspace_id = m.workspace_id and u.user_id = m.pinned_by WHERE m.is_group=0 and m.status=1 and message_id>0 and m.pin=1  and ((m.sender_id=:loginUserId and m.receiver_id=:entityId) or (m.sender_id=:entityId and m.receiver_id=:loginUserId)) ORDER BY m.pinned_at DESC limit 40 offset :offset")
    fun fetchUserPinnedMessage(loginUserId: String,entityId: String,offset:Int):List<PinMessages>

    @Query("SELECT m.ID,u.name as pinnedUserName,m.caption as caption,m.message_id as messageID,m.message,m.message_type as messageType,m.attachment,m.is_group as isGroup,m.file_path as filePath,m.pinned_at as pinnedAt,m.pinned_by as pinnedBy,m.workspace_id as workspaceid,m.sender_id as senderId,m.receiver_id as receiverId,m.is_downloaded as isDownloaded FROM chat as m LEFT JOIN user as u ON u.workspace_id = m.workspace_id and u.user_id = m.pinned_by WHERE  m.status=1 and m.message_id>0 and m.pin=1 and m.workspace_id=:workspaceId and m.message_id=:messageId")
    fun fetchWorkspacePinMessageByMessageId(messageId: Long,workspaceId: String):PinMessages
    @Query("SELECT m.ID,u.name as pinnedUserName,m.caption as caption,m.message_id as messageID,m.message,m.message_type as messageType,m.attachment,m.is_group as isGroup,m.file_path as filePath,m.pinned_at as pinnedAt,m.pinned_by as pinnedBy,m.workspace_id as workspaceid,m.sender_id as senderId,m.receiver_id as receiverId,m.is_downloaded as isDownloaded FROM chat as m LEFT JOIN user as u ON u.workspace_id = m.workspace_id and u.user_id = m.pinned_by WHERE  m.status=1 and m.message_id>0 and m.pin=1 and m.message_id=:messageId")
    fun fetchPinMessageByMessageId(messageId: Long):PinMessages

    @Query("SELECT m.ID,u.name as pinnedUserName,m.caption as caption,m.message_id as messageID,m.message,m.message_type as messageType,m.attachment,m.is_group as isGroup,m.file_path as filePath,m.pinned_at as pinnedAt,m.pinned_by as pinnedBy,m.workspace_id as workspaceid,m.sender_id as senderId,m.receiver_id as receiverId,m.is_downloaded as isDownloaded FROM chat as m LEFT JOIN user as u ON u.workspace_id = m.workspace_id and u.user_id = m.pinned_by WHERE m.is_group=1 and m.status=1 and message_id>0 and m.pin=1 and m.workspace_id=:workspaceId and m.receiver_id=:entityId ORDER BY m.pinned_at DESC")
    fun fetchWorkspaceGroupPinnedMessages(workspaceId: String,entityId: String):PagingSource<Int,PinMessages>
    @Query("SELECT m.ID,u.name as pinnedUserName,m.caption as caption,m.message_id as messageID,m.message,m.message_type as messageType,m.attachment,m.is_group as isGroup,m.file_path as filePath,m.pinned_at as pinnedAt,m.pinned_by as pinnedBy,m.workspace_id as workspaceid,m.sender_id as senderId,m.receiver_id as receiverId,m.is_downloaded as isDownloaded FROM chat as m LEFT JOIN user as u ON u.workspace_id = m.workspace_id and u.user_id = m.pinned_by WHERE m.is_group=1 and m.status=1 and message_id>0 and m.pin=1  and m.receiver_id=:entityId ORDER BY m.pinned_at DESC")
    fun fetchGroupPinnedMessages(entityId: String):PagingSource<Int,PinMessages>

    @Query("SELECT m.ID,u.name as pinnedUserName,m.caption as caption,m.message_id as messageID,m.message,m.message_type as messageType,m.attachment,m.is_group as isGroup,m.file_path as filePath,m.pinned_at as pinnedAt,m.pinned_by as pinnedBy,m.workspace_id as workspaceid,m.sender_id as senderId,m.receiver_id as receiverId,m.is_downloaded as isDownloaded FROM chat as m LEFT JOIN user as u ON u.workspace_id = m.workspace_id and u.user_id = m.pinned_by WHERE m.is_group=1 and m.status=1 and message_id>0 and m.pin=1 and m.workspace_id=:workspaceId and m.receiver_id=:entityId ORDER BY m.pinned_at DESC limit 40 offset :offset")
    fun fetchWorkspaceGroupPinnedMessage(workspaceId: String,entityId: String,offset: Int):List<PinMessages>
    @Query("SELECT m.ID,u.name as pinnedUserName,m.caption as caption,m.message_id as messageID,m.message,m.message_type as messageType,m.attachment,m.is_group as isGroup,m.file_path as filePath,m.pinned_at as pinnedAt,m.pinned_by as pinnedBy,m.workspace_id as workspaceid,m.sender_id as senderId,m.receiver_id as receiverId,m.is_downloaded as isDownloaded FROM chat as m LEFT JOIN user as u ON u.workspace_id = m.workspace_id and u.user_id = m.pinned_by WHERE m.is_group=1 and m.status=1 and message_id>0 and m.pin=1  and m.receiver_id=:entityId ORDER BY m.pinned_at DESC limit 40 offset :offset")
    fun fetchGroupPinnedMessage(entityId: String,offset: Int):List<PinMessages>*/


    //-- start--//
    @Query("SELECT message_id FROM chat WHERE message_id=:messageId ")
    fun checkMessageExists(messageId:Long) : Long
    @Query("DELETE FROM chat WHERE receiver_id IN (:groupIds) AND is_group= 1")
    fun deleteGroupMessages(groupIds: List<Long>)
    @Query("DELETE FROM chat WHERE (receiver_id IN (:userIds) OR sender_id IN (:userIds)) AND is_group= 0")
    fun deleteUserMessages(userIds:List<String>)
    @Query("DELETE FROM chat WHERE (sender_id=:entityId AND receiver_id=:userId AND is_group=0) OR (sender_id=:userId AND receiver_id=:entityId AND is_group=0)")
    fun bulkDeleteUserChats(entityId:String,userId:String)
    @Query("DELETE FROM chat WHERE receiver_id=:entityId AND is_group=1")
    fun bulkDeleteGroupChats(entityId:String)
    /*@Query("SELECT GROUP_CONTACT(message,'\n') as message FROM chat WHERE ID IN (:ids) AND message_type=0")
    fun fetchCopiedMessages(ids: List<Long>):List<String>*/
    @Query("SELECT message FROM chat WHERE ID IN (:ids) AND message_type=0")
    fun fetchCopiedMessages(ids: List<Long>):List<String>
    @Query("SELECT ID,message_id,message_type,sender_id,receiver_id,is_group,status FROM chat WHERE ID IN (:ids)")
    fun fetchMessagesForMessageActions(ids: List<Long>):List<MessengerModel>
    @Query("SELECT message_id FROM chat WHERE (is_group=0 AND receiver_id=:entityId AND is_delivered=0 AND message_id!=0) OR (is_group=1 AND sender_id!=:entityId AND is_delivered=0 AND message_id!=0)")
    fun fetchUndeliveredMessages(entityId: String):List<Long>
    @Query("DELETE FROM chat WHERE message_id IN (:messageIds)")
    fun deleteServerMessages(messageIds: List<Long>)
    @Query("DELETE FROM chat WHERE ID IN (:ids)")
    fun deleteLocalMessages(ids:List<Long>)
    @Query("SELECT local_attachment_path,attachment_downloaded,ID,message_id,message,attachment,message_type,sender_id FROM chat WHERE ID IN (:ids)")
    fun fetchMessageIdsFormLocalIds(ids: List<Long>):List<MessageMap>

    @Query("SELECT message_id FROM chat WHERE ID IN (:ids)")
    fun fetchMessagesIdsIdsFormLocalIds(ids: List<Long>):List<Long>


    @Query("SELECT local_attachment_path,attachment_downloaded,ID,message_id,message,attachment,message_type,sender_id FROM chat WHERE message_id IN (:messageIds) AND message_id!=0 AND status=1 GROUP BY message_id")
    fun fetchMessagesForForwarding(messageIds: List<String>):List<MessageMap>

    @Query("SELECT local_attachment_path,attachment_downloaded,ID,message_id,sender_id,message,attachment,message_type FROM chat WHERE receiver_id=:receiverId AND message_type= 4 AND is_group=1")
    fun fetchGroupCreationRecord(receiverId: String):List<MessageMap>
    /*@Query("SELECT * FROM chat WHERE status!=0 AND is_sync=0")
    fun fetchUnsentMessages():List<Messenger>*/
    @Query("SELECT * FROM chat WHERE message_id=:messageId")
    fun fetchReplyOriginalMessageDetails(messageId: Long):Messenger
    @Query("SELECT message_id FROM chat WHERE message_id IN (:messageIds)")
    fun matchMessageIds(messageIds: List<Long>):List<Long>
    @Query("SELECT ID FROM chat WHERE ID IN (:localIds)")
    fun matchLocalIds(localIds: List<Long>):List<Long>
    @Query("SELECT message_id FROM chat ORDER BY message_id DESC LIMIT 1")
    fun fetchLastMessageId():Long
    @Query("SELECT ID FROM chat ORDER BY ID DESC LIMIT 1")
    fun fetchLastLocalId():Long
    @Query("SELECT ID FROM chat WHERE message_id=:messageId")
    fun fetchLocalIdFromMessageId(messageId: Long):Long
    @Query("SELECT * FROM chat WHERE message_id=:messageId")
    fun fetchMessageByMessageId(messageId: Long):Messenger
    @Query("SELECT * FROM chat WHERE ID=:id")
    fun fetchMessageByLocalId(id: String):Messenger

    @RawQuery(observedEntities = [Messenger::class, User::class,MessengerGroup::class,GroupMembers::class])
    fun fetchConversationMessages(query: SupportSQLiteQuery):PagingSource<Int, ConversationModel>
    @RawQuery(observedEntities = [Messenger::class,User::class,MessengerGroup::class])
    fun fetchForwardList(query: SupportSQLiteQuery):LiveData<List<ForwardModel>>
    @RawQuery(observedEntities = [Messenger::class,User::class,MessengerGroup::class])
    fun fetchArchivedList(query: SupportSQLiteQuery):LiveData<List<RecentMessageList>>
    @RawQuery(observedEntities = [Messenger::class,User::class,MessengerGroup::class])
    fun fetchRecentMessageList(query: SupportSQLiteQuery):LiveData<List<RecentMessageList>>

    /*@RawQuery(observedEntities = [Messenger::class,User::class])
    fun fetchUserRecentMessageList(query: SupportSQLiteQuery):LiveData<List<RecentList>>*/
    @RawQuery(observedEntities = [Messenger::class,User::class])
    fun fetchUserOrGroupRecentMessageList(query: SupportSQLiteQuery):List<RecentLastMessage>

    @Query("UPDATE chat SET is_sync = 1,is_delivered = 1,updated_at =:updatedAt WHERE message_id=:messageId")
    fun updateMessageDeliveredStatus(messageId: Long,updatedAt: String)

    @Query("UPDATE chat SET is_sync = 1,is_delivered = 1,updated_at =:updatedAt WHERE message_id IN (:messageId) ")
    fun updateMessageDeliveredStatus(messageId: List<Long>,updatedAt: String)

    @Query("UPDATE chat SET is_sync = 1,is_delivered = 1,is_read =1,updated_at =:updatedAt WHERE message_id=:messageId ")
    fun updateMessageReadStatus(messageId: Long,updatedAt: String)
    @Query("UPDATE chat SET is_sync = 1,is_delivered = 1,is_read =1,updated_at =:updatedAt WHERE message_id IN (:messageId)")
    fun updateMessageReadStatus(messageId: List<Long>,updatedAt: String)

    @Query("UPDATE chat SET status =:status , message=:message,attachment='',caption='',updated_at = :updatedAt WHERE message_id IN (:messageIds)")
    fun  updateMessageRecall(status:Int,messageIds:List<String>,updatedAt: String,message:String)

    /*@Query("SELECT IFNULL(MAX(updated_at,'')) AS updated_at FROM chat UNION ALL SELECT IFNULL(MAX(updated_at,'')) AS updated_at FROM user UNION ALL SELECT IFNULL(MAX(updated_at),'') AS updated_at FROM chat_group UNION ALL SELECT IFNULL(MAX(updated_at),'') AS updated_at FROM group_member UNION ALL SELECT IFNULL(MAX(updated_at),'') AS updated_at FROM contact UNION ALL SELECT IFNULL(MAX(updated_at),'') AS updated_at FROM story UNION ALL SELECT IFNULL(MAX(created_at),'') AS updated_at FROM broadcast ORDER BY updated_at DESC LIMIT 1 ")
    fun fetchCurrentTime():String*/

    @RawQuery(observedEntities = [Messenger::class,User::class,MessengerGroup::class])
    fun fetchCurrentTime(query: SupportSQLiteQuery):String

    @Query("SELECT 3 as platform,sender_uid,receiver_uid,ID as id,message_id,sender_id,receiver_id,message,attachment,caption,preview_link,is_group,is_reply,is_forward,is_forkout,is_flag,original_message_id,is_sync,is_read,is_delivered,message_type,status,created_at,attachment_downloaded,local_attachment_path,'' as access_token,'' as reference_id  FROM chat WHERE status!=0 AND is_sync=0")
    fun fetchUnSentMessages():List<UnSentMessageModel>

    @Query("UPDATE chat SET attachment_downloaded=1,local_attachment_path=:path WHERE message_id=:messageId")
    fun updateAttachmentDownloaded(path:String,messageId: Long)

    @Query("SELECT message_id FROM chat WHERE is_sync=1 AND is_read=0 AND message_id>0 AND sender_id=:entityId and receiver_id=:userId AND is_group = 0 ")
    fun fetchUserUnReadMessages(entityId: String,userId: String) : List<Long>

    @Query("SELECT message_id FROM chat WHERE is_sync=1 AND is_read=0 AND message_id>0 AND sender_id!=:userId AND receiver_id=:entityId AND is_group = 1 ")
    fun fetchGroupUnReadMessages(userId: String,entityId: String):List<Long>

    @Query("UPDATE chat SET attachment_path=:awsPath WHERE ID=:id")
    fun updateAwsPath(awsPath:String,id:Long)

    @Query("UPDATE chat SET is_like=:status WHERE message_id=:messageId")
    fun updateLikeStatus(messageId: Long,status: Int)

    @Query("UPDATE chat SET message =:message, is_edited =:isEdited,updated_at=:updatedAt,edited_at =:editedAt,message_type = :messageType,caption =:caption WHERE message_id =:messageId")
    fun updateMessageEdit(message:String,isEdited:Int,editedAt:String,updatedAt: String,messageType:Int,messageId: Long,caption:String)

    @Query("SELECT ID,message_id,message_type,attachment,preview_link,attachment_downloaded,created_at,sender_name FROM chat WHERE ((sender_id=:userId AND receiver_id=:loginUserId) OR (sender_id=:loginUserId AND receiver_id=:userId)) AND is_group = 0  AND message_id>0 AND is_sync != 0 AND message_type IN (1,23)")
    fun fetchUserMedia(userId: String,loginUserId:String):LiveData<List<MediaModel>>

    @Query("SELECT ID,message_id,message_type,attachment,preview_link,attachment_downloaded,created_at,sender_name FROM chat WHERE receiver_id=:groupId AND is_group = 1  AND message_id>0 AND is_sync != 0 AND message_type IN (1,23)")
    fun fetchGroupMedia(groupId: String):LiveData<List<MediaModel>>
}