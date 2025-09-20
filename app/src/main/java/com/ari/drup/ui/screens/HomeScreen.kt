package com.ari.drup.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.regular_font
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Try the Chat",
                color = Color.White,
                fontFamily = regular_font,
                modifier = Modifier.padding(horizontal = 20.dp),
                fontSize = 25.sp
            )
            Text(
                text = "Home Screen Under Development",
                color = Color.White.copy(0.6f),
                fontFamily = regular_font,
                modifier = Modifier.padding(horizontal = 20.dp),
                fontSize = 18.sp
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val start = Offset(size.width / 2f, size.height / 2.8f) // screen center
            val end = Offset(size.width * 0.85f, size.height * -0.1f) // near top-right

            // Arrow line
            drawLine(
                color = Color.White,
                start = start,
                end = end,
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )

            // Arrowhead
            val arrowAngle = 25f
            val arrowLength = 40f

            val angle = atan2(end.y - start.y, end.x - start.x)

            val arrowPoint1 = Offset(
                x = end.x - arrowLength * cos(angle - Math.toRadians(arrowAngle.toDouble())).toFloat(),
                y = end.y - arrowLength * sin(angle - Math.toRadians(arrowAngle.toDouble())).toFloat()
            )

            val arrowPoint2 = Offset(
                x = end.x - arrowLength * cos(angle + Math.toRadians(arrowAngle.toDouble())).toFloat(),
                y = end.y - arrowLength * sin(angle + Math.toRadians(arrowAngle.toDouble())).toFloat()
            )

            drawLine(color = Color.White, start = end, end = arrowPoint1, strokeWidth = 5f)
            drawLine(color = Color.White, start = end, end = arrowPoint2, strokeWidth = 5f)
        }
    }
}

@Preview
@Composable
private fun HomeScreenPrev() {
    HomeScreen()
}