package com.chatbot.geminiai.repository

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.chatbot.geminiai.adapter.MessageRvAdapter
import com.chatbot.geminiai.api.ChatRequest
import com.chatbot.geminiai.api.Content
import com.chatbot.geminiai.api.GeminiApiService
import com.chatbot.geminiai.api.Part
import com.chatbot.geminiai.utils.ChatList
import com.chatbot.geminiai.utils.ApiState
import com.chatbot.geminiai.utils.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatRepository @Inject constructor(private val apiService: GeminiApiService) {
    suspend fun getAndSaveApiResponse(apiKey: String, request: ChatRequest): ApiState{
        return try {
            val apiResponse = apiService.getApiResponse("application/json", apiKey, request)
            if (apiResponse.isSuccessful) {
                val response = apiResponse.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.replace("*","")
                if (response != null) {
                    val currentUser = Firebase.auth.currentUser
                    val user = User(currentUser?.uid, currentUser?.displayName, currentUser?.email, currentUser?.photoUrl.toString())
                    saveChatsInDatabase(Firebase.database.reference, ChatList(request.contents.firstOrNull()?.parts?.firstOrNull()?.text,response), user)
                    ApiState.Success(response)
                } else {
                    ApiState.Error("No text found in the response")
                }
            } else {
                ApiState.Error(apiResponse.message())
            }
        } catch (e: Exception) {
            ApiState.Error(e.message ?: "An error occurred")
        }
    }
    private suspend fun saveChatsInDatabase(databaseReference: DatabaseReference, chatList: ChatList, user: User) = withContext(
        Dispatchers.IO) {
        val conversationKey = databaseReference.child("Chats").child(user.userId!!).child("Conversations").push().key
        if (conversationKey != null) {
            databaseReference.child("Chats").child(user.userId).child("Conversations").child(conversationKey).setValue(chatList)
        }
    }
    suspend fun retrieveChatsFromDatabase(databaseReference: DatabaseReference, user: User, livedata: MutableLiveData<List<ChatList>>): MutableList<ChatList> = withContext(Dispatchers.IO) {
        val chatList: MutableList<ChatList> = mutableListOf()
        val chatReference = databaseReference.child("Chats").child(user.userId!!).child("Conversations")
        chatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (chat in snapshot.children) {
                    val chatItem = chat.getValue(ChatList::class.java)
                    if (chatItem != null) {
                        chatList.add(chatItem)
                        Log.d("ChatRepository", "Chat: ${chatItem.request} ${chatItem.response}")
                    }
                }
                livedata.postValue(chatList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ChatRepository", "Error fetching chats: ${error.message}")
            }
        })
        chatList
    }

    suspend fun regenerateResponseAndStore(databaseReference: DatabaseReference, user: User, prompt: String) = withContext(Dispatchers.IO) {
        val apiResponse = apiService.getApiResponse("application/json", "AIzaSyCS87bapnHyyTuJ5mp0vcHwMNVoaLIpjzw", ChatRequest(listOf(Content(listOf(Part(prompt))))))
        val chatReference = databaseReference.child("Chats").child(user.userId!!).child("Conversations")
        chatReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (chat in snapshot.children) {
                    val chatItem = chat.getValue(ChatList::class.java)
                    if (chatItem != null && chatItem.request == prompt) {
                        val conversationKey = chat.key
                        val response = chatItem.response
                        if (response != null && conversationKey != null) {
                            if (apiResponse.isSuccessful) {
                                val newResponse = apiResponse.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.replace("*", "")
                                if (!newResponse.isNullOrEmpty()) {
                                    databaseReference.child("Chats").child(user.userId).child("Conversations").child(conversationKey)
                                        .child("response").setValue(newResponse)
                                } else {
                                    Log.d("RegenerateResponse", "Failed to generate a new response")
                                }
                            } else {
                                Log.d("RegenerateResponse", "API call failed: ${apiResponse.message()}")
                            }
                        }
                        break
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("dbError", error.message)
            }
        })
    }


}