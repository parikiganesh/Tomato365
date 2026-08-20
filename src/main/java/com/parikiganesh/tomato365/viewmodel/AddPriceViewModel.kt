package com.parikiganesh.tomato365.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.tomato365.repository.PriceRepository
import com.parikiganesh.tomato365.repository.SavePriceInput
import com.parikiganesh.tomato365.repository.SelectionOption
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TOMATO_LOCAL_VARIETY_ID = "tomato_local"

data class AddPriceUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val markets: List<SelectionOption> = emptyList(),
    val selectedMarketId: String = "",
    val selectedBoxTypeId: String = "",
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val dateDisplay: String = "",
    val lowestRate: String = "",
    val topRate: String = "",
    val marketError: AddPriceFieldError? = null,
    val boxTypeError: AddPriceFieldError? = null,
    val lowestRateError: AddPriceFieldError? = null,
    val topRateError: AddPriceFieldError? = null,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

enum class AddPriceFieldError {
    REQUIRED,
    INVALID,
    NEGATIVE,
    LOWEST_GT_TOP
}

@HiltViewModel
class AddPriceViewModel @Inject constructor(
    private val priceRepository: PriceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPriceUiState(isLoading = true))
    val uiState: StateFlow<AddPriceUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val todayMillis = System.currentTimeMillis()
                val markets = priceRepository.getActiveMarkets()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        markets = markets,
                        selectedDateMillis = todayMillis,
                        dateDisplay = priceRepository.formatDateForDisplay(todayMillis)
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Unable to load market data."
                    )
                }
            }
        }
    }

    fun onMarketSelected(marketId: String) {
        _uiState.update { it.copy(selectedMarketId = marketId, marketError = null, error = null) }
    }

    fun onBoxTypeSelected(boxTypeId: String) {
        _uiState.update { it.copy(selectedBoxTypeId = boxTypeId, boxTypeError = null, error = null) }
    }

    fun onLowestRateChanged(value: String) {
        _uiState.update { it.copy(lowestRate = value, lowestRateError = null, error = null) }
    }

    fun onTopRateChanged(value: String) {
        _uiState.update { it.copy(topRate = value, topRateError = null, error = null) }
    }

    fun savePrice() {
        val current = _uiState.value
        val lowestValue = current.lowestRate.toDoubleOrNull()
        val topValue = current.topRate.toDoubleOrNull()

        var marketError: AddPriceFieldError? = null
        var boxError: AddPriceFieldError? = null
        var lowestError: AddPriceFieldError? = null
        var topError: AddPriceFieldError? = null

        if (current.selectedMarketId.isBlank()) marketError = AddPriceFieldError.REQUIRED
        if (current.selectedBoxTypeId.isBlank()) boxError = AddPriceFieldError.REQUIRED

        if (current.lowestRate.isBlank()) {
            lowestError = AddPriceFieldError.REQUIRED
        } else if (lowestValue == null) {
            lowestError = AddPriceFieldError.INVALID
        } else if (lowestValue < 0) {
            lowestError = AddPriceFieldError.NEGATIVE
        }

        if (current.topRate.isBlank()) {
            topError = AddPriceFieldError.REQUIRED
        } else if (topValue == null) {
            topError = AddPriceFieldError.INVALID
        } else if (topValue < 0) {
            topError = AddPriceFieldError.NEGATIVE
        }

        if (lowestError == null && topError == null && lowestValue != null && topValue != null && lowestValue > topValue) {
            lowestError = AddPriceFieldError.LOWEST_GT_TOP
        }

        if (marketError != null || boxError != null || lowestError != null || topError != null) {
            _uiState.update {
                it.copy(
                    marketError = marketError,
                    boxTypeError = boxError,
                    lowestRateError = lowestError,
                    topRateError = topError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val date = priceRepository.formatDateForStorage(current.selectedDateMillis)
                val selectedMarketName = current.markets
                    .firstOrNull { it.id == current.selectedMarketId }
                    ?.name
                    .orEmpty()
                priceRepository.savePrice(
                    SavePriceInput(
                        marketId = current.selectedMarketId,
                        marketName = selectedMarketName,
                        varietyId = TOMATO_LOCAL_VARIETY_ID,
                        date = date,
                        minPrice = lowestValue!!,
                        maxPrice = topValue!!,
                        boxTypeKg = current.selectedBoxTypeId.toInt()
                    )
                )
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = exception.message ?: "Failed to save price. Please try again."
                    )
                }
            }
        }
    }

    fun onSaveSuccessHandled() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
