package com.parikiganesh.tomato365.ui.screens.home.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme

@Composable
fun HomeHeader(title: String) {
    Text(text = title)
}

@Preview(showBackground = true)
@Composable
private fun HomeHeaderPreview() {
    TomatoPricesTheme {
        HomeHeader(title = "Today's Tomato Price")
    }
}
