package com.ari.drup.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ari.drup.data.Chat
import kotlinx.coroutines.flow.StateFlow

class GroupChatViewModel : ViewModel(){
    val groupChatMessages = mutableListOf<String>()
    private val _chatBox = mutableStateOf("")
    var chatTitle = mutableStateOf("")
//    create a state flow list


    fun setChatBox(message: String){
        _chatBox.value = message
    }


//
//    fun sendMessage(){
//        groupChatMessages.add(_chatBox.value)
//        _chatBox.value = ""
//    }
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