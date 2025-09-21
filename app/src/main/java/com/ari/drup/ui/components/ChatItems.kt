package com.ari.drup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.R
import com.ari.drup.data.MessDao
import com.ari.drup.data.User
import com.ari.drup.regular_font
import com.ari.drup.semibold_font
import com.google.firebase.Timestamp
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun SystemChat(message: String, modifier: Modifier = Modifier) {
    Box(modifier=modifier.background(Color.White.copy(0.1f),
        shape = RoundedCornerShape(topEnd = 20.dp, topStart = 20.dp, bottomEnd = 20.dp,))) {
        Column {
            Text(
                text = "Sorea",
                modifier = Modifier.padding(start = 15.dp,
                    top = 10.dp),
                fontFamily = semibold_font,
                color = Color.White.copy(0.4f),
                fontSize = 15.sp
            )
            MarkdownText(
                markdown = message,
                fontResource = R.font.tweb_regular,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(15.dp),
            )
        }
    }
}
@Composable
fun UserChat(message: String,modifier: Modifier = Modifier) {
    Box(modifier=modifier.background(Color.White.copy(0.03f),
        shape = RoundedCornerShape(topEnd = 20.dp, topStart = 20.dp, bottomStart = 20.dp,))) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "You",
                modifier = Modifier.padding(end = 15.dp,
                    top = 10.dp),
                fontFamily = semibold_font,
                color = Color.White.copy(0.4f),
                fontSize = 15.sp
            )
            MarkdownText(
                markdown = message,
                fontResource = R.font.tweb_regular,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(15.dp),
            )
        }
    }
}

@Composable
fun ChatPrev(messDao: MessDao, modifier: Modifier = Modifier) {
    Column(modifier=modifier.fillMaxWidth().padding(10.dp)) {
        UserChat(messDao.user,modifier=modifier.align(Alignment.End))
        Spacer(modifier= Modifier.height(20.dp))
        SystemChat(messDao.model,modifier=modifier.align(Alignment.Start))
    }
}

@Preview
@Composable
private fun ChatItemPrev() {
    //UserChat("This is a normal text\nbut **when** can I will")
    ChatPrev(
        MessDao(
            isCrisis = false,
            model="I understand what you are going through **but** it not the end, you will have to think of every thing around this.",
            user = "I don't feel like goint through this",
            timestamp = Timestamp.now(),
            urgencyLevel = 0
        )
    )
}