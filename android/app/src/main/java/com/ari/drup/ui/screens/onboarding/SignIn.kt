package com.ari.drup.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.R
import com.ari.drup.darkAccent
import com.ari.drup.mainAccent
import com.ari.drup.mainLight
import com.ari.drup.regular_font
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun SignInScreen(systemUiController: SystemUiController, onSignInClick: () -> Unit, modifier: Modifier = Modifier) {
    val darkTheme = isSystemInDarkTheme()
    LaunchedEffect(Unit) {
        val color = mainLight
        systemUiController.setStatusBarColor(
            color = color,
            darkIcons = !darkTheme
        )
        systemUiController.setNavigationBarColor(
            color = Color.Black,
            darkIcons = !darkTheme
        )
    }
    Scaffold (
        containerColor = Color.Black,
        bottomBar = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .padding(20.dp)
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .fillMaxWidth()
                    .background(darkAccent.copy(0.6f))
                    .padding(vertical = 25.dp)
            ) {
                Text(
                    text = "Lets begin our journey together!",
                    color = mainAccent,
                    fontFamily = regular_font,
                    fontSize = 18.sp,
                    modifier = modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = {
                        onSignInClick()
                    },
                    contentPadding = PaddingValues(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Image(
                        painterResource(R.drawable.android_dark_rd_si),
                        contentDescription = null
                    )
                }

            }
        },

    ){innerPadding->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(R.drawable.temp),
                contentDescription = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.FillWidth
            )

            Text(
                text = "Sorea",
                color = mainLight.copy(0.7f),
                fontFamily = regular_font,
                fontSize = 35.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp) // keeps text centered
            )
        }

    }
}

@Preview
@Composable
private fun SignInScreenPrev() {
    SignInScreen(rememberSystemUiController(),{})
}