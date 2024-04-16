package com.chatbot.geminiai.repository

import com.chatbot.geminiai.utils.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(private val auth: FirebaseAuth) {
    suspend fun signUp(email: String, password: String) = withContext(Dispatchers.IO){
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            AuthState.Success
        }catch (e: Exception){
            AuthState.Error("Registration failed! Try again")
        }
    }

    suspend fun signIn(email: String, password: String) = withContext(Dispatchers.IO){
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthState.Success
        }catch (e: Exception){
            AuthState.Error("Login failed! Try again")
        }
    }

    fun currentUser() = auth.currentUser

    fun signOut() = auth.signOut()

    suspend fun googleSignIn(token: String?) = withContext(Dispatchers.IO){
        try{
            val credential = GoogleAuthProvider.getCredential(token, null)
            auth.signInWithCredential(credential).await()
            AuthState.Success
        }catch (e: Exception){
            AuthState.Error("SignIn failed")
        }
    }
}