package com.chatbot.geminiai.utils

data class User(
    val userId: String ?= null,
    val email: String ?= null,
    val password: String ?= null,
    val userName: String ?= null,
    val profilePic: String ?= null
)
