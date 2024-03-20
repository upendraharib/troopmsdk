package com.tvisha.trooponprime.lib.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.tvisha.trooponprime.lib.database.Story

@Dao
internal interface StoryDAO {
    @Insert
    fun insertStory(story: Story)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBulkStories(storyList:List<Story>)
    @Update
    fun updateStory(story: Story)

    @Query("DELETE FROM story")
    fun deleteStories()

    @Query("DELETE FROM story WHERE user_id IN (:userIds)")
    fun deleteUserStories(userIds:List<String>)

    @Query("SELECT id FROM story WHERE created_at< date('now','-1 days')")
    fun fetchExpiredStories():List<Long>

    @Query("SELECT id FROM story WHERE user_id=:userId AND ID IN (:ids)")
    fun verifyMyStories(userId: String,ids:List<Long>):List<Long>

    @Query("SELECT * FROM story WHERE ID=:id")
    fun fetchViewStoryData(id:Long):Story

    @Query("DELETE FROM story WHERE created_at< date('now','-1 days')")
    fun deleteExpiredStories()

    //@Query("SELECT id,user_id,type,data,shared_to,status as seen,created_at,updated_at,'' as profile_pic,'' as user_name FROM story WHERE user_id=:userId AND created_at>'' AND status!=0")
    @Query("SELECT s.id,s.user_id,s.type,s.data,s.shared_to,s.status as seen,s.created_at,s.updated_at,u.name as user_name,u.profile_pic FROM story as s INNER JOIN user as u ON u.user_id=s.user_id WHERE s.user_id=:userId   AND s.created_at > date('now','-1 days') AND status!=0")
    fun fetchMyStories(userId: String):LiveData<List<com.tvisha.trooponprime.lib.clientModels.StoryModel>>

    //@Query("SELECT s.id as id,s.user_id,u.name as user_name,u.profile_pic,s.type,s.data,CASE WHEN s.status=2 THEN 1 ELSE 0 END as seen,s.created_at,s.updated_at,s.shared_to FROM story as s LEFT JOIN user as u ON s.user_id=u.user_id INNER JOIN contact as uc ON u.user_id=uc.uid WHERE s.user_id!=:userId AND s.created_at>'' AND status!=0 GROUP BY s.ID HAVING u.name is not null ORDER BY s.created_at ASC")
    @Query("SELECT s.id as id,s.user_id,u.name as user_name,u.profile_pic,s.type,s.data,CASE WHEN s.status=2 THEN 1 ELSE 0 END as seen,s.created_at,s.updated_at,s.shared_to FROM story as s INNER JOIN user as u ON s.user_id=u.user_id INNER JOIN contact as uc ON u.uid=uc.uid WHERE s.user_id!=:userId AND s.created_at > date('now','-1 days')  GROUP BY s.ID HAVING u.name is not null ORDER BY s.created_at ASC")
    fun fetchOtherStories(userId: String):LiveData<List<com.tvisha.trooponprime.lib.clientModels.StoryModel>>

    @Query("SELECT s.id as id,s.user_id,u.name as user_name,u.profile_pic,s.type,s.data,CASE WHEN s.status=2 THEN 1 ELSE 0 END as seen,s.created_at,s.updated_at,s.shared_to FROM story as s LEFT JOIN user as u ON s.user_id=u.user_id INNER JOIN contact as uc ON u.uid=uc.uid WHERE s.user_id=:userId AND s.created_at > date('now','-1 days')  GROUP BY s.ID HAVING u.name is not null ORDER BY s.created_at ASC")
    fun fetchStoriesByUserId(userId: String):LiveData<List<com.tvisha.trooponprime.lib.clientModels.StoryModel>>

    @Query("SELECT user_id FROM story WHERE ID=:id AND status=1")
    fun fetchUserId(id:Long):Long

    @Query("SELECT * FROM story WHERE ID=:id")
    fun fetchStoryById(id: Long):Story

    @Query("UPDATE story SET status=:status,updated_at=:updatedAt WHERE ID=:id")
    fun updateStoryStatus(status:Int,id:Long,updatedAt:String)

}
