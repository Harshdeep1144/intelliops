package com.harshdeep.intelliops.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
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

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val viewModel: ProfileViewModel = viewModel { ProfileViewModel() }
    val user by viewModel.user.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showAiHub by remember { mutableStateOf(false) }

    val tabs = listOf("Dashboard", "Tasks", "Meetings", "Profile")
    val icons = listOf(Icons.Default.Home, Icons.Default.List, Icons.Default.Event, Icons.Default.Person)

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Main Navigation Pill (4 tabs)
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
                        val isSelected = !showAiHub && selectedTab == index
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(32.dp))
                                .background(if (isSelected) Color(0xFFF4F5F7) else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { 
                                    selectedTab = index 
                                    showAiHub = false 
                                },
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
                
                // Separate AI Hub Floating Action Button on the Right
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .glassmorphism(shape = CircleShape, alpha = 0.95f, tint = Color.White)
                        .clickable { showAiHub = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Hub",
                        tint = Color(0xFF0052CC),
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
            if (showAiHub) {
                AiHubScreen()
            } else {
                when (selectedTab) {
                    0 -> DashboardScreen(currentUser = user)
                    1 -> TasksScreen(currentUser = user)
                    2 -> MeetingsScreen()
                    3 -> ProfileScreen(onLogout)
                }
            }
        }
    }
}

@Composable
fun MeetingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F5F7))
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Text("Team Meetings", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold)
        Text("Upcoming syncs and standups", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B778C))
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Daily Standup", style = MaterialTheme.typography.titleMedium, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Today at 10:00 AM", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF0052CC), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Review sprint progress, blockers, and deliverables.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B778C))
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Join meeting */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Join Meeting", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AiHubScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F5F7))
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Text("AI Hub", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold)
        Text("Intelligent orchestration and agent tools", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B778C))
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("IntelliOps AI Assistant", style = MaterialTheme.typography.titleLarge, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your intelligent agent for team coordination, automated task tracking, and insights.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B778C))
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { /* AI action */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ask AI Assistant", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
