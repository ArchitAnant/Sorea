package com.ari.drup.ui.screens.communitychat

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ari.drup.R
import com.ari.drup.data.community.Community
import com.ari.drup.regular_font
import com.ari.drup.viewmodels.GroupChatViewModel

@Composable
fun CommunityPage(groupChatViewModel: GroupChatViewModel,onOpenClick: (String, String) -> Unit,onCreateClick: () -> Unit, modifier: Modifier = Modifier) {
    val activeComms = groupChatViewModel.activeCommunities.collectAsState().value
    val context = LocalContext.current
    Column(modifier = modifier
        .fillMaxSize()
        .padding(start = 10.dp, top = 40.dp, end = 10.dp)) {

        Text(
            text = "Community",
            color = Color.White,
            fontFamily = regular_font,
            fontSize = 40.sp
        )
        Spacer(Modifier.height(50.dp))
        CreateChannelButton(){
            Toast.makeText(context,"Feature in development!",Toast.LENGTH_LONG).show()
        }
        Spacer(Modifier.height(50.dp))
        Text(
            text = "Joined Communities",
            color = Color.White.copy(0.7f),
            fontFamily = regular_font,
            fontSize = 20.sp
        )
        Spacer(Modifier.height(30.dp))
        if (activeComms.value.isEmpty()){
            Row(modifier=modifier.fillMaxWidth()){
                CircularProgressIndicator(color = Color.White)
            }

        }
        else {
            LazyColumn {
                items(activeComms.value) { coms ->
                    Log.d("comms", coms.toString())
                    CommunityItem(coms) {
                        onOpenClick(coms.chatId, coms.title)
                        groupChatViewModel.joinRoom(coms.chatId)
                    }
                }
            }
        }

    }
}

@Composable
fun CreateChannelButton(onCreateClick: () -> Unit = {}) {
    Button(onClick = onCreateClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.3f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Add,
                contentDescription = "",
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Text(
                text = "Create Community",
                color = Color.White,
                fontFamily = regular_font,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 5.dp)
            )

        }
    }
}

@Composable
fun CommunityItem(displayCommunity: Community,
                  onOpenClick : () -> Unit
) {
    Button(onClick = onOpenClick,
        modifier = Modifier.padding(vertical = 5.dp).fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
           ) {
            if (displayCommunity.logo.isEmpty()){
                Image(painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "",
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                    )
            }
            else {
                AsyncImage(
                    model = displayCommunity.logo, // URL of your image
                    contentDescription = "Sample image",
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column {
                Text(
                    text = displayCommunity.title,
                    color = Color.White,
                    fontFamily = regular_font,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 5.dp)
                )
                Text(
                    text = displayCommunity.desc,
                    color = Color.White.copy(0.5f),
                    fontFamily = regular_font,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "",
                tint = Color.White.copy(0.5f)
            )
        }

    }
    
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ChannelPageScreen() {
//    CommunityPage({id,title->
//
//    },{})
}