package com.ari.drup.data.notification

import android.content.ContentValues.TAG
import android.util.Log
import com.ari.drup.data.FriendRequest
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue

class RealtimeManager{
    private val database = Firebase.database
    private val myRef = database.getReference("notifications")

    fun sendRequestNotification(recUsername: String, request: FriendRequest) {
        val requestRef = myRef
            .child(recUsername)
            .child("requests")
            .child(request.username)

        requestRef.setValue(request)
            .addOnSuccessListener {
                Log.d("Firebase", "Request sent from ${request.username} to $recUsername")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to send request: ${e.message}")
            }
    }


    fun removeRequest(currentUsername: String, senderUsername: String) {
        val userRef = myRef
            .child(currentUsername)
            .child("requests")
            .child(senderUsername)

        userRef.removeValue()
            .addOnSuccessListener {
                Log.d("Firebase", "Removed request from $senderUsername")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to remove request: ${e.message}")
            }
    }


    fun getRef(): DatabaseReference {
        return myRef
    }




}