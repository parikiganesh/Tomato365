package com.parikiganesh.tomato365.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.parikiganesh.tomato365.utils.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun marketsCollection() = firestore.collection(Constants.COLLECTION_MARKETS)

    fun varietiesCollection() = firestore.collection(Constants.COLLECTION_VARIETIES)

    fun pricesCollection() = firestore.collection(Constants.COLLECTION_PRICES)

    fun usersCollection() = firestore.collection(Constants.COLLECTION_USERS)

    fun farmersCollection() = firestore.collection(Constants.COLLECTION_FARMERS)

    fun currentUserId(): String? = auth.currentUser?.uid
}
