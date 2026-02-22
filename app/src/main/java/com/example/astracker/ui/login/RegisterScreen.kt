package com.example.astracker.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.astracker.ui.common.UiState
import com.example.astracker.ui.login.AuthViewModel
import com.example.astracker.ui.login.BackgroundDark
import com.example.astracker.ui.login.BackgroundLight
import com.example.astracker.ui.login.AnimatedGradientBackground
import com.example.astracker.ui.login.PrimaryColor
import com.example.astracker.ui.login.PulsingBlobs
import com.example.astracker.ui.login.ThemeToggleButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astracker.ui.theme.AsTrackerTheme

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToAssignmentList: () -> Unit
) {
    var isDarkTheme by remember { mutableStateOf(false) }
    val registerState by viewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is UiState.Success -> {
                viewModel.resetRegisterState()
                onNavigateToAssignmentList()
            }
            is UiState.Error -> snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }

    AsTrackerTheme(darkTheme = isDarkTheme) {
        val backgroundColor = if (isDarkTheme) BackgroundDark else BackgroundLight

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = backgroundColor
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                AnimatedGradientBackground()
                PulsingBlobs(isDarkTheme)

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    RegisterForm(
                        isDarkTheme = isDarkTheme,
                        isLoading = registerState is UiState.Loading,
                        onNavigateToLogin = onNavigateToLogin,
                        onSignUp = { name, email, password ->
                            viewModel.register(name, email, password)
                        }
                    )
                }

                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onToggle = { isDarkTheme = !isDarkTheme },
                    modifier = Modifier.align(Alignment.TopEnd).padding(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterForm(
    isDarkTheme: Boolean,
    isLoading: Boolean = false,
    onNavigateToLogin: () -> Unit,
    onSignUp: (String, String, String) -> Unit
) {
    var fullname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val surfaceColor = if (isDarkTheme) SurfaceDark else SurfaceLight
    val contentColor = if (isDarkTheme) Color.White else Color(0xFF111827)
    val secondaryTextColor = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val inputBgColor = if (isDarkTheme) InputDark else InputLight
    val borderColor = if (isDarkTheme) Color(0xFF374151) else Color(0xFFE5E7EB)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Join us to manage your assignments",
                fontSize = 14.sp,
                color = secondaryTextColor,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Full Name Field
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                Text(
                    text = "Full Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color(0xFFD1D5DB) else Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
                OutlinedTextField(
                    value = fullname,
                    onValueChange = { fullname = it },
                    placeholder = { Text("John Doe", color = Color(0xFF9CA3AF)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputBgColor,
                        unfocusedContainerColor = inputBgColor,
                        focusedIndicatorColor = PrimaryColor,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = PrimaryColor,
                        focusedTextColor = contentColor,
                        unfocusedTextColor = contentColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, if(isDarkTheme) Color(0xFF374151) else Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                )
            }

            // Email Field
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                Text(
                    text = "Email Address",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color(0xFFD1D5DB) else Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("student@university.edu", color = Color(0xFF9CA3AF)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputBgColor,
                        unfocusedContainerColor = inputBgColor,
                        focusedIndicatorColor = PrimaryColor,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = PrimaryColor,
                        focusedTextColor = contentColor,
                        unfocusedTextColor = contentColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, if(isDarkTheme) Color(0xFF374151) else Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                )
            }

            // Password Field
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                Text(
                    text = "Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color(0xFFD1D5DB) else Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("••••••••", color = Color(0xFF9CA3AF)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF9CA3AF)) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(image, contentDescription = if (passwordVisible) "Hide password" else "Show password", tint = Color(0xFF9CA3AF))
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputBgColor,
                        unfocusedContainerColor = inputBgColor,
                        focusedIndicatorColor = PrimaryColor,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = PrimaryColor,
                        focusedTextColor = contentColor,
                        unfocusedTextColor = contentColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, if(isDarkTheme) Color(0xFF374151) else Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                )
            }

            // Confirm Password Field
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                Text(
                    text = "Confirm Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color(0xFFD1D5DB) else Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("••••••••", color = Color(0xFF9CA3AF)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF9CA3AF)) },
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(image, contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password", tint = Color(0xFF9CA3AF))
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputBgColor,
                        unfocusedContainerColor = inputBgColor,
                        focusedIndicatorColor = PrimaryColor,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = PrimaryColor,
                        focusedTextColor = contentColor,
                        unfocusedTextColor = contentColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, if(isDarkTheme) Color(0xFF374151) else Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                )
            }

            // Sign Up Button
            Button(
                onClick = { onSignUp(fullname, email, password) },
                enabled = !isLoading && confirmPassword == password,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text("Sign Up", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
            
            // Sign In Link
            Row(
                modifier = Modifier.padding(top = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = secondaryTextColor
                )
                Text(
                    text = "Sign in",
                    fontSize = 14.sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

// Preview removed — RegisterScreen now requires AuthViewModel
