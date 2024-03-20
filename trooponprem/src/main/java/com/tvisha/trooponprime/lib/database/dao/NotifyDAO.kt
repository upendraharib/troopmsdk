package com.tvisha.trooponprime.lib.database.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.tvisha.trooponprime.lib.database.Notify
import com.tvisha.trooponprime.lib.database.model.NotifyDataModel

@Dao
internal interface NotifyDAO {
    @Insert
    fun insert(data: Notify)
    @Insert
    fun insertAll(data: List<Notify>)
    @Update
    fun update(data: Notify)

    @Query("SELECT workspace_id,max(updated_at) as updated_at FROM notify GROUP BY workspace_id")
    fun fetchNotifyUpdatedData():List<NotifyDataModel>

    @Query("SELECT * FROM notify WHERE notify_id =:notifyId AND workspace_id = :workspaceId")
    fun fetchWorkspaceNotify(notifyId:Long,workspaceId:String):Notify
    @Query("SELECT * FROM notify WHERE notify_id =:notifyId")
    fun fetchNotify(notifyId:Long):Notify

    @RawQuery(observedEntities = [Notify::class])
    fun fetchNotifies(query: SupportSQLiteQuery): PagingSource<Int, Notify>

    @Query("UPDATE notify SET status = 2,message=:message WHERE workspace_id =:workspaceId AND notify_id IN (:notifyId)")
    fun updateWorkspaceMessageStatusToRecall(notifyId: List<Long>,workspaceId: String,message:String)
    @Query("UPDATE notify SET status = 2,message=:message,updated_at=:updatedAt WHERE notify_id IN (:notifyId)")
    fun updateMessageStatusToRecall(notifyId: List<Long>,message:String,updatedAt:String)

    @Query("SELECT notify_id FROM notify WHERE workspace_id=:workspaceId AND sender_id!=:workspaceUserId AND is_read = 0 ")
    fun fetchWorkspaceUnreadNotify(workspaceId: String,workspaceUserId:String):List<Long>
    @Query("SELECT notify_id FROM notify WHERE sender_id!=:loginUserId AND is_read = 0 ")
    fun fetchUnreadNotify(loginUserId:String):List<Long>

    @Query("UPDATE notify SET is_read = 1 WHERE workspace_id=:workspaceId AND sender_id!=:workspaceUserId")
    fun updateWorkspaceUnreadMessages(workspaceId: String,workspaceUserId: String)
    @Query("UPDATE notify SET is_read = 1 WHERE sender_id!=:loginUserId")
    fun updateUnreadMessages(loginUserId: String)

    @Query("UPDATE notify SET is_read = 1,updated_at=:updatedAt WHERE workspace_id=:workspaceId AND notify_id IN (:notifyId)")
    fun updateWorkspaceNotifyReadStatus(workspaceId: String,notifyId: List<Long>,updatedAt: String)
    @Query("UPDATE notify SET is_read = 1,updated_at=:updatedAt WHERE  notify_id IN (:notifyId)")
    fun updateNotifyReadStatus(notifyId: List<Long>,updatedAt: String)

    @Query("SELECT n1.* FROM notify as n1 INNER JOIN (SELECT MAX(notify_id) as notify_id FROM notify WHERE workspace_id=:workspaceId AND sender_id=:workspaceUserId AND status=1 GROUP BY message) as n2 ON n1.notify_id = n2.notify_id WHERE workspace_id =:workspaceId AND sender_id=:workspaceUserId AND status = 1 GROUP BY message ORDER BY n1.notify_id DESC  limit 5")
    fun fetchWorkspaceSelfNotifyMessages(workspaceId: String,workspaceUserId: String):List<Notify>
    @Query("SELECT n1.* FROM notify as n1 INNER JOIN (SELECT MAX(notify_id) as notify_id FROM notify WHERE sender_id=:loginUserId AND status=1 GROUP BY message) as n2 ON n1.notify_id = n2.notify_id WHERE  sender_id=:loginUserId AND status = 1 GROUP BY message ORDER BY n1.notify_id DESC  limit 5")
    fun fetchSelfNotifyMessages(loginUserId: String):List<Notify>

    @Query("SELECT * FROM notify WHERE workspace_id=:workspaceId AND sender_id!=:workspaceUserId AND status=1 AND is_read = 0 GROUP BY notify_id ORDER BY priority DESC,created_at DESC limit 2")
    fun fetchWorkspaceRecentNotifies(workspaceId: String,workspaceUserId: String): LiveData<List<Notify>>
    @Query("SELECT * FROM notify WHERE  sender_id!=:loginUserId AND status=1 AND is_read = 0 GROUP BY notify_id ORDER BY priority DESC,created_at DESC limit 2")
    fun fetchRecentNotifies(loginUserId: String): LiveData<List<Notify>>

    @Query("UPDATE notify SET sender_avatar=:avatar WHERE sender_id=:senderId AND workspace_id =:workspaceId")
    fun updateWorkspaceSenderAvatar(avatar:String,senderId:String,workspaceId: String)
    @Query("UPDATE notify SET sender_avatar=:avatar WHERE sender_id=:senderId")
    fun updateSenderAvatar(avatar:String,senderId:String)

    @Query("SELECT count(notify_id) FROM notify WHERE workspace_id =:workspaceId")
    fun countWorkspaceOfNotify(workspaceId: String):Int
    @Query("SELECT count(notify_id) FROM notify ")
    fun countOfNotify():Int

    @Query("DELETE FROM notify WHERE workspace_id=:workspaceId")
    fun removeWorkspaceNotify(workspaceId: String)
    @Query("DELETE FROM notify")
    fun removeNotify()

    @Query("SELECT notify_id FROM notify WHERE workspace_id=:workspaceId AND is_read=0 and priority=2 and sender_id!=:workspaceUserId")
    fun fetchWorkspaceActiveSosNotifyRead(workspaceId: String,workspaceUserId: String):Long
    @Query("SELECT notify_id FROM notify WHERE is_read=0 and priority=2 and sender_id!=:loginUserId")
    fun fetchActiveSosNotifyRead(loginUserId: String):Long

}
