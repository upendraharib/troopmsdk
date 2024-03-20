package com.tvisha.trooponprime

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tvisha.trooponprime.calls.CallPreviewActivity
import com.tvisha.trooponprime.constants.Constants
import com.tvisha.trooponprime.lib.TroopMessengerClient
import com.tvisha.trooponprime.lib.clientModels.Contact
import com.tvisha.trooponprime.lib.clientModels.UserClientModel
import com.tvisha.trooponprime.lib.listeneres.ClientCallBackListener
import com.tvisha.trooponprime.lib.utils.Helper
import com.tvisha.trooponprime.ui.recent.conversation.ChatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject


class MyApplication : Application(), ClientCallBackListener {
    companion object{
        var appPreferences : SharedPreferences? = null
        lateinit var troopClient : TroopMessengerClient
        var usersContacts : ArrayList<UserClientModel> = ArrayList()
        var deviceContacts : ArrayList<UserClientModel> = ArrayList()
        var LOGINUSERUID : String=  ""
        var LOGINUSERTMID : String=  ""

    }
    var clientListeners : ClientListeners? = null
    interface ClientListeners {
        fun typing(jsonObject: JSONObject)
        fun userStatus(jsonObject: JSONObject)
        fun syncing(status: Boolean)
    }
    fun setListener(listeners: ClientListeners){
        clientListeners = listeners
    }
    var callListeners : CallListeners? = null
    interface CallListeners{
        fun callUserStatus(jsonObject: JSONObject)
        fun callUserPermission(jsonObject: JSONObject)
        fun callEndCall(jsonObject: JSONObject)
        fun callRemoveUser(jsonObject: JSONObject)
        fun callLeaveCall(jsonObject: JSONObject)
        fun muteUnMuteAudio(jsonObject: JSONObject)
        fun muteUnMuteVideo(jsonObject: JSONObject)
        fun selfStream(jsonObject: JSONObject)
        fun userStream(jsonObject: JSONObject)
        fun hostMuteVideo(jsonObject: JSONObject)
        fun hostMuteAudio(jsonObject: JSONObject)
        fun hostRequestUnMuteAudio(jsonObject: JSONObject)
        fun hostRequestUnMuteVideo(jsonObject: JSONObject)
    }
    fun setCallsListeners(listener:CallListeners){
        callListeners = listener
    }

    override fun onCreate() {
        super.onCreate()
        appPreferences = getSharedPreferences(Constants.SharedPreferenceConstant.APP_NAME, MODE_PRIVATE)
        troopClient = TroopMessengerClient()
        troopClient.initializeTroop(this)

        LOGINUSERUID = appPreferences!!.getString(Constants.SharedPreferenceConstant.UID,"")!!
        if (appPreferences!!.getBoolean(Constants.SharedPreferenceConstant.LOGIN_APP_STATUS,false)) {
            listenClient()
        }
        /*startKoin {
            androidContext(this@MyApplication)
            modules(appComponent)
        }*/
        //var type = mGetNetworkClass(this)!!
        //Log.e("type===> ",""+type)
    }
    fun listenClient(){
        val str = Build.MODEL
        var name  = appPreferences!!.getString(Constants.SharedPreferenceConstant.NAME,"")
        var mobileNumber = appPreferences!!.getString(Constants.SharedPreferenceConstant.MOBILE,"")
        var countryCode = appPreferences!!.getString(Constants.SharedPreferenceConstant.COUNTRY_CODE,"")

        if (!troopClient.isLoggedIn()){
            troopClient.runTheClientSdk(LOGINUSERUID, name!!, mobileNumber!!, countryCode!!, this)
        }else{
            LOGINUSERTMID = troopClient.tmUserId()
            troopClient.startListening(this)
        }
    }
    private fun callContactsAndSync(){
        Thread{
            if (Helper.getConnectivityStatus(this) && troopClient.isLoggedIn()) {
                if (Constants.checkContactPermissions(this)) {
                    syncContacts()
                }
            }
        }.start()
    }

    @SuppressLint("Range")
    private fun syncContacts() {
        var existingContact : ArrayList<UserClientModel> = ArrayList()
        var selfContact = UserClientModel()
        selfContact.mobile = "8374466508"
        selfContact.entity_name = "upendra"
        try {
            existingContact = troopClient.fetchContactList() as ArrayList<UserClientModel>
        }catch (e:Exception){
            e.printStackTrace()
        }
        val cr = contentResolver
        val cur = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                //var avatar = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    var userClientModel = UserClientModel()
                    val pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null)
                    var phoneNo = ""
                    while (pCur!!.moveToNext()) {
                        phoneNo = pCur!!.getString(pCur!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    }
                    userClientModel.isAlreadyUser = 0
                    userClientModel.entity = 1
                    userClientModel.mobile = phoneNo
                    userClientModel.entity_name = name
                    if (!phoneNo.isNullOrEmpty() && !notExists(phoneNo,existingContact)) {
                        //userClientModel.entity_avatar = avatar
                        usersContacts.add(userClientModel)
                    }
                    if (!phoneNo.isNullOrEmpty()){
                        deviceContacts.add(userClientModel)
                    }
                    pCur!!.close()
                }
            }
        }
        cur?.close()
        if (usersContacts!=null && usersContacts.size>0){
            var listOfContacts : ArrayList<Contact> = ArrayList()
            for (i in 0 until usersContacts.size) {
                var contact = Contact()
                contact.name = usersContacts[i].entity_name!!
                contact.mobile = usersContacts[i].mobile!!
                contact.country_code = "91"
                listOfContacts.add(contact)
            }
            GlobalScope.launch {
                troopClient.saveContacts(listOfContacts)
            }
        }
    }

    private fun mGetNetworkClass(context: Context): String? {

        // ConnectionManager instance
        val mConnectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val mInfo = mConnectivityManager.activeNetworkInfo

        // If not connected, "-" will be displayed
        if (mInfo == null || !mInfo.isConnected) return "-"

        // If Connected to Wifi
        if (mInfo.type == ConnectivityManager.TYPE_WIFI) return "WIFI"

        // If Connected to Mobile
        if (mInfo.type == ConnectivityManager.TYPE_MOBILE) {
            return when (mInfo.subtype) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                else -> "?"
            }
        }
        return "?"
    }
    private fun notExists(phoneNo: String, existingContact: java.util.ArrayList<UserClientModel>): Boolean {
        for (i in 0 until existingContact.size){
            if (existingContact[i].mobile==phoneNo){
                return true
            }
        }
        return false
    }
    override fun clientConnected() {
        LOGINUSERTMID = troopClient.tmUserId()
        Log.e("SDk==> "," client connected ")
        callContactsAndSync()
    }

    override fun clientDisConnected() {
        Log.e("SDk==> "," client disconnected ")
    }
    override fun tmError(jsonObject: JSONObject) {
        Log.e("SDk==> ", " error $jsonObject")
    }

    override fun loginResponse(jsonObject: JSONObject) {
        Log.e("SDk==> ", " login response $jsonObject")
    }

    override fun messageSaved(jsonObject: JSONObject) {
        Log.e("SDk==> ", " message saved $jsonObject")
    }

    override fun typing(jsonObject: JSONObject) {
        Log.e("SDk==> ", " typing $jsonObject"+"  "+(clientListeners!=null))
        if (clientListeners!=null) {
            clientListeners!!.typing(jsonObject)
        }

    }

    override fun userPresenceResponse(jsonObject: JSONObject) {
        Log.e("SDk==> ", " user presence $jsonObject")
        if (clientListeners!=null){
            clientListeners!!.userStatus(jsonObject)
        }
    }

    override fun reportUserResponse(jsonObject: JSONObject) {
        Log.e("SDk==> ", " report  user $jsonObject")
    }

    override fun blockUserResponse(jsonObject: JSONObject) {
        Log.e("SDk==> ", " block user $jsonObject")
    }

    override fun likeUnLikeMessageResponse(jsonObject: JSONObject) {
        Log.e("SDk==> ", " like unlike message $jsonObject")
    }

    override fun reportMessageResponse(jsonObject: JSONObject) {
        Log.e("SDk==> ", " report message $jsonObject")
    }

    override fun deleteChatResponse(jsonObject: JSONObject) {
        Log.e("SDk==> ", " delete chats $jsonObject")
    }

    override fun recallResponse(jsonObject: JSONObject) {
        Log.e("SDk==> ", " recall message $jsonObject")
    }

    override fun deleteResponse(jsonObject: JSONObject) {
        Log.e("SDk==> ", " delete message $jsonObject")
    }

    override fun pushNotification(jsonObject: JSONObject) {
        Log.e("SDk==> ", " notification"+jsonObject.toString())
        if (Constants.checkNotificationPermissions(this)){
            showNotification(jsonObject)
        }
    }
    override fun loadingMessages(status:Boolean){
        if (clientListeners!=null){
            clientListeners!!.syncing(status)
        }
    }

    override fun tmCallError(jsonObject: JSONObject) {
        Log.e("SDk==> ", " tmcallerror "+jsonObject.toString())
    }

    override fun onCallRequest(jsonObject: JSONObject) {
        Log.e("SDk==> ", " callrequest "+jsonObject.toString())
        var intent  =Intent(this,CallPreviewActivity::class.java)
        intent.putExtra("call_id",jsonObject.optString("call_id"))
        intent.putExtra("participants",jsonObject.optString("participants"))
        intent.putExtra("isMineCall",false)
        intent.putExtra("call_type",jsonObject.optInt("call_type"))
        intent.putExtra("initiator_id",jsonObject.optString("initiator_id"))
        startActivity(intent)
    }


    override fun onCallPermission(jsonObject: JSONObject) {
        Log.e("SDk==> ", " call permission "+jsonObject.toString())
        if (callListeners!=null){
            callListeners!!.callUserPermission(jsonObject)
        }
    }

    override fun onCallParticipantStatusUpdated(jsonObject: JSONObject) {
        Log.e("SDk==> ", " callparticipantstatus "+jsonObject.toString())
    }

    override fun onEndCall(jsonObject: JSONObject) {
        Log.e("SDk==> ", " endcall "+jsonObject.toString())
        if (callListeners!=null){
            callListeners!!.callEndCall(jsonObject)
        }
    }

    override fun onCallNewParticipants(jsonObject: JSONObject) {
        Log.e("SDk==> ", " newparticipants "+jsonObject.toString())
    }

    override fun onLeaveCall(jsonObject: JSONObject) {
        Log.e("SDk==> ", " leavecall "+jsonObject.toString())
        if (callListeners!=null){
            callListeners!!.callLeaveCall(jsonObject)
        }
    }

    override fun onCallUserVideoStatusUpdated(jsonObject: JSONObject) {
        Log.e("SDk==> ", " videostatus "+jsonObject.toString())
    }

    override fun onMuteCallAudio(jsonObject: JSONObject) {
        Log.e("SDk==> ", " muteAudio "+jsonObject.toString())
        if (callListeners!=null){
            callListeners!!.hostMuteAudio(jsonObject)
        }
    }

    override fun onMuteCallVideo(jsonObject: JSONObject) {
        Log.e("SDk==> ", " mutevideo "+jsonObject.toString())
        if (callListeners!=null){
            callListeners!!.hostMuteVideo(jsonObject)
        }
    }

    private fun showNotification(jsonObject: JSONObject) {
        createNotificationChannel()
        var intent = Intent(this,ChatActivity::class.java)
        intent.putExtra("entity_name", jsonObject.optString("entity_name"))
        intent.putExtra("entity_id", jsonObject.optString("entity_id"))
        intent.putExtra("entity_type", jsonObject.optInt("entity_type"))
        intent.putExtra("entity_uid", jsonObject.optString("entity_uid"))
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, (PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
        Log.e("notiication===> ",""+jsonObject.optInt("entity_type"))
        var builder = NotificationCompat.Builder(this, "Message")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(jsonObject.optString("sender_name")+" sent a message")
            .setContentText(jsonObject.optString("message"))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(jsonObject.optString("message")))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        NotificationManagerCompat.from(this).apply {
            notificationManager.notify(jsonObject.optInt("message_id"), builder)
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = "Message Notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Message", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun selfStream(jsonObject: JSONObject) {
        if (callListeners!=null){
            callListeners!!.selfStream(jsonObject)
        }
    }

    override fun userStream(jsonObject: JSONObject) {
        if (callListeners!=null){
            callListeners!!.userStream(jsonObject)
        }
    }

    override fun onSelfAudioMute(jsonObject: JSONObject) {

    }

    override fun onSelfVideoMute(jsonObject: JSONObject) {

    }

    override fun hostAudioMuteRequest(jsonObject: JSONObject) {
        if (callListeners!=null){
            callListeners!!.hostRequestUnMuteAudio(jsonObject)
        }
    }

    override fun hostVideoMuteRequest(jsonObject: JSONObject) {
        if (callListeners!=null){
            callListeners!!.hostRequestUnMuteVideo(jsonObject)
        }
    }


}