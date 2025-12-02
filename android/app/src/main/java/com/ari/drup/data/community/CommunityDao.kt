package com.ari.drup.data.community

import com.google.firebase.Timestamp


data class Community(
    val chatId : String = "",
    val title: String = "",
    val desc: String = "",
    val logo : String = "",
    val creationDate : Timestamp = Timestamp.now(),
    val users : List<Map<String, String>> = emptyList(),
    val createdBy : Map<String, String> = emptyMap()
)