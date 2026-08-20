package com.parikiganesh.tomato365.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.tomato365.repository.AdminAuthErrorReason
import com.parikiganesh.tomato365.repository.AdminAuthResult
import com.parikiganesh.tomato365.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminLoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: AdminEmailError? = null,
    val passwordError: AdminPasswordError? = null,
    val authError: AdminAuthError? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false
)

enum class AdminEmailError {
    REQUIRED,
    INVALID
}

enum class AdminPasswordError {
    REQUIRED
}

enum class AdminAuthError {
    NO_ADMIN_ACCESS,
    LOGIN_FAILED
}

@HiltViewModel
class AdminLoginViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminLoginUiState())
    val uiState: StateFlow<AdminLoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = null,
                authError = null
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = null,
                authError = null
            )
        }
    }

    fun onLoginClicked() {
        val currentState = _uiState.value
        val email = currentState.email.trim()
        val password = currentState.password

        var emailError: AdminEmailError? = null
        var passwordError: AdminPasswordError? = null

        if (email.isEmpty()) {
            emailError = AdminEmailError.REQUIRED
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = AdminEmailError.INVALID
        }
        if (password.isBlank()) {
            passwordError = AdminPasswordError.REQUIRED
        }

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }
            when (val result = adminRepository.signInAndVerifyAdmin(email, password)) {
                AdminAuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                }
                is AdminAuthResult.Error -> {
                    val error = when (result.reason) {
                        AdminAuthErrorReason.NO_ADMIN_ACCESS -> AdminAuthError.NO_ADMIN_ACCESS
                        AdminAuthErrorReason.AUTH_FAILED -> AdminAuthError.LOGIN_FAILED
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            authError = error
                        )
                    }
                }
            }
        }
    }

    fun onLoginNavigated() {
        _uiState.update { it.copy(loginSuccess = false) }
    }
}
