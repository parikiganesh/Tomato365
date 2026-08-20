package com.parikiganesh.tomato365.data.model

data class TomatoPrice(
    val id: String = "",
    val marketId: String = "",
    val date: String = "",
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val boxTypeKg: Int = 0,
    val modalPrice: Double = 0.0,
    val updatedAtMillis: Long = 0L
)
