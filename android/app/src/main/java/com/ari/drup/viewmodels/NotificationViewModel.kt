package com.ari.drup.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ari.drup.data.FirebaseManager
import com.ari.drup.data.FriendRequest
import com.ari.drup.data.notification.RealtimeManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class NotificationViewModel(
    private val realtimeManager: RealtimeManager,
    private val firebaseManager: FirebaseManager
): ViewModel() {
    private val myRef = realtimeManager.getRef()
    private val _requests = MutableLiveData<List<FriendRequest>>()
    val requests: LiveData<List<FriendRequest>> = _requests

    fun startListening(username: String) {
        Log.d("NotificationViewModel", "Checking for $username")

        val userRef = myRef.child(username).child("requests")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<FriendRequest>()
                Log.d("NotificationViewModel", "Snapshot size: ${snapshot.children.count()}")

                for (req in snapshot.children) {
                    val request = req.getValue(FriendRequest::class.java)
                    if (request != null) {
                        list.add(request)
                    }
                }

                Log.d("NotificationViewModel", "Received ${list.size} friend requests")
                _requests.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", error.message)
            }
        }

        userRef.addValueEventListener(listener)
    }


    suspend fun removeRequest(currentUsername: String, currentEmail:String, request: FriendRequest, rejected: Boolean){
        realtimeManager.removeRequest(currentUsername,request.username)
        if (!rejected){
            firebaseManager.addFriend(currentEmail,request.email)
            firebaseManager.addFriend(request.email,currentEmail)
        }
    }
}