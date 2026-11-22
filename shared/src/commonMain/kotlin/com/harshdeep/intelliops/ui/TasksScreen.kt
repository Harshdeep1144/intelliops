package com.harshdeep.intelliops.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.harshdeep.intelliops.data.model.Task
import com.harshdeep.intelliops.data.model.TaskStatus
import com.harshdeep.intelliops.ui.theme.glassmorphism

@Composable
fun TasksScreen() {
    var tasks by remember { 
        mutableStateOf(
            listOf(
                Task(id = "1", title = "Market research & segmentation", description = "GTM-1", status = TaskStatus.IN_PROGRESS, progress = 50),
                Task(id = "2", title = "Conduct market analysis", description = "GTM-9", status = TaskStatus.COMPLETED, progress = 100),
                Task(id = "3", title = "Identify target customer segments", description = "GTM-8", status = TaskStatus.IN_PROGRESS, progress = 20),
                Task(id = "4", title = "Compile a market research report", description = "GTM-10", status = TaskStatus.IN_PROGRESS, progress = 70),
                Task(id = "5", title = "Gather competitive intelligence", description = "GTM-12", status = TaskStatus.PENDING, progress = 0)
            )
        ) 
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("List", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF172B4D), fontWeight = FontWeight.Bold)
            
            // Avatar Pile Mock
            Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                repeat(4) {
                    Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(Color.Gray.copy(alpha=0.5f)).border(1.dp, Color.White, RoundedCornerShape(16.dp)))
                }
                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).border(1.dp, Color.LightGray, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Text("+3", color = Color(0xFF172B4D), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphism(shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp), alpha = 0.95f)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(32.dp)) // Indent spacer
            Text("Summary", color = Color(0xFF42526E), modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
            Text("Status", color = Color(0xFF42526E), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("Assignee", color = Color(0xFF42526E), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("Due date", color = Color(0xFF42526E), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        }
        
        // Table Body
        LazyColumn(modifier = Modifier.fillMaxSize().glassmorphism(shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp), alpha = 0.95f)) {
            items(tasks) { task ->
                TaskTableRow(task) { updatedTask ->
                    tasks = tasks.map { if (it.id == updatedTask.id) updatedTask else it }
                }
                Divider(color = Color.Black.copy(alpha = 0.1f), thickness = 1.dp)
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create", color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun TaskTableRow(task: Task, onUpdate: (Task) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Expand icon
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF6B778C), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        
        // Title & ID
        Column(modifier = Modifier.weight(2f)) {
            Text(
                task.title, 
                style = MaterialTheme.typography.bodyMedium, 
                color = Color(0xFF172B4D),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                task.description, 
                style = MaterialTheme.typography.labelSmall, 
                color = Color(0xFF0052CC) // Jira Link blue
            )
        }
        
        // Status Badge
        Box(modifier = Modifier.weight(1f)) {
            val (bgColor, textColor, text) = when (task.status) {
                TaskStatus.PENDING -> Triple(Color(0xFFDFE1E6), Color(0xFF42526E), "TO DO")
                TaskStatus.IN_PROGRESS -> Triple(Color(0xFFDEEBFF), Color(0xFF0747A6), "IN PROGRESS")
                TaskStatus.COMPLETED -> Triple(Color(0xFFE3FCEF), Color(0xFF006644), "DONE")
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(bgColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text, color = textColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        
        // Assignee Mock
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(12.dp)).background(Color.Gray.copy(alpha=0.5f)))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Alana S.", color = Color(0xFF172B4D), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        
        // Due Date Mock
        Text("May 25", color = Color(0xFF42526E), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
    }
}