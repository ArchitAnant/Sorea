package com.ari.drup.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.darkAccent
import com.ari.drup.mainAccent
import com.ari.drup.mainLight
import com.ari.drup.regular_font


@Composable
fun ChatBox(message: String,
            onMessageChange: (String) -> Unit,
            modifier: Modifier = Modifier
) {

    OutlinedTextField(
        value = message,
        onValueChange = { onMessageChange(it) },
        placeholder = {
            Text(
                text="Type your message here",
                color = mainLight.copy(0.3f),
                fontFamily = regular_font,
                fontSize = 18.sp
            )
        },
        modifier = modifier.fillMaxWidth(0.85f),
        colors = TextFieldDefaults.colors(
            focusedTextColor = mainLight,
            unfocusedTextColor = Color.Transparent,
            focusedContainerColor = darkAccent.copy(0.7f),
            unfocusedContainerColor = darkAccent.copy(0.7f),
            focusedLabelColor = darkAccent,
            focusedIndicatorColor = darkAccent,
            unfocusedIndicatorColor = Color.Transparent,
            unfocusedLeadingIconColor = darkAccent,
            focusedLeadingIconColor = darkAccent,

            ),
        shape = RoundedCornerShape(40.dp),
        textStyle = TextStyle(
            fontFamily = regular_font,
            fontSize = 18.sp
        )
    )
}