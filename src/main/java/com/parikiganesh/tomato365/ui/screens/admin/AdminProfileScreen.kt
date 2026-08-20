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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.parikiganesh.tomato365.navigation.Routes
import com.parikiganesh.tomato365.ui.components.AdminBottomNavigation
import com.parikiganesh.tomato365.ui.theme.GreenPrimary
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme
import com.parikiganesh.tomato365.viewmodel.AdminProfileUiState
import com.parikiganesh.tomato365.viewmodel.AdminProfileViewModel

@Composable
fun AdminProfileScreen(
    onNavigateToRoute: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) {
            onLogout()
            viewModel.onLoggedOutHandled()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.admin_profile),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    ProfileCard(uiState = uiState)
                    Spacer(modifier = Modifier.height(10.dp))
                    StatusCard(isActive = uiState.isActive)
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = viewModel::logout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(text = stringResource(R.string.logout))
                    }
                }
            }
        }

        AdminBottomNavigation(
            selectedRoute = Routes.ADMIN_PROFILE,
            onSelected = onNavigateToRoute
        )
    }
}

@Composable
private fun ProfileCard(uiState: AdminProfileUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(Color(0xFFEAF7EA), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = GreenPrimary
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
                Column {
                    Text(uiState.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(uiState.email, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF667085))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            ProfileInfoRow(
                icon = Icons.Outlined.Shield,
                title = stringResource(R.string.role),
                value = uiState.role.ifBlank { "admin" }
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProfileInfoRow(
                icon = Icons.Outlined.Email,
                title = stringResource(R.string.email),
                value = uiState.email
            )
        }
    }
}

@Composable
private fun StatusCard(isActive: Boolean) {
    val statusText = if (isActive) stringResource(R.string.active) else stringResource(R.string.inactive)
    val textColor = if (isActive) Color(0xFF15803D) else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.account_status), style = MaterialTheme.typography.bodyLarge)
            Text(text = statusText, color = textColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.material3.Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF667085))
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "$title: $value",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1F2937)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminProfileScreenPreview() {
    TomatoPricesTheme {
        AdminProfileScreen(
            onNavigateToRoute = {},
            onLogout = {}
        )
    }
}
