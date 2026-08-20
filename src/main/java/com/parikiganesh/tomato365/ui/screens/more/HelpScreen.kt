package com.parikiganesh.tomato365.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme

@Composable
fun HelpScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.coming_soon))
    }
}

@Preview(showBackground = true)
@Composable
private fun HelpScreenPreview() {
    TomatoPricesTheme {
        HelpScreen()
    }
}
