package com.parikiganesh.tomato365.ui.screens.admin

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.navigation.Routes
import com.parikiganesh.tomato365.repository.SelectionOption
import com.parikiganesh.tomato365.ui.components.AdminBottomNavigation
import com.parikiganesh.tomato365.ui.components.AppDropdownField
import com.parikiganesh.tomato365.ui.components.AppNumberField
import com.parikiganesh.tomato365.ui.theme.GreenPrimary
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme
import com.parikiganesh.tomato365.viewmodel.PriceRowUi
import com.parikiganesh.tomato365.viewmodel.ViewPricesViewModel
import java.util.Calendar

@Composable
fun ViewPricesScreen(
    onBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    viewModel: ViewPricesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val marketOptions = listOf(SelectionOption(id = "", name = stringResource(R.string.all_markets))) + uiState.markets
    val unknownMarketText = stringResource(R.string.unknown_market)
    var editTarget by remember { mutableStateOf<PriceRowUi?>(null) }
    var deleteTarget by remember { mutableStateOf<PriceRowUi?>(null) }

    val calendar = remember(uiState.selectedDateMillis) {
        Calendar.getInstance().apply { timeInMillis = uiState.selectedDateMillis }
    }
    val datePickerDialog = remember(uiState.selectedDateMillis) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance().apply { set(year, month, dayOfMonth, 0, 0, 0) }
                viewModel.onDateSelected(picked.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    editTarget?.let { target ->
        EditPriceDialog(
            initialPrice = target,
            onDismiss = { editTarget = null },
            onSave = { minPrice, maxPrice, boxTypeKg ->
                viewModel.updatePrice(
                    priceId = target.id,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    boxTypeKg = boxTypeKg
                )
                editTarget = null
            }
        )
    }

    deleteTarget?.let { target ->
        DeletePriceDialog(
            marketName = target.marketName.ifBlank { unknownMarketText },
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deletePrice(target.id)
                deleteTarget = null
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
                }
                Text(
                    text = stringResource(R.string.view_prices),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DateField(
                    text = uiState.displayDate,
                    modifier = Modifier.weight(1f),
                    onClick = { datePickerDialog.show() }
                )
                AppDropdownField(
                    label = stringResource(R.string.select_market),
                    selectedId = uiState.selectedMarketId,
                    options = marketOptions,
                    errorMessage = null,
                    placeholder = stringResource(R.string.all_markets),
                    optionLeadingIcon = Icons.Outlined.LocationOn,
                    onSelected = viewModel::onMarketSelected,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.loadPrices() },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Outlined.FilterAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.filter))
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.error != null) {
                Text(
                    text = uiState.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                } else if (uiState.prices.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.no_prices_found_for_date), color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEAF7EA))
                                    .padding(vertical = 10.dp, horizontal = 10.dp)
                            ) {
                                TableHeaderCell(text = stringResource(R.string.market), modifier = Modifier.weight(1.3f))
                                TableHeaderCell(text = stringResource(R.string.box_type), modifier = Modifier.weight(0.9f))
                                TableHeaderCell(text = stringResource(R.string.lowest_rate_short), modifier = Modifier.weight(0.9f))
                                TableHeaderCell(text = stringResource(R.string.top_rate_short), modifier = Modifier.weight(0.9f))
                                TableHeaderCell(text = stringResource(R.string.actions), modifier = Modifier.weight(0.8f))
                            }
                        }
                        items(uiState.prices, key = { it.id }) { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    row.marketName.ifBlank { stringResource(R.string.unknown_market) },
                                    modifier = Modifier.weight(1.3f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = stringResource(R.string.box_type_value, row.boxTypeKg),
                                    modifier = Modifier.weight(0.9f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(row.min, modifier = Modifier.weight(0.9f), style = MaterialTheme.typography.bodyMedium)
                                Text(row.max, modifier = Modifier.weight(0.9f), style = MaterialTheme.typography.bodyMedium)
                                Row(
                                    modifier = Modifier.weight(0.8f),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = { editTarget = row }) {
                                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit_price))
                                    }
                                    IconButton(onClick = { deleteTarget = row }) {
                                        Icon(imageVector = Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.delete_price))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        AdminBottomNavigation(
            selectedRoute = Routes.ADMIN_PRICES,
            onSelected = onNavigateToRoute
        )
    }
}

@Composable
private fun DateField(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = {},
        readOnly = true,
        label = { Text(text = stringResource(R.string.date)) },
        leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun EditPriceDialog(
    initialPrice: PriceRowUi,
    onDismiss: () -> Unit,
    onSave: (minPrice: Double, maxPrice: Double, boxTypeKg: Int) -> Unit
) {
    var selectedBoxId by remember(initialPrice.id) { mutableStateOf(initialPrice.boxTypeKg.toString()) }
    var minPrice by remember(initialPrice.id) { mutableStateOf(initialPrice.min) }
    var maxPrice by remember(initialPrice.id) { mutableStateOf(initialPrice.max) }
    var error by remember(initialPrice.id) { mutableStateOf<String?>(null) }
    val invalidNumberError = stringResource(R.string.error_invalid_number)
    val negativePriceError = stringResource(R.string.error_negative_price)
    val lowestGtTopError = stringResource(R.string.error_lowest_gt_top)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.edit_price)) },
        text = {
            Column {
                Text(
                    text = initialPrice.marketName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                AppDropdownField(
                    label = stringResource(R.string.box_type),
                    selectedId = selectedBoxId,
                    options = listOf(
                        SelectionOption("15", stringResource(R.string.box_15kg)),
                        SelectionOption("30", stringResource(R.string.box_30kg))
                    ),
                    errorMessage = null,
                    onSelected = { selectedBoxId = it }
                )
                Spacer(modifier = Modifier.height(10.dp))
                AppNumberField(
                    label = stringResource(R.string.lowest_rate),
                    value = minPrice,
                    errorMessage = null,
                    onValueChange = {
                        minPrice = it
                        error = null
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                AppNumberField(
                    label = stringResource(R.string.top_rate),
                    value = maxPrice,
                    errorMessage = null,
                    onValueChange = {
                        maxPrice = it
                        error = null
                    }
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val minValue = minPrice.toDoubleOrNull()
                    val maxValue = maxPrice.toDoubleOrNull()
                    val boxValue = selectedBoxId.toIntOrNull()
                    when {
                        minValue == null || maxValue == null || boxValue == null -> error = invalidNumberError
                        minValue < 0 || maxValue < 0 -> error = negativePriceError
                        minValue > maxValue -> error = lowestGtTopError
                        else -> onSave(minValue, maxValue, boxValue)
                    }
                }
            ) {
                Text(text = stringResource(R.string.save_price))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel_text))
            }
        }
    )
}

@Composable
private fun DeletePriceDialog(
    marketName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.delete_price)) },
        text = { Text(text = stringResource(R.string.delete_price_confirmation, marketName)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.delete_text))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel_text))
            }
        }
    )
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
}

@Preview(showBackground = true)
@Composable
private fun ViewPricesScreenPreview() {
    TomatoPricesTheme {
        ViewPricesScreen(onBack = {}, onNavigateToRoute = {})
    }
}
