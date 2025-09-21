package com.ari.drup.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ari.drup.data.Chat
import com.ari.drup.data.Community
import com.ari.drup.data.FirebaseManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroupChatViewModel(
    private val firebaseManager: FirebaseManager
) : ViewModel(){
    private val _activeCommunities = MutableStateFlow(mutableStateOf(listOf<Community>()))
    val activeCommunities = _activeCommunities.asStateFlow()

    init {
        fillActiveCommunities()
    }

    val groupChatMessages = mutableListOf<String>()
    private val _chatBox = mutableStateOf("")
    var chatTitle = mutableStateOf("")
//    create a state flow list


    fun setChatBox(message: String){
        _chatBox.value = message
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

val currentTimestamp: Long = System.currentTimeMillis()

val dummyChatList = listOf(
    Chat(
        chatId = "id1",
        text = "hey, how are you",
//        set a dummy timestamp : Timestamp
        timestamp = currentTimestamp,
        username = "ari_archit",
        image = "https://developer.android.com/static/guide/practices/ui_guidelines/images/article_icon_adaptive.gif"
    ),
    Chat(
        chatId = "id2",
        text = "hey, how are you",
        timestamp = currentTimestamp+100000,
        username = "ari_archit",
        image = "https://developer.android.com/static/guide/practices/ui_guidelines/images/article_icon_adaptive.gif"
    ),
    Chat(
        chatId = "id3",
        text = "hey, how are you",
        timestamp = currentTimestamp+200000,
        username = "ari_archit",
        image = "https://developer.android.com/static/guide/practices/ui_guidelines/images/article_icon_adaptive.gif"
    ),
    Chat(
        chatId = "id4",
        text = "hey, how are you",
        timestamp = currentTimestamp+300000,
        username = "ari_archit",
        image = "https://developer.android.com/static/guide/practices/ui_guidelines/images/article_icon_adaptive.gif"
    ),
    Chat(
        chatId = "id1",
        text = "hey, how are you",
//        set a dummy timestamp : Timestamp
        timestamp = currentTimestamp,
        username = "ari_archit",
        image = "https://developer.android.com/static/guide/practices/ui_guidelines/images/article_icon_adaptive.gif"
    ),
    Chat(
        chatId = "id2",
        text = "hey, how are you",
        timestamp = currentTimestamp+100000,
        username = "ari_archit",
        image = "https://developer.android.com/static/guide/practices/ui_guidelines/images/article_icon_adaptive.gif"
    ),
    Chat(
        chatId = "id3",
        text = "hey, how are you",
        timestamp = currentTimestamp+200000,
        username = "ari_archit",
        image = "https://developer.android.com/static/guide/practices/ui_guidelines/images/article_icon_adaptive.gif"
    )
)