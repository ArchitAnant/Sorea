package com.ari.drup.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ari.drup.R
import com.ari.drup.baseDark
import com.ari.drup.data.FirebaseManager
import com.ari.drup.mainAccent
import com.ari.drup.mainLight
import com.ari.drup.notification.scheduleNotificationAt
import com.ari.drup.regular_font
import com.ari.drup.semibold_font
import com.ari.drup.ui.Screen
import com.ari.drup.ui.components.ChatSoreaButton
import com.ari.drup.ui.components.avatars
import com.ari.drup.ui.theme.LocalAppDimens
import com.ari.drup.viewmodels.HomeScreenViewModel
import com.ari.drup.viewmodels.MainChatViewModel
import com.ari.drup.viewmodels.OnboardingViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    context: Context,
    onboardingViewModel: OnboardingViewModel,
    homeScreenViewModel: HomeScreenViewModel,
    mainChatViewModel: MainChatViewModel,
    navHostController : NavHostController,
    modifier: Modifier = Modifier
) {
    val dimens = LocalAppDimens.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val today = homeScreenViewModel.today.collectAsState().value

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = modifier

                    .padding(dimens.paddingMedium)
            ) {
                    Image(
                        painterResource(avatars[onboardingViewModel.currUser!!.avatar]),
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = "Hello, ${onboardingViewModel.currUser!!.name}!",
                        color = mainLight,
                        fontFamily = semibold_font,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        fontSize = 18.sp
                    )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    navHostController.navigate(Screen.notification.route)
                }) {
                    Icon(Icons.Default.Notifications,
                        contentDescription = "")
                }
            }
        },
        containerColor = Color.Black
    ) {  innerPadding->
        Box(modifier = Modifier
            .padding(top=dimens.paddingMedium)
            .fillMaxSize()
            ,
            contentAlignment = Alignment.Center
        ){
            Column(
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StreakPrev(
                        homeScreenViewModel.streakCount.collectAsState().value,
                        "Streak",
                        R.drawable.fire,
                        Modifier.weight(1f)
                    ){
                        scope.launch {
                            snackbarHostState.showSnackbar("Chatting everyday with sorea increases your streak!")
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    StreakPrev(0, "Badges", R.drawable.badge,Modifier.weight(1f)){
                        scope.launch {
                            snackbarHostState.showSnackbar("Badges are awarded by Sorea's AI.")
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(40.dp))
                        .background(
                            mainAccent.copy(0.4f)
                        )
                        .padding(15.dp),
                ) {
                    Text(
                        text = "Feel like discussing \nsomething!",
                        color = Color.White.copy(0.8f),
                        fontFamily = regular_font,
                        modifier = Modifier.padding(end = 20.dp, start =15.dp),
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.weight(1f))
                    ChatSoreaButton {
                        navHostController.navigate(Screen.mainChatScreen.route)
                        if (!today.isNullOrEmpty()) {
                            mainChatViewModel.selectChat(today)
                            mainChatViewModel.fetchChats()
                        }
                        else {
                            mainChatViewModel.selectChat(null)
                            mainChatViewModel.clearChats()
                        }
                        val twoHoursLater = System.currentTimeMillis() + 2 * 60 * 60 * 1000
                        scheduleNotificationAt(context, Timestamp(Date(twoHoursLater)))

                    }
                }

            }

        }
    }
}
@Composable
fun StreakPrev(
    itemCount: Int,
    itemDesc: String,
    sideImage: Int,
    modifier: Modifier = Modifier,
    onTap:()-> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(baseDark.copy(0.6f))
                .clickable{
                    onTap()
                }
                .fillMaxWidth()
        ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(20.dp)) {
                    Column {
                        Text(
                            text = itemCount.toString(),
                            color = mainLight,
                            fontFamily = semibold_font,
                            fontSize = 24.sp
                        )
                        Text(
                            text = itemDesc,
                            color = mainLight.copy(0.8f),
                            fontFamily = regular_font,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        painterResource(sideImage),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }

        }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun HomeScreenPrev() {
//    HomeScreen()
}