package com.ari.drup.data

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.function.BooleanSupplier

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
}

