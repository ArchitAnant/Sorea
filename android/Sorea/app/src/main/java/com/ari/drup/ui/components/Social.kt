package com.ari.drup.ui.components

import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.baseDark
import com.ari.drup.darkAccent
import com.ari.drup.data.Friend
import com.ari.drup.mainAccent
import com.ari.drup.mainLight
import com.ari.drup.semibold_font

@Composable
fun FriendList(friendList: List<Friend>, modifier: Modifier = Modifier,addFriendScreen : ()-> Unit) {
    LazyRow(contentPadding = PaddingValues(10.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        items(friendList.size) {
            if (friendList[it].avatar==-1){
                AddFriendButton {
                    addFriendScreen()
                }
            }
            else {
                FriendItem(friendList[it])
            }
        }
    }
}

@Composable
fun FriendItem(friend:Friend,modifier: Modifier = Modifier) {
    Box{
        Image(
            painterResource(avatars[friend.avatar]),
            contentDescription = null,
            modifier = Modifier.padding(bottom = 5.dp).clip(CircleShape).size(80.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = friend.username,
            color = mainLight,
            fontFamily = semibold_font,
            fontSize = 12.sp,
            modifier= Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(darkAccent)
                .padding(vertical = 5.dp, horizontal = 7.dp)
                .align(Alignment.BottomCenter)
        )
    }
}

@Preview(showBackground = true, backgroundColor = Color.BLACK.toLong())
@Composable
private fun SocialPrev() {
    val temp =Friend(email = "architanant5@gmail.com",
        username = "ari_archit",
        avatar = 0)
    FriendList(
        listOf(Friend("","",-1),temp,temp,temp,temp)
    ){}
}