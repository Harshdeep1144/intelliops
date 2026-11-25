package com.harshdeep.intelliops.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harshdeep.intelliops.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val viewModel: ProfileViewModel = viewModel { ProfileViewModel() }
    val user by viewModel.user.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color(0xFF6B778C))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (user != null) {
            Text(user!!.name, style = MaterialTheme.typography.titleLarge, color = Color(0xFF172B4D))
            Text(user!!.email, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B778C))
            
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFDEEBFF))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Role: ${user!!.role.name}", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color(0xFF0747A6),
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            CircularProgressIndicator(color = Color(0xFF0052CC))
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                viewModel.clearData() // Clear local profile data
                viewModel.logout {
                    onLogout()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5630)), // Red for logout
            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
        ) {
            Text("Logout", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
    }
}