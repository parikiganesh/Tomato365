package com.parikiganesh.tomato365.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.tomato365.repository.PriceRepository
import com.parikiganesh.tomato365.repository.SelectionOption
import com.parikiganesh.tomato365.repository.UpdatePriceInput
import com.parikiganesh.tomato365.utils.toWholeNumberString
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ViewPricesUiState(
    val isLoading: Boolean = false,
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val selectedDate: String = "",
    val displayDate: String = "",
    val selectedMarketId: String = "",
    val markets: List<SelectionOption> = emptyList(),
    val prices: List<PriceRowUi> = emptyList(),
    val error: String? = null
)

data class PriceRowUi(
    val id: String,
    val marketName: String,
    val boxTypeKg: Int,
    val min: String,
    val max: String
)

@HiltViewModel
class ViewPricesViewModel @Inject constructor(
    private val priceRepository: PriceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewPricesUiState(isLoading = true))
    val uiState: StateFlow<ViewPricesUiState> = _uiState.asStateFlow()

    private val dateFormatStorage = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dateFormatDisplay = SimpleDateFormat("dd MMM yyyy", Locale.US)

    init {
        val todayMillis = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                selectedDateMillis = todayMillis,
                selectedDate = dateFormatStorage.format(Date(todayMillis)),
                displayDate = dateFormatDisplay.format(Date(todayMillis))
            )
        }
        loadPrices()
    }

    fun loadPrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val markets = priceRepository.getActiveMarkets()
                val marketMap = markets.associateBy { it.id }
                val rawPrices = priceRepository.getPricesByDate(_uiState.value.selectedDate)
                val filteredPrices = if (_uiState.value.selectedMarketId.isBlank()) {
                    rawPrices
                } else {
                    rawPrices.filter { it.marketId == _uiState.value.selectedMarketId }
                }
                val rows = filteredPrices.map { price ->
                    PriceRowUi(
                        id = price.id,
                        marketName = marketMap[price.marketId]?.name.orEmpty(),
                        boxTypeKg = price.boxTypeKg,
                        min = price.minPrice.toWholeNumberString(),
                        max = price.maxPrice.toWholeNumberString()
                    )
                }.sortedBy { it.marketName }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        markets = markets,
                        prices = rows
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load prices."
                    )
                }
            }
        }
    }

    fun onDateSelected(dateInMillis: Long) {
        _uiState.update {
            it.copy(
                selectedDateMillis = dateInMillis,
                selectedDate = dateFormatStorage.format(Date(dateInMillis)),
                displayDate = dateFormatDisplay.format(Date(dateInMillis))
            )
        }
        loadPrices()
    }

    fun onMarketSelected(marketId: String) {
        _uiState.update { it.copy(selectedMarketId = marketId) }
        loadPrices()
    }

    fun updatePrice(
        priceId: String,
        minPrice: Double,
        maxPrice: Double,
        boxTypeKg: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                priceRepository.updatePrice(
                    UpdatePriceInput(
                        priceId = priceId,
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        boxTypeKg = boxTypeKg
                    )
                )
                loadPrices()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deletePrice(priceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                priceRepository.deletePrice(priceId)
                loadPrices()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
