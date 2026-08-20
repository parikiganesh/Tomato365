package com.parikiganesh.tomato365.ui.screens.more

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.ui.components.FarmerScreenBackground
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme
import com.parikiganesh.tomato365.viewmodel.MoreViewModel

@Composable
fun ProfileScreen(
    selectedMarketName: String,
    onBack: () -> Unit,
    onPreferredMarketClick: () -> Unit = {},
    onAdminLoginClick: () -> Unit = {},
    viewModel: MoreViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var pendingLanguageCode by remember { mutableStateOf(uiState.selectedLanguageCode) }
    val appName = stringResource(R.string.app_name)
    val shareText = stringResource(R.string.share_app_message, context.packageName)
    val shareChooserTitle = stringResource(R.string.share_app_chooser_title)
    val farmerName = uiState.farmerName.ifBlank { stringResource(R.string.farmer_label) }
    val language = if (uiState.selectedLanguageCode == "te") {
        stringResource(R.string.language_telugu)
    } else {
        stringResource(R.string.language_english)
    }
    val preferredMarket = uiState.preferredMarketName.ifBlank { selectedMarketName }

    FarmerScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp)
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
                    text = stringResource(R.string.my_profile),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@FarmerScreenBackground
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFEAF4FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.farmer_avathar),
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = farmerName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            SectionTitle(text = stringResource(R.string.preferences_section))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                ProfileItemRow(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.select_language),
                    value = language,
                    onClick = {
                        pendingLanguageCode = uiState.selectedLanguageCode
                        showLanguageDialog = true
                    }
                )
                ProfileItemRow(
                    icon = Icons.Outlined.LocationOn,
                    title = stringResource(R.string.preferred_market),
                    value = preferredMarket,
                    onClick = onPreferredMarketClick
                )
            }

            SectionTitle(text = stringResource(R.string.about_section))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                ProfileItemRow(
                    icon = Icons.Outlined.Lock,
                    title = stringResource(R.string.admin_login),
                    onClick = onAdminLoginClick
                )
                ProfileItemRow(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.about_us),
                    onClick = { showAboutDialog = true }
                )
//                ProfileItemRow(
//                    icon = Icons.Outlined.HelpOutline,
//                    title = stringResource(R.string.help_support)
//                )
                ProfileItemRow(
                    icon = Icons.Outlined.Share,
                    title = stringResource(R.string.share_app),
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, appName)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, shareChooserTitle))
                    }
                )
            }

            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    title = { Text(text = stringResource(R.string.about_us)) },
                    text = { Text(text = stringResource(R.string.about_us_message)) },
                    confirmButton = {
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text(text = stringResource(R.string.ok_text))
                        }
                    }
                )
            }

            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = { Text(text = stringResource(R.string.select_language)) },
                    text = {
                        Column {
                            LanguageOptionRow(
                                text = stringResource(R.string.language_english),
                                selected = pendingLanguageCode == "en",
                                onClick = { pendingLanguageCode = "en" }
                            )
                            LanguageOptionRow(
                                text = stringResource(R.string.language_telugu),
                                selected = pendingLanguageCode == "te",
                                onClick = { pendingLanguageCode = "te" }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.updateLanguage(pendingLanguageCode)
                                showLanguageDialog = false
                            }
                        ) {
                            Text(text = stringResource(R.string.save_text))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLanguageDialog = false }) {
                            Text(text = stringResource(R.string.cancel_text))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF344054),
        modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
    )
}

@Composable
private fun ProfileItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { rowModifier ->
                if (onClick != null) rowModifier.then(Modifier.clickable(onClick = onClick)) else rowModifier
            }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF667085))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = title, modifier = Modifier.weight(1f))
        if (!value.isNullOrBlank()) {
            Text(text = value, color = Color(0xFF344054), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF98A2B3)
        )
    }
}

@Composable
private fun LanguageOptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text)
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    TomatoPricesTheme {
        ProfileScreen(
            selectedMarketName = "Madanapalle Market",
            onBack = {},
            onPreferredMarketClick = {},
            onAdminLoginClick = {}
        )
    }
}
