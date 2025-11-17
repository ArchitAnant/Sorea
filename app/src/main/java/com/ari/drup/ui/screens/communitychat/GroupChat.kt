package com.ari.drup.ui.screens.communitychat

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.baseDark
import com.ari.drup.darkAccent
import com.ari.drup.data.community.Chat
import com.ari.drup.mainAccent
import com.ari.drup.mainLight
import com.ari.drup.regular_font
import com.ari.drup.semibold_font
import com.ari.drup.ui.components.ChatBox
import com.ari.drup.ui.components.JoinCommunityDrawer
import com.ari.drup.ui.components.avatars
import com.ari.drup.ui.components.backColor
import com.ari.drup.viewmodels.GroupChatViewModel
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        scope.launch {
            groupChatViewModel.fillCurrUsers(chatTitle)
        }
    }
    Scaffold(
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .background(Color.Black).clickable{

                }.fillMaxWidth().padding(top = 20.dp, start = 30.dp, bottom = 20.dp)
            ) {
                Text(
                    text = chatTitle,
                    color = Color.White,
                    fontFamily = regular_font,
                    fontSize = 30.sp,
                    modifier = modifier

                )
                Icon(Icons.Default.ChevronRight, contentDescription = "", tint = Color.White, modifier = Modifier.padding(start = 10.dp).size(25.dp))

            }
        },
        bottomBar = {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    ,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var showBottomSheet by remember { mutableStateOf(false) }

                if (groupChatViewModel.checkUserInCommunity(
                        groupChatViewModel.onboardingViewModel.currentUserEmail!!
                    )
                ) {
                    ChatBox(
                        message,
                        onMessageChange = { groupChatViewModel.setChatBox(it) }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (message.isNotBlank()) {
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
                } else {


                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (showBottomSheet){
                            JoinCommunityDrawer(
                                communityName = chatTitle,
                                uname = groupChatViewModel.onboardingViewModel.currUser!!.username,
                                onDismissRequest = { showBottomSheet = false },
                                joinCommunityClick = {newUname->
                                    groupChatViewModel.addUserToCommunity(chatTitle,groupChatViewModel.onboardingViewModel.currentUserEmail!!,newUname)
                                }
                            )
                    }

                        Button(
                            onClick = { showBottomSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = baseDark)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "",
                                    modifier = Modifier.size(20.dp),
                                    tint = mainLight
                                )
                                Text(
                                    text = "Join $chatTitle",
                                    color = mainLight,
                                    fontFamily = regular_font,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(start = 5.dp)
                                )
                            }
                        }

                        Text(
                            text = "Join the community to send texts!",
                            fontFamily = regular_font,
                            fontSize = 15.sp,
                            color = mainLight,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
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
            if (currentChats.messages.isEmpty() && chatStateLoading){
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            else if (currentChats.messages.isEmpty()){
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


                LaunchedEffect(currentChats.messages.size) {
                    // Scroll to last message whenever a new message appears
                    if (currentChats.messages.isNotEmpty()) {
                        listState.scrollToItem(currentChats.messages.size - 1)
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = innerPadding,
                ) {
                    items(currentChats.messages.reversed()) { chat ->
                        var uname by remember { mutableStateOf<String?>(null) }

                        LaunchedEffect(chat.email) {
                            uname = groupChatViewModel.getUsername(chat.email)
                        }

                        ChatItem(
                            uname ?: "...",
                            chat = chat,
                            own = chat.email == groupChatViewModel.onboardingViewModel.currentUserEmail
                        )
                    }
                }
            }

        }
    }

}

@Composable
fun ChatItem(
    uname : String?,
    chat : Chat,
    own : Boolean,
    modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (own) Arrangement.End else Arrangement.Start
    ) {

        Spacer(modifier = Modifier.width(10.dp))
        Column(
            Modifier.padding(2.dp)
        ) {
            val zonedDateTime = ZonedDateTime.parse(chat.timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            val timeString = zonedDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.getDefault()))

            Text(
                text=if (own) "You" else uname?:"Unknown",
                color = Color.White.copy(0.5f),
                fontFamily = semibold_font,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 10.dp)
                    .align (if(own) Alignment.End else Alignment.Start)
            )
            Row(verticalAlignment = Alignment.Bottom) {
                if (!own) {
                    Image(
                        painterResource(avatars.random()),
                        contentDescription = null,
                        modifier = Modifier.clip(CircleShape).size(30.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Box(
                    modifier = Modifier
                        .widthIn(max = 320.dp) // LIMIT width instead of using weight
                        .clip(
                            RoundedCornerShape(
                                topEnd = 20.dp,
                                topStart = 20.dp,
                                bottomStart = if (own) 20.dp else 0.dp,
                                bottomEnd = if (own) 0.dp else 20.dp
                            )
                        )
                        .background(if (!own) Color.White.copy(0.3f) else baseDark)
                        .padding(vertical = 13.dp, horizontal = 13.dp)
                ) {
                    Text(
                        text = chat.text,
                        color = Color.White,
                        fontFamily = regular_font,
                        fontSize = if (chat.text.contains("[RETRACTED]")) 12.sp else 16.sp,
                        fontStyle = if (chat.text.contains("[RETRACTED]")) FontStyle.Italic else FontStyle.Normal
                    )

                }

                if (own) {
                    Spacer(Modifier.width(10.dp))
                    Image(
                        painterResource(avatars.random()),
                        contentDescription = null,
                        modifier = Modifier.clip(CircleShape).size(30.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Text(
                text= timeString,
                color = Color.White.copy(0.7f),
                fontFamily = regular_font,
                fontSize = 10.sp,
                modifier = Modifier.align(if(own) Alignment.Start else Alignment.End)
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
