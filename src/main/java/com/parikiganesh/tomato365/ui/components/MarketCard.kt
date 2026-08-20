package com.parikiganesh.tomato365.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.parikiganesh.tomato365.R
import com.parikiganesh.tomato365.data.model.Market
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme

@Composable
fun MarketCard(
    market: Market,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = market.name)
            Text(
                text = stringResource(
                    R.string.market_location,
                    market.district,
                    market.state
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MarketCardPreview() {
    TomatoPricesTheme {
        MarketCard(
            market = Market(
                id = "madanapalle",
                name = "Madanapalle Market",
                district = "Annamayya",
                state = "Andhra Pradesh"
            )
        )
    }
}
