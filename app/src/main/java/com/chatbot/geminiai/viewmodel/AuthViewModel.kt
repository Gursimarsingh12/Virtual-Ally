package com.chatbot.geminiai.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatbot.geminiai.repository.AuthRepository
import com.chatbot.geminiai.utils.AuthState
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel @Inject constructor(private val repository: AuthRepository): ViewModel() {
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    fun signUp(email :String, password: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        _authState.value = repository.signUp(email, password)
    }

    fun signIn(email: String, password: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        _authState.value = repository.signIn(email, password)
    }

    fun signOut() = repository.signOut()

    fun currentUser() = repository.currentUser()

    fun googleSignIn(token: String?) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        _authState.value = repository.googleSignIn(token)
    }
}