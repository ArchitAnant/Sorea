package com.ari.drup.data

data class Chat(
    val chatId: String,
    val text: String,
    val timestamp: Long,
    val username: String,
    val image: String
)
