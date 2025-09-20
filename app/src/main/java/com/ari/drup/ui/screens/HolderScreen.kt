package com.ari.drup.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ari.drup.ui.Screen
import com.ari.drup.ui.components.BottomNavigation
import com.ari.drup.viewmodels.GroupChatViewModel
import com.ari.drup.viewmodels.OnboardingViewModel
import kotlinx.coroutines.launch

@Composable
fun HolderScreen(
    onboardingViewModel: OnboardingViewModel,
    chatViewModel : GroupChatViewModel,
    navHostController: NavHostController,

    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Home") }
    var chatId by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (selectedTab=="Home") {
                Row(modifier = modifier.fillMaxWidth().padding(20.dp)) {
                    Spacer(modifier = Modifier.weight(2f))
                    IconButton(onClick = {
                        navHostController.navigate(Screen.mainChatScreen.route)
                    }) {
                        Icon(
                            Icons.Outlined.BubbleChart,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }

                }
            }
        },
        bottomBar = {
            Box(
                modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                    BottomNavigation(
                        currentSelection = when (selectedTab) {
                            "Home" -> 0
                            "Community" -> 1
                            "Profile" -> 2
                            else -> 0
                        },
                        onBottomClick = { selectedTab = it }
                    )
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 20.dp).fillMaxSize()
        ) {
            // Content based on selected tab
            when (selectedTab) {
                "Home" -> HomeScreen(modifier)
                "Community" -> CommunityPage({id,title ->
                    chatId = id
                    chatViewModel.chatTitle.value = title
                    Log.d("title",chatViewModel.chatTitle.value)
                    navHostController.navigate(Screen.chatScreen.route)
                },
                    {
                        scope.launch {
                            snackbarHostState.showSnackbar("Feature in development!")
                        }
                    },modifier)
                "Profile" -> ProfileScreen(onboardingViewModel.currUser!!){
                    onboardingViewModel.currUser = null
                    onboardingViewModel.currentUserEmail = null
                    navHostController.navigate(Screen.signin.route)
                }
            }
        }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun HolderScreenPrev() {
    HolderScreen(OnboardingViewModel(rememberNavController()),GroupChatViewModel(), rememberNavController())
}