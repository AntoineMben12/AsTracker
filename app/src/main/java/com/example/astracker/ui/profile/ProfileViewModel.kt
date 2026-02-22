package com.example.astracker.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astracker.data.AuthRepository
import com.example.astracker.data.ProfileRepository
import com.example.astracker.network.models.ProfileDataDto
import com.example.astracker.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepo: ProfileRepository = ProfileRepository(),
    private val authRepo: AuthRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ProfileDataDto>>(UiState.Idle)
    val state: StateFlow<UiState<ProfileDataDto>> = _state

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val result = profileRepo.getProfile()
            _state.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load profile") }
            )
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepo?.logout()
            onComplete()
        }
    }
}
