package com.tvisha.trooponprime.lib.utils

import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.WindowManager
import android.webkit.URLUtil
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.lib.TroopMessengerClient
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object Helper {
    fun dpToPx(dp: Float, resources: Resources): Int {
        val px =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
        return px.toInt()
    }
    fun getConnectivityStatus(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = actNw!=null
        } else {
            var networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo!=null && networkInfo.isConnected
        }

        return result
    }
    fun checkTagExists(tagsData: String?, tag: String): Boolean {
        try {
            if (tagsData == null || tagsData.trim { it <= ' ' }.isEmpty()) {
                return false
            }
            if (tagsData.contains(",")) {
                val strings = tagsData.split(",".toRegex()).toTypedArray()
                if (strings.size > 0) {
                    for (s in strings.indices) {
                        val t = strings[s]
                        if (t.trim { it <= ' ' }
                                .replace("\"".toRegex(), "") == tag.trim { it <= ' ' }
                                .replace("\"".toRegex(), "")) {
                            return true
                        }
                    }
                }
            } else if (tagsData.trim { it <= ' ' }
                    .replace("\"".toRegex(), "") == tag.trim { it <= ' ' }
                    .replace("\"".toRegex(), "")) {
                return true
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }
    fun checkTagExistsAndRemove(tagsData: String?, tag: String): String? {
        try {
            if (tagsData != null && tagsData.contains(",")) {
                val strings = tagsData.split(",".toRegex()).toTypedArray()
                var list: ArrayList<String> = ArrayList(listOf(*strings))
                var  newlist = list.filter { !it.isNotEmpty() }.toList()
                list = newlist as ArrayList<String>
                for (i in list.indices) {
                    if (list[i].toString().replace("\"".toRegex(), "").equals(tag)){
                        list.removeAt(i)
                        break
                    }
                }
                var lists = list.toString().replace("[", "").replace("]", "")
                lists = if (lists != null && lists.trim { it <= ' ' }.isNotEmpty()) {
                    "$lists,"
                } else {
                    ""
                }
                return lists
            }
        } catch (e: java.lang.Exception) {

        }
        return ""
    }
    fun deleteTheFilesInDeviceStorage(filePathList: List<String>) {
        for (i in filePathList.indices) {
            if (filePathList[i] != null && filePathList.get(i).trim { it <= ' ' }.isNotEmpty() && filePathList[i].trim { it <= ' ' } != "null") {
                val filePath: String = filePathList.get(i)
                val file = File(filePath)
                if (file != null && file.length() > 0) {
                    file.delete()
                }
            }
        }
    }
    fun validEmail(email:String):Boolean{
        return if (email.isNotEmpty()) {
            val ePattern =
                "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$"
            val p = Pattern.compile(ePattern)
            val m = p.matcher(email)
            m.matches()
        } else {
            false
        }
    }
    fun isValidName(name: String?): Boolean {
        return if (name != null) {
            if (name.length >= 3 && !isNumeric(name)) {
                val inputStr: CharSequence = name
                val pattern = Pattern.compile("^[a-zA-Z\\s]*$")
                val matcher = pattern.matcher(inputStr)
                matcher.matches()
            } else {
                false
            }
        } else {
            false
        }
    }
    private fun isNumeric(str: String): Boolean {
        return str.matches(Regex("-?\\d+(\\.\\d+)?"))
    }
    fun isValidUrl(url: String?): Boolean {
        if (url != null) {
            return if (URLUtil.isValidUrl(url)) {
                true
            } else if (URLUtil.isHttpsUrl(url)) {
                true
            } else if (URLUtil.isHttpUrl(url)) {
                true
            } else if (url.lowercase().startsWith("www.")) {
                true
            }else{
                Patterns.WEB_URL.matcher(url).matches()
            }
        }
        return false
    }
    fun isValidPhoneNumber(str: String?): Boolean {
        return str != null && (str.startsWith("7") || str.startsWith("8") || str.startsWith("9"))
    }
    fun validMobileNumber(number:String):Boolean{
        var phoneNumber = number.replace("\\s+", "")
        return  Patterns.PHONE.matcher(phoneNumber).matches()
    }
    public fun stringToJsonObject(data: String?): JSONObject? {
        return if (!data.isNullOrEmpty()) {
            try {
                JSONObject(data)
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
    fun stringToJsonArray(data: String?): JSONArray? {
        return if (data != null) {
            try {
                JSONArray(data)
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
    fun localTimeToUTCTime(date: String?): String? {
        var utcTime = ""
        var dateFormat: SimpleDateFormat? = null
        try {
            if (!date.isNullOrEmpty() && date != "null") {
                dateFormat = if (date.contains(".")) {
                    SimpleDateFormat(ConstantValues.DATE_TIME_FORMAT_WITH_MILLI_SECONDS,Locale.getDefault())
                } else {
                    SimpleDateFormat(ConstantValues.DATE_TIME_FORMAT_WITH_OUT_MILLI_SECONDS,Locale.getDefault())
                }
                val date1 = dateFormat.parse(date)
                //dateFormat.timeZone = TimeZone.getTimeZone("GMT+0")
                dateFormat.timeZone = TimeZone.getTimeZone("GMT+5:30")
                utcTime = dateFormat.format(date1)
            }
        } catch (e: Exception) {

        }
        return utcTime
    }
    fun utcToLocalTime(date: String?): String? {
        var date = date
        var localTime = ""
        try {
            val timestamp = date
            if (!date.isNullOrEmpty() && date != "null") {
                val formatterUTC: DateFormat = if (date.contains(".")) {
                    SimpleDateFormat(ConstantValues.DATE_TIME_FORMAT_WITH_MILLI_SECONDS,Locale.getDefault())
                } else {
                    SimpleDateFormat(ConstantValues.DATE_TIME_FORMAT_WITH_OUT_MILLI_SECONDS,Locale.getDefault())
                }
                //formatterUTC.timeZone = TimeZone.getTimeZone("GMT+0")
                formatterUTC.timeZone = TimeZone.getTimeZone("GMT+5:30")
                val date11 = formatterUTC.parse(date)
                val dateFormats: DateFormat =
                    SimpleDateFormat(ConstantValues.DATE_TIME_FORMAT_WITH_MILLI_SECONDS,Locale.getDefault())
                dateFormats.timeZone = TimeZone.getDefault()
                localTime = dateFormats.format(date11)
            }
        } catch (e: java.lang.Exception) {

        }
        return localTime
    }
    fun toStringArray(array: JSONArray): ArrayList<String> {
        val list: ArrayList<String> = ArrayList()
        var arrayLength = array!!.length()
        for (i in 0 until arrayLength) {
            list.add(array.optString(i))
        }
        Log.e(ConstantValues.TAG, " string "+list.toString())
        return list
    }
    fun toLongArray(array: JSONArray): ArrayList<Long> {
        val list: ArrayList<Long> = ArrayList()
        var arrayLength = array!!.length()
        for (i in 0 until arrayLength) {
            list.add(array.optString(i).toLong())
        }
        return list
    }
    fun getConversationMessagesTime(timestamp: String?): String? {
        try {
            var d1: Date? = null
            val calendar = Calendar.getInstance()
            var form: SimpleDateFormat? = null
            form = if (timestamp!!.contains(".")) {
                SimpleDateFormat(ConstantValues.DATE_TIME_FORMAT_WITH_MILLI_SECONDS,Locale.getDefault())
            } else {
                SimpleDateFormat(ConstantValues.DATE_TIME_FORMAT_WITH_OUT_MILLI_SECONDS,Locale.getDefault())
            }
            d1 = if (timestamp != null) {
                form.parse(timestamp)
            } else {
                val date = form.format(calendar.time)
                form.parse(date)
            }
            val postDate = Calendar.getInstance()
            postDate.time = d1
            val dateFormat = SimpleDateFormat("hh:mm a")
            val date = dateFormat.format(d1)
            //return date.toUpperCase();
            return ((if (postDate[Calendar.HOUR] > 0) postDate[Calendar.HOUR] else 12).toString() + ":" +
                    (if (postDate[Calendar.MINUTE] > 9) postDate[Calendar.MINUTE] else "0" + postDate[Calendar.MINUTE]) + " "
                    + if (postDate[Calendar.AM_PM] == 0) "AM" else "PM")
        } catch (e: java.lang.Exception) {
            //Helper.Companion.printExceptions(e)
        }
        return null
    }
    fun getRecentListMessagesDateTime(timestamp: String?): String? {
        try {
            var form: SimpleDateFormat? = null
            form = if (timestamp != null && timestamp.contains(".")) {
                SimpleDateFormat(ConstantValues.DATE_TIME_FORMAT_WITH_MILLI_SECONDS,Locale.getDefault())
            } else {
                SimpleDateFormat(ConstantValues.DATE_TIME_FORMAT_WITH_OUT_MILLI_SECONDS,Locale.getDefault())
            }
            val d1 = form.parse(timestamp)
            val tdy1 = Calendar.getInstance()
            tdy1.time = d1
            val sysDate = Calendar.getInstance()
            val time: String
            time = if (isSameDay(tdy1, sysDate)) {
                ((if (tdy1[Calendar.HOUR] > 0) tdy1[Calendar.HOUR] else 12).toString() + ":" +
                        (if (tdy1[Calendar.MINUTE] > 9) tdy1[Calendar.MINUTE] else "0" + tdy1[Calendar.MINUTE]) + " "
                        + if (tdy1[Calendar.AM_PM] == 0) "AM" else "PM")
            } else if (sysDate[Calendar.YEAR] != tdy1[Calendar.YEAR]) {
                val months_3: Array<String> =
                    TroopMessengerClient.context!!.resources
                        .getStringArray(R.array.months_array_3)
                tdy1[Calendar.DATE].toString() + " " + months_3[tdy1[Calendar.MONTH]] + " " + tdy1[Calendar.YEAR]
            } else {
                val months_3: Array<String> =
                    TroopMessengerClient.context!!.resources
                        .getStringArray(R.array.months_array_3)
                tdy1[Calendar.DATE].toString() + " " + months_3[tdy1[Calendar.MONTH]]
            }
            return time
        } catch (e: java.lang.Exception) {
            //Helper.Companion.printExceptions(e)
        }
        return null
    }
    fun isSameDay(cal1: Calendar?, cal2: Calendar?): Boolean {
        require(!(cal1 == null || cal2 == null)) { "The dates must not be null" }
        return cal1[Calendar.ERA] == cal2[Calendar.ERA] && cal1[Calendar.YEAR] == cal2[Calendar.YEAR] && cal1[Calendar.MONTH] == cal2[Calendar.MONTH] && cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
    }
    fun getRealPathFromURI(context: Context, contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }
    fun getFileNameWithExt(filename: String): String? {
        try {
            val raw_data_path = filename.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val file_name =
                raw_data_path[raw_data_path.size - 1].replace("[^A-Za-z0-9 .]".toRegex(), "_")
            val message_attachment = file_name.replace("\"".toRegex(), "")
            val file_split = message_attachment.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            return file_split[file_split.size - 1]
        } catch (e: java.lang.Exception) {
            //Helper.Companion.printExceptions(e)
        }
        return filename
    }
     fun isSeverMessage(messageType:Int):Boolean{
        if (messageType == ConstantValues.MessageTypes.GROUP_CREATED
            || messageType==ConstantValues.MessageTypes.GROUP_NAME_UPDATED
            || messageType==ConstantValues.MessageTypes.GROUP_AVATAR_UPDATED
            || messageType==ConstantValues.MessageTypes.GROUP_DELETED
            || messageType==ConstantValues.MessageTypes.GROUP_USER_EXITED
            || messageType==ConstantValues.MessageTypes.GROUP_USER_ADDED
            || messageType==ConstantValues.MessageTypes.GROUP_USER_DELETED
            || messageType==ConstantValues.MessageTypes.GROUP_USER_ROLE_CHANGED
            || messageType==ConstantValues.MessageTypes.ROOM_CREATED
            || messageType==ConstantValues.MessageTypes.ROOM_NAME_UPDATED
            || messageType==ConstantValues.MessageTypes.ROOM_AVATAR_UPDATED
            || messageType==ConstantValues.MessageTypes.ROOM_STATUS_UPDATED
            || messageType==ConstantValues.MessageTypes.ROOM_MEMBER_EXITED
            || messageType==ConstantValues.MessageTypes.ROOM_MEMBER_ADDED
            || messageType==ConstantValues.MessageTypes.ROOM_MEMBER_DELETED
            || messageType==ConstantValues.MessageTypes.ROOM_MEMBER_ROLE_UPDATED
            || messageType==ConstantValues.MessageTypes.ROOM_MEMBER_JOINED
        ){
            return true
        }
        return false
    }
    fun isGroupUpdatedMessage(msgType: Int,userLable: String,msgObj: String?,mine: Boolean,workspace_userid: String,sender_id:String):String{
        var headerMessage = ""
        try {
            when (msgType) {
                ConstantValues.MessageTypes.GROUP_CREATED -> headerMessage =
                    "$userLable Created group"
                ConstantValues.MessageTypes.GROUP_NAME_UPDATED -> headerMessage =
                    "$userLable changed group name"
                ConstantValues.MessageTypes.GROUP_AVATAR_UPDATED -> headerMessage =
                    if (msgObj != null && !msgObj.isEmpty() && msgObj != "null") {
                        val `object` = stringToJsonObject(msgObj)
                        if (`object`!!.optInt("status") == 1) {
                            "$userLable changed group avatar"
                        } else {
                            "$userLable removed group avatar"
                        }
                    } else {
                        "$userLable changed group avatar"
                    }
                ConstantValues.MessageTypes.GROUP_DELETED -> headerMessage =
                    "$userLable deleted group"
                ConstantValues.MessageTypes.GROUP_USER_EXITED -> {
                    val jsonObject = stringToJsonObject(msgObj)
                    headerMessage = if (jsonObject!!.optInt("admin_removed") == ConstantValues.ACTIVE) {
                        jsonObject.optString("name") + " is not a member anymore "
                    } else {
                        "$userLable left"
                    }
                }
                ConstantValues.MessageTypes.GROUP_USER_ADDED -> {
                    val adduserObject = stringToJsonObject(msgObj)
                    if (sender_id==adduserObject!!.optString("user_id")){
                        if (workspace_userid==sender_id) {
                            headerMessage = "You are added into group"
                        }else{
                            headerMessage =
                                userLable+" is added into group"
                        }
                    }else {
                        headerMessage =
                            userLable + " added "+ if (workspace_userid == adduserObject!!.getString("user_id")) "You" else adduserObject.getString("name")
                    }
                }
                ConstantValues.MessageTypes.GROUP_USER_ROLE_CHANGED -> {
                    val userroleObject = stringToJsonObject(msgObj)
                    headerMessage = userLable + " made " + if (workspace_userid == userroleObject!!.getString("user_id")) "you" else userroleObject.optString("name")

                    if (userroleObject.optInt("is_server") == ConstantValues.ACTIVE) {
                        val messager_name = if (workspace_userid == userroleObject.getString("user_id")) "You are now admin" else userroleObject.optString("name") + " is now admin "
                        headerMessage = "$messager_name "
                    } else if (userroleObject.optInt("new_role") == ConstantValues.GroupValues.ADMIN.toInt()) {
                        if (sender_id==userroleObject!!.optString("user_id")){
                            if (workspace_userid==sender_id) {
                                headerMessage = " you are now admin "
                            }else{
                                headerMessage = userLable+" is now admin "
                            }
                        }else{
                            headerMessage += " as admin"
                        }
                    } else if (userroleObject.optInt("new_role") == ConstantValues.GroupValues.MODERATOR.toInt()) {
                        if (sender_id==userroleObject!!.optString("user_id")){
                            if (workspace_userid==sender_id) {
                                headerMessage =
                                     "you are now moderator "
                            }else{
                                headerMessage = userLable+" is now moderator "
                            }
                        } else {
                            headerMessage += " as moderator"
                        }
                        //headerMessage += " " + MessengerApplication.context.getString(R.string.as_moderator)
                    } else if (userroleObject.optInt("new_role") == ConstantValues.GroupValues.MEMBER.toInt()) {
                        if (userroleObject.optInt("old_role") == ConstantValues.GroupValues.ADMIN.toInt()) {
                            if (sender_id==userroleObject!!.optString("user_id")){
                                if (workspace_userid==sender_id ) {
                                    headerMessage =
                                        "you were removed as admin "
                                }else{
                                    headerMessage = userLable+" was removed as admin "
                                }
                            } else {
                                headerMessage =
                                    userLable + " removed "  + (if (workspace_userid == userroleObject.getString(
                                            "user_id"
                                        )
                                    ) "You" else userroleObject.getString(
                                        "name"
                                    )) + " as admin "
                            }
                        } else if (userroleObject.optInt("old_role") == ConstantValues.GroupValues.MODERATOR.toInt()) {
                            if (sender_id==userroleObject!!.optString("user_id")){
                                if (workspace_userid==sender_id) {
                                    headerMessage =
                                        "you were removed as moderator "
                                }else{
                                    headerMessage = userLable+" was removed as moderator "
                                }
                            } else {
                                headerMessage =
                                    userLable + " removed " + (if (workspace_userid == userroleObject.getString(
                                            "user_id"
                                        )
                                    ) "you" else userroleObject.getString(
                                        "name"
                                    )) + " as moderator "
                            }
                        }
                    }
                }
                ConstantValues.MessageTypes.GROUP_USER_DELETED -> {
                    val userDeleteObject = stringToJsonObject(msgObj)
                    if (userDeleteObject!=null) {
                        if (sender_id == userDeleteObject!!.optString("user_id")) {
                            if (workspace_userid == sender_id) {
                                headerMessage = "You are removed from group"
                            } else {
                                headerMessage =
                                    userLable + " is removed from group "
                            }
                        } else {
                            headerMessage =
                                userLable + " " + if (workspace_userid == userDeleteObject!!.getString(
                                        "user_id"
                                    )
                                ) "removed you" else  " removed " + userDeleteObject.getString("name")
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            //printExceptions(e)
        }
        return headerMessage
    }

    fun timeCalculate(created_at: String, time: Long): Boolean {
        try {
            val seconds: Long = 0
            var d2: Date? = null
            var d1: Date? = null
            var form: SimpleDateFormat? = null
            form = if (created_at.contains(".")) {
                SimpleDateFormat(ConstantValues.DATE_TIME_FORMAT_WITH_MILLI_SECONDS, Locale.getDefault())
            } else {
                SimpleDateFormat(ConstantValues.DATE_TIME_FORMAT_WITH_OUT_MILLI_SECONDS, Locale.getDefault())
            }
            d1 = form.parse(created_at)
            val date =
                SimpleDateFormat(ConstantValues.DATE_TIME_FORMAT_WITH_MILLI_SECONDS, Locale.getDefault()).format(Calendar.getInstance().time)
            d2 = SimpleDateFormat(ConstantValues.DATE_TIME_FORMAT_WITH_OUT_MILLI_SECONDS, Locale.getDefault()).parse(date)
            val different = d2.time - d1.time
            val minutes = TimeUnit.MILLISECONDS.toMinutes(different)
            if (minutes < time) {
                return true
            }
        } catch (e: java.lang.Exception) {
        }
        return false
    }
    fun getDeviceMetrics(context: Context): DisplayMetrics {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        display.getMetrics(metrics)
        return metrics
    }
    fun getFileIconPath(path: String?): Int {
        try {
            if (path != null && !path.isEmpty() && path != "null") {
                val filenameArray = path.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                var extention = filenameArray[filenameArray.size - 1]
                extention = extention.lowercase(Locale.getDefault())
                return getFileType(extention.lowercase(Locale.getDefault()))
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return 0
    }
    fun getFileType(extention: String?): Int {
        try {
            if (extention == null) //if null
                return 0
            return if (extention.matches("jpg|jpeg|png|gif|bmp|ico|icns".toRegex())) {
                ConstantValues.Gallery.GALLERY_IMAGE
            } else if (extention.matches("docx|doc".toRegex())) {
                ConstantValues.Gallery.GALLERY_DOC
            } else if (extention.matches("xls|xlsx".toRegex())) {
                ConstantValues.Gallery.GALLERY_XLS
            } else if (extention.matches("txt".toRegex())) {
                ConstantValues.Gallery.GALLERY_TXT
            } /*else if (extention.matches("zip|rar")) {
                    return Values.Gallery.GALLERY_ZIP;
                }*/ else if (extention.matches("pdf".toRegex())) {
                ConstantValues.Gallery.GALLERY_PDF
            } else if (extention.matches("mp3|wav|ogg|m4a".toRegex())) { //if (extention.matches("mpeg|mp3|wma|voc|ra|wav|ogg|opus|aac|ac3|aiff|amr|au|flac|m4a|mid|mka|m4r|vox")) {
                ConstantValues.Gallery.GALLERY_AUDIO
            } else if (extention.matches("mp4|mpeg|3gpp|mkv|flv|3gp|mov|avi|webm|wmv".toRegex())) {
                ConstantValues.Gallery.GALLERY_VIDEO
            } /*else if (extention.matches("json")) {
                    return Values.Gallery.GALLERY_JSON;
                } else if (extention.matches("sql")) {
                    return Values.Gallery.GALLERY_SQL;
                } else if (extention.matches("odt")) {
                    return R.drawable.ic_attachment_file;
                } */ else if (extention.matches("csv".toRegex())) {
                ConstantValues.Gallery.GALLERY_CSV
            } /*else if (extention.matches("xml")) {
                    return Values.Gallery.GALLERY_XML;
                } else if (extention.matches("html")) {
                    return Values.Gallery.GALLERY_HTML;
                } else if (extention.matches("js")) {
                    return Values.Gallery.GALLERY_JS;
                }*/ else if (extention.matches("v-card|octet_stream|vcard|vcf|x_vcard".toRegex())) {
                ConstantValues.Gallery.CONTACT_TYPE
            } else if (extention.lowercase(Locale.getDefault()).matches("apk".toRegex())) {
                ConstantValues.Gallery.APK
            } else {
                ConstantValues.Gallery.GALLERY_OTHER
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return 0
    }
    fun getAttachmentIcon(attachmentIcon: Int): Int {
        try {
            return when (attachmentIcon) {
                ConstantValues.Gallery.GALLERY_IMAGE -> R.drawable.ic_attachment_img_small
                ConstantValues.Gallery.GALLERY_VIDEO -> R.drawable.ic_attachment_video
                ConstantValues.Gallery.GALLERY_PDF -> R.drawable.ic_pdf
                ConstantValues.Gallery.GALLERY_DOC -> R.drawable.ic_doc
                ConstantValues.Gallery.GALLERY_XLS -> R.drawable.ic_xls
                ConstantValues.Gallery.GALLERY_HTML -> R.drawable.ic_html
                ConstantValues.Gallery.GALLERY_PPT -> R.drawable.ic_ppt
                ConstantValues.Gallery.GALLERY_LINK -> R.drawable.ic_link
                ConstantValues.Gallery.GALLERY_TXT -> R.drawable.ic_txt
                ConstantValues.Gallery.GALLERY_SQL -> R.drawable.ic_sql
                ConstantValues.Gallery.GALLERY_ZIP -> R.drawable.ic_zip
                ConstantValues.Gallery.GALLERY_CSV -> R.drawable.ic_csv
                ConstantValues.Gallery.GALLERY_JS -> R.drawable.ic_js
                ConstantValues.Gallery.GALLERY_AUDIO -> R.drawable.ic_attachment_audio
                else -> R.drawable.other
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return 0
    }

    fun getMemberRoleText(memberRole: Int): String {
        return when(memberRole){
            ConstantValues.GroupValues.ADMIN.toInt()->{
                "Admin"
            }

            ConstantValues.GroupValues.MODERATOR.toInt()->{
                "Moderator"
            }

            else ->{
                "Member"
            }
        }
    }
    fun printExceptions(e:Exception){
        e.printStackTrace()
    }
    fun cryptLibEncryptMessage(data: String?):String{
        return data!!
    }

    fun getCallStatusText(status:Int): String? {
        when(status){
            ConstantValues.UserCallStatus.ACTIVE->{
                return "In call"
            }
            ConstantValues.UserCallStatus.UNABLE_TAKE_CALL->{
                return "Unable to take call"
            }
            ConstantValues.UserCallStatus.USER_DISCONNECTED->{
                return "Disconnected"
            }
            ConstantValues.UserCallStatus.USER_BUSY->{
                return "Busy"
            }
            ConstantValues.UserCallStatus.WAITING->{
                return "Waiting"
            }
            ConstantValues.UserCallStatus.REJECT->{
                return "Rejected"
            }
            ConstantValues.UserCallStatus.IN_ACTIVE->{
                return "Not answered"
            }
        }
        return ""
    }
}