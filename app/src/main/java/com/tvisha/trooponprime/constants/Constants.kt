package com.tvisha.trooponprime.constants

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Constants {
    val CONTATCT_PERMISSION =
        arrayOf("android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS")
    val CAMERA_PERMISSION =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                "android.permission.CAMERA",
                "android.permission.READ_MEDIA_IMAGES",
                "android.permission.READ_MEDIA_VIDEO",
                "android.permission.READ_MEDIA_AUDIO",
            )
        }else
        {
            arrayOf("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE")
        }
    val STORAGE_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                "android.permission.READ_MEDIA_IMAGES",
                "android.permission.READ_MEDIA_VIDEO",
                "android.permission.READ_MEDIA_AUDIO",
            )
        }else{
            arrayOf(
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE"
            )
        }
    val LOCATION_PERMISSION =
        arrayOf(
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION"
        )
    val NOTIFICATION_PERMISSION =
        arrayOf(
            "android.permission.POST_NOTIFICATIONS"
        )
    val CALL_PERMISSION_LIST = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            "android.permission.CAMERA",
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.RECORD_AUDIO",
            "android.permission.READ_PHONE_STATE"
        )
    }else{
        arrayOf(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.READ_PHONE_STATE"
        )
    }
    val CONTACT_PERMISSION_CALLBACK = 10
    val STORAGE_PERMISSION_CALLBACK = 11
    val CAMERA_PERMISSION_CALLBACK = 12
    val LOCATION_PERMISSION_CALLBACK = 13
    val NOTIFICATION_PERMISSION_CALLBACK = 14
    var CALL_PERMISSION_CALLBACK = 15
    var TAG = "SDk=>"
    object ChatLayoutViewType{
        const val CHAT_MINE_TEXT = 0
        const val CHAT_OTHER_TEXT = 1
        const val CHAT_MINE_ATTACHMENT =2
        const val CHAT_OTHER_ATTACHMENT =3
        const val CHAT_MINE_AUDIO = 4
        const val CHAT_OTHER_AUDIO = 5
        const val CHAT_MINE_CONTACT = 6
        const val CHAT_OTHER_CONTACT = 7
        const val CHAT_MINE_LOCATION = 8
        const val CHAT_OTHER_LOCATION = 9
        const val CHAT_GROUP_NOTIFICATION = 10
    }
    fun checkCallPermission(context: Context?):Boolean{
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            return !(ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED)
        }else if (Build.VERSION.SDK_INT< Build.VERSION_CODES.TIRAMISU && Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            return !(ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED)
        }else {
            return true
        }

    }
    fun checkContactPermissions(context: Context?): Boolean {
        return !(ActivityCompat.checkSelfPermission(
            context!!,
            Manifest.permission.READ_CONTACTS
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context, Manifest.permission.WRITE_CONTACTS
        ) != PackageManager.PERMISSION_GRANTED)
    }
    fun checkLocationPermissions(context: Context?): Boolean {
        return !(ActivityCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
    }
    fun checkNotificationPermissions(context: Context?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            !(ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED )
        } else {
            true
        }
    }
    fun checkCameraStoragePermissions(context: Context?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return !(ActivityCompat.checkSelfPermission(
                context!!, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
                    ||  ActivityCompat.checkSelfPermission(
                context!!, Manifest.permission.READ_MEDIA_VIDEO
            ) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                context!!, Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED)

        }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return !(ActivityCompat.checkSelfPermission(
                context!!, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
                    || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(
                context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
                    || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(
                context!!, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        }else{
            return true
        }
    }
    fun checkStoragePermissions(context: Context?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return !(ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_MEDIA_VIDEO
            ) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED)
        }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return !(ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        }else{
            return true
        }
    }
    object SharedPreferenceConstant{
        const val APP_NAME = "com_tvisha_sdk"
        const val LOGIN_APP_STATUS = "login_app_status"
        const val UID = "uid"
        const val MOBILE = "mobile"
        const val NAME = "name"
        const val COUNTRY_CODE = "country_code"
    }

}