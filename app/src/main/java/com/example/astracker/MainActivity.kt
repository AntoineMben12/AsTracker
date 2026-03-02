package com.example.astracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.example.astracker.network.SupabaseClientConfig
import com.example.astracker.ui.assignment.AddAssignmentScreen
import com.example.astracker.ui.assignment.AssignmentListScreen
import com.example.astracker.ui.assignment.AssignmentViewModel
import com.example.astracker.ui.deadline.DeadlineScreen
import com.example.astracker.ui.login.AuthViewModel
import com.example.astracker.ui.login.LoginScreen
import com.example.astracker.ui.login.RegisterScreen
import com.example.astracker.ui.notification.NotificationScreen
import com.example.astracker.ui.notification.NotificationViewModel
import com.example.astracker.ui.profile.ProfileScreen
import com.example.astracker.ui.profile.ProfileViewModel
import com.example.astracker.ui.theme.AsTrackerTheme
import io.github.jan.supabase.auth.auth

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
    val app = LocalContext.current.applicationContext as AsTrackerApp
    val factory = AppViewModelFactory(app)

    // ── Shared ViewModels ─────────────────────────────────────────────────────
    // Created once at the NavGraph scope so all screens share the same state.
    // AssignmentViewModel is intentionally shared between AssignmentListScreen
    // and AddAssignmentScreen so the list refreshes automatically after a save.
    val authViewModel:         AuthViewModel         = viewModel(factory = factory)
    val assignmentViewModel:   AssignmentViewModel   = viewModel(factory = factory)
    val notificationViewModel: NotificationViewModel = viewModel(factory = factory)
    val profileViewModel:      ProfileViewModel      = viewModel(factory = factory)

    // ── Session restoration ───────────────────────────────────────────────────
    // If the Supabase SDK still holds a valid session (persisted on-device),
    // skip the login screen and go straight to the assignment list.
    LaunchedEffect(Unit) {
        val session = SupabaseClientConfig.client.auth.currentSessionOrNull()
        if (session != null) {
            navController.navigate("assignmentList") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // ── Navigation graph ──────────────────────────────────────────────────────
    NavHost(navController = navController, startDestination = "login") {

        // ── Login ─────────────────────────────────────────────────────────────
        composable("login") {
            LoginScreen(
                viewModel            = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToAssignmentList = {
                    navController.navigate("assignmentList") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // ── Register ──────────────────────────────────────────────────────────
        composable("register") {
            RegisterScreen(
                viewModel          = authViewModel,
                onNavigateToLogin  = { navController.popBackStack() },
                onNavigateToAssignmentList = {
                    navController.navigate("assignmentList") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // ── Assignment list ───────────────────────────────────────────────────
        composable("assignmentList") {
            AssignmentListScreen(
                viewModel                 = assignmentViewModel,
                onNavigateToLogin         = {
                    navController.navigate("login") {
                        popUpTo("assignmentList") { inclusive = true }
                    }
                },
                onNavigateToAddAssignment = { navController.navigate("addAssignment") },
                onNavigateToDeadline      = { navController.navigate("deadline") },
                onNavigateToNotification  = { navController.navigate("notification") },
                onNavigateToProfile       = { navController.navigate("profile") }
            )
        }

        // ── Add assignment ────────────────────────────────────────────────────
        // Reuses the same AssignmentViewModel instance so the list auto-refreshes
        // after a successful save.
        composable("addAssignment") {
            AddAssignmentScreen(
                viewModel = assignmentViewModel,
                onBack    = { navController.popBackStack() }
            )
        }

        // ── Deadline / calendar ───────────────────────────────────────────────
        composable("deadline") {
            DeadlineScreen(
                viewModel                = assignmentViewModel,
                onBack                   = { navController.popBackStack() },
                onNavigateToTasks        = {
                    navController.navigate("assignmentList") {
                        popUpTo("deadline") { inclusive = true }
                    }
                },
                onNavigateToAddAssignment = { navController.navigate("addAssignment") },
                onNavigateToNotification  = { navController.navigate("notification") },
                onNavigateToProfile       = { navController.navigate("profile") }
            )
        }

        // ── Notifications ─────────────────────────────────────────────────────
        composable("notification") {
            NotificationScreen(
                viewModel            = notificationViewModel,
                onBack               = { navController.popBackStack() },
                onNavigateToTasks    = {
                    navController.navigate("assignmentList") {
                        popUpTo("notification") { inclusive = true }
                    }
                },
                onNavigateToCalendar = { navController.navigate("deadline") },
                onNavigateToProfile  = { navController.navigate("profile") }
            )
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable("profile") {
            ProfileScreen(
                viewModel              = profileViewModel,
                onBack                 = { navController.popBackStack() },
                onLogout               = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToTasks      = {
                    navController.navigate("assignmentList") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                onNavigateToCalendar     = { navController.navigate("deadline") },
                onNavigateToNotification = { navController.navigate("notification") }
            )
        }
    }
}
