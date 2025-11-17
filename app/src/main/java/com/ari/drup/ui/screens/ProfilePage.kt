package com.ari.drup.ui.screens

import android.annotation.SuppressLint
import android.widget.Space
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.R
import com.ari.drup.baseDark
import com.ari.drup.data.FirebaseManager
import com.ari.drup.data.Friend
import com.ari.drup.data.User
import com.ari.drup.mainAccent
import com.ari.drup.mainLight
import com.ari.drup.regular_font
import com.ari.drup.semibold_font
import com.ari.drup.ui.Screen
import com.ari.drup.ui.components.ChatSoreaButton
import com.ari.drup.ui.components.FriendList
import com.ari.drup.ui.components.SignOutButton
import com.ari.drup.ui.components.avatars
import com.ari.drup.viewmodels.OnboardingViewModel
import com.ari.drup.viewmodels.ProfilePageViewModel
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen(
    profilePageViewModel: ProfilePageViewModel,
    modifier: Modifier = Modifier,
    onAddFriendClick:()-> Unit,
    onSignOut:()-> Unit
) {
    var user = profilePageViewModel.user!!
    var expanded by remember { mutableStateOf(false) }
    var selectedLevel by remember { mutableIntStateOf(user.visibility) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val friends = profilePageViewModel.friendList.collectAsState()

    Scaffold (
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = modifier
//                    .padding(WindowInsets.statusBars.add(WindowInsets(top = 20.dp)).asPaddingValues())
            ){
                Text(
                    text = "Profile",
                    color = mainLight,
                    fontFamily = regular_font,
                    fontSize = 45.sp,
                )
                Spacer(modifier= Modifier.weight(1f))
                SignOutButton(onSignOut=onSignOut)
            }
        },
        containerColor = Color.Black
    ){ innerPadding->
        Column(modifier= Modifier
            .padding(  innerPadding)
            .padding(start = 10.dp, end = 10.dp)
            .fillMaxSize(), verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NameAndAvatar(user.name,user.username,user.avatar,Modifier.padding(vertical = 20.dp))
            Spacer(modifier = Modifier.height(40.dp))
            ProfileSelection(selectedLevel,{
                scope.launch {
                    val result = snackbarHostState
                        .showSnackbar(
                            message = "Sure Change Anonymity Level?",
                            actionLabel = "Sure",
                            // Defaults to SnackbarDuration.Short
                            duration = SnackbarDuration.Long
                        )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                           selectedLevel = it
                            profilePageViewModel.updateVisibility(selectedLevel, context)
                        }
                        SnackbarResult.Dismissed -> {
                            /* Handle snackbar dismissed */
                        }
                    }
                }
            },expanded,{
                expanded = !expanded
            })
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Friends",
                color = mainLight.copy(0.8f),
                fontFamily = regular_font,
                fontSize = 25.sp,
                modifier = Modifier.fillMaxWidth()
            )

            if (selectedLevel>0) {
                val friendList = mutableListOf(Friend("", "", -1))+friends.value
                FriendList(friendList){
                    onAddFriendClick()
                }
            }
            else {
                Text(
                    text = "Switch to Closed or Open mode to see or add friends",
                    color = mainLight.copy(0.4f),
                    fontFamily = regular_font,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier= Modifier.padding(vertical = 20.dp)
                )
            }
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileSelection(
    selectedLevel:Int,
    onSelectClick: (Int) -> Unit,
    expanded: Boolean,
    onExpandedClick: () -> Unit,
    modifier: Modifier = Modifier
) {


    AnimatedContent(
        targetState = expanded,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing)
            ) + expandVertically(
                animationSpec = tween(250, easing = LinearOutSlowInEasing)
            ) togetherWith
                    fadeOut(
                        animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                    ) + shrinkVertically(
                animationSpec = tween(200, easing = FastOutLinearInEasing)
            )
        },
        label = "ExpandCollapseAnimation"
    ) { isExpanded ->
        if (isExpanded) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(40.dp))
                    .background(mainAccent.copy(0.4f))
                    .clickable { onExpandedClick() }
                    .padding(15.dp)
            ) {
                Text(
                    text = "Select your anonymity level",
                    color = mainLight,
                    fontFamily = regular_font,
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp),
                    fontSize = 18.sp
                )

                AnimatedContent(
                    targetState = selectedLevel,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(200, easing = LinearOutSlowInEasing)
                        ) togetherWith
                                fadeOut(
                                    animationSpec = tween(150, easing = FastOutLinearInEasing)
                                )
                    },
                    label = "DescriptionChangeAnimation"
                ) { level ->
                    Text(
                        text = when (level) {
                            0 -> "The Anonymous Circle is the most secured, Your leaderboard, streak score and badges will be hidden from everyone."
                            1 -> "The Closed Circle is where, Your leaderboard score, streak score and badges will be visible to only people you want."
                            else -> "The Open Circle is where, Your leaderboard score, streak score and badges will be visible to all."
                        },
                        color = mainLight.copy(0.6f),
                        fontFamily = regular_font,
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .padding(horizontal = 5.dp),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Row {
                    TypeLevels(
                        typeCircle = {
                            Canvas(modifier = Modifier.size(40.dp)) {
                                drawDottedCircle(
                                    center = center,
                                    radius = (size.minDimension / 2f) - 4.dp.toPx(),
                                    dotCount = 14,
                                    dotRadius = 2.dp.toPx(),
                                    color = mainLight
                                )
                            }
                        },
                        levelText = "Anonymous",
                        selected = selectedLevel == 0,
                        onSelectClick = { onSelectClick(0) },
                        modifier = Modifier.weight(1f)
                    )

                    TypeLevels(
                        typeCircle = {
                            Canvas(modifier = Modifier.size(40.dp)) {
                                drawCircle(
                                    color = mainLight,
                                    radius = size.minDimension / 5f,
                                    center = center,
                                    style = Fill
                                )
                                drawDottedCircle(
                                    center = center,
                                    radius = (size.minDimension / 2f) - 4.dp.toPx(),
                                    dotCount = 14,
                                    dotRadius = 2.dp.toPx(),
                                    color = mainLight
                                )
                            }
                        },
                        levelText = "Closed",
                        selected = selectedLevel == 1,
                        onSelectClick = { onSelectClick(1)  },
                        modifier = Modifier.weight(1f)
                    )

                    TypeLevels(
                        typeCircle = {
                            Canvas(modifier = Modifier.size(40.dp)) {
                                drawCircle(
                                    color = mainLight,
                                    radius = size.minDimension / 2.5f,
                                    center = center,
                                    style = Fill
                                )
                            }
                        },
                        levelText = "Open",
                        selected = selectedLevel == 2,
                        onSelectClick = { onSelectClick(2)  },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(40.dp))
                    .background(mainAccent.copy(0.4f))
                    .clickable { onExpandedClick() }
                    .padding(15.dp)
            ) {
                Text(
                    text = "Anonymity level",
                    color = mainLight,
                    fontFamily = regular_font,
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp, start = 10.dp),
                    fontSize = 18.sp
                )
                Text(
                    text = when (selectedLevel) {
                        0 -> "Anonymous"
                        1 -> "Closed"
                        else -> "Everyone"
                    },
                    color = mainAccent,
                    fontFamily = semibold_font,
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp, start = 10.dp),
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = mainLight,
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
        }
    }
}

@Composable
fun TypeLevels(
    typeCircle: @Composable () -> Unit,
    levelText: String,
    selected: Boolean,
    onSelectClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val bgColor by animateColorAsState(
        targetValue = if (selected) mainAccent.copy(0.4f) else Color.Transparent,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "colorAnim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(25.dp))
            .background(bgColor)
            .clickable { onSelectClick() }
            .padding(vertical = 10.dp)
    ) {
        typeCircle()
        Text(
            text = levelText,
            color = mainLight,
            fontFamily = regular_font,
            modifier = Modifier.padding(top = 10.dp),
            fontSize = 12.sp
        )
    }
}


@Composable
fun NameAndAvatar(name: String, username: String, avatarId:Int, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Column{
            Text(
                text = "Hello,",
                color = mainLight.copy(0.7f),
                fontFamily = regular_font,
                fontSize = 20.sp
            )
            Text(
                text = name,
                color = mainLight,
                fontFamily = regular_font,
                fontSize = 25.sp
            )
            Text(
                text = username,
                color = mainLight.copy(0.5f),
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

private fun DrawScope.drawDottedCircle(
    center: Offset,
    radius: Float,
    dotCount: Int,
    dotRadius: Float,
    color: Color
) {
    if (radius <= 0f || dotCount <= 0) return
    val twoPi = (2.0 * PI).toFloat()
    for (i in 0 until dotCount) {
        val angle = twoPi * (i.toFloat() / dotCount)
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)
        drawCircle(
            color = color,
            radius = dotRadius,
            center = Offset(x, y)
        )
    }
}

@Preview
@Composable
private fun ProfileScreenPev() {
//    ProfileScreen(ProfilePageViewModel(FirebaseManager(), OnboardingViewModel()),modifier = Modifier.background(Color.Black),{}){
//    }
}