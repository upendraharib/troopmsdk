package com.tvisha.trooponprime.lib.utils

object SocketEvents {
    /*const val ACCOUNT_VERIFIED = "account_verified"
    const val SIGNUP_EVENT = "signup_event"
    const val REGISTER_EVENT = "register_event"
    const val LOGOUT_FROM_OTHER_DEVICES = "logout_from_other_devices"

    //logout

    const val LOGOUT_ALL_DEVICES = "logout_from_all_devices"

    //user status events
    const val NEW_ONLINE           = "new_online"
    const val NEW_OFFLINE          = "new_offline"
    const val NEW_DND              = "new_dnd"
    const val NEW_USER_AVAILABLE_STATUS = "get_new_users_available_status"
    const val STATUS_OPTION_ONE = "status_option1"
    const val STATUS_OPTION_TWO = "status_option2"
    const val STATUS_OPTION_THREE = "status_option3"
    const val GET_USER_LAS_SEEN = "get_user_last_seen"
    const val USER_STATUS_UPDATE = "user_status_update"


    //messages event



    const val SEND_MESSAGE_ERROR = "message_sending_error"


    const val SHARE_CONTACT = "share_contact" // emit with jsonObject data
    const val SHARE_LOCATION = "share_location" //emit with jsonObject data
    const val FORWARD_MESSAGE = "forward_message"
    const val FORWARD_MESSAGE_NEW = "forward_message_new" //emit with jsonObject data
    const val REPLY_MESSAGE_OLD = "reply-message"

    const val SYNC_MESSAGE_BY_TIME = "sync_messages_by_time" //on & emit with callback jsonObject
    const val SYNC_MESSAGE_BY_TIME_NEW = "sync_messages_by_time_new"
    const val RECALL_MESSAGE = "recall_message" // on & emit with callback jsonObject
    const val DELETE_MESSAGE = "delete_message" // on & emit with callback jsonObject
    const val DELETE_MESSAGES_OFFLINE = "delete_message_offline" // emit with callback jsonObject
    const val GET_MISSING_MESSAGES = "get_missing_messages" // on event

    const val ERROR = "error" // on event
    const val RESPOND_LATER_MESSAGE = "respond_later"
    const val FLAG_MESSAGE = "flag_message"
    const val READ_RECEIPT = "read_receipt"

    //group events


    const val GROUP_META_INFO = "get_group_meta_info"

    //app version event
    const val VERSION_MANAGEMENT = "version_management"
    const val FORCE_UPDATE = "force_update"

    //user
    const val MUTE_CONVERSATION = "mute_conversation" // on & emit with callback jsonObject
    const val MARK_FAVOURITE = "mark_favourite" // on & emit with callback jsonObject

    const val USER_UPDATE = "user_updated"
    const val USER_STATUS_UPDATED = "user_status_updated"
    const val GET_ACTIVE_DEVICES = "get_active_devices"
    const val USER_PIC_UPDATE = "user_pic_updated"
    const val NEW_USER_CREATED = "user_created"

    //private chat or burnout
    const val GET_ALL_BURN_LIST = "get_all_burnouts"
    const val INIT_PRIVATE_CHAT = "init_private_chat"
    const val PRIVATE_CHAT_PERMISSION = "private_chat_permission"
    const val END_PRIVATE_CHAT = "end_private_chat"
    const val CHECK_PRIVATE_CHAT = "check_private_chat"

    //liveTracking
    const val INIT_LOCATION_TRACKING = "init_location_tracking"
    const val LOCATION_TRACKING_PERMISSION = "location_tracking_permission"
    const val STOP_LOCATION_TRACKING = "stop_location_tracking"
    const val LOCATION_TRACKING_SIGNAL = "location_tracking_signal"
    const val CHECK_LOCATION_TRACKING = "check_location_tracking"
    const val LAST_KNOW_LIVE_LOCATION = "last_known_live_location"

    //workspace
    const val CONNECT_WORKSPACE = "connect_workspace"
    const val REMOVE_WORKSPACE = "remove_workspace"

    // orange member
    const val IS_ORANGE_MEMBER_ADDED = "orange_member_added"
    const val IS_ORANGE_MEMBER_REMOVED = "orange_member_removed"

    //permission
    const val PERMISSION_UPDATED = "permission_updated"
    const val GLOBAL_PERMISSION_UPDATED = "global_permission_updated"

    //PLAN EVENTS
    const val PLAN_UPDATE = "plan_updated"
    const val PLAN_EXPIRED = "plan_expired"
    const val PLAN_ERROR = "plan_error"

    //cattlecall events
    const val CC_MEETING_STARTED = "cattle_call_meeting_started" // on cc meeting started
    const val MEETING_CALL_DATA = "meeting_call_data"
    const val CC_REMAINDER = "cattle_call_reminder" //on event
    const val START_SCHEDULE_CALL = "start_schedule_call"

    //calling events



    const val CALL_REQUEST = "call_request"



    const val CALL_SIGNAL = "call_signal"

    const val UPDATE_CALL_SOCKET = "update_call_socket"
    const val JOIN_CALL = "join_call"
    const val REQUEST_STREAM = "request_stream"
    const val STREAM_PERMISSION = "stream_permission"
    const val CALL_REQUEST_STATUS = "call_request_screen_status"
    const val END_CALL_STREAM = "end_call_stream"
    const val IS_CALL_ACTIVE = "is_call_active"



    const val GET_CALL_PARTICIPANTS = "get_call_participants"


    const val CAN_JOIN_CALL = "can_join_call"











    //server events
    const val SERVER_TIME = "server_time"
    const val UNAUTHORIZED_ME = "unauthorize_me"
    const val IS_AUTHORIZED = "is_authorized"
    const val UN_AUTHORIZED = "unauthorized"
    const val GLOBAL_CONSTANTS_UPDATED = "global_constants_updated"

    //placeholder event
    const val ATTACHMENT_PLACEHOLDER_DELETED = "attachment_placeholder_deleted"
    const val DELETE_ATTACHMENT_PLACEHOLDER = "delete_attachment_placeholder"
    const val SERVER_CHECK = "server_check"

    //media soup




    //archive
    const val ARCHIVED_MESSAGES = "archived_messages"
    const val ARCHIVED_MESSAGE_BY_TIME = "sync_archives_by_time"
    const val DELETE_MESSAGE_HISTORY = "delete_message_history"
    const val LAST_MESSAGE_DELETE_HISTORY = "last_message_delete_history"
    const val AUTO_DELETE_HISTORY= "auto_delete_history"//autodelete only on event

    const val ACCESS_PERMISSION_STATUS_UPDATED = "access_permission_status_updated"
    const val UPDATE_BACKGROUND_PROCESS = "update_background_process"//emit only
    const val USER_PLATFORM_UPDATED = "user_platform_updated"//on only
    const val GET_USER_PLATFORMS = "get_user_platforms"//emit only
    const val NOTIFY_RECALL = "notify_recall"//(emit with callback)
    const val NOTIFY_READ = "notify_read"
    const val NOTIFY = "notify"//emit with callback and On
    const val MY_ACTIVE_CALLS = "my_active_calls"
    const val CALL_TAKE_OVER = "take_over"
    const val ACTIVE_CALL = "active_call"
    const val ON_CALL = "on_call"
    const val GET_ON_CALL_ACTIVE_USERS = "get_on_call_active_users"
    const val PIN_MESSAGE = "pin_message"


    // grit extra
    const val GET_REFERENCE_ID = "get_reference_id"
    const val GROUP_KEY = "group_key"
    const val USER_KEY = "user_key"
    const val MFA_PIN_SET = "mfa_pin_set"
    const val NEW_USER_REQUEST_AUTHORIZATION = "new_user_request_authorization"
    const val UPDATE_UNIT_USER_AUTHORIZATION = "update_unit_user_authorization"
    const val USER_REMOVED_FROM_UNIT  = "user_removed_from_unit"
    const val USER_ADD_TO_UNIT  = "user_added_to_unit"
    const val UNIT_REQUEST_COUNT  = "unit_request_count"
    const val CALL_PARTICIPANT_RINGING_STATUS = "call_participant_ringing_status" //emit/on/callback
    const val COMPANY_NAME_UPDATE = "company_name_updated" //on event only
    const val APPOINTMENT_MODE = "appointment_model"
    const val TOPIC_CRATED = "topic_created"
    const val TOPIC_UPDATED = "topic_updated"
    const val DELETE_TOPIC = "delete_topic"
    const val CREATE_TOPIC = "create_topic"
    const val UPDATE_TOPIC = "update_topic"
    const val SWAPPED_APPOINTMENTS = "swapped_appointments"
    const val UPDATE_LANG_HANDSHAKE = "update_lang_handshake"
    const val NOTIFY_SENT = "notify_sent"
    const val SAVE_COORDINATES = "save_coordinates"*/

    //Actual events
    const val ACCESS_TOKEN = "access_token"
    const val USER_PRESENCE = "user_presence" // emit with callback and on event
    const val USER_MESSAGE_SENT = "message_sent"
    const val USER_RECEIVE_MESSAGE = "receive_message"
    const val MESSAGE_DELIVERED = "message_delivered" // on and emit with jsonObject event
    const val MESSAGE_READ = "message_read" // on and emit with jsonObject data
    const val MESSAGE_READ_BY_ME = "message_read_by_me" // on and emit with jsonObject data
    const val USER_GROUP_CREATED = "group_created"
    const val USER_GROUP_UPDATED = "group_updated"
    const val MESSAGE_DELETED = "message_deleted"
    const val MESSAGE_RECALLED = "message_recalled"
    const val NEW_STORY = "new_story"
    const val STORY_VIEWED = "story_viewed"
    const val STORY_DELETED = "story_deleted"
    const val CHAT_ARCHIVED = "chat_archived"
    const val CHAT_UNARCHIVED = "chat_unarchived"
    const val USER_BLOCKED = "user_blocked"
    const val BLOCKED_ME = "blocked_me"
    const val USER_UNBLOCKED = "user_unblocked"
    const val UNBLOCKED_ME = "unblocked_me"
    const val CHATS_DELETED = "chats_deleted"
    const val MESSAGE_BLOCKED = "message_blocked"
    const val LOGOUT= "logout"
    const val USER_DEACTIVATED = "user_deactivated"
    const val RECEIVE_BROADCAST_MESSAGE = "receive_broadcast_message"
    const val USER_PROFILE_ABOUT_UPDATED = "user_profile_about_updated"
    const val USER_REGISTERED = "user_registered"
    const val PROFILE_PIC_UPDATED = "profile_pic_updated"

    const val FORK_OUT = "fork_out" // emit with jsonArray messages
    const val SEND_MESSAGE = "send_message" // emit with jsonObject data
    const val SEND_ATTACHMENT = "send_attachment" // emit with jsonObject data
    const val SEND_AUDIO_MESSAGE = "send_audio_message" // emit with jsonObject data
    const val SEND_CONTACT_V2 = "send_contact_v2" // emit with jsonObject data
    const val SEND_LOCATION_V2 = "send_location_v2" // emit with jsonObject data
    const val SEND_ACTIVITY_MESSAGE = "send_activity_message" // emit with jsonObject data
    const val REPLY_MESSAGE = "reply_message" //emit with jsonObject data
    const val USER_TYPING = "typing"
    const val UPDATE_STATUS = "update_status"
    const val GET_USER_STATUS = "get_user_status"
    const val SYNC_OFFLINE_MESSAGES = "sync_offline_messages" //emit with jsonArray of messages data

    const val LIKE_MESSAGE = "like_message" //emit with callback
    const val UNLIKE_MESSAGE = "unlike_message" //emit with callback
    const val REPORT_MESSAGE = "report_message" //emit with callback
    const val DELETE_CHATS = "delete_chats" //emit with callback
    const val UPDATE_PROFILE_ABOUT = "update_profile_about" //emit with callback
    const val UPDATE_PROFILE_ABOUT_UPDATED = "user_profile_about_updated" //emit with callback
    const val ARCHIVED_CHAT = "archive_chat" //emit with callback
    const val UNARCHIVED_CHAT = "unarchive_chat" //emit with callback
    const val UNBLOCK_USER = "unblock_user" //emit with callback
    const val BLOCK_USER = "block_user" //emit with callback
    const val DELETE_STORY = "delete_story" //emit with callback
    const val VIEW_STORY = "view_story" //emit with callback
    const val DELETE_MESSAGE = "delete_message" //emit with callback
    const val ADD_STORY = "add_story" //emit with callback
    const val REPORT_USER = "report_user" //emit with callback
    const val TM_ERROR = "tm_error" //emit with callback
    const val RECALL_MESSAGE = "recall_message" //emit with callback
    const val MESSAGE_EDIT = "edit_message" //emit with callback jsonObject
    const val MESSAGE_EDITED = "message_edited" // on & emit with callback jsonObject


    //calls
    const val INITIATE_CALL = "initiate_call"
    const val CALL_REQUEST = "call_request"
    const val CALL_PERMISSION = "call_permission"
    const val CALL_PARTICIPANT_STATUS_UPDATE = "call_participant_status_updated"
    const val END_CALL = "end_call"
    const val CALL_NEW_PARTICIPANTS = "call_new_participants"
    const val CALL_STREAM_REQUEST = "call_stream_request"
    const val MUTE_CALL_AUDIO = "mute_call_audio"
    const val MUTE_CALL_VIDEO = "mute_call_video"
    const val CALL_USER_VIDEO_STATUS_UPDATED = "call_user_video_status_updated"
    const val CALL_UPDATE_HOST_UPDATED = "call_update_host"
    const val CALL_REMOVE_USER = "call_remove_user"
    const val CALL_MUTE_REQUEST = "call_mute_request"
    const val END_SCREEN_SHARE = "end_screen_share"
    const val LEAVE_CALL = "leave_call"
    const val STOP_JOIN_CALL_REQUEST: String = "stop_join_call_request"
    const val JOIN_CALL_PERMISSION = "join_call_permission"
    const val REQUEST_JOIN_CALL = "request_join_call"
    const val HOST_MUTE_AUDIO = "host_mute_audio"
    const val HOST_MUTE_VIDEO = "host_mute_video"
    const val END_JOINTLY_CODE = "end_jointly_code"
    const val CONSUMERCLOSED = "consumerClosed" //on only
    const val NEWPRODUCERS = "newProducers" //on only
    const val CREATEWEBRTCTRANSPORT = "createWebRtcTransport" //emit only callback
    const val CONNECTTRANSPORT = "connectTransport" //emit only callback
    const val CONSUME = "consume" //emit only
    const val PRODUCE = "produce" //emit only
    const val GETPRODUCERS = "getProducers" //emit only
    const val JOIN_ROOM = "join_room" //emit only
    const val PRODUCERCLOSED = "producerClosed" //emit only
    const val EXITROOM = "exitRoom" //emit only
    const val GETROUTERCAPABILITIES = "getRouterRtpCapabilities" //emit only
    const val ERROR = "error" //emit only
    const val ADD_CALL_USER = "add_call_users"
    const val GET_CALL_DATA = "get_call_data"
    const val CALL_USER_RETRY = "call_user_retry"
}
