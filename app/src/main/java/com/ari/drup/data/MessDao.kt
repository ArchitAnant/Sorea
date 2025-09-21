package com.ari.drup.data

import com.google.firebase.Timestamp

data class MessDao(
    val isCrisis: Boolean = false,
    val model : String = "",
    val user: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val urgencyLevel: Int = 0,
)

