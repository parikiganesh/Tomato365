package com.parikiganesh.tomato365.ui.screens.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.ui.components.BottomNavItem
import com.parikiganesh.tomato365.ui.components.BottomNavigationBar
import com.parikiganesh.tomato365.ui.components.FarmerScreenBackground
import com.parikiganesh.tomato365.ui.components.AppDropdownField
import com.parikiganesh.tomato365.ui.screens.more.ProfileScreen
import com.parikiganesh.tomato365.ui.screens.pricehistory.PriceHistoryScreen
import com.parikiganesh.tomato365.ui.screens.marketselection.MarketSelectionScreen
import com.parikiganesh.tomato365.ui.theme.GreenPrimary
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme
import com.parikiganesh.tomato365.ui.theme.TomatoRed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.tomato365.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    selectedMarketName: String,
    onOpenAdminLogin: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavItem.HOME) }
    var currentSelectedMarket by rememberSaveable {
        mutableStateOf(selectedMarketName.ifBlank { "Madanapalle Market" })
    }

    LaunchedEffect(selectedMarketName) {
        viewModel.loadTodaysPrice(currentSelectedMarket)
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selected = selectedTab,
                onSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                BottomNavItem.HOME -> HomePriceTab(
                    viewModel = viewModel,
                    onMarketSelected = { marketId, marketName ->
                        currentSelectedMarket = marketName
                        viewModel.onMarketSelected(marketId)
                    },
                    onRefresh = { viewModel.loadTodaysPrice(currentSelectedMarket) }
                )
                BottomNavItem.MARKETS -> MarketSelectionScreen(
                    onDone = { selected ->
                        currentSelectedMarket = selected
                        viewModel.loadTodaysPrice(selected)
                        selectedTab = BottomNavItem.HOME
                    },
                    showTopBar = true,
                    onBack = { selectedTab = BottomNavItem.HOME }
                )
                BottomNavItem.PRICES -> PriceHistoryScreen(
                    selectedMarketName = currentSelectedMarket,
                    onBack = { selectedTab = BottomNavItem.HOME }
                )
                BottomNavItem.PROFILE -> ProfileScreen(
                    selectedMarketName = currentSelectedMarket,
                    onBack = { selectedTab = BottomNavItem.HOME },
                    onPreferredMarketClick = { selectedTab = BottomNavItem.MARKETS },
                    onAdminLoginClick = onOpenAdminLogin
                )
            }
        }
    }
}

@Composable
private fun HomePriceTab(
    viewModel: HomeViewModel,
    onMarketSelected: (String, String) -> Unit,
    onRefresh: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(isNotificationPermissionGranted(context))
    }
    var autoPromptRequestedThisLaunch by rememberSaveable { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted || isNotificationPermissionGranted(context)
    }
    LaunchedEffect(hasNotificationPermission, autoPromptRequestedThisLaunch) {
        if (
            !hasNotificationPermission &&
            !autoPromptRequestedThisLaunch &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            autoPromptRequestedThisLaunch = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    FarmerScreenBackground {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.todays_tomato_prices),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF101828)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Outlined.Cached,
                            contentDescription = null,
                            tint = Color(0xFF344054)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.height(16.dp),
                        tint = Color(0xFF667085)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = uiState.date,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF667085)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                AppDropdownField(
                    label = stringResource(R.string.select_market),
                    selectedId = uiState.selectedMarketId,
                    options = uiState.markets,
                    errorMessage = null,
                    placeholder = stringResource(R.string.choose_market),
                    optionLeadingIcon = Icons.Outlined.LocationOn,
                    onSelected = { marketId ->
                        uiState.markets.firstOrNull { it.id == marketId }?.let { market ->
                            onMarketSelected(market.id, market.name)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = stringResource(R.string.tomato_local),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF101828)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${uiState.minPrice} - ${uiState.maxPrice}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenPrimary
                            )
                            if (uiState.boxTypeKg > 0) {
                                Text(
                                    text = "${uiState.boxTypeKg} kg",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF667085)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFEAECF0))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF98A2B3)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.last_updated),
                                color = Color(0xFF667085),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = uiState.lastUpdated,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF101828)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFEAECF0))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.TrendingUp,
                                contentDescription = null,
                                tint = GreenPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.price_change_from_yesterday),
                                color = Color(0xFF344054),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row {
                            Text(
                                text = if (uiState.priceChange.isEmpty()) "--" else uiState.priceChange,
                                color = if (uiState.isPositiveChange) GreenPrimary else TomatoRed,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (uiState.priceChangePercent.isEmpty()) "--" else uiState.priceChangePercent,
                                color = if (uiState.isPositiveChange) GreenPrimary else TomatoRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Price disclaimer
                Text(
                    text = stringResource(R.string.price_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF667085),
                    fontSize = 15.sp
                )

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = uiState.error!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }

                if (!hasNotificationPermission) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = GreenPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.get_daily_tomato_updates),
                                    color = Color(0xFF344054),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        hasNotificationPermission = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = stringResource(R.string.allow_notifications))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.TrendingUp,
                                contentDescription = null,
                                tint = GreenPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.last_7_days_trend),
                                color = Color(0xFF344054),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (uiState.sevenDayTrend.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.no_price_history_available),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF667085)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            uiState.sevenDayTrend.forEach { trend ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = trend.dateLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF667085),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${trend.minPriceLabel} - ${trend.maxPriceLabel}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF101828),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isNotificationPermissionGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    TomatoPricesTheme {
        HomeScreen(selectedMarketName = "Madanapalle Market")
    }
}
