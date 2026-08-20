package com.parikiganesh.tomato365.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = text)
    }
}

@Preview(showBackground = true)
@Composable
private fun AppButtonPreview() {
    TomatoPricesTheme {
        AppButton(text = "Continue", onClick = {})
    }
}
