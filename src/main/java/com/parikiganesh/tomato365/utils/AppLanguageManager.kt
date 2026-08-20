package com.parikiganesh.tomato365.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppLanguageManager {

    fun applyLanguage(languageCode: String) {
        val normalizedCode = when (languageCode.trim().lowercase()) {
            "te", "telugu", "తెలుగు" -> "te"
            else -> "en"
        }

        val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (currentTags == normalizedCode) return

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(normalizedCode)
        )
    }
}
