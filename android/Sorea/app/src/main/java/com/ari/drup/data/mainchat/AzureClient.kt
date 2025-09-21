package com.ari.drup.data.mainchat

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AzureClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)  // connection timeout
    .readTimeout(60, TimeUnit.SECONDS)     // read timeout
    .writeTimeout(60, TimeUnit.SECONDS)    // write timeout
    .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
    .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://mentalbot.azurewebsites.net/api/") // <-- must end with /
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val chatApi: ChatApi = retrofit.create(ChatApi::class.java)
}