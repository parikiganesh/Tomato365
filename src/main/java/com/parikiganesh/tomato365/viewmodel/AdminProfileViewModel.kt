package com.parikiganesh.tomato365.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.tomato365.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminProfileUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val isActive: Boolean = true,
    val error: String? = null,
    val loggedOut: Boolean = false
)

@HiltViewModel
class AdminProfileViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProfileUiState(isLoading = true))
    val uiState: StateFlow<AdminProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val profile = adminRepository.fetchAdminProfile()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = profile.name,
                        email = profile.email,
                        role = profile.role,
                        isActive = profile.isActive
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Unable to load profile."
                    )
                }
            }
        }
    }

    fun logout() {
        adminRepository.signOut()
        _uiState.update { it.copy(loggedOut = true) }
    }

    fun onLoggedOutHandled() {
        _uiState.update { it.copy(loggedOut = false) }
    }
}
