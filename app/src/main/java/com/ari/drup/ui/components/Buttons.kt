package com.ari.drup.ui.components

import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.baseDark
import com.ari.drup.darkAccent
import com.ari.drup.mainAccent
import com.ari.drup.mainLight
import com.ari.drup.regular_font
import kotlinx.coroutines.launch

@Composable
fun CreateChannelButton(onCreateClick: () -> Unit = {}) {
    Button(onClick = onCreateClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = baseDark)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Add,
                contentDescription = "",
                modifier = Modifier.size(20.dp),
                tint = mainLight
            )
            Text(
                text = "Create Community",
                color = mainLight,
                fontFamily = regular_font,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 5.dp)
            )

        }
    }
}

@Composable
fun ChatSoreaButton(onButtonClick: () -> Unit) {
    // Define animatable values (start from initial positions)
    val centerX = remember { Animatable(0f) }
    val centerY = remember { Animatable(0f) }

    // Launch one-time animation on composition
    LaunchedEffect(Unit) {
        launch {
            centerX.animateTo(
                targetValue = 900f,
                animationSpec = tween(durationMillis = 4000, easing = EaseInOut)
            )
            centerX.animateTo(
                targetValue = 300f,
                animationSpec = tween(durationMillis = 2000, easing = EaseInOut)
            )
        }
        launch {
            centerY.animateTo(
                targetValue = 600f,
                animationSpec = tween(durationMillis = 3000, easing = EaseInOut)
            )
            centerY.animateTo(
                targetValue = 900f,
                animationSpec = tween(durationMillis = 2000, easing = EaseInOut)
            )
        }

    }

    val colors = listOf(Color.White, mainAccent)
    val shader = RadialGradient(
        centerX.value, centerY.value, 500.0f,
        colors.map { it.toArgb() }.toIntArray(),
        null,
        Shader.TileMode.CLAMP
    )

    Button(
        onClick = { onButtonClick() },
        modifier = Modifier
            .width(200.dp)
            .border(
                width = 2.dp,
                brush = ShaderBrush(shader),
                shape = RoundedCornerShape(50.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Chat with Sorea",
                color = Color.White,
                fontFamily = regular_font,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 5.dp)
            )

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
        }
    }
}




@Preview
@Composable
private fun ButtonsPrev() {
    ChatSoreaButton {  }
}