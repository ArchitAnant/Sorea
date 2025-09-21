package com.ari.drup.data.mainchat


data class AzureQuery(
    val email: String,
    val message : String
)

data class Response(
    val message : String,
    val timestamp: String
)