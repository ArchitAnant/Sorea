package com.ari.drup.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ari.drup.data.FirebaseManager
import com.ari.drup.data.MessDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainChatViewModel(
    private val onboardingViewModel: OnboardingViewModel,
    private val firebaseManager: FirebaseManager
): ViewModel() {

    private val _chatList = MutableStateFlow<List<String>>(mutableListOf())
    val chatList = _chatList.asStateFlow()

    private val _selectedChat = MutableStateFlow<String?>(null)
    val selectedChat = _selectedChat.asStateFlow()

    private val _chats = MutableStateFlow<List<Map<String, MessDao>>>(mutableListOf())
    val chats = _chats.asStateFlow()


    fun initializeChatScreen() {
        fetchChatNames()
        val today:String? = chatList.value.getTodayIfPresent()
        if (!today.isNullOrBlank()){
            selectChat(today)
            fetchChats()
        }
    }


    fun fetchChats() {
        if (!selectedChat.value.isNullOrBlank()){
            Log.d("MainChatViewModel", "Fetching chats for chat: ${selectedChat.value}")
            viewModelScope.launch {
                val fetchedChats = firebaseManager.fetchMessages(
                    onboardingViewModel.currentUserEmail!!,
                    selectedChat.value!!
                )
                // Sort messages by timestamp ascending (oldest first)
                val sortedChats = fetchedChats.map { it.toList() } // convert map to list of pairs
                    .map { it.sortedBy { pair -> pair.second.timestamp } } // sort each chat map
                    .map { it.toMap() } // convert back to map if needed

                _chats.value = sortedChats
            }
        }
    }

    fun fetchChatNames() {
        viewModelScope.launch {
            val fetchedChatNames = firebaseManager.fetchAllChatNames(onboardingViewModel.currentUserEmail!!)

            // Sort chat names as dates
            val sortedChatNames = fetchedChatNames.sortedByDescending { chatName ->
                try {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy")
                    java.time.LocalDate.parse(chatName, formatter)
                } catch (e: Exception) {
                    // fallback if parsing fails, use MIN so invalid dates go last
                    java.time.LocalDate.MIN
                }
            }

            _chatList.value = sortedChatNames
            Log.d("MainChatViewModel", "Chat names fetched and sorted: ${chatList.value}")
        }
    }

    fun selectChat(chatName: String) {
        _selectedChat.value = chatName
    }
    fun getSelectedChat(): String?{
        return _selectedChat.value
    }

    fun sendChat(email:String,prompt: String){


    }
    fun isToday(dateString: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("ddMMyyyy", Locale.getDefault())
            val parsedDate = LocalDate.parse(dateString, formatter)
            val today = LocalDate.now()
            parsedDate.isEqual(today)
        } catch (e: Exception) {
            false
        }
    }
    fun List<String>.getTodayIfPresent(): String? {
        val formatter = DateTimeFormatter.ofPattern("ddMMyyyy", Locale.getDefault())
        val today = LocalDate.now().format(formatter) // today's date in ddMMyyyy
        return this.find { it == today }
    }
}