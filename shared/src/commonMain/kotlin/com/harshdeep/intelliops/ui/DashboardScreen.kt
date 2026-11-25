package com.harshdeep.intelliops.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harshdeep.intelliops.data.model.TaskStatus
import com.harshdeep.intelliops.data.model.User
import com.harshdeep.intelliops.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(currentUser: User?) {
    val viewModel: DashboardViewModel = viewModel { DashboardViewModel() }
    val teams by viewModel.teams.collectAsState()
    val users by viewModel.users.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    LaunchedEffect(currentUser?.companyId) {
        if (currentUser != null && currentUser.companyId.isNotEmpty()) {
            viewModel.loadCompanyData(currentUser.companyId)
        }
    }

    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF0052CC))
        }
        return
    }

    val pendingTasks = tasks.count { it.status == TaskStatus.PENDING }
    val inProgressTasks = tasks.count { it.status == TaskStatus.IN_PROGRESS }
    val completedTasks = tasks.count { it.status == TaskStatus.COMPLETED }
    val myTasks = tasks.filter { it.assigneeIds.contains(currentUser.id) || it.assigneeId == currentUser.id }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F5F7))
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Welcome Header
        item {
            Column {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF172B4D),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Welcome back, ${currentUser.name} (${currentUser.role.name})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B778C)
                )
            }
        }

        // Metrics Summary Grid (Jira Style)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "To Do",
                    count = pendingTasks.toString(),
                    color = Color(0xFF42526E),
                    bgColor = Color(0xFFDFE1E6),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "In Progress",
                    count = inProgressTasks.toString(),
                    color = Color(0xFF0747A6),
                    bgColor = Color(0xFFDEEBFF),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Done",
                    count = completedTasks.toString(),
                    color = Color(0xFF006644),
                    bgColor = Color(0xFFE3FCEF),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Company & Project Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Company Overview",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF172B4D),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF0052CC), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Company Name", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B778C))
                            Text(currentUser.companyName.ifEmpty { "IntelliOps Organization" }, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF172B4D), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF0052CC), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Invite Code", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B778C))
                            Text(currentUser.companyId.take(6).uppercase(), style = MaterialTheme.typography.bodyLarge, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF6B778C), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Members: ${users.size}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF172B4D))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF6B778C), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Teams: ${teams.size}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF172B4D))
                        }
                    }
                }
            }
        }

        // My Assigned Tasks Header
        item {
            Text(
                text = "Assigned to Me (${myTasks.size})",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF172B4D),
                fontWeight = FontWeight.Bold
            )
        }

        if (myTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No tasks currently assigned to you.", color = Color(0xFF6B778C), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            items(myTasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(task.title, style = MaterialTheme.typography.titleSmall, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold)
                            val statusColor = when (task.status) {
                                TaskStatus.PENDING -> Color(0xFF42526E) to Color(0xFFDFE1E6)
                                TaskStatus.IN_PROGRESS -> Color(0xFF0747A6) to Color(0xFFDEEBFF)
                                TaskStatus.COMPLETED -> Color(0xFF006644) to Color(0xFFE3FCEF)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(statusColor.second)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(task.status.name.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = statusColor.first, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(task.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B778C), maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, count: String, color: Color, bgColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}
