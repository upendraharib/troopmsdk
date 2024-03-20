package com.tvisha.trooponprime.lib.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tvisha.trooponprime.lib.database.dao.*
import com.tvisha.trooponprime.lib.utils.ConstantValues
import java.util.concurrent.Executors

@Database(entities = [User::class,MessengerGroup::class,Messenger::class,GroupMembers::class,GroupMemberStatus::class,MessengerPinnedUser::class,
    Contact::class, Notify::class,Story::class,BroadCast::class,Permission::class,Plan::class,Setting::class,UserAppointments::class],  version = 1, exportSchema = false)
internal abstract class TroopDataBase : RoomDatabase() {
    internal abstract val userDAO:UserDAO
    internal abstract val contactDAO:ContactDAO
    internal abstract val storyDAO:StoryDAO
    internal abstract val messengerDAO : MessengerDAO
    internal abstract val messengerGroupDAO:MessengerGroupDAO
    internal abstract val groupMembersDAO : GroupMembersDAO
    internal abstract val notifyDAO : NotifyDAO
    internal abstract val permissionDAO : PermissionDAO
    internal abstract val planDAO : PlanDAO
    internal abstract val settingDAO : SettingDAO
    internal abstract val userAppointmentsDAO : UserAppointmentsDAO
    internal abstract val workspaceDAO:WorkspaceDAO
    internal abstract val broadCastDAO : BroadCastDAO
    companion object{
        private var dbInstance : TroopDataBase? = null
        private const val dataBaseName  = "com_tvisha_trooponprime"
        val dataBaseRWExecutor  = Executors.newFixedThreadPool(4)!!
        fun getDataBaseInstance(context: Context): TroopDataBase {
            synchronized(this){
                var instance = dbInstance
                if (instance==null){
                    instance = Room.databaseBuilder(context, TroopDataBase::class.java, dataBaseName)
                        .fallbackToDestructiveMigration()
                        .setQueryExecutor(dataBaseRWExecutor)
                        .setTransactionExecutor(dataBaseRWExecutor)
                        .addCallback(callBack)
                        .setQueryCallback(RoomDatabase.QueryCallback{sqlQuery,bindArgs->
                            //Log.e(ConstantValues.TAG, " query " + sqlQuery + " Args=>  " + bindArgs)
                        },Executors.newSingleThreadExecutor())
                        .allowMainThreadQueries().build()
                    dbInstance = instance
                }
                return instance
            }
        }
        private val callBack = object : RoomDatabase.Callback(){
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }
    }
}