package com.ari.drup.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.ari.drup.ui.Screen
import com.ari.drup.ui.components.BottomNavigation
import com.ari.drup.ui.screens.communitychat.CommunityPage
import com.ari.drup.viewmodels.GroupChatViewModel
import com.ari.drup.viewmodels.HomeScreenViewModel
import com.ari.drup.viewmodels.MainChatViewModel
import com.ari.drup.viewmodels.OnboardingViewModel
import com.ari.drup.viewmodels.regState
import kotlinx.coroutines.launch

@Composable
fun HolderScreen(
    context : Context,
    onboardingViewModel: OnboardingViewModel,
    chatViewModel : GroupChatViewModel,
    mainChatViewModel: MainChatViewModel,
    homeScreenViewModel: HomeScreenViewModel,
    navHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Home") }
    var chatId by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ){ isGranted ->
        if (isGranted) {
            Log.d("Permission", "Notification permission granted")
        } else {
            Log.d("Permission", "Notification permission denied")
        }
    }
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            permissionLauncher.launch(Manifest.permission.SCHEDULE_EXACT_ALARM)
        }
        homeScreenViewModel.initializeChatScreen{today->
            mainChatViewModel.selectChat(today)
        }
        mainChatViewModel.observeChat()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                "Home" -> HomeScreen(onboardingViewModel,homeScreenViewModel,mainChatViewModel,navHostController,modifier)
                "Community" -> CommunityPage(
                    chatViewModel, { id, title ->
                    chatId = id
                    chatViewModel.chatTitle.value = title
                    Log.d("title", chatViewModel.chatTitle.value)
                    navHostController.navigate(Screen.chatScreen.route)
                },
                    {
                        scope.launch {
                            snackbarHostState.showSnackbar("Feature in development!")
                        }
                    }, modifier
                )
                "Profile" -> ProfileScreen(onboardingViewModel.currUser!!, modifier,onPushNotification = {
//                    scheduleDailyNotification(context)
                }){
                    onboardingViewModel.currUser = null
                    onboardingViewModel.currentUserEmail = null
                    onboardingViewModel.changeRegisteredState(regState.waiting)
                    navHostController.navigate(Screen.signin.route)
                    scope.launch {
                        UserCache.clearUser(context)
                    }

                }
            }
        }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun HolderScreenPrev() {
//    HolderScreen(Acti,OnboardingViewModel(rememberNavController()),GroupChatViewModel(), rememberNavController())
}