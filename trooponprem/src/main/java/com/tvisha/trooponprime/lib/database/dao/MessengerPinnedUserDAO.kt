package com.tvisha.trooponprime.lib.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.tvisha.trooponprime.lib.database.MessengerPinnedUser

@Dao
internal interface MessengerPinnedUserDAO {
    @Insert
    fun insert(data: MessengerPinnedUser)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllData(group: List<MessengerPinnedUser>)
}