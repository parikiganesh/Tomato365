package com.parikiganesh.tomato365.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.navigation.Routes

@Composable
fun AdminBottomNavigation(
    selectedRoute: String,
    onSelected: (String) -> Unit
) {
    NavigationBar {
        val items = listOf(
            AdminBottomNavItem(Routes.ADMIN_DASHBOARD, R.string.dashboard, Icons.Outlined.Home),
            AdminBottomNavItem(Routes.ADMIN_PRICES, R.string.prices, Icons.Outlined.TrendingUp),
            AdminBottomNavItem(Routes.MANAGE_MARKETS, R.string.markets, Icons.Outlined.Storefront),
            AdminBottomNavItem(Routes.ADMIN_PROFILE, R.string.profile, Icons.Outlined.Person)
        )
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedRoute == item.route,
                onClick = { onSelected(item.route) },
                icon = { Icon(imageVector = item.icon, contentDescription = null) },
                label = { Text(text = stringResource(item.labelResId)) }
            )
        }
    }
}

private data class AdminBottomNavItem(
    val route: String,
    val labelResId: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
