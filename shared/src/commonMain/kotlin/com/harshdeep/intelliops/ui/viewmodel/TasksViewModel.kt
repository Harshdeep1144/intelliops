package com.harshdeep.intelliops.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harshdeep.intelliops.data.model.Task
import com.harshdeep.intelliops.data.model.TaskStatus
import com.harshdeep.intelliops.data.model.User
import com.harshdeep.intelliops.data.repository.IntelliOpsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TasksViewModel : ViewModel() {
    private val repository = IntelliOpsRepository()
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    fun clearData() {
        _tasks.value = emptyList()
        _users.value = emptyList()
    }

    fun loadData(companyId: String) {
        viewModelScope.launch {
            repository.getTasks(companyId).collect {
                _tasks.value = it
            }
        }
        viewModelScope.launch {
            repository.getCompanyUsers(companyId).collect {
                _users.value = it
            }
        }
    }

    fun createTask(
        companyId: String, 
        title: String, 
        description: String, 
        assigneeIds: List<String> = emptyList(), 
        createdBy: String = "", 
        teamId: String?, 
        priority: com.harshdeep.intelliops.data.model.Priority, 
        parentTaskId: String? = null
    ) {
        viewModelScope.launch {
            val task = Task(
                id = com.harshdeep.intelliops.data.model.randomUUID(),
                title = title,
                description = description,
                assigneeId = assigneeIds.firstOrNull(),
                assigneeIds = assigneeIds,
                teamId = teamId,
                createdBy = createdBy,
                status = TaskStatus.PENDING,
                progress = 0,
                priority = priority,
                parentTaskId = parentTaskId
            )
            repository.createTask(companyId, task)
        }
    }

    fun updateProgress(companyId: String, taskId: String, progress: Int, status: TaskStatus) {
        viewModelScope.launch {
            repository.updateTaskProgress(companyId, taskId, progress, status)
        }
    }

    fun updateTaskDetails(
        companyId: String, 
        taskId: String, 
        title: String, 
        description: String, 
        assigneeIds: List<String> = emptyList(),
        priority: com.harshdeep.intelliops.data.model.Priority = com.harshdeep.intelliops.data.model.Priority.MID
    ) {
        viewModelScope.launch {
            repository.updateTaskDetails(
                companyId = companyId, 
                taskId = taskId, 
                title = title, 
                description = description, 
                assigneeIds = assigneeIds,
                priority = priority,
                assigneeId = assigneeIds.firstOrNull()
            )
        }
    }

    fun deleteTask(companyId: String, taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(companyId, taskId)
        }
    }
}