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
//import com.ari.drup.data.community.ChatWebSocket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val SERVER_URL = "wss://websocket-web-server-1.onrender.com"

class GroupChatViewModel(
    private val onboardingViewModel: OnboardingViewModel,
    private val firebaseManager: FirebaseManager
) : ViewModel(){

    private var chatWebSocket: ChatWebSocket? = null
    private val _activeCommunities = MutableStateFlow(mutableStateOf(listOf<Community>()))
    val activeCommunities = _activeCommunities.asStateFlow()

    init {
        fillActiveCommunities()
    }

    private val _selectedCommunity = MutableStateFlow(mutableStateOf<Community?>(null))
    val selectedCommunity = _selectedCommunity.asStateFlow().value

    fun setCommunity(community: Community){
        _selectedCommunity.value.value = community
    }

    private var _chatLoading = MutableStateFlow(mutableStateOf(false))
    val chatLoading = _chatLoading.asStateFlow()

    private val _chatBox = MutableStateFlow(mutableStateOf(""))
    val chatBox = _chatBox.asStateFlow()
    var chatTitle = mutableStateOf("")


    fun setChatBox(message: String){
        _chatBox.value.value = message
    }

    private val _messages = MutableStateFlow(Messages())
    val messages = _messages



    fun setMessages(newMessages: Messages) {
        _messages.value = newMessages
    }

    fun addChat(chat: Chat) {
        val current = _messages.value
        val updatedList = current.messages.toMutableList()
        updatedList.add(chat)
        _messages.value = current.copy(messages = updatedList.sortedBy { it.timestamp })
    }

    fun checkUserInCommunity(): Boolean{
        if (selectedCommunity.value!=null){
            selectedCommunity.value!!.users.forEach { map->
                if (map.values.toList()[0]==onboardingViewModel.currentUserEmail){
                    return true
                }
            }
        }
        return false
    }

    fun joinRoom(roomId: String) {
        // Disconnect previous socket if switching rooms
        chatWebSocket?.disconnect()
        _chatBox.value.value = ""
        _messages.value = Messages()

        chatWebSocket = ChatWebSocket(
            token = onboardingViewModel.firebaseIdToken!!,
            roomId = roomId,
            viewModel = this
        )
        chatWebSocket?.connect()
    }

    fun setChatLoading(loading: Boolean){
        _chatLoading.value.value = loading
    }

    fun fetchChats() {
        chatWebSocket?.loadChat()
    }

    fun sendMessage() {
        if (chatBox.value.value.isNotBlank()) {
            chatWebSocket?.sendMessage(
                text = chatBox.value.value,
                userId = onboardingViewModel.currentUserEmail!!
            )
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
}