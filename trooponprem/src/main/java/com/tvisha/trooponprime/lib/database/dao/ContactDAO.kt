package com.tvisha.trooponprime.lib.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tvisha.trooponprime.lib.database.Contact
import com.tvisha.trooponprime.lib.database.Messenger
@Dao
internal interface ContactDAO {
    @Insert
    fun insertContact(contact: Contact)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBulkContacts(contact: List<Contact>)
    @Update
    fun updateMessage(contact: Contact)

    @Query("SELECT * FROM contact")
    fun fetchContacts():List<Contact>

    @Query("SELECT count(*) as count FROM contact WHERE uid =:uid")
    fun isMyContact(uid:String):Int

    @Query("SELECT uid FROM contact WHERE uid =:uid")
    fun isMyContactExists(uid:String):Int

    @Query("DELETE FROM contact")
    fun deleteContact()

    @Query("DELETE FROM contact WHERE uid IN (:uIds)")
    fun deleteContacts(uIds:List<String>)


}