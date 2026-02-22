package com.example.astracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.astracker.data.AssignmentRepository
import com.example.astracker.data.AuthRepository
import com.example.astracker.data.NotificationRepository
import com.example.astracker.data.ProfileRepository
import com.example.astracker.ui.assignment.AssignmentViewModel
import com.example.astracker.ui.login.AuthViewModel
import com.example.astracker.ui.notification.NotificationViewModel
import com.example.astracker.ui.profile.ProfileViewModel

class AppViewModelFactory(private val app: AsTrackerApp) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(AuthRepository(app.tokenManager)) as T
            modelClass.isAssignableFrom(AssignmentViewModel::class.java) ->
                AssignmentViewModel(AssignmentRepository()) as T
            modelClass.isAssignableFrom(NotificationViewModel::class.java) ->
                NotificationViewModel(NotificationRepository()) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(ProfileRepository(), AuthRepository(app.tokenManager)) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
