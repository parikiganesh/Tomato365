package com.parikiganesh.tomato365.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String) {
    CenterAlignedTopAppBar(
        title = { Text(text = title) }
    )
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarPreview() {
    TomatoPricesTheme {
        AppTopBar(title = "Tomato Market Prices")
    }
}
