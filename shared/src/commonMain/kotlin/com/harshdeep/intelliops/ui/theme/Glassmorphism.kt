package com.harshdeep.intelliops.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphism(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.dp,
    alpha: Float = 0.85f,
    tint: Color = Color.White
): Modifier = this
    .clip(shape)
    .background(tint.copy(alpha = alpha))
    .border(
        width = borderWidth,
        color = Color.Black.copy(alpha = 0.08f),
        shape = shape
    )