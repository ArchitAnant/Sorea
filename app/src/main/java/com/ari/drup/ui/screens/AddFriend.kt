package com.ari.drup.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.baseDark
import com.ari.drup.data.User
import com.ari.drup.mainLight
import com.ari.drup.regular_font
import com.ari.drup.ui.components.AvatarSelector
import com.ari.drup.ui.components.DatePickerModal
import com.ari.drup.ui.components.convertMillisToDate
import com.ari.drup.ui.screens.onboarding.ForwardButton
import com.ari.drup.ui.screens.onboarding.GenderSelection
import com.ari.drup.ui.screens.onboarding.OnboardTextField
import com.ari.drup.viewmodels.ProfilePageViewModel
import kotlinx.coroutines.launch

@Composable
fun AddFriendScreen(profilePageViewModel: ProfilePageViewModel, modifier: Modifier = Modifier, onAddFriendClick:()-> Unit) {
    var username by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(start = 40.dp, top = 10.dp) // your custom padding
            ) {
                Text(
                    text = "Add Friend",
                    fontFamily = regular_font,
                    color = mainLight,
                    fontSize = 30.sp,
                    modifier = modifier.padding(top = 30.dp)
                )
            }
        },
        bottomBar = {
            Button(onClick = {
                if (username == profilePageViewModel.user!!.username){
                    scope.launch {
                        snackbarHostState.showSnackbar("You can't add yourself as a friend")
                    }
                }
                else{
                    scope.launch {
                        profilePageViewModel.refreshUsernames()
                        val validUsernames = profilePageViewModel.getRegisteredUsernames()
                        if (username in validUsernames){
                            profilePageViewModel.pushRequest(username)
                        }
                    }
                onAddFriendClick()
                }

            },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(horizontal = 40.dp)
                    .padding(bottom = 20.dp)
//                    .padding(horizontal = 40.dp, bottom = 20.dp),
                        ,
                colors = ButtonDefaults.buttonColors(containerColor = baseDark)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Send Request",
                        color = mainLight,
                        fontFamily = regular_font,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "",
                        modifier = Modifier.size(20.dp),
                        tint = mainLight
                    )

                }
            }
        },
        containerColor = Color.Black,
    ) { innerPadding ->
        Box(modifier= Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            OnboardTextField(
                        username,
                        {
                           username = it
                        },
                        "Enter the username of friend"
                    )
                }


    }
}

@Preview
@Composable
private fun AddFriendPrev() {
//    AddFriendScreen(ProfilePageViewModel()){
//
//    }
}