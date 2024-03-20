package com.tvisha.trooponprime.lib.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.tvisha.trooponprime.lib.database.GroupMemberStatus

@Dao
internal interface GroupMemberStatusDAO {
    @Insert
    fun insert(data: GroupMemberStatus)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllData(group: List<GroupMemberStatus>)

}