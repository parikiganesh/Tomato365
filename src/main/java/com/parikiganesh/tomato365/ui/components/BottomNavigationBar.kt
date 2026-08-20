package com.parikiganesh.tomato365.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme

enum class BottomNavItem {
    HOME,
    MARKETS,
    PRICES,
    PROFILE
}

@Composable
fun BottomNavigationBar(
    selected: BottomNavItem,
    onSelected: (BottomNavItem) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == BottomNavItem.HOME,
            onClick = { onSelected(BottomNavItem.HOME) },
            icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.home)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1B6B3A),
                unselectedIconColor = Color(0xFF5F6368),
                selectedTextColor = Color(0xFF1B6B3A),
                unselectedTextColor = Color(0xFF5F6368)
            )
        )
        NavigationBarItem(
            selected = selected == BottomNavItem.MARKETS,
            onClick = { onSelected(BottomNavItem.MARKETS) },
            icon = { Icon(Icons.Outlined.List, contentDescription = null) },
            label = { Text(stringResource(R.string.markets)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1B6B3A),
                unselectedIconColor = Color(0xFF5F6368),
                selectedTextColor = Color(0xFF1B6B3A),
                unselectedTextColor = Color(0xFF5F6368)
            )
        )
        NavigationBarItem(
            selected = selected == BottomNavItem.PRICES,
            onClick = { onSelected(BottomNavItem.PRICES) },
            icon = { Icon(Icons.Outlined.TrendingUp, contentDescription = null) },
            label = { Text(stringResource(R.string.prices)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1B6B3A),
                unselectedIconColor = Color(0xFF5F6368),
                selectedTextColor = Color(0xFF1B6B3A),
                unselectedTextColor = Color(0xFF5F6368)
            )
        )
        NavigationBarItem(
            selected = selected == BottomNavItem.PROFILE,
            onClick = { onSelected(BottomNavItem.PROFILE) },
            icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.profile)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1B6B3A),
                unselectedIconColor = Color(0xFF5F6368),
                selectedTextColor = Color(0xFF1B6B3A),
                unselectedTextColor = Color(0xFF5F6368)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavigationBarPreview() {
    TomatoPricesTheme {
        BottomNavigationBar(
            selected = BottomNavItem.HOME,
            onSelected = {}
        )
    }
}
