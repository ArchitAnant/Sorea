package com.ari.drup.data.community

data class Chat(
    val userId: String = "",
    val text: String = "",
    val timestamp: String = "",
)

data class Messages(
    val messages: List<Chat> = emptyList<Chat>(),
    val nextCursor: String? = ""
)

data class MessagesResponse(
    val type: String,
    val messages: Messages
)
