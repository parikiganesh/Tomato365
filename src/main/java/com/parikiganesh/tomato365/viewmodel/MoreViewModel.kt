package com.parikiganesh.tomato365.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.tomato365.data.local.UserPreferencesDataStore
import com.parikiganesh.tomato365.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FarmerProfileUiState(
    val isLoading: Boolean = false,
    val farmerName: String = "",
    val selectedLanguageCode: String = "en",
    val preferredMarketName: String = "",
    val error: String? = null
)

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmerProfileUiState(isLoading = true))
    val uiState: StateFlow<FarmerProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                userPreferencesDataStore.userPreferences.collectLatest { prefs ->
                    val normalizedLanguage = normalizeLanguageCode(prefs.selectedLanguage)
                    val preferredMarketFromPrefs = prefs.preferredMarketName.ifBlank {
                        if (prefs.preferredMarketId.isNotBlank() && !looksLikeFirestoreId(prefs.preferredMarketId)) {
                            prefs.preferredMarketId
                        } else {
                            ""
                        }
                    }
                    val preferredMarketName = if (preferredMarketFromPrefs.isNotBlank()) {
                        preferredMarketFromPrefs
                    } else {
                        resolveMarketNameFromId(prefs.preferredMarketId)
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            farmerName = prefs.farmerName,
                            selectedLanguageCode = normalizedLanguage,
                            preferredMarketName = preferredMarketName,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load profile."
                    )
                }
            }
        }
    }

    private suspend fun resolveMarketNameFromId(preferredMarketId: String): String {
        if (preferredMarketId.isBlank()) return ""
        return try {
            val activeMarkets = marketRepository.getActiveMarkets()
            activeMarkets.firstOrNull { it.id == preferredMarketId }?.name
                ?: activeMarkets.firstOrNull { it.name == preferredMarketId }?.name
                .orEmpty()
        } catch (_: Exception) {
            if (!looksLikeFirestoreId(preferredMarketId)) preferredMarketId else ""
        }
    }

    private fun looksLikeFirestoreId(value: String): Boolean {
        val trimmed = value.trim()
        return trimmed.length >= 16 && trimmed.all { it.isLetterOrDigit() }
    }

    private fun normalizeLanguageCode(savedValue: String): String {
        return when (savedValue.trim().lowercase()) {
            "te", "telugu", "తెలుగు" -> "te"
            else -> "en"
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            userPreferencesDataStore.saveSelectedLanguage(languageCode)
        }
    }
}
