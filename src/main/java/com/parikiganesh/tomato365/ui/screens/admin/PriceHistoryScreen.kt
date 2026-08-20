package com.parikiganesh.tomato365.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.navigation.Routes
import com.parikiganesh.tomato365.ui.components.AdminBottomNavigation
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.tomato365.ui.components.AppDropdownField
import com.parikiganesh.tomato365.repository.SelectionOption
import com.parikiganesh.tomato365.viewmodel.PriceHistoryViewModel

@Composable
fun AdminPriceHistoryScreen(
    onBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    viewModel: PriceHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val marketOptions = listOf(SelectionOption(id = "", name = stringResource(R.string.all_markets))) + uiState.markets

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
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
                    text = stringResource(R.string.price_history),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            AppDropdownField(
                label = stringResource(R.string.select_market),
                selectedId = uiState.selectedMarket?.id.orEmpty(),
                options = marketOptions,
                errorMessage = null,
                placeholder = stringResource(R.string.all_markets),
                optionLeadingIcon = Icons.Outlined.LocationOn,
                onSelected = { marketId ->
                    if (marketId.isBlank()) {
                        viewModel.onMarketSelected(null)
                    } else {
                        uiState.markets.firstOrNull { it.id == marketId }?.let(viewModel::onMarketSelected)
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            SelectorField(
                text = stringResource(R.string.tomato_local),
                leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                showTrailingIcon = false
            )
            Spacer(modifier = Modifier.height(8.dp))
            SelectorField(
                text = uiState.displayRange,
                leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 10.dp)) {
                    SummaryValue(
                        label = stringResource(R.string.box_15kg),
                        value = uiState.box15Count.toString(),
                        color = Color(0xFF15803D),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryValue(
                        label = stringResource(R.string.box_30kg),
                        value = uiState.box30Count.toString(),
                        color = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF15803D))
                    }
                } else if (uiState.history.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.no_price_history_available), color = Color.Gray)
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
                                HeaderCell(stringResource(R.string.date), Modifier.weight(1.2f))
                                HeaderCell(stringResource(R.string.box_type), Modifier.weight(1f))
                                HeaderCell(stringResource(R.string.lowest_rate_short), Modifier.weight(1f))
                                HeaderCell(stringResource(R.string.top_rate_short), Modifier.weight(1f))
                            }
                        }
                        items(uiState.history) { row ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp, horizontal = 10.dp)) {
                                Text(row.date, modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = if (row.boxTypeKg > 0) stringResource(R.string.box_type_value, row.boxTypeKg) else "-",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(row.min, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                Text(row.max, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
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
private fun SelectorField(
    text: String,
    leadingIcon: @Composable () -> Unit,
    showTrailingIcon: Boolean = true
) {
    OutlinedTextField(
        value = text,
        onValueChange = {},
        readOnly = true,
        leadingIcon = leadingIcon,
        trailingIcon = if (showTrailingIcon) {
            { Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null) }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SummaryValue(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.headlineSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
}

@Preview(showBackground = true)
@Composable
private fun AdminPriceHistoryScreenPreview() {
    TomatoPricesTheme {
        AdminPriceHistoryScreen(onBack = {}, onNavigateToRoute = {})
    }
}
