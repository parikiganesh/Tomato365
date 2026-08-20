package com.parikiganesh.tomato365.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.parikiganesh.tomato365.data.remote.FirestoreDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class FarmerRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) {
    suspend fun upsertFarmerRegistration(
        registrationId: String,
        farmerName: String,
        languageCode: String,
        preferredMarketId: String,
        preferredMarketName: String
    ) {
        require(registrationId.isNotBlank()) { "Farmer registration id is required." }
        require(farmerName.isNotBlank()) { "Farmer name is required." }
        require(preferredMarketName.isNotBlank()) { "Preferred market name is required." }

        val normalizedLanguageCode = when (languageCode.trim().lowercase()) {
            "te", "telugu", "తెలుగు" -> "te"
            else -> "en"
        }

        val payload = mapOf(
            "farmerId" to registrationId,
            "name" to farmerName.trim(),
            "selectedLanguage" to normalizedLanguageCode,
            "preferredMarketId" to preferredMarketId,
            "preferredMarketName" to preferredMarketName,
            "isRegistered" to true,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestoreDataSource.farmersCollection()
            .document(registrationId)
            .set(payload, SetOptions.merge())
            .awaitResult()
    }

    suspend fun fetchRegisteredFarmerCount(): Int {
        val snapshot = firestoreDataSource.farmersCollection().get().awaitResult()
        return snapshot.documents.count { doc ->
            doc.getBoolean("isRegistered") != false
        }
    }
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
}
