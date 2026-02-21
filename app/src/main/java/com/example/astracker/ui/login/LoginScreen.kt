package com.example.astracker.ui.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astracker.ui.theme.AsTrackerTheme

// Custom Colors from CSS
val PrimaryColor = Color(0xFFEF4444)
val BackgroundLight = Color(0xFFF3F4F6)
val BackgroundDark = Color(0xFF111827)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1F2937)
val InputLight = Color(0xFFF9FAFB)
val InputDark = Color(0xFF374151)

// Gradient Colors
val Gradient1 = Color(0xFFee7752)
val Gradient2 = Color(0xFFe73c7e)
val Gradient3 = Color(0xFF23a6d5)
val Gradient4 = Color(0xFF23d5ab)

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToAssignmentList: () -> Unit
) {
    var isDarkTheme by remember { mutableStateOf(false) } // Local state for demo, ideally lifted up

    AsTrackerTheme(darkTheme = isDarkTheme) {
        val backgroundColor = if (isDarkTheme) BackgroundDark else BackgroundLight
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // Animated Background
            AnimatedGradientBackground()
            
            // Blobs
            PulsingBlobs(isDarkTheme)

            // Content
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoginForm(isDarkTheme, onNavigateToRegister, onNavigateToAssignmentList)
            }

            // Theme Toggle
            ThemeToggleButton(
                isDarkTheme = isDarkTheme,
                onToggle = { isDarkTheme = !isDarkTheme },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
            )
        }
    }
}

@Composable
fun AnimatedGradientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {

                val brush = Brush.linearGradient(
                    colors = listOf(Gradient1, Gradient2, Gradient3, Gradient4),
                    start = Offset(size.width * offset, 0f),
                    end = Offset(size.width * (offset + 1), size.height),
                    tileMode = TileMode.Mirror
                )
                drawRect(brush = brush, alpha = 0.05f) // Subtle overlay
            }
    )
}

@Composable
fun PulsingBlobs(isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobScale"
    )

    val primaryWithAlpha = PrimaryColor.copy(alpha = if (isDarkTheme) 0.1f else 0.2f)
    val blueWithAlpha = Color(0xFF3B82F6).copy(alpha = if (isDarkTheme) 0.1f else 0.2f)

    Box(modifier = Modifier.fillMaxSize()) {
        // Top Left Blob
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = (-50).dp)
                .size(200.dp)
                .background(primaryWithAlpha, CircleShape)
                .blur(80.dp)
        )
        
        // Bottom Right Blob
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = 50.dp)
                .size(200.dp)
                .background(blueWithAlpha, CircleShape)
                .blur(80.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm(
    isDarkTheme: Boolean,
    onNavigateToRegister: () -> Unit,
    onNavigateToAssignmentList: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

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
                text = "Welcome Back",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Please enter your details to sign in",
                fontSize = 14.sp,
                color = secondaryTextColor,
                modifier = Modifier.padding(bottom = 32.dp)
            )

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
                        unfocusedIndicatorColor = Color.Transparent, // Mimic border-gray-200 behavior
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

            // Remember Me & Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryColor,
                        checkmarkColor = Color.White,
                        uncheckedColor = Color(0xFFD1D5DB)
                    )
                )
                Text(
                    text = "Remember me",
                    fontSize = 14.sp,
                    color = secondaryTextColor
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Forgot Password?",
                    fontSize = 14.sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { /* Handle forgot password */ }
                )
            }

            // Sign In Button
            Button(
                onClick = {
                    // Simulate login validation
                    onNavigateToAssignmentList()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            
            // Sign Up Link
            Row(
                modifier = Modifier.padding(top = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 14.sp,
                    color = secondaryTextColor
                )
                Text(
                    text = "Sign up now",
                    fontSize = 14.sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}

@Composable
fun ThemeToggleButton(
    isDarkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onToggle,
        containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight,
        contentColor = if (isDarkTheme) Color.White else Color(0xFF1F2937),
        shape = CircleShape,
        modifier = modifier.size(50.dp)
    ) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = "Toggle Theme"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onNavigateToRegister = {}, onNavigateToAssignmentList = {})
}
