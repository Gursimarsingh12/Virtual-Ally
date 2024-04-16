package com.chatbot.geminiai.utils

sealed class AuthState {
    data object Success : AuthState()
    data object Loading: AuthState()
    class Error(val message: String ?= null): AuthState()
}