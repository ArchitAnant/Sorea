package com.ari.drup.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.regular_font

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(modifier=modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Home Screen",
            color = Color.White,
            fontFamily = regular_font,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            fontSize = 25.sp
        )
    }
}

@Preview
@Composable
private fun HomeScreenPrev() {
    HomeScreen()
}