package com.parikiganesh.tomato365.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.tomato365.data.local.UserPreferencesDataStore
import com.parikiganesh.tomato365.repository.PriceRepository
import com.parikiganesh.tomato365.repository.SelectionOption
import com.parikiganesh.tomato365.utils.toInr
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

data class HomeUiState(
    val isLoading: Boolean = false,
    val markets: List<SelectionOption> = emptyList(),
    val selectedMarketId: String = "",
    val marketName: String = "",
    val date: String = "",
    val minPrice: String = "--",
    val maxPrice: String = "--",
    val boxTypeKg: Int = 0,
    val lastUpdated: String = "--",
    val priceChange: String = "",
    val priceChangePercent: String = "",
    val sevenDayTrend: List<HomeTrendEntry> = emptyList(),
    val isPositiveChange: Boolean = true,
    val error: String? = null
)

data class HomeTrendEntry(
    val dateStorage: String,
    val dateLabel: String,
    val minPriceLabel: String,
    val maxPriceLabel: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val priceRepository: PriceRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val dateFormatStorage = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dateFormatDisplay = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val timeFormatDisplay = SimpleDateFormat("hh:mm a", Locale.US)
    private val trendDateDisplayFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    fun loadTodaysPrice(marketName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val activeMarkets = priceRepository.getActiveMarkets()
                val selectedMarket = activeMarkets.find { it.name == marketName } ?: activeMarkets.firstOrNull()
                if (selectedMarket == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            markets = emptyList(),
                            selectedMarketId = "",
                            marketName = marketName,
                            error = "No markets found"
                        )
                    }
                    return@launch
                }

                val todayDate = Date()
                val todayStorage = dateFormatStorage.format(todayDate)
                val yesterdayDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
                val yesterdayStorage = dateFormatStorage.format(yesterdayDate)
                val sevenDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }.time
                val sevenDaysStart = dateFormatStorage.format(sevenDaysAgo)

                val todaysPrice = priceRepository.getPricesByDate(todayStorage)
                    .sortedByDescending { it.updatedAtMillis }
                    .find { it.marketId == selectedMarket.id }
                val yesterdaysPrice = priceRepository.getPricesByDate(yesterdayStorage)
                    .sortedByDescending { it.updatedAtMillis }
                    .find { it.marketId == selectedMarket.id }
                val sevenDayTrend = buildSevenDayTrend(
                    marketId = selectedMarket.id,
                    startDate = sevenDaysStart,
                    endDate = todayStorage
                )

                if (todaysPrice == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            markets = activeMarkets,
                            selectedMarketId = selectedMarket.id,
                            marketName = selectedMarket.name,
                            date = dateFormatDisplay.format(todayDate),
                            minPrice = "--",
                            maxPrice = "--",
                            lastUpdated = "--",
                            priceChange = "",
                            priceChangePercent = "",
                            sevenDayTrend = sevenDayTrend,
                            error = "No price updated for today yet"
                        )
                    }
                    return@launch
                }

                val currentAvg = (todaysPrice.minPrice + todaysPrice.maxPrice) / 2.0
                val previousAvg = yesterdaysPrice?.let { (it.minPrice + it.maxPrice) / 2.0 }
                val changeValue = if (previousAvg != null) currentAvg - previousAvg else null
                val changePercent = if (previousAvg != null && previousAvg != 0.0) {
                    (changeValue!! / previousAvg) * 100.0
                } else {
                    null
                }
                val isPositive = (changeValue ?: 0.0) >= 0
                val changePrefix = if (isPositive) "+" else "-"

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        markets = activeMarkets,
                        selectedMarketId = selectedMarket.id,
                        marketName = selectedMarket.name,
                        date = dateFormatDisplay.format(todayDate),
                        minPrice = todaysPrice.minPrice.toInr(),
                        maxPrice = todaysPrice.maxPrice.toInr(),
                        boxTypeKg = todaysPrice.boxTypeKg,
                        lastUpdated = if (todaysPrice.updatedAtMillis > 0L) {
                            timeFormatDisplay.format(Date(todaysPrice.updatedAtMillis))
                        } else {
                            "--"
                        },
                        priceChange = if (changeValue != null) {
                            "$changePrefix ${kotlin.math.abs(changeValue).toInr()}"
                        } else {
                            ""
                        },
                        priceChangePercent = if (changePercent != null) {
                            "($changePrefix ${kotlin.math.abs(changePercent).toWholeNumberString()}%)"
                        } else {
                            ""
                        },
                        sevenDayTrend = sevenDayTrend,
                        isPositiveChange = isPositive,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onMarketSelected(marketId: String) {
        val marketName = _uiState.value.markets.firstOrNull { it.id == marketId }?.name ?: return
        viewModelScope.launch {
            userPreferencesDataStore.savePreferredMarket(marketId, marketName)
        }
        loadTodaysPrice(marketName)
    }

    private suspend fun buildSevenDayTrend(
        marketId: String,
        startDate: String,
        endDate: String
    ): List<HomeTrendEntry> {
        val history = priceRepository.getPriceHistoryByDateRange(
            startDate = startDate,
            endDate = endDate,
            marketId = marketId
        )

        val latestByDate = history
            .groupBy { it.date }
            .mapValues { (_, prices) -> prices.maxByOrNull { it.updatedAtMillis } }

        return latestByDate.entries
            .sortedBy { it.key }
            .mapNotNull { (dateStorage, latestPrice) ->
                latestPrice ?: return@mapNotNull null
                HomeTrendEntry(
                    dateStorage = dateStorage,
                    dateLabel = formatTrendDate(dateStorage),
                    minPriceLabel = latestPrice.minPrice.toInr(),
                    maxPriceLabel = latestPrice.maxPrice.toInr()
                )
            }
    }

    private fun formatTrendDate(storageDate: String): String {
        val parsedDate = runCatching {
            dateFormatStorage.parse(storageDate)
        }.getOrNull()
        return if (parsedDate != null) trendDateDisplayFormat.format(parsedDate) else storageDate
    }
}
