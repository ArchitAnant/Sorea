package com.ari.drup.viewmodels

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ari.drup.data.FirebaseManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

fun parseChatDate(chatId: String): LocalDate? {
    return try {
        val clean = chatId.removePrefix("conv_")
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        LocalDate.parse(clean, formatter)
    } catch (e: Exception) {
        null
    }
}

class HomeScreenViewModel(
    private val firebaseManager: FirebaseManager,
    private val onboardingViewModel: OnboardingViewModel,
) : ViewModel(){
    private val _streakCount = MutableStateFlow(0)
    val streakCount = _streakCount.asStateFlow()
    private val _chatList = MutableStateFlow<List<String>>(mutableListOf())
    val chatList = _chatList.asStateFlow()

    private val _today = MutableStateFlow<String?>(null)
    val today = _today.asStateFlow()


    fun calculateStreak() {
        val chats = chatList.value
        if (chats.isEmpty()) {
            Log.d("Streak","The stupid list is empty!")
            _streakCount.value = 0
            return
        }

        val dates = chats.mapNotNull { parseChatDate(it) }.sortedDescending()
        if (dates.isEmpty()) {
            _streakCount.value = 0
            return
        }

        var streak = 1
        var current = dates.first()

        for (i in 1 until dates.size) {
            val next = dates[i]
            val diff = ChronoUnit.DAYS.between(next, current)

            when (diff) {
                1L -> { streak++; current = next }
                0L -> continue
                else -> break
            }
        }

        val today = LocalDate.now()
        val lastChat = dates.first()
        val gap = ChronoUnit.DAYS.between(lastChat, today)
        if (gap > 1) streak = 0

        Log.d("Streak", streak.toString())
        _streakCount.value = streak
    }
    fun initializeChatScreen(selectCurrentChat:(String)-> Unit) {
        fetchChatNames() // triggers async update
        viewModelScope.launch {
            chatList.collect { list ->
                _today.value = list.getTodayIfPresent()
                if (!today.value.isNullOrBlank()) {
                    selectCurrentChat(today.value!!)
                }
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
            calculateStreak()
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