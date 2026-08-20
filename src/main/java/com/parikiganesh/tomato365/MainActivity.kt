package com.parikiganesh.tomato365

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.parikiganesh.tomato365.navigation.AppNavGraph
import com.parikiganesh.tomato365.data.local.UserPreferencesDataStore
import com.parikiganesh.tomato365.notifications.NotificationScheduler
import com.parikiganesh.tomato365.ui.components.UpdateAvailableDialog
import com.parikiganesh.tomato365.ui.components.UpdateDownloadingDialog
import com.parikiganesh.tomato365.ui.components.UpdateReadyDialog
import com.parikiganesh.tomato365.ui.theme.TomatoPricesTheme
import com.parikiganesh.tomato365.utils.AppLanguageManager
import com.parikiganesh.tomato365.utils.Constants
import com.parikiganesh.tomato365.utils.InAppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var userPreferencesDataStore: UserPreferencesDataStore
    
    @Inject
    lateinit var notificationScheduler: NotificationScheduler
    
    private var pendingNotificationMarketName by mutableStateOf<String?>(null)
    private lateinit var inAppUpdateManager: InAppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize InAppUpdateManager with Play Core
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val firestore = FirebaseFirestore.getInstance()
        inAppUpdateManager = InAppUpdateManager(this, appUpdateManager, firestore)
        inAppUpdateManager.checkForUpdates()
        
        // Schedule daily notifications (7 AM & 6 PM)
        notificationScheduler.scheduleNotifications()
        
        pendingNotificationMarketName = extractNotificationMarketName(intent)
        lifecycleScope.launch {
            userPreferencesDataStore.userPreferences
                .map { it.selectedLanguage }
                .distinctUntilChanged()
                .collect { languageCode ->
                    AppLanguageManager.applyLanguage(languageCode)
                }
        }
        enableEdgeToEdge()
        setContent {
            TomatoPricesTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(notificationMarketName = pendingNotificationMarketName)
                    
                    // Show update dialogs based on update state
                    val updateState = inAppUpdateManager.updateState.value
                    when (updateState) {
                        is InAppUpdateManager.UpdateState.Available -> {
                            UpdateAvailableDialog(
                                onUpdateClick = { inAppUpdateManager.startFlexibleUpdate() },
                                onLaterClick = { inAppUpdateManager.updateState.value = InAppUpdateManager.UpdateState.NoUpdate }
                            )
                        }
                        is InAppUpdateManager.UpdateState.Downloading -> {
                            UpdateDownloadingDialog(progress = updateState.progress)
                        }
                        is InAppUpdateManager.UpdateState.ReadyToInstall -> {
                            UpdateReadyDialog(
                                onInstallClick = { inAppUpdateManager.completeFlexibleUpdate() }
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationMarketName = extractNotificationMarketName(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppUpdateManager.unregisterListener()
    }

    private fun extractNotificationMarketName(intent: Intent?): String? {
        val marketName = intent?.getStringExtra(Constants.EXTRA_NOTIFICATION_MARKET_NAME).orEmpty()
        return marketName.ifBlank { null }
    }
}
