package com.ari.drup.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.R
import com.ari.drup.regular_font

@Composable
fun SignInScreen(onSignInClick: () -> Unit,modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize().background(Color.Black)) {
        Text(
            text = "Sorea",
            color = Color.White.copy(0.5f),
            fontFamily = regular_font,
            fontSize = 30.sp,
            modifier = modifier
        )
        Spacer(modifier= Modifier.height(20.dp))
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
}

@Preview
@Composable
private fun SignInScreenPrev() {
    SignInScreen({})
}