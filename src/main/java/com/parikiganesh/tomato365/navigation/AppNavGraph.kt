package com.parikiganesh.tomato365.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.parikiganesh.tomato365.ui.screens.admin.AddPriceScreen
import com.parikiganesh.tomato365.ui.screens.admin.AdminProfileScreen
import com.parikiganesh.tomato365.ui.screens.admin.AdminPriceHistoryScreen
import com.parikiganesh.tomato365.ui.screens.admin.AdminDashboardScreen
import com.parikiganesh.tomato365.ui.screens.admin.AdminLoginScreen
import com.parikiganesh.tomato365.ui.screens.admin.ManageMarketsScreen
import com.parikiganesh.tomato365.ui.screens.admin.ViewPricesScreen
import com.parikiganesh.tomato365.ui.screens.home.HomeScreen
import com.parikiganesh.tomato365.ui.screens.marketselection.MarketSelectionScreen
import com.parikiganesh.tomato365.ui.screens.splash.SplashScreen
import com.parikiganesh.tomato365.ui.screens.welcome.WelcomeScreen

@Composable
fun AppNavGraph(notificationMarketName: String? = null) {
    val navController = rememberNavController()

    LaunchedEffect(notificationMarketName) {
        val marketName = notificationMarketName?.trim().orEmpty()
        if (marketName.isNotBlank()) {
            navController.navigate("${Routes.HOME}/${Uri.encode(marketName)}") {
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = { marketName ->
                    navController.navigate("${Routes.HOME}/${Uri.encode(marketName)}") {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onContinue = { navController.navigate(Routes.MARKET_SELECTION) },
                onAdminLogin = { navController.navigate(Routes.ADMIN_LOGIN) }
            )
        }
        composable(Routes.MARKET_SELECTION) {
            MarketSelectionScreen(onDone = { marketName ->
                navController.navigate("${Routes.HOME}/${Uri.encode(marketName)}") {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME_WITH_MARKET) { backStackEntry ->
            val selectedMarket = backStackEntry.arguments?.getString("marketName").orEmpty()
            HomeScreen(
                selectedMarketName = selectedMarket,
                onOpenAdminLogin = { navController.navigate(Routes.ADMIN_LOGIN) }
            )
        }
        composable(Routes.ADMIN_LOGIN) {
            AdminLoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.ADMIN_DASHBOARD) {
                        popUpTo(Routes.ADMIN_LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                onNavigateToRoute = { route ->
                    if (route != Routes.ADMIN_DASHBOARD) {
                        navController.navigate(route)
                    }
                }
            )
        }
        composable(Routes.ADMIN_PRICES) {
            ViewPricesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToRoute = { route ->
                    if (route != Routes.ADMIN_PRICES) {
                        navController.navigate(route)
                    }
                }
            )
        }
        composable(Routes.MANAGE_MARKETS) {
            ManageMarketsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToRoute = { route ->
                    if (route != Routes.MANAGE_MARKETS) {
                        navController.navigate(route)
                    }
                }
            )
        }
        composable(Routes.ADMIN_PROFILE) {
            AdminProfileScreen(
                onNavigateToRoute = { route ->
                    if (route != Routes.ADMIN_PROFILE) {
                        navController.navigate(route)
                    }
                },
                onLogout = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ADMIN_ADD_PRICE) {
            AddPriceScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.navigate(Routes.ADMIN_DASHBOARD) {
                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ADMIN_PRICE_HISTORY) {
            AdminPriceHistoryScreen(
                onBack = { navController.popBackStack() },
                onNavigateToRoute = { route ->
                    navController.navigate(route)
                }
            )
        }
    }
}
