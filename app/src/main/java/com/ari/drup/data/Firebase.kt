package com.ari.drup.data

import android.content.ContentValues.TAG
import android.util.Log
import com.ari.drup.data.community.Community
import com.ari.drup.data.mainchat.MessDao
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.getField
import com.google.type.DateTime
import kotlinx.coroutines.tasks.await
import kotlin.collections.mapOf

class FirebaseManager {
    private val db = Firebase.firestore
    private var allUsernames = emptyList<String>()
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

    suspend fun getChatRoomUsers(communityName: String): Map<String, String> {
        return try {
            val document = db.collection("communities")
                .document(communityName)
                .get()
                .await()

            if (!document.exists()) {
                Log.e("Firestore", "Document does not exist")
                return emptyMap()
            }

            // Firestore stores "users" as List<Map<String, String>>
            val usersList = document.get("users") as? List<Map<String, String>>
            if (usersList == null || usersList.isEmpty()) {
                Log.e("Firestore", "Users list empty")
                return emptyMap()
            }

            usersList[0]   // return the first map

        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching chat users", e)
            emptyMap()
        }
    }

    suspend fun addUserToCommunity(communityName: String, email: String, uname: String) {
        try {
            val documentRef = db.collection("communities").document(communityName)

            val snapshot = documentRef.get().await()
            if (!snapshot.exists()) {
                Log.e("Firestore", "Community does not exist: $communityName")
                return
            }

            // Fetch users list
            val usersList = snapshot.get("users") as? List<Map<String, String>>
                ?: listOf(emptyMap())

            // The map at index 0
            val firstMap = usersList.getOrNull(0)?.toMutableMap() ?: mutableMapOf()

            // Add/update entry
            firstMap[email] = uname

            // Put this map back at index 0
            val updatedList = listOf(firstMap)

            // Upload back to Firestore
            documentRef.update("users", updatedList).await()

            Log.d("Firestore", "Added/Updated user: $email -> $uname")

        } catch (e: Exception) {
            Log.e("Firestore", "Error adding user to community", e)
        }
    }





    suspend fun registerNewUser(newUser : User,email : String): Boolean{
        return try{
            db.collection("users")
                .document(email)
                .set(newUser)
                .await()
            db.collection("users")
                .document("all")
                .set(mapOf(newUser.username to email), SetOptions.merge())
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

    suspend fun checkValidUsername(username: String): Boolean {
        if (allUsernames.isEmpty()) {
           setUsernames()
        }
        return username in allUsernames
    }

    fun changeVisibilityLevel(email:String,visibility:Int){
        db.collection("users").document(email).update("visibility",visibility)
    }
    fun clearAllUsers(){
        allUsernames = emptyList()
    }
    suspend fun setUsernames(){
        val document = db.collection("users").document("all").get().await()
        allUsernames = document.data?.keys?.toList() ?: emptyList()
    }

    fun getUsernames(): List<String>{
        return allUsernames
    }

    suspend fun addFriend( currentEmail: String, senderEmail: String) {
        val userRef = db.collection("users").document(currentEmail)

        try {
            // Atomically add the new friend username to the list field "friends"
            userRef.update("friends", FieldValue.arrayUnion(senderEmail))
                .await() // suspend until the operation completes
            Log.d("Firestore", "Friend added successfully: $senderEmail")
        } catch (e: Exception) {
            // If the "friends" field doesn’t exist yet, create it
            if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                userRef.set(mapOf("friends" to listOf(senderEmail)), SetOptions.merge())
                    .await()
                Log.d("Firestore", "Friend list created and friend added: $senderEmail")
            } else {
                Log.e("Firestore", "Error adding friend", e)
            }
        }
    }

    suspend fun getFriendList(email: String): MutableList<Friend> {
        val friendList = mutableListOf<Friend>()

        try {
            val documentSnapshot = db.collection("users").document(email).get().await()
            val friendEmails = documentSnapshot.get("friends") as? List<String> ?: emptyList()

            for (friendEmail in friendEmails) {
                val friendDocumentSnapshot = db.collection("users").document(friendEmail).get().await()
                Log.d("FirebaseFriend","${friendDocumentSnapshot.getLong("avatar")?.toInt()}")
                val friend = Friend(
                    email = friendEmail,
                    username = friendDocumentSnapshot.getString("username") ?: "",
                    avatar = friendDocumentSnapshot.getLong("avatar")?.toInt() ?: 0
                )
                friendList.add(friend)
            }
        }
        catch (e : Exception){
            Log.e("FirebaseFriend","Error $e")
        }
        return friendList
    }


}

