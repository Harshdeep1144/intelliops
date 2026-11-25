package com.harshdeep.intelliops.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harshdeep.intelliops.ui.theme.glassmorphism

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel { AuthViewModel() }
    val authState by viewModel.authState.collectAsState()
    
    var isLogin by remember { mutableStateOf(true) }
    var isAdmin by remember { mutableStateOf(false) }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }

    val glassTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF0052CC),
        unfocusedBorderColor = Color(0xFFDFE1E6),
        focusedTextColor = Color(0xFF172B4D),
        unfocusedTextColor = Color(0xFF172B4D),
        focusedLabelColor = Color(0xFF0052CC),
        unfocusedLabelColor = Color(0xFF6B778C),
        focusedLeadingIconColor = Color(0xFF0052CC),
        unfocusedLeadingIconColor = Color(0xFF6B778C),
        cursorColor = Color(0xFF0052CC)
    )

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            viewModel.resetState()
            onAuthenticated()
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.checkUser()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .glassmorphism(shape = RoundedCornerShape(24.dp), alpha = 1.0f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (isLogin) "Welcome Back" else "Create Account", 
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF172B4D)
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (authState is AuthState.Error) {
                Text((authState as AuthState.Error).message, color = Color(0xFFFF6B6B))
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = glassTextFieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = glassTextFieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (!isLogin) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = glassTextFieldColors
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isAdmin, 
                        onCheckedChange = { isAdmin = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF0052CC),
                            checkmarkColor = Color.White,
                            uncheckedColor = Color(0xFFDFE1E6)
                        )
                    )
                    Text("Register as Company Admin", color = Color(0xFF172B4D))
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                if (isAdmin) {
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company Name") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = "Business") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = glassTextFieldColors
                    )
                } else {
                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it },
                        label = { Text("Company Invite Code") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = glassTextFieldColors
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color(0xFF0052CC))
            } else {
                Button(
                    onClick = {
                        if (isLogin) {
                            viewModel.login(email, password)
                        } else {
                            if (isAdmin) {
                                viewModel.signUpAdmin(email, password, name, companyName)
                            } else {
                                viewModel.signUpEmployee(email, password, name, inviteCode)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC), contentColor = Color.White)
                ) {
                    Text(if (isLogin) "Login" else "Sign Up", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { isLogin = !isLogin }) {
                Text(
                    if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Login",
                    color = Color(0xFF0052CC)
                )
            }
        }
    }
}