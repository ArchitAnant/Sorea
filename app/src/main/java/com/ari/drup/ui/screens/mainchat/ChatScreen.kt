package com.ari.drup.ui.screens.mainchat

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.data.mainchat.ApiState
import com.ari.drup.data.mainchat.MessDao
import com.ari.drup.regular_font
import com.ari.drup.semibold_font
import com.ari.drup.ui.components.ChatBox
import com.ari.drup.ui.components.ChatPrev
import com.ari.drup.viewmodels.MainChatViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatDate(input: String): String {
    return try {
        val cleanInput = input.removePrefix("conv_") // extract yyyyMMdd
        val parser = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = parser.parse(cleanInput)
        formatter.format(date!!)
    } catch (e: Exception) {
        e.printStackTrace()
        input // fallback to original if parsing fails
    }
}

enum class ResponseState{
    waiting,
    success,
    failed,
    idle
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun MainChatScreen(
    mainChatViewModel: MainChatViewModel,
    modifier: Modifier = Modifier,
    stop: Boolean = false // trigger this when chat starts
) {
    var message by remember { mutableStateOf("") }

    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Fake history list
    val historyItems = mainChatViewModel.chatList.collectAsState().value // replace with VM state
    val chats = mainChatViewModel.chats.collectAsState().value
    val responseState = mainChatViewModel.chatState.collectAsState().value
    var uiResponseState by remember { mutableStateOf(ResponseState.idle) }
    val listState = rememberLazyListState()

    // Dynamic messages for waiting state
    val messages = listOf(
        "Take a deep breath 🌿",
        "You are not alone 💙",
        "This space is safe 🤍",
        "It’s okay to rest 🌸",
        "One step at a time 🌟"
    )
    var currentIndex by remember { mutableIntStateOf(0) }

    // Animate through texts while not stopped
    LaunchedEffect(stop) {
        if (!stop) {
            while (true) {
                delay(2000L)
                currentIndex = (messages.indices).random()
            }
        }
        listState.animateScrollToItem(listState.layoutInfo.totalItemsCount)
    }
    val snackbarHostState = remember { SnackbarHostState() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color.DarkGray
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "History",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = semibold_font,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                scope.launch { drawerState.close() }
                            }
                    )
                }
                Divider(color = Color.White.copy(0.2f))

                if (historyItems.isEmpty()) {
                    Text(
                        text = "No history",
                        color = Color.White.copy(0.7f),
                        fontSize = 14.sp,
                        fontFamily = regular_font,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn (reverseLayout = true){
                        items(historyItems) { item ->
                            Text(
                                text = formatDate(item),
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clickable{
                                        mainChatViewModel.selectChat(item)
                                        mainChatViewModel.fetchChats()
                                    }
                            )
                        }
                    }
                }
            }
        }
    ) {
        // Main Screen Content
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                Row(modifier = modifier.fillMaxWidth().padding(20.dp)) {
                    Icon(
                        Icons.Outlined.ClearAll,
                        contentDescription = "",
                        tint = Color.White.copy(0.7f),
                        modifier = Modifier
                            .size(31.dp)
                            .clickable {
                                scope.launch {
                                    drawerState.open()
                                    mainChatViewModel.fetchChatNames()
                                }
                            }
                    )
                    if (chats.isNotEmpty()){
                        Text(
                            text = formatDate(mainChatViewModel.selectedChat.collectAsState().value!!),
                            color = Color.White.copy(0.7f),
                            fontFamily = regular_font,
                            modifier = Modifier.padding(start = 10.dp),
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.weight(2f))
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = "",
                        tint = Color.White.copy(0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            bottomBar = {
                Column(modifier
                    .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom) {
                    when(uiResponseState){
                        ResponseState.idle -> {

                        }
                        ResponseState.waiting -> {
                            Text(
                                text = "Waiting For Response...",
                                color = Color.White.copy(0.7f),
                                fontSize = 14.sp,
                                fontFamily = regular_font,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        ResponseState.failed -> {
                            Text(
                                text = "Failed Fetching Response try again.",
                                color = Color.Red.copy(0.7f),
                                fontSize = 14.sp,
                                fontFamily = regular_font,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        ResponseState.success -> {

                        }
                    }
                    Row(
                        modifier = Modifier
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
                                if (mainChatViewModel.isToday(mainChatViewModel.getSelectedChat()!!)) {
                                    if (message.isNotBlank()) {
                                        mainChatViewModel.sendMessage(message)
                                        message = ""
                                        uiResponseState = ResponseState.waiting
                                    }
                                }
                                else if(message.isNotBlank()){
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Switch back to today's chat to continue")
                                    }
                                }
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(50.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (uiResponseState!=ResponseState.waiting) Color.White else Color.White.copy(0.3f)),
                            enabled = uiResponseState!=ResponseState.waiting
                        ) {
                            Icon(
                                Icons.Filled.ArrowUpward,
                                contentDescription = "",
                                tint = Color.Black,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                }

            }
        ) { innerPadding ->
            Box(modifier = Modifier.background(Color.Black)) {
                if (chats.isEmpty()) {
                    Column(
                        modifier = modifier.fillMaxSize().padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        AnimatedContent(
                            targetState = if (stop) "Conversation started ✨" else messages[currentIndex],
                            transitionSpec = {
                                fadeIn(tween(700)) with fadeOut(tween(700))
                            }
                        ) { text ->
                            Text(
                                text = text,
                                color = Color.White,
                                fontFamily = regular_font,
                                modifier = Modifier.padding(20.dp),
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else {
                    Column(modifier= Modifier.align(Alignment.TopEnd)) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = innerPadding,
                            reverseLayout = true
                        ) {
                            items(chats) { item ->
                                ChatPrev(item)
                            }

                        }
                        when (responseState) {
                            is ApiState.Waiting -> {
                                Log.d("Repo","Waitign for resoinse")
                            }
                            is ApiState.Success -> {
                                uiResponseState = ResponseState.success
                                val response = responseState.data
                                val tempMess = MessDao(
                                    user = message,
                                    model = response.message,
                                    timestamp = Timestamp.now()
                                )
                                mainChatViewModel.appendMess(tempMess)
                                uiResponseState = ResponseState.idle

                            }
                            is ApiState.Failed -> {
                                uiResponseState = ResponseState.failed
                            }
                        }

                    }
                }

            }
        }
    }
}
fun parseToFirebaseTimestamp(dateString: String): Timestamp {
    val formatter = DateTimeFormatter.ofPattern(
        "MMMM d, yyyy 'at' h:mm:ss a O",
        Locale.ENGLISH
    )
    val zonedDateTime = ZonedDateTime.parse(dateString, formatter)
    val instant = zonedDateTime.toInstant()
    return Timestamp(instant.epochSecond, instant.nano)
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun MainChatScreenPrev() {
//    MainChatScreen(MainChatViewModel(FirebaseManager()),ONb,modifier = Modifier.background(color = Color.Black))
}