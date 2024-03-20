package com.tvisha.trooponprime.lib.api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.tvisha.trooponprime.lib.TroopClient
import com.tvisha.trooponprime.lib.utils.ConstantValues
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


object BaseRetrofitClient {
    private const val BASE_URL = "${ConstantValues.API_BASE_URL}/"
    //val logging = HttpLoggingInterceptor()
    private val okHttpClient = OkHttpClient.Builder()
        .callTimeout(1, TimeUnit.MINUTES)
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                /*.addHeader("Authorization", "Bearer " + MessengerApplication.mSocket)*/
                .method(original.method, original.body)
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        //.addInterceptor(logging.setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()
    val instance: BaseAPIService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        retrofit.create(BaseAPIService::class.java)
    }
}