package com.harshdeep.intelliops.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harshdeep.intelliops.data.model.Company
import com.harshdeep.intelliops.data.model.Role
import com.harshdeep.intelliops.data.model.User
import com.harshdeep.intelliops.data.repository.IntelliOpsRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = IntelliOpsRepository()
    private val auth = Firebase.auth
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun checkUser() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Idle
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun signUpAdmin(email: String, pass: String, name: String, companyName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass)
                val user = User(
                    id = result.user?.uid ?: "",
                    email = email,
                    name = name,
                    role = Role.ADMIN
                )
                // Use the user's ID as the company ID for simplicity, or generate one
                val company = Company(
                    id = user.id,
                    name = companyName,
                    adminId = user.id,
                    inviteCode = user.id.take(6).uppercase(),
                    createdAt = 0L // use clock here normally
                )
                repository.createCompany(company, user.copy(companyId = company.id))
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Signup Failed")
            }
        }
    }

    fun signUpEmployee(email: String, pass: String, name: String, inviteCode: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass)
                val user = User(
                    id = result.user?.uid ?: "",
                    email = email,
                    name = name,
                    role = Role.EMPLOYEE
                )
                
                val joinResult = repository.joinCompany(inviteCode, user)
                if (joinResult.isSuccess) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Failed to join company")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Signup Failed")
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, pass)
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login Failed")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}