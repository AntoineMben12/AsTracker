package com.example.astracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.astracker.ui.assignment.AddAssignmentScreen
import com.example.astracker.ui.assignment.AssignmentListScreen
import com.example.astracker.ui.deadline.DeadlineScreen
import com.example.astracker.ui.login.LoginScreen
import com.example.astracker.ui.login.RegisterScreen
import com.example.astracker.ui.notification.NotificationScreen
import com.example.astracker.ui.profile.ProfileScreen
import com.example.astracker.ui.theme.AsTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AsTrackerTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToAssignmentList = { 
                    navController.navigate("assignmentList") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToAssignmentList = {
                    navController.navigate("assignmentList") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
        composable("assignmentList") {
            AssignmentListScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("assignmentList") { inclusive = true }
                    }
                },
                onNavigateToAddAssignment = {
                    navController.navigate("addAssignment")
                },
                onNavigateToDeadline = {
                    navController.navigate("deadline")
                },
                onNavigateToNotification = {
                    navController.navigate("notification")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        composable("addAssignment") {
            AddAssignmentScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("deadline") {
            DeadlineScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTasks = {
                    navController.navigate("assignmentList") {
                        popUpTo("deadline") { inclusive = true }
                    }
                },
                onNavigateToAddAssignment = {
                    navController.navigate("addAssignment")
                },
                onNavigateToNotification = {
                    navController.navigate("notification")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        composable("notification") {
            NotificationScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTasks = {
                    navController.navigate("assignmentList") {
                        popUpTo("notification") { inclusive = true }
                    }
                },
                onNavigateToCalendar = {
                    navController.navigate("deadline")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToTasks = {
                    navController.navigate("assignmentList") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                onNavigateToCalendar = {
                    navController.navigate("deadline")
                },
                onNavigateToNotification = {
                    navController.navigate("notification")
                }
            )
        }
    }
}
