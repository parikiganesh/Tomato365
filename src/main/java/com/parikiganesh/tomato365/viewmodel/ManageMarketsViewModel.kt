package com.parikiganesh.tomato365.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.tomato365.data.model.Market
import com.parikiganesh.tomato365.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageMarketsUiState(
    val markets: List<Market> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false
)

@HiltViewModel
class ManageMarketsViewModel @Inject constructor(
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageMarketsUiState())
    val uiState: StateFlow<ManageMarketsUiState> = _uiState.asStateFlow()

    init {
        loadMarkets()
    }

    fun loadMarkets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val markets = marketRepository.getMarkets()
                _uiState.update { it.copy(markets = markets, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun addMarket(name: String, district: String, state: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val newMarket = Market(name = name, district = district, state = state, isActive = true)
                marketRepository.addMarket(newMarket)
                _uiState.update { it.copy(showAddDialog = false) }
                loadMarkets()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteMarket(marketId: String) {
        viewModelScope.launch {
            try {
                marketRepository.deleteMarket(marketId)
                loadMarkets()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onAddMarketClick() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun onDismissDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }
}
