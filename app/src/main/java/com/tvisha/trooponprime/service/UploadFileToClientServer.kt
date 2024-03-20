package com.tvisha.trooponprime.service

import android.app.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.amazonaws.ClientConfiguration
import com.amazonaws.mobileconnectors.s3.transferutility.*
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.tvisha.trooponprime.MyApplication
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.constants.AmazonUtil
import com.tvisha.trooponprime.lib.clientModels.MessageData
import com.tvisha.trooponprime.lib.utils.Helper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Random

class UploadFileToClientServer : Service() {
    var nManager: NotificationManager? = null
    var mBuilder: NotificationCompat.Builder? = null
    var AWS_ANDROID_FILE_PATH:String = ""
    var AWS_REGION:String = ""
    var AWS_ACCESS_KEY:String = ""
    var AWS_SECRET_KEY:String = ""
    var AWS_END_POINT:String = ""
    var AWS_BUCKET_NAME:String = ""
    var trasnferDataObject : JSONArray = JSONArray()
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var data = Helper.stringToJsonArray(intent!!.getStringExtra("data")!!)
        if (data!=null && data.length()>0){
            uploadFilesToServer(data)
        }
        return Service.START_STICKY
    }
    private fun uploadFilesToServer(data:JSONArray){
        if (nManager == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startMyOwnForeground()
            } else {
                startForegroundService()
            }
        }
        for (i in 0 until data.length()){
            sendToAws(data.optJSONObject(i),data.length())
        }
    }
    inner class sendToAws(messageObject: JSONObject, var totalFiles:Int) {
        val jsonObject = messageObject
        init {
            callAws()
        }
        private fun callAws() {

            val lastMessageid = jsonObject.optLong("ID")
            var filePath = jsonObject.optString("attachment")
            if (File(filePath).isFile && File(filePath).length()>0) {
                val raw_data_path: Array<String> =
                    filePath!!.replace("[^\\x00-\\x7F]".toRegex(), "_").split("/".toRegex())
                        .toTypedArray()
                var file_name =
                    raw_data_path[raw_data_path.size - 1].replace("[^A-Za-z0-9 .]".toRegex(), "_")
                        .replace("[^\\x00-\\x7F]".toRegex(), "_")
                file_name = file_name.replace("_{2,}".toRegex(), "_")
                val random = Random()
                val code = 100000 + random.nextInt(900000)
                val tsLong = System.currentTimeMillis() / 1000
                val ts = tsLong.toString() + "" + code
                var AWS_FILE_KEY = AWS_ANDROID_FILE_PATH + ts + "/" + file_name.replace(" ", "")
                val configuration: ClientConfiguration = AmazonUtil.getTheConfigaration()!!
                val region: Region = Region.getRegion(AWS_REGION)
                val s3Client = AmazonS3Client(
                    AmazonUtil.getCredProvider(
                        AWS_ACCESS_KEY,
                        AWS_SECRET_KEY,
                        ""
                    ),
                    region,
                    configuration
                )
                s3Client.endpoint = AWS_END_POINT
                //s3Client.setRegion(Region.getRegion(AWS_REGION));

                //s3Client.setRegion(Region.getRegion(AWS_REGION));
                val transferUtility = TransferUtility.builder()
                    .context(applicationContext)
                    .s3Client(s3Client)
                    .defaultBucket(AWS_BUCKET_NAME)
                    .build() //new TransferUtility(s3Client, getApplicationContext());

                val observers: MutableList<TransferObserver> = transferUtility.getTransfersWithType(
                    TransferType.UPLOAD)
                observers.clear()
                val observer: TransferObserver = transferUtility.upload(
                    AWS_BUCKET_NAME,
                    AWS_FILE_KEY,
                    File(filePath),
                    ObjectMetadata(),
                    CannedAccessControlList.PublicRead
                )
                var jsonObjects = JSONObject()
                jsonObjects.put("observer",observer)
                jsonObjects.put("transferUtility",transferUtility)
                jsonObjects.put("local_id",lastMessageid)
                trasnferDataObject.put(jsonObjects)
                observers.add(observer)
                try {
                    observer.setTransferListener(
                        UploadAttachment(AWS_FILE_KEY!!, jsonObject, filePath, lastMessageid,totalFiles)
                    )
                } catch (e: Exception) {

                }
            }else{
                totalFiles= totalFiles-1
            }
        }

    }
    var awsFileObject : JSONArray = JSONArray()
    var completedFiles : Int = 0
    var inProgress = false
    inner class UploadAttachment(awsFileKey: String, jsonObject: JSONObject, filePath: String, lastMessageid: Long, var totalFiles: Int) :
        TransferListener {
        val awsFile = awsFileKey
        val orignalPath = filePath
        val messageObjet = jsonObject
        val lastMessageid = lastMessageid
        override fun onStateChanged(id: Int, transferState: TransferState?) {
            if (transferState == TransferState.COMPLETED) {
                completedFiles++
                messageObjet.put("attachment",awsFile)
                awsFileObject.put(messageObjet)

                //awsFilePaths.add(file_path)
                updateFilePathInDb(lastMessageid,orignalPath)
                if (totalFiles==completedFiles ){
                    inProgress = false
                    sendAttachmentMessageToServer(awsFileObject)
                }
            } else if (transferState == TransferState.FAILED || transferState == TransferState.CANCELED) {
                totalFiles--
                /*if (MessengerApplication.SOCKET_OPENED_ACTIVITY == Values.AppActivities.ACTIVITY_CHAT) {
                    if (HandlerHolder.conversationHandler != null) {
                        *//*val `object`: JSONObject = getTheFileData(i, jsonArray, listArray)
                        if (`object` != null) {*//*
                        val uploaderror = JSONObject()
                        uploaderror.put("local_id", lastMessageid)
                        uploaderror.put(Values.WORKSPACEID_KEY, messageObjet.optString(Values.WORKSPACEID_KEY))
                        HandlerHolder.conversationHandler.obtainMessage(
                            Values.RecentList.AWS_FILE_ERROR,
                            uploaderror
                        ).sendToTarget()
                        //}
                    }
                }*/
                if (totalFiles==0){
                    inProgress = false
                    stopService()
                }
            }
        }

        private fun sendAttachmentMessageToServer(awsFileObject: JSONArray) {
            for (i in 0 until awsFileObject.length()){
                var messageObject = awsFileObject.optJSONObject(i)
                var messageData = MessageData()
                messageData.message = ""
                messageData.message_id = if (messageObject.optInt("is_reply")==1) messageObject.optLong("message_id") else 0
                messageData.receiver_uid = messageObject.optString("receiver_uid")
                messageData.sender_uid = messageObject.optString("sender_uid")
                messageData.attachment = messageObject.optString("attachment")
                messageData.is_room = messageObject.optInt("is_room")
                messageData.is_group = messageObject.optInt("is_group")
                messageData.receiver_id = messageObject.optString("receiver_id")
                messageData.caption = messageObject.optString("caption")
                messageData.preview_link = messageObject.optString("attachment")
                if (messageObject.optInt("is_reply")==1) {
                    troopClient.sendReplyAttachmentMessage(messageData)
                } else{
                    troopClient.sendAttachmentMessage(messageData)
                }
            }
            inProgress = false
            stopService()

        /*if (mSocket!=null && mSocket!!.connected()){
                mSocket!!.emit(SocketConstants.SYNC_OFFLINE, awsFileObject)
                completedFiles = 0
                inProgress = false
                stopService()
            }*/
        }
        fun stopService(){
            Thread{
                Thread.sleep(5000)
                if (!inProgress) {
                    stopService()
                }
            }.start()
        }

        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
            inProgress = true
            val _bytesCurrent: Long = bytesCurrent
            val _bytesTotal: Long = bytesTotal
            val percentage = _bytesCurrent.toFloat() / _bytesTotal.toFloat() * 100
            val percentages = percentage.toInt()
            if (percentage>0) {
                /*if (MessengerApplication.SOCKET_OPENED_ACTIVITY == Values.AppActivities.ACTIVITY_CHAT) {
                    if (HandlerHolder.conversationHandler != null) {
                        val progressObject = JSONObject()
                        progressObject.put("local_id", lastMessageid)
                        progressObject.put("percentage", percentages)
                        progressObject.put(
                            Values.WORKSPACEID_KEY, messageObjet.optString(Values.WORKSPACEID_KEY)
                        )
                        HandlerHolder.conversationHandler.obtainMessage(
                            Values.RecentList.AWS_FILE_PROGRESS,
                            progressObject
                        ).sendToTarget()
                    }
                }*/
            }
        }

        override fun onError(id: Int, ex: java.lang.Exception?) {
            /*if (MessengerApplication.SOCKET_OPENED_ACTIVITY == Values.AppActivities.ACTIVITY_CHAT) {
                if (HandlerHolder.conversationHandler != null) {
                    *//*val `object`: JSONObject = getTheFileData(i, jsonArray, listArray)
                    if (`object` != null) {*//*
                    val uploaderror = JSONObject()
                    uploaderror.put("local_id", lastMessageid)
                    uploaderror.put(Values.WORKSPACEID_KEY, messageObjet.optString(Values.WORKSPACEID_KEY))
                    HandlerHolder.conversationHandler.obtainMessage(
                        Values.RecentList.AWS_FILE_ERROR,
                        uploaderror
                    ).sendToTarget()
                    //}
                }
            }*/
            if (totalFiles==0){
                inProgress = false
                stopService()
            }
        }
        private fun updateFilePathInDb(lastMessageid: Long, filePath: String) {
            Thread{
                troopClient.updateAwsFilePath(lastMessageid,filePath)
            }.start()
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "com.tvisha.trooponprime"
        val channelName = "FileUploading"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        chan.setSound(null, null)
        nManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        assert(nManager != null)
        nManager!!.createNotificationChannel(chan)
        mBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID).setOngoing(true)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle("uploading file")
                .setSound(null)
        //.setProgress(100, 0, false)
        startForeground(112233, mBuilder!!.build())
    }

    private fun getNotificationIcon(): Int {
        return  R.drawable.ic_launcher_foreground
    }

    private fun startForegroundService() {
        val intent = Intent()
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Create notification builder.
        mBuilder = NotificationCompat.Builder(this)
        nManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?


        // Make notification show big text.
        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle("uploading file")
        // Set big text style.
        mBuilder!!.setStyle(bigTextStyle)
        mBuilder!!.setWhen(System.currentTimeMillis())
        mBuilder!!.setSmallIcon(getNotificationIcon())
        mBuilder!!.setOngoing(true)
        val largeIconBitmap = BitmapFactory.decodeResource(
            resources, R.drawable.ic_launcher_foreground
        )
        mBuilder!!.setLargeIcon(largeIconBitmap)
        // Make the notification max priority.
        mBuilder!!.priority = Notification.PRIORITY_MAX
        // Make head-up notification.
        mBuilder!!.setFullScreenIntent(pendingIntent, true)
        //mBuilder!!.setProgress(100, 0, false)
        val notification: Notification = mBuilder!!.build()
        startForeground(112233, notification)
    }
    override fun onDestroy() {
        super.onDestroy()
        if (nManager != null) {
            nManager!!.cancel(112233)
            nManager = null
        }
    }
}