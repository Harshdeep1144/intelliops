package com.harshdeep.intelliops.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harshdeep.intelliops.ui.viewmodel.DashboardViewModel
import com.harshdeep.intelliops.data.model.Role
import com.harshdeep.intelliops.data.model.User

@Composable
fun DashboardScreen(currentUser: User?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "Dashboard is disabled. Please navigate to the Tasks tab.", 
            color = Color.Gray,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}