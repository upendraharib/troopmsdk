package com.tvisha.trooponprime.lib.call

import android.opengl.GLES20
import android.os.AsyncTask
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.tvisha.trooponprime.lib.TroopClient
import com.tvisha.trooponprime.lib.TroopMessengerClient
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.callSocket
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.clientCallBackListener
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.usbVideoCapture
import com.tvisha.trooponprime.lib.socket.TroopSocketClient
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper
import com.tvisha.trooponprime.lib.utils.SocketEvents
import io.socket.client.Ack
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.mediasoup.droid.*
import org.webrtc.*

object RoomClient : SendTransport.Listener, Producer.Listener, RecvTransport.Listener,
    DataProducer.Listener {
    var CALLTYPE = 0
    var mMediasoupDevice: Device? = Device()
    var mSendTransport: SendTransport? = null
    var mRecvTransport: RecvTransport? = null
    var mRecvVidoeTransport: RecvTransport? = null
    var rootEglBase: EglBase? = null
    var audioConstraints: MediaConstraints? = null
    var videoConstraints: MediaConstraints? = null
    var videoSource: VideoSource? = null
    var localVideoTrack: VideoTrack? = null
    var remoteVideoTrack: VideoTrack? = null
    var audioSource: AudioSource? = null
    var localAudioTrack: AudioTrack? = null
    var videoCapturer: VideoCapturer? = null
    var remoteMediaStream: MediaStream? = null
    var localMediaStream: MediaStream? = null
    var peerConnectionFactory: PeerConnectionFactory? = null
    var AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression"
    var AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation"
    val VIDEO_TRACK_ID = "TroopIMv1"
    val SCREEN_SHARE_TRACK_ID = "TroopIMs1"
    private val LOCAL_STREAM_ID = "TroopIM"
    val AUDIO_TRACK_ID = "TroopIM1"
    val SCREEN_AUDIO_TRACK_ID = "TroopIMsa1"
    val SCREEN_SHARE_AUDIO_TRACK_ID = "TroopIMsa1"
    var producerLable: MutableMap<String, String> = HashMap()
    var mConsumers: MutableMap<String, Consumer?>? = HashMap()
    var WORKSPACEUSERID = ""
    var CALL_ID = ""
    var isvideostreaming = false
    var videoProducer: Producer? = null
    var audiProducer: Producer? = null
    var screenProducer: Producer? = null
    var screenAudioTrack: AudioTrack? = null

    var streamObject: JSONObject? = JSONObject()
    var consumerObject: JSONObject? = JSONObject()

    //VideoCapturer videoCapturerAndroid = null;
    var consumerObjectToClose: JSONObject? = JSONObject()
    var surfaceTextureHelper: SurfaceTextureHelper? = null

    var isAlreadyStarted = false
    var isVideoAdded = false
    var isAudioAdded = false
    var senderTAdded = false
    var recAdded = false
    var recVideoAdded = false
    var isProducersCalledFirstTime = false
    var addAudio = false
    var addVideo = false

    /*public MediaStream getLocalMediaStream(){
        return localMediaStream;
    }*/
    fun initialize(
        user_id: String,
        call_id: String,
        callType: Int,
        notCreated: Boolean,
        audio: Boolean,
        video: Boolean
    ) {
        Log.e(ConstantValues.TAG,"  client sdk init  "+user_id+"   "+call_id+"   "+callType+"    "+notCreated)
        WORKSPACEUSERID = user_id
        CALL_ID = call_id
        if (mMediasoupDevice == null) {
            mMediasoupDevice = Device()
        }
        producerLable = HashMap()
        mConsumers = HashMap()
        consumerObject = JSONObject()
        consumerObjectToClose = JSONObject()
        streamObject = JSONObject()
        isAlreadyStarted = true
        senderTAdded = false
        recAdded = false
        recVideoAdded = false
        isProducersCalledFirstTime = false
        addAudio = audio
        addVideo = video
        try {
            CALLTYPE = callType
            if (!addAudio && (CALLTYPE == ConstantValues.NewCallTypes.AUDIO_CALL || CALLTYPE == ConstantValues.NewCallTypes.VIDEO_CALL)) {
                addAudio = true
            }
            if (!addVideo && CALLTYPE == ConstantValues.NewCallTypes.VIDEO_CALL) {
                addAudio = true
                addVideo = true
            }
            val jsonObject = JSONObject()
            jsonObject.put("user_id", user_id)
            jsonObject.put("room_id", call_id)
            Log.e(ConstantValues.TAG," joinroom  "+(callSocket!!.connected())+"  "+(callSocket==null))
            if (callSocket != null && callSocket!!.connected()) {
                Log.e(ConstantValues.TAG," joinroom  "+jsonObject.toString())
                callSocket!!.emit(SocketEvents.JOIN_ROOM, jsonObject, Ack {
                    Log.e(ConstantValues.TAG," joinroom callback ")
                    getRtpCapability(notCreated) })
            } /*else {
                if (callSocket!!==null){
                    if (HandlerHolder.applicationHandler!=null){
                        HandlerHolder.applicationHandler.obtainMessage(Values.RecentList.CONNECT_TO_CALLSOCKET).sendToTarget();
                    }
                }else {
                    callSocket!!.connect();
                }
                initialize(user_id,call_id,callType,notCreated,audio,video);
            }*/
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    fun getRtpCapability(notCreated: Boolean) {
        callSocket!!
            .emit(SocketEvents.GETROUTERCAPABILITIES, JSONObject(),
                Ack { args ->
                    try {
                        val routeRtpCapabilities = args[0].toString()
                        Log.e(ConstantValues.TAG," router capabilities  "+routeRtpCapabilities.toString())
                        if (routeRtpCapabilities != null && !routeRtpCapabilities.trim { it <= ' ' }.isEmpty()) {
                            val decryptCapabilities: String =
                                Helper.cryptLibEncryptMessage(routeRtpCapabilities)
                            if (mMediasoupDevice != null && !mMediasoupDevice!!.isLoaded) {
                                try {
                                    Log.e(ConstantValues.TAG," load devices  ")
                                    mMediasoupDevice!!.load(decryptCapabilities, null)
                                } catch (e: Exception) {
                                    Helper.printExceptions(e)
                                }
                            }
                            /*if (HandlerHolder.callerView != null) {
                                HandlerHolder.callerView.obtainMessage(Values.RecentList.ADD_STREAMS)
                                    .sendToTarget()
                            }*/
                            createWebRtcTransPort(notCreated, false)
                        }
                    } catch (e: Exception) {
                        Helper.printExceptions(e)
                    }
                })
    }

    var createWebRtcTransport = JSONObject()


    fun createWebRtcTransPort(notCreated: Boolean, callPeerConnection: Boolean) {
        try {
            if (!isAlreadyStarted) {
                return
            }
            /*JSONObject object = new JSONObject();
            object.put("forceTcp", false);
            object.put("rtpCapabilities", mMediasoupDevice.getRtpCapabilities());*/
            //String encryptCreateTrnsPort= Helper.getInstance().cryptLibEncryptCall(object.toString());
            callSocket!!
                .emit(SocketEvents.CREATEWEBRTCTRANSPORT, JSONObject(),
                    Ack { args ->
                        try {
                            createWebRtcTransport = args[0] as JSONObject
                            //String transportData = (String) args[0];
                            //if (transportData!=null && !transportData.trim().isEmpty()) {
                            //String transportDecrypt = Helper.cryptLibEncryptMessage(transportData,transportData);
                            //createWebRtcTransport = Helper.stringToJsonObject(transportDecrypt);
                            if (createWebRtcTransport.has("error")) {
                                Handler(Looper.getMainLooper()).post { /*if (createWebRtcTransport.has("message")) {
                                                                        ToastUtil.getInstance().showToast(context, createWebRtcTransport.optString("message"));
                                                                    } else {
                                                                        ToastUtil.getInstance().showToast(context, createWebRtcTransport.optString("error"));
                                                                    }*/
                                }
                            } else {
                                createProducerTransport(
                                    createWebRtcTransport,
                                    callPeerConnection,
                                    notCreated
                                )
                                if (notCreated) {
                                    peerConnectionAndStream(notCreated)
                                }
                            }
                            //}
                        } catch (e: Exception) {
                            Helper.printExceptions(e)
                        }
                    })
            createReciverTransport(JSONObject())
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    fun createProducerTransport(
        jsonObject: JSONObject,
        callPeerConnection: Boolean,
        notCreated: Boolean
    ) {
        try {
            if (rootEglBase == null) {
                rootEglBase = EglBase.create()
            }
            if (!jsonObject.has("id")) {
                createWebRtcTransPort(false, callPeerConnection)
                return
            }
            val meObject = JSONObject()
            streamObject!!.put(WORKSPACEUSERID, meObject)
            val id = jsonObject.optString("id")
            val icParameters = jsonObject.optJSONObject("iceParameters").toString()
            val iceCandidates = jsonObject.optJSONArray("iceCandidates").toString()
            val dtlsParameters = jsonObject.optJSONObject("dtlsParameters").toString()
            if (mMediasoupDevice == null) {
                return
            }
            if (mMediasoupDevice!!.isLoaded) {
                mSendTransport = mMediasoupDevice!!.createSendTransport(
                    this,
                    id,
                    icParameters,
                    iceCandidates,
                    dtlsParameters
                )
            }
            if (callPeerConnection) {
                peerConnectionAndStream(notCreated)
            }
            /*if (notCreated && Helper.isAdded) {
                addScreenShare(RtcActivity.screenShareTrack, false)
            }*/
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    fun peerConnectionAndStream(notCreated: Boolean) {
        try {
            if (rootEglBase == null) {
                rootEglBase = EglBase.create()
            }
            if (mSendTransport == null) {
                createProducerTransport(createWebRtcTransport, true, notCreated)
                return
            }
            if (!streamObject!!.has(WORKSPACEUSERID)) {
                val meObject = JSONObject()
                streamObject!!.put(WORKSPACEUSERID, meObject)
            }
            //if (!notCreated) {
            if (peerConnectionFactory == null) {
                PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions
                        .builder(TroopMessengerClient.context)
                        .createInitializationOptions()
                )
                /*AudioDeviceModule audioDeviceModule = JavaAudioDeviceModule.builder(MessengerApplication.context)
                        .setUseHardwareAcousticEchoCanceler(true)
                        .setUseHardwareNoiseSuppressor(true)
                        .createAudioDeviceModule();*/

                /*WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
                WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(true);
                WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true);*/
                val options = PeerConnectionFactory.Options()
                val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(
                    rootEglBase!!.eglBaseContext, true, true
                )
                val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(
                    rootEglBase!!.eglBaseContext
                )
                peerConnectionFactory = PeerConnectionFactory.builder()
                    .setOptions(options) //.setAudioDeviceModule(audioDeviceModule)
                    .setVideoEncoderFactory(defaultVideoEncoderFactory)
                    .setVideoDecoderFactory(defaultVideoDecoderFactory)
                    .createPeerConnectionFactory()
            }
            val mediaConstraints = MediaConstraints()
            mediaConstraints.mandatory.add(
                MediaConstraints.KeyValuePair(
                    AUDIO_NOISE_SUPPRESSION_CONSTRAINT,
                    "true"
                )
            )
            mediaConstraints.mandatory.add(
                MediaConstraints.KeyValuePair(
                    AUDIO_ECHO_CANCELLATION_CONSTRAINT,
                    "true"
                )
            )
            if (audioSource == null) {
                audioSource = peerConnectionFactory!!.createAudioSource(mediaConstraints)
            }

            //}
            if ( /*CALLTYPE == ConstantValues.NewCallTypes.VIDEO_CALL || CALLTYPE == ConstantValues.NewCallTypes.AUDIO_CALL || isVideoAdded || isAudioAdded*/addAudio || addVideo) {
                //if (!notCreated) {
                var isEnable =
                    addAudio || addVideo /*CALLTYPE == ConstantValues.NewCallTypes.AUDIO_CALL || CALLTYPE == ConstantValues.NewCallTypes.VIDEO_CALL;*/
                //if (localAudioTrack==null) {
                if (localAudioTrack == null) {
                    localAudioTrack =
                        peerConnectionFactory!!.createAudioTrack(AUDIO_TRACK_ID, audioSource)
                }
                isAudioAdded = true
                /*CallService.Companion.getParticipantList()
                if (CallService.Companion.getParticipantList().size() > 0) {
                    for (i in 0 until CallService.Companion.getParticipantList().size()) {
                        if (CallService.Companion.getParticipantList().get(i).getUser_id()
                                .equals(WORKSPACEUSERID)
                        ) {
                            isEnable = CallService.Companion.getParticipantList().get(i)
                                .isAudioMuted() === 0
                            break
                        }
                    }
                }*/
                //}
                localAudioTrack!!.setEnabled(isEnable)
                Log.e(ConstantValues.TAG," track==> "+(localAudioTrack==null))
                //}
                val track = JSONObject()
                track.put("mediaType", "audioType")
                //audiProducer = mSendTransport.
                audiProducer = mSendTransport!!.produce(
                    this,
                    localAudioTrack,  /*bitrates*/
                    null,
                    null,
                    null,
                    track.toString()
                )
                //audiProducer = mSendTransport.produceData(this,"","",true,0,0,track.toString());
                val `object` = streamObject!!.optJSONObject(WORKSPACEUSERID)
                `object`.put("audio", localAudioTrack)
            }
            if (/*CALLTYPE == ConstantValues.NewCallTypes.VIDEO_CALL || isVideoAdded*/addVideo /* && localVideoTrack==null*/) {
                if (videoCapturer == null || localVideoTrack == null) {
                    surfaceTextureHelper =
                        SurfaceTextureHelper.create("CaptureThread", rootEglBase!!.eglBaseContext)
                    videoCapturer = createCameraCapturer(Camera1Enumerator())
                }
                if (videoCapturer != null) {
                    //if (!notCreated) {
                    videoSource =
                        peerConnectionFactory!!.createVideoSource( /*videoCapturerAndroid.isScreencast()*/
                            false
                        )
                    //}
                    if (videoSource != null) {
                        if ( /*CALLTYPE == ConstantValues.NewCallTypes.VIDEO_CALL || isVideoAdded*/addVideo) {
                            //if (!notCreated) {
                            if (localVideoTrack == null) {
                                localVideoTrack = peerConnectionFactory!!.createVideoTrack(
                                    VIDEO_TRACK_ID,
                                    videoSource
                                )
                                if (videoCapturer != null) {
                                    videoCapturer!!.initialize(
                                        surfaceTextureHelper,
                                        TroopMessengerClient.context, videoSource!!.capturerObserver
                                    )
                                    videoCapturer!!.startCapture( /*AppSocket.deviceHeight*/640,  /*AppSocket.deviceWidth*/
                                        360,
                                        15
                                    )
                                }
                            }
                            isVideoAdded = true
                            var isEnable =
                                addVideo //CALLTYPE == ConstantValues.NewCallTypes.VIDEO_CALL;
                            /*CallService.Companion.getParticipantList()
                            if (CallService.Companion.getParticipantList().size() > 0) {
                                for (i in 0 until CallService.Companion.getParticipantList()
                                    .size()) {
                                    if (CallService.Companion.getParticipantList().get(i)
                                            .getUser_id().equals(WORKSPACEUSERID)
                                    ) {
                                        isEnable = CallService.Companion.getParticipantList().get(i)
                                            .isVideoMuted() === 0
                                        break
                                    }
                                }
                            }*/
                            localVideoTrack!!.setEnabled(isEnable)
                            //}
                            val `object` = streamObject!!.optJSONObject(WORKSPACEUSERID)
                            `object`.put("video", localVideoTrack)
                            var codeType = JSONObject()
                            try {
                                val jsonObject = JSONObject(mMediasoupDevice!!.rtpCapabilities)
                                val array = jsonObject.optJSONArray("codecs")
                                for (i in 0 until array.length()) {
                                    if (array.optJSONObject(i)
                                            .optString("mimeType") == "video/VP8"
                                    ) {
                                        codeType = array.optJSONObject(i)
                                        /*codeType.put("videoGoogleStartBitrate",1000);
                                        codeType.put("videoGoogleMaxBitrate",50000);*/
                                    }
                                }
                            } catch (e: JSONException) {
                                Helper.printExceptions(e)
                            }
                            val track = JSONObject()
                            track.put("mediaType", "videoType")
                            videoProducer = mSendTransport!!.produce(
                                this,
                                localVideoTrack,
                                TroopMessengerClient.bitRates,
                                null,
                                codeType.toString(),
                                track.toString()
                            )
                            //videoProducer = mSendTransport.produce(this, localVideoTrack, null, codeType.toString());
                            producerLable["video"] = videoProducer!!.id
                        }
                    }
                }
            }
            if (clientCallBackListener!= null) {
                val streamObjet = JSONObject()
                streamObjet.put("user_id", WORKSPACEUSERID)
                streamObjet.put("stream_type", 1)
                streamObjet.put("stream", streamObject)
                clientCallBackListener.selfStream(streamObjet)
            }
            senderTAdded = true
            streamsAdded = true
            if (mRecvTransport != null && mRecvVidoeTransport != null) {
                getProducers(1)
            } else {
                Thread {
                    try {
                        Thread.sleep(2000)
                        getProducers(2)
                    } catch (e: Exception) {
                        Helper.printExceptions(e)
                    }
                }.start()
            }
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    fun createProduce() {}

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        //Camera.setDisplayOrientation(90);
        if (usbVideoCapture!=null){
            return usbVideoCapture
        }
        val deviceNames = enumerator.deviceNames
        // First, try to find front facing camera
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        // Front facing camera not found, try something else
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    private fun createVideoReciverTransport(transObject: JSONObject) {
        try {
            if (!isAlreadyStarted) {
                return
            }
            callSocket!!
                .emit(SocketEvents.CREATEWEBRTCTRANSPORT, JSONObject(),
                    Ack { args ->
                        try {
                            createWebRtcTransport = args[0] as JSONObject
                            //String transportData = (String) args[0];
                            //if (transportData!=null && !transportData.trim().isEmpty()) {
                            //String transportDecrypt = Helper.cryptLibEncryptMessage(transportData,transportData);
                            //createWebRtcTransport = Helper.stringToJsonObject(transportDecrypt);
                            if (createWebRtcTransport.has("error")) {
                                Handler(Looper.getMainLooper()).post { /*if (createWebRtcTransport.has("message")) {
                                                                        ToastUtil.getInstance().showToast(context, createWebRtcTransport.optString("message"));
                                                                    } else {
                                                                        ToastUtil.getInstance().showToast(context, createWebRtcTransport.optString("error"));
                                                                    }*/
                                }
                            } else {
                                try {
                                    val id = createWebRtcTransport.optString("id")
                                    val icParameters =
                                        createWebRtcTransport.optJSONObject("iceParameters")
                                            .toString()
                                    val iceCandidates =
                                        createWebRtcTransport.optJSONArray("iceCandidates")
                                            .toString()
                                    val dtlsParameters =
                                        createWebRtcTransport.optJSONObject("dtlsParameters")
                                            .toString()
                                    if (mMediasoupDevice == null) {
                                        return@Ack
                                    }
                                    mRecvVidoeTransport =
                                        mMediasoupDevice!!.createRecvTransport(object :
                                            RecvTransport.Listener {
                                            override fun onConnect(
                                                transport: Transport,
                                                dtlsParameters: String?
                                            ) {
                                                try {
                                                    val jsonObject = JSONObject()
                                                    jsonObject.put(
                                                        "dtlsParameters",
                                                        Helper.stringToJsonObject(
                                                            dtlsParameters
                                                        )
                                                    )
                                                    jsonObject.put("transport_id", transport.id)
                                                    //String connectTransportEncryprt = Helper.cryptLibEncryptMessage(jsonObject.toString(),jsonObject.toString());
                                                    callSocket!!
                                                        .emit(SocketEvents.CONNECTTRANSPORT,
                                                            jsonObject,
                                                            Ack {
                                                                //getProducers();
                                                            })
                                                } catch (e: Exception) {
                                                    Helper.printExceptions(e)
                                                }
                                            }

                                            override fun onConnectionStateChange(
                                                transport: Transport?,
                                                connectionState: String?
                                            ) {
                                            }
                                        }, id, icParameters, iceCandidates, dtlsParameters, null)
                                    recVideoAdded = true
                                    /*if (isProducersCalledFirstTime) {
                                                    getProducers();
                                                }*/
                                } catch (e: Exception) {
                                    selectedCount = 1
                                    Helper.printExceptions(e)
                                }
                            }
                            //}
                        } catch (e: Exception) {
                            Helper.printExceptions(e)
                        }
                    })
            //}
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    private fun createReciverTransport(transObject: JSONObject) {
        try {
            if (!isAlreadyStarted) {
                return
            }
            callSocket!!
                .emit(SocketEvents.CREATEWEBRTCTRANSPORT, JSONObject(),
                    Ack { args ->
                        try {
                            val createWebRtcTransport = args[0] as JSONObject
                            //String transportData = (String) args[0];
                            //if (transportData != null && !transportData.trim().isEmpty()) {
                            //String transportDecrypt = Helper.cryptLibEncryptMessage(transportData,transportData);
                            //JSONObject createWebRtcTransport = Helper.stringToJsonObject(transportDecrypt);
                            if (createWebRtcTransport.has("error")) {
                                Handler(Looper.getMainLooper()).post { /*if (createWebRtcTransport.has("message")) {
                                                                        ToastUtil.getInstance().showToast(context, createWebRtcTransport.optString("message"));
                                                                    } else {
                                                                        ToastUtil.getInstance().showToast(context, createWebRtcTransport.optString("error"));
                                                                    }*/
                                }
                            } else {
                                try {
                                    val id = createWebRtcTransport.optString("id")
                                    val icParameters =
                                        createWebRtcTransport.optJSONObject("iceParameters")
                                            .toString()
                                    val iceCandidates =
                                        createWebRtcTransport.optJSONArray("iceCandidates")
                                            .toString()
                                    val dtlsParameters =
                                        createWebRtcTransport.optJSONObject("dtlsParameters")
                                            .toString()
                                    if (mMediasoupDevice == null) {
                                        return@Ack
                                    }
                                    mRecvTransport = mMediasoupDevice!!.createRecvTransport(object :
                                        RecvTransport.Listener {
                                        override fun onConnect(
                                            transport: Transport,
                                            dtlsParameters: String?
                                        ) {
                                            try {
                                                val jsonObject = JSONObject()
                                                jsonObject.put(
                                                    "dtlsParameters",
                                                    Helper.stringToJsonObject(
                                                        dtlsParameters
                                                    )
                                                )
                                                jsonObject.put("transport_id", transport.id)
                                                //String connectTransportEncryprt = Helper.cryptLibEncryptMessage(jsonObject.toString(),jsonObject.toString());
                                                callSocket!!.emit(
                                                    SocketEvents.CONNECTTRANSPORT,
                                                    jsonObject,
                                                    Ack { })
                                            } catch (e: Exception) {
                                                Helper.printExceptions(e)
                                            }
                                        }

                                        override fun onConnectionStateChange(
                                            transport: Transport?,
                                            connectionState: String?
                                        ) {
                                        }
                                    }, id, icParameters, iceCandidates, dtlsParameters, null)
                                    recAdded = true
                                } catch (e: Exception) {
                                    selectedCount = 1
                                    Helper.printExceptions(e)
                                }
                            }
                            //}
                        } catch (e: Exception) {
                            Helper.printExceptions(e)
                        }
                    })
            createVideoReciverTransport(transObject)
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    fun checkSTATUS(transport: Transport?, connectionStatus: String?) {
        /*if (mConsumers.size() > 0) {
            Iterator myVeryOwnIterator = mConsumers.keySet().iterator();
            while (myVeryOwnIterator.hasNext()) {
                String key = (String) myVeryOwnIterator.next();
                if (mConsumers.get(key) != null) {
                    try {
                    } catch (MediasoupException e) {
                        Helper.printExceptions(e);
                    }
                }
                //String value=(String)meMap.get(key);
            }
        }*/
    }

    fun createCameraCapturer(isFront: Boolean): VideoCapturer? {
        val enumerator = Camera1Enumerator(false)
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (if (isFront) enumerator.isFrontFacing(deviceName) else enumerator.isBackFacing(
                    deviceName
                )
            ) {
                videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    fun release(dontRemove: Boolean) {
        Log.e(ConstantValues.TAG,"  release  "+dontRemove)
        if (!isAlreadyStarted) {
            return
        }
        try {
            isAlreadyStarted = false
            isProducersCalledFirstTime = false
            isAudioAdded = false
            isVideoAdded = false
            senderTAdded = false
            recVideoAdded = false
            //Helper.isRinging = false;
            recAdded = false
            //Helper.iscallPreview = false;
            if (!dontRemove) {
                isvideostreaming = false
            }
            createWebRtcTransport = JSONObject()
            if (timer != null) {
                timer!!.cancel()
                timer = null
            }
            //if (!dontRemove) {
            Handler(Looper.getMainLooper()).post {
                try {
                    if (!dontRemove) {
                        if (videoCapturer != null) {
                            videoCapturer!!.stopCapture()
                            videoCapturer!!.dispose()
                            videoCapturer = null
                        }
                        if (audioSource != null) {
                            audioSource!!.dispose()
                            audioSource = null
                        }
                        if (localAudioTrack != null && localAudioTrack!!.state() == MediaStreamTrack.State.LIVE) {
                            localAudioTrack!!.dispose()
                            localAudioTrack = null
                        }
                        if (videoSource != null) {
                            videoSource!!.dispose()
                            videoSource = null
                        }
                        if (localVideoTrack != null) {
                            localVideoTrack!!.dispose()
                            localVideoTrack = null
                        }
                        streamsAdded = false
                        addAudio = false
                        addVideo = false
                    }
                } catch (e: Exception) {
                    Helper.printExceptions(e)
                }
                if (mSendTransport != null) {
                    try {
                        mSendTransport!!.dispose()
                    } catch (e: Exception) {
                        Helper.printExceptions(e)
                    }
                    mSendTransport = null
                }
                if (mRecvTransport != null) {
                    try {
                        mRecvTransport!!.dispose()
                    } catch (e: Exception) {
                        Helper.printExceptions(e)
                    }
                    mRecvTransport = null
                }
                if (mRecvVidoeTransport != null) {
                    try {
                        mRecvVidoeTransport!!.dispose()
                    } catch (e: Exception) {
                        Helper.printExceptions(e)
                    }
                    mRecvVidoeTransport = null
                }
                if (mConsumers != null && mConsumers!!.size > 0) {
                    /*for (int i = 0;i<mConsumers.size();i++){
                                if (!Objects.requireNonNull(mConsumers.get(i)).isClosed()) {
                                    Objects.requireNonNull(mConsumers.get(i)).close();
                                }
                            }*/
                    mConsumers!!.clear()
                    mConsumers = null
                }
                if (consumerObject != null) {
                    consumerObject = JSONObject()
                }
                if (consumerObjectToClose != null) {
                    consumerObjectToClose = JSONObject()
                }
                if (streamObject != null) {
                    if (dontRemove) {
                        if (streamObject!!.has(WORKSPACEUSERID)) {
                            try {
                                val jsonObject = streamObject!!.optJSONObject(WORKSPACEUSERID)
                                jsonObject.put(WORKSPACEUSERID, jsonObject)
                                streamObject = JSONObject()
                                streamObject!!.put(WORKSPACEUSERID, jsonObject)
                            } catch (e: Exception) {
                                Helper.printExceptions(e)
                            }
                        } else {
                            streamObject = JSONObject()
                        }
                    } else {
                        streamObject = JSONObject()
                    }
                }
                if (!dontRemove && rootEglBase != null) {
                    try {
                        if (mMediasoupDevice != null) {
                            mMediasoupDevice!!.dispose()
                            mMediasoupDevice = null
                        }
                        if (peerConnectionFactory != null) {
                            peerConnectionFactory!!.dispose()
                            peerConnectionFactory = null
                        }
                        rootEglBase!!.releaseSurface()
                        GLES20.glClearColor(0f, 0f, 0f, 0f)
                        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                        rootEglBase!!.release()
                        rootEglBase = null
                    } catch (e: Exception) {
                        Helper.printExceptions(e)
                    }
                }
                if (dontRemove) {
                    initialize(
                        WORKSPACEUSERID,
                        CALL_ID, CALLTYPE, true, addAudio, addVideo
                    )
                }
            }
            //}
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    override fun onProduce(
        transport: Transport,
        kind: String?,
        rtpParameters: String?,
        appData: String?
    ): String? {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("producerTransportId", transport.id)
            jsonObject.put("kind", kind)
            jsonObject.put("rtpParameters", Helper.stringToJsonObject(rtpParameters))
            jsonObject.put("user_id", WORKSPACEUSERID)
            val a: JSONObject = Helper.stringToJsonObject(appData)!!
            jsonObject.put("mediaType", a.optString("mediaType"))
            //String consumeEncrypt = Helper.cryptLibEncryptMessage(jsonObject.toString(),jsonObject.toString());
            callSocket!!.emit(SocketEvents.PRODUCE, jsonObject,
                Ack { args ->
                    if (args != null && args.size > 0) {
                    }
                })
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
        return transport.id
        /*if (producerLable!=null && producerLable.size()>0) {
            if (producerLable.get(kind)!=null){

            }


            return producerLable.get(kind);
        }else {
            return "";
        }*/
    }

    override fun onProduceData(
        transport: Transport?,
        sctpStreamParameters: String?,
        label: String?,
        protocol: String?,
        appData: String?
    ): String? {
        return null
    }

    /*@Override
    public String onProduceData(Transport transport, String sctpStreamParameters, String label, String protocol, String appData) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("producerTransportId", transport.getId());
            jsonObject.put("kind", label);
            jsonObject.put("rtpParameters", Helper.stringToJsonObject(sctpStreamParameters));
            jsonObject.put("user_id", WORKSPACEUSERID);
            JSONObject a = Helper.stringToJsonObject(appData);
            jsonObject.put("mediaType", a.optString("mediaType"));
            //String consumeEncrypt = Helper.cryptLibEncryptMessage(jsonObject.toString(),jsonObject.toString());
            callSocket!!.emit(SocketEvents.PRODUCE, jsonObject, new Ack() {
                @Override
                public void call(Object... args) {
                    if (args != null && args.length > 0) {

                    }
                }
            });
        } catch (Exception e) {
            Helper.printExceptions(e);
        }
        return transport.getId();
    }*/

    /*@Override
    public String onProduceData(Transport transport, String sctpStreamParameters, String label, String protocol, String appData) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("producerTransportId", transport.getId());
            jsonObject.put("kind", label);
            jsonObject.put("rtpParameters", Helper.stringToJsonObject(sctpStreamParameters));
            jsonObject.put("user_id", WORKSPACEUSERID);
            JSONObject a = Helper.stringToJsonObject(appData);
            jsonObject.put("mediaType", a.optString("mediaType"));
            //String consumeEncrypt = Helper.cryptLibEncryptMessage(jsonObject.toString(),jsonObject.toString());
            callSocket!!.emit(SocketEvents.PRODUCE, jsonObject, new Ack() {
                @Override
                public void call(Object... args) {
                    if (args != null && args.length > 0) {

                    }
                }
            });
        } catch (Exception e) {
            Helper.printExceptions(e);
        }
        return transport.getId();
    }*/
    override fun onConnect(transport: Transport, dtlsParameters: String?) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("dtlsParameters", Helper.stringToJsonObject(dtlsParameters))
            jsonObject.put("transport_id", transport.id)
            //String connectTransportEncryprt = Helper.cryptLibEncryptMessage(jsonObject.toString(),jsonObject.toString());
            callSocket!!
                .emit(SocketEvents.CONNECTTRANSPORT, jsonObject,
                    Ack { })
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    var timer: CountDownTimer? = null

    override fun onConnectionStateChange(transport: Transport?, connectionState: String) {
        if (connectionState == "disconnected") {
            /*if (HandlerHolder.callerView != null) {
                HandlerHolder.callerView.obtainMessage(Values.VideoCall.CONNECTION_DISCONNECTED).sendToTarget();
            }*/
        } else if (connectionState == "connected" || connectionState == "completed") {
            /*if (HandlerHolder.callerView != null) {
                HandlerHolder.callerView.obtainMessage(Values.VideoCall.CONNECTION_CONNECTED).sendToTarget();
            }*/
        }
        if (connectionState == "failed") {
            if (callSocket!! != null && callSocket!!
                    .connected()
            ) {
                Handler(Looper.getMainLooper()).post { setTheTimer() }

                /*if (HandlerHolder.callerView!=null){
                HandlerHolder.callerView.obtainMessage(Values.VideoCall.SOCKET_CONNECT).sendToTarget();
            }*/
            } else if (callSocket!! != null) {
                callSocket!!.connect()
            }
        } else {
            if (timer != null) {
                timer!!.cancel()
                timer = null
            }
        }
    }

    private fun setTheTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        timer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                //here you can have your logic to set text to edittext
            }

            override fun onFinish() {
                val audio = isAudioAdded
                val video = isVideoAdded
                release(true)
                isVideoAdded = video
                isAudioAdded = audio
                //initialize(WORKSPACEUSERID, CALL_ID, CALLTYPE, true);
            }
        }.start()
    }

    override fun onTransportClose(producer: Producer?) {}

    /*public String addProducer(JSONObject data){

    }*/
    var selectedCount = 0
    var calledCount = 0
    var thread: Thread? = null
    var streamsAdded = false

    @Synchronized
    fun newProducer(array: JSONArray?) {
        try {
            isProducersCalledFirstTime = true
            //if (encrypt!=null && !encrypt.trim().isEmpty()) {
            //String decrypt = Helper.cryptLibEncryptMessage(encrypt,encrypt);
            //JSONArray array = Helper.stringToJsonArray(decrypt);
            if (recAdded && recVideoAdded && streamsAdded) {
                selectedCount = 0
                Log.e(ConstantValues.TAG," new "+array!!.length())
                if (array != null && array.length() > 0) {
                    for (i in 0 until array.length()) {
                        val data = array.optJSONObject(i)
                        if (data.optString("user_id") != WORKSPACEUSERID) {
                            ConsumerCreate(data, array).execute()
                        }
                    }
                } else {
                    Log.e(ConstantValues.TAG," call new producers " )
                    getProducers(3)
                }
            }
            //}
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    private fun videoConsumed(callBackObject: JSONObject, data: JSONObject) {
        try {
            if (mRecvVidoeTransport == null) {
                createVideoReciverTransport(createWebRtcTransport)
            } else {
                val consumer = mRecvVidoeTransport!!.consume(
                    { c: Consumer ->
                        if (mConsumers != null && mConsumers!!.size > 0 && mConsumers!![c.id] != null) {
                            mConsumers!!.remove(c.id)
                        }
                    },
                    callBackObject.optString("id"),
                    callBackObject.optString("producerId"),
                    callBackObject.optString("kind"),
                    callBackObject.optJSONObject("rtpParameters").toString(),
                    data.optString("appData")
                )

                mConsumers!![consumer.id] = consumer
                if (!isvideostreaming) {
                    isvideostreaming = true
                }
                var `object` = JSONObject()
                if (consumerObject!!.has(data.optString("user_id"))) {
                    `object` = consumerObject!!.optJSONObject(data.optString("user_id"))
                }
                `object`.put(callBackObject.optString("kind"), consumer)
                `object`.put("consumer_id", consumer.id)
                `object`.put("mediaType", data.optJSONObject("appData").optString("mediaType"))
                consumerObject!!.put(data.optString("user_id"), `object`)
                consumerObjectToClose!!.put(
                    consumer.id,
                    data.optJSONObject("appData").optString("mediaType")
                )
                var jsonObject1 = JSONObject()
                if (data.optString("user_id") != WORKSPACEUSERID) {
                    if (streamObject!!.has(data.optString("user_id"))) {
                        jsonObject1 = streamObject!!.optJSONObject(data.optString("user_id"))
                    }
                    var kind = consumer.track.kind()
                    if (kind == "video" && data.optJSONObject("appData").has("mediaType")) {
                        kind = data.optJSONObject("appData").optString("mediaType")
                    }
                    if (kind == "videoType" || kind == "video") {
                        val videoTrack = consumer.track as VideoTrack
                        jsonObject1.put("video", videoTrack)
                        //mediaStream.addTrack(videoTrack);
                    } else if (kind == "audioType" || kind == "audio") {
                        val audioTrack = consumer.track as AudioTrack
                        jsonObject1.put("audio", audioTrack)
                        //mediaStream.addTrack(audioTrack);
                    } else if (kind == "screen" || kind == "screenType") {
                        val screenTrack = consumer.track as VideoTrack
                        jsonObject1.put("screen", screenTrack)
                    }
                    streamObject!!.put(data.optString("user_id"), jsonObject1)
                    /*if (HandlerHolder.callerView != null) {
                        val streamObjet = JSONObject()
                        streamObjet.put("user_id", data.optString("user_id"))
                        streamObjet.put(
                            "stream_type",
                            data.optJSONObject("appData").optString("mediaType")
                        )
                        HandlerHolder.callerView.obtainMessage(
                            Values.VideoCall.UPDATE_STREAMS,
                            streamObjet
                        ).sendToTarget()
                    }*/
                    if (clientCallBackListener!= null) {
                        val streamObjet = JSONObject()
                        //streamObjet.put("user_id", TroopSocketClient.getUid(data.optString("user_id")))
                        streamObjet.put("user_id", data.optString("user_id"))
                        streamObjet.put("stream_type", data.optJSONObject("appData")!!.optString("mediaType"))
                        streamObjet.put("stream", streamObject)

                        Log.e(ConstantValues.TAG," video 1 ")
                        clientCallBackListener.userStream(streamObjet)
                    }
                }
            }
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    fun audioConsume(callBackObject: JSONObject, data: JSONObject) {
        try {
            if (mRecvTransport == null) {
                createReciverTransport(createWebRtcTransport)
            } else {
                val consumer = mRecvTransport!!.consume(
                    { c: Consumer ->
                        if (mConsumers != null && mConsumers!!.size > 0 && mConsumers!![c.id] != null) {
                            mConsumers!!.remove(c.id)
                        }
                    },
                    callBackObject.optString("id"),
                    callBackObject.optString("producerId"),
                    callBackObject.optString("kind"),
                    callBackObject.optJSONObject("rtpParameters").toString(),
                    data.optString("appData")
                )
                mConsumers!![consumer.id] = consumer
                if (!isvideostreaming) {
                    isvideostreaming = true
                }
                var `object` = JSONObject()
                if (consumerObject!!.has(data.optString("user_id"))) {
                    `object` = consumerObject!!.optJSONObject(data.optString("user_id"))
                }
                `object`.put(callBackObject.optString("kind"), consumer)
                `object`.put("consumer_id", consumer.id)
                `object`.put("mediaType", data.optJSONObject("appData").optString("mediaType"))
                consumerObject!!.put(data.optString("user_id"), `object`)
                consumerObjectToClose!!.put(
                    consumer.id,
                    data.optJSONObject("appData").optString("mediaType")
                )
                var jsonObject1 = JSONObject()
                if (data.optString("user_id") != WORKSPACEUSERID) {
                    if (streamObject!!.has(data.optString("user_id"))) {
                        jsonObject1 = streamObject!!.optJSONObject(data.optString("user_id"))
                    }
                    var kind = consumer.track.kind()
                    if (kind == "video") {
                        kind = data.optJSONObject("appData").optString("mediaType")
                    }
                    if (kind == "videoType" || kind == "video") {
                        val videoTrack = consumer.track as VideoTrack
                        jsonObject1.put("video", videoTrack)
                    } else if (kind == "audioType" || kind == "audio") {
                        val audioTrack = consumer.track as AudioTrack
                        jsonObject1.put("audio", audioTrack)
                        //mediaStream.addTrack(audioTrack);
                    } else if (kind == "screen" || kind == "screenType") {
                        val screenTrack = consumer.track as VideoTrack
                        jsonObject1.put("screen", screenTrack)
                    }
                    streamObject!!.put(data.optString("user_id"), jsonObject1)
                    /*if (HandlerHolder.callerView != null) {
                        val streamObjet = JSONObject()
                        streamObjet.put("user_id", data.optString("user_id"))
                        streamObjet.put(
                            "stream_type",
                            data.optJSONObject("appData").optString("mediaType")
                        )
                        HandlerHolder.callerView.obtainMessage(
                            Values.VideoCall.UPDATE_STREAMS,
                            streamObjet
                        ).sendToTarget()
                    }*/
                    if (clientCallBackListener!= null) {
                        val streamObjet = JSONObject()
                        //streamObjet.put("user_id", TroopSocketClient.getUid(data.optString("user_id")))
                        streamObjet.put("user_id", data.optString("user_id"))
                        streamObjet.put("stream_type", data.optJSONObject("appData")!!.optString("mediaType"))
                        streamObjet.put("stream", streamObject)
                        Log.e(ConstantValues.TAG," video 2 ")
                        clientCallBackListener.userStream(streamObjet)
                    }
                }
            }
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }
    fun consumerClosed(arg: JSONObject) {
        /*if (mConsumers != null && mConsumers.size() > 0 && isAlreadyStarted) {
            if (mConsumers.get(arg.optString("consumer_id")) != null) {
                Consumer consumer = mConsumers.get(arg.optString("consumer_id"));
                if (consumer!=null ) {
                    if (!consumer.isClosed() &&  isAlreadyStarted) {
                        consumer.close();
                        //consumer.getTrack().dispose();
                    } else {
                    }
                }
                mConsumers.remove(arg.optString("consumer_id"));
            }
        }*/
        if (consumerObjectToClose != null) {
            val iterator = consumerObjectToClose!!.keys()
            while (iterator.hasNext()) {
                val key = iterator.next()
                if (key.trim { it <= ' ' } == arg.optString("consumer_id")) {
                    /*if (HandlerHolder.callerView != null) {
                        try {
                            val jsonObject1 = JSONObject()
                            jsonObject1.put(
                                "streamType",
                                if (consumerObjectToClose!!.optString(key)
                                        .contains("screen")
                                ) 2 else 1
                            )
                            jsonObject1.put("connection", "closed")
                            jsonObject1.put("user_id", arg.optString("user_id"))
                            HandlerHolder.callerView.obtainMessage(
                                Values.VideoCall.USER_CONNECTION_STATUS,
                                jsonObject1
                            ).sendToTarget()
                        } catch (e: Exception) {
                            Helper.printExceptions(e)
                        }
                    }*/
                    break
                }
            }
        }
    }

    fun getMediaStreamObject(): JSONObject? {
        return streamObject
    }

    fun getProducers(num: Int) {
        callSocket!!.emit(SocketEvents.GETPRODUCERS) /*, new JSONObject(), new Ack() {
            @Override
            public void call(Object... args) {
                newProducer((JSONArray) args[0]);
            }
        });*/
    }

    fun addVideoStream() {
        try {
            if ( /*CALLTYPE == ConstantValues.NewCallTypes.VIDEO_CALL*/addVideo && peerConnectionFactory != null) {
                surfaceTextureHelper =
                    SurfaceTextureHelper.create("CaptureThread", rootEglBase!!.eglBaseContext)
                videoCapturer = createCameraCapturer(Camera1Enumerator())
                if (videoCapturer != null) {
                    //if (!notCreated) {
                    videoSource =
                        peerConnectionFactory!!.createVideoSource( /*videoCapturerAndroid.isScreencast()*/
                            false
                        )
                    //}
                    if (videoSource != null) {
                        if ( /*CALLTYPE == ConstantValues.NewCallTypes.VIDEO_CALL || isVideoAdded*/addVideo) {
                            //if (!notCreated) {
                            localVideoTrack = peerConnectionFactory!!.createVideoTrack(
                                VIDEO_TRACK_ID,
                                videoSource
                            )
                            isVideoAdded = true
                            if (videoCapturer != null) {
                                videoCapturer!!.initialize(
                                    surfaceTextureHelper,
                                    TroopMessengerClient.context, videoSource!!.capturerObserver
                                )
                                videoCapturer!!.startCapture( /*AppSocket.deviceHeight*/640,  /*AppSocket.deviceWidth*/
                                    360,
                                    15
                                )
                            }
                            var isEnable =
                                addVideo //CALLTYPE == ConstantValues.NewCallTypes.VIDEO_CALL;
                            /*if (CallService.Companion.getParticipantList() != null && CallService.Companion.getParticipantList()
                                    .size() > 0
                            ) {
                                for (i in 0 until CallService.Companion.getParticipantList()
                                    .size()) {
                                    if (java.lang.String.valueOf(
                                            CallService.Companion.getParticipantList().get(i)
                                                .getUser_id()
                                        ) == WORKSPACEUSERID
                                    ) {
                                        isEnable = CallService.Companion.getParticipantList().get(i)
                                            .isVideoMuted() === 0
                                        break
                                    }
                                }
                            }*/
                            localVideoTrack!!.setEnabled(isEnable)
                            //}
                            val `object` = streamObject!!.optJSONObject(WORKSPACEUSERID)
                            `object`.put("video", localVideoTrack)
                            var codeType = JSONObject()
                            try {
                                val jsonObject = JSONObject(mMediasoupDevice!!.rtpCapabilities)
                                val array = jsonObject.optJSONArray("codecs")
                                for (i in 0 until array.length()) {
                                    if (array.optJSONObject(i)
                                            .optString("mimeType") == "video/VP8"
                                    ) {
                                        codeType = array.optJSONObject(i)
                                        /*codeType.put("videoGoogleStartBitrate",1000);
                                        codeType.put("videoGoogleMaxBitrate",50000);*/
                                    }
                                }
                            } catch (e: JSONException) {
                                Helper.printExceptions(e)
                            }
                            val track = JSONObject()
                            track.put("mediaType", "videoType")
                            videoProducer = mSendTransport!!.produce(
                                this,
                                localVideoTrack,
                                TroopMessengerClient.bitRates,
                                null,
                                codeType.toString(),
                                track.toString()
                            )
                            //videoProducer = mSendTransport.produce(this, localVideoTrack, null, codeType.toString());
                            producerLable["video"] = videoProducer!!.getId()
                        }
                    }
                }
                /*if (HandlerHolder.callerView != null) {
                    val streamObjet = JSONObject()
                    streamObjet.put("user_id", WORKSPACEUSERID)
                    streamObjet.put("stream_type", 1)
                    HandlerHolder.callerView.obtainMessage(
                        Values.VideoCall.UPDATE_STREAMS,
                        streamObjet
                    ).sendToTarget()
                }*/
                if (clientCallBackListener!= null) {
                    val streamObjet = JSONObject()
                    streamObjet.put("stream_type", 1)
                    streamObjet.put("stream", streamObject)
                    clientCallBackListener.selfStream(streamObjet)
                }
                if (localAudioTrack == null) {
                    addAudioStream()
                }
            }
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    fun addAudioStream() {
        try {
            if (peerConnectionFactory != null && audioSource != null && ( /*CALLTYPE == ConstantValues.NewCallTypes.VIDEO_CALL || CALLTYPE == ConstantValues.NewCallTypes.AUDIO_CALL)*/addAudio || addVideo)) {
                localAudioTrack =
                    peerConnectionFactory!!.createAudioTrack(AUDIO_TRACK_ID, audioSource)
                if (localAudioTrack != null && mSendTransport != null) {
                    if (streamObject == null) {
                        streamObject = JSONObject()
                        val jsonObject = JSONObject()
                        streamObject!!.put(WORKSPACEUSERID, jsonObject)
                    }
                    var `object` = streamObject!!.optJSONObject(WORKSPACEUSERID)
                    if (`object` == null) {
                        `object` = JSONObject()
                        streamObject!!.put(WORKSPACEUSERID, `object`)
                    }
                    `object`.put("audio", localAudioTrack)
                    val track = JSONObject()
                    track.put("mediaType", "audioType")
                    audiProducer = mSendTransport!!.produce(
                        this,
                        localAudioTrack,
                        null,  /*codeType.toString()*/
                        null,
                        null,
                        track.toString()
                    )
                    isAudioAdded = true
                    localAudioTrack!!.setEnabled(true)
                }
            }
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    fun setCallType(callType: Int) {
        CALLTYPE = callType
        if (callType == ConstantValues.NewCallTypes.VIDEO_CALL) {
            addVideo = true
            addAudio = true
        } else if (callType == ConstantValues.NewCallTypes.AUDIO_CALL) {
            addAudio = true
        }
    }

    fun setAudioMute(audioMute: Boolean) {
        if (localAudioTrack != null) {
            localAudioTrack!!.setEnabled(audioMute)
        }
    }

    fun setVideoMute(videoMute: Boolean) {
        if (localVideoTrack != null) {
            localVideoTrack!!.setEnabled(videoMute)
        }
    }

    fun switchCamera() {
        try {
            if (localVideoTrack != null) {
                if (localVideoTrack!!.enabled()) {
                    val cameraVideoCapturer = videoCapturer as CameraVideoCapturer?
                    cameraVideoCapturer?.switchCamera(null)
                }
            }
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    fun addScreenShare(screenShareTrack: VideoTrack?, emit: Boolean) {
        try {
            if (emit) {
                val `object` = JSONObject()
                `object`.put(
                    "call_id",
                    CALL_ID
                )
                `object`.put("call_type", ConstantValues.NewCallTypes.SCREEN_SHARE)
                `object`.put("user_id", WORKSPACEUSERID)
                callSocket!!.emit(SocketEvents.CALL_STREAM_REQUEST, `object`,
                    Ack { })
            }
            //CallService.Companion.setJointlyCodeVisible(false)
            val track = JSONObject()
            track.put("mediaType", "screenType")
            screenProducer =
                mSendTransport!!.produce(this, screenShareTrack, null, null, null, track.toString())
        } catch (e: Exception) {
            Helper.printExceptions(e)
        }
    }

    override fun onOpen(dataProducer: DataProducer?) {}

    override fun onClose(dataProducer: DataProducer?) {}

    override fun onBufferedAmountChange(dataProducer: DataProducer?, sentDataSize: Long) {}

    override fun onTransportClose(dataProducer: DataProducer?) {}

    class ConsumerCreate(data: JSONObject, array: JSONArray) :
        AsyncTask<Void?, Void?, Void?>() {
        var data = JSONObject()
        var jsonArray = JSONArray()

        init {
            this.data = data
            jsonArray = array
        }

        override fun doInBackground(vararg voids: Void?): Void? {
            try {
                if (mRecvTransport == null) {
                    return null
                }
                var id: String = mRecvTransport!!.getId()
                Log.e(ConstantValues.TAG," call new transportid "+id )
                if (data.optJSONObject("appData").has("mediaType")) {
                    if (!data.optJSONObject("appData").optString("mediaType").contains("audio")) {
                        if (mRecvVidoeTransport != null) {
                            id = mRecvVidoeTransport!!.id
                        }
                    }
                }
                val jsonObject = JSONObject()
                jsonObject.put(
                    "rtpCapabilities",
                    Helper.stringToJsonObject(mMediasoupDevice!!.getRtpCapabilities())
                )
                jsonObject.put("consumerTransportId", id)
                jsonObject.put("producerId", data.optString("producer_id"))
                jsonObject.put("user_id", data.optString("user_id"))
                //String consumeEncrypt = Helper.cryptLibEncryptMessage(jsonObject.toString(),jsonObject.toString());
                callSocket!!
                    .emit(SocketEvents.CONSUME, jsonObject,
                        Ack { args ->
                            try {
                                val callBackObject = args[0] as JSONObject
                                createConsumer(callBackObject, data)
                                /*String consumeDecrypt = (String) args[0];
                                        if(consumeDecrypt != null && !consumeDecrypt.trim().isEmpty()){
                                            String decrypt = Helper.cryptLibEncryptMessage(consumeDecrypt,consumeDecrypt);
                                            JSONObject callBackObject = Helper.stringToJsonObject(decrypt);
                                            createConsumer(callBackObject, data);
                                        }*/
                            } catch (e: Exception) {
                                Helper.printExceptions(e)
                            }
                        })
            } catch (e: Exception) {
                //Helper.printExceptions(e)
            }
            return null
        }


    }

    private fun createConsumer(callBackObject: JSONObject, data: JSONObject) {
        if (callBackObject.optString("kind") == "audio") {
            AudioConsumer(callBackObject, data)
        } else {
            VideoConsumer(callBackObject, data)
        }
    }

    class AudioConsumer(callBackObject: JSONObject?, data: JSONObject?) {
        init {
            audioConsume(callBackObject!!, data!!)
        }
    }

    class VideoConsumer(callBackObject: JSONObject?, data: JSONObject?) {
        init {
            videoConsumed(callBackObject!!, data!!)
        }
    }
}