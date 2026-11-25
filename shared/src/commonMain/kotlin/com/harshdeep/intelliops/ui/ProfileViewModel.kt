package com.harshdeep.intelliops.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harshdeep.intelliops.data.model.User
import com.harshdeep.intelliops.data.repository.IntelliOpsRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = IntelliOpsRepository()
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                _user.value = repository.getCurrentUser()
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                Firebase.auth.signOut()
                onLogoutSuccess()
            } catch (e: Exception) {
                // In a real app, handle error properly
                onLogoutSuccess() // Fallback to allow UI to proceed even if network fails
            }
        }
    }
}