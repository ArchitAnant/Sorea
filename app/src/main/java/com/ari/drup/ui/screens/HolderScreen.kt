package com.ari.drup.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ari.drup.ui.Screen
import com.ari.drup.ui.components.BottomNavigation
import com.ari.drup.viewmodels.GroupChatViewModel

@Composable
fun HolderScreen(
    chatViewModel : GroupChatViewModel,
    navHostController: NavHostController,

    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Home") }
    var chatId by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            Box(
                modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                BottomNavigation(
                    currentSelection = when (selectedTab) {
                        "Home" -> 0
                        "Chat" -> 1
                        "Community" -> 2
                        "Profile" -> 3
                        else -> 0
                    },
                    onBottomClick = { selectedTab = it }
                )
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            // Content based on selected tab
            when (selectedTab) {
                "Home" -> HomeScreen()
                "Chat" -> MainChatScreen()
                "Community" -> CommunityPage({id,title ->
                    chatId = id
                    chatViewModel.chatTitle.value = title
                    Log.d("title",chatViewModel.chatTitle.value)
                    navHostController.navigate(Screen.chatScreen.route)
                },
                    modifier)
                "Profile" -> ProfileScreen()
            }
        }
    }
}


@Preview
@Composable
private fun HolderScreenPrev() {
    HolderScreen(GroupChatViewModel(), rememberNavController())
}