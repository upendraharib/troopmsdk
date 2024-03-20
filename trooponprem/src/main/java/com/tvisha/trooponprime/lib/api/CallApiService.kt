package com.tvisha.trooponprime.lib.api

import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface CallApiService {
    @FormUrlEncoded()
    @POST("api/register")
    fun registerTm(
        @Field("v") v: String,
        @Field("token") token :String,
        @Field("uid") uid:String,
        @Field("name") name:String,
        @Field("mobile_number") mobileNumber : String,
        @Field("mobile_cc") countryCode:String,
        @Field("avatar") avatar:String,
        @Field("email") email:String,
    ): Deferred<Response<String>>
    @FormUrlEncoded()
    @POST("api/fetch-user-by-uids")
    fun fetchUsersByUid(
        @Field("v") v: String,
        @Field("uids") uids :String,
    ): Deferred<Response<String>>
    @FormUrlEncoded()
    @POST("api/fetch-users")
    fun fetchUsers(
        @Field("v") v:String,
    ):Deferred<Response<String>>
}