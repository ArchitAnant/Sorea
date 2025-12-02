package com.ari.drup.ui.components

import android.widget.Space
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.ari.drup.darkAccent
import com.ari.drup.mainLight
import com.ari.drup.regular_font
import com.ari.drup.semibold_font

@Composable
fun MainChatTips(
    suggestions: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    // Height animation for expand/collapse
    val animatedVisibility = remember { MutableTransitionState(true).apply { targetState = true } }

    animatedVisibility.targetState = expanded

    Box(
        modifier = modifier.padding(top = 10.dp)
    ) {

        if (suggestions.isNotEmpty()) {
            Column {
                // Top Bar
                Row(modifier = Modifier.padding(start = 10.dp).clickable(indication = null,
                    interactionSource = remember { MutableInteractionSource() }) { expanded = !expanded }) {

                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = "",
                        tint = mainLight
                    )

                    Text(
                        text = "Tips:",
                        fontFamily = semibold_font,
                        color = mainLight,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.weight(2f))

                    Icon(
                        imageVector = if (expanded)
                            Icons.Default.KeyboardArrowDown
                        else
                            Icons.Default.KeyboardArrowUp,
                        contentDescription = "",
                        tint = mainLight,
                        modifier = Modifier
                            .padding(end = 10.dp)

                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Animated content
                AnimatedVisibility(
                    visibleState = animatedVisibility,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    LazyRow(contentPadding = PaddingValues(10.dp)) {
                        items(
                            items = suggestions,
                            key = { it } // IMPORTANT: key needed so Jetpack Compose knows which item is which
                        ) { tip ->
                            Row(
                                modifier = Modifier.animateItem(tween(
                                    durationMillis = 300,
                                    easing = FastOutLinearInEasing
                                ))
                            ) {
                                Text(
                                    text = tip,
                                    fontFamily = regular_font,
                                    color = mainLight,
                                    fontSize = 15.sp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .width(250.dp)
                                        .background(darkAccent.copy(0.6f))
                                        .padding(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                        }
                    }

                }
            }
        }
    }
}


@Preview
@Composable
private fun TipsPrev() {
    MainChatTips(
        listOf(
            "Break down tough OS topics into smaller steps",
            "When overwhelmed, Archit, switch to an easier OS task.",
            "Take a quick 5-minute mental break, Archit.",
            "Try explaining tricky concepts out loud to yourself."
        )
    )
}