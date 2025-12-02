package com.ari.drup.viewmodels

import ChatWebSocket
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ari.drup.data.community.Chat
import com.ari.drup.data.community.Community
import com.ari.drup.data.FirebaseManager
import com.ari.drup.data.community.Messages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.collection.ImmutableSortedMap.Builder.emptyMap
//import com.ari.drup.data.community.ChatWebSocket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.collections.emptyMap

const val SERVER_URL = "wss://websocket-web-server-1.onrender.com"

class GroupChatViewModel(
    val onboardingViewModel: OnboardingViewModel,
    private val firebaseManager: FirebaseManager
) : ViewModel(){

    private var chatWebSocket: ChatWebSocket? = null
    private val _activeCommunities = MutableStateFlow(mutableStateOf(listOf<Community>()))
    val activeCommunities = _activeCommunities.asStateFlow()

    private val _currUsers = MutableStateFlow(mutableStateOf(emptyMap<String, String>()))
    val currUsers = _currUsers.asStateFlow()

    init {
        fillActiveCommunities()
    }

    private val _selectedCommunity = MutableStateFlow<Community?>(null)
    val selectedCommunity = _selectedCommunity.asStateFlow() // Use asStateFlow for read-only access

    private var _chatLoading = MutableStateFlow(false)
    val chatLoading = _chatLoading.asStateFlow()

    private val _chatBox = MutableStateFlow("")
    val chatBox = _chatBox.asStateFlow()
    var chatTitle = mutableStateOf("")

    fun setChatBox(message: String) {
        _chatBox.value = message // Simpler update
    }


    private val _messages = MutableStateFlow(Messages())
    val messages = _messages.asStateFlow()



    fun setMessages(newMessages: Messages) {
        _messages.value = newMessages
    }

    fun addChat(chat: Chat) {
        _messages.update { currentMessagesObject ->
            val updatedList = listOf(chat) + currentMessagesObject.messages
            // Return a new Messages object with the updated list
            currentMessagesObject.copy(messages = updatedList)
        }
    }


    fun joinRoom(roomId: String) {
        // Disconnect previous socket if switching rooms
        chatWebSocket?.disconnect()
        _chatBox.value = ""
        _messages.value = Messages()

        chatWebSocket = ChatWebSocket(
            token = onboardingViewModel.firebaseIdToken!!,
            roomId = roomId,
            viewModel = this
        )
        chatWebSocket?.connect()
    }

    fun setChatLoading(loading: Boolean){
        _chatLoading.value = loading
    }

    fun fetchChats() {
        chatWebSocket?.loadChat()
    }

    fun sendMessage() {
        if (chatBox.value.isNotBlank()) {
            chatWebSocket?.sendMessage(
                text = chatBox.value,
                userId = onboardingViewModel.currentUserEmail!!
            )
            addChat(Chat(
                onboardingViewModel.currentUserEmail!!,
                onboardingViewModel.currentUserEmail!!,
                chatBox.value,
                OffsetDateTime.now(ZoneOffset.UTC).toString()
            ))
            setChatBox("") // clear the input box
        }
    }


    fun fillActiveCommunities(){
        Log.d("fillActiveCommunities","called")
        viewModelScope.launch {
            _activeCommunities.value.value = firebaseManager.fetchActiveCommunities()
        }
    }
    fun createCommunity(email:String,name: String,community: Community){
        viewModelScope.launch {
            firebaseManager.createCommunity(community,email,name)
            fillActiveCommunities()
        }
    }

    suspend fun fillCurrUsers(communityName: String) {
        _currUsers.value.value = firebaseManager.getChatRoomUsers(communityName)
    }

    fun getUsername(email: String): String {
        if (currUsers.value.value.isNotEmpty()){
            if(currUsers.value.value.keys.contains(email)){
                return currUsers.value.value[email]!!
            }
        }
        return "Unknown"
    }

    fun checkUserInCommunity(email:String): Boolean{
        if (currUsers.value.value.isNotEmpty()){
            if(currUsers.value.value.keys.contains(email)){
                return true
            }
        }
        return false

    }

    fun addUserToCommunity(communityName: String,email:String,uname : String){
        viewModelScope.launch {
            firebaseManager.addUserToCommunity(communityName, email, uname)
            fillCurrUsers(communityName)
        }
    }


}