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

}

sealed class ApiState<out T> {
    object Idle : ApiState<Nothing>()
    object Waiting : ApiState<Nothing>()
    data class Success<T>(val data: T) : ApiState<T>()
    data class Failed(val error: String) : ApiState<Nothing>()
}