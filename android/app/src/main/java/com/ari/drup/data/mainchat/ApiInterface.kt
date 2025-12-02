package com.ari.drup.data.mainchat

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ChatApi {
    @POST("chat")
    suspend fun sendMessage(
        @Query("code") apiKey: String,
        @Body request: AzureQuery
    ): Response

    @POST(value = "notification")
    suspend fun getNotification(
        @Query("code") apiKey: String,
        @Body request: NotifApi
    ): NotifRes
}

sealed class ApiState<out T> {
    object Idle : ApiState<Nothing>()
    object Waiting : ApiState<Nothing>()
    data class Success<T>(val data: T) : ApiState<T>()
    data class Failed(val error: String) : ApiState<Nothing>()
}