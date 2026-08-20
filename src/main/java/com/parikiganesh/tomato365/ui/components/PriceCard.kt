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
import com.parikiganesh.tomato365.data.model.TomatoPrice
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme
import com.parikiganesh.tomato365.utils.toInr

@Composable
fun PriceCard(
    price: TomatoPrice,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(
                    R.string.price_range_per_kg,
                    price.minPrice.toInr(),
                    price.maxPrice.toInr()
                )
            )
            Text(
                text = stringResource(
                    R.string.modal_price_label,
                    price.modalPrice.toInr()
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PriceCardPreview() {
    TomatoPricesTheme {
        PriceCard(
            price = TomatoPrice(
                marketId = "madanapalle",
                date = "2026-08-16",
                minPrice = 18.0,
                maxPrice = 24.0,
                modalPrice = 21.0
            )
        )
    }
}
