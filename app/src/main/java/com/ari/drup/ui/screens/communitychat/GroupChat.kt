package com.ari.drup.ui.screens.communitychat

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.data.community.Chat
import com.ari.drup.regular_font
import com.ari.drup.ui.components.ChatBox
import com.ari.drup.viewmodels.GroupChatViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ChatScreen(chatId: String,
               chatTitle : String,
               groupChatViewModel: GroupChatViewModel
               ,modifier: Modifier = Modifier
) {
    val currentChats = groupChatViewModel.messages.collectAsState().value
    var message = groupChatViewModel.chatBox.collectAsState().value
    val chatStateLoading = groupChatViewModel.chatLoading.collectAsState().value
    Scaffold(
        topBar = {
            Text(
                text = chatTitle,
                color = Color.White,
                fontFamily = regular_font,
                fontSize = 30.sp,
                modifier = modifier
                    .padding(top = 30.dp, start = 30.dp)
                    .fillMaxWidth()
            )
        },
        bottomBar = {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    ,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChatBox(
                    message.value,
                    onMessageChange = { groupChatViewModel.setChatBox(it) }
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        if (message.value.isNotBlank()) {
                            groupChatViewModel.sendMessage()
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "",
                        tint = Color.Black,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        // Your scrollable chat messages go here
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)

        ) {
            if (currentChats.messages.isEmpty() && chatStateLoading.value){
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            else if (currentChats.messages.isEmpty() && !chatStateLoading.value){
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Be the first one!",
                        color = Color.White,
                        fontFamily = regular_font,
                        fontSize = 20.sp,
                        modifier = modifier
                            .padding(top = 30.dp, start = 30.dp)
                            .fillMaxWidth()

                    )
                }
            }
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding,
                    reverseLayout = true
                ) {
                    items(currentChats.messages) { chat ->
                        ChatItem(chat)
                    }
                }
            }

        }
    }

}

@Composable
fun ChatItem(chat : Chat,modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
//        AsyncImage(
//            model = chat.image, // URL of your image
//            contentDescription = "Sample image",
//            modifier = Modifier.size(50.dp).clip(CircleShape),
//            contentScale = ContentScale.Crop
//        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            Modifier.padding(2.dp)
        ) {
            val zonedDateTime = ZonedDateTime.parse(chat.timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

// format to HH:mm
            val timeString = zonedDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.getDefault()))

            Text(
                text=chat.userId,
                color = Color.White.copy(0.5f),
                fontFamily = regular_font,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 10.dp)
            )
            Text(
                text=chat.text,
                color = Color.White,
                fontFamily = regular_font,
                fontSize = 16.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(0.3f))
                    .padding(vertical = 11.dp, horizontal = 10.dp)
            )
            Text(
                text= timeString,
                color = Color.White.copy(0.5f),
                fontFamily = regular_font,
                fontSize = 12.sp,
//                modifier = Modifier.padding(start = 10.dp)
                modifier = Modifier.align(Alignment.End)
            )
        }

    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ChatScreenPrev() {
//    val vm = GroupChatViewModel()
//    ChatScreen(chatId = "", chatTitle = "Exam Pressure",groupChatViewModel = vm)
//    ChatItem(dummyChatList[0])
}
