package com.chatbot.geminiai.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chatbot.geminiai.utils.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepository @Inject constructor(private val databaseReference: DatabaseReference) {
    suspend fun saveUser(user: User) = withContext(Dispatchers.IO){
        user.userId?.let { databaseReference.child("Users").child(it).setValue(user) }
    }

    suspend fun getUser(user: User): User = withContext(Dispatchers.IO){
        val userReference = user.userId?.let { databaseReference.child("Users").child(it) }
        userReference?.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(User::class.java)
                Log.d("user info ", snapshot.getValue(User::class.java).toString())
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("UserRepository", "Error fetching user: ${error.message}")
            }

        })
        return@withContext user
    }
}