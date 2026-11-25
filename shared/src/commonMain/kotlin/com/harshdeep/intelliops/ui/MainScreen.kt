package com.harshdeep.intelliops.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.harshdeep.intelliops.ui.theme.glassmorphism
import com.harshdeep.intelliops.ui.viewmodel.ProfileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harshdeep.intelliops.ui.theme.glassmorphism

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    // We can use ProfileViewModel here to get the current user to pass to tabs
    val viewModel: ProfileViewModel = viewModel { ProfileViewModel() }
    val user by viewModel.user.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Tasks", "AI Hub", "Profile")
    val icons = listOf(Icons.Default.Home, Icons.Default.List, Icons.Default.Star, Icons.Default.Person)

    Scaffold(
        containerColor = Color.Transparent, // Let the app background show through
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp), // Lift it up slightly
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp) // Space between the main bar and the FAB
            ) {
                // Main Navigation Pill
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .glassmorphism(shape = RoundedCornerShape(36.dp), alpha = 0.95f, tint = Color.White)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(32.dp))
                                .background(if (isSelected) Color(0xFFF4F5F7) else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null // Removes the ripple effect to match iOS tab bars
                                ) { selectedTab = index },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = title,
                                tint = if (isSelected) Color(0xFF172B4D) else Color(0xFF6B778C),
                                modifier = Modifier.size(if (isSelected) 28.dp else 24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color(0xFF172B4D) else Color(0xFF6B778C),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                // Floating Action Button (AI Icon)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .glassmorphism(shape = CircleShape, alpha = 0.95f, tint = Color.White)
                        .clickable { /* AI Action */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "AI Hub",
                        tint = Color(0xFF0052CC), // Blue accent for AI
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(currentUser = user)
                1 -> TasksScreen(currentUser = user)
                2 -> Text("AI Hub (Coming soon)", color = Color(0xFF172B4D), modifier = Modifier.padding(16.dp))
                3 -> ProfileScreen(onLogout)
            }
        }
    }
}