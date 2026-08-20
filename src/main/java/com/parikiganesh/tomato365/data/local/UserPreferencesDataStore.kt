package com.parikiganesh.tomato365.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class UserPreferences(
    val farmerName: String = "",
    val selectedLanguage: String = "en",
    val preferredMarketId: String = "",
    val preferredMarketName: String = "",
    val farmerRegistrationId: String = ""
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val userPreferences: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            farmerName = preferences[Keys.FARMER_NAME].orEmpty(),
            selectedLanguage = preferences[Keys.SELECTED_LANGUAGE]
                ?: preferences[Keys.LEGACY_SELECTED_LANGUAGE]
                ?: "en",
            preferredMarketId = preferences[Keys.PREFERRED_MARKET_ID].orEmpty(),
            preferredMarketName = preferences[Keys.PREFERRED_MARKET_NAME]
                ?: preferences[Keys.LEGACY_PREFERRED_MARKET]
                ?: "",
            farmerRegistrationId = preferences[Keys.FARMER_REGISTRATION_ID]
                ?: preferences[Keys.LEGACY_FARMER_ID]
                ?: ""
        )
    }

    suspend fun saveFarmerName(name: String) {
        dataStore.edit { it[Keys.FARMER_NAME] = name }
    }

    suspend fun saveSelectedLanguage(languageCode: String) {
        val normalized = when (languageCode.trim().lowercase()) {
            "te", "telugu", "తెలుగు" -> "te"
            else -> "en"
        }
        dataStore.edit {
            it[Keys.SELECTED_LANGUAGE] = normalized
            it[Keys.LEGACY_SELECTED_LANGUAGE] = normalized
        }
    }

    suspend fun savePreferredMarketId(marketId: String) {
        dataStore.edit { it[Keys.PREFERRED_MARKET_ID] = marketId }
    }

    suspend fun savePreferredMarket(marketId: String, marketName: String) {
        dataStore.edit {
            it[Keys.PREFERRED_MARKET_ID] = marketId
            it[Keys.PREFERRED_MARKET_NAME] = marketName
            it[Keys.LEGACY_PREFERRED_MARKET] = marketName
        }
    }

    suspend fun getOrCreateFarmerRegistrationId(): String {
        var registrationId = ""
        dataStore.edit { preferences ->
            val existing = preferences[Keys.FARMER_REGISTRATION_ID]
                ?: preferences[Keys.LEGACY_FARMER_ID]
                ?: ""
            registrationId = if (existing.isNotBlank()) {
                existing
            } else {
                UUID.randomUUID().toString().also {
                    preferences[Keys.FARMER_REGISTRATION_ID] = it
                    preferences[Keys.LEGACY_FARMER_ID] = it
                }
            }
        }
        return registrationId
    }

    private object Keys {
        val FARMER_NAME = stringPreferencesKey("farmerName")
        val FARMER_REGISTRATION_ID = stringPreferencesKey("farmerRegistrationId")
        val LEGACY_FARMER_ID = stringPreferencesKey("farmerId")
        val SELECTED_LANGUAGE = stringPreferencesKey("selectedLanguage")
        val LEGACY_SELECTED_LANGUAGE = stringPreferencesKey("language")
        val PREFERRED_MARKET_ID = stringPreferencesKey("preferredMarketId")
        val PREFERRED_MARKET_NAME = stringPreferencesKey("preferredMarketName")
        val LEGACY_PREFERRED_MARKET = stringPreferencesKey("preferredMarket")
    }
}
