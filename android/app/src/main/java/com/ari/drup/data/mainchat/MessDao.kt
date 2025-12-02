package com.ari.drup.data.mainchat

import com.google.firebase.Timestamp

data class MessDao(
    val isCrisis: Boolean = false,
    val model : String = "",
    val user: String = "",
    val timestamp: Timestamp = Timestamp.Companion.now(),
    val urgencyLevel: Int = 0,
)