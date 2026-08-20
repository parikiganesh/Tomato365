package com.parikiganesh.tomato365.utils

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

fun Double.toInr(): String {
    return "₹${toWholeNumberString()}"
}

fun Double.toWholeNumberString(): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
        isGroupingUsed = false
    }
    return formatter.format(this.roundToInt())
}
