package com.example.astracker.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astracker.data.AuthRepository
import com.example.astracker.network.models.AuthResponse
import com.example.astracker.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<AuthResponse>> = _loginState

    private val _registerState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val registerState: StateFlow<UiState<AuthResponse>> = _registerState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = UiState.Error("Email and password are required")
            return
        }
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            val result = authRepository.login(email.trim(), password)
            _loginState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun register(
        name: String, email: String, password: String,
        major: String = "", year: Int = 1
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _registerState.value = UiState.Error("Please fill in all required fields")
            return
        }
        if (password.length < 6) {
            _registerState.value = UiState.Error("Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            val result = authRepository.register(name.trim(), email.trim(), password, major, year)
            _registerState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun resetLoginState() { _loginState.value = UiState.Idle }
    fun resetRegisterState() { _registerState.value = UiState.Idle }

    suspend fun logout() {
        authRepository.logout()
    }
}
