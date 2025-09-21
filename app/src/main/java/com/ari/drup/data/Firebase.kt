package com.ari.drup.data

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.function.BooleanSupplier
import kotlin.math.log

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
                .collection("conversation")
                .document("chats")
                .collection("chats") // if "chats" is itself a subcollection
                .get()
                .await()

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
            .collection("conversation")
            .document("chats")
            .collection("chats")
            .document(chatId)
            .collection(chatId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val mess = doc.toObject(MessDao::class.java)
            mess?.let { mapOf(doc.id to it) }
        }
    }

    suspend fun createCommunity(community: Community, email: String,name:String){
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
}

