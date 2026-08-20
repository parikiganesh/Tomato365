package com.parikiganesh.tomato365.ui.screens.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.repository.SelectionOption
import com.parikiganesh.tomato365.ui.components.AppDropdownField
import com.parikiganesh.tomato365.ui.components.AppNumberField
import com.parikiganesh.tomato365.ui.components.AppPrimaryButton
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme
import com.parikiganesh.tomato365.viewmodel.AddPriceFieldError
import com.parikiganesh.tomato365.viewmodel.AddPriceUiState
import com.parikiganesh.tomato365.viewmodel.AddPriceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPriceScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddPriceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(R.string.price_saved_success)

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(message = successMessage)
            viewModel.onSaveSuccessHandled()
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.add_update_price_admin)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        AddPriceContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onSelectMarket = viewModel::onMarketSelected,
            onSelectBoxType = viewModel::onBoxTypeSelected,
            onLowestRateChanged = viewModel::onLowestRateChanged,
            onTopRateChanged = viewModel::onTopRateChanged,
            onSaveClick = viewModel::savePrice
        )
    }
}

@Composable
private fun AddPriceContent(
    modifier: Modifier = Modifier,
    uiState: AddPriceUiState,
    onSelectMarket: (String) -> Unit,
    onSelectBoxType: (String) -> Unit,
    onLowestRateChanged: (String) -> Unit,
    onTopRateChanged: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    val boxTypeOptions = listOf(
        SelectionOption(id = "15", name = stringResource(R.string.box_15kg)),
        SelectionOption(id = "30", name = stringResource(R.string.box_30kg))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AppDropdownField(
            label = stringResource(R.string.select_market),
            selectedId = uiState.selectedMarketId,
            options = uiState.markets,
            errorMessage = uiState.marketError?.toErrorMessage(),
            placeholder = stringResource(R.string.choose_market),
            optionLeadingIcon = Icons.Outlined.LocationOn,
            onSelected = onSelectMarket
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = stringResource(R.string.tomato_local),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = stringResource(R.string.select_variety)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.dateDisplay,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = stringResource(R.string.date)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        AppDropdownField(
            label = stringResource(R.string.box_type),
            selectedId = uiState.selectedBoxTypeId,
            options = boxTypeOptions,
            errorMessage = uiState.boxTypeError?.toErrorMessage(),
            onSelected = onSelectBoxType
        )
        Spacer(modifier = Modifier.height(12.dp))

        AppNumberField(
            label = stringResource(R.string.lowest_rate),
            value = uiState.lowestRate,
            errorMessage = uiState.lowestRateError?.toErrorMessage(),
            onValueChange = onLowestRateChanged
        )
        Spacer(modifier = Modifier.height(12.dp))

        AppNumberField(
            label = stringResource(R.string.top_rate),
            value = uiState.topRate,
            errorMessage = uiState.topRateError?.toErrorMessage(),
            onValueChange = onTopRateChanged
        )
        Spacer(modifier = Modifier.height(18.dp))

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        AppPrimaryButton(
            text = stringResource(R.string.save_price),
            isLoading = uiState.isSaving,
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AddPriceFieldError.toErrorMessage(): String {
    return when (this) {
        AddPriceFieldError.REQUIRED -> stringResource(R.string.error_field_required)
        AddPriceFieldError.INVALID -> stringResource(R.string.error_invalid_number)
        AddPriceFieldError.NEGATIVE -> stringResource(R.string.error_negative_price)
        AddPriceFieldError.LOWEST_GT_TOP -> stringResource(R.string.error_lowest_gt_top)
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPriceScreenPreview() {
    TomatoPricesTheme {
        AddPriceContent(
            uiState = AddPriceUiState(
                markets = listOf(
                    com.parikiganesh.tomato365.repository.SelectionOption("madanapalle", "Madanapalle Market")
                ),
                selectedMarketId = "madanapalle",
                selectedBoxTypeId = "15",
                dateDisplay = "15 Aug 2026",
                lowestRate = "1800",
                topRate = "2400"
            ),
            onSelectMarket = {},
            onSelectBoxType = {},
            onLowestRateChanged = {},
            onTopRateChanged = {},
            onSaveClick = {}
        )
    }
}
