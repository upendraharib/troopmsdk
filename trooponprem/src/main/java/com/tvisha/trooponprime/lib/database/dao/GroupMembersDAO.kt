package com.tvisha.trooponprime.lib.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.tvisha.trooponprime.lib.database.GroupMembers
import com.tvisha.trooponprime.lib.database.model.GroupMembersModel


@Dao
internal interface GroupMembersDAO {
    @Insert
    fun insert(data: GroupMembers)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllData(group: List<GroupMembers>)

    @Update
    fun update(members: GroupMembers)

    /*@Query("DELETE FROM group_member WHERE group_id = :groupId AND workspace_id = :workspaceId")
    fun deleteWorkspaceGroupMembers(groupId: String,workspaceId: String)
    @Query("DELETE FROM group_member WHERE group_id = :groupId")
    fun deleteGroupMembers(groupId: String)

    @Query("UPDATE group_member SET user_status = 0,user_role=2,updated_at =:updatedAt WHERE user_id = :userId AND group_id=:groupId AND workspace_id=:workspaceId")
    fun updateWorkspaceGroupMemberStatus(groupId:String,userId:String,workspaceId:String,updatedAt:String)


    @Query("SELECT user_id FROM group_member WHERE group_id = :groupId AND user_id = :userId AND workspace_id =:workspaceId")
    fun checkWorkspaceGroupUserExists(groupId: String,userId: String,workspaceId: String) : Long
    @Query("SELECT user_id FROM group_member WHERE group_id = :groupId AND user_id = :userId")
    fun checkGroupUserExists(groupId: String,userId: String) : Long

    @Query("UPDATE group_member SET user_role=:newRole,updated_at=:updatedAt WHERE group_id=:groupId AND user_id=:userId AND workspace_id=:workspaceId")
    fun changeWorkspaceUserRole(groupId: String,userId: String,newRole:Int,workspaceId: String,updatedAt: String)
    @Query("UPDATE group_member SET user_role=:newRole,updated_at=:updatedAt WHERE group_id=:groupId AND user_id=:userId")
    fun changeUserRole(groupId: String,userId: String,newRole:Int,updatedAt: String)

    @Query("SELECT * FROM group_member WHERE user_id = :userId AND workspace_id=:workspaceId AND group_id=:groupId")
    fun fetchWorkspaceGroupMemberData(userId: String,workspaceId: String,groupId: String):GroupMembers
    @Query("SELECT * FROM group_member WHERE user_id = :userId AND  group_id=:groupId")
    fun fetchGroupMemberData(userId: String,groupId: String):GroupMembers

    @Query("SELECT count(user_id) as count FROM group_member WHERE workspace_id =:workspaceId AND group_id=:groupId AND user_status=1 GROUP BY user_id ")
    fun fetchWorkspaceGroupMembersCount(workspaceId: String,groupId: String):List<Int>
    @Query("SELECT count(user_id) as count FROM group_member WHERE  group_id=:groupId AND user_status=1 GROUP BY user_id ")
    fun fetchGroupMembersCount(groupId: String):List<Int>

    @Query("SELECT user_status FROM group_member WHERE user_id=:userId AND workspace_id = :workspaceId AND group_id=:groupId")
    fun fetchWorkspaceGroupMemberStatus(userId: String,workspaceId: String,groupId: String):Int
    @Query("SELECT user_status FROM group_member WHERE user_id=:userId  AND group_id=:groupId")
    fun fetchGroupMemberStatus(userId: String,groupId: String):Int

    @Query("SELECT user_role FROM group_member WHERE user_id=:userId AND workspace_id = :workspaceId AND group_id=:groupId")
    fun fetchWorkspaceGroupMemberRole(userId: String,workspaceId: String,groupId: String):Int
    @Query("SELECT user_role FROM group_member WHERE user_id=:userId AND group_id=:groupId")
    fun fetchGroupMemberRole(userId: String,groupId: String):Int

    @Query("SELECT mgm.*,u.name,u.user_avatar,u.is_orange_member as orange_member,0 as isSelected FROM group_member as mgm INNER JOIN user as u ON u.user_id = mgm.user_id and u.workspace_id = mgm.workspace_id WHERE group_id = :groupId and mgm.user_status = 1 and mgm.workspace_id = :workspaceId  group by mgm.user_id")
    fun fetchWorkspaceGroupMembers(workspaceId: String,groupId: String): LiveData<List<GroupMembersModel>>
    @Query("SELECT mgm.*,u.name,u.user_avatar,u.is_orange_member as orange_member,0 as isSelected FROM group_member as mgm INNER JOIN user as u ON u.user_id = mgm.user_id  WHERE group_id = :groupId and mgm.user_status = 1  group by mgm.user_id")
    fun fetchGroupMembers(groupId: String): LiveData<List<GroupMembersModel>>

    @Query("SELECT mgm.*,u.name,u.user_avatar,u.is_orange_member as orange_member,0 as isSelected FROM group_member as mgm INNER JOIN user as u ON u.user_id = mgm.user_id and u.workspace_id = mgm.workspace_id WHERE group_id = :groupId and mgm.user_status = 1 and mgm.workspace_id = :workspaceId  group by mgm.user_id")
    fun fetchWorkspaceGroupMembersList(workspaceId: String,groupId: String):List<GroupMembersModel>
    @Query("SELECT mgm.*,u.name,u.user_avatar,u.is_orange_member as orange_member,0 as isSelected FROM group_member as mgm INNER JOIN user as u ON u.user_id = mgm.user_id  WHERE group_id = :groupId and mgm.user_status = 1  group by mgm.user_id")
    fun fetchGroupMembersList(groupId: String):List<GroupMembersModel>

    @Query("SELECT mgm.*,u.name,u.user_avatar,u.is_orange_member as orange_member,0 as isSelected FROM group_member as mgm INNER JOIN user as u ON u.user_id = mgm.user_id and u.workspace_id = mgm.workspace_id WHERE group_id = :groupId  and mgm.workspace_id = :workspaceId  group by mgm.user_id")
    fun fetchWorkspaceGroupMembersSearch(workspaceId: String,groupId: String):List<GroupMembersModel>
    @Query("SELECT mgm.*,u.name,u.user_avatar,u.is_orange_member as orange_member,0 as isSelected FROM group_member as mgm INNER JOIN user as u ON u.user_id = mgm.user_id and u.workspace_id = mgm.workspace_id WHERE group_id = :groupId  group by mgm.user_id")
    fun fetchGroupMembersSearch(groupId: String):List<GroupMembersModel>

    @Query("DELETE FROM group_member WHERE workspace_id =:workspaceId")
    fun removeWorkspaceGroupMembers(workspaceId: String)
    @Query("DELETE FROM group_member")
    fun removeGroupMembers()

    @Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted, 0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,u.name,u.email,u.user_avatar as userpic,u.is_orange_member as isOrangeMember,u.user_id FROM user as u INNER JOIN group_member as mgm ON u.user_id = mgm.user_id and u.workspace_id = mgm.workspace_id WHERE group_id = :groupId and mgm.user_status = 1 and mgm.workspace_id = :workspaceId AND u.user_id!=:workspaceUserId AND u.user_status=1 AND u.workspace_id = :workspaceId GROUP BY mgm.user_id HAVING (((u.user_status = 1) AND ((isOrangeMember IN (1) AND u.user_id IN (:orange)) OR (isOrangeMember IN (0) AND u.user_id IN (:users))))) ORDER BY LOWER(name) ASC " )
    fun fetchWorkspaceParticipantsWithUserAndOrange(workspaceId: String, workspaceUserId: String, users: List<String>, orange: List<String>,groupId: String):List<ParticipantUsers>
    @Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted, 0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,u.name,u.email,u.user_avatar as userpic,u.is_orange_member as isOrangeMember,u.user_id FROM user as u INNER JOIN group_member as mgm ON u.user_id = mgm.user_id  WHERE group_id = :groupId and mgm.user_status = 1 AND u.user_id!=:loginUserId AND u.user_status=1  GROUP BY mgm.user_id HAVING (((u.user_status = 1) AND ((isOrangeMember IN (1) AND u.user_id IN (:orange)) OR (isOrangeMember IN (0) AND u.user_id IN (:users))))) ORDER BY LOWER(name) ASC " )
    fun fetchParticipantsWithUserAndOrange(loginUserId:String, users: List<String>, orange: List<String>,groupId: String):List<ParticipantUsers>

    @Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted,0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,u.name,u.email,u.user_avatar as userpic,u.is_orange_member as isOrangeMember,u.user_id FROM user as u INNER JOIN group_member as mgm ON u.user_id = mgm.user_id and u.workspace_id = mgm.workspace_id WHERE group_id = :groupId and mgm.user_status = 1 and mgm.workspace_id = :workspaceId AND u.user_id!=:workspaceUserId AND u.user_status=1 AND u.workspace_id = :workspaceId GROUP BY mgm.user_id HAVING (((u.user_status = 1) AND ((isOrangeMember IN (1)) OR (isOrangeMember IN (0) AND u.user_id IN (:users))))) ORDER BY LOWER(name) ASC " )
    fun fetchWorkspaceParticipantsWithUser(workspaceId: String, workspaceUserId: String, users: List<String>,groupId: String):List<ParticipantUsers>
    @Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted,0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,u.name,u.email,u.user_avatar as userpic,u.is_orange_member as isOrangeMember,u.user_id FROM user as u INNER JOIN group_member as mgm ON u.user_id = mgm.user_id WHERE group_id = :groupId and mgm.user_status = 1  AND u.user_id!=:loginUserId AND u.user_status=1 GROUP BY mgm.user_id HAVING (((u.user_status = 1) AND ((isOrangeMember IN (1)) OR (isOrangeMember IN (0) AND u.user_id IN (:users))))) ORDER BY LOWER(name) ASC " )
    fun fetchParticipantsWithUser(loginUserId: String, users: List<String>,groupId: String):List<ParticipantUsers>

    @Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted,0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,u.name,u.email,u.user_avatar as userpic,u.is_orange_member as isOrangeMember,u.user_id FROM user as u INNER JOIN group_member as mgm ON u.user_id = mgm.user_id and u.workspace_id = mgm.workspace_id WHERE group_id = :groupId and mgm.user_status = 1 and mgm.workspace_id = :workspaceId AND u.user_id!=:workspaceUserId AND u.user_status=1 AND u.workspace_id = :workspaceId GROUP BY mgm.user_id HAVING (((u.user_status = 1) AND ((isOrangeMember IN (1) AND u.user_id IN (:orange)) OR (isOrangeMember IN (0))))) ORDER BY LOWER(name) ASC " )
    fun fetchWorkspaceParticipantsWithOrange(workspaceId: String, workspaceUserId: String,  orange: List<String>,groupId: String):List<ParticipantUsers>
    @Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted,0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,u.name,u.email,u.user_avatar as userpic,u.is_orange_member as isOrangeMember,u.user_id FROM user as u INNER JOIN group_member as mgm ON u.user_id = mgm.user_id  WHERE group_id = :groupId and mgm.user_status = 1  AND u.user_id!=:loginUserId AND u.user_status=1  GROUP BY mgm.user_id HAVING (((u.user_status = 1) AND ((isOrangeMember IN (1) AND u.user_id IN (:orange)) OR (isOrangeMember IN (0))))) ORDER BY LOWER(name) ASC " )
    fun fetchParticipantsWithOrange(loginUserId: String,  orange: List<String>,groupId: String):List<ParticipantUsers>

    @Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted,0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,u.name,u.email,u.user_avatar as userpic,u.is_orange_member as isOrangeMember,u.user_id FROM user as u INNER JOIN group_member as mgm ON u.user_id = mgm.user_id and u.workspace_id = mgm.workspace_id WHERE group_id = :groupId and mgm.user_status = 1 and mgm.workspace_id = :workspaceId AND u.user_id!=:workspaceUserId AND u.user_status=1 AND u.workspace_id = :workspaceId GROUP BY mgm.user_id  ORDER BY LOWER(name) ASC " )
    fun fetchWorkspaceParticipants(workspaceId: String, workspaceUserId: String,groupId: String):List<ParticipantUsers>
    @Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted,0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,u.name,u.email,u.user_avatar as userpic,u.is_orange_member as isOrangeMember,u.user_id FROM user as u INNER JOIN group_member as mgm ON u.user_id = mgm.user_id  WHERE group_id = :groupId and mgm.user_status = 1  AND u.user_id!=:loginUserId AND u.user_status=1  GROUP BY mgm.user_id  ORDER BY LOWER(name) ASC " )
    fun fetchWorkspaceParticipants(loginUserId: String,groupId: String):List<ParticipantUsers>

    @Query("UPDATE group_member SET user_status = 2,user_role=2,updated_at =:updatedAt WHERE user_id = :userId AND group_id=:groupId AND workspace_id=:workspaceId")
    fun updateWorkspaceGroupMemberStatusToMember(groupId:String,userId:String,workspaceId:String,updatedAt:String)*/

    @Query("DELETE FROM group_member WHERE group_id IN (:groupIds)")
    fun deleteUsersFromGroup(groupIds:List<Long>)
    @Query("SELECT user_status FROM group_member WHERE group_id=:groupId AND user_id=:memberId")
    fun fetchGroupMemberUserStatus(groupId:String,memberId:String):Int
    @Query("DELETE FROM group_member")
    fun deleteGroupMembers()
    @Query("DELETE FROM group_member WHERE group_id=:groupId")
    fun deleteGroupMembers(groupId: String)
    @Query("SELECT group_id || '_ ' || user_id as group_user FROM group_member GROUP BY group_id,user_id HAVING group_user IN (:memberIds)")
    fun fetchBulkGroupMembers(memberIds:List<String>) :List<String>

    @Query("UPDATE group_member SET user_status = 2,user_role=2,updated_at =:updatedAt WHERE user_id = :userId AND group_id=:groupId")
    fun updateGroupMemberStatusToMember(groupId:String,userId:String,updatedAt:String)
    @Query("UPDATE group_member SET user_role=:newRole,updated_at=:updatedAt WHERE group_id=:groupId AND user_id=:userId")
    fun changeUserRole(groupId: String,userId: String,newRole:Int,updatedAt: String)
    @Query("UPDATE group_member SET user_status = 0,user_role=2,updated_at =:updatedAt WHERE user_id = :userId AND group_id=:groupId")
    fun updateGroupMemberStatus(groupId:String,userId:String,updatedAt:String)

    @Query("SELECT mgm.user_role as member_role,mgm.user_status as member_status,IFNULL(u.user_id,0) as member_id,IFNULL(u.profile_pic,'') as member_profile_pic,IFNULL(u.name,'') as member_name,u.uid as member_uid FROM group_member as mgm INNER JOIN user as u ON u.user_id=mgm.user_id WHERE mgm.group_id=:groupId AND mgm.user_status=1")
    fun fetchGroupMemberList(groupId: String):LiveData<List<GroupMembersModel>>

    @Query("SELECT mgm.user_role as member_role,mgm.user_status as member_status,IFNULL(u.user_id,0) as member_id,IFNULL(u.profile_pic,'') as member_profile_pic,IFNULL(u.name,'') as member_name,u.uid as member_uid FROM group_member as mgm INNER JOIN user as u ON u.user_id=mgm.user_id WHERE mgm.group_id=:groupId AND mgm.user_status=1 AND u.user_id=:userId AND mgm.user_id=:userId")
    fun fetchGroupMember(groupId: String,userId: String):LiveData<GroupMembersModel>

    @Query("SELECT user_id FROM group_member WHERE group_id = :groupId AND user_id = :userId")
    fun checkGroupUserExists(groupId: String,userId: String) : Long

    @Query("SELECT * FROM group_member WHERE group_id = :groupId AND user_id = :userId")
    fun fetchGroupUser(groupId: String,userId: String) : GroupMembers

    @Query("SELECT count(user_id) as count FROM group_member WHERE group_id=:groupId AND user_status=1 GROUP BY user_id ")
    fun fetchGroupActiveMemberCount(groupId: String):List<Int>

}