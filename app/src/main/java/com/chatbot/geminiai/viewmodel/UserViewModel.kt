package com.chatbot.geminiai.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatbot.geminiai.repository.UserRepository
import com.chatbot.geminiai.utils.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserViewModel @Inject constructor(private val repository: UserRepository): ViewModel() {
    fun saveUser(user: User) = viewModelScope.launch {
        repository.saveUser(user)
    }
    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData
    fun getUser(user: User) = viewModelScope.launch {
        val retrievedUser = repository.getUser(user)
        _userData.value = retrievedUser
    }
}