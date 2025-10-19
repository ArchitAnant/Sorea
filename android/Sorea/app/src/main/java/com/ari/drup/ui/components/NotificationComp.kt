package com.ari.drup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.JoinFull
import androidx.compose.material.icons.filled.JoinInner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.R
import com.ari.drup.darkAccent
import com.ari.drup.data.Friend
import com.ari.drup.mainAccent
import com.ari.drup.mainLight
import com.ari.drup.regular_font
import com.ari.drup.semibold_font
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequest(
    username: String,
    onActionClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onActionClick(false) // dragged left → reject
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.2f))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Rejecting Request",
                    fontFamily = semibold_font,
                    color = mainLight,
                    fontSize = 15.sp,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        },
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(vertical = 10.dp)
            ) {
                Icon(
                    Icons.Default.JoinInner,
                    contentDescription = "",
                    tint = mainLight,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                    Text(text = username,
                        fontFamily = semibold_font,
                        color = mainLight,
                        fontSize = 18.sp)
                    Text(text = "sent you a friend request",
                        fontFamily = regular_font,
                        color = mainLight,
                        fontSize = 18.sp)
                    Text(
                        text = "Drag left to cancel",
                        fontFamily = regular_font,
                        color = mainLight.copy(0.5f),
                        fontSize = 15.sp
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Accept",
                    color = Color.Green.copy(0.8f),
                    fontFamily = semibold_font,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Green.copy(0.15f))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable { onActionClick(true) },
                    fontSize = 18.sp
                )
                Spacer(Modifier.width(10.dp))
            }
        }
    )
}


@Preview(showBackground = true, backgroundColor = android.graphics.Color.BLACK.toLong())
@Composable
private fun NotifPrev() {
    FriendRequest("ari_archit",{})
}