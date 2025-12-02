package com.ari.drup.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.ari.drup.data.Notifications
import com.ari.drup.mainLight
import com.ari.drup.regular_font
import com.ari.drup.ui.components.FriendRequest
import com.ari.drup.viewmodels.NotificationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NotificationScreen(notificationViewModel: NotificationViewModel,
                       username : String,
                       currentEmail: String,
                       modifier: Modifier = Modifier
) {
    val requests by notificationViewModel.requests.observeAsState(emptyList())

    LaunchedEffect(username) {
        notificationViewModel.startListening(username)
    }

    val scope = rememberCoroutineScope ()

    Column(modifier = modifier
        .fillMaxSize()
        .padding(start = 10.dp, top = 40.dp, end = 10.dp),
        ) {

        Text(
            text = "Notifications",
            color = mainLight,
            fontFamily = regular_font,
            fontSize = 40.sp,
            modifier=Modifier.padding(start = 30.dp)
        )
        Spacer(Modifier.height(50.dp))
        if (requests.isEmpty()){
            Text(
                text = "No new Notifications",
                color = mainLight.copy(0.5f),
                fontFamily = regular_font,
                fontSize = 20.sp,
                modifier= Modifier.fillMaxSize().padding(start = 30.dp)
            )
        }
        else {
            LazyColumn {
                items(
                    items = requests,
                    key = { it.username }
                ) { notif ->
                    var visible by remember { mutableStateOf(true) }

                    // Use AnimatedVisibility for exit animation
                    AnimatedVisibility(
                        visible = visible,
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        FriendRequest(notif.username ,{ accepted ->
                            if (!accepted) {
                                visible = false

                            }
                            scope.launch {
                                notificationViewModel.removeRequest(username,currentEmail,notif,!accepted)
                            }
                        })
                    }

                    // Handle removal *outside* the AnimatedVisibility block
                    if (!visible) {
                        LaunchedEffect(Unit) {
                            delay(300) // match animation duration

                        }
                    }
                }
            }
        }

    }
}

@Preview
@Composable
private fun NotificationPrevScreen() {

//    NotificationScreen(
//        mutableListOf()
//    )
}
