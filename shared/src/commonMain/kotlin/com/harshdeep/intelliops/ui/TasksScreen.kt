package com.harshdeep.intelliops.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.harshdeep.intelliops.data.model.Task
import com.harshdeep.intelliops.data.model.TaskStatus

@Composable
fun TasksScreen() {
    // In a real app, use a ViewModel to collect tasks from the repository
    // For now, mock data to show the UI
    var tasks by remember { 
        mutableStateOf(
            listOf(
                Task(id = "1", title = "Setup CI/CD", description = "Configure GitHub Actions", status = TaskStatus.IN_PROGRESS, progress = 50),
                Task(id = "2", title = "Write Tests", description = "Unit tests for Auth", status = TaskStatus.PENDING, progress = 0)
            )
        ) 
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tasks", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tasks) { task ->
                TaskCard(task) { updatedTask ->
                    tasks = tasks.map { if (it.id == updatedTask.id) updatedTask else it }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TaskCard(task: Task, onUpdate: (Task) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(task.title, style = MaterialTheme.typography.titleMedium)
            Text(task.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Progress: ${task.progress}%")
            Slider(
                value = task.progress.toFloat(),
                onValueChange = { newValue ->
                    val newProgress = newValue.toInt()
                    val newStatus = when (newProgress) {
                        0 -> TaskStatus.PENDING
                        100 -> TaskStatus.COMPLETED
                        else -> TaskStatus.IN_PROGRESS
                    }
                    onUpdate(task.copy(progress = newProgress, status = newStatus))
                },
                valueRange = 0f..100f
            )
        }
    }
}