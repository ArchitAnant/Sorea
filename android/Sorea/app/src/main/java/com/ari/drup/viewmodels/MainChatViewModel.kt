package com.ari.drup.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ari.drup.BuildConfig
import com.ari.drup.data.FirebaseManager
import com.ari.drup.data.mainchat.ApiState
import com.ari.drup.data.mainchat.AzureClient
import com.ari.drup.data.mainchat.MessDao
import com.ari.drup.data.mainchat.AzureQuery
import com.ari.drup.data.mainchat.Response
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

    private val _chats = MutableStateFlow<List<MessDao>>(mutableListOf())
    val chats = _chats.asStateFlow()

    private val _chatState = MutableStateFlow<ApiState<Response>>(ApiState.Idle)
    val chatState  = _chatState.asStateFlow()

    fun resetResponseState() {
        _chatState.value = ApiState.Idle // or null if you prefer
    }
    fun initializeChatScreen() {
        fetchChatNames() // triggers async update

        viewModelScope.launch {
            chatList.collect { list ->
                val today = list.getTodayIfPresent()
                if (!today.isNullOrBlank()) {
                    selectChat(today)
                    fetchChats()
                }
            }
        }
    }


    fun fetchChats() {
        if (!selectedChat.value.isNullOrBlank()){
            viewModelScope.launch {
                val fetchedChats = firebaseManager.fetchMessages(
                    onboardingViewModel.currentUserEmail!!,
                    selectedChat.value!!
                ) // this gives List<Map<String, MessDao>>

                // Flatten all messages into one list
                val allMessages = fetchedChats.flatMap { it.values }

                // Sort by timestamp (ascending = oldest first)
                val sortedMessages = allMessages.sortedByDescending { it.timestamp }

                // If you want descending (newest first):
                // val sortedMessages = allMessages.sortedByDescending { it.timestamp }

                // Store back in StateFlow (wrap in listOf(mapOf(..)) if needed)
                _chats.value = sortedMessages
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
    fun getSelectedChat(): String {
        val current = _selectedChat.value
        if (current != null) return current

        // Generate today's chat ID: conv_yyyymmdd
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val todayChatId = "conv_${today.format(formatter)}"

        // Call selectChat with today's chat
        selectChat(todayChatId)

        return todayChatId
    }

    fun sendMessage(message: String) {
        _chatState.value = ApiState.Waiting
        viewModelScope.launch {

            try {
                val request = AzureQuery(onboardingViewModel.currentUserEmail!!,message)
                val response = AzureClient().chatApi.sendMessage(BuildConfig.AZURE_KEY, request)
                _chatState.value = ApiState.Success(response)
            } catch (e: Exception) {
                _chatState.value = ApiState.Failed(e.localizedMessage ?: "Unknown error")
            }
        }
    }
    fun appendMess(messDao: MessDao) {
        _chats.value = _chats.value + messDao
        fetchChats()
        fetchChatNames()
    }


    fun isToday(dateString: String): Boolean {
        return try {
            val cleanString = dateString.removePrefix("conv_") // get yyyyMMdd
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.getDefault())
            val parsedDate = LocalDate.parse(cleanString, formatter)
            val today = LocalDate.now()
            parsedDate.isEqual(today)
        } catch (e: Exception) {
            false
        }
    }

    fun List<String>.getTodayIfPresent(): String? {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.getDefault())
        val today = LocalDate.now().format(formatter)

        return this.find { raw ->
            raw.lowercase().removePrefix("conv_").trim() == today
        }
    }
}