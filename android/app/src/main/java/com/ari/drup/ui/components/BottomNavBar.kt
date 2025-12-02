package com.ari.drup.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeMini
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material.icons.outlined.ChangeHistory
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HomeMini
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.baseDark
import com.ari.drup.mainAccent
import com.ari.drup.mainLight
import com.ari.drup.semibold_font

val mainColor = Color.Black
val backColor = Color.White.copy(0.4f)

@Composable
fun BottomNavigation(
    currentSelection: Int = 0,
    onBottomClick: (String) -> Unit
) {
    val radioOptions = listOf("Home", "Community", "Profile")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[currentSelection]) }

    Column(
        modifier = Modifier
//            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(mainAccent)
            .padding(horizontal = 16.dp, vertical = 10.dp)
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.selectableGroup()) {
            radioOptions.forEach { text ->
                val isSelected = text == selectedOption

                // Animate background color
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) backColor.copy(alpha = 0.5f) else Color.Transparent,
                    animationSpec = tween(durationMillis = 200)
                )

                // Animate icon tint
                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) mainColor else mainColor.copy(alpha = 0.7f),
                    animationSpec = tween(durationMillis = 200)
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(backgroundColor)
                        .selectable(
                            selected = isSelected,
                            onClick = {
                                onOptionSelected(text)
                                onBottomClick(text)
                            },
                            role = Role.RadioButton,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (text) {
                            "Home" -> if (isSelected) Icons.Filled.HomeMini else Icons.Outlined.HomeMini
                            "Chat" -> if (isSelected) Icons.Filled.BubbleChart else Icons.Outlined.BubbleChart
                            "Community" -> if (isSelected) Icons.Filled.ChangeHistory else Icons.Outlined.ChangeHistory
                            "Profile" -> if (isSelected) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle
                            else -> Icons.Default.Home
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = text,
                            tint = baseDark,
                            modifier = Modifier.size(24.dp)
                        )

                        AnimatedVisibility(visible = isSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = text,
                                color = baseDark,
                                fontFamily = semibold_font,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun BottomNavPrev() {
    BottomNavigation(){

    }
}