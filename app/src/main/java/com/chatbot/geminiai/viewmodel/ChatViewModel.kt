package com.chatbot.geminiai.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatbot.geminiai.api.ChatRequest
import com.chatbot.geminiai.repository.ChatRepository
import com.chatbot.geminiai.utils.ApiState
import com.chatbot.geminiai.utils.ChatList
import com.chatbot.geminiai.utils.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatViewModel @Inject constructor(private val repository: ChatRepository): ViewModel() {
    private val _responseData = MutableLiveData<ApiState>()
    val responseData: LiveData<ApiState> = _responseData

    private val _chatList = MutableLiveData<List<ChatList>>()
    val chatList: LiveData<List<ChatList>> = _chatList

    fun getAndSaveApiResponse(apiKey: String, request: ChatRequest) = viewModelScope.launch {
        _responseData.value = repository.getAndSaveApiResponse(apiKey, request)
    }

    fun retrieveChatsFromDatabase(databaseReference: DatabaseReference, user: User) = viewModelScope.launch {
        repository.retrieveChatsFromDatabase(databaseReference, user, _chatList)
        Log.d("retrieve", _chatList.value.toString())
    }

    fun regenerateResponseAndStore(prompt: String) = viewModelScope.launch {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            repository.regenerateResponseAndStore(Firebase.database.reference, User(currentUser.uid), prompt)
        } else {
            Log.e("ChatViewModel", "User is null")
        }
    }
}