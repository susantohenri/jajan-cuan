package com.henrisusanto.vitmate.data.api

import com.henrisusanto.vitmate.util.AnalyticsLogger
import com.squareup.okhttp3.OkHttpClient
import com.squareup.okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.KotlinxSerializerFactory

class ApiClient {
    private val baseUrl = "https://jajan-cuan-default-rtdb.asia-southeast1.firebasedatabase.app/"

    private val loggingInterceptor = HttpLoggingInterceptor {
        AnalyticsLogger.logAdsRequest("retrofit", "api")
    }.setLevel(HttpLoggingInterceptor.Level.BASIC)

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(KotlinxSerializerFactory())
        .build()

    fun <T> create(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}