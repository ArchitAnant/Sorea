package com.ari.drup.ui.screens.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.regular_font
import com.ari.drup.viewmodels.OnboardingViewModel
import com.ari.drup.viewmodels.regState

@Composable
fun WaitingScreen(
    vm: OnboardingViewModel,
    modifier: Modifier = Modifier
) {
    val currRegState = vm.successRegistered.collectAsState().value

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (currRegState.value) {
            regState.waiting -> {
                CircularProgressIndicator(color = Color.White)
            }
            regState.success -> {
                Text(
                    text = "Welcome",
                    fontFamily = regular_font,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
            regState.failed -> {
                Text(
                    text = "Failed",
                    fontFamily = regular_font,
                    fontSize = 24.sp,
                    color = Color.Red
                )
            }
            null -> { /* do nothing or show empty */ }
        }
    }
}
