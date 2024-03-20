package com.tvisha.trooponprime.lib.api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.tvisha.trooponprime.lib.TroopMessengerClient
import com.tvisha.trooponprime.lib.utils.ConstantValues
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object CallRetrofitClient {
    private const val BASE_URL = "${ConstantValues.CALL_API_BASE_URL}/"
    //val logging = HttpLoggingInterceptor()
    private val okHttpClient = OkHttpClient.Builder()
        .callTimeout(1, TimeUnit.MINUTES)
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .addHeader("Authorization", "Bearer " + TroopMessengerClient.CALL_AUTHORIZATION_TOKEN)
                .method(original.method, original.body)
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        //.addInterceptor(logging.setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()
    val instance: CallApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        retrofit.create(CallApiService::class.java)
    }
}