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

data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val totalMarkets: Int = 0,
    val todayPriceEntries: Int = 0,
    val totalFarmers: Int = 0,
    val error: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState(isLoading = true))
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        refreshDashboardStats()
    }

    fun refreshDashboardStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val stats = adminRepository.fetchDashboardStats()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalMarkets = stats.totalMarkets,
                        todayPriceEntries = stats.todayPriceEntries,
                        totalFarmers = stats.totalFarmers
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Unable to load dashboard data."
                    )
                }
            }
        }
    }
}
