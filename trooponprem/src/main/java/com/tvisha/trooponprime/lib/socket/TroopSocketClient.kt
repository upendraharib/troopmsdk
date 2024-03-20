package com.tvisha.trooponprime.lib.socket

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.gson.Gson
import com.tvisha.trooponprime.lib.MessengerOnSocketListeners
import com.tvisha.trooponprime.lib.TroopClient
import com.tvisha.trooponprime.lib.TroopMessengerClient
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.ACTIVE_CALL_ID
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.ACTIVE_CALL_TYPE
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APPLICATION_TM_LOGIN_USER_ID
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APP_ACCESS_TOKEN
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.APP_LOGIN_USER_NAME
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.CALL_TM_USER_ID
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.CLIENT_IS_CONNECTED
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.activeCallData
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.clientCallBackListener
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.convEntityType
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.dataBase
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.messengerSocket
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.participantUsersList
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.sharedPreferences
import com.tvisha.trooponprime.lib.TroopMessengerClient.Companion.usbVideoCapture
import com.tvisha.trooponprime.lib.call.RoomClient
import com.tvisha.trooponprime.lib.clientModels.Chats
import com.tvisha.trooponprime.lib.database.*
import com.tvisha.trooponprime.lib.database.model.RecentLastMessage
import com.tvisha.trooponprime.lib.database.model.RecentList
import com.tvisha.trooponprime.lib.listeneres.ClientCallBackListener
import com.tvisha.trooponprime.lib.listeneres.SocketCallbackListener
import com.tvisha.trooponprime.lib.utils.*
import com.tvisha.trooponprime.lib.utils.ConstantValues.TAG
import com.tvisha.trooponprime.lib.utils.ConstantValues.deviceId
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal object TroopSocketClient {
    private lateinit var messengerOnSocketListeners : MessengerOnSocketListeners
    private lateinit var socketCallbackListener: SocketCallbackListener
    fun setMessengerListener(listeners: MessengerOnSocketListeners){
        messengerOnSocketListeners = listeners
    }
    fun setSocketCallBackListener(listeners: SocketCallbackListener){
        socketCallbackListener = listeners
    }
    fun callOnMessengerSocketListeners(mSocket: Socket){
        mSocket.on(Socket.EVENT_CONNECT, messengerSocketOnConnect)
        mSocket.on(Socket.EVENT_DISCONNECT, messengerSocketOnDisConnect)
        mSocket.on(Socket.EVENT_CONNECT_ERROR, messengerSocketOnConnectError)

        mSocket.on(SocketEvents.TM_ERROR, messengerSocketOnTmError)
        mSocket.on(SocketEvents.ACCESS_TOKEN,messengerSocketOnAccessToken)
        mSocket.on(SocketEvents.USER_PRESENCE,messengerSocketOnUserPresence)
        mSocket.on(SocketEvents.NEW_STORY,messengerSocketOnNewStory)
        mSocket.on(SocketEvents.STORY_VIEWED,messengerSocketOnStoryViewed)
        mSocket.on(SocketEvents.STORY_DELETED,messengerSocketOnStoryDeleted)
        mSocket.on(SocketEvents.CHAT_ARCHIVED,messengerSocketOnChatArchived)
        mSocket.on(SocketEvents.CHAT_UNARCHIVED,messengerSocketOnChatUnArchived)
        mSocket.on(SocketEvents.USER_BLOCKED,messengerSocketOnUserBlocked)
        mSocket.on(SocketEvents.USER_UNBLOCKED,messengerSocketOnUserUnBlocked)
        mSocket.on(SocketEvents.BLOCKED_ME,messengerSocketOnBlockedMe)
        mSocket.on(SocketEvents.UNBLOCKED_ME,messengerSocketOnUnBlockedMe)
        mSocket.on(SocketEvents.CHATS_DELETED,messengerSocketOnChatDeleted)
        mSocket.on(SocketEvents.MESSAGE_BLOCKED,messengerSocketOnMessageBlocked)
        mSocket.on(SocketEvents.USER_DEACTIVATED,messengerSocketOnUserDeactivated)
        mSocket.on(SocketEvents.RECEIVE_BROADCAST_MESSAGE,messengerSocketOnReceiveBroadCastMessage)
        mSocket.on(SocketEvents.USER_PROFILE_ABOUT_UPDATED,messengerSocketOnUserProfileAboutUpdated)
        mSocket.on(SocketEvents.PROFILE_PIC_UPDATED,messengerSocketOnUserProfilePicUpdated)
        mSocket.on(SocketEvents.USER_REGISTERED,messengerSocketOnUserRegistered)

        //mSocket.on(SocketEvents.ERROR, messengerSocketOnError)

        //signup/register/account verify
        //mSocket.on(SocketEvents.SIGNUP_EVENT, messengerSocketOnSignUp)
        //mSocket.on(SocketEvents.REGISTER_EVENT, messengerSocketOnRegister)
        /*mSocket.on(SocketEvents.ACCOUNT_VERIFIED, messengerSocketOnAccountVerified)
        mSocket.on(SocketEvents.UN_AUTHORIZED, messengerSocketOnUnAuthorized)
        mSocket.on(SocketEvents.COMPANY_NAME_UPDATE, messengerSocketOnCompanyNameUpdate)*/

        //user online/offline status
       /* mSocket.on(SocketEvents.NEW_ONLINE, messengerSocketOnOnLine)
        mSocket.on(SocketEvents.NEW_OFFLINE, messengerSocketOnOffLine)
        mSocket.on(SocketEvents.NEW_DND, messengerSocketOnDND)
        mSocket.on(SocketEvents.STATUS_OPTION_ONE, messengerSocketOnStatusOptionsOne)
        mSocket.on(SocketEvents.STATUS_OPTION_TWO, messengerSocketOnStatusOptionsTwo)
        mSocket.on(SocketEvents.STATUS_OPTION_THREE, messengerSocketOnStatusOptionsThree)*/

        //miscellaneous
        mSocket.on(SocketEvents.USER_TYPING, messengerSocketOnTyping)
        mSocket.on(SocketEvents.LOGOUT, messengerSocketOnLogout)
        /*mSocket.on(SocketEvents.GET_ACTIVE_DEVICES, messengerSocketOnActiveDevices)
        mSocket.on(SocketEvents.GET_USER_LAS_SEEN, messengerSocketOnUserLastSeen)
        mSocket.on(SocketEvents.VERSION_MANAGEMENT, messengerSocketOnVersionManagement)
        mSocket.on(SocketEvents.FORCE_UPDATE, messengerSocketOnForceUpdate)
        mSocket.on(SocketEvents.SERVER_CHECK, messengerSocketOnServerCheck)
        mSocket.on(SocketEvents.MARK_FAVOURITE, messengerSocketOnMarkFavourite)
        mSocket.on(SocketEvents.MUTE_CONVERSATION, messengerSocketOnMuteConversation)
        mSocket.on(SocketEvents.GLOBAL_CONSTANTS_UPDATED, messengerSocketOnGlobalConstantUpdated)
        mSocket.on(SocketEvents.SERVER_TIME, messengerSocketOnServerTime)
        mSocket.on(SocketEvents.USER_PLATFORM_UPDATED, messengerSocketOnUserPlatformUpdated)
        mSocket.on(SocketEvents.MFA_PIN_SET, messengerSocketOnMFAPinSet)*/


        //message events
        /*mSocket.on(SocketEvents.SEND_MESSAGE_ERROR, messengerSocketOnErrorWhileSendingMessage)
        mSocket.on(SocketEvents.GET_MISSING_MESSAGES, messengerSocketOnMissingMessages)*/
        //mSocket.on(SocketEvents.DELETE_MESSAGES_OFFLINE, messengerSocketOnDeleteMessageOffline)
        /*mSocket.on(SocketEvents.READ_RECEIPT, messengerSocketOnReadReceiptMessage)
        mSocket.on(SocketEvents.FLAG_MESSAGE, messengerSocketOnFlagMessage)
        mSocket.on(SocketEvents.RESPOND_LATER_MESSAGE, messengerSocketOnRespondLater)
        mSocket.on(SocketEvents.ATTACHMENT_PLACEHOLDER_DELETED, messengerSocketOnAttachmentPlaceholderDeleted)
        mSocket.on(SocketEvents.PIN_MESSAGE, messengerSocketOnPinMessage)*/
        mSocket.on(SocketEvents.MESSAGE_EDITED, messengerSocketOnMessageEdit)
        mSocket.on(SocketEvents.USER_RECEIVE_MESSAGE, messengerSocketOnReceiveMessage)
        mSocket.on(SocketEvents.USER_MESSAGE_SENT, messengerSocketOnMessageSent)
        mSocket.on(SocketEvents.MESSAGE_DELIVERED, messengerSocketOnMessageDelivered)
        mSocket.on(SocketEvents.MESSAGE_READ, messengerSocketOnMessageRead)
        mSocket.on(SocketEvents.MESSAGE_READ_BY_ME, messengerSocketOnMessageReadByMe)
        mSocket.on(SocketEvents.MESSAGE_DELETED, messengerSocketOnDeleteMessage)
        mSocket.on(SocketEvents.MESSAGE_RECALLED, messengerSocketOnRecallMessage)
        mSocket.on(SocketEvents.SYNC_OFFLINE_MESSAGES, messengerSocketOnSyncOfflineMessages)

        //group
        mSocket.on(SocketEvents.USER_GROUP_CREATED, messengerSocketOnGroupCreated)
        mSocket.on(SocketEvents.USER_GROUP_UPDATED, messengerSocketOnGroupUpdated)
        //mSocket.on(SocketEvents.GROUP_KEY, messengerSocketOnGroupKey)

        //user
        /*mSocket.on(SocketEvents.NEW_USER_CREATED, messengerSocketOnNewUserCreated)
        mSocket.on(SocketEvents.USER_PIC_UPDATE, messengerSocketOnUserPicUpdated)
        mSocket.on(SocketEvents.USER_STATUS_UPDATE, messengerSocketOnUserStatusUpdated)
        mSocket.on(SocketEvents.USER_UPDATE, messengerSocketOnUserUpdated)
        mSocket.on(SocketEvents.USER_KEY, messengerSocketOnUserKey)*/

        //private chat (burnout)
        /*mSocket.on(SocketEvents.INIT_PRIVATE_CHAT, messengerSocketOnInitPrivateChat)
        mSocket.on(SocketEvents.PRIVATE_CHAT_PERMISSION, messengerSocketOnPrivateChatPermission)
        mSocket.on(SocketEvents.END_PRIVATE_CHAT, messengerSocketOnEndPrivateChat)
        //mSocket.on(SocketEvents.CHECK_PRIVATE_CHAT, messengerSocketOnCheckPrivateChat)
        mSocket.on(SocketEvents.GET_ALL_BURN_LIST, messengerSocketOnAllBurnoutList)*/

        //location tracking
        /*mSocket.on(SocketEvents.INIT_LOCATION_TRACKING, messengerSocketOnInitLocationTracking)
        mSocket.on(SocketEvents.LOCATION_TRACKING_PERMISSION, messengerSocketOnLocationTrackingPermission)
        mSocket.on(SocketEvents.LOCATION_TRACKING_SIGNAL, messengerSocketOnLocationTrackingSignal)
        mSocket.on(SocketEvents.STOP_LOCATION_TRACKING, messengerSocketOnStopLocationTracking)*/
        //mSocket.on(SocketEvents.CHECK_LOCATION_TRACKING, messengerSocketOnCheckLocationTracking)

        //orange-member
        /*mSocket.on(SocketEvents.IS_ORANGE_MEMBER_ADDED, messengerSocketOnIsOrangeMemberAdded)
        mSocket.on(SocketEvents.IS_ORANGE_MEMBER_REMOVED, messengerSocketOnIsOrangeMemberRemoved)*/

        //permissions
        /*mSocket.on(SocketEvents.PERMISSION_UPDATED, messengerSocketOnPermissionUpdate)
        mSocket.on(SocketEvents.GLOBAL_PERMISSION_UPDATED, messengerSocketOnGlobalPermissionUpdate)
        mSocket.on(SocketEvents.ACCESS_PERMISSION_STATUS_UPDATED, messengerSocketOnAccessPermissionStatusUpdated)*/

        //plan
        /*mSocket.on(SocketEvents.PLAN_UPDATE, messengerSocketOnPlanUpdate)
        mSocket.on(SocketEvents.PLAN_ERROR, messengerSocketOnPlanError)
        mSocket.on(SocketEvents.PLAN_EXPIRED, messengerSocketOnPlanExpired)*/



        //archived
        /*mSocket.on(SocketEvents.ARCHIVED_MESSAGES, messengerSocketOnArchivedMessages)
        mSocket.on(SocketEvents.DELETE_MESSAGE_HISTORY, messengerSocketOnDeleteMessageHistory)
        mSocket.on(SocketEvents.AUTO_DELETE_HISTORY, messengerSocketOnAutoDeleteHistory)*/

        //notify
        /*mSocket.on(SocketEvents.NOTIFY, messengerSocketOnNewNotify)
        mSocket.on(SocketEvents.NOTIFY_RECALL, messengerSocketOnNotifyRecall)
        mSocket.on(SocketEvents.NOTIFY_READ, messengerSocketOnNotifyRead)*/

        //global authorization
        /*mSocket.on(SocketEvents.NEW_USER_REQUEST_AUTHORIZATION, messengerSocketOnNewUserRequestAuthorization)
        mSocket.on(SocketEvents.UPDATE_UNIT_USER_AUTHORIZATION, messengerSocketOnUpdateUnitUserAuthorization)
        mSocket.on(SocketEvents.USER_REMOVED_FROM_UNIT, messengerSocketOnUserRemovedFromUnit)
        mSocket.on(SocketEvents.USER_ADD_TO_UNIT, messengerSocketOnUserAddToUnit)
        mSocket.on(SocketEvents.UNIT_REQUEST_COUNT, messengerSocketOnUnitRequestCount)*/

        //IAF appointments/topic
        /*mSocket.on(SocketEvents.APPOINTMENT_MODE, messengerSocketOnAppointmentMode)
        mSocket.on(SocketEvents.TOPIC_CRATED, messengerSocketOnTopicCreated)
        mSocket.on(SocketEvents.TOPIC_UPDATED, messengerSocketOnTopicUpdated)
        mSocket.on(SocketEvents.SWAPPED_APPOINTMENTS, messengerSocketOnSwappedAppointments)

        mSocket.on(SocketEvents.CC_REMAINDER, messengerSocketOnCcRemainder)*/

    }

    fun stopOnMessengerSocketListeners(mSocket: Socket){
        mSocket.off(Socket.EVENT_CONNECT, messengerSocketOnConnect)
        mSocket.off(Socket.EVENT_DISCONNECT, messengerSocketOnDisConnect)
        mSocket.off(Socket.EVENT_CONNECT_ERROR, messengerSocketOnConnectError)
        //mSocket.off(SocketEvents.ERROR, messengerSocketOnError)
        mSocket.off(SocketEvents.TM_ERROR, messengerSocketOnTmError)
        mSocket.off(SocketEvents.ACCESS_TOKEN,messengerSocketOnAccessToken)
        mSocket.off(SocketEvents.USER_PRESENCE,messengerSocketOnUserPresence)
        mSocket.off(SocketEvents.NEW_STORY,messengerSocketOnNewStory)
        mSocket.off(SocketEvents.STORY_VIEWED,messengerSocketOnStoryViewed)
        mSocket.off(SocketEvents.STORY_DELETED,messengerSocketOnStoryDeleted)
        mSocket.off(SocketEvents.CHAT_ARCHIVED,messengerSocketOnChatArchived)
        mSocket.off(SocketEvents.CHAT_UNARCHIVED,messengerSocketOnChatUnArchived)
        mSocket.off(SocketEvents.USER_BLOCKED,messengerSocketOnUserBlocked)
        mSocket.off(SocketEvents.USER_UNBLOCKED,messengerSocketOnUserUnBlocked)
        mSocket.off(SocketEvents.BLOCKED_ME,messengerSocketOnBlockedMe)
        mSocket.off(SocketEvents.UNBLOCKED_ME,messengerSocketOnUnBlockedMe)
        mSocket.off(SocketEvents.CHATS_DELETED,messengerSocketOnChatDeleted)
        mSocket.off(SocketEvents.MESSAGE_BLOCKED,messengerSocketOnMessageBlocked)
        mSocket.off(SocketEvents.USER_DEACTIVATED,messengerSocketOnUserDeactivated)
        mSocket.off(SocketEvents.RECEIVE_BROADCAST_MESSAGE,messengerSocketOnReceiveBroadCastMessage)
        mSocket.off(SocketEvents.USER_PROFILE_ABOUT_UPDATED,messengerSocketOnUserProfileAboutUpdated)
        mSocket.off(SocketEvents.PROFILE_PIC_UPDATED,messengerSocketOnUserProfilePicUpdated)
        mSocket.off(SocketEvents.USER_REGISTERED,messengerSocketOnUserRegistered)
        //signup/register/account verify
        //mSocket.off(SocketEvents.SIGNUP_EVENT, messengerSocketOnSignUp)
        //mSocket.off(SocketEvents.REGISTER_EVENT, messengerSocketOnRegister)
        /*mSocket.off(SocketEvents.ACCOUNT_VERIFIED, messengerSocketOnAccountVerified)
        mSocket.off(SocketEvents.UN_AUTHORIZED, messengerSocketOnUnAuthorized)
        mSocket.off(SocketEvents.COMPANY_NAME_UPDATE, messengerSocketOnCompanyNameUpdate)*/


        //user online/offline status
        /*mSocket.off(SocketEvents.NEW_ONLINE, messengerSocketOnOnLine)
        mSocket.off(SocketEvents.NEW_OFFLINE, messengerSocketOnOffLine)
        mSocket.off(SocketEvents.NEW_DND, messengerSocketOnDND)
        mSocket.off(SocketEvents.STATUS_OPTION_ONE, messengerSocketOnStatusOptionsOne)
        mSocket.off(SocketEvents.STATUS_OPTION_TWO, messengerSocketOnStatusOptionsTwo)
        mSocket.off(SocketEvents.STATUS_OPTION_THREE, messengerSocketOnStatusOptionsThree)*/


        //miscellaneous
        mSocket.off(SocketEvents.USER_TYPING, messengerSocketOnTyping)
        mSocket.off(SocketEvents.LOGOUT, messengerSocketOnLogout)

        /*mSocket.off(SocketEvents.GET_USER_LAS_SEEN, messengerSocketOnUserLastSeen)
        mSocket.off(SocketEvents.GET_ACTIVE_DEVICES, messengerSocketOnActiveDevices)
        mSocket.off(SocketEvents.VERSION_MANAGEMENT, messengerSocketOnVersionManagement)
        mSocket.off(SocketEvents.FORCE_UPDATE, messengerSocketOnForceUpdate)
        mSocket.off(SocketEvents.SERVER_CHECK, messengerSocketOnServerCheck)
        mSocket.off(SocketEvents.MARK_FAVOURITE, messengerSocketOnMarkFavourite)
        mSocket.off(SocketEvents.MUTE_CONVERSATION, messengerSocketOnMuteConversation)
        mSocket.off(SocketEvents.GLOBAL_CONSTANTS_UPDATED, messengerSocketOnGlobalConstantUpdated)
        mSocket.off(SocketEvents.SERVER_TIME, messengerSocketOnServerTime)
        mSocket.off(SocketEvents.USER_PLATFORM_UPDATED, messengerSocketOnUserPlatformUpdated)
        mSocket.off(SocketEvents.MFA_PIN_SET, messengerSocketOnMFAPinSet)*/



        //message events

        //mSocket.off(SocketEvents.SEND_MESSAGE_ERROR, messengerSocketOnErrorWhileSendingMessage)
        //mSocket.off(SocketEvents.GET_MISSING_MESSAGES, messengerSocketOnMissingMessages)
        //mSocket.off(SocketEvents.DELETE_MESSAGES_OFFLINE, messengerSocketOnDeleteMessageOffline)
        /*mSocket.off(SocketEvents.READ_RECEIPT, messengerSocketOnReadReceiptMessage)
        mSocket.off(SocketEvents.FLAG_MESSAGE, messengerSocketOnFlagMessage)
        mSocket.off(SocketEvents.RESPOND_LATER_MESSAGE, messengerSocketOnRespondLater)
        mSocket.off(SocketEvents.ATTACHMENT_PLACEHOLDER_DELETED, messengerSocketOnAttachmentPlaceholderDeleted)
        mSocket.off(SocketEvents.PIN_MESSAGE, messengerSocketOnPinMessage)*/
        mSocket.off(SocketEvents.MESSAGE_EDITED, messengerSocketOnMessageEdit)
        mSocket.off(SocketEvents.USER_RECEIVE_MESSAGE, messengerSocketOnReceiveMessage)
        mSocket.off(SocketEvents.USER_MESSAGE_SENT, messengerSocketOnMessageSent)
        mSocket.off(SocketEvents.MESSAGE_DELIVERED, messengerSocketOnMessageDelivered)
        mSocket.off(SocketEvents.MESSAGE_READ, messengerSocketOnMessageRead)
        mSocket.off(SocketEvents.MESSAGE_READ_BY_ME, messengerSocketOnMessageReadByMe)
        mSocket.off(SocketEvents.MESSAGE_DELETED, messengerSocketOnDeleteMessage)
        mSocket.off(SocketEvents.MESSAGE_RECALLED, messengerSocketOnRecallMessage)
        mSocket.off(SocketEvents.SYNC_OFFLINE_MESSAGES, messengerSocketOnSyncOfflineMessages)
        //group
        mSocket.off(SocketEvents.USER_GROUP_CREATED, messengerSocketOnGroupCreated)
        mSocket.off(SocketEvents.USER_GROUP_UPDATED, messengerSocketOnGroupUpdated)
        //mSocket.off(SocketEvents.GROUP_KEY, messengerSocketOnGroupKey)

        //user
        /*mSocket.off(SocketEvents.NEW_USER_CREATED, messengerSocketOnNewUserCreated)
        mSocket.off(SocketEvents.USER_PIC_UPDATE, messengerSocketOnUserPicUpdated)
        mSocket.off(SocketEvents.USER_STATUS_UPDATED, messengerSocketOnUserStatusUpdated)
        mSocket.off(SocketEvents.USER_UPDATE, messengerSocketOnUserUpdated)
        mSocket.off(SocketEvents.USER_KEY, messengerSocketOnUserKey)*/

        //private chat (burnout)
        /*mSocket.off(SocketEvents.INIT_PRIVATE_CHAT, messengerSocketOnInitPrivateChat)
        mSocket.off(SocketEvents.PRIVATE_CHAT_PERMISSION, messengerSocketOnPrivateChatPermission)
        mSocket.off(SocketEvents.END_PRIVATE_CHAT, messengerSocketOnEndPrivateChat)
        //mSocket.off(SocketEvents.CHECK_PRIVATE_CHAT, messengerSocketOnCheckPrivateChat)
        mSocket.off(SocketEvents.GET_ALL_BURN_LIST, messengerSocketOnAllBurnoutList)*/

        //location tracking
        /*mSocket.off(SocketEvents.INIT_LOCATION_TRACKING, messengerSocketOnInitLocationTracking)
        mSocket.off(SocketEvents.LOCATION_TRACKING_PERMISSION, messengerSocketOnLocationTrackingPermission)
        mSocket.off(SocketEvents.STOP_LOCATION_TRACKING, messengerSocketOnStopLocationTracking)
        mSocket.off(SocketEvents.LOCATION_TRACKING_SIGNAL, messengerSocketOnLocationTrackingSignal)*/
        //mSocket.off(SocketEvents.CHECK_LOCATION_TRACKING, messengerSocketOnCheckLocationTracking)

        //orange member
        /*mSocket.off(SocketEvents.IS_ORANGE_MEMBER_ADDED, messengerSocketOnIsOrangeMemberAdded)
        mSocket.off(SocketEvents.IS_ORANGE_MEMBER_REMOVED, messengerSocketOnIsOrangeMemberRemoved)*/

        //permissions
        /*mSocket.off(SocketEvents.PERMISSION_UPDATED, messengerSocketOnPermissionUpdate)
        mSocket.off(SocketEvents.GLOBAL_PERMISSION_UPDATED, messengerSocketOnGlobalPermissionUpdate)
        mSocket.off(SocketEvents.ACCESS_PERMISSION_STATUS_UPDATED, messengerSocketOnAccessPermissionStatusUpdated)*/

        //plan
        /*mSocket.off(SocketEvents.PLAN_UPDATE, messengerSocketOnPlanUpdate)
        mSocket.off(SocketEvents.PLAN_ERROR, messengerSocketOnPlanError)
        mSocket.off(SocketEvents.PLAN_EXPIRED, messengerSocketOnPlanExpired)*/




        //archived
        /*mSocket.off(SocketEvents.ARCHIVED_MESSAGES, messengerSocketOnArchivedMessages)
        mSocket.off(SocketEvents.DELETE_MESSAGE_HISTORY, messengerSocketOnDeleteMessageHistory)
        mSocket.off(SocketEvents.AUTO_DELETE_HISTORY, messengerSocketOnAutoDeleteHistory)*/

        //notify
        /*mSocket.off(SocketEvents.NOTIFY, messengerSocketOnNewNotify)
        mSocket.off(SocketEvents.NOTIFY_RECALL, messengerSocketOnNotifyRecall)
        mSocket.off(SocketEvents.NOTIFY_READ, messengerSocketOnNotifyRead)*/

        //global authorization
        /*mSocket.off(SocketEvents.NEW_USER_REQUEST_AUTHORIZATION, messengerSocketOnNewUserRequestAuthorization)
        mSocket.off(SocketEvents.UPDATE_UNIT_USER_AUTHORIZATION, messengerSocketOnUpdateUnitUserAuthorization)
        mSocket.off(SocketEvents.USER_REMOVED_FROM_UNIT, messengerSocketOnUserRemovedFromUnit)
        mSocket.off(SocketEvents.USER_ADD_TO_UNIT, messengerSocketOnUserAddToUnit)
        mSocket.off(SocketEvents.UNIT_REQUEST_COUNT, messengerSocketOnUnitRequestCount)*/

        //IAF appointments/topic
        /*mSocket.off(SocketEvents.APPOINTMENT_MODE, messengerSocketOnAppointmentMode)
        mSocket.off(SocketEvents.TOPIC_CRATED, messengerSocketOnTopicCreated)
        mSocket.off(SocketEvents.TOPIC_UPDATED, messengerSocketOnTopicUpdated)
        mSocket.off(SocketEvents.SWAPPED_APPOINTMENTS, messengerSocketOnSwappedAppointments)

        mSocket.off(SocketEvents.CC_REMAINDER, messengerSocketOnCcRemainder)*/
    }
    fun callOnCallSocketListeners(cSocket: Socket){
        cSocket.on(Socket.EVENT_CONNECT, callSocketOnConnect)
        cSocket.on(Socket.EVENT_DISCONNECT, callSocketOnDisConnect)
        cSocket.on(Socket.EVENT_CONNECT_ERROR, callSocketOnConnectError)
        cSocket.on(SocketEvents.ERROR, callSocketOnConnectError)

        cSocket.on(SocketEvents.NEWPRODUCERS, callSocketOnNewProducers)
        cSocket.on(SocketEvents.CONSUMERCLOSED, callSocketOnConsumerClosed)
        cSocket.on(SocketEvents.STOP_JOIN_CALL_REQUEST, callSocketOnStopJoinCallRequest)
        cSocket.on(SocketEvents.CALL_REQUEST, callSocketOnCallRequest)
        cSocket.on(SocketEvents.CALL_PERMISSION, callSocketOnCallPermission)
        cSocket.on(SocketEvents.END_CALL, callSocketOnEndCall)
        /*cSocket.on(SocketEvents.REQUEST_STREAM, callSocketOnRequestStream)
        cSocket.on(SocketEvents.STREAM_PERMISSION, callSocketOnStreamPermission)
        cSocket.on(SocketEvents.END_CALL_STREAM, callSocketOnEndCallStream)
        cSocket.on(SocketEvents.JOIN_CALL, callSocketOnJoinCall)*/
        cSocket.on(SocketEvents.MUTE_CALL_AUDIO, callSocketOnMuteCallAudio)
        cSocket.on(SocketEvents.MUTE_CALL_VIDEO, callSocketOnMuteCallVideo)
        cSocket.on(SocketEvents.END_SCREEN_SHARE, callSocketOnEndScreenShare)
        cSocket.on(SocketEvents.CALL_STREAM_REQUEST, callSocketOnCallStreamRequest)
        cSocket.on(SocketEvents.CALL_MUTE_REQUEST, callSocketOnCallMuteRequest)
        cSocket.on(SocketEvents.CALL_REMOVE_USER, callSocketOnCallRemoveUser)
        cSocket.on(SocketEvents.CALL_NEW_PARTICIPANTS, callSocketOnCallNewParticipants)
        cSocket.on(SocketEvents.CALL_UPDATE_HOST_UPDATED, callSocketOnCallHostUpdated)
        cSocket.on(SocketEvents.CALL_USER_VIDEO_STATUS_UPDATED, callSocketOnCallUserVideoStatusUpdated)
        cSocket.on(SocketEvents.LEAVE_CALL, callSocketOnLeaveCall)
        cSocket.on(SocketEvents.REQUEST_JOIN_CALL, callSocketOnRequestJoinCall)
        cSocket.on(SocketEvents.JOIN_CALL_PERMISSION, callSocketOnJoinCallPermission)
        cSocket.on(SocketEvents.CALL_PARTICIPANT_STATUS_UPDATE, callSocketOnCallParticipantStatusUpdate)
        cSocket.on(SocketEvents.HOST_MUTE_AUDIO, callSocketOnHostMuteAudio)
        cSocket.on(SocketEvents.HOST_MUTE_VIDEO, callSocketOnHostMuteVideo)
        cSocket.on(SocketEvents.END_JOINTLY_CODE, callSocketOnEndJointlyCode)
        /*cSocket.on(SocketEvents.CC_MEETING_STARTED, callSocketOnCcMeetingStarted)
        cSocket.on(SocketEvents.ACTIVE_CALL, callSocketOnActiveCall)
        cSocket.on(SocketEvents.ON_CALL, callSocketOnOnCall)*/


        //cSocket.on(SocketEvents.CALL_SIGNAL, socketOnCallSignal)
    }
    fun stopOnCallSocketListeners(cSocket: Socket){
        cSocket.off(Socket.EVENT_CONNECT, callSocketOnConnect)
        cSocket.off(Socket.EVENT_DISCONNECT, callSocketOnDisConnect)
        cSocket.off(Socket.EVENT_CONNECT_ERROR, callSocketOnConnectError)

        cSocket.off(SocketEvents.NEWPRODUCERS, callSocketOnNewProducers)
        cSocket.off(SocketEvents.CONSUMERCLOSED, callSocketOnConsumerClosed)
        cSocket.off(SocketEvents.STOP_JOIN_CALL_REQUEST, callSocketOnStopJoinCallRequest)
        cSocket.off(SocketEvents.CALL_REQUEST, callSocketOnCallRequest)
        cSocket.off(SocketEvents.CALL_PERMISSION, callSocketOnCallPermission)
        cSocket.off(SocketEvents.END_CALL, callSocketOnEndCall)
        /*cSocket.off(SocketEvents.REQUEST_STREAM, callSocketOnRequestStream)
        cSocket.off(SocketEvents.STREAM_PERMISSION, callSocketOnStreamPermission)
        cSocket.off(SocketEvents.END_CALL_STREAM, callSocketOnEndCallStream)
        cSocket.off(SocketEvents.JOIN_CALL, callSocketOnJoinCall)*/
        cSocket.off(SocketEvents.MUTE_CALL_AUDIO, callSocketOnMuteCallAudio)
        cSocket.off(SocketEvents.MUTE_CALL_VIDEO, callSocketOnMuteCallVideo)
        cSocket.off(SocketEvents.END_SCREEN_SHARE, callSocketOnEndScreenShare)
        cSocket.off(SocketEvents.CALL_STREAM_REQUEST, callSocketOnCallStreamRequest)
        cSocket.off(SocketEvents.CALL_MUTE_REQUEST, callSocketOnCallMuteRequest)
        cSocket.off(SocketEvents.CALL_REMOVE_USER, callSocketOnCallRemoveUser)
        cSocket.off(SocketEvents.CALL_NEW_PARTICIPANTS, callSocketOnCallNewParticipants)
        cSocket.off(SocketEvents.CALL_UPDATE_HOST_UPDATED, callSocketOnCallHostUpdated)
        cSocket.off(SocketEvents.CALL_USER_VIDEO_STATUS_UPDATED, callSocketOnCallUserVideoStatusUpdated)
        cSocket.off(SocketEvents.LEAVE_CALL, callSocketOnLeaveCall)
        cSocket.off(SocketEvents.REQUEST_JOIN_CALL, callSocketOnRequestJoinCall)
        cSocket.off(SocketEvents.JOIN_CALL_PERMISSION, callSocketOnJoinCallPermission)
        cSocket.off(SocketEvents.CALL_PARTICIPANT_STATUS_UPDATE, callSocketOnCallParticipantStatusUpdate)
        cSocket.off(SocketEvents.HOST_MUTE_AUDIO, callSocketOnHostMuteAudio)
        cSocket.off(SocketEvents.HOST_MUTE_VIDEO, callSocketOnHostMuteVideo)
        cSocket.off(SocketEvents.END_JOINTLY_CODE, callSocketOnEndJointlyCode)
        /*cSocket.off(SocketEvents.CC_MEETING_STARTED, callSocketOnCcMeetingStarted)
        cSocket.off(SocketEvents.ACTIVE_CALL, callSocketOnActiveCall)
        cSocket.off(SocketEvents.ON_CALL, callSocketOnOnCall)*/

        //cSocket.off(SocketEvents.CALL_SIGNAL, socketOnCallSignal)
    }

    //messenger on event listener
    private val messengerSocketOnConnect = Emitter.Listener {
        Log.e(TAG," Socket connected ")
        clientCallBackListener!!.clientConnected()
        //messengerOnSocketListeners.messengerOnSocketConnected()
    }
    private val messengerSocketOnDisConnect = Emitter.Listener {
        CLIENT_IS_CONNECTED = false
        Log.e(TAG," Socket disconnected ")
        clientCallBackListener!!.clientDisConnected()
        //messengerOnSocketListeners.messengerOnSocketDisconnected()
    }
    private val messengerSocketOnConnectError = Emitter.Listener {args->
        //messengerOnSocketListeners.messengerOnSocketConnectError()
        Log.e(TAG," Socket connect error ")
    }
    private val messengerSocketOnTmError= Emitter.Listener { args ->
        Log.e(TAG," tmerror ${args[0].toString()}")
    }
    private val messengerSocketOnAccessToken =Emitter.Listener {args ->
        var jsonObject = args[0] as JSONObject
        CLIENT_IS_CONNECTED = true
        APP_ACCESS_TOKEN = jsonObject.optString("access_token")
        Log.e(TAG," acesstoke $APP_ACCESS_TOKEN")
        if (sharedPreferences!!.getBoolean(SharePreferenceConstants.TM_USER_LOGIN_STATUS,false)){
            if (socketCallbackListener!=null){
                socketCallbackListener.syncData()
            }
        }
        fetchUserStatuses()
    }
    private val messengerSocketOnUserPresence = Emitter.Listener { args ->
        var jsonObject = args[0] as JSONObject
        if (dataBase!!.userDAO.checkIsUserIsBlocked(jsonObject.optString("user_id"))==1){
            jsonObject.put("success",false)
            jsonObject.put("status","offline")
            jsonObject.put("last_seen","")
            Log.e("Socket ==> "," userpresnse  "+jsonObject.toString())
            //messengerOnSocketListeners.messengerOnSocketUserPresence(jsonObject)
            return@Listener
        }
        if (jsonObject.has("last_seen") && !jsonObject.optString("last_seen").isNullOrEmpty()){
            jsonObject.put("last_seen",Helper.utcToLocalTime(jsonObject.optString("last_seen")))
        }
        Log.e(TAG,"  userpresence ${jsonObject.toString()}")
        clientCallBackListener.userPresenceResponse(jsonObject)
    }
    private val messengerSocketOnNewStory = Emitter.Listener { args ->
        val jsonObject = args[0] as JSONObject
        Log.e("newstory==> ",""+jsonObject.toString())
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        var story =  Story()
        story.id = jsonObject.optLong("id")
        story.data = jsonObject.optString("data")
        story.created_at = jsonObject.optString("created_at")
        story.user_id = jsonObject.optLong("user_id")
        story.status = ConstantValues.StoryStatus.UNSEEN
        story.shared_to = jsonObject.optString("shared_to")
        story.type = jsonObject.optInt("type")
        story.updated_at = updatedAtTime!!
        dataBase!!.storyDAO.insertStory(story)
    }
    private val messengerSocketOnStoryViewed= Emitter.Listener { args ->
        var jsonObject = args[0] as JSONObject
        var story = dataBase!!.storyDAO.fetchStoryById(jsonObject.optLong("id"))
        //var story = dataBase!!.storyDAO.fetchStory()
    }
    private val messengerSocketOnStoryDeleted =Emitter.Listener { args ->
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        var jsonObject = args[0] as JSONObject
        var story = dataBase!!.storyDAO.fetchStoryById(jsonObject.optLong("id"))
        story.status= ConstantValues.StoryStatus.DELETED
        story.updated_at = updatedAtTime!!
        dataBase!!.storyDAO.updateStory(story)
    }
    private val messengerSocketOnChatArchived = Emitter.Listener { args ->
        var jsonObject = args[0] as JSONObject
        GlobalScope.launch {
            var chats = jsonObject.optJSONArray("chats")
            var userIds : ArrayList<Long> = ArrayList()
            var groupIds : ArrayList<Long> = ArrayList()
            var chatLength = chats.length()
            for (i in 0 until  chatLength){
                if (chats.optJSONObject(i).optString("entity_id").isNullOrEmpty() && chats.optJSONObject(i).optString("entity_type").isNullOrEmpty()){
                    if (chats.optJSONObject(i).optInt("entity_type")==ConstantValues.Entity.USER){
                        userIds.add(jsonObject.optLong("entity_id"))
                    }else{
                        groupIds.add(jsonObject.optLong("entity_id"))
                    }
                }
            }
            if (userIds.size>0){
                dataBase!!.userDAO.updateUserArchived(userIds)
            }
            if (groupIds.size>0){
                dataBase!!.messengerGroupDAO.updateGroupArchived(groupIds)
            }
        }
    }
    private val messengerSocketOnChatUnArchived= Emitter.Listener { args ->
        var jsonObject = args[0] as JSONObject
        GlobalScope.launch {
            var chats = jsonObject.optJSONArray("chats")
            var userIds : ArrayList<Long> = ArrayList()
            var groupIds : ArrayList<Long> = ArrayList()
            var chatLength = chats.length()
            for (i in 0 until  chatLength){
                if (chats.optJSONObject(i).optString("entity_id").isNullOrEmpty() && chats.optJSONObject(i).optString("entity_type").isNullOrEmpty()){
                    if (chats.optJSONObject(i).optInt("entity_type")==ConstantValues.Entity.USER){
                        userIds.add(jsonObject.optLong("entity_id"))
                    }else{
                        groupIds.add(jsonObject.optLong("entity_id"))
                    }
                }
            }
            if (userIds.size>0){
                dataBase!!.userDAO.updateUserUnArchived(userIds)
            }
            if (groupIds.size>0){
                dataBase!!.messengerGroupDAO.updateGroupUnArchived(groupIds)
            }
        }
    }
    private val messengerSocketOnUserBlocked= Emitter.Listener { args ->
        var jsonObject = args[0] as JSONObject
        updateUserBlockUnBlockAndMe(ConstantValues.BlockedStatus.BLOCK,jsonObject,ConstantValues.BlockedStatus.USER_BLOCKED)

    }
    private val messengerSocketOnUserUnBlocked= Emitter.Listener { args ->
        var jsonObject = args[0] as JSONObject
        updateUserBlockUnBlockAndMe(ConstantValues.BlockedStatus.UNBLOCK,jsonObject,ConstantValues.BlockedStatus.USER_UNBLOCKED)
    }
    private val  messengerSocketOnBlockedMe =Emitter.Listener { args ->
        var jsonObject = args[0] as JSONObject
        updateUserBlockUnBlockAndMe(ConstantValues.BlockedStatus.BLOCK,jsonObject,ConstantValues.BlockedStatus.USER_BLOCKED_ME)
    }
    private val  messengerSocketOnUnBlockedMe =Emitter.Listener { args ->
        var jsonObject = args[0] as JSONObject
        updateUserBlockUnBlockAndMe(ConstantValues.BlockedStatus.UNBLOCK,jsonObject,ConstantValues.BlockedStatus.USER_UNBLOCKED_ME)
    }
    private val messengerSocketOnChatDeleted = Emitter.Listener { args ->
        var jsonObject = args[0] as JSONObject
        var jsonArray = jsonObject.optJSONArray("chats")
        if (jsonArray!=null && jsonArray.length()>0){
            GlobalScope.launch {
                var arrayLength = jsonArray.length()
                for (i in 0 until arrayLength){
                    if (jsonArray.optJSONObject(i).optInt("entity_type")==ConstantValues.Entity.USER){
                        dataBase!!.messengerDAO.bulkDeleteUserChats(jsonArray.optJSONObject(i).optString("entity_id"),APPLICATION_TM_LOGIN_USER_ID)
                        dataBase!!.userDAO.updateUserLastMessageData(jsonArray.optJSONObject(i).optString("entity_id"))
                    }else {
                        dataBase!!.messengerDAO.bulkDeleteGroupChats(jsonArray.optJSONObject(i).optString("entity_id"))
                        dataBase!!.messengerGroupDAO.updateGroupLastMessageData(jsonArray.optJSONObject(i).optString("entity_id"))
                    }
                }
            }
        }
    }
    private val messengerSocketOnMessageBlocked = Emitter.Listener { args ->

        //dataBase!!.messengerDAO.deleteLocalMessages()
    }
    private val messengerSocketOnUserDeactivated = Emitter.Listener { args ->
        var jsonObject =args[0] as JSONObject
        if (jsonObject.has("user_id")) {
            deActivatedUsers(jsonObject.optJSONArray("user_id")!!)
        }
    }
    private val  messengerSocketOnReceiveBroadCastMessage= Emitter.Listener { args ->
        var jsonObject = args[0] as JSONObject
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        var broadCast = BroadCast()
        broadCast.message_id = jsonObject.optLong("message_id")
        broadCast.message = jsonObject.optString("message")
        broadCast.created_at = jsonObject.optString("created_at")
        broadCast.updated_at = updatedAtTime!!
        broadCast.attachment = jsonObject.optString("attachment")
        broadCast.title = jsonObject.optString("title")
        dataBase!!.broadCastDAO.insertBroadCast(broadCast)
    }
    private val messengerSocketOnUserProfileAboutUpdated =Emitter.Listener { args ->
        var jsonObject = args[0] as JSONObject
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        dataBase!!.userDAO.updateUserProfileAbout(jsonObject.optString("about"),updatedAtTime!!,jsonObject.optString("user_id"))
    }
    private val messengerSocketOnUserProfilePicUpdated = Emitter.Listener { args ->
        var jsonObject = args[0] as JSONObject
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        dataBase!!.userDAO.updateUserProfileAvatar(jsonObject.optString("user_id"),jsonObject.optString("profile_pic"),updatedAtTime!!)
    }
    private val messengerSocketOnUserRegistered = Emitter.Listener { args ->
        var jsonObject = args[0] as JSONObject
    }
    private val messengerSocketOnError = Emitter.Listener {
        Log.e("Socket ==> "," on error ")
        //messengerOnSocketListeners.messengerOnSocketError()
    }
    /*private val messengerSocketOnAccountVerified = Emitter.Listener{
        messengerOnSocketListeners.messengerOnSocketAccountVerified()
    }
    private val messengerSocketOnUnAuthorized = Emitter.Listener {
        messengerOnSocketListeners.messengerOnSocketUnAuthorized()
    }
    private val messengerSocketOnCompanyNameUpdate = Emitter.Listener {args ->
        updateTheCompanyNameInDB(args[0] as JSONObject)
    }
    private val messengerSocketOnOnLine = Emitter.Listener {
        messengerOnSocketListeners.messengerOnSocketUserStatusOnline()
    }
    private val messengerSocketOnOffLine = Emitter.Listener {
        messengerOnSocketListeners.messengerOnSocketUserStatusOffline()
    }
    private val messengerSocketOnDND = Emitter.Listener {
        messengerOnSocketListeners.messengerOnSocketUserStatusDND()
    }
    private val messengerSocketOnStatusOptionsOne = Emitter.Listener {
        messengerOnSocketListeners.messengerOnSocketUserStatusOptionOne()
    }
    private val messengerSocketOnStatusOptionsTwo = Emitter.Listener {
        messengerOnSocketListeners.messengerOnSocketUserStatusOptionTwo()
    }
    private val messengerSocketOnStatusOptionsThree = Emitter.Listener {
        messengerOnSocketListeners.messengerOnSocketUserStatusOptionThree()
    }*/
    private val messengerSocketOnTyping = Emitter.Listener {args->
        Log.e(TAG,"  typing ${args[0].toString()}")
        clientCallBackListener.typing(args[0] as JSONObject)
        //messengerOnSocketListeners.messengerOnSocketTypingStatus(args[0] as JSONObject)
    }
    private val messengerSocketOnUserLastSeen = Emitter.Listener {args ->
        Log.e(TAG,"  lastseen ${args[0].toString()}")
        //messengerOnSocketListeners.messengerOnSocketUserLastSeen(args[0] as JSONObject)
    }
    private val messengerSocketOnLogout = Emitter.Listener {}
    private val messengerSocketOnActiveDevices= Emitter.Listener {}
    private val messengerSocketOnVersionManagement = Emitter.Listener {}
    private val messengerSocketOnForceUpdate = Emitter.Listener {}
    private val messengerSocketOnServerCheck = Emitter.Listener {  }
    private val messengerSocketOnMarkFavourite = Emitter.Listener {args->
        updateConversationFavouriteStatus(args[0] as JSONObject)
    }
    private val messengerSocketOnMuteConversation = Emitter.Listener {args->
        updateConversationMuteStatus(args[0] as JSONObject)
    }
    private val messengerSocketOnGlobalConstantUpdated = Emitter.Listener {  }
    private val messengerSocketOnServerTime = Emitter.Listener {  }
    private val messengerSocketOnUserPlatformUpdated = Emitter.Listener {  }
    private val messengerSocketOnMFAPinSet = Emitter.Listener {  }
    private val messengerSocketOnReceiveMessage = Emitter.Listener {args->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG, " new receviMessage $jsonObject")
        var jsonArray = JSONArray()
        jsonArray.put(jsonObject)
        messageInsertOrUpdate(jsonArray,ConstantValues.SocketOn.RECEIVE)
    }
    private val messengerSocketOnMessageSent = Emitter.Listener {args ->
        Log.e(TAG,"messagesent "+args[0].toString())
        var jsonObject = args[0] as JSONObject
        var jsonArray = JSONArray()
        jsonArray.put(jsonObject)
        messageInsertOrUpdate(jsonArray,ConstantValues.SocketOn.SENT)
    }
    private val messengerSocketOnMessageDelivered = Emitter.Listener {args ->
        Log.e(TAG,"  messageDelivered ${args[0].toString()}")
        updateMessageDeliveredStatus(args[0] as JSONObject)
    }
    private val messengerSocketOnMessageRead = Emitter.Listener {args->
        Log.e(TAG,"  messageRead ${args[0].toString()}")
        updateMessageReadStatus(args[0] as JSONObject,false)
    }
    private val messengerSocketOnMessageReadByMe= Emitter.Listener { args ->
        Log.e(TAG,"  messageReadByMe ${args[0].toString()}")
        var jsonObject = args[0] as JSONObject
        jsonObject.put("receiver_id",jsonObject.optString("sender_id"))
        updateMessageReadStatus(args[0] as JSONObject,true)
    }
    private val messengerSocketOnErrorWhileSendingMessage = Emitter.Listener {}
    private val messengerSocketOnSyncOfflineMessages= Emitter.Listener {args ->
        Log.e(TAG,"  offlinemessages ${args[0].toString()}")
        messageInsertOrUpdate(args[0] as JSONArray,ConstantValues.SocketOn.SYNC)
    }
    private val messengerSocketOnMissingMessages = Emitter.Listener {args ->
        Log.e(TAG,"  missingMessages ${args[0].toString()}")
        messageInsertOrUpdate(args[0] as JSONArray,ConstantValues.SocketOn.SYNC)
    }
    private val messengerSocketOnDeleteMessage = Emitter.Listener {args ->
        deletedMessages(args[0] as JSONObject)
    }
    private val messengerSocketOnRecallMessage = Emitter.Listener {args ->
        Log.e(TAG,"  recallMessage ${args[0].toString()}")
        recalledMessages(args[0] as JSONObject)
    }
    private val messengerSocketOnReadReceiptMessage= Emitter.Listener { args ->
        readReceiptMessage(args[0] as JSONObject)
    }
    private val messengerSocketOnFlagMessage= Emitter.Listener { args ->
        flaggedMessage(args[0] as JSONObject)
    }
    private val messengerSocketOnRespondLater= Emitter.Listener {args ->
        respondLaterMessage(args[0] as JSONObject)
    }
    private val messengerSocketOnMessageEdit= Emitter.Listener {args ->
        Log.e("messageedit==> "," on "+args[0].toString())
        messageEdited(args[0] as JSONObject)
    }
    private val messengerSocketOnAttachmentPlaceholderDeleted= Emitter.Listener {args ->
        deleteThePlaceHolderMessage(args[0] as JSONObject)
    }
    private val messengerSocketOnGroupCreated = Emitter.Listener {args ->
        val dataObject = args[0] as JSONObject
        Log.e(TAG,"  newGroup ${dataObject.toString()}")
        groupCreated(if (dataObject.has("group_data")) dataObject.optJSONObject("group_data") else dataObject)
    }
    private val messengerSocketOnGroupUpdated = Emitter.Listener {args ->
        Log.e(TAG,"  updateGroup ${args[0].toString()}")
        groupUpdated(args[0] as JSONObject)
    }
    private val messengerSocketOnGroupKey = Emitter.Listener {}
    private val messengerSocketOnNewUserCreated = Emitter.Listener {args ->
        insertOrUpdateNewUser(args[0] as JSONObject)
    }
    private val messengerSocketOnUserPicUpdated =Emitter.Listener {args ->
        updateUserProfilePic(args[0] as JSONObject)
    }
    private val messengerSocketOnUserStatusUpdated = Emitter.Listener {args ->
        updateUserStatus(args[0] as JSONObject)
    }
    private val messengerSocketOnUserUpdated = Emitter.Listener {args ->
        insertOrUpdateNewUser(args[0] as JSONObject)
    }
    private val messengerSocketOnUserKey = Emitter.Listener {}
    private val messengerSocketOnInitPrivateChat =Emitter.Listener {}
    private val messengerSocketOnPrivateChatPermission =Emitter.Listener {}
    private val messengerSocketOnEndPrivateChat = Emitter.Listener {}
    private val messengerSocketOnAllBurnoutList = Emitter.Listener {}
    private val messengerSocketOnInitLocationTracking = Emitter.Listener {}
    private val messengerSocketOnLocationTrackingPermission =Emitter.Listener {}
    private val messengerSocketOnLocationTrackingSignal = Emitter.Listener {}
    private val messengerSocketOnStopLocationTracking = Emitter.Listener {}
    private val messengerSocketOnIsOrangeMemberAdded =Emitter.Listener {}
    private val messengerSocketOnIsOrangeMemberRemoved = Emitter.Listener {  }

    private val messengerSocketOnPermissionUpdate = Emitter.Listener {  }
    private val messengerSocketOnGlobalPermissionUpdate = Emitter.Listener {  }
    private val messengerSocketOnAccessPermissionStatusUpdated = Emitter.Listener {  }
    private val messengerSocketOnPlanUpdate = Emitter.Listener {args ->
        updateTMPlan(args[0] as JSONObject)
    }
    private val messengerSocketOnPlanError = Emitter.Listener {  }
    private val messengerSocketOnPlanExpired = Emitter.Listener { args ->
        updateTMPlan(args[0] as JSONObject)
    }



    private val messengerSocketOnArchivedMessages = Emitter.Listener {  }
    private val messengerSocketOnDeleteMessageHistory = Emitter.Listener {  }
    private val messengerSocketOnAutoDeleteHistory = Emitter.Listener {  }

    private val messengerSocketOnNewNotify = Emitter.Listener {args ->
        insertOrUpdateNotifyMessage(args[0] as JSONObject)
    }
    private val messengerSocketOnNotifyRecall = Emitter.Listener {args ->
        recallNotifyMessage(args[0] as JSONObject)
    }
    private val messengerSocketOnNotifyRead = Emitter.Listener {args ->
        updateNotifyMessageReadStatus(args[0] as JSONObject)
    }

    private val messengerSocketOnPinMessage = Emitter.Listener {args ->
        //updatePinMessage(args[0] as JSONObject)
    }

    private val messengerSocketOnNewUserRequestAuthorization = Emitter.Listener {  }
    private val messengerSocketOnUpdateUnitUserAuthorization = Emitter.Listener {  }
    private val messengerSocketOnUserRemovedFromUnit = Emitter.Listener {  }
    private val messengerSocketOnUserAddToUnit = Emitter.Listener {  }
    private val messengerSocketOnUnitRequestCount = Emitter.Listener {  }
    private val messengerSocketOnAppointmentMode = Emitter.Listener {  }
    private val messengerSocketOnTopicCreated = Emitter.Listener {  }
    private val messengerSocketOnTopicUpdated = Emitter.Listener {  }
    private val messengerSocketOnSwappedAppointments = Emitter.Listener {  }
    private val messengerSocketOnCcRemainder = Emitter.Listener {  }



    //call on event listener
    private val callSocketOnConnect = Emitter.Listener {
        Log.e(TAG," callsocket connect ")
    }
    private val callSocketOnDisConnect = Emitter.Listener {
        Log.e(TAG," callsocket dis connect ")
    }
    private val callSocketOnConnectError = Emitter.Listener {args->
        Log.e(TAG," callsocket connect error ")
        if (args[0]!=null){
            Log.e(TAG," callsocket connect error "+args[0].toString())
        }
    }
    private val callSocketOnNewProducers = Emitter.Listener {args ->
        Log.e(TAG," new produceres => "+args[0].toString())
        if (RoomClient.isAlreadyStarted){
            RoomClient.newProducer(args[0] as JSONArray)
        }
    }
    private val callSocketOnConsumerClosed = Emitter.Listener {args ->
        if (RoomClient.isAlreadyStarted){
            RoomClient.consumerClosed(args[0] as JSONObject)
        }
    }
    private val callSocketOnStopJoinCallRequest = Emitter.Listener {}
    private val callSocketOnParticipantStatusUpdated = Emitter.Listener {args ->

    }
    private val callSocketOnCallRequest = Emitter.Listener {args ->
        if (args[0]!=null){
            var jsonObject = args[0] as JSONObject
            Log.e(TAG, "callrequest==> "+jsonObject.toString())
            activeCallData = jsonObject
            ACTIVE_CALL_ID = jsonObject.optString("call_id")
            ACTIVE_CALL_TYPE = jsonObject.optInt("call_type")
            clientCallBackListener.onCallRequest(jsonObject)
            TroopMessengerCall.addParticipantsIntoList()
            TroopMessengerCall.addParticipantsIntoList()
            if (!RoomClient.isAlreadyStarted){
                RoomClient.initialize(CALL_TM_USER_ID, ACTIVE_CALL_ID, ACTIVE_CALL_TYPE,false,true,(ACTIVE_CALL_TYPE==ConstantValues.NewCallTypes.VIDEO_CALL))
            }
        }
    }
    private val callSocketOnCallPermission = Emitter.Listener {args ->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG," callpermission=> "+jsonObject.toString())
        if (jsonObject.optString("call_id")== ACTIVE_CALL_ID){
            jsonObject.put("uid", getUid(jsonObject.optString("user_id")))
            clientCallBackListener.onCallPermission(jsonObject)
        }
    }
    private val callSocketOnEndCall = Emitter.Listener {args ->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG," endcall=> "+jsonObject.toString())
        if (jsonObject.optString("call_id")== ACTIVE_CALL_ID) {
            activeCallData = JSONObject()
            ACTIVE_CALL_ID = ""
            ACTIVE_CALL_TYPE = 0
            participantUsersList = ArrayList()
            usbVideoCapture = null
            if (RoomClient.isAlreadyStarted) {
                RoomClient.release(false)
            }
            clientCallBackListener.onEndCall(jsonObject)
        }
    }
    private val callSocketOnRequestStream = Emitter.Listener {}
    private val callSocketOnStreamPermission = Emitter.Listener {}
    private val callSocketOnEndCallStream = Emitter.Listener {}
    private val callSocketOnJoinCall = Emitter.Listener {}
    private val callSocketOnMuteCallAudio = Emitter.Listener {args ->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG," mute audio=> "+jsonObject.toString())
        if (jsonObject.optString("call_id")== ACTIVE_CALL_ID) {
            val userId = jsonObject.optString("user_id")
            var uid = getUid(userId)
            jsonObject.put("uid",uid)
            activeCallData.optJSONObject("participants")!!.optJSONObject(userId)!!.put("audio_muted",jsonObject.optInt("audio_muted"))
            clientCallBackListener.onMuteCallAudio(jsonObject)
            TroopMessengerCall.updateParticipants(jsonObject,"audio_mute")
        }
    }
    private val callSocketOnMuteCallVideo = Emitter.Listener {args->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG," mute video=> "+jsonObject.toString())
        if (jsonObject.optString("call_id")== ACTIVE_CALL_ID) {
            val userId = jsonObject.optString("user_id")
            var uid = getUid(userId)
            jsonObject.put("uid",uid)
            activeCallData.optJSONObject("participants")!!.optJSONObject(userId)!!.put("video_muted",jsonObject.optInt("video_muted"))
            clientCallBackListener.onMuteCallVideo(jsonObject)
            TroopMessengerCall.updateParticipants(jsonObject,"video_mute")
        }
    }
    private val callSocketOnEndScreenShare = Emitter.Listener {args ->

    }
    private val callSocketOnCallStreamRequest = Emitter.Listener {args ->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG," stream request ${jsonObject.toString()}")
        if (jsonObject.optString("call_id")== ACTIVE_CALL_ID){
            ACTIVE_CALL_TYPE = jsonObject.optInt("call_type")
            if (ACTIVE_CALL_TYPE==ConstantValues.NewCallTypes.AUDIO_CALL || ACTIVE_CALL_TYPE==ConstantValues.NewCallTypes.VIDEO_CALL) {
                TroopMessengerCall.updateParticipants(jsonObject, if (ACTIVE_CALL_TYPE == ConstantValues.NewCallTypes.VIDEO_CALL) "video_add" else "audio_add")
            }
        }
    }
    private val callSocketOnCallMuteRequest = Emitter.Listener {args ->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG," mute request ${jsonObject.toString()}")
        if (jsonObject.optString("call_id")== ACTIVE_CALL_ID){
            if (jsonObject.optInt("type")==1){
                clientCallBackListener.hostAudioMuteRequest(jsonObject)
            }else{
                clientCallBackListener.hostVideoMuteRequest(jsonObject)
            }
        }
    }
    private val callSocketOnCallRemoveUser = Emitter.Listener {args ->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG," remove user ${jsonObject.toString()}")
        if (jsonObject.optString("call_id")== ACTIVE_CALL_ID){
            if (jsonObject.optString("user_id")== CALL_TM_USER_ID){
                activeCallData = JSONObject()
                ACTIVE_CALL_ID = ""
                ACTIVE_CALL_TYPE = 0
                participantUsersList = ArrayList()
                usbVideoCapture = null
                if (RoomClient.isAlreadyStarted) {
                    RoomClient.release(false)
                }
            }
            clientCallBackListener.onEndCall(jsonObject)
            return@Listener
        }
        TroopMessengerCall.updateParticipants(jsonObject,"remove")

    }
    private val callSocketOnCallNewParticipants = Emitter.Listener {args ->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG," newparticipatns : "+jsonObject.toString())
        if (jsonObject.optString("call_id")== ACTIVE_CALL_ID) {
            if (jsonObject.has("participants")) {
                //var participantsData = jsonObject.optJSONObject("participants")
                TroopMessengerCall.addNewParticipants(jsonObject)
                clientCallBackListener.onCallNewParticipants(jsonObject)
            }
        }
    }
    private val callSocketOnCallHostUpdated = Emitter.Listener {args ->
        var jsonObject  = args[0] as JSONObject
    }
    private val callSocketOnCallUserVideoStatusUpdated = Emitter.Listener {args ->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG," video status => "+jsonObject.toString())
        if (jsonObject.optString("call_id")== ACTIVE_CALL_ID){
            if (activeCallData.optJSONObject("participants")!!.has(jsonObject.optString("user_id"))){
                activeCallData.optJSONObject("participants")!!.optJSONObject(jsonObject.optString("user_id"))!!.put("video_active",jsonObject.optInt("video_active"))
                activeCallData.optJSONObject("participants")!!.optJSONObject(jsonObject.optString("user_id"))!!.put("video_muted",0)
                jsonObject.put("uid", getUid(jsonObject.optString("user_id")))
                clientCallBackListener.onCallUserVideoStatusUpdated(jsonObject)
                TroopMessengerCall.updateParticipants(jsonObject,"video_status_update")
            }
        }
    }
    private val callSocketOnLeaveCall = Emitter.Listener {args ->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG," leave call => "+jsonObject.toString())
        if (jsonObject.optString("call_id")== ACTIVE_CALL_ID){
            if (jsonObject.optString("user_id")== CALL_TM_USER_ID){
                activeCallData = JSONObject()
                ACTIVE_CALL_ID = ""
                ACTIVE_CALL_TYPE = 0
                participantUsersList = ArrayList()
                usbVideoCapture=null
                if (RoomClient.isAlreadyStarted) {
                    RoomClient.release(false)
                }
                clientCallBackListener.onEndCall(jsonObject)
                return@Listener
            }
            if (activeCallData!=null && activeCallData.has("participants") && activeCallData.optJSONObject("participants")!!.has(jsonObject.optString("user_id"))){
                activeCallData.optJSONObject("participants")!!.optJSONObject(jsonObject.optString("user_id"))!!.put("status",ConstantValues.CallUserStatus.CALL_USER_STATUS_LEFT)
                jsonObject.put("status",ConstantValues.CallUserStatus.CALL_USER_STATUS_LEFT)
                var uid = getUid(jsonObject.optString("user_id"))
                jsonObject.put("uid",uid)
                clientCallBackListener.onCallParticipantStatusUpdated(jsonObject)
                TroopMessengerCall.updateParticipants(jsonObject,"user_status_update")
            }

        }

    }
    private val callSocketOnRequestJoinCall = Emitter.Listener {}
    private val callSocketOnJoinCallPermission = Emitter.Listener {}
    private val callSocketOnCallParticipantStatusUpdate = Emitter.Listener {args ->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG, " participants status : $jsonObject")
        if (jsonObject.optString("call_id")== ACTIVE_CALL_ID){
            if (activeCallData.optJSONObject("participants")!!.has(jsonObject.optString("user_id"))){
                activeCallData.optJSONObject("participants")!!.optJSONObject(jsonObject.optString("user_id"))!!.put("status",jsonObject.optInt("status"))
                jsonObject.put("uid", getUid(jsonObject.optString("user_id")))
                clientCallBackListener.onCallParticipantStatusUpdated(jsonObject)
                TroopMessengerCall.updateParticipants(jsonObject,"user_status_update")
            }
        }
    }
    private val callSocketOnHostMuteAudio = Emitter.Listener {args->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG," host mute audio=> "+jsonObject.toString())
        if (jsonObject.optString("call_id")== ACTIVE_CALL_ID) {
            val userId = jsonObject.optString("user_id")
            var uid = getUid(userId)
            jsonObject.put("uid",uid)
            activeCallData.optJSONObject("participants")!!.optJSONObject(userId)!!.put("audio_muted",jsonObject.optInt("audio_muted"))
            clientCallBackListener.onMuteCallAudio(jsonObject)
            if (userId== CALL_TM_USER_ID && RoomClient.localAudioTrack!=null) {
                RoomClient.localAudioTrack!!.setEnabled(false)
            }
            TroopMessengerCall.updateParticipants(jsonObject,"audio_mute")
        }
    }
    private val callSocketOnHostMuteVideo = Emitter.Listener {args->
        var jsonObject = args[0] as JSONObject
        Log.e(TAG," host mute video=> "+jsonObject.toString())
        if (jsonObject.optString("call_id")== ACTIVE_CALL_ID) {
            val userId = jsonObject.optString("user_id")
            var uid = getUid(userId)
            jsonObject.put("uid",uid)
            activeCallData.optJSONObject("participants")!!.optJSONObject(userId)!!.put("audio_muted",jsonObject.optInt("audio_muted"))
            clientCallBackListener.onMuteCallAudio(jsonObject)
            if (userId== CALL_TM_USER_ID && RoomClient.localVideoTrack!=null){
                RoomClient.localVideoTrack!!.setEnabled(false)
            }
            TroopMessengerCall.updateParticipants(jsonObject,"video_mute")
        }
    }
    private val callSocketOnEndJointlyCode = Emitter.Listener {}
    private val callSocketOnCcMeetingStarted = Emitter.Listener {}
    private val callSocketOnActiveCall = Emitter.Listener {}
    private val callSocketOnOnCall = Emitter.Listener {}
    private val callSocketOnBridgeCallInitiated = Emitter.Listener {}
    private val callSocketOnBridgeCallToHost = Emitter.Listener {}
    private val socketOnCallSignal = Emitter.Listener {}

    private fun updateTheCompanyNameInDB(jsonObject: JSONObject){

    }
    private fun updateConversationFavouriteStatus(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        /*if (jsonObject.optInt("is_group")==ConstantValues.IS_GROUP_CONVERSATION){
            dataBase!!.messengerGroupDAO.updateGroupFavouriteStatus(jsonObject.optString("entity_id"),jsonObject.optInt("is_favourite"),updatedAtTime!!)
        }else {
            dataBase!!.userDAO.updateUserFavouriteStatus(jsonObject.optString("entity_id"),jsonObject.optInt("is_favourite"),updatedAtTime!!)
        }*/
    }
    private fun updateConversationMuteStatus(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        /*if (jsonObject.optInt("is_group")==ConstantValues.IS_GROUP_CONVERSATION){
            dataBase!!.messengerGroupDAO.updateGroupMuteStatus(jsonObject.optString("entity_id"),jsonObject.optInt("is_muted"),updatedAtTime!!)
        }else {
            dataBase!!.userDAO.updateUserMuteStatus(jsonObject.optString("entity_id"),jsonObject.optInt("is_muted"),updatedAtTime!!)
        }*/
    }
    fun messageInsertOrUpdate(jsonArray: JSONArray,syncEmitText:String){
        Thread{
            if (jsonArray!=null && jsonArray.length()>0){
                //val entityIdObject = JSONObject()
                var arrayLength = jsonArray.length()
                for (i in 0 until arrayLength){
                    var messageObject = jsonArray.optJSONObject(i)
                    messageObject.put("is_sync",1)
                    val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
                    messageObject.put("updated_at",updatedAtTime)
                    messageObject = Encryption.parseMessage(messageObject, APPLICATION_TM_LOGIN_USER_ID)
                    messageObject.put("original_message",messageObject.optString("original_message"))



                    val messenger = Gson().fromJson(messageObject.toString(),Messenger::class.java)

                    var entityId = if (messenger.is_group==ConstantValues.Entity.IS_GROUP) messenger.receiver_id.toString() else if (messenger.is_group==ConstantValues.Entity.IS_USER && messenger.sender_id.toString()== APPLICATION_TM_LOGIN_USER_ID) messenger.receiver_id.toString() else messenger.sender_id.toString()
                    var isSenderMe = if (messenger.sender_id.toString()==APPLICATION_TM_LOGIN_USER_ID) 1 else 0
                    var insertMessage = false
                    var lastLocalId:Long = 0



                    if (messenger!=null){
                        if (dataBase!!.messengerDAO.checkMessageExists(messenger.message_id)>0){
                            var localMessenger = dataBase!!.messengerDAO.fetchMessageByMessageId(messenger.message_id)
                            messenger.ID = localMessenger.ID
                            messenger.isMine = isSenderMe
                            messenger.attachment = localMessenger.attachment
                            messenger.attachment_downloaded = localMessenger.attachment_downloaded
                            messenger.sender_name = localMessenger.sender_name
                            messenger.preview_link = localMessenger.preview_link
                            messenger.attachment_path = localMessenger.attachment_path
                            messenger.local_attachment_path = localMessenger.local_attachment_path
                            lastLocalId = messenger.ID
                            dataBase!!.messengerDAO.updateMessage(messenger)
                        }else
                            if (messageObject.optString("reference_id").contains(sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")!!)){
                                var id = messageObject.optString("reference_id").replace(sharedPreferences!!.getString(SharePreferenceConstants.DEVICE_ID,"")!!,"")
                                var localMessenger = dataBase!!.messengerDAO.fetchMessageByLocalId(id)
                                if (localMessenger!=null) {
                                    messenger.ID = localMessenger.ID
                                    messenger.isMine = isSenderMe
                                    messenger.attachment = localMessenger.attachment
                                    messenger.attachment_downloaded = localMessenger.attachment_downloaded
                                    messenger.sender_name = localMessenger.sender_name
                                    messenger.preview_link = localMessenger.preview_link
                                    messenger.attachment_path = localMessenger.attachment_path
                                    messenger.local_attachment_path = localMessenger.local_attachment_path
                                    lastLocalId = messenger.ID
                                    dataBase!!.messengerDAO.updateMessage(messenger)
                                }else{
                                    val messageSenderName = dataBase!!.userDAO.fetchUseName(messageObject.optString("sender_id"))
                                    messenger.sender_name = messageSenderName
                                    messenger.isMine = isSenderMe
                                    dataBase!!.messengerDAO.insertMessage(messenger)
                                    lastLocalId = dataBase!!.messengerDAO.fetchLastLocalId()
                                    insertMessage= true
                                }
                            }/*else if (dataBase!!.messengerDAO.checkMessageExists(messenger.message_id)>0){
                        var localMessenger = dataBase!!.messengerDAO.fetchMessageByMessageId(messenger.message_id)
                        messenger.ID = localMessenger.ID
                        messenger.isMine = isSenderMe
                        messenger.attachment = localMessenger.attachment
                        messenger.attachment_downloaded = localMessenger.attachment_downloaded
                        messenger.sender_name = localMessenger.sender_name
                        messenger.preview_link = localMessenger.preview_link
                        messenger.attachment_path = localMessenger.attachment_path
                        messenger.local_attachment_path = localMessenger.local_attachment_path
                        dataBase!!.messengerDAO.updateMessage(messenger)
                    }*/else{
                                val messageSenderName = dataBase!!.userDAO.fetchUseName(messageObject.optString("sender_id"))
                                messenger.isMine = isSenderMe
                                messenger.sender_name = messageSenderName
                                dataBase!!.messengerDAO.insertMessage(messenger)
                                lastLocalId = dataBase!!.messengerDAO.fetchLastLocalId()
                                insertMessage = true
                            }
                        var status = ConstantValues.MessageStatus.SENT
                        if (messenger.status==0){
                            status = ConstantValues.MessageStatus.DELETE
                        }else
                        if (messenger.status==ConstantValues.MessageStatus.MESSAGE_RECALLED){
                            status = ConstantValues.MessageStatus.RECALLED
                        }else
                        if (messenger.is_read==1){
                            status = ConstantValues.MessageStatus.READ
                        }else if (messenger.is_delivered==1){
                            status = ConstantValues.MessageStatus.DELIVERED
                        }


                        Log.e("messagestatus===> "," "+status+"   "+messenger.status+"   "+messageObject.optInt("status")+"   "+messageObject.optString("message_id"))

                        if (messenger.is_delivered!=1 && messenger.sender_id.toString()!= APPLICATION_TM_LOGIN_USER_ID){
                            var jsonObject = JSONObject()
                            jsonObject.put("access_token", APP_ACCESS_TOKEN)
                            jsonObject.put("message_id",messenger.message_id)
                            jsonObject.put("is_group",messenger.is_group)
                            jsonObject.put("is_room",messenger.is_room)
                            dataBase!!.messengerDAO.updateMessageDeliveredStatus(messenger.message_id,ConstantValues.fetchCurrentTimeInUTC())
                            status = ConstantValues.MessageStatus.DELIVERED
                            TroopMessenger.sendMessageDeliveryStatus(jsonObject)
                        }
                        if (messenger.sender_id.toString()!= APPLICATION_TM_LOGIN_USER_ID && messenger.is_read!=1 && TroopMessengerClient.currentScreen==ConstantValues.CurrentScreen.CONVERSATION && entityId==TroopMessengerClient.convEntityId && convEntityType==messenger.is_group+1){
                            var jsonObject = JSONObject()
                            if (messenger.message_id>0) {
                                jsonObject.put("access_token", APP_ACCESS_TOKEN)
                                jsonObject.put("message_id", messenger.message_id)
                                jsonObject.put("is_group", messenger.is_group)
                                jsonObject.put("is_room", messenger.is_room)
                                Log.e(TAG,"readmesages===>  called "+ APPLICATION_TM_LOGIN_USER_ID+"  "+messenger.sender_id)
                                dataBase!!.messengerDAO.updateMessageReadStatus(messenger.message_id,ConstantValues.fetchCurrentTimeInUTC())
                                status = ConstantValues.MessageStatus.READ
                                TroopMessenger.sendMessageReadStatus(jsonObject)
                            }
                        }
                        if (syncEmitText==ConstantValues.SocketOn.SENT && messenger.sender_id.toString()== APPLICATION_TM_LOGIN_USER_ID && messenger.receiver_id.toString()== APPLICATION_TM_LOGIN_USER_ID && messenger.is_group==ConstantValues.Entity.IS_USER){
                            if (messenger.message_id>0) {
                                var jsonObject = JSONObject()
                                jsonObject.put("access_token", APP_ACCESS_TOKEN)
                                jsonObject.put("message_id",messenger.message_id)
                                jsonObject.put("is_group",messenger.is_group)
                                jsonObject.put("is_room",messenger.is_room)
                                dataBase!!.messengerDAO.updateMessageDeliveredStatus(messenger.message_id,ConstantValues.fetchCurrentTimeInUTC())
                                TroopMessenger.sendMessageDeliveryStatus(jsonObject)
                                dataBase!!.messengerDAO.updateMessageReadStatus(messenger.message_id,ConstantValues.fetchCurrentTimeInUTC())
                                status = ConstantValues.MessageStatus.READ
                                TroopMessenger.sendMessageReadStatus(jsonObject)
                            }
                        }

                        if (status!=ConstantValues.MessageStatus.READ && syncEmitText==ConstantValues.SocketOn.RECEIVE
                            && TroopMessengerClient.currentScreen!=ConstantValues.CurrentScreen.RECENT.toInt()
                            && ((TroopMessengerClient.currentScreen==ConstantValues.CurrentScreen.OTHER) || (TroopMessengerClient.currentScreen==ConstantValues.CurrentScreen.CONVERSATION
                                    &&  entityId!=TroopMessengerClient.convEntityId && convEntityType!=messenger.is_group+1))){
                            var name : String = ""
                            if (messenger.is_group==1){
                                name = dataBase!!.messengerGroupDAO.fetchGroupName(entityId)
                            }else{
                                name = dataBase!!.userDAO.fetchUserName(entityId)
                            }
                            var message :String = messenger.sender_name+" : "
                            if (status==ConstantValues.MessageStatus.RECALLED){
                                message = " This message recalled "
                            }else {
                                when (messageObject.optInt("message_type")) {
                                    ConstantValues.MessageTypes.TEXT_MESSAGE->{
                                        message+= messageObject.optString("message")
                                    }
                                    ConstantValues.MessageTypes.ATTACHMENT->{
                                        message+= "Attachment"
                                    }
                                    ConstantValues.MessageTypes.CONTACT_MESSAGE->{
                                        message+= "Contact"
                                    }
                                    ConstantValues.MessageTypes.LOCATION_MESSAGE->{
                                        message+= "Location"
                                    }
                                    else ->{
                                        if (Helper.isSeverMessage(messenger.message_type)){
                                            message+= Helper.isGroupUpdatedMessage(messenger.message_type,if (messenger.sender_id.toString()== APPLICATION_TM_LOGIN_USER_ID)"You" else messenger.sender_name!!,messenger.message,isSenderMe==1,
                                                APPLICATION_TM_LOGIN_USER_ID,messenger.sender_id.toString())
                                        }else{
                                            message+= messageObject.optString("message")
                                        }
                                    }

                                }
                            }
                            messageObject.put("message",message)
                            messageObject.put("entity_name",name)
                            messageObject.put("entity_id",entityId)
                            messageObject.put("entity_type",messenger.is_group+1)
                            messageObject.put("sender_name",messenger.sender_name)
                            messageObject.put("entity_uid",if (messenger.is_group==0) messenger.sender_uid else 0)
                            Log.e("entity----> ",""+name+"   "+messageObject.optString("entity_name"))
                            clientCallBackListener.pushNotification(messageObject)
                            //sendPushNotification
                        }
                        if (jsonArray.length()==1){
                            if (insertMessage) {
                                if (messenger.is_group == 0) {
                                    dataBase!!.userDAO.updateUserRecentMessage(lastLocalId, messenger.message_id,if (messenger.message != null) messenger.message!! else "", messenger.created_at!!, messenger.message_type, status, entityId, APP_LOGIN_USER_NAME, isSenderMe, if (messenger.sender_name != null) messenger.sender_name!! else "", messenger.sender_id.toString())
                                } else {
                                    dataBase!!.messengerGroupDAO.updateRecentGroupMessage(lastLocalId, messenger.message_id, if (messenger.message != null) messenger.message!! else "", messenger.created_at!!, messenger.message_type, status, messenger.receiver_id.toString(), messenger.sender_id.toString(), isSenderMe, if (messenger.sender_name != null) messenger.sender_name!! else "", APPLICATION_TM_LOGIN_USER_ID)
                                }
                            }else if (lastLocalId>0){
                                if (messenger.is_group==0) {
                                    var userLastMessage = dataBase!!.userDAO.fetchUser(entityId)
                                    if (userLastMessage!=null && userLastMessage.last_message_local_id==messenger.ID){
                                        userLastMessage.last_message_status = status
                                        userLastMessage.last_message_message_id = messenger.message_id
                                        dataBase!!.userDAO.updateUserRecentMessage(lastLocalId, messenger.message_id,if (messenger.message != null) messenger.message!! else "", messenger.created_at!!, messenger.message_type, status, entityId, APP_LOGIN_USER_NAME, isSenderMe, if (messenger.sender_name != null) messenger.sender_name!! else "", messenger.sender_id.toString())
                                    }
                                }else if (messenger.is_group==1){
                                    var groupLastMessage = dataBase!!.messengerGroupDAO.fetchingGroupData(entityId)
                                    if (groupLastMessage!=null && groupLastMessage.last_message_local_id==messenger.ID){
                                        groupLastMessage.last_message_status = status
                                        groupLastMessage.last_message_message_id = messenger.message_id
                                        dataBase!!.messengerGroupDAO.updateRecentGroupMessage(lastLocalId, messenger.message_id, if (messenger.message != null) messenger.message!! else "", messenger.created_at!!, messenger.message_type, status, messenger.receiver_id.toString(), messenger.sender_id.toString(), isSenderMe, if (messenger.sender_name != null) messenger.sender_name!! else "", APPLICATION_TM_LOGIN_USER_ID)
                                    }
                                }
                            }
                        }
                        /*if (jsonArray.length()<=1 ) {
                            if (insertMessage) {
                                var localMessageId = dataBase!!.messengerDAO.fetchLastLocalId()
                                if (messenger.is_group == 0) {
                                    dataBase!!.userDAO.updateUserRecentMessage(localMessageId, messenger.message_id, if (messenger.message != null) messenger.message!! else "", messenger.created_at!!, messenger.message_type, ConstantValues.MessageStatus.SENT, entityId, APP_LOGIN_USER_NAME, isSenderMe, if (messenger.sender_name != null) messenger.sender_name!! else "", messenger.sender_id.toString())
                                } else {
                                    dataBase!!.messengerGroupDAO.updateRecentGroupMessage(localMessageId, messenger.message_id, if (messenger.message != null) messenger.message!! else "", messenger.created_at!!, messenger.message_type, ConstantValues.MessageStatus.SENT, messenger.receiver_id.toString(), messenger.sender_id.toString(), isSenderMe, if (messenger.sender_name != null) messenger.sender_name!! else "", APPLICATION_TM_LOGIN_USER_ID)
                                }
                            }else{
                                if (messenger.is_group==0) {
                                    var userLastMessage = User()
                                    userLastMessage = dataBase!!.userDAO.fetchUser(entityId)
                                    if (userLastMessage!=null && userLastMessage.last_message_local_id==messenger.ID.toInt()){
                                        dataBase!!.userDAO.update(userLastMessage)
                                    }
                                }else if (messenger.is_group==1){
                                    var groupLastMessage = MessengerGroup()
                                    groupLastMessage = dataBase!!.messengerGroupDAO.fetchingGroupData(entityId)
                                    if (groupLastMessage!=null && groupLastMessage.last_message_local_id==messenger.ID.toInt()){
                                        dataBase!!.messengerGroupDAO.updateGroup(groupLastMessage)
                                    }
                                }
                            }
                        }*/
                        if (syncEmitText==ConstantValues.SocketOn.RECEIVE) {
                            TroopMessenger.newMessageVerifyChatArchive(
                                entityId,
                                messenger.is_group + 1
                            )
                        }

                        /*when(syncEmitText){
                            ConstantValues.SocketOn.SENT->{

                            }
                        }*/

                    }


                    /*if (!entityIdObject.has(entityId+"_"+messenger.is_group)){
                        var jsonObject = JSONObject()
                        jsonObject.put("entity_id",entityId)
                        jsonObject.put("is_group",messenger.is_group)
                        entityIdObject.put(entityId+"_"+messenger.is_group,jsonObject)
                    }*/
                }
                if (jsonArray.length()>1) {
                    updateRecentUserGroupMessage()
                }
                TroopClient.isRegisterIsInProcessing = false

            }
        }.start()
        clientCallBackListener.loadingMessages(false)

    }
    internal fun updateRecentUserGroupMessage(){
        try {
            var userQuery =
                "SELECT max(id) as id,message_id,1 as entity,m.sender_name as member_name,m.sender_id as member_id,CASE WHEN is_sync=0 THEN 0 WHEN status=2 THEN 4 WHEN is_read = 1 THEN 1 ELSE 2 END as message_status,CASE WHEN sender_id=$APPLICATION_TM_LOGIN_USER_ID THEN 1 ELSE 0 END as is_sender,sum(CASE WHEN receiver_id =$APPLICATION_TM_LOGIN_USER_ID AND is_read = 0 THEN 1 ELSE 0 END) as unread_messages_count,CASE WHEN sender_id > receiver_id THEN sender_id || '_' || receiver_id ELSE receiver_id || '_' || sender_id END as contact_id,sender_id,receiver_id,CASE WHEN sender_id =$APPLICATION_TM_LOGIN_USER_ID THEN receiver_id ELSE sender_id END as entity_id,CASE WHEN status=2 THEN '' ELSE message END as message,message_type,created_at FROM chat as m WHERE is_group = 0 GROUP BY contact_id ORDER BY created_at DESC"
            var userRecentList: List<RecentLastMessage> =
                dataBase!!.messengerDAO.fetchUserOrGroupRecentMessageList(SimpleSQLiteQuery(userQuery))
            var groupQuery =
                "SELECT max(c.id) as id,2 as entity,IFNULL(u.user_id,0) as member_id,IFNULL(u.name, '') as member_name,'' as contact_id,c.message_id,CASE WHEN c.is_sync=0 THEN 0 WHEN c.status=2 THEN 4 WHEN c.is_read = 1 THEN 1 ELSE 2 END as message_status,CASE WHEN c.sender_id=$APPLICATION_TM_LOGIN_USER_ID THEN 1 ELSE 0 END as is_sender,sum(CASE WHEN c.receiver_id =$APPLICATION_TM_LOGIN_USER_ID AND c.is_read = 0 THEN 1 ELSE 0 END) as unread_messages_count,c.sender_id,c.receiver_id,c.receiver_id as entity_id,CASE WHEN c.status=2 THEN '' ELSE c.message END as message,c.message_type,c.created_at FROM chat as c LEFT JOIN user as u ON u.user_id=c.sender_id WHERE c.is_group = 1 GROUP BY c.receiver_id ORDER BY c.created_at DESC"
            var groupRecentList: List<RecentLastMessage> =
                dataBase!!.messengerDAO.fetchUserOrGroupRecentMessageList(
                    SimpleSQLiteQuery(groupQuery)
                )
            for (i in userRecentList.indices) {
                Log.e("recent===> ",""+Gson().toJson(userRecentList[i]))
                if (userRecentList[i].member_name == null) {
                    userRecentList[i].member_name = APP_LOGIN_USER_NAME
                }
                dataBase!!.userDAO.updateUserRecentMessage(
                    userRecentList[i].id,
                    userRecentList[i].message_id,
                    if (userRecentList[i].message!=null) userRecentList[i].message!! else "",
                    userRecentList[i].created_at!!,
                    userRecentList[i].message_type,
                    userRecentList[i].message_status,
                    userRecentList[i].entity_id.toString(),
                    APPLICATION_TM_LOGIN_USER_ID,
                    userRecentList[i].is_sender,
                    userRecentList[i].member_name!!,
                    userRecentList[i].sender_id.toString()
                )
            }
            for (i in groupRecentList.indices) {
                if (groupRecentList[i].member_name == null) {
                    groupRecentList[i].member_name = APP_LOGIN_USER_NAME
                }
                dataBase!!.messengerGroupDAO.updateRecentGroupMessage(
                    groupRecentList[i].id,
                    groupRecentList[i].message_id,
                    if (groupRecentList[i].message!=null) groupRecentList[i].message!! else "",
                    groupRecentList[i].created_at!!,
                    groupRecentList[i].message_type,
                    groupRecentList[i].message_status,
                    groupRecentList[i].entity_id.toString(),
                    groupRecentList[i].member_id.toString(),
                    groupRecentList[i].is_sender,
                    groupRecentList[i].member_name!!,
                    APPLICATION_TM_LOGIN_USER_ID
                )
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    private fun updateMessageDeliveredStatus(jsonObject: JSONObject){
        if (jsonObject.optInt("is_delivered_to_all")==ConstantValues.ACTIVE){
            val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
            dataBase!!.messengerDAO.updateMessageDeliveredStatus(jsonObject.optLong("message_id"),updatedAtTime!!)
            var localId = dataBase!!.messengerDAO.fetchLastMessageId()
            if (jsonObject.optInt("is_group")==ConstantValues.Entity.IS_USER){
                var query  = "UPDATE user SET last_message_status = ${ConstantValues.MessageStatus.DELIVERED} WHERE user_id = ${jsonObject.optString("receiver_id")} AND (last_message_id IN (${jsonObject.optString("message_id").replace("[","").replace("]","")}) OR last_message_local_id =$localId) AND last_message_status < ${ConstantValues.MessageStatus.DELIVERED}"
                dataBase!!.userDAO.updateUserLastMessageStatus(SimpleSQLiteQuery(query))
            }else if (jsonObject.optInt("is_group")==ConstantValues.Entity.IS_GROUP){
                var query  = "UPDATE chat_group SET last_message_status = ${ConstantValues.MessageStatus.DELIVERED} WHERE group_id = ${jsonObject.optString("group_id")} AND (last_message_id IN (${jsonObject.optString("message_id").replace("[","").replace("]","")}) OR last_message_local_id =$localId) AND last_message_status < ${ConstantValues.MessageStatus.DELIVERED}"
                dataBase!!.messengerGroupDAO.updateGroupLastMessageStatus(SimpleSQLiteQuery(query))
            }
        }

    }
    private fun updateMessageReadStatus(jsonObject: JSONObject,readByMe:Boolean){
        if (jsonObject.optInt("is_read_by_all")==ConstantValues.ACTIVE){
            val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
            dataBase!!.messengerDAO.updateMessageReadStatus(jsonObject.optLong("message_id"),updatedAtTime!!)
            var localId = dataBase!!.messengerDAO.fetchLastMessageId()
            if (jsonObject.optInt("is_group")==ConstantValues.Entity.IS_USER){
                var query  = "UPDATE user SET last_message_status = ${ConstantValues.MessageStatus.IS_READ} WHERE user_id = ${jsonObject.optString("receiver_id")} AND (last_message_id=${jsonObject.optString("message_id")} OR last_message_local_id =$localId) AND last_message_status < ${ConstantValues.MessageStatus.IS_READ}"
                dataBase!!.userDAO.updateUserLastMessageStatus(SimpleSQLiteQuery(query))
                if (readByMe){
                    dataBase!!.userDAO.updateUserUnreadCount(jsonObject.optString("sender_id"),
                        APPLICATION_TM_LOGIN_USER_ID)
                }
            }else if (jsonObject.optInt("is_group")==ConstantValues.Entity.IS_GROUP){
                var query  = "UPDATE chat_group SET last_message_status = ${ConstantValues.MessageStatus.IS_READ} WHERE group_id = ${jsonObject.optString("group_id")} AND (last_message_id=${jsonObject.optString("message_id")} OR last_message_local_id =$localId) AND last_message_status < ${ConstantValues.MessageStatus.IS_READ}"
                dataBase!!.messengerGroupDAO.updateGroupLastMessageStatus(SimpleSQLiteQuery(query))
                if (readByMe){
                    dataBase!!.messengerGroupDAO.updateUnreadCount(APPLICATION_TM_LOGIN_USER_ID,jsonObject.optString("group_id"))
                }
            }
        }
    }
    private fun deletedMessages(jsonObject: JSONObject){
        val messageArray = jsonObject.optJSONArray("message_id")
        if (messageArray!=null && messageArray.length()>0) {
            var listOfMessageIds = Helper.toLongArray(messageArray)
            dataBase!!.messengerDAO.deleteServerMessages(listOfMessageIds)
            updateRecentUserGroupMessage()
            //update unread/read receipt/respond later counts
        }
    }
    private fun recalledMessages(jsonObject: JSONObject){
        val messageArray = jsonObject.optJSONArray("message_id")
        Log.e(TAG," recall message-> "+jsonObject.toString())
        if (messageArray!=null && messageArray.length()>0) {
            var listOfMessageIds = Helper.toStringArray(messageArray)
            val object1 = JSONObject()
            object1.put("name", jsonObject.optString("member_name"))
            object1.put("user_id", jsonObject.optString("user_id"))
            val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
            var messageObject = JSONObject()
            messageObject.put("name", APP_LOGIN_USER_NAME)
            messageObject.put("user_id", APPLICATION_TM_LOGIN_USER_ID)
            dataBase!!.messengerDAO.updateMessageRecall(
                ConstantValues.MessageStatus.MESSAGE_RECALLED,
                listOfMessageIds,
                updatedAtTime!!,
                messageObject.toString()
            )
            updateRecentUserGroupMessage()
            //update unread/read receipt/respond later counts
        }
    }
    private fun readReceiptMessage(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        var entityId = jsonObject.optString("receiver_id")
        if (entityId=="" && jsonObject.optInt("is_group")==ConstantValues.IS_USER_CONVERSATION){
            entityId = jsonObject.optString("sender_id")
        }
        /*if (jsonObject.optInt("is_read_by_all")==ConstantValues.ACTIVE){
            dataBase!!.messengerDAO.updateMessageReadReceiptStatus(jsonObject.optString("message_id"),updatedAtTime!!)
             if (jsonObject.optInt("is_group")==ConstantValues.IS_USER_CONVERSATION){
                 val unreadReadReceiptCount = dataBase!!.messengerDAO.fetchUserUnreadReadReceiptCount("",entityId)
                 dataBase!!.userDAO.updateUserReadReceiptCount(unreadReadReceiptCount,entityId)
            }else {
                 val unreadReadReceiptCount =  dataBase!!.messengerDAO.fetchGroupUnreadReadReceiptCount("",entityId)
                 dataBase!!.messengerGroupDAO.updateGroupReadReceiptCount(unreadReadReceiptCount,entityId)
            }
        }*/
    }
    private fun flaggedMessage(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        //dataBase!!.messengerDAO.updateMessageFlagStatus(jsonObject.optInt("flag"),jsonObject.optLong("message_id"),updatedAtTime!!)
    }
    private fun respondLaterMessage(jsonObject: JSONObject) {
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        var entityId = jsonObject.optString("receiver_id")
        if (entityId=="" && jsonObject.optInt("is_group")==ConstantValues.IS_USER_CONVERSATION){
            entityId = jsonObject.optString("sender_id")
        }
        /*dataBase!!.messengerDAO.updateMessageRespondLaterStatus(jsonObject.optInt("is_respond_later"),jsonObject.optLong("message_id"),updatedAtTime!!)
        if (jsonObject.optInt("is_group")==ConstantValues.IS_USER_CONVERSATION){
            var respondLaterCount = dataBase!!.messengerDAO.fetchUserRespondLaterCount("",entityId)
            dataBase!!.userDAO.updateUserRespondLaterCount(respondLaterCount,entityId)
        }else {
            var respondLaterCount =  dataBase!!.messengerDAO.fetchGroupRespondLaterCount("",entityId)
            dataBase!!.messengerGroupDAO.updateGroupRespondLaterCount(respondLaterCount,entityId)
        }*/
    }
    internal fun messageEdited(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        /*if (jsonObject.optInt("message_type")==ConstantValues.MessageTypes.CODE_SNIPPET){
            // code for code snippet message
        }else{*/
        var entityId : String = jsonObject.optString("receiver_id")
        if (jsonObject.optInt("is_group")==0 && entityId== APPLICATION_TM_LOGIN_USER_ID){
            entityId = jsonObject.optString("sender_id")
        }
        var decryptMessage = Encryption.decryptMessage(Encryption.EncryptionType.STANDARD,jsonObject.optString("message"),entityId,jsonObject.optInt("is_group")+1,"",jsonObject.optString("sender_id"),jsonObject.optString("receiver_id"))
            dataBase!!.messengerDAO.updateMessageEdit(decryptMessage,jsonObject.optInt("is_edited"),jsonObject.optString("edited_at"),updatedAtTime!!,jsonObject.optInt("message_type"),jsonObject.optLong("message_id"),jsonObject.optString("caption"))
        if (jsonObject.optInt("is_group")==0) {
            var userLastMessage = User()
            userLastMessage = dataBase!!.userDAO.fetchUser(entityId)
            if (userLastMessage!=null && userLastMessage.last_message_message_id==jsonObject.optLong("message_id")){
                userLastMessage.last_message = decryptMessage
                userLastMessage.last_message_type = jsonObject.optInt("message_type")
                dataBase!!.userDAO.update(userLastMessage)
            }
        }else if (jsonObject.optInt("is_group")==1){
            var groupLastMessage = MessengerGroup()
            groupLastMessage = dataBase!!.messengerGroupDAO.fetchingGroupData(entityId)
            if (groupLastMessage!=null && groupLastMessage.last_message_message_id==jsonObject.optLong("message_id")){
                groupLastMessage.last_message = decryptMessage
                groupLastMessage.last_message_type = jsonObject.optInt("message_type")
                dataBase!!.messengerGroupDAO.updateGroup(groupLastMessage)
            }
        }
        //}
    }
    private fun deleteThePlaceHolderMessage(jsonObject: JSONObject){
        //
    }
    private fun groupCreated(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        if (dataBase!!.messengerGroupDAO.checkGroupExists(jsonObject.optLong("group_id"))<=0){
            val groupMembers = jsonObject.optJSONArray("members")
            val groupMemberList = ArrayList<GroupMembers>()
            for (m in  0 until groupMembers!!.length()){
                val groupMember = GroupMembers()
                groupMember.group_id = jsonObject.optLong("group_id")
                groupMember.user_id = if (groupMembers.optJSONObject(m).has("user_id")) groupMembers.optJSONObject(m).optLong("user_id") else groupMembers.optJSONObject(m).optLong("member_id")
                groupMember.user_status = if (groupMembers.optJSONObject(m).has("user_status")) groupMembers.optJSONObject(m).optInt("user_status") else groupMembers.optJSONObject(m).optInt("member_status")
                groupMember.user_role = if (groupMembers.optJSONObject(m).has("user_role")) groupMembers.optJSONObject(m).optInt("user_role") else groupMembers.optJSONObject(m).optInt("member_role")
                //groupMember.member_color =  groupMembers.optJSONObject(m).optString("member_role")
                groupMember.created_at =  groupMembers.optJSONObject(m).optString("created_at")
                groupMember.updated_at =  updatedAtTime
                groupMemberList.add(groupMember)
            }
            dataBase!!.groupMembersDAO.deleteGroupMembers(jsonObject.optString("group_id"))
            dataBase!!.groupMembersDAO.insertAllData(groupMemberList)
            TroopClient.bulckAddGroupMembers(groupMembers)
            val messengerGroup = MessengerGroup()
            messengerGroup.group_id = jsonObject.optLong("group_id")
            messengerGroup.group_name = jsonObject.optString("group_name")
            messengerGroup.group_description = jsonObject.optString("group_description")
            messengerGroup.group_avatar = jsonObject.optString("group_avatar")
            messengerGroup.created_by = jsonObject.optLong("created_by")
            messengerGroup.created_at = jsonObject.optString("created_at")
            messengerGroup.updated_at = updatedAtTime
            messengerGroup.is_active = 1
            //messengerGroup.workspace_id = workspaceId.trim()
            //messengerGroup.is_orange_group = jsonObject.optInt("is_orange_group")
            //messengerGroup.is_muted = jsonObject.optInt("is_muted")
            //messengerGroup.is_favourite = jsonObject.optInt("is_favourite")
            messengerGroup.group_type = jsonObject.optInt("group_type")
            dataBase!!.messengerGroupDAO.insertGroup(messengerGroup)
        }
    }
    private fun groupUpdated(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        when (jsonObject.optString("payload")) {
            ConstantValues.GroupUpdatePayLoads.PAYLOAD_CHANGE_NAME -> {
                dataBase!!.messengerGroupDAO.updateGroupName(
                    jsonObject.optString("group_name"),
                    jsonObject.optString("group_id"),
                    updatedAtTime!!
                )
                /*groupObject.put(Values.WORKSPACEID_KEY, workspace_id)*/
            }
            ConstantValues.GroupUpdatePayLoads.PAYLOAD_CHANGE_AVATAR -> {
                dataBase!!.messengerGroupDAO.updateGroupAvatar(
                    jsonObject.optString("group_avatar"),
                    jsonObject.optString("group_id"),
                    updatedAtTime!!
                )
                //groupObject.put(Values.WORKSPACEID_KEY, workspace_id)
            }
            ConstantValues.GroupUpdatePayLoads.PAYLOAD_GROUP_EXIT -> {
                if (jsonObject.optString("user_id").equals(APPLICATION_TM_LOGIN_USER_ID)) {
                    var groupList:ArrayList<Long> = java.util.ArrayList<Long>()
                    groupList.add(jsonObject.optLong("group_id"))
                    dataBase!!.messengerDAO.deleteGroupMessages(
                        groupList as List<Long>
                    )
                    dataBase!!.messengerGroupDAO.deleteGroup(
                        jsonObject.optString("group_id")
                    )
                    dataBase!!.groupMembersDAO.updateGroupMemberStatusToMember(
                        jsonObject.optString(
                            "group_id"
                        ),
                        jsonObject.optString("user_id"),
                        updatedAtTime!!
                    )
                } else {
                    dataBase!!.groupMembersDAO.updateGroupMemberStatus(
                        jsonObject.optString(
                            "group_id"
                        ),
                        jsonObject.optString("user_id"),
                        updatedAtTime!!
                    )
                }
            }
            ConstantValues.GroupUpdatePayLoads.PAYLOAD_ADD_MEMBERS -> {
                if (dataBase!!.messengerGroupDAO.checkGroupExists(
                        jsonObject.optLong("group_id")
                    ) <= 0
                ) {
                    groupCreated(jsonObject)
                } else {

                    if (jsonObject.has("members")) {
                        var arrayLength = jsonObject.optJSONArray("members")
                            .length()
                        var groupMembersArray = jsonObject.getJSONArray("members")
                        var groupMemberList: ArrayList<GroupMembers> = ArrayList()
                        for (i in 0 until arrayLength) {
                            var userObject: JSONObject =
                                jsonObject.optJSONArray("members").optJSONObject(i)
                            var member: GroupMembers = GroupMembers()
                            //var members:GroupMembers = Gson().fromJson(userObect.toString(),GroupMembers::class.java)
                            member.group_id = jsonObject.optLong("group_id")
                            member.user_id = userObject.optLong("member_id")
                            member.user_status = userObject.optInt("member_status")
                            member.user_role = userObject.optInt("member_role")
                            //member.member_color = userObject.optString("member_color")
                            //member.workspace_id = workspace_id.trim()
                            member.created_at = userObject.optString("created_at")
                            groupMemberList.add(member)
                        }
                        if (groupMemberList != null && groupMemberList.size > 0) {
                            /*for (i in 0 until groupMemberList.size) {
                                groupMemberList[i].workspace_id =
                                    groupMemberList[i].workspace_id!!.trim()
                            }*/
                            dataBase!!.groupMembersDAO.deleteGroupMembers(
                                jsonObject.optString(
                                    "group_id"
                                )
                            )
                            dataBase!!.groupMembersDAO.insertAllData(groupMemberList)
                            TroopClient.bulckAddGroupMembers(groupMembersArray)
                        }
                    }
                }
            }
            ConstantValues.GroupUpdatePayLoads.PAYLOAD_REMOVE_MEMBERS -> {
                dataBase!!.groupMembersDAO.updateGroupMemberStatus(
                    jsonObject.optString(
                        "group_id"
                    ),
                    jsonObject.optString("group_members"),
                    updatedAtTime!!
                )
            }
            ConstantValues.GroupUpdatePayLoads.PAYLOAD_CHANGE_ROLE -> {
                dataBase!!.groupMembersDAO.changeUserRole(
                    jsonObject.optString("group_id"),
                    jsonObject.optString("member_id"),
                    jsonObject.optInt("new_role"),
                    updatedAtTime!!
                )
            }
            ConstantValues.GroupUpdatePayLoads.PAYLOAD_GROUP_DELETE -> {
                var groupList:ArrayList<Long> = java.util.ArrayList<Long>()
                groupList.add(jsonObject.optLong("group_id"))
                dataBase!!.messengerDAO.deleteGroupMessages(groupList as List<Long>)
                dataBase!!.groupMembersDAO.updateGroupMemberStatusToMember(
                    jsonObject.optString(
                        "group_id"
                    ),
                    jsonObject.optString("user_id"),
                    updatedAtTime!!
                )
                dataBase!!.messengerGroupDAO.deleteGroup(
                    jsonObject.optString("group_id")
                )
            }
        }
    }
    private fun insertOrUpdateNewUser(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        if (jsonObject.has("users")){
            var usersArray = jsonObject.optJSONArray("users")
            if (usersArray!=null && usersArray.length()>0){
                var userLength = usersArray.length()
                for (i in 0 until  userLength){
                    var userObject = usersArray.optJSONObject(i)
                    userObject.put("user_status",userObject.optInt("status"))
                    userObject.put("updated_at",updatedAtTime!!)
                    val user: User = Gson().fromJson(userObject.toString(),User::class.java)
                    if (dataBase!!.userDAO.checkIsUserActive(userObject.optString("user_id"))<=0){
                        dataBase!!.userDAO.insert(user)
                    }else{
                        dataBase!!.userDAO.update(user)
                    }
                }
            }
        }
    }
    private fun updateUserProfilePic(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        dataBase!!.userDAO.updateUserProfileAvatar(jsonObject.optString("user_id"),jsonObject.optString("user_avatar"),updatedAtTime!!)
        dataBase!!.notifyDAO.updateSenderAvatar(jsonObject.optString("user_avatar"),jsonObject.optString("user_id"))
    }
    private fun updateUserStatus(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        dataBase!!.userDAO.updateUserStatus(jsonObject.optString("user_id"),jsonObject.optInt("status"),updatedAtTime!!)
    }
    private fun updateTMPlan(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        jsonObject.put("updated_at",updatedAtTime)
        val  permission : Plan = Gson().fromJson(jsonObject.toString(),Plan::class.java)
        dataBase!!.planDAO.insert(permission)
    }

    private fun insertOrUpdateNotifyMessage(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        jsonObject.put("updated_at",updatedAtTime!!)
        jsonObject.put("receiver_id",jsonObject.optString("receiver_id"))
        val notify = Gson().fromJson(jsonObject.toString(),Notify::class.java)
        val existingNotifyMessage = dataBase!!.notifyDAO!!.fetchNotify(jsonObject.optLong("notify_id"))
        if (existingNotifyMessage!=null && existingNotifyMessage.notify_id>0){
            notify.ID = existingNotifyMessage.ID
            notify.sender_name = existingNotifyMessage.sender_name
            notify.sender_avatar = existingNotifyMessage.sender_avatar
            dataBase!!.notifyDAO.update(notify)
        }else{
            val user : User = dataBase!!.userDAO.fetchUser(jsonObject.optString("sender_id"))
            notify.sender_name = user.name
            notify.sender_avatar = user.profile_pic
            dataBase!!.notifyDAO.insert(notify)
        }
    }
    private fun recallNotifyMessage(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        val notifyMessagesArray = jsonObject.optJSONArray("notify_id")
        val userid = jsonObject.optString("user_id")
        val list: MutableList<Long> = ArrayList()
        var notifyMessageLength = notifyMessagesArray!!.length()
        for (i in 0 until notifyMessageLength) {
            list.add(notifyMessagesArray[i].toString().toLong())
        }
        val name = dataBase!!.userDAO.fetchUseName(userid)
        val messageObject = JSONObject()
        messageObject.put("user_id",userid)
        messageObject.put("name",name)
        dataBase!!.notifyDAO.updateMessageStatusToRecall(list,messageObject.toString(),updatedAtTime!!)
    }
    private fun updateNotifyMessageReadStatus(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        val notifyMessagesArray = jsonObject.optJSONArray("notify_id")
        val list: MutableList<Long> = ArrayList()
        var notifyMessageLength = notifyMessagesArray!!.length()
        for (i in 0 until notifyMessageLength) {
            list.add(notifyMessagesArray[i].toString().toLong())
        }
        dataBase!!.notifyDAO.updateNotifyReadStatus(list,updatedAtTime!!)
    }
    /*private fun updatePinMessage(jsonObject: JSONObject){
        val updatedAtTime = ConstantValues.fetchCurrentTimeInUTC()
        dataBase!!.messengerDAO.updatePinMessage(jsonObject.optInt("pin"),jsonObject.optString("user_id"),jsonObject.optLong("message_id"),updatedAtTime!!)
    }*/
    private fun updateUserBlockUnBlockAndMe(status:Int,jsonObject: JSONObject,type:Int){
        var jsonArray = jsonObject.optJSONArray("entity_id")
        if (jsonArray!=null && jsonArray.length()>0){
            val list = Helper.toLongArray(jsonArray)
            when(type){
                ConstantValues.BlockedStatus.USER_BLOCKED->{
                    dataBase!!.userDAO.updateUserBlockedStatus(status,list)
                }
                ConstantValues.BlockedStatus.USER_UNBLOCKED->{
                    dataBase!!.userDAO.updateUserBlockedStatus(status,list)
                }
                ConstantValues.BlockedStatus.USER_BLOCKED_ME->{
                    dataBase!!.userDAO.updateUserBlockedMeStatus(status,list)
                }
                ConstantValues.BlockedStatus.USER_UNBLOCKED_ME->{
                    dataBase!!.userDAO.updateUserBlockedMeStatus(status,list)
                }
            }

        }
    }
    private fun deActivatedUsers(jsonArray: JSONArray){
        val userIds = Helper.toStringArray(jsonArray)
        val listOfuIds = dataBase!!.userDAO.fetchuIds(userIds)
        dataBase!!.userDAO.deleteUsers(userIds)
        dataBase!!.storyDAO.deleteUserStories(userIds)
        dataBase!!.contactDAO.deleteContacts(listOfuIds)
        val groupIds = dataBase!!.messengerGroupDAO.fetchAdminGroupIds(userIds)
        dataBase!!.messengerGroupDAO.deleteAdminGroup(userIds)
        dataBase!!.groupMembersDAO.deleteUsersFromGroup(groupIds)
        dataBase!!.messengerDAO.deleteUserMessages(userIds)
        dataBase!!.messengerDAO.deleteGroupMessages(groupIds)
    }
    internal fun likeMessage(messageId:Long,listener: ClientCallBackListener){
        var callObject= JSONObject()
        if (messengerSocket!=null && !messengerSocket!!.connected()){
            callObject.put("success",false)
            callObject.put("message","Connection to server is not yet established!")
            listener.likeUnLikeMessageResponse(callObject)
            return
        }
        if (messageId<=0){
            callObject.put("success",false)
            callObject.put("message","Invalid Message")
            listener.likeUnLikeMessageResponse(callObject)
            return
        }
        var likeObject = JSONObject()
        likeObject.put("message_id",messageId)
        likeObject.put("access_token",APP_ACCESS_TOKEN)
        messengerSocket!!.emit(SocketEvents.LIKE_MESSAGE,likeObject,Ack{args ->
            dataBase!!.messengerDAO.updateLikeStatus(messageId, 1)
            if (args[0]!=null){
                listener.likeUnLikeMessageResponse(args[0] as JSONObject)
            }
        })
    }
    internal fun unLikeMessage(messageId:Long,listener: ClientCallBackListener){
        var callObject= JSONObject()
        if (messengerSocket!=null && !messengerSocket!!.connected()){
            callObject.put("success",false)
            callObject.put("message","Connection to server is not yet established!")
            listener.likeUnLikeMessageResponse(callObject)
            return
        }
        if (messageId<=0){
            callObject.put("success",false)
            callObject.put("message","Invalid Message")
            listener.likeUnLikeMessageResponse(callObject)
            return
        }
        var unLikeObject = JSONObject()
        unLikeObject.put("message_id",messageId)
        unLikeObject.put("access_token",APP_ACCESS_TOKEN)
        messengerSocket!!.emit(SocketEvents.UNLIKE_MESSAGE,unLikeObject,Ack{args ->
            dataBase!!.messengerDAO.updateLikeStatus(messageId, 0)
            if (args[0]!=null){
                listener.likeUnLikeMessageResponse(args[0] as JSONObject)
            }
        })
    }
    internal fun reportMessage(messageId:Long,message:String,listener: ClientCallBackListener){
        var callObject= JSONObject()
        if (messengerSocket!=null && !messengerSocket!!.connected()){
            callObject.put("success",false)
            callObject.put("message","Connection to server is not yet established!")
            listener.reportMessageResponse(callObject)
            return
        }
        if (messageId<=0){
            callObject.put("success",false)
            callObject.put("message","Invalid Message")
            listener.reportMessageResponse(callObject)
            return
        }
        var reportObject = JSONObject()
        reportObject.put("message_id",messageId)
        reportObject.put("access_token",APP_ACCESS_TOKEN)
        reportObject.put("report_message",message)
        reportObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
        messengerSocket!!.emit(SocketEvents.REPORT_MESSAGE,reportObject,Ack{args ->
            if (args[0]!=null){
                callObject.put("success",true)
                listener.reportMessageResponse(args[0] as JSONObject)
            }
        })
    }
    internal fun deleteChats(chats:List<Chats>,listener: ClientCallBackListener){
        var callObject= JSONObject()
        if (messengerSocket!=null && !messengerSocket!!.connected()){
            callObject.put("success",false)
            callObject.put("message","Connection to server is not yet established!")
            listener.deleteChatResponse(callObject)
            return
        }
        if (chats.isEmpty()){
            callObject.put("success",false)
            callObject.put("message","Chats are empty")
            listener.deleteChatResponse(callObject)
            return
        }
        var chatArray = JSONArray()
        for (i in 0 until chats.size){
            var jsonObject = JSONObject()
            jsonObject.put("entity_id",chats[i].entity_id)
            jsonObject.put("entity_type",chats[i].entity_type)
            chatArray.put(jsonObject)
        }

        var deleteObject = JSONObject()
        deleteObject.put("chats",chatArray)
        deleteObject.put("access_token",APP_ACCESS_TOKEN)
        deleteObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
        Log.e(TAG, "delete---> $deleteObject")
        messengerSocket!!.emit(SocketEvents.DELETE_CHATS,deleteObject,Ack{args ->
            Log.e(TAG, "delete---> callback ")
            if (args[0]!=null){
                callObject.put("success",true)
                listener.deleteChatResponse(args[0] as JSONObject)
                for (i in chats.indices) {
                    if (chats[i].entity_type==ConstantValues.Entity.USER) {
                        dataBase!!.messengerDAO.bulkDeleteUserChats(chats[i].entity_id,APPLICATION_TM_LOGIN_USER_ID)
                        dataBase!!.userDAO.updateUserLastMessageData(chats[i].entity_id)
                    }else{
                        dataBase!!.messengerDAO.bulkDeleteGroupChats(chats[i].entity_id)
                        dataBase!!.messengerGroupDAO.updateGroupLastMessageData(chats[i].entity_id)
                    }

                }
            }
        })
    }
    internal fun updateUserAbout(about:String):JSONObject{
        var callObject= JSONObject()
        if (messengerSocket!=null && !messengerSocket!!.connected()){
            callObject.put("success",false)
            callObject.put("message","Connection to server is not yet established!")
            return callObject
        }
        var aboutObject = JSONObject()
        aboutObject.put("user_id",APPLICATION_TM_LOGIN_USER_ID)
        aboutObject.put("about",about)
        aboutObject.put("access_token",APP_ACCESS_TOKEN)
        aboutObject.put("device_id", deviceId)
        aboutObject.put("version",ConstantValues.CLIENT_VERSION.toInt())
        Log.e("calledabout--> "," "+aboutObject.toString())
        messengerSocket!!.emit(SocketEvents.UPDATE_PROFILE_ABOUT,aboutObject, Ack {args->
            Log.e("calledabout--> "," callback "+args.size)
            if (args[0]==null) {
                Log.e("calledabout--> "," callback "+args[0].toString())
                callObject.put("success", true)
            }
            dataBase!!.userDAO.updateUserProfileAbout(about,ConstantValues.fetchCurrentTimeInUTC(),APPLICATION_TM_LOGIN_USER_ID)
        })
        callObject.put("success", true)
        return callObject
    }
    internal fun archivedChats(chats:List<Chats>):JSONObject{
        var callObject= JSONObject()

        if (chats.isEmpty()){
            callObject.put("success",false)
            callObject.put("message","Chats are empty")
            return callObject
        }
        var archivedChats = ArrayList<Chats>()
        for (i in chats.indices){
            if (chats[i].entity_id.isNotEmpty() && chats[i].entity_type>0){
                archivedChats.add(chats[i])
            }
        }
        if (archivedChats.isEmpty() && chats.size!=archivedChats.size){
            callObject.put("success",false)
            callObject.put("message","Invalid Data")
            return callObject
        }
        var archiveLength = archivedChats.size
        var userIds:ArrayList<Long> = ArrayList()
        var groupIds:ArrayList<Long> = ArrayList()
        for (i in 0 until archiveLength){
            if (archivedChats[i].entity_type==ConstantValues.Entity.USER) {
                userIds.add(archivedChats[i].entity_id.toLong())
            }else {
                groupIds.add(archivedChats[i].entity_id.toLong())
            }
        }
        if (userIds.size>0) {
            dataBase!!.userDAO.updateUserArchived(userIds)
        }
        if (groupIds.size>0){
            dataBase!!.messengerGroupDAO.updateGroupArchived(groupIds)
        }
        if (messengerSocket!=null && messengerSocket!!.connected()){
            var archiveObject = JSONObject()
            archiveObject.put("chats",Helper.stringToJsonArray(Gson().toJson(archivedChats)))
            archiveObject.put("access_token",APP_ACCESS_TOKEN)
            messengerSocket!!.emit(SocketEvents.ARCHIVED_CHAT,archiveObject,Ack{args ->
                if (args[0]!=null){
                    Log.e(TAG," userlist===> callback "+args[0].toString())
                    /*for (i in chats.indices) {
                        if (chats[i].entity_type==ConstantValues.Entity.USER) {
                            dataBase!!.messengerDAO.bulkDeleteUserChats(chats[i].entity_id,APPLICATION_TM_LOGIN_USER_ID)
                            dataBase!!.userDAO.updateUserLastMessageData(chats[i].entity_id)
                        }else{
                            dataBase!!.messengerDAO.bulkDeleteGroupChats(chats[i].entity_id)
                            dataBase!!.messengerGroupDAO.updateGroupLastMessageData(chats[i].entity_id)
                        }

                    }*/
                }
            })
        }else{
            sharedPreferences!!.edit().putString(SharePreferenceConstants.TM_OFFLINE_ARCHIVED_CHATS,Gson().toJson(archivedChats)).apply()
        }
        callObject.put("success",true)
        callObject.put("chats",Gson().toJson(archivedChats))
        return callObject
    }
    internal fun unArchivedChats(chats:List<Chats>):JSONObject{
        var callObject= JSONObject()

        if (chats.isEmpty()){
            callObject.put("success",false)
            callObject.put("message","Chats are empty")
            return callObject
        }
        var archivedChats = ArrayList<Chats>()
        for (i in chats.indices){
            if (chats[i].entity_id.isNotEmpty() && chats[i].entity_type>0){
                archivedChats.add(chats[i])
            }
        }
        if (archivedChats.isEmpty() && chats.size!=archivedChats.size){
            callObject.put("success",false)
            callObject.put("message","Invalid Data")
            return callObject
        }
        var archiveLength = archivedChats.size
        var userIds:ArrayList<Long> = ArrayList()
        var groupIds:ArrayList<Long> = ArrayList()
        for (i in 0 until archiveLength){
            if (archivedChats[i].entity_type==ConstantValues.Entity.USER) {
                userIds.add(archivedChats[i].entity_id.toLong())
            }else {
                groupIds.add(archivedChats[i].entity_id.toLong())
            }
        }
        if (userIds.size>0) {
            dataBase!!.userDAO.updateUserUnArchived(userIds)
        }
        if (groupIds.size>0){
            dataBase!!.messengerGroupDAO.updateGroupUnArchived(groupIds)
        }
        if (messengerSocket!=null && messengerSocket!!.connected()){
            var archiveObject = JSONObject()
            archiveObject.put("chats",Helper.stringToJsonArray(Gson().toJson(archivedChats)))
            archiveObject.put("access_token",APP_ACCESS_TOKEN)
            messengerSocket!!.emit(SocketEvents.UNARCHIVED_CHAT,archiveObject,Ack{args ->
                if (args[0]!=null){
                    Log.e(TAG," userlist===> callback 1 "+args[0].toString())
                    /*for (i in chats.indices) {
                        if (chats[i].entity_type==ConstantValues.Entity.USER) {
                            dataBase!!.messengerDAO.bulkDeleteUserChats(chats[i].entity_id,APPLICATION_TM_LOGIN_USER_ID)
                            dataBase!!.userDAO.updateUserLastMessageData(chats[i].entity_id)
                        }else{
                            dataBase!!.messengerDAO.bulkDeleteGroupChats(chats[i].entity_id)
                            dataBase!!.messengerGroupDAO.updateGroupLastMessageData(chats[i].entity_id)
                        }
                    }*/
                }
            })
        }else{
            sharedPreferences!!.edit().putString(SharePreferenceConstants.UNARCHIVED_CHATS,Gson().toJson(archivedChats)).apply()
        }
        callObject.put("success",true)
        callObject.put("chats",Gson().toJson(archivedChats))
        return callObject
    }

    fun addTextStory(text: String, bgCode: String, fontName: String):JSONObject {
        var callBackObject = JSONObject()
        if (text.isNullOrEmpty()){
            callBackObject.put("success",false)
            callBackObject.put("message","Text is required")
            return callBackObject
        }
        if (bgCode.isNullOrEmpty()){
            callBackObject.put("success",false)
            callBackObject.put("message","bgCode is required")
            return callBackObject
        }
        if (fontName.isNullOrEmpty()) {
            callBackObject.put("success",false)
            callBackObject.put("message","fontname is required")
            return callBackObject
        }
        var jsonObject = JSONObject()
        var dataObject = JSONObject()
        dataObject.put("text",text)
        dataObject.put("bgCode",bgCode)
        dataObject.put("fontName",fontName)
        jsonObject.put("access_token", APP_ACCESS_TOKEN)
        jsonObject.put("type",ConstantValues.StoryType.TEXT)
        jsonObject.put("data",dataObject)
        jsonObject.put("device_id", ConstantValues.deviceId)
        messengerSocket!!.emit(SocketEvents.ADD_STORY,jsonObject, Ack { args ->
            Log.e("addstory==> ","callback ")
            if (args[0]!=null){
                var responce = args[0] as JSONObject
                if (responce.optBoolean("success")){

                    var data = responce.optJSONObject("data")
                    data.put("user_id", APPLICATION_TM_LOGIN_USER_ID)
                    data.put("status",1)
                    Log.e("addstory==> ","callback text "+data.toString())
                    var story = Gson().fromJson(data.toString(),Story::class.java)
                    dataBase!!.storyDAO.insertStory(story)
                }
            }
        })
        return callBackObject
    }
    fun addImageStory(url: String):JSONObject {
        var callBackObject = JSONObject()
        if (url.isNullOrEmpty()){
            callBackObject.put("success",false)
            callBackObject.put("message","url is required")
            return callBackObject
        }
        var jsonObject = JSONObject()
        var dataObject = JSONObject()
        dataObject.put("url",url)
        jsonObject.put("access_token", APP_ACCESS_TOKEN)
        jsonObject.put("type",ConstantValues.StoryType.IMAGE)
        jsonObject.put("data",dataObject)
        jsonObject.put("device_id", ConstantValues.deviceId)
        messengerSocket!!.emit(SocketEvents.ADD_STORY,jsonObject, Ack { args ->

            if (args[0]!=null){
                var responce = args[0] as JSONObject

                if (responce.optBoolean("success")){
                    var data = responce.optJSONObject("data")
                    data.put("user_id", APPLICATION_TM_LOGIN_USER_ID)
                    data.put("status",1)
                    Log.e("addstory==> ","callback image "+data.toString())
                    var story = Gson().fromJson(data.toString(),Story::class.java)
                    dataBase!!.storyDAO.insertStory(story)
                }
            }
        })
        return callBackObject
    }
    fun addVideoStory(url: String):JSONObject {
        var callBackObject = JSONObject()
        if (url.isNullOrEmpty()){
            callBackObject.put("success",false)
            callBackObject.put("message","url is required")
            return callBackObject
        }
        var jsonObject = JSONObject()
        var dataObject = JSONObject()
        dataObject.put("url",url)
        jsonObject.put("access_token", APP_ACCESS_TOKEN)
        jsonObject.put("type",ConstantValues.StoryType.VIDEO)
        jsonObject.put("data",dataObject)
        jsonObject.put("device_id", ConstantValues.deviceId)
        messengerSocket!!.emit(SocketEvents.ADD_STORY,jsonObject, Ack { args ->
            Log.e("addstory==> ","callback video ")
            if (args[0]!=null){
                var responce = args[0] as JSONObject
                if (responce.optBoolean("success")){
                    var data = responce.optJSONObject("data")
                    data.put("status",1)
                    data.put("user_id", APPLICATION_TM_LOGIN_USER_ID)
                    Log.e("addstory==> ","callback video "+data.toString())
                    var story = Gson().fromJson(data.toString(),Story::class.java)
                    dataBase!!.storyDAO.insertStory(story)
                }
            }
        })
        return callBackObject
    }
    fun storyViewed(id:Long){
        var storyData = dataBase!!.storyDAO.fetchStoryById(id)
        if (storyData.user_id.toString()!= APPLICATION_TM_LOGIN_USER_ID && storyData.status==ConstantValues.StoryStatus.UNSEEN){
            if (CLIENT_IS_CONNECTED){
                var jsonObject = JSONObject()
                jsonObject.put("access_token", APP_ACCESS_TOKEN)
                jsonObject.put("id",id)
                jsonObject.put("user_id",storyData.user_id)
                jsonObject.put("seen_at",ConstantValues.fetchCurrentTimeInUTC())
                jsonObject.put("name", APP_LOGIN_USER_NAME)
                messengerSocket!!.emit(SocketEvents.VIEW_STORY,jsonObject, Ack { args ->
                    Log.e("story view==> "," callback "+args.size)
                    if (args[0]!=null){
                        var response = args[0] as JSONObject
                        if (response.optBoolean("success")){
                            dataBase!!.storyDAO.updateStoryStatus(ConstantValues.StoryStatus.VIEWED,id,ConstantValues.fetchCurrentTimeInUTC())
                        }
                    }
                })
            }else{
                var time = ConstantValues.fetchCurrentTimeInUTC()
                var offlineStoryViews = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.OFFLINE_VIEWED_STORIES,"[]"))!!
                var jsonObject = JSONObject()
                jsonObject.put("id",id)
                jsonObject.put("seen_at",time)
                jsonObject.put("user_id",storyData.user_id)
                offlineStoryViews.put(jsonObject)
                sharedPreferences!!.edit().putString(SharePreferenceConstants.OFFLINE_VIEWED_STORIES,offlineStoryViews.toString()).apply()
            }
        }
    }
    fun deleteStory(id: List<Long>):JSONObject{
        var callBackObject = JSONObject()
        if (id.isEmpty()){
            callBackObject.put("success",false)
            callBackObject.put("message","id cannot be empty")
            return callBackObject
        }
        var actualId = dataBase!!.storyDAO.verifyMyStories(APPLICATION_TM_LOGIN_USER_ID,id)
        if (actualId.isEmpty() || id.size!=actualId.size){
            callBackObject.put("success",false)
            callBackObject.put("message",ConstantValues.SOMETHING_WENT_WRON_ERROR_MESSAGE)
            return callBackObject
        }
        if (CLIENT_IS_CONNECTED){
            var jsonObject = JSONObject()
            jsonObject.put("access_token", APP_ACCESS_TOKEN)
            jsonObject.put("id",Helper.stringToJsonArray(Gson().toJson(actualId)))
            messengerSocket!!.emit(SocketEvents.DELETE_STORY,jsonObject, Ack { args ->
                if (args[0]!=null){
                    var response = args[0] as JSONObject
                    if (response.optBoolean("success")){
                        Log.e("story delete==> "," callback "+response.toString())
                        var time = ConstantValues.fetchCurrentTimeInUTC()
                        var lenght = actualId.size
                        for (i in 0 until lenght){
                            dataBase!!.storyDAO.updateStoryStatus(ConstantValues.StoryStatus.DELETED,actualId[i],time,)
                        }

                    }
                }
            })
        }else{
            var time = ConstantValues.fetchCurrentTimeInUTC()
            var lenght = actualId.size
            var offlineDeletedStories = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.TM_OFFLINE_DELETED_STORIES,"[]"))!!
            for (i in 0 until lenght){
                dataBase!!.storyDAO.updateStoryStatus(ConstantValues.StoryStatus.DELETED,actualId[i],time,)
                offlineDeletedStories.put(actualId[i].toString())
            }
            sharedPreferences!!.edit().putString(SharePreferenceConstants.TM_OFFLINE_DELETED_STORIES,offlineDeletedStories.toString()).apply()

        }
        callBackObject.put("success",true)
        callBackObject.put("id",actualId)
        return callBackObject
    }
    fun reportUser(entityId:Long,entityType:Int,reason:String,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (entityId<=0){
            callBackObject.put("success",false)
            callBackObject.put("message","Invalid Action")
            listener.reportUserResponse(callBackObject)
            return
        }
        if (entityId.toString()== APPLICATION_TM_LOGIN_USER_ID && entityType==ConstantValues.Entity.USER){
            callBackObject.put("success",false)
            callBackObject.put("message","Cannot report yourself")
            listener.reportUserResponse(callBackObject)
            return
        }
        Log.e("report==> "," connection "+CLIENT_IS_CONNECTED)
        if (CLIENT_IS_CONNECTED){
            var jsonObject = JSONObject()
            jsonObject.put("access_token", APP_ACCESS_TOKEN)
            jsonObject.put("entity_id",entityId.toString())
            jsonObject.put("entity_type",entityType)
            jsonObject.put("reason",reason)
            jsonObject.put("version",ConstantValues.CLIENT_VERSION)
            var list : ArrayList<Long> = ArrayList()
            Log.e("report==> "," sending "+jsonObject.toString())
            messengerSocket!!.emit(SocketEvents.REPORT_USER,jsonObject, Ack { args ->
                Log.e("report==> "," callback "+args[0].toString())
                if (entityType==ConstantValues.Entity.USER){
                    list.add(entityId)
                    dataBase!!.userDAO.updateUserBlockedStatus(1,list)
                    var presenceObject = JSONObject()
                    presenceObject.put("success",true)
                    presenceObject.put("status","offline")
                    presenceObject.put("last_seen","")
                    listener.userPresenceResponse(presenceObject)
                }else{
                    var groupAuthor = dataBase!!.messengerGroupDAO.fetchGroupAuthor(entityId.toString())
                    if (groupAuthor.user_id.toString()!="0"){
                        list.add(groupAuthor.user_id)
                        dataBase!!.userDAO.updateUserBlockedStatus(1,list)
                        var presenceObject = JSONObject()
                        presenceObject.put("success",true)
                        presenceObject.put("status","offline")
                        presenceObject.put("last_seen","")
                        listener.userPresenceResponse(presenceObject)
                    }
                }
                callBackObject.put("success",true)
                callBackObject.put("entity_id",entityId)
                callBackObject.put("entity_type",entityType)
                listener.reportUserResponse(callBackObject)
            })
        }else{
            callBackObject.put("success",false)
            callBackObject.put("message","Network Unavailable")
            listener.reportUserResponse(callBackObject)
        }
    }
    fun unblockUser(entityId: Long,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (entityId<=0){
            callBackObject.put("success",false)
            callBackObject.put("message","Invalid User")
            listener.blockUserResponse(callBackObject)
            return
        }
        var list:ArrayList<Long> = ArrayList()
        list.add(entityId)
        var jsonArray = JSONArray()
        jsonArray.put(entityId.toString())
        dataBase!!.userDAO.updateUserBlockedStatus(0,list)
        if (CLIENT_IS_CONNECTED){
            var jsonObject = JSONObject()
            jsonObject.put("access_token", APP_ACCESS_TOKEN)
            jsonObject.put("entity_id",jsonArray)
            jsonObject.put("version",ConstantValues.CLIENT_VERSION)
            Log.e("unblock==> "," sending "+jsonObject.toString())
            messengerSocket!!.emit(SocketEvents.UNBLOCK_USER,jsonObject, Ack { args ->
                Log.e("unblock==> "," callback "+args.size)
                /*if (args[0]!=null){
                    Log.e("unblock==> "," callback "+args[0].toString())
                }*/
            })
        }else{
            var offlineUnBlockedUsers = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.TM_OFFLINE_UNBLOCKED_USERS,"[]"))!!
            offlineUnBlockedUsers.put(entityId)
            sharedPreferences!!.edit().putString(SharePreferenceConstants.TM_OFFLINE_UNBLOCKED_USERS,offlineUnBlockedUsers.toString()).apply()
        }
        callBackObject.put("success",true)
        callBackObject.put("entity_id",entityId)
        listener.blockUserResponse(callBackObject)
    }
    fun blockUser(entityId: Long,listener: ClientCallBackListener){
        var callBackObject = JSONObject()
        if (entityId<=0){
            callBackObject.put("success",false)
            callBackObject.put("message","Invalid User")
            listener.blockUserResponse(callBackObject)
            return
        }
        var list:ArrayList<Long> = ArrayList()
        list.add(entityId)
        var jsonArray = JSONArray()
        jsonArray.put(entityId.toString())
        dataBase!!.userDAO.updateUserBlockedStatus(1,list)
        var presenceObject = JSONObject()
        presenceObject.put("success",true)
        presenceObject.put("status","offline")
        presenceObject.put("last_seen","")
        listener.userPresenceResponse(presenceObject)
        if (CLIENT_IS_CONNECTED){
            var jsonObject = JSONObject()
            jsonObject.put("access_token", APP_ACCESS_TOKEN)
            jsonObject.put("entity_id",jsonArray)
            jsonObject.put("version",ConstantValues.CLIENT_VERSION)
            Log.e("block==> "," sending "+jsonObject.toString())
            messengerSocket!!.emit(SocketEvents.BLOCK_USER,jsonObject, Ack { args ->
                Log.e("block==> "," callback "+args.size)
            })
        }else{
            var offlineUnBlockedUsers = Helper.stringToJsonArray(sharedPreferences!!.getString(SharePreferenceConstants.TM_OFFLINE_UNBLOCKED_USERS,"[]"))!!
            offlineUnBlockedUsers.put(entityId)
            sharedPreferences!!.edit().putString(SharePreferenceConstants.TM_OFFLINE_UNBLOCKED_USERS,offlineUnBlockedUsers.toString()).apply()
        }
        callBackObject.put("success",true)
        callBackObject.put("entity_id",entityId)
        listener.blockUserResponse(callBackObject)
    }
    private fun fetchUserStatuses(){
        var jsonObject = JSONObject()
        jsonObject.put("access_token", APP_ACCESS_TOKEN)
        messengerSocket!!.emit(SocketEvents.GET_USER_STATUS,jsonObject)
    }
    fun getUid(userId:String) :String{
        if (activeCallData!=null && activeCallData.has("participants")){
            if (activeCallData.optJSONObject("participants")!!.has(userId)){
                return activeCallData.optJSONObject("participants")!!.optJSONObject(userId).optString("uid")
            }
        }
        return  ""
    }

}