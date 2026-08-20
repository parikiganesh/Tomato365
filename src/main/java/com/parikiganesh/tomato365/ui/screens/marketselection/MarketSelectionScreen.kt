package com.parikiganesh.tomato365.ui.screens.marketselection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.ui.theme.GreenPrimary
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme
import com.parikiganesh.tomato365.ui.components.FarmerScreenBackground

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.tomato365.data.model.Market
import com.parikiganesh.tomato365.viewmodel.MarketSelectionViewModel

@Composable
fun MarketSelectionScreen(
    onDone: (String) -> Unit,
    showTopBar: Boolean = false,
    onBack: (() -> Unit)? = null,
    viewModel: MarketSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedMarketId by rememberSaveable { mutableStateOf("") }

    val filteredMarkets = remember(searchQuery, uiState.markets) {
        if (searchQuery.isBlank()) {
            uiState.markets
        } else {
            uiState.markets.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.district.contains(searchQuery, ignoreCase = true) ||
                    it.state.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    FarmerScreenBackground {
        val topContentPadding = if (showTopBar) 0.dp else 12.dp
        val topInsetModifier = if (showTopBar) Modifier else Modifier.statusBarsPadding()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(topInsetModifier)
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 14.dp, end = 14.dp, top = topContentPadding, bottom = 12.dp)
        ) {
        if (showTopBar) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBack?.invoke() }) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
                Text(
                    text = stringResource(R.string.select_market_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        if (!showTopBar) {
            Text(
                text = stringResource(R.string.select_market_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101828)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(R.string.search_market_hint)) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFFB0B7C3),
                unfocusedBorderColor = Color(0xFFB0B7C3)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.error ?: stringResource(R.string.unable_to_load_markets),
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else if (filteredMarkets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.no_markets_found), color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMarkets) { market ->
                    MarketSelectionItem(
                        market = market,
                        selected = selectedMarketId == market.id,
                        onClick = {
                            selectedMarketId = market.id
                            viewModel.savePreferredMarket(market.id, market.name) {
                                onDone(market.name)
                            }
                        }
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun MarketSelectionItem(
    market: Market,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) GreenPrimary else Color(0xFFEAECEF)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = if (selected) GreenPrimary else Color(0xFF667085),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = market.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${market.district}, ${market.state}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF667085)
                )
            }

            Icon(
                imageVector = if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = if (selected) GreenPrimary else Color(0xFF98A2B3)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MarketSelectionScreenPreview() {
    TomatoPricesTheme {
        MarketSelectionScreen(onDone = { _ -> })
    }
}
