package com.parikiganesh.tomato365.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.parikiganesh.tomato365.data.model.Market
import com.parikiganesh.tomato365.data.model.TomatoPrice
import com.parikiganesh.tomato365.data.remote.FirestoreDataSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class PriceRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) {
    fun pricesCollection() = firestoreDataSource.pricesCollection()

    suspend fun getActiveMarkets(): List<SelectionOption> {
        val snapshot = firestoreDataSource.marketsCollection().get().awaitResult()
        return snapshot.documents.mapNotNull { doc ->
            val market = doc.toObject(Market::class.java)?.copy(id = doc.id)
            val name = market?.name?.takeIf { it.isNotBlank() } ?: doc.getString("name").orEmpty()
            if (name.isBlank()) return@mapNotNull null
            val rawActive = doc.get("isActive")
            val isActive = when (rawActive) {
                null -> market?.isActive ?: true
                is Boolean -> rawActive
                is String -> rawActive.equals("true", ignoreCase = true)
                is Number -> rawActive.toInt() != 0
                else -> market?.isActive ?: true
            }
            if (!isActive) return@mapNotNull null
            SelectionOption(id = doc.id, name = name)
        }.sortedBy { it.name }
    }

    suspend fun getActiveVarieties(): List<SelectionOption> {
        val snapshot = firestoreDataSource.varietiesCollection()
            .whereEqualTo("isActive", true)
            .get()
            .awaitResult()
        return snapshot.documents.mapNotNull { doc ->
            val name = doc.getString("name").orEmpty()
            if (name.isBlank()) null else SelectionOption(id = doc.id, name = name)
        }.sortedBy { it.name }
    }

    suspend fun savePrice(input: SavePriceInput) {
        val adminUid = firestoreDataSource.currentUserId()
            ?: throw IllegalStateException("Admin user not found.")
        val priceId = pricesCollection().document().id
        val payload = hashMapOf(
            "marketId" to input.marketId,
            "marketName" to input.marketName,
            "varietyId" to input.varietyId,
            "date" to input.date,
            "minPrice" to input.minPrice,
            "maxPrice" to input.maxPrice,
            "boxTypeKg" to input.boxTypeKg,
            "updatedBy" to adminUid,
            "updatedAt" to Timestamp.now()
        )
        pricesCollection().document(priceId).set(payload).awaitResult()
    }

    suspend fun updatePrice(input: UpdatePriceInput) {
        val adminUid = firestoreDataSource.currentUserId()
            ?: throw IllegalStateException("Admin user not found.")
        val payload = hashMapOf(
            "minPrice" to input.minPrice,
            "maxPrice" to input.maxPrice,
            "boxTypeKg" to input.boxTypeKg,
            "updatedBy" to adminUid,
            "updatedAt" to Timestamp.now()
        )
        pricesCollection().document(input.priceId).update(payload as Map<String, Any>).awaitResult()
    }

    suspend fun deletePrice(priceId: String) {
        pricesCollection().document(priceId).delete().awaitResult()
    }

    suspend fun getPricesByDate(date: String): List<com.parikiganesh.tomato365.data.model.TomatoPrice> {
        val snapshot = pricesCollection()
            .whereEqualTo("date", date)
            .get()
            .awaitResult()
        return snapshot.documents.map { document ->
            document.toTomatoPrice()
        }
    }

    suspend fun getPriceHistory(
        marketId: String,
        varietyId: String,
        startDate: String,
        endDate: String
    ): List<com.parikiganesh.tomato365.data.model.TomatoPrice> {
        val snapshot = pricesCollection()
            .whereEqualTo("marketId", marketId)
            .whereEqualTo("varietyId", varietyId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .awaitResult()
        return snapshot.documents.map { document ->
            document.toTomatoPrice()
        }
            .sortedByDescending { it.date }
    }

    suspend fun getPriceHistoryByDateRange(
        startDate: String,
        endDate: String,
        marketId: String? = null
    ): List<com.parikiganesh.tomato365.data.model.TomatoPrice> {
        val snapshot = if (marketId.isNullOrBlank()) {
            pricesCollection()
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .awaitResult()
        } else {
            // Use market-only query to avoid composite-index requirement for market + date-range filters.
            pricesCollection()
                .whereEqualTo("marketId", marketId)
                .get()
                .awaitResult()
        }

        val raw = snapshot.documents.map { document ->
            document.toTomatoPrice()
        }

        return raw
            .filter { price -> price.date in startDate..endDate }
            .sortedByDescending { it.date }
    }

    fun formatDateForStorage(dateInMillis: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(dateInMillis))
    }

    fun formatDateForDisplay(dateInMillis: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(dateInMillis))
    }
}

data class SelectionOption(
    val id: String,
    val name: String
)

data class SavePriceInput(
    val marketId: String,
    val marketName: String,
    val varietyId: String,
    val date: String,
    val minPrice: Double,
    val maxPrice: Double,
    val boxTypeKg: Int
)

data class UpdatePriceInput(
    val priceId: String,
    val minPrice: Double,
    val maxPrice: Double,
    val boxTypeKg: Int
)

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toTomatoPrice(): TomatoPrice {
    val updatedTimestamp = getTimestamp("updatedAt")?.toDate()?.time ?: 0L
    return TomatoPrice(
        id = id,
        marketId = getString("marketId").orEmpty(),
        date = getString("date").orEmpty(),
        minPrice = getDouble("minPrice") ?: 0.0,
        maxPrice = getDouble("maxPrice") ?: 0.0,
        boxTypeKg = (getLong("boxTypeKg") ?: 0L).toInt(),
        modalPrice = getDouble("modalPrice") ?: 0.0,
        updatedAtMillis = updatedTimestamp
    )
}
