package com.chatbot.geminiai.utils

sealed class ApiState {
    data class Success(val response: String): ApiState()
    data class Error(val error: String): ApiState()
    data object Loading: ApiState()
}