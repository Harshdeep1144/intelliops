package com.harshdeep.intelliops.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harshdeep.intelliops.data.model.Company
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

    private val _company = MutableStateFlow<Company?>(null)
    val company: StateFlow<Company?> = _company.asStateFlow()

    init {
        loadUser()
    }

    fun clearData() {
        _user.value = null
        _company.value = null
    }

    private fun loadUser() {
        viewModelScope.launch {
            var attempts = 0
            while (attempts < 15 && _user.value == null) {
                try {
                    kotlinx.coroutines.delay(200)
                    val u = repository.getCurrentUser()
                    if (u != null) {
                        _user.value = u
                        if (u.companyId.isNotEmpty()) {
                            _company.value = repository.getCompany(u.companyId)
                        }
                        break
                    }
                } catch (e: Exception) {
                    // Ignore and retry
                }
                attempts++
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