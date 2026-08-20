package com.parikiganesh.tomato365.ui.screens.pricehistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.ui.components.FarmerScreenBackground
import com.parikiganesh.tomato365.ui.theme.GreenPrimary
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme
import com.parikiganesh.tomato365.viewmodel.HistoryRowUi
import com.parikiganesh.tomato365.viewmodel.PriceHistoryViewModel

@Composable
fun PriceHistoryScreen(
    selectedMarketName: String,
    onBack: () -> Unit,
    viewModel: PriceHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(selectedMarketName) {
        viewModel.loadInitialData(selectedMarketName)
    }

    FarmerScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
                Text(
                    text = stringResource(R.string.price_history),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF667085)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = uiState.selectedMarket?.name ?: selectedMarketName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF475467)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RangeButton(
                    text = stringResource(R.string.last_7_days),
                    selected = uiState.selectedRangeDays == 7,
                    onClick = { viewModel.onRangeSelected(7) }
                )
                RangeButton(
                    text = stringResource(R.string.last_30_days),
                    selected = uiState.selectedRangeDays == 30,
                    onClick = { viewModel.onRangeSelected(30) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = Color(0xFF667085)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = uiState.displayRange,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF667085)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
                return@FarmerScreenBackground
            }

            if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                return@FarmerScreenBackground
            }

            if (uiState.history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.no_price_history_available),
                        color = Color.Gray
                    )
                }
                return@FarmerScreenBackground
            }

            HistoryTrendBars(rows = uiState.history)
            Spacer(modifier = Modifier.height(10.dp))
            HistoryTable(rows = uiState.history)
        }
    }
}

@Composable
private fun RangeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) GreenPrimary else Color.White,
            contentColor = if (selected) Color.White else Color(0xFF344054)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selected) 3.dp else 1.dp)
    ) {
        Text(text = text)
    }
}

@Composable
private fun HistoryTrendBars(rows: List<HistoryRowUi>) {
    val chartRows = rows.take(7).reversed()
    val maxTop = chartRows.maxOfOrNull { it.max.toFloatOrNull() ?: 0f } ?: 1f

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.top_rate_trend),
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF667085)
            )
            Spacer(modifier = Modifier.height(8.dp))
            chartRows.forEach { row ->
                val topValue = row.max.toFloatOrNull() ?: 0f
                val ratio = if (maxTop > 0f) (topValue / maxTop).coerceIn(0f, 1f) else 0f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.date,
                        modifier = Modifier.width(86.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF667085)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .background(Color(0xFFEAF7EA), RoundedCornerShape(999.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(ratio)
                                .background(GreenPrimary, RoundedCornerShape(999.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = row.max,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF344054)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryTable(rows: List<HistoryRowUi>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
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
            items(rows) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                ) {
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

@Composable
private fun HeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Preview(showBackground = true)
@Composable
private fun PriceHistoryScreenPreview() {
    TomatoPricesTheme {
        PriceHistoryScreen(
            selectedMarketName = "Madanapalle Market",
            onBack = {}
        )
    }
}
