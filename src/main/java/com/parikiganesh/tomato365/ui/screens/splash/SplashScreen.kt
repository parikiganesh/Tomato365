package com.parikiganesh.tomato365.ui.screens.splash

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.viewmodel.SplashDestination
import com.parikiganesh.tomato365.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToWelcome: () -> Unit,
    onNavigateToHome: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val noInternetMessage = stringResource(R.string.no_internet_first_time_message)
    var navigated by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.destination, navigated) {
        val destination = uiState.destination ?: return@LaunchedEffect
        if (navigated) return@LaunchedEffect
        delay(1800)
        when (destination) {
            SplashDestination.Welcome -> {
                if (!isInternetAvailable(context)) {
                    Toast.makeText(context, noInternetMessage, Toast.LENGTH_SHORT).show()
                }
                onNavigateToWelcome()
            }
            is SplashDestination.Home -> onNavigateToHome(destination.marketName)
        }
        navigated = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.splash_screen_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}
