package com.ari.drup.ui.screens

import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.R
import com.ari.drup.data.User
import com.ari.drup.regular_font
import com.ari.drup.ui.components.avatars

@Composable
fun ProfileScreen(user: User, modifier: Modifier = Modifier,onSignOut:()-> Unit) {
    Column(modifier=modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        NameAndAvatar(user.name,user.username,user.avatar)
        Button(onClick = {onSignOut()},
            modifier = Modifier.padding(vertical = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.3f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sign Out",
                    color = Color.White,
                    fontFamily = regular_font,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 5.dp)
                )

            }
        }
    }
}

@Composable
fun NameAndAvatar(name: String, username: String, avatarId:Int, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Column{
            Text(
                text = "Hello,",
                color = Color.White.copy(0.7f),
                fontFamily = regular_font,
                fontSize = 20.sp
            )
            Text(
                text = name,
                color = Color.White,
                fontFamily = regular_font,
                fontSize = 25.sp
            )
            Text(
                text = username,
                color = Color.White.copy(0.5f),
                fontFamily = regular_font,
                fontSize = 17.sp
            )

        }
        Spacer(modifier.weight(2f))
        Image(
            painterResource(avatars[avatarId]),
            contentDescription = null,
            modifier=modifier.clip(CircleShape),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview
@Composable
private fun ProfileScreenPev() {
    ProfileScreen(User("ari_archit","Archit Anant",2, gender = "Male",
        1100304000000),modifier = Modifier.background(Color.Black)){

    }
}