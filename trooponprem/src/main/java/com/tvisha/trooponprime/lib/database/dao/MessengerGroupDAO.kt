package com.tvisha.trooponprime.lib.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.tvisha.trooponprime.lib.clientModels.MessengerGroupModel
import com.tvisha.trooponprime.lib.database.GroupMembers
import com.tvisha.trooponprime.lib.database.Messenger
import com.tvisha.trooponprime.lib.database.MessengerGroup
import com.tvisha.trooponprime.lib.database.User
import com.tvisha.trooponprime.lib.database.model.CommonGroups
import com.tvisha.trooponprime.lib.database.model.ForkOutModel
import com.tvisha.trooponprime.lib.database.model.GroupModel
import com.tvisha.trooponprime.lib.database.model.RecentList

@Dao
internal interface MessengerGroupDAO {
    @Insert
    fun insertGroup(messengerGroup: MessengerGroup)
    @Update
    fun updateGroup(messengerGroup: MessengerGroup)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBulkMessengerGroup(messengerGroups: List<MessengerGroup>)

    /*@Query("SELECT group_id FROM chat_group WHERE group_id =:groupId and workspace_id =:workspaceId")
    fun checkWorkspaceGroupExists(groupId:Long,workspaceId:String):Int
    @Query("SELECT group_id FROM chat_group WHERE group_id =:groupId")
    fun checkGroupExists(groupId:Long):Int

    @Query("UPDATE chat_group SET is_favourite = :favourite,updated_at = :updatedAt WHERE group_id = :entityId AND workspace_id =:workspaceId")
    fun updateWorkspaceGroupFavouriteStatus(entityId:String,favourite:Int,workspaceId: String,updatedAt:String)
    @Query("UPDATE chat_group SET is_favourite = :favourite,updated_at = :updatedAt WHERE group_id = :entityId")
    fun updateGroupFavouriteStatus(entityId:String,favourite:Int,updatedAt:String)

    @Query("UPDATE chat_group SET is_muted = :muteStatus,updated_at = :updatedAt WHERE group_id = :entityId AND workspace_id =:workspaceId")
    fun updateWorkspaceGroupMuteStatus(entityId:String,muteStatus:Int,workspaceId: String,updatedAt:String)
    @Query("UPDATE chat_group SET is_muted = :muteStatus,updated_at = :updatedAt WHERE group_id = :entityId")
    fun updateGroupMuteStatus(entityId:String,muteStatus:Int,updatedAt:String)

    @Query("UPDATE chat_group SET unread_count = :count WHERE group_id = :groupId AND workspace_id=:workspaceId")
    fun updateWorkspaceGroupUnreadCount(count:Int,groupId: String,workspaceId: String)
    @Query("UPDATE chat_group SET unread_count = :count WHERE group_id = :groupId")
    fun updateGroupUnreadCount(count:Int,groupId: String)

    @Query("UPDATE chat_group SET respond_later_count = :count WHERE group_id = :groupId AND workspace_id=:workspaceId")
    fun updateWorkspaceGroupRespondLaterCount(count:Int,groupId: String,workspaceId: String)
    @Query("UPDATE chat_group SET respond_later_count = :count WHERE group_id = :groupId")
    fun updateGroupRespondLaterCount(count:Int,groupId: String)

    @Query("UPDATE chat_group SET read_receipt_count = :count WHERE group_id = :groupId AND workspace_id=:workspaceId")
    fun updateWorkspaceGroupReadReceiptCount(count:Int,groupId: String,workspaceId: String)
    @Query("UPDATE chat_group SET read_receipt_count = :count WHERE group_id = :groupId")
    fun updateGroupReadReceiptCount(count:Int,groupId: String)

    @Query("SELECT * FROM chat_group WHERE workspace_id =:workspaceId AND group_id = :groupId")
    fun fetchWorkspaceGroup(workspaceId: String,groupId: String) : MessengerGroup
    @Query("SELECT * FROM chat_group WHERE group_id = :groupId")
    fun fetchGroup(groupId: String) : MessengerGroup

    @Query("SELECT group_name FROM chat_group WHERE group_id=:groupId AND workspace_id=:workspaceId" )
    fun fetchWorkspaceGroupName(groupId:String,workspaceId: String):String
    @Query("SELECT group_name FROM chat_group WHERE group_id=:groupId" )
    fun fetchGroupName(groupId:String):String

    @Query("UPDATE chat_group SET is_orange_group =:orangeStatus,updated_at =:updatedAt WHERE workspace_id= :workspaceId AND group_id= :groupId")
    fun updateWorkspaceOrangeGroupStatus(orangeStatus:Int,workspaceId: String,groupId: String,updatedAt: String)
    @Query("UPDATE chat_group SET is_orange_group =:orangeStatus,updated_at =:updatedAt WHERE  group_id= :groupId")
    fun updateOrangeGroupStatus(orangeStatus:Int,groupId: String,updatedAt: String)

    @Query("UPDATE chat_group SET group_name =:groupName,updated_at =:updatedAt WHERE workspace_id= :workspaceId AND group_id= :groupId")
    fun updateWorkspaceGroupName(groupName:String,workspaceId: String,groupId: String,updatedAt: String)
    @Query("UPDATE chat_group SET group_name =:groupName,updated_at =:updatedAt WHERE  group_id= :groupId")
    fun updateGroupName(groupName:String,groupId: String,updatedAt: String)

    @Query("UPDATE chat_group SET group_avatar =:groupAvatar,updated_at =:updatedAt WHERE workspace_id= :workspaceId AND group_id= :groupId")
    fun updateWorkspaceGroupAvatar(groupAvatar:String,workspaceId: String,groupId: String,updatedAt: String)
    @Query("UPDATE chat_group SET group_avatar =:groupAvatar,updated_at =:updatedAt WHERE  group_id= :groupId")
    fun updateGroupAvatar(groupAvatar:String,groupId: String,updatedAt: String)

    @Query("DELETE FROM chat_group WHERE group_id= :groupId AND workspace_id=:workspaceId")
    fun deleteWorkspaceGroup(groupId: String,workspaceId: String)
    @Query("DELETE FROM chat_group WHERE group_id= :groupId ")
    fun deleteTheGroup(groupId: String)

    @Query("UPDATE chat_group SET unread_count = (SELECT unread_count FROM chat_group WHERE group_id = :groupId AND workspace_id = :workspaceId )+1 WHERE group_id = :groupId AND workspace_id = :workspaceId")
    fun increaseWorkspaceUnreadGroupCount(groupId: String,workspaceId: String)
    @Query("UPDATE chat_group SET unread_count = (SELECT unread_count FROM chat_group WHERE group_id = :groupId )+1 WHERE group_id = :groupId")
    fun increaseUnreadGroupCount(groupId: String)

    @Query("UPDATE chat_group SET unread_count = (SELECT unread_count FROM chat_group WHERE group_id = :groupId AND workspace_id = :workspaceId )-1 WHERE group_id = :groupId AND workspace_id = :workspaceId AND unread_count>0")
    fun decreaseWorkspaceUnreadGroupCount(groupId: String,workspaceId: String)
    @Query("UPDATE chat_group SET unread_count = (SELECT unread_count FROM chat_group WHERE group_id = :groupId)-1 WHERE group_id = :groupId AND unread_count>0")
    fun decreaseUnreadGroupCount(groupId: String)

    @Query("UPDATE chat_group SET respond_later_count = (SELECT respond_later_count FROM chat_group WHERE group_id = :groupId AND workspace_id = :workspaceId )+1 WHERE group_id = :groupId AND workspace_id = :workspaceId")
    fun increaseWorksapceRespondLaterGroupCount(groupId: String,workspaceId: String)
    @Query("UPDATE chat_group SET respond_later_count = (SELECT respond_later_count FROM chat_group WHERE group_id = :groupId )+1 WHERE group_id = :groupId")
    fun increaseRespondLaterGroupCount(groupId: String)

    @Query("UPDATE chat_group SET respond_later_count = (SELECT respond_later_count FROM chat_group WHERE group_id = :groupId AND workspace_id = :workspaceId )-1 WHERE group_id = :groupId AND workspace_id = :workspaceId AND respond_later_count>0")
    fun decreaseWorkspaceRespondLaterGroupCount(groupId: String,workspaceId: String)
    @Query("UPDATE chat_group SET respond_later_count = (SELECT respond_later_count FROM chat_group WHERE group_id = :groupId )-1 WHERE group_id = :groupId AND respond_later_count>0")
    fun decreaseRespondLaterGroupCount(groupId: String)

    @Query("UPDATE chat_group SET read_receipt_count = (SELECT read_receipt_count FROM chat_group WHERE group_id = :groupId AND workspace_id = :workspaceId )+1 WHERE group_id = :groupId AND workspace_id = :workspaceId")
    fun increaseWorkspaceReadReceiptGroupCount(groupId: String,workspaceId: String)
    @Query("UPDATE chat_group SET read_receipt_count = (SELECT read_receipt_count FROM chat_group WHERE group_id = :groupId  )+1 WHERE group_id = :groupId")
    fun increaseReadReceiptGroupCount(groupId: String)

    @Query("UPDATE chat_group SET read_receipt_count = (SELECT read_receipt_count FROM chat_group WHERE group_id = :groupId AND workspace_id = :workspaceId )-1 WHERE group_id = :groupId AND workspace_id = :workspaceId AND read_receipt_count>0")
    fun decreaseWorkspaceReadReceiptGroupCount(groupId: String,workspaceId: String)
    @Query("UPDATE chat_group SET read_receipt_count = (SELECT read_receipt_count FROM chat_group WHERE group_id = :groupId  )-1 WHERE group_id = :groupId AND read_receipt_count>0")
    fun decreaseReadReceiptGroupCount(groupId: String)

    @Query("SELECT 0 as isSelected,1 as type,(SELECT count(user_id) FROM group_member WHERE group_id = mgm.group_id AND user_status = 1 AND workspace_id = mgm.workspace_id) AS user_count,g.is_favourite,g.is_muted,g.is_active AS status,CASE WHEN (m.is_group = 1) THEN 1 ELSE 0 end as entity_type,g.group_id AS entity_id,g.is_orange_group as orange,g.group_type,g.group_name AS entity_name,g.group_name AS search_name, g.group_avatar AS entity_avatar,m.created_at AS created_at FROM chat AS m INNER JOIN group_member as mgm on mgm.user_id = m.sender_id and mgm.group_id = m.receiver_id INNER JOIN user AS u ON m.sender_id = u.user_id INNER JOIN chat_group AS g ON m.receiver_id = g.group_id WHERE m.message_id IN (SELECT cm.message_id AS id FROM chat AS cm INNER JOIN (SELECT m.id AS id, max (created_at) AS created_at FROM chat AS m WHERE m.receiver_id in (SELECT group_id from group_member where user_id = :workspaceUserId and user_status = 1 group by group_id) and is_group = 1 and g.is_active = 1  and m.status IN (1,'1',2,'2',3,'3') GROUP BY receiver_id ORDER BY created_at DESC) AS cmi ON cmi.id = cm.id) and g.workspace_id = :workspaceId GROUP BY g.group_id having (user_count > 1) OR (mgm.user_id = :workspaceUserId AND mgm.workspace_id = :workspaceId AND mgm.user_status != 1) ORDER BY created_at DESC")
    fun fetchWorkspaceForkOutRecentGroupList(workspaceId: String,workspaceUserId: String):List<ForkOutModel>
    @Query("SELECT 0 as isSelected,1 as type,(SELECT count(user_id) FROM group_member WHERE group_id = mgm.group_id AND user_status = 1 AND workspace_id = mgm.workspace_id) AS user_count,g.is_favourite,g.is_muted,g.is_active AS status,CASE WHEN (m.is_group = 1) THEN 1 ELSE 0 end as entity_type,g.group_id AS entity_id,g.is_orange_group as orange,g.group_type,g.group_name AS entity_name,g.group_name AS search_name, g.group_avatar AS entity_avatar,m.created_at AS created_at FROM chat AS m INNER JOIN group_member as mgm on mgm.user_id = m.sender_id and mgm.group_id = m.receiver_id INNER JOIN user AS u ON m.sender_id = u.user_id INNER JOIN chat_group AS g ON m.receiver_id = g.group_id WHERE m.message_id IN (SELECT cm.message_id AS id FROM chat AS cm INNER JOIN (SELECT m.id AS id, max (created_at) AS created_at FROM chat AS m WHERE m.receiver_id in (SELECT group_id from group_member where user_id = :loginUserId and user_status = 1 group by group_id) and is_group = 1 and g.is_active = 1  and m.status IN (1,'1',2,'2',3,'3') GROUP BY receiver_id ORDER BY created_at DESC) AS cmi ON cmi.id = cm.id) GROUP BY g.group_id having (user_count > 1) OR (mgm.user_id = :loginUserId AND mgm.user_status != 1) ORDER BY created_at DESC")
    fun fetchForkOutRecentGroupList(loginUserId:String):List<ForkOutModel>

    @Query("SELECT 1 as entity_type,0 as isSelected,2 as type,g.group_type,g.group_id as entity_id,g.is_muted,is_orange_group as orange,g.group_name AS entity_name,g.group_name AS search_name,g.is_favourite,g.is_active AS status,(SELECT count(user_id) FROM group_member WHERE group_id = g.group_id AND user_status = 1 AND workspace_id = g.workspace_id) AS user_count, g.group_avatar AS entity_avatar,g.created_at  FROM chat_group as g inner join group_member as gm on g.group_id = gm.group_id and g.workspace_id = gm.workspace_id where gm.user_status = 1 and g.is_active = 1 and gm.user_id = :workspaceUserId and g.workspace_id = :workspaceId and g.is_favourite IN (1,'1') group by g.group_id")
    fun fetchWorkspaceForkOutFavouriteList(workspaceId: String,workspaceUserId: String):List<ForkOutModel>
    @Query("SELECT 1 as entity_type,0 as isSelected,2 as type,g.group_type,g.group_id as entity_id,g.is_muted,is_orange_group as orange,g.group_name AS entity_name,g.group_name AS search_name,g.is_favourite,g.is_active AS status,(SELECT count(user_id) FROM group_member WHERE group_id = g.group_id AND user_status = 1 AND workspace_id = g.workspace_id) AS user_count, g.group_avatar AS entity_avatar,g.created_at  FROM chat_group as g inner join group_member as gm on g.group_id = gm.group_id and g.workspace_id = gm.workspace_id where gm.user_status = 1 and g.is_active = 1 and gm.user_id = :loginUserId and g.is_favourite IN (1,'1') group by g.group_id")
    fun fetchForkOutFavouriteList(loginUserId: String):List<ForkOutModel>

    @Query("SELECT 0 as isSelected,4 as type,g.is_active AS status,(SELECT count(user_id) FROM group_member WHERE group_id = g.group_id AND user_status = 1 AND workspace_id = g.workspace_id) AS user_count,g.group_id AS entity_id,g.group_name AS entity_name,g.group_name AS search_name,1 as entity_type,g.group_type,g.is_muted,g.is_favourite, g.group_avatar AS entity_avatar,g.is_orange_group AS orange,g.created_at FROM chat_group AS g INNER JOIN group_member AS gm ON g.group_id = gm.group_id AND g.workspace_id = gm.workspace_id WHERE gm.user_status = 1 and g.is_active = 1 AND gm.user_id = :workspaceUserId AND g.workspace_id = :workspaceId GROUP BY g.group_id HAVING (user_count > 1) OR (gm.user_id = :workspaceUserId AND gm.workspace_id = :workspaceId AND gm.user_status != 1)")
    fun fetchWorkspaceForkOutGroupList(workspaceId: String,workspaceUserId: String):List<ForkOutModel>
    @Query("SELECT 0 as isSelected,4 as type,g.is_active AS status,(SELECT count(user_id) FROM group_member WHERE group_id = g.group_id AND user_status = 1 AND workspace_id = g.workspace_id) AS user_count,g.group_id AS entity_id,g.group_name AS entity_name,g.group_name AS search_name,1 as entity_type,g.group_type,g.is_muted,g.is_favourite, g.group_avatar AS entity_avatar,g.is_orange_group AS orange,g.created_at FROM chat_group AS g INNER JOIN group_member AS gm ON g.group_id = gm.group_id AND g.workspace_id = gm.workspace_id WHERE gm.user_status = 1 and g.is_active = 1 AND gm.user_id = :loginUserId GROUP BY g.group_id HAVING (user_count > 1) OR (gm.user_id = :loginUserId AND gm.user_status != 1)")
    fun fetchForkOutGroupList(loginUserId: String):List<ForkOutModel>

    @Query("SELECT is_muted FROM chat_group WHERE group_id =:groupId AND workspace_id =:workspaceId ")
    fun fetchWorkspaceMuteStatus(groupId:String,workspaceId: String):Int
    @Query("SELECT is_muted FROM chat_group WHERE group_id =:groupId")
    fun fetchMuteStatus(groupId:String):Int

    @Query("DELETE FROM chat_group WHERE workspace_id =:workspaceId")
    fun removeWorkspaceGroup(workspaceId: String)
    @Query("DELETE FROM chat_group")
    fun removeGroup()

    @Query("SELECT g.ID,g.group_id as receiver,g.group_id as receiver_id,0 as message_id,0 as sender_id,0 as is_read,0 as is_delivered,0 as is_sync,0 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respondlater_count,g.is_muted as muted,g.is_favourite as favourite, 1 as entity_type,0 as message_status,g.is_orange_group as orange,1 as userstatus,g.group_type as group_type,'' as message,g.created_at as created_at,g.group_name as entity_name,g.group_name as searchString,g.group_avatar as entity_avatar,g.group_name as sender_name,g.workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM chat_group AS g INNER JOIN group_member as gm ON gm.group_id = g.group_id AND gm.workspace_id = g.workspace_id AND g.is_active=1 AND g.group_id NOT IN (:groupIds) AND gm.user_status = 1 AND gm.user_id =:userId WHERE g.workspace_id = :workspaceId GROUP BY g.group_id")
    fun fetchWorkspaceOtherGroupRecent(groupIds:List<String>,userId:String,workspaceId: String): LiveData<List<RecentList>>
    @Query("SELECT g.ID,g.group_id as receiver,g.group_id as receiver_id,0 as message_id,0 as sender_id,0 as is_read,0 as is_delivered,0 as is_sync,0 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respondlater_count,g.is_muted as muted,g.is_favourite as favourite, 1 as entity_type,0 as message_status,g.is_orange_group as orange,1 as userstatus,g.group_type as group_type,'' as message,g.created_at as created_at,g.group_name as entity_name,g.group_name as searchString,g.group_avatar as entity_avatar,g.group_name as sender_name,g.workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM chat_group AS g INNER JOIN group_member as gm ON gm.group_id = g.group_id AND gm.workspace_id = g.workspace_id AND g.is_active=1 AND g.group_id NOT IN (:groupIds) AND gm.user_status = 1 AND gm.user_id =:userId  GROUP BY g.group_id")
    fun fetchOtherGroupRecent(groupIds:List<String>,userId:String): LiveData<List<RecentList>>

    @Query("SELECT group_id FROM group_member WHERE user_id = :workspaceUserId AND user_status = 1 AND workspace_id = :workspaceId AND group_id in (select group_id from group_member where user_id = :entityId AND user_status = 1 AND workspace_id = :workspaceId group by group_id) group by group_id")
    fun fetchWorkspaceCommonGroupIds(workspaceId: String,workspaceUserId: String,entityId: String) :List<Long>
    @Query("SELECT group_id FROM group_member WHERE user_id = :loginUserId AND user_status = 1  AND group_id in (select group_id from group_member where user_id = :entityId AND user_status = 1 group by group_id) group by group_id")
    fun fetchCommonGroupIds(loginUserId: String,entityId: String) :List<Long>

    @Query("SELECT mg.group_id,mg.group_name,mg.group_avatar,mg.group_type,mg.is_orange_group,count(mgm.user_id) as members_count FROM chat_group as mg inner join group_member as mgm on mgm.group_id = mg.group_id AND mgm.workspace_id = mg.workspace_id AND mgm.user_status = 1 WHERE mg.is_active = 1 AND mg.group_id in (:groupIds) group by mg.group_id")
    fun fetchCommonGroup(groupIds: List<Long>):List<CommonGroups>

    @RawQuery(observedEntities = [MessengerGroup::class, GroupMembers::class])
    fun fetchCommonGroup(query: SupportSQLiteQuery):List<CommonGroups>

    @Query("SELECT unread_count FROM chat_group WHERE group_id=:groupId AND workspace_id=:workspaceId")
    fun fetchWorkspaceUnreadCount(groupId: String,workspaceId: String):Int
    @Query("SELECT unread_count FROM chat_group WHERE group_id=:groupId")
    fun fetchUnreadCount(groupId: String):Int

    @Query("SELECT respond_later_count FROM chat_group WHERE group_id=:groupId AND workspace_id=:workspaceId")
    fun fetchWorkspaceRespondLaterCount(groupId: String,workspaceId: String):Int
    @Query("SELECT respond_later_count FROM chat_group WHERE group_id=:groupId")
    fun fetchRespondLaterCount(groupId: String):Int

    @Query("SELECT read_receipt_count FROM chat_group WHERE group_id=:groupId AND workspace_id=:workspaceId")
    fun fetchWorkspaceReadReceiptCount(groupId: String,workspaceId: String):Int
    @Query("SELECT read_receipt_count FROM chat_group WHERE group_id=:groupId")
    fun fetchReadReceiptCount(groupId: String):Int*/

    @Query("SELECT group_id FROM chat_group WHERE group_id =:groupId")
    fun checkGroupExists(groupId:Long):Int
    @Query("SELECT IFNULL(u.uid,'') as uid, IFNULL(u.user_id,0) as user_id FROM chat_group as g LEFT JOIN user as u ON u.user_id = g.created_by WHERE group_id=:groupId")
    fun fetchGroupAuthor(groupId:String): GroupModel
    @Query("DELETE FROM chat_group WHERE created_by IN (:userIds)")
    fun deleteAdminGroup(userIds:List<String>)
    @Query("SELECT group_id FROM chat_group WHERE created_by IN (:userIds)")
    fun fetchAdminGroupIds(userIds: List<String>):List<Long>
    @Query("SELECT group_avatar FROM chat_group WHERE group_id =:groupId")
    fun fetchGroupAvatar(groupId: String):String
    @Query("SELECT message_id FROM chat WHERE receiver_id=:groupId AND sender_id=:senderId AND is_group=1 ORDER BY message_id DESC limit 1")
    fun fetchEntityLastMessageId(senderId:String,groupId: String) : Long
    @Query("SELECT group_name FROM chat_group WHERE group_id=:groupId")
    fun fetchGroupName(groupId: String):String
    @Query("SELECT is_archived FROM chat_group WHERE group_id=:groupId AND is_archived=1")
    fun isArchivedGroup(groupId: String):Int
    @Query("UPDATE chat_group SET unread_messages_count = (SELECT count(*) as unread_messages_count FROM chat WHERE (sender_id!=:senderId AND receiver_id=:groupId AND is_group=1 AND is_read=0)) WHERE group_id=:groupId")
    fun updateUnreadCount(senderId: String,groupId: String)
    //@Query("UPDATE chat_group SET last_message_local_id=:localId,last_message_id=:messageId,last_message=:message,last_message_time=:createdAt,last_message_type=:messageType,last_message_status= (SELECT IFNULL(CASE WHEN last_message_status>:messageStatus THEN last_message_status ELSE :messageStatus END, :messageStatus) as last_message_status FROM chat_group WHERE group_id=:entityId),last_message_by_me=:isMine,last_message_sender_id=:memberId,last_message_sender_name=:memberName,unread_messages_count=(SELECT count(*) as unread_message_count FROM chat WHERE (sender_id=:userId AND receiver_id=:entityId AND is_group=1 AND is_read=0)) WHERE group_id=:entityId")
    @Query("UPDATE chat_group SET last_message_local_id=:localId,last_message_id=:messageId,last_message=:message,last_message_time=:createdAt,last_message_type=:messageType,last_message_status=:messageStatus,last_message_by_me=:isMine,last_message_sender_id=:memberId,last_message_sender_name=:memberName,unread_messages_count=(SELECT count(*) as unread_message_count FROM chat WHERE (sender_id!=:userId AND receiver_id=:entityId AND is_group=1 AND is_read=0)) WHERE group_id=:entityId")
    fun updateRecentGroupMessage(localId:Long,messageId:Long,message:String,createdAt:String,messageType:Int,messageStatus:Int,entityId:String,memberId:String,isMine:Int,memberName:String,userId:String)
    @Query("SELECT * FROM chat WHERE receiver_id=:groupId AND is_group=1 AND status!=0 ORDER BY created_at DESC,message_id DESC LIMIT 1")
    fun fetchDataForRecentRecord(groupId: String):Messenger

    @Query("DELETE FROM chat_group WHERE group_id= :groupId ")
    fun deleteGroup(groupId: String)
    @Query("UPDATE chat_group SET group_name =:groupName,updated_at =:updatedAt WHERE  group_id= :groupId")
    fun updateGroupName(groupName:String,groupId: String,updatedAt: String)
    @Query("UPDATE chat_group SET group_avatar =:groupAvatar,updated_at =:updatedAt WHERE  group_id= :groupId")
    fun updateGroupAvatar(groupAvatar:String,groupId: String,updatedAt: String)

    @Query("UPDATE chat_group SET is_archived= 1 WHERE group_id IN (:groupId)")
    fun updateGroupArchived(groupId: List<Long>)
    @Query("UPDATE chat_group SET is_archived= 0 WHERE group_id IN (:groupId)")
    fun updateGroupUnArchived(groupId: List<Long>)
    @Query("UPDATE chat_group SET last_message_local_id=0,last_message_id=0,last_message='',last_message_time='',last_message_type=-1,last_message_status=-1,last_message_by_me=0,unread_messages_count=0 WHERE group_id=:groupId")
    fun updateGroupLastMessageData(groupId: String)
    @RawQuery(observedEntities = [MessengerGroup::class])
    fun updateGroupLastMessageStatus(query: SupportSQLiteQuery):Long
    @Query("SELECT group_id,group_name,group_avatar,group_description,created_by,created_at,conversation_reference_id,is_active,is_archived FROM chat_group WHERE group_id=:groupId")
    fun fetchGroupData(groupId: String):MessengerGroupModel

    @Query("SELECT * FROM chat_group WHERE group_id=:groupId")
    fun fetchingGroupData(groupId: String):MessengerGroup
    /*@Query("UPDATE chat_group SET last_message_status=:status,updated_at=:updatedAt WHERE user_id=:userId AND (last_message_id =:lastMessageId OR last_message_local_id=:lastMessageLocalId) AND last_message_status<:status")
        fun updateUserLastMessageStatus(status: Int,updatedAt: String,userId: String,lastMessageId: Long,lastMessageLocalId: Long)*/
}