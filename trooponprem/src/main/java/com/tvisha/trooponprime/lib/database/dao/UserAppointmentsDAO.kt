package com.tvisha.trooponprime.lib.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.tvisha.trooponprime.lib.database.UserAppointments

@Dao
internal interface UserAppointmentsDAO {
    @Insert
    fun insertAppointment(appointments: UserAppointments)
    @Update
    fun updateAppointment(appointments: UserAppointments)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insetBulkAppointments(appointments: List<UserAppointments>)

}