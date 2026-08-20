package com.parikiganesh.tomato365.ui.screens.markets.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme

@Composable
fun MarketInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Text(text = value)
    }
}

@Preview(showBackground = true)
@Composable
private fun MarketInfoRowPreview() {
    TomatoPricesTheme {
        MarketInfoRow(label = "Modal Price", value = "Rs. 21/kg")
    }
}
