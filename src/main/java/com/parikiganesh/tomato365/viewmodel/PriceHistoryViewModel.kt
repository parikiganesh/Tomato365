package com.parikiganesh.tomato365.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.tomato365.repository.PriceRepository
import com.parikiganesh.tomato365.repository.SelectionOption
import com.parikiganesh.tomato365.utils.toWholeNumberString
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TOMATO_LOCAL_VARIETY_ID = "tomato_local"

data class PriceHistoryUiState(
    val isLoading: Boolean = false,
    val markets: List<SelectionOption> = emptyList(),
    val varieties: List<SelectionOption> = listOf(
        SelectionOption(
            id = TOMATO_LOCAL_VARIETY_ID,
            name = ""
        )
    ),
    val selectedMarket: SelectionOption? = null,
    val selectedVariety: SelectionOption? = SelectionOption(
        id = TOMATO_LOCAL_VARIETY_ID,
        name = ""
    ),
    val startDate: String = "",
    val endDate: String = "",
    val displayRange: String = "",
    val selectedRangeDays: Int = 7,
    val history: List<HistoryRowUi> = emptyList(),
    val box15Count: Int = 0,
    val box30Count: Int = 0,
    val minPrice: String = "0",
    val maxPrice: String = "0",
    val modalPrice: String = "0",
    val error: String? = null
)

data class HistoryRowUi(
    val date: String,
    val boxTypeKg: Int,
    val min: String,
    val max: String,
    val modal: String
)

@HiltViewModel
class PriceHistoryViewModel @Inject constructor(
    private val priceRepository: PriceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PriceHistoryUiState(isLoading = true))
    val uiState: StateFlow<PriceHistoryUiState> = _uiState.asStateFlow()

    private val dateFormatStorage = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dateFormatDisplay = SimpleDateFormat("dd MMM yyyy", Locale.US)

    init {
        applyRange(days = 7)
    }

    fun loadInitialData(initialMarketName: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val markets = priceRepository.getActiveMarkets()
                val tomatoLocal = SelectionOption(
                    id = TOMATO_LOCAL_VARIETY_ID,
                    name = ""
                )
                val initialMarket = if (initialMarketName != null) {
                    markets.find { it.name == initialMarketName } ?: markets.firstOrNull()
                } else {
                    null
                }

                _uiState.update {
                    it.copy(
                        markets = markets,
                        varieties = listOf(tomatoLocal),
                        selectedMarket = initialMarket,
                        selectedVariety = tomatoLocal
                    )
                }
                if (initialMarketName != null && initialMarket == null) {
                    _uiState.update { it.copy(isLoading = false, history = emptyList()) }
                } else {
                    loadHistory()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadHistory() {
        val marketId = _uiState.value.selectedMarket?.id
        val start = _uiState.value.startDate
        val end = _uiState.value.endDate

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val rawHistory = priceRepository.getPriceHistoryByDateRange(
                    startDate = start,
                    endDate = end,
                    marketId = marketId
                )
                
                val historyRows = rawHistory.map { price ->
                    val computedModal = if (price.modalPrice > 0.0) {
                        price.modalPrice
                    } else {
                        (price.minPrice + price.maxPrice) / 2.0
                    }
                    HistoryRowUi(
                        date = try {
                            val dateObj = dateFormatStorage.parse(price.date)
                            if (dateObj != null) dateFormatDisplay.format(dateObj) else price.date
                        } catch (e: Exception) {
                            price.date
                        },
                        boxTypeKg = price.boxTypeKg,
                        min = price.minPrice.toWholeNumberString(),
                        max = price.maxPrice.toWholeNumberString(),
                        modal = computedModal.toWholeNumberString()
                    )
                }

                val minVal = rawHistory.minOfOrNull { it.minPrice } ?: 0.0
                val maxVal = rawHistory.maxOfOrNull { it.maxPrice } ?: 0.0
                val modalAvg = if (rawHistory.isNotEmpty()) {
                    rawHistory.map { (it.minPrice + it.maxPrice) / 2.0 }.average()
                } else {
                    0.0
                }
                val box15Count = rawHistory.count { it.boxTypeKg == 15 }
                val box30Count = rawHistory.count { it.boxTypeKg == 30 }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        history = historyRows,
                        box15Count = box15Count,
                        box30Count = box30Count,
                        minPrice = minVal.toWholeNumberString(),
                        maxPrice = maxVal.toWholeNumberString(),
                        modalPrice = modalAvg.toWholeNumberString()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onMarketSelected(market: SelectionOption?) {
        _uiState.update { it.copy(selectedMarket = market) }
        loadHistory()
    }

    fun onVarietySelected(variety: SelectionOption) {
        _uiState.update {
            it.copy(
                selectedVariety = SelectionOption(
                    id = TOMATO_LOCAL_VARIETY_ID,
                    name = ""
                )
            )
        }
    }

    fun onRangeSelected(days: Int) {
        applyRange(days)
        loadHistory()
    }

    private fun applyRange(days: Int) {
        val calendar = Calendar.getInstance()
        val end = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -(days - 1))
        val start = calendar.time
        _uiState.update {
            it.copy(
                selectedRangeDays = days,
                startDate = dateFormatStorage.format(start),
                endDate = dateFormatStorage.format(end),
                displayRange = "${dateFormatDisplay.format(start)} - ${dateFormatDisplay.format(end)}"
            )
        }
    }
}
