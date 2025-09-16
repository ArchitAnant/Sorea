package com.ari.drup.ui.screens

import android.annotation.SuppressLint
import android.os.Message
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ari.drup.data.Chat
import com.ari.drup.regular_font
import com.ari.drup.viewmodels.GroupChatViewModel
import com.ari.drup.viewmodels.dummyChatList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(chatId: String,
               chatTitle : String,
               groupChatViewModel: GroupChatViewModel
               ,modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf("") }
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
                    message,
                    onMessageChange = { message = it }
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            groupChatViewModel.setChatBox(message)
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
            LazyColumn(modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding) {
                items(dummyChatList){chat->
                    ChatItem(chat)
                }
            }

        }
    }

}

@Composable
fun ChatItem(chat : Chat,modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = chat.image, // URL of your image
            contentDescription = "Sample image",
            modifier = Modifier.size(50.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
        ) {
            Text(
                text=chat.username,
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
                text= SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(chat.timestamp)),
                color = Color.White.copy(0.5f),
                fontFamily = regular_font,
                fontSize = 12.sp,
//                modifier = Modifier.padding(start = 10.dp)
                modifier = Modifier.align(Alignment.End)
            )
        }

    }
}

@Composable
fun ChatBox(message: String,
            onMessageChange: (String) -> Unit,
            modifier: Modifier = Modifier
) {

    OutlinedTextField(
        value = message,
        onValueChange = { onMessageChange(it) },
        placeholder = {
            Text(
                text="Type your message here",
                color = Color.White.copy(0.3f),
                fontFamily = regular_font,
                fontSize = 18.sp
                )
                      },
        modifier = modifier.fillMaxWidth(0.85f),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.Transparent,
            focusedContainerColor = Color.White.copy(0.3f),
            unfocusedContainerColor = Color.White.copy(0.3f),
            focusedLabelColor = Color.White,
            focusedIndicatorColor = Color.White,
            unfocusedIndicatorColor = Color.Transparent,
            unfocusedLeadingIconColor = Color.White,
            focusedLeadingIconColor = Color.White,

            ),
        shape = RoundedCornerShape(40.dp),
        textStyle = TextStyle(
            fontFamily = regular_font,
            fontSize = 18.sp
        )
    )
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ChatScreenPrev() {
//    val vm = GroupChatViewModel()
//    ChatScreen(chatId = "", chatTitle = "Exam Pressure",groupChatViewModel = vm)
//    ChatItem(dummyChatList[0])
}
