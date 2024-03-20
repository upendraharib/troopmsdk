package com.tvisha.trooponprime.lib.database.dao

import androidx.room.*
import com.tvisha.trooponprime.lib.database.Permission
import com.tvisha.trooponprime.lib.database.model.OragneUserPermission

@Dao
internal interface PermissionDAO {
    @Insert
    fun insert(data: Permission)
    @Update
    fun update(data: Permission)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllData(group: List<Permission>)

    /*@Query("SELECT user_id FROM permission WHERE user_id= :userId AND workspace_id =:workspaceId")
    fun isWorkspacePermissionUserExists(userId:String,workspaceId:String) : String
    @Query("SELECT user_id FROM permission WHERE user_id= :userId")
    fun isPermissionUserExists(userId:String) : String

    @Query("SELECT  * FROM permission WHERE  workspace_id=:workspaceId AND (user_id = :userId OR type = 1) GROUP BY user_id,workspace_id")
    fun fetchWorkspaceUserPermission(userId: String,workspaceId: String):List<Permission>
    @Query("SELECT  * FROM permission WHERE   (user_id = :userId OR type = 1) GROUP BY user_id,workspace_id")
    fun fetchUserPermission(userId: String):List<Permission>

    @Query("SELECT * FROM permission WHERE  workspace_id=:workspaceId AND user_id = :userId GROUP BY user_id,workspace_id")
    fun fetchWorkspaceUserPermissionData(userId: String,workspaceId: String):Permission
    @Query("SELECT * FROM permission WHERE   user_id = :userId GROUP BY user_id,workspace_id")
    fun fetchUserPermissionData(userId: String):Permission

    @Query("SELECT u.user_id, p.type, p.permission,(select permission from permission where user_id = 0 and workspace_id = u.workspace_id limit 1) as default_permission FROM user AS u LEFT JOIN permission AS p ON u.user_id = p.user_id AND u.workspace_id = p.workspace_id WHERE (u.is_orange_member = 1 OR p.user_id = 0) AND u.workspace_id = :workspaceId group by u.user_id order by p.user_id ASC")
    fun fetchWorkspaceMyOrangeMember(workspaceId: String) : List<OragneUserPermission>
    @Query("SELECT u.user_id, p.type, p.permission,(select permission from permission where user_id = 0  limit 1) as default_permission FROM user AS u LEFT JOIN permission AS p ON u.user_id = p.user_id WHERE (u.is_orange_member = 1 OR p.user_id = 0) AND  group by u.user_id order by p.user_id ASC")
    fun fetchMyOrangeMember() : List<OragneUserPermission>

    @Query("SELECT * FROM permission WHERE user_id = 0 AND workspace_id = :workspaceId AND type=1")
    fun fetchWorkspaceGlobalPermission(workspaceId: String):Permission
    @Query("SELECT * FROM permission WHERE user_id = 0  AND type=1")
    fun fetchGlobalPermission():Permission

    @Query("DELETE FROM permission WHERE workspace_id=:workspaceId")
    fun removeWorkspacePermission(workspaceId: String)*/
    @Query("DELETE FROM permission ")
    fun removeWorkspacePermission()

    @Query("DELETE FROM permission")
    fun deleteData()
}