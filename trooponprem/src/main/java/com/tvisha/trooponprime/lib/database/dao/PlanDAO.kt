package com.tvisha.trooponprime.lib.database.dao

import androidx.room.*
import com.tvisha.trooponprime.lib.database.Plan

@Dao
internal interface PlanDAO {
    @Insert
    fun insert(data: Plan)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllData(group: List<Plan>)
    @Update
    fun update(data: Plan)

}