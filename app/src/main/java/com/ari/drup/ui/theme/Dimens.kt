package com.ari.drup.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimens(
    val paddingSmall: Dp = 8.dp,
    val paddingMedium: Dp = 16.dp,
    val paddingLarge: Dp = 24.dp,
    val verticalSpaceMedium: Dp = 24.dp,
    val verticalSpaceLarge: Dp = 32.dp
)

// For most phones in portrait
val CompactDimens = Dimens(
    paddingSmall = 8.dp,
    paddingMedium = 16.dp,
    paddingLarge = 20.dp,
    verticalSpaceMedium = 24.dp,
    verticalSpaceLarge = 32.dp
)

// For larger phones or tablets
val ExpandedDimens = Dimens(
    paddingSmall = 12.dp,
    paddingMedium = 24.dp,
    paddingLarge = 32.dp,
    verticalSpaceMedium = 32.dp,
    verticalSpaceLarge = 48.dp
)

val LocalAppDimens = staticCompositionLocalOf { CompactDimens }