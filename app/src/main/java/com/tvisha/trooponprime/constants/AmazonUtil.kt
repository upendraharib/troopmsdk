package com.tvisha.trooponprime.constants

import android.content.Context
import android.net.Uri
import android.os.Build
import com.amazonaws.ClientConfiguration
import com.amazonaws.Protocol
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class AmazonUtil {
    companion object {
        // We only need one instance of the clients and credentials provider
        private var sS3Client: AmazonS3Client? = null
        private var sCredProvider: BasicSessionCredentials? = null
        private var sTransferUtility: TransferUtility? = null
        private var clientConfiguration: ClientConfiguration? = null
        private var context: Context? = null

        /**
         * Gets an instance of CognitoCachingCredentialsProvider which is
         * constructed using the given Context.
         *
         * //@param context An Context instance.
         * //@return A default credential provider.
         */
        fun getCredProvider(
            AccessKey: String?,
            SecretKey: String?,
            SessionToken: String?
        ): BasicSessionCredentials? {
            if (sCredProvider == null) {
                sCredProvider = BasicSessionCredentials(AccessKey, SecretKey, SessionToken)
            }
            return sCredProvider
        }

        fun getTheConfigaration(): ClientConfiguration? {
            val timeoutConnection = 60 * 1000
            if (clientConfiguration == null) {
                clientConfiguration = ClientConfiguration()
            }
            clientConfiguration = ClientConfiguration()
            clientConfiguration!!.maxErrorRetry = 1
            clientConfiguration!!.connectionTimeout = timeoutConnection
            clientConfiguration!!.socketTimeout = timeoutConnection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                clientConfiguration!!.protocol = Protocol.HTTPS
            } else {
                clientConfiguration!!.protocol = Protocol.HTTP
            }
            return clientConfiguration
        }

        /**
         * Gets an instance of a S3 client which is constructed using the given
         * Context.
         *
         * // @param context An Context instance.
         * //@return A default S3 client.
         */
        fun getS3Client(
            AccessKey: String?,
            SecretKey: String?,
            SessionToken: String?,
            Endpoint: String?,
            appcontext: Context?
        ): AmazonS3Client? {
            if (sS3Client == null) {
                sS3Client = AmazonS3Client(getCredProvider(AccessKey, SecretKey, SessionToken))
            }
            return sS3Client
        }

        /**
         * Gets an instance of the Transfer Utility which is constructed using the
         * given Context
         *
         * @param appcontext
         * @return a TransferUtility instance
         */
        fun getTransferUtility(
            AccessKey: String?,
            SecretKey: String?,
            appcontext: Context,
            SessionToken: String?,
            Endpoint: String?
        ): TransferUtility? {
            context = appcontext
            if (sTransferUtility == null) {
                sTransferUtility = TransferUtility(
                    getS3Client(AccessKey, SecretKey, SessionToken, Endpoint, appcontext),
                    appcontext.applicationContext
                )
            }
            return sTransferUtility
        }

        /**
         * Converts number of bytes into proper scale.
         *
         * @param bytes number of bytes to be converted.
         * @return A string that represents the bytes in a proper scale.
         */
        fun getBytesString(bytes: Long): String {
            val quantifiers = arrayOf(
                "KB", "MB", "GB", "TB"
            )
            var speedNum = bytes.toDouble()
            var i = 0
            while (true) {
                if (i >= quantifiers.size) {
                    return ""
                }
                speedNum /= 1024.0
                if (speedNum < 512) {
                    return String.format("%.2f", speedNum) + " " + quantifiers[i]
                }
                i++
            }
        }

        /**
         * Copies the data from the passed in Uri, to a new file for use with the
         * Transfer Service
         *
         * @param context
         * @param uri
         * @return
         * @throws IOException
         */
        @Throws(IOException::class)
        fun copyContentUriToFile(context: Context, uri: Uri?): File? {
            val `is` = context.contentResolver.openInputStream(uri!!)
            val copiedData = File(
                context.getDir("SampleImagesDir", Context.MODE_PRIVATE), UUID
                    .randomUUID().toString()
            )
            copiedData.createNewFile()
            val fos = FileOutputStream(copiedData)
            val buf = ByteArray(2046)
            var read = -1
            while (`is`!!.read(buf).also { read = it } != -1) {
                fos.write(buf, 0, read)
            }
            fos.flush()
            fos.close()
            return copiedData
        }

        /*
     * Fills in the map with information in the observer so that it can be used
     * with a SimpleAdapter to populate the UI
     */
        fun fillMap(
            map: MutableMap<String?, Any?>,
            observer: TransferObserver,
            isChecked: Boolean
        ) {
            val progress = (observer.bytesTransferred.toDouble() * 100 / observer
                .bytesTotal).toInt()
            map["id"] = observer.id
            map["checked"] = isChecked
            map["fileName"] = observer.absoluteFilePath
            map["progress"] = progress
            map["bytes"] = (getBytesString(observer.bytesTransferred) + "/"
                    + getBytesString(observer.bytesTotal))
            map["state"] = observer.state
            map["percentage"] = "$progress%"
        }
    }
}