package com.harshdeep.intelliops.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harshdeep.intelliops.data.model.Task
import com.harshdeep.intelliops.data.model.Team
import com.harshdeep.intelliops.data.model.User
import com.harshdeep.intelliops.data.repository.IntelliOpsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val repository = IntelliOpsRepository()
    
    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    fun clearData() {
        _teams.value = emptyList()
        _users.value = emptyList()
        _tasks.value = emptyList()
    }

    fun loadCompanyData(companyId: String) {
        viewModelScope.launch {
            repository.getTeams(companyId).collect {
                _teams.value = it
            }
        }
        viewModelScope.launch {
            repository.getCompanyUsers(companyId).collect {
                _users.value = it
            }
        }
        viewModelScope.launch {
            repository.getTasks(companyId).collect {
                _tasks.value = it
            }
        }
    }

    fun createTeam(companyId: String, name: String, description: String) {
        viewModelScope.launch {
            repository.createTeam(companyId, Team(name = name, description = description))
        }
    }

    fun updateTeamDetails(companyId: String, teamId: String, name: String, description: String) {
        viewModelScope.launch {
            repository.updateTeamDetails(companyId, teamId, name, description)
        }
    }

    fun updateTeamName(companyId: String, teamId: String, newName: String) {
        viewModelScope.launch {
            repository.updateTeamName(companyId, teamId, newName)
        }
    }

    fun assignUserToTeam(companyId: String, userId: String, teamId: String) {
        viewModelScope.launch {
            repository.assignUserToTeam(companyId, userId, teamId)
        }
    }

    fun removeUserFromTeam(companyId: String, teamId: String, userId: String) {
        viewModelScope.launch {
            repository.removeUserFromTeam(companyId, teamId, userId)
        }
    }

    fun deleteTeam(companyId: String, teamId: String) {
        viewModelScope.launch {
            repository.deleteTeam(companyId, teamId)
        }
    }
}