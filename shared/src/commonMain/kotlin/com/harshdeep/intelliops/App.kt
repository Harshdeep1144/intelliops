package com.harshdeep.intelliops

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import com.harshdeep.intelliops.ui.AuthScreen
import com.harshdeep.intelliops.ui.MainScreen

@Composable
fun App() {
    MaterialTheme {
        var isAuthenticated by remember { mutableStateOf(false) }

        // Clean light background instead of a harsh gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F5F7))
        ) {
            if (isAuthenticated) {
                MainScreen(onLogout = { isAuthenticated = false })
            } else {
                AuthScreen(onAuthenticated = { isAuthenticated = true })
            }
        }
    }
}