package com.parikiganesh.tomato365.repository

import com.google.android.gms.tasks.Task
import com.parikiganesh.tomato365.data.model.Market
import com.parikiganesh.tomato365.data.remote.FirestoreDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class MarketRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) {
    fun marketsCollection() = firestoreDataSource.marketsCollection()

    suspend fun getMarkets(): List<Market> {
        val snapshot = marketsCollection().get().awaitResult()
        return snapshot.documents.mapNotNull { it.toObject(Market::class.java)?.copy(id = it.id) }
    }

    suspend fun addMarket(market: Market) {
        val docRef = marketsCollection().document()
        docRef.set(market.copy(id = docRef.id)).awaitResult()
    }

    suspend fun getActiveMarkets(): List<Market> {
        val snapshot = marketsCollection().get().awaitResult()
        return snapshot.documents.mapNotNull { doc ->
            val market = doc.toObject(Market::class.java)?.copy(id = doc.id)
            val name = market?.name?.takeIf { it.isNotBlank() } ?: doc.getString("name").orEmpty()
            if (name.isBlank()) return@mapNotNull null

            val district = market?.district ?: doc.getString("district").orEmpty()
            val state = market?.state ?: doc.getString("state").orEmpty()
            val rawActive = doc.get("isActive")
            val isActive = when (rawActive) {
                null -> market?.isActive ?: true
                is Boolean -> rawActive
                is String -> rawActive.equals("true", ignoreCase = true)
                is Number -> rawActive.toInt() != 0
                else -> market?.isActive ?: true
            }

            if (!isActive) return@mapNotNull null
            Market(
                id = doc.id,
                name = name,
                district = district,
                state = state,
                isActive = true
            )
        }.sortedBy { it.name }
    }

    suspend fun deleteMarket(marketId: String) {
        marketsCollection().document(marketId).delete().awaitResult()
    }
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
}
