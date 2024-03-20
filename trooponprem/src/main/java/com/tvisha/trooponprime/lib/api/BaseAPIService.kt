package com.tvisha.trooponprime.lib.api

import kotlinx.coroutines.Deferred
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface BaseAPIService {
    @FormUrlEncoded()
    @POST("api/messenger/v2/register")
    fun registerTm(
        @Field("v") v: String,
        @Field("token") token :String,
        @Field("uid") uid:String,
        @Field("name") name:String,
        @Field("mobile_number") mobileNumber : String,
        @Field("country_code") countryCode:String
    ):Deferred<Response<String>>
    @FormUrlEncoded
    @POST("api/messenger/v2/sync-contacts")
    fun syncContacts(
        @Field("v") v: String,
        @Field("user_id") userId: String,
        @Field("at") authorizationToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,
        @Field("contacts")contacts:String,
        @Field("device_id")deviceId: String
    ):Deferred<Response<String>>
    @FormUrlEncoded
    @POST("api/messenger/get-messenger-contacts")
    fun fetchMessengerContacts(
        @Field("v") v: String,
        @Field("user_id") userId: String,
        @Field("at") authorizationToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,
        @Field("device_id")deviceId:String
    ):Deferred<Response<String>>
    @FormUrlEncoded
    @POST("api/messenger/get-chat-list")
    fun fetchChatList(
        @Field("v") v: String,
        @Field("socket_id") socketId:String,
        @Field("user_id") userId:String,
        @Field("access_token") accessToken:String,
        @Field("token") token:String,
        @Field("conversation_reference_id") conversationReferenceId:String
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/create-group")
    fun createGroup(
        @Field("v") v: String,
        @Field("socket_id") socketId: String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("group_name") groupName:String,
        @Field("group_description") groupDescription: String,
        @Field("group_avatar") groupAvatar:String,
        @Field("group_members") groupMembers: String,
        @Field("platform") platForm:Int,
        @Field("conversation_reference_id") conversationReferenceId: String
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/update-group")
    fun updateGroupName(
        @Field("v") v: String,
        @Field("socket_id") socketId: String,
        @Field("payload") payLoad:String,
        @Field("group_id") groupId:String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("group_name") groupName: String,
        @Field("platform") platForm: Int
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/update-group")
    fun updateGroupAvatar(
        @Field("v") v: String,
        @Field("socket_id") socketId: String,
        @Field("payload") payLoad:String,
        @Field("group_id") groupId:String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("group_avatar") groupAvatar: String,
        @Field("platform") platForm: Int
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/update-group")
    fun updateGroupUserRole(
        @Field("v") v: String,
        @Field("socket_id") socketId: String,
        @Field("payload") payLoad:String,
        @Field("group_id") groupId:String,
        @Field("member_id") memberId:String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("user_role") userRole: String,
        @Field("platform") platForm: Int
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/update-group")
    fun removeGroupUser(
        @Field("v") v: String,
        @Field("socket_id") socketId: String,
        @Field("payload") payLoad:String,
        @Field("group_id") groupId:String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("group_members") groupMembers: String,
        @Field("platform") platForm: Int
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/update-group")
    fun deleteGroup(
        @Field("v") v: String,
        @Field("socket_id") socketId: String,
        @Field("payload") payLoad:String,
        @Field("group_id") groupId:String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("delete_group") deleteGroup: String,
        @Field("platform") platForm: Int
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/update-group")
    fun exitGroup(
        @Field("v") v: String,
        @Field("socket_id") socketId: String,
        @Field("payload") payLoad:String,
        @Field("group_id") groupId:String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("platform") platForm: Int
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/add-group-members")
    fun addGroupMembers(
        @Field("v") v: String,
        @Field("socket_id") socketId: String,
        @Field("group_id") groupId: String,
        @Field("user_id") userId: String,
        @Field("access_token")accessToken: String,
        @Field("token") token: String,
        @Field("group_members") groupMembers: JSONArray,
        @Field("platform") platForm: Int
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/create-room")
    fun createRoom(
        @Field("v") v: String,
        @Field("socket_id")socketId: String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,
        @Field("conversation_reference_id")conversationReferenceId: String,
        @Field("room_name") roomName:String,
        @Field("room_description") roomDescription: String,
        @Field("room_avatar") roomAvatar:String,
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/update-room")
    fun updateRoomName(
        @Field("v") v: String,
        @Field("socket_id")socketId: String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,

        @Field("payload") payload: String,
        @Field("room_name") roomName:String,
        @Field("room_id") roomId:String,
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/update-room")
    fun updateRoomAvatar(
        @Field("v") v: String,
        @Field("socket_id")socketId: String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,

        @Field("payload") payload: String,
        @Field("room_id") roomId:String,
        @Field("room_avatar") roomAvatar:String,
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/update-room")
    fun deleteRoom(
        @Field("v") v: String,
        @Field("socket_id")socketId: String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,

        @Field("payload") payload: String,
        @Field("room_id") roomId:String,
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/update-room")
    fun exitRoom(
        @Field("v") v: String,
        @Field("socket_id")socketId: String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,

        @Field("payload") payload: String,
        @Field("room_id") roomId:String,
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/join-room")
    fun joinRoom(
        @Field("v") v: String,
        @Field("socket_id")socketId: String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,

        @Field("payload") payload: String,
        @Field("room_id") roomId:String,
        @Field("room_members") roomMembers: String,
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/get-room-details")
    fun fetchRoomDetails(
        @Field("v") v: String,
        @Field("socket_id")socketId: String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,

        @Field("room_id") roomId: String
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/get-rooms")
    fun fetchRooms(
        @Field("v") v: String,
        @Field("socket_id")socketId: String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int
    ):Deferred<Response<String>>/*Call<String>*/

    @FormUrlEncoded
    @POST("api/messenger/get-all-messages")
    fun fetchAllMessages(
        @Field("v") v: String,
        @Field("user_id") userId: String,
        @Field("at") authorizationToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,
        @Field("device_id")deviceId:String
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/get-updated-messenger-contacts")
    fun fetchUpdateMessengerContacts(
        @Field("v") v: String,
        @Field("socket_id")socketId: String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,
        @Field("device_id")deviceId:String,
        @Field("time")time:String,
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/get-offline-messages")
    fun fetchOfflineMessages(
        @Field("v") v: String,
        @Field("socket_id")socketId: String,
        @Field("user_id") userId: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,
        @Field("device_id")deviceId:String,
        @Field("time")time:String,
        @Field("last_message_id")lastNessageId:String
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/get-all-stories")
    fun fetchAllStories(
        @Field("v") v: String,
        @Field("user_id") userId: String,
        @Field("at") authorizationToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,
        @Field("device_id")deviceId: String
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/get-updated-stories")
    fun fetchUpdatedStories(
        @Field("v") v: String,
        @Field("socket_id") socketId: String,
        @Field("user_id") userId: String,
        @Field("at") authorizationToken: String,
        @Field("access_token") accessToken: String,
        @Field("token") token: String,
        @Field("platform")platForm: Int,
        @Field("device_id")deviceId: String,
        @Field("time") time: String,
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/v2/update-fcm-token")
    fun updateFCMToken(
        @Field("v") v: String,
        @Field("user_id") userId: String,
        @Field("token")token:String,
        @Field("type")type:String,
        @Field("fcm_token") fcmToken: String,
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/v2/delete-fcm-token")
    fun deleteFCMToken(
        @Field("v") v: String,
        @Field("user_id") userId: String,
        @Field("token")token:String,
        @Field("type")type:String,
        @Field("fcm_token")fcmToken:String
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/update-profile-pic")
    fun updateProfilePic(
        @Field("v") v: String,
        @Field("socket_id") socketId: String,
        @Field("user_id") userId: String,
        @Field("token")token:String,
        @Field("at")authorizationToken: String,
        @Field("access_token")accessToken: String,
        @Field("profile_pic")profilePic: String
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/get-user-profile-by-uid")
    fun fetchUserProfilePicByUid(
        @Field("v") v: String,
        @Field("socket_id") socketId: String,
        @Field("uid") uid: String,
        @Field("token")token:String,
        @Field("at")authorizationToken: String,
        @Field("access_token")accessToken: String
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/get-deactivated-users")
    fun fetchDeactivatedUsers(
        @Field("v") v: String,
        @Field("socket_id") socketId: String,
        @Field("user_id") userId: String,
        @Field("token")token:String,
        @Field("at")authorizationToken: String,
        @Field("access_token")accessToken: String,
        @Field("time")time: String
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/get-all-broadcast-messages")
    fun fetchAllBroadCastMessages(
        @Field("v") v: String,
        @Field("user_id") userId: String,
        @Field("token")token:String,
        @Field("at")authorizationToken: String,
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/get-updated-broadcast-messages")
    fun fetchUpdatedBroadCastMessages(
        @Field("v") v: String,
        @Field("socket_id")socketId: String,
        @Field("user_id") userId: String,
        @Field("token")token:String,
        @Field("at")authorizationToken: String,
        @Field("access_token")accessToken: String,
        @Field("time") time: String,
    ):Deferred<Response<String>>/*Call<String>*/
    @FormUrlEncoded
    @POST("api/messenger/add-to-contact")
    fun addNewContact(
        @Field("v") v: String,
        @Field("socket_id")socketId: String,
        @Field("user_id") userId: String,
        @Field("at")authorizationToken: String,
        @Field("access_token")accessToken: String,
        @Field("token")token:String,
        @Field("uid") uid: String,
        @Field("name") name: String,
        @Field("device_id") deviceId: String,
    ):Deferred<Response<String>>/*Call<String>*/
}