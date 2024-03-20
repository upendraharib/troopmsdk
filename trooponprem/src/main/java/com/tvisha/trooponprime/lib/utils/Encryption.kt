package com.tvisha.trooponprime.lib.utils

import android.util.Base64
import android.util.Log
import com.tvisha.trooponprime.lib.TroopClient
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APPLICATION_TM_LOGIN_USER_ID
import org.json.JSONObject
import org.spongycastle.crypto.digests.SHA256Digest
import org.spongycastle.crypto.digests.SHA384Digest
import org.spongycastle.crypto.digests.SHA512Digest
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.spongycastle.crypto.params.KeyParameter
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
 object Encryption {
    var skey = "12fb1e094d386fe072a754407c0c7ea1"
    var IV = "2fbbb67304b5599c"
    //var AAD_KEY :String = "kwl4ey2VFXs5kn0WmEw2Go1xM9St9AOZ"
    var AAD_KEY :String = ""
    private const val PBKDF2_ITERATIONS = 10000
    object EncryptionType{
        const val PLAIN = 0
        const val STANDARD = 1
        const val E2EE = 2
    }

    fun encryptMessage(encryptionType: Int,plainText:String,entityId:String,entityType:Int,referenceId:String,senderId:String,receiverId:String):String{
        /*if (true){
            return plainText
        }*/
        if (encryptionType==EncryptionType.STANDARD){
            var masterKey = getMasterKey(senderId,receiverId,referenceId,entityType)
            var secretKeyData = constructKey(masterKey)
            var security = getKeyAndSalt(secretKeyData!!,"")
            var iv = constructIv(masterKey)
            return encryptE2E(security!!.optString("key"),iv!!,plainText,security!!.optString("salt"))!!
        }else{
            return plainText
        }
    }
    fun decryptMessage(encryptionType: Int,cipherText:String,entityId: String,entityType: Int,referenceId: String,senderId: String,receiverId: String):String{
        if (cipherText.isNullOrEmpty()){
            return cipherText
        }
        try {
            if (encryptionType==EncryptionType.STANDARD){
                var masterKey = getMasterKey(senderId,receiverId,referenceId,entityType)
                var secretKeyData = constructKey(masterKey)

                //var iv = constructIv(masterKey)
                return decryptE2eMessage(cipherText,secretKeyData)!!
            }else{
                return cipherText
            }
        }catch (e:Exception){
            return cipherText
        }
    }
    private fun getMasterKey(senderId: String, receiverId: String, referenceId: String, entityType: Int):String{
        return "$senderId$receiverId$referenceId${entityType.toString()}AZW8vozr5v7ryiKbraDWKGOUeMo2muQqoZo+26T0w4sr1VEysDYPAO3j4KR7DX1K"
    }
     private fun constructKey(masterKey: String):String?{
         var secretKey = ""
         for (i in masterKey.indices){
             if (mutableListOf(0, 1, 2, 3, 4, 5, 9, 11, 14, 15, 20, 21, 22, 25, 28, 30, 32, 35, 37, 40, 41, 45, 46, 48, 50, 52, 54, 56, 58, 60, 62, 63).contains(i)){
                 secretKey+=masterKey[i]
             }
             if (secretKey.length==24){
                 break
             }
         }
         return secretKey
     }
    /*private fun constructKey(encryptionType: Int, secretKey: String): String? {
        //return secretKey;
        var cipherKey = StringBuilder()
        if (encryptionType == EncryptionType.E2EE) {
            for (i in secretKey.indices) {
                if (mutableListOf(
                        1,
                        3,
                        4,
                        7,
                        9,
                        11,
                        12,
                        13,
                        15,
                        18,
                        19,
                        21,
                        22,
                        25,
                        28,
                        30,
                        32,
                        35,
                        37,
                        40,
                        41,
                        45,
                        46,
                        48,
                        50,
                        52,
                        54,
                        56,
                        58,
                        60,
                        62,
                        63
                    ).contains(i)
                ) {
                    cipherKey = cipherKey.append(secretKey[i])
                }
                if (cipherKey.length == 32) {
                    break
                }
            }
        } else {
            if (secretKey.length > 100) {
                for (i in secretKey.indices) {
                    if (mutableListOf<Int>(
                            0,
                            1,
                            2,
                            3,
                            4,
                            5,
                            9,
                            11,
                            14,
                            15,
                            20,
                            21,
                            22,
                            25,
                            28,
                            30,
                            32,
                            35,
                            37,
                            40,
                            41,
                            45,
                            46,
                            48,
                            50,
                            60,
                            65,
                            75,
                            85,
                            95,
                            97,
                            99
                        ).contains(i)
                    ) {
                        cipherKey = cipherKey.append(secretKey[i])
                    }
                    if (cipherKey.length == 32) {
                        break
                    }
                }
            } else {
                for (i in secretKey.indices) {
                    if (mutableListOf<Int>(
                            0,
                            1,
                            2,
                            3,
                            4,
                            5,
                            9,
                            11,
                            14,
                            15,
                            20,
                            21,
                            22,
                            25,
                            28,
                            30,
                            32,
                            35,
                            37,
                            40,
                            41,
                            45,
                            46,
                            48,
                            50,
                            52,
                            54,
                            56,
                            58,
                            60,
                            62,
                            63
                        ).contains(i)
                    ) {
                        cipherKey = cipherKey.append(secretKey[i])
                    }
                    if (cipherKey.length == 32) {
                        break
                    }
                }
            }
        }
        return cipherKey.toString()
    }*/
    private fun constructIv(secretKey: String?): String? {
        /*val r = SecureRandom()
        val ivBytes = ByteArray(16)
        r.nextBytes(ivBytes)
        return String(ivBytes, StandardCharsets.UTF_8)*/
        var iv :String = ""
        for (i in 0 until  secretKey!!.length){
            if (mutableListOf<Int>(0, 1, 2, 10, 19, 20, 21, 22, 24, 26, 28, 30, 33, 36, 50, 63).contains(i)){
                iv+=secretKey[i]
            }
            if (iv.length==12){
                break
            }
        }
        return iv
    }
    private fun encryptE2E(securityKey: String, iv: String, plainText: String, salt: String?): String? {
        return try {
            val bytes = Base64.decode(
                securityKey.toByteArray(StandardCharsets.UTF_8),
                Base64.NO_WRAP
            )
            val originalKey: SecretKey = SecretKeySpec(bytes, "AES")
            val cipher: Cipher =
                Cipher.getInstance("AES/GCM/NoPadding", BouncyCastleProvider())
            val gcmParameterSpec =
                GCMParameterSpec(
                    16 * 8,
                    iv.toByteArray(StandardCharsets.UTF_8)
                )
            cipher.init(Cipher.ENCRYPT_MODE, originalKey,gcmParameterSpec)
            cipher.updateAAD(AAD_KEY.toByteArray(StandardCharsets.UTF_8))
            val ciphertext = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val ci: ByteArray = Arrays.copyOfRange(ciphertext, 0, ciphertext.size - 16)
            val tag: ByteArray = Arrays.copyOfRange(ciphertext, ciphertext.size - 16, ciphertext.size)
            val jsonObject = JSONObject()
            jsonObject.put("v", /*encrypt(*/iv)
            jsonObject.put("k", /*encrypt(*/bytesArrayToBase64(tag)!!)
            jsonObject.put("m", bytesArrayToBase64(ci))
            Log.e("enc===> "," before "+iv+"    "+bytesArrayToBase64(tag)+"    "+ci.size+"    "+bytesArrayToBase64(ci))
            Log.e("enc===> "," after "+jsonObject.optString("v")+"   "+jsonObject.optString("k")+"   "+jsonObject.optString("m"))
            //jsonObject.put("s", salt)
            Base64.encodeToString(
                jsonObject.toString().toByteArray(StandardCharsets.UTF_8),
                Base64.NO_WRAP
            )
        } catch (e: Exception) {
            e.printStackTrace()
            plainText
        }
    }
    fun decryptE2eMessage(plainText: String?, securityKey: String?): String? {
        try {
            val decoded = String(Base64.decode(plainText, Base64.NO_WRAP))
            var jsonObject = JSONObject()
            if (decoded.contains("{")) {
                jsonObject = Helper.stringToJsonObject(decoded)!!
            }else{
                return plainText
            }
            //val iv: String = decrypt(jsonObject!!.optString("v"))!!
            val iv: String = jsonObject!!.optString("v")!!
            val message = Base64.decode(jsonObject.optString("m").toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
            //val at: String = decrypt(jsonObject.optString("k"))!!
            val at: String = jsonObject.optString("k")!!
            //val s: String = decrypt(jsonObject.optString("s"))!!
            //var security = getKeyAndSalt(securityKey!!,s)


            val tag = Base64.decode(at.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
            val newBytes = ByteArray(message.size + tag.size)
            //val newBytes = ByteArray(message.size)
            for (i in newBytes.indices) {
                newBytes[i] = if (i < message.size) message[i] else tag[i - message.size]
            }
            val jsonObject1: JSONObject = getKeyAndSalt(securityKey!!,"")!!
            return decryptE2E(jsonObject1.optString("key"), iv, newBytes, plainText)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return plainText
        }
        return plainText
    }
    fun decryptE2E(
        securitykey: String,
        iv: String,
        cipherText: ByteArray?,
        plaintext: String?
    ): String? {
        return try {
            val bytes = Base64.decode(
                securitykey.toByteArray(StandardCharsets.UTF_8),
                Base64.NO_WRAP
            )
            val originalKey: SecretKey = SecretKeySpec(bytes, "AES")
            val cipher: Cipher =
                Cipher.getInstance("AES/GCM/NoPadding", BouncyCastleProvider())
            val gcmParameterSpec =
                GCMParameterSpec(
                    16 * 8,
                    iv.toByteArray(StandardCharsets.UTF_8)
                )
            //GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, cipherText.getBytes(StandardCharsets.UTF_8), 0, 16);
            cipher.init(Cipher.DECRYPT_MODE, originalKey,gcmParameterSpec)
            cipher.updateAAD(AAD_KEY.toByteArray(StandardCharsets.UTF_8))
            val plainText = cipher.doFinal(cipherText)
            String(plainText)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            plaintext
        }
    }
    private fun bytesArrayToBase64(cipherText: ByteArray?): String? {
        return Base64.encodeToString(cipherText, Base64.NO_WRAP)
    }
    fun encrypt(strToEncrypt: String): String? {
        try {
            var ivkey: String = IV
            val iv = ivkey.toByteArray(StandardCharsets.UTF_8)
            val ivs = IvParameterSpec(iv)
            var skey: String = skey
            val skeySpec = SecretKeySpec(skey.toByteArray(StandardCharsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivs)
            val encrypted = cipher.doFinal(strToEncrypt.toByteArray())
            return Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: java.lang.Exception) {
        }
        return null
    }

    fun decrypt(strToDecrypt: String?): String? {
        var ivkey: String = IV
        val iv = ivkey.toByteArray(StandardCharsets.UTF_8)
        try {
            val ivs = IvParameterSpec(iv)
            var skey: String = skey
            val skeySpec = SecretKeySpec(skey.toByteArray(StandardCharsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivs)
            val original = cipher.doFinal(Base64.decode(strToDecrypt, Base64.NO_WRAP))
            return String(original)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return null
    }
    private fun getKeyAndSalt(security: String,s:String): JSONObject? {
        try {
            var salt = ""
            /*if (salt.isNullOrEmpty()) {
               salt =  ConstantValues.generateRandomString(16)!! // use salt size at least as long as hash
            }*/
            val gen = PKCS5S2ParametersGenerator(SHA384Digest())
            gen.init(
                security.toByteArray(StandardCharsets.UTF_8),
                salt!!.toByteArray(StandardCharsets.UTF_8),
                PBKDF2_ITERATIONS
            )
            //val dk = (gen.generateDerivedParameters(256) as KeyParameter).key
            val dk = (gen.generateDerivedParameters(192) as KeyParameter).key
            val s: String = bytesArrayToBase64(dk)!!
            val `object` = JSONObject()
            `object`.put("salt", encrypt(salt))
            `object`.put("key", s)
            return `object`
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }
    fun parseMessage(jsonObject: JSONObject,userId:String) : JSONObject{
        try {
            if (Helper.isSeverMessage(jsonObject.optInt("message_type"))){
                return jsonObject
            }
            val entityId = if (jsonObject.optInt("is_group")==1) jsonObject.optString("receiver_id") else if (jsonObject.optString("sender_id")==APPLICATION_TM_LOGIN_USER_ID) jsonObject.optString("receiver_id") else jsonObject.optString("sender_id")
            val entityType = jsonObject.optInt("is_group")+1
            var referenceId : String = jsonObject.optString("reference_id")
            if (jsonObject.optInt("is_edited")==1){
                referenceId = ""
            }
            if (jsonObject.has("message") && !jsonObject.optString("message").isNullOrEmpty()){
                jsonObject.put("message", decryptMessage(EncryptionType.STANDARD,jsonObject.optString("message"),entityId,entityType,referenceId,jsonObject.optString("sender_id"),jsonObject.optString("receiver_id")))
            }
            if (jsonObject.has("attachment") && !jsonObject.optString("attachment").isNullOrEmpty()){
                jsonObject.put("attachment", decryptMessage(EncryptionType.STANDARD,jsonObject.optString("attachment"),entityId,entityType,referenceId,jsonObject.optString("sender_id"),jsonObject.optString("receiver_id")))
            }
            if (jsonObject.has("caption") && !jsonObject.optString("caption").isNullOrEmpty()){
                jsonObject.put("caption", decryptMessage(EncryptionType.STANDARD,jsonObject.optString("caption"),entityId,entityType,referenceId,jsonObject.optString("sender_id"),jsonObject.optString("receiver_id")))
            }
            if (jsonObject.has("is_reply") && jsonObject.optInt("is_reply")==1 && jsonObject.has("original_message")){
                var referenceId : String = jsonObject.optJSONObject("original_message")!!.optString("reference_id")
                if (jsonObject.optJSONObject("original_message")!!.optInt("is_edited")==1){
                    referenceId = ""
                }
                val replyEntityId = if (jsonObject.optInt("is_group")==1) jsonObject.optString("receiver_id") else if (jsonObject.optJSONObject("original_message")!!.optString("sender_id")==APPLICATION_TM_LOGIN_USER_ID) jsonObject.optJSONObject("original_message")!!.optString("receiver_id") else jsonObject.optJSONObject("original_message")!!.optString("sender_id")
                if (jsonObject.optJSONObject("original_message")!!.has("message") && !jsonObject.optJSONObject("original_message")!!.optString("message").isNullOrEmpty()){
                    jsonObject.optJSONObject("original_message")!!.put("message", decryptMessage(EncryptionType.STANDARD,jsonObject.optJSONObject("original_message")!!.optString("message"),replyEntityId,entityType,referenceId,jsonObject.optJSONObject("original_message")!!.optString("sender_id"),jsonObject.optJSONObject("original_message")!!.optString("receiver_id")))
                }
                if (jsonObject.optJSONObject("original_message")!!.has("attachment") && !jsonObject.optJSONObject("original_message")!!.optString("attachment").isNullOrEmpty()){
                    jsonObject.optJSONObject("original_message")!!.put("attachment", decryptMessage(EncryptionType.STANDARD,jsonObject.optJSONObject("original_message")!!.optString("attachment"),replyEntityId,entityType,referenceId,jsonObject.optJSONObject("original_message")!!.optString("sender_id"),jsonObject.optJSONObject("original_message")!!.optString("receiver_id")))
                }
                if (jsonObject.optJSONObject("original_message")!!.has("caption") && !jsonObject.optJSONObject("original_message")!!.optString("caption").isNullOrEmpty()){
                    jsonObject.optJSONObject("original_message")!!.put("caption", decryptMessage(EncryptionType.STANDARD,jsonObject.optJSONObject("original_message")!!.optString("caption"),replyEntityId,entityType,referenceId,jsonObject.optJSONObject("original_message")!!.optString("sender_id"),jsonObject.optJSONObject("original_message")!!.optString("receiver_id")))
                }
            }
            return  jsonObject
        }catch (e:Exception){
            return jsonObject
        }
    }

}