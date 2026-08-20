package com.parikiganesh.tomato365.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.parikiganesh.tomato365.data.local.UserPreferencesDataStore
import com.parikiganesh.tomato365.data.model.Market
import com.parikiganesh.tomato365.notifications.NotificationTopics
import com.parikiganesh.tomato365.repository.FarmerRepository
import com.parikiganesh.tomato365.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MarketSelectionUiState(
    val markets: List<Market> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MarketSelectionViewModel @Inject constructor(
    private val marketRepository: MarketRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val farmerRepository: FarmerRepository,
    private val firebaseMessaging: FirebaseMessaging
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketSelectionUiState())
    val uiState: StateFlow<MarketSelectionUiState> = _uiState.asStateFlow()

    init {
        loadMarkets()
    }

    fun loadMarkets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val markets = marketRepository.getActiveMarkets()
                _uiState.update { it.copy(markets = markets, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun savePreferredMarket(
        marketId: String,
        marketName: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val previousPreferences = userPreferencesDataStore.userPreferences.first()
            userPreferencesDataStore.savePreferredMarket(marketId, marketName)
            try {
                val preferences = userPreferencesDataStore.userPreferences.first()
                if (preferences.farmerName.isNotBlank()) {
                    val registrationId = userPreferencesDataStore.getOrCreateFarmerRegistrationId()
                    farmerRepository.upsertFarmerRegistration(
                        registrationId = registrationId,
                        farmerName = preferences.farmerName,
                        languageCode = preferences.selectedLanguage,
                        preferredMarketId = marketId,
                        preferredMarketName = marketName
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = exception.message) }
            }
            updateMarketTopicSubscription(
                previousMarketId = previousPreferences.preferredMarketId,
                currentMarketId = marketId
            )
            onSaved()
        }
    }

    private fun updateMarketTopicSubscription(
        previousMarketId: String,
        currentMarketId: String
    ) {
        if (previousMarketId.isNotBlank() && previousMarketId != currentMarketId) {
            firebaseMessaging.unsubscribeFromTopic(NotificationTopics.marketTopic(previousMarketId))
        }
        if (currentMarketId.isNotBlank()) {
            firebaseMessaging.subscribeToTopic(NotificationTopics.marketTopic(currentMarketId))
        }
    }
}
