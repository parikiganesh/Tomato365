package com.parikiganesh.tomato365.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.tomato365.data.local.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    fun saveFarmerSetup(
        farmerName: String,
        languageCode: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            userPreferencesDataStore.saveFarmerName(farmerName.trim())
            userPreferencesDataStore.saveSelectedLanguage(languageCode)
            onSaved()
        }
    }

    fun updateSelectedLanguage(languageCode: String) {
        viewModelScope.launch {
            userPreferencesDataStore.saveSelectedLanguage(languageCode)
        }
    }
}
