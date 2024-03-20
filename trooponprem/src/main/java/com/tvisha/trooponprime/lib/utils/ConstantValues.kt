package com.tvisha.trooponprime.lib.utils

import androidx.core.text.isDigitsOnly
import com.tvisha.trooponprime.lib.TroopClient
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.sharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.floor

object ConstantValues {
    val SOMETHING_WENT_WRON_ERROR_MESSAGE="Something went wrong. Please try again later."
    val CHECK_NETWORK_ERROR_MESSAGE="Please check your network connection."
    var characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    const val DATE_TIME_FORMAT_WITH_MILLI_SECONDS = "yyyy-MM-dd HH:mm:ss.SSSSSS"
    const val DATE_TIME_FORMAT_WITH_OUT_MILLI_SECONDS = "yyyy-MM-dd HH:mm:ss"
    const val DATE_TIME_FORMAT = "yyyy-MM-dd"
    const val DATE_TIME_FORMAT_24_HOURS = "yyyy-MM-dd HH:mm"
    const val SAAS = 0
    const val GRIT = 1
    const val OTHER = 2
    const val ONLY_CALLS = 3
    const val MAX_GROUP_NAME_CHARACTERS = 50
    const val DEVICE_TYPE_ANDROID = "0"
    const val PLATFORM_ANDROID = 1
    const val CALL_PLATFORM_ANDROID = 3
    const val ACTIVE = 1
    const val IN_ACTIVE = 0
    const val IS_USER_CONVERSATION = 0
    const val IS_GROUP_CONVERSATION = 1
    const val CLIENT_VERSION = "2"
    const val FOLDER_ROOT_PATH = "/storage/emulated/0/.TM/MyDeck/"
    const val API_BASE_URL = "https://apis.troopmessenger.com"
    const val CALL_API_BASE_URL = "http://192.168.2.55:8088"
    //const val API_BASE_URL = "http://192.168.2.19:8080"
    val TOKEN = "Adg9LY1hdZDpbW9ueARNZUhR1K"
    val CALL_TOKEN = "a9762e2eba5c46019ac39ac4e7a1cf1f"
    //val TOKEN = "K2xpY2syTWFnaWNUTUFQSUtFWQ"
    var deviceId:String = ""
    var TAG = "SDK==>"
    object Entity{
        const val USER =1
        const val IS_USER = 0
        const val GROUP = 2
        const val IS_GROUP = 1
    }
    object MessageTypes{
        const val TEXT_MESSAGE = 0
        const val ATTACHMENT = 1
        const val CONTACT_MESSAGE = 2
        const val LOCATION_MESSAGE = 3
        const val GROUP_CREATED  = 4
        const val GROUP_NAME_UPDATED = 5
        const val GROUP_AVATAR_UPDATED = 6
        const val GROUP_DELETED = 7
        const val GROUP_USER_EXITED = 8
        const val GROUP_USER_ADDED = 9
        const val GROUP_USER_DELETED = 10
        const val GROUP_USER_ROLE_CHANGED = 11
        const val ACTIVITY =12

        const val AUDIO_MESSAGE = 23
        const val META_LINK = 24
        const val ROOM_CREATED = 25
        const val ROOM_NAME_UPDATED = 26
        const val ROOM_AVATAR_UPDATED = 27
        const val ROOM_STATUS_UPDATED = 28
        const val ROOM_MEMBER_EXITED= 29
        const val ROOM_MEMBER_ADDED = 30
        const val ROOM_MEMBER_DELETED = 31
        const val ROOM_MEMBER_ROLE_UPDATED = 32
        const val ROOM_MEMBER_JOINED = 33

    }
    object PermissionKey{
        const val GROUP_KEY = "2"
        const val CONDITION_KEY = "4"
        const val VOICE_MESSAGE_KEY = "9"
        const val BURNOUT_KEY = "12"
        const val USER_STATUS_KEY = "16"
        const val MISCELLANEOUS_KEY = "17"
        const val ATTACHMENT_KEY ="18"

        const val IMAGE_KEY_SAAS = "19"
        const val LOCATION_MESSAGE_KEY = "19"
        const val SCREEN_SHARE_KEY = "20"
        const val IMAGE_KEY_GRIT = "23"
        const val AUDIO_KEY_SAAS = "20"
        const val AUDIO_KEY_GRIT = "24"
        const val MFA_KEY = "24"
        const val VIDEO_KEY_SAAS = "21"
        const val VIDEO_KEY_GRIT = "25"
        const val DOCUMENT_KEY_SAAS = "22"
        const val DOCUMENT_KEY_GRIT = "26"
        const val META_LINK_KEY_SAAS = "23"
        const val META_LINK_KEY_GRIT = "23"

        const val AIRTIME_GROUP_KEY = "24"
        const val RECALL_KEY_SAAS = "25"
        const val RECALL_KEY_GRIT = "45"
        const val EDIT_KEY_SAAS = "26"
        const val EDIT_KEY_GRIT = "46"
        const val LOCATION_TRACKING_KEY = "27"
        const val MENTIONS_KEY_SAAS = "29"
        const val MENTIONS_KEY_GRIT = "47"
        const val MESSAGE_INFO_KEY_SAAS = "31"
        const val MESSAGE_INFO_KEY_GRIT = "49"
        const val NOTIFY_KEY_SAAS = "33"
        const val NOTIFY_KEY_GRIT = "50"
        const val TRUMPET_KEY = "35"
        const val MISCELLANEOUS_NEW_KEY_SAAS = "36"
        const val MISCELLANEOUS_NEW_KEY_GRIT = "44"
        const val TEXT_MESSAGE_KEY_SAAS = "37"
        const val TEXT_MESSAGE_KEY_GRIT = "43"
        const val MESSAGE_DELETE_KEY_SAAS = "38"
        const val MESSAGE_DELETE_KEY_GRIT = "53"
        const val CATTLE_CALL_KEY = "42"

        const val ACCESS = "48"
    }
    object GroupValues{
        const val ADMIN = "1"
        const val MEMBER = "2"
        const val MODERATOR = "3"
        //payloads
        const val PAYLOAD_CHANGE_NAME = 1
        const val PAYLOAD_CHANGE_AVATAR = 2
        const val PAYLOAD_GROUP_DELETE = 3
        const val PAYLOAD_GROUP_CREATE = 4
        const val PAYLOAD_GROUP_EXIT = 5
        const val PAYLOAD_ADD_GROUP_MEMBER = 6
        const val PAYLOAD_REMOVE_GROUP_MEMBER = 7
        const val PAYLOAD_CHANGE_ROLE = 8
        const val PAYLOAD_GROUP_DELETE_HISTORY = 9
        const val PAYLOAD_GROUP_CHANGE_DESCRIPTION = 10
    }
    object BurnoutAndLocationTrackStatus{
        const val INACTIVE = 0
        const val ACTIVE = 1
        const val PENDING = 2
    }
    object MessageStatus{
        const val PENDING = 0
        const val SENT = 1
        const val DELIVERED = 2
        const val READ  = 3
        const val RECALLED = 4
        const val DELETE = 5

        const val IS_SYNC = 1
        const val IS_DELIVERED = 2
        const val IS_READ = 3
        const val MESSAGE_RECALLED = 2
    }
    object AttachmentMimeType{
        const val IMAGE = 1
        const val VIDEO = 2
        const val AUDIO = 3
        const val TEXT = 4
        const val PDF = 5
        const val DOC = 6
        const val XLS = 7
        const val ZIP = 8
        const val JSON = 9
        const val SQL = 10
        const val ODT = 11
        const val CSV = 12
        const val XML = 13
        const val HTML = 14
        const val JS = 15
        const val LINK = 16
        const val PPT = 17
        const val APK = 18
        const val OTHERS = 19
    }
    object UserCallStatus{
        const val IN_ACTIVE = 0
        const val ACTIVE = 1
        const val REJECT = 2
        const val UNABLE_TAKE_CALL = 3
        const val USER_DISCONNECTED = 4
        const val USER_BUSY = 5
        const val WAITING = 6
    }
    object MessageFormatsConstants{
        const val BOLD = "aHlwaGVu"
        const val STRIKE_THROUGH = "c3RyaWtldGhyb3VnaA"
        const val ITALIC = "aXRhbGljcw"
        const val UNDER_SCORE = "dW5kZXJzY29yZQ"
    }
    object GroupUpdatePayLoads{
        const val PAYLOAD_CHANGE_NAME = "1"
        const val PAYLOAD_CHANGE_AVATAR = "2"
        const val PAYLOAD_GROUP_DELETE = "3"
        const val PAYLOAD_GROUP_CREATE = "4"
        const val PAYLOAD_GROUP_EXIT = "5"
        const val PAYLOAD_ADD_MEMBERS = "6"
        const val PAYLOAD_REMOVE_MEMBERS = "7"
        const val PAYLOAD_CHANGE_ROLE = "8"
        const val PAYLOAD_GROUP_DELETE_HISTORY = "9"
        const val PAYLOAD_CHANGE_DESCRIPTION = "10"
    }
    object StoryStatus{
        const val UNSEEN = 1
        const val VIEWED = 2
        const val DELETED = 0
    }
    object StoryType{
        const val TEXT = 1
        const val IMAGE = 2
        const val VIDEO = 3
    }
    object BlockedStatus{
        const val BLOCK = 1
        const val UNBLOCK = 0
        const val USER_BLOCKED = 1
        const val USER_UNBLOCKED =2
        const val USER_BLOCKED_ME =3
        const val USER_UNBLOCKED_ME =4
    }
    object RoomPayloads{
        const val ROOM_NAME_CHANGE = "1"
        const val ROOM_AVATAR_CHANGE ="2"
        const val ROOM_DELETE = "3"
        const val ROOM_MEMBER = "4"
        const val ROOM_EXIT = "5"
        const val ADD_ROOM_MEMBERS = "6"
        const val DELETE_ROOM_MEMBERS = "7"
        const val UPDATE_ROOM_USER_ROLE = "8"
        const val JOIN_ROOM = "9"
    }
    fun fetchCurrentTimeInUTC():String{
        return Helper.localTimeToUTCTime(SimpleDateFormat(DATE_TIME_FORMAT_WITH_MILLI_SECONDS, Locale.getDefault()).format(Calendar.getInstance().time))!!
    }
    fun getMessageLocalIdFromReferenceId(referenceId:String):Long{
        try {
            if (referenceId.isNullOrEmpty()){
                return 0
            }
            var systemDeviceId = sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")!!
            if (referenceId.contains(deviceId)){
                var id  = referenceId.replace(deviceId,"")
                return if (id.isDigitsOnly()){
                    id.toLong()
                }else{
                    0
                }
            }
            return 0
        }catch (e:Exception){
            return 0;
        }
    }
    fun generateRandomString(length: Int): String? {
        var result = ""
        val charactersLength = characters.length
        for (i in 0 until length) {
            result += characters[floor(Math.random() * charactersLength).toInt()]
        }
        return result
    }
    object CurrentScreen{
        const val RECENT = 1
        const val CONVERSATION =2
        const val OTHER = 3
    }
    object MessageStatusText{
        const val PENDING ="pending"
        const val DELIVERED ="delivered"
        const val READ ="read"
        const val SENT ="sent"
    }
    object SocketOn{
        const val SENT = "SENT"
        const val RECEIVE = "RECEIVE"
        const val READ = "READ"
        const val DELIVERED = "DELIVERED"
        const val SYNC = "SYNC"
    }
    object Gallery {
            const val GALLERY_NON = 0 //not file
            const val GALLERY_IMAGE = 1
            const val GALLERY_VIDEO = 2
            const val GALLERY_AUDIO = 3
            const val GALLERY_TXT = 4
            const val GALLERY_PDF = 5
            const val GALLERY_DOC = 6
            const val GALLERY_XLS = 7
            const val GALLERY_ZIP = 8
            const val GALLERY_OTHER = 9
            const val GALLERY_JSON = 10
            const val GALLERY_SQL = 11
            const val GALLERY_ODT = 12
            const val GALLERY_CSV = 13
            const val GALLERY_XML = 14
            const val GALLERY_HTML = 15
            const val GALLERY_JS = 16
            const val GALLERY_LINK = 17
            const val GALLERY_PPT = 18
            const val CONTACT_TYPE = 19
            const val VOICE_MESSAGE = 23
            const val METALINK_MESSAGE = 24
            const val APK = 25
            const val OTHERS = 26

            //actions
            const val GALLERY_OPEN = 0
            const val GALLERY_OPEN_IMAGE = 1
            const val GALLERY_PLAY_VIDEO = 2
            const val GALLERY_PLAY_AUDIO = 3
            const val GALLERY_OPEN_DOC = 4
    }
    object NewCallTypes{
        const val SCREEN_SHARE =1
        const val VIDEO_CALL = 2
        const val AUDIO_CALL = 3
        const val CODE_SHARE = 4
        const val CATTLE_CALL = 5
    }
    object CallUserStatus{
        const val CALL_USER_STATUS_REQUEST_PENDING = 0
        const val CALL_USER_STATUS_ACTIVE = 1
        const val CALL_USER_STATUS_REJECTED = 2
        const val CALL_USER_STATUS_REQUEST_ABORTED = 3
        const val CALL_USER_STATUS_LEFT = 4
        const val CALL_USER_STATUS_BUSY = 5
        const val CALL_USER_STATUS_WAITING = 6
    }
    object CallPlatForm{
        const val PLATFORM_WEB = 1
        const val PLATFORM_DESKTOP = 2
        const val PLATFORM_ANDROID = 3
        const val PLATFORM_IOS = 4
        const val PLATFORM_BROWSER = 5
    }
    object CallKinds{
        const val KIND_AUDIO = "audio"
        const val KIND_VIDEO = "video"
    }

}