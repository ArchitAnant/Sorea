package com.ari.drup.ui.screens.onboarding

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.regular_font
import com.ari.drup.semibold_font
import com.ari.drup.ui.Screen
import com.ari.drup.viewmodels.OnboardingViewModel
import com.ari.drup.viewmodels.regState
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WaitingScreen(
    loadNextScreen: (Boolean) -> Unit,
    vm: OnboardingViewModel,
    modifier: Modifier = Modifier
) {
    val currRegState = vm.successRegistered.collectAsState().value
    val context = LocalContext.current

    // List of fun loading texts
    val loadingTexts = listOf(
        "Almost there...",
        "Setting things up...",
        "Getting everything ready...",
        "Loading magic..."
    )

    // Randomly pick a text every 1.5 seconds
    var currentTextIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(currRegState) {
        while (currRegState.value == regState.waiting) {
            currentTextIndex = (0 until loadingTexts.size).random()
            delay(1500L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (currRegState.value) {
            regState.waiting -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedContent(
                        targetState = loadingTexts[currentTextIndex],
                        transitionSpec = {
                            fadeIn(tween(500)) with fadeOut(tween(500))
                        }
                    ) { text ->
                        Text(
                            text = text,
                            color = Color.White.copy(0.7f),
                            fontFamily = semibold_font,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            regState.success -> {
                loadNextScreen(true)
            }
            regState.failed -> {
                Toast.makeText(context, "Registration Failed", Toast.LENGTH_SHORT).show()
                loadNextScreen(false)
            }
            null -> { /* do nothing */ }
        }
    }
}

