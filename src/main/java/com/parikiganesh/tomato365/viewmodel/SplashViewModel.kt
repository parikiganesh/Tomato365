package com.parikiganesh.tomato365.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.parikiganesh.tomato365.data.local.UserPreferencesDataStore
import com.parikiganesh.tomato365.notifications.NotificationTopics
import com.parikiganesh.tomato365.repository.FarmerRepository
import com.parikiganesh.tomato365.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SplashDestination {
    data object Welcome : SplashDestination
    data class Home(val marketName: String) : SplashDestination
}

data class SplashUiState(
    val destination: SplashDestination? = null
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val marketRepository: MarketRepository,
    private val farmerRepository: FarmerRepository,
    private val firebaseMessaging: FirebaseMessaging
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        resolveDestination()
    }

    private fun resolveDestination() {
        viewModelScope.launch {
            val prefs = userPreferencesDataStore.userPreferences.first()
            val farmerName = prefs.farmerName.trim()
            val preferredMarketName = resolvePreferredMarketName(
                preferredMarketId = prefs.preferredMarketId,
                preferredMarketName = prefs.preferredMarketName
            )

            val destination = if (farmerName.isNotBlank() && preferredMarketName.isNotBlank()) {
                try {
                    syncFarmerRegistration(
                        farmerName = farmerName,
                        selectedLanguage = prefs.selectedLanguage,
                        preferredMarketId = prefs.preferredMarketId,
                        preferredMarketName = preferredMarketName
                    )
                } catch (_: Exception) {
                    // Keep app startup resilient; registration will retry on next app open.
                }
                subscribeToPreferredMarketTopic(prefs.preferredMarketId)
                SplashDestination.Home(preferredMarketName)
            } else {
                SplashDestination.Welcome
            }
            _uiState.update { it.copy(destination = destination) }
        }
    }

    private suspend fun resolvePreferredMarketName(
        preferredMarketId: String,
        preferredMarketName: String
    ): String {
        if (preferredMarketName.isNotBlank()) return preferredMarketName
        if (preferredMarketId.isBlank()) return ""
        if (!looksLikeFirestoreId(preferredMarketId)) return preferredMarketId

        return try {
            marketRepository.getActiveMarkets()
                .firstOrNull { it.id == preferredMarketId }
                ?.name
                .orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    private fun looksLikeFirestoreId(value: String): Boolean {
        val trimmed = value.trim()
        return trimmed.length >= 16 && trimmed.all { it.isLetterOrDigit() }
    }

    private suspend fun syncFarmerRegistration(
        farmerName: String,
        selectedLanguage: String,
        preferredMarketId: String,
        preferredMarketName: String
    ) {
        val registrationId = userPreferencesDataStore.getOrCreateFarmerRegistrationId()
        farmerRepository.upsertFarmerRegistration(
            registrationId = registrationId,
            farmerName = farmerName,
            languageCode = selectedLanguage,
            preferredMarketId = preferredMarketId,
            preferredMarketName = preferredMarketName
        )
    }

    private fun subscribeToPreferredMarketTopic(marketId: String) {
        if (marketId.isBlank()) return
        firebaseMessaging.subscribeToTopic(NotificationTopics.marketTopic(marketId))
    }
}
