package com.tvisha.trooponprime.lib.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tvisha.trooponprime.lib.clientModels.BroadCastMessage
import com.tvisha.trooponprime.lib.database.BroadCast

@Dao
internal interface BroadCastDAO {
    @Insert
    fun insertBroadCast(broadCast: BroadCast)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBulkBroadCast(broadCastList: List<BroadCast>)
    @Update
    fun updateBroadCast(broadCast: BroadCast)
    @Query("DELETE FROM broadcast")
    fun deleteBroadCast()
    @Query("SELECT message_id,title,message,attachment,created_at,updated_at FROM broadcast ORDER BY message_id DESC LIMIT 1")
    fun fetchLiveRecentBroadCastMessage():LiveData<List<BroadCastMessage>>
    @Query("SELECT message_id,title,message,attachment,created_at,updated_at FROM broadcast ORDER BY message_id DESC LIMIT 1")
    fun fetchRecentBroadCastMessage():BroadCastMessage
    @Query("SELECT message_id,title,message,attachment,created_at,updated_at FROM broadcast")
    fun fetchAllBroadCastMessages():LiveData<List<BroadCastMessage>>
    @Query("SELECT IFNULL(MAX(created_at),'') as created_at FROM broadcast")
    fun fetchLastRecordTime():String
    @Query("SELECT message_id FROM broadcast WHERE message_id IN (:messageIds)")
    fun matchMessageIds(messageIds:List<Long>):List<Long>

}