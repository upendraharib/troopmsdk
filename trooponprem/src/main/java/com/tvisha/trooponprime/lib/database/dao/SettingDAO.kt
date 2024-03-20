package com.tvisha.trooponprime.lib.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tvisha.trooponprime.lib.database.Setting

@Dao
internal interface SettingDAO {
    @Insert
    fun insert(data: Setting)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllData(group: List<Setting>)

    @Query("SELECT * FROM troop_profile_settings WHERE settings_type = :type")
    fun getSettings(type:Int) : Setting
    @Query("UPDATE troop_profile_settings SET settings_type =:type,status = :status")
    fun updateStatus(type: Int,status:Int)
    @Query("UPDATE troop_profile_settings SET settings_type = :settingType,type=:type,value=:pattern,status=:status,attached_file=:imagePath")
    fun updateSettingData(settingType:Int,type: Int,pattern: String,status: Int,imagePath:String)
    @Query("SELECT settings_type FROM troop_profile_settings WHERE settings_type =:settingsType")
    fun checkSettingExist(settingsType:Int) : Int
}