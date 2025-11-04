package com.example.quickpoll

data class User(
    val uid: String = "",
    val email: String = "",
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val displayName: String = ""
)