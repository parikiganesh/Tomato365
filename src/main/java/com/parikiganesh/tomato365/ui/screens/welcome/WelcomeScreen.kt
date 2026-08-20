package com.parikiganesh.tomato365.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.ui.components.FarmerScreenBackground
import com.parikiganesh.tomato365.ui.theme.GreenPrimary
import com.parikiganesh.tomato365.ui.theme.LocalTypography
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme
import com.parikiganesh.tomato365.ui.theme.TomatoRed
import com.parikiganesh.tomato365.utils.AppLanguageManager
import com.parikiganesh.tomato365.viewmodel.WelcomeViewModel
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,
    onAdminLogin: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    var farmerName by remember { mutableStateOf("") }
    val currentLanguageCode = when (LocalConfiguration.current.locales[0]?.language) {
        "te" -> "te"
        else -> "en"
    }
    var selectedLanguage by rememberSaveable(currentLanguageCode) { mutableStateOf(currentLanguageCode) }
    var showNameError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val appTypography = LocalTypography.current
    val tomatoImageName = stringResource(R.string.tomato_image_name)
    val scrollState = rememberScrollState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val tomatoResId = remember(tomatoImageName) {
        context.resources.getIdentifier(tomatoImageName, "drawable", context.packageName)
    }

    FarmerScreenBackground(overlayAlpha = 0.56f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 92.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

            if (tomatoResId != 0) {
                Image(
                    painter = painterResource(id = tomatoResId),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(0.dp))
            }

            Text(
                text = stringResource(R.string.welcome_title_top),
                style = appTypography.headingNormalWeightFontAndLargeFont,
                color = TomatoRed,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.offset(y = (-18).dp)
            )
            Text(
                text = stringResource(R.string.welcome_title_bottom),
                style = appTypography.headingNormalWeightFontAndLargeFont,
                color = GreenPrimary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.offset(y = (-18).dp)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .fillMaxWidth()
                    .shadow(14.dp, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = null,
                            tint = GreenPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.select_language),
                            color = GreenPrimary,
                            style = appTypography.titleLargeSemibold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LanguageButton(
                            text = stringResource(R.string.language_english),
                            selected = selectedLanguage == "en",
                            onClick = {
                                AppLanguageManager.applyLanguage("en")
                                selectedLanguage = "en"
                                viewModel.updateSelectedLanguage("en")
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LanguageButton(
                            text = stringResource(R.string.language_telugu),
                            selected = selectedLanguage == "te",
                            onClick = {
                                AppLanguageManager.applyLanguage("te")
                                selectedLanguage = "te"
                                viewModel.updateSelectedLanguage("te")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.PersonOutline,
                            contentDescription = null,
                            tint = GreenPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.what_is_your_name),
                            style = appTypography.titleLargeSemibold,
                            color = GreenPrimary
                        )
                    }
                    Text(
                        text = stringResource(R.string.personalize_experience),
                        style = appTypography.bodyMediumPrimary,
                        color = Color(0xFF344054),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = farmerName,
                        onValueChange = {
                            farmerName = it
                            if (it.trim().isNotEmpty()) showNameError = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusEvent { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.PersonOutline, contentDescription = null)
                        },
                        placeholder = { Text(text = stringResource(R.string.enter_your_name)) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(16.dp),
                        isError = showNameError,
                        supportingText = {
                            if (showNameError) {
                                Text(text = stringResource(R.string.error_farmer_name_required))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val trimmedName = farmerName.trim()
                            if (trimmedName.isEmpty()) {
                                showNameError = true
                                return@Button
                            }
                            viewModel.saveFarmerSetup(
                                farmerName = trimmedName,
                                languageCode = selectedLanguage,
                                onSaved = onContinue
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.continue_text),
                            style = appTypography.bodyLargeSemibold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(imageVector = Icons.Outlined.ArrowForward, contentDescription = null)
                    }
                }
            }

            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.or_text),
                    style = appTypography.bodyMediumPrimary,
                    color = GreenPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.94f))
                        .clickable(onClick = onAdminLogin)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEAF7EA)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.admin_login),
                        color = Color(0xFF1F2937),
                        style = appTypography.bodyMediumSemibold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFFEAF7EA) else Color.White,
            contentColor = GreenPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) GreenPrimary else Color(0xFFD5D9E2)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = modifier.height(46.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
        if (selected) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = GreenPrimary
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
    TomatoPricesTheme {
        WelcomeScreen(
            onContinue = {},
            onAdminLogin = {}
        )
    }
}
