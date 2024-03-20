package com.tvisha.trooponprime.lib.database.dao

import androidx.room.*

@Dao
internal interface WorkspaceDAO {
    /*@Insert
    fun insert(group: Workspace)
    @Update
    fun update(group: Workspace)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroupData(group: List<Workspace>)

    @Query("SELECT workspace_id from workspace GROUP BY workspace_id")
    fun fetchWorkspaceIds():List<String>
    @Query("SELECT * FROM workspace GROUP BY workspace_id ORDER BY workspace_name ASC,is_orange_member DESC")
    fun fetchWorkspaceData():List<Workspace>
    @Query("SELECT is_orange_member FROM workspace WHERE workspace_id =:workspaceId")
    fun isOrangeMember(workspaceId:String):Int
    @Query("SELECT workspace_id FROM workspace WHERE workspace_id=:workspaceId")
    fun checkWorkspaceId(workspaceId: String):String
    @Query("DELETE FROM workspace WHERE workspace_id = :workspaceId")
    fun removeWorkspace(workspaceId: String)
    @Query("SELECT created_at FROM workspace WHERE workspace_id=:workspaceId")
    fun fetchCreatedAt(workspaceId: String) : String
    @Query("SELECT * FROM workspace WHERE workspace_id=:workspaceId")
    fun fetchWorkspaceData(workspaceId: String):Workspace
    @Query("DELETE FROM workspace")
    fun deleteData()*/
}
