package com.ari.drup.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.ari.drup.regular_font
import com.ari.drup.semibold_font
import com.ari.drup.ui.components.ChatBox
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun MainChatScreen(
    modifier: Modifier = Modifier,
    stop: Boolean = false // trigger this when chat starts
) {
    var message by remember { mutableStateOf("") }

    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Fake history list
    val historyItems = listOf<String>() // replace with VM state

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
    }

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
                    LazyColumn {
                        items(historyItems) { item ->
                            Text(
                                text = item,
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    ) {
        // Main Screen Content
        Scaffold(
            topBar = {
                Row(modifier = modifier.fillMaxWidth().padding(20.dp)) {
                    Icon(
                        Icons.Outlined.ClearAll,
                        contentDescription = "",
                        tint = Color.White.copy(0.7f),
                        modifier = Modifier
                            .size(31.dp)
                            .clickable {
                                scope.launch { drawerState.open() }
                            }
                    )
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
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(10.dp),
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
                                // handle send
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(50.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
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
        ) { innerPadding ->
            Box(modifier = Modifier.background(Color.Black).padding(innerPadding)) {
                Column(
                    modifier = modifier.fillMaxSize(),
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
        }
    }
}



@Preview
@Composable
private fun MainChatScreenPrev() {
    MainChatScreen(modifier = Modifier.background(color = Color.Black))
}