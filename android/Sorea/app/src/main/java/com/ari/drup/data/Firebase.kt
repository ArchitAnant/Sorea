package com.ari.drup.data

import android.content.ContentValues.TAG
import android.util.Log
import com.ari.drup.data.community.Community
import com.ari.drup.data.mainchat.MessDao
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.type.DateTime
import kotlinx.coroutines.tasks.await

class FirebaseManager {
    private val db = Firebase.firestore

    suspend fun checkRegisteredUsers(email: String): Boolean{
        return try {
            val document = db.collection("users")
                .document(email)
                .get()
                .await()
            document.exists()
        } catch (e : Exception){
            Log.e(TAG, "Error checking document", e)
            false
        }
    }


    suspend fun registerNewUser(newUser : User,email : String): Boolean{
        return try{
            db.collection("users")
                .document(email)
                .set(newUser)
                .await()
            true
        }
        catch (e : Exception){
            Log.e(TAG, "Error writing document", e)
            false
        }
    }

    suspend fun getRegisteredUser(email: String): User? {
        return try {
            val documentSnapshot = db.collection("users")
                .document(email)
                .get()
                .await()

            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(User::class.java)
            } else {
                Log.w(TAG, "No user found with email: $email")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user", e)
            null
        }
    }
    suspend fun fetchAllChatNames(userEmail: String): List<String> {
        Log.d("FirebaseManager", "Fetching chat names for user: $userEmail")
        return try {
            val snapshot = db.collection("users")
                .document(userEmail)
                .collection("conversations")
                .get()
                .await()
            Log.d("tag",snapshot.documents.toList().toString())
            snapshot.documents.map { it.id }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun fetchMessages(
        email: String,
        chatId: String
    ): List<Map<String, MessDao>> {

        val snapshot = db.collection("users")
            .document(email)
            .collection("conversations")
            .document(chatId)
            .collection("chat")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val mess = doc.toObject(MessDao::class.java)
            mess?.let { mapOf(doc.id to it) }
        }
    }

    suspend fun createCommunity(community: Community, email: String, name:String){
        try {
            db.collection("communities")
                .document(name)
                .set(community)
        }catch (e : Exception){
            Log.e(TAG, "Error writing document", e)
        }
    }

    suspend fun fetchActiveCommunities(): List<Community>{
        return try {
            val snapshot = db.collection("communities")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(Community::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching communities", e)
            emptyList()
        }
    }

    suspend fun getLastActiveTime(email: String): Timestamp? {
        Log.d("FirebaseManager", "Fetching chat names for user: $email")
        return try {
            val snapshot = db.collection("users")
                .document(email)
                .collection("conversations")
                .get()
                .await()

            Log.d("tag", snapshot.documents.toString())

            snapshot.documents
                .mapNotNull { it.getTimestamp("lastMessageAt") } // safe cast
                .maxByOrNull { it } // latest timestamp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

     fun listenToMessages(
        userEmail: String,
        chatId: String,
        onNewMessage: (List<MessDao>) -> Unit
    ){
        db.collection("users")
            .document(userEmail)
            .collection("conversations")
            .document(chatId)
            .collection("chat")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener {
                snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val messages = snapshot.documents.mapNotNull { it.toObject(MessDao::class.java) }
                onNewMessage(messages)
            }
    }
}

