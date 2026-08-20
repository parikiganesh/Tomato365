package com.parikiganesh.tomato365.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.navigation.Routes
import com.parikiganesh.tomato365.ui.components.AdminBottomNavigation
import com.parikiganesh.tomato365.ui.components.AdminQuickActionCard
import com.parikiganesh.tomato365.ui.components.AdminStatCard
import com.parikiganesh.tomato365.ui.components.AdminTopBar
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme
import com.parikiganesh.tomato365.viewmodel.AdminDashboardViewModel

@Composable
fun AdminDashboardScreen(
    onNavigateToRoute: (String) -> Unit,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AdminTopBar(
                title = stringResource(R.string.admin_dashboard),
                onMenuClick = {},
                onNotificationClick = {}
            )
        },
        bottomBar = {
            AdminBottomNavigation(
                selectedRoute = Routes.ADMIN_DASHBOARD,
                onSelected = onNavigateToRoute
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.dashboard_overview),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminStatCard(
                        title = stringResource(R.string.markets),
                        value = uiState.totalMarkets,
                        subtitle = stringResource(R.string.total_markets),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    AdminStatCard(
                        title = stringResource(R.string.today_prices),
                        value = uiState.todayPriceEntries,
                        subtitle = stringResource(R.string.total_records),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    AdminStatCard(
                        title = stringResource(R.string.users),
                        value = uiState.totalFarmers,
                        subtitle = stringResource(R.string.registered_farmers),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = uiState.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.quick_actions),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminQuickActionCard(
                    title = stringResource(R.string.add_price),
                    icon = Icons.Outlined.Add,
                    onClick = { onNavigateToRoute(Routes.ADMIN_ADD_PRICE) },
                    modifier = Modifier.weight(1f)
                )
                AdminQuickActionCard(
                    title = stringResource(R.string.manage_markets),
                    icon = Icons.Outlined.Storefront,
                    onClick = { onNavigateToRoute(Routes.MANAGE_MARKETS) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminQuickActionCard(
                    title = stringResource(R.string.view_prices),
                    icon = Icons.Outlined.TrendingUp,
                    onClick = { onNavigateToRoute(Routes.ADMIN_PRICES) },
                    modifier = Modifier.weight(1f)
                )
                AdminQuickActionCard(
                    title = stringResource(R.string.price_history),
                    icon = Icons.Outlined.History,
                    onClick = { onNavigateToRoute(Routes.ADMIN_PRICE_HISTORY) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminDashboardScreenPreview() {
    TomatoPricesTheme {
        AdminDashboardScreen(onNavigateToRoute = {})
    }
}
