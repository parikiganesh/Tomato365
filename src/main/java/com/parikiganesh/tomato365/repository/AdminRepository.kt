package com.parikiganesh.tomato365.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.parikiganesh.tomato365.data.remote.FirestoreDataSource
import com.parikiganesh.tomato365.utils.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

enum class AdminAuthErrorReason {
    NO_ADMIN_ACCESS,
    AUTH_FAILED
}

sealed interface AdminAuthResult {
    data object Success : AdminAuthResult
    data class Error(val reason: AdminAuthErrorReason) : AdminAuthResult
}

data class AdminDashboardStats(
    val totalMarkets: Int,
    val todayPriceEntries: Int,
    val totalFarmers: Int
)

data class AdminProfileData(
    val name: String,
    val email: String,
    val role: String,
    val isActive: Boolean
)

@Singleton
class AdminRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val firebaseAuth: FirebaseAuth,
    private val farmerRepository: FarmerRepository
) {
    fun currentAdminUid(): String? = firebaseAuth.currentUser?.uid

    fun usersCollection() = firestoreDataSource.usersCollection()

    suspend fun signInAndVerifyAdmin(email: String, password: String): AdminAuthResult {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).awaitResult()
            val uid = authResult.user?.uid
                ?: return AdminAuthResult.Error(AdminAuthErrorReason.AUTH_FAILED)
            val userSnapshot = usersCollection().document(uid).get().awaitResult()
            val role = userSnapshot.getString("role").orEmpty()
            val isActive = userSnapshot.getBoolean("isActive") ?: true

            if (role == Constants.ROLE_ADMIN && isActive) {
                AdminAuthResult.Success
            } else {
                firebaseAuth.signOut()
                AdminAuthResult.Error(AdminAuthErrorReason.NO_ADMIN_ACCESS)
            }
        } catch (exception: Exception) {
            AdminAuthResult.Error(AdminAuthErrorReason.AUTH_FAILED)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun fetchAdminProfile(): AdminProfileData {
        val uid = currentAdminUid() ?: throw IllegalStateException("Admin user not found.")
        val document = usersCollection().document(uid).get().awaitResult()
        return AdminProfileData(
            name = document.getString("name").orEmpty().ifBlank { "Admin" },
            email = document.getString("email").orEmpty().ifBlank { firebaseAuth.currentUser?.email.orEmpty() },
            role = document.getString("role").orEmpty(),
            isActive = document.getBoolean("isActive") ?: true
        )
    }

    suspend fun fetchDashboardStats(): AdminDashboardStats {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val marketsSnapshot = firestoreDataSource.marketsCollection().get().awaitResult()
        val pricesSnapshot = firestoreDataSource.pricesCollection()
            .whereEqualTo("date", today)
            .get()
            .awaitResult()
        val farmersCount = farmerRepository.fetchRegisteredFarmerCount()

        return AdminDashboardStats(
            totalMarkets = marketsSnapshot.size(),
            todayPriceEntries = pricesSnapshot.size(),
            totalFarmers = farmersCount
        )
    }
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
}
