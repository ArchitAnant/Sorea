package com.ari.drup.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.ari.drup.darkAccent
import com.ari.drup.mainLight
import com.ari.drup.regular_font
import com.ari.drup.semibold_font
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinCommunityDrawer(communityName:String, uname:String, onDismissRequest: ()-> Unit, joinCommunityClick:(String)-> Unit, modifier: Modifier = Modifier) {
    val sheetState = rememberModalBottomSheetState()

    var newUname by remember { mutableStateOf(uname) }
    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest()
        },
        sheetState = sheetState,
        containerColor = darkAccent
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
            Text(
                text = "Join $communityName",
                fontFamily = semibold_font,
                fontSize = 23.sp,
                color = mainLight
            )
            Text(
                text = "You can choose a new username everytime you join a community. You can't change this afterwards and this will not effect your current username!",
                fontFamily = regular_font,
                fontSize = 15.sp,
                color = mainLight,
                modifier = Modifier.padding(top = 10.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = newUname,
                    onValueChange = { newUname = it },
                    placeholder = {
                        Text(
                            text = "Type your message here",
                            color = mainLight.copy(0.3f),
                            fontFamily = regular_font,
                            fontSize = 15.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f),

                    colors = TextFieldDefaults.colors(
                        focusedTextColor = mainLight,
                        unfocusedTextColor = mainLight,
                        focusedContainerColor = mainLight.copy(0.2f),
                        unfocusedContainerColor = mainLight.copy(0.2f),
                        focusedLabelColor = mainLight,
                        focusedIndicatorColor = mainLight,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(40.dp),
                    textStyle = TextStyle(
                        fontFamily = semibold_font,
                        fontSize = 15.sp
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        if (uname.isNotEmpty()) {
                            joinCommunityClick(newUname)
                        }
                    },
                    modifier = Modifier.height(55.dp).widthIn(max=100.dp,min=70.dp),   // SAME HEIGHT AS TEXT FIELD
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Join",
                        fontFamily = semibold_font,
                        fontSize = 15.sp,
                        color = darkAccent
                    )
                }
            }

        }
    }
}

@Preview
@Composable
private fun PreviewJoinCommunityDrawer() {
//    JoinCommunityDrawer("Dev-Testing","ari_archit")
}