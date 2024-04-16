package com.chatbot.geminiai.utils

data class MessageRvModel(
    val message: String, val photoUrl: String?, val sender: String, val userId: String?, val messageCopy: String?,
    val messageShare: String?/*, val messageRegenerate: String, val messageEdit: String*/)
