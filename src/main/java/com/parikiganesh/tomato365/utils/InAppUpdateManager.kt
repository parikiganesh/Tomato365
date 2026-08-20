package com.parikiganesh.tomato365.utils

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.firestore.FirebaseFirestore

class InAppUpdateManager(
    private val activity: Activity,
    private val appUpdateManager: AppUpdateManager,
    private val firestore: FirebaseFirestore
) {
    
    val updateState: MutableState<UpdateState> = mutableStateOf(UpdateState.NoUpdate)
    private val TAG = "InAppUpdateManager"
    
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
                val progress = if (totalBytesToDownload > 0) {
                    (bytesDownloaded * 100 / totalBytesToDownload).toInt()
                } else {
                    0
                }
                updateState.value = UpdateState.Downloading(progress)
                Log.d(TAG, "Download progress: $progress%")
            }
            InstallStatus.DOWNLOADED -> {
                Log.d(TAG, "Update downloaded, ready to install")
                updateState.value = UpdateState.ReadyToInstall
            }
            InstallStatus.INSTALLING, InstallStatus.PENDING -> {
                Log.d(TAG, "Update installing")
            }
            InstallStatus.INSTALLED -> {
                Log.d(TAG, "Update installed")
                updateState.value = UpdateState.NoUpdate
            }
            InstallStatus.FAILED -> {
                Log.e(TAG, "Update failed")
                updateState.value = UpdateState.Error("Update installation failed")
            }
            else -> {}
        }
    }
    
    fun checkForUpdates() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val updateAvailability = appUpdateInfo.updateAvailability()
            val isUpdateAvailable = updateAvailability == UpdateAvailability.UPDATE_AVAILABLE
            val stalenessDays = appUpdateInfo.clientVersionStalenessDays()
            val isImmediateUpdateAllowed = stalenessDays != null &&
                stalenessDays < 7 &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            val isFlexibleUpdateAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) &&
                (stalenessDays == null || stalenessDays < 14)
            
            Log.d(
                TAG,
                "Update available: $isUpdateAvailable, stalenessDays: $stalenessDays, " +
                    "Immediate: $isImmediateUpdateAllowed, Flexible: $isFlexibleUpdateAllowed"
            )
            
            // Check priority from Firestore
            if (isUpdateAvailable) {
                firestore.collection("app_config").document("version_info")
                    .get()
                    .addOnSuccessListener { document ->
                        val priority = document.getLong("priority")?.toInt() ?: 0
                        Log.d(TAG, "Update priority: $priority")
                        
                        when {
                            priority >= 4 && isImmediateUpdateAllowed -> {
                                Log.d(TAG, "Critical update - starting immediate update")
                                startImmediateUpdate(appUpdateInfo)
                            }
                            isFlexibleUpdateAllowed -> {
                                Log.d(TAG, "Optional update available")
                                updateState.value = UpdateState.Available(priority)
                            }
                            else -> {
                                Log.d(TAG, "No flexible update allowed")
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // If Firestore fails, default to flexible update
                        Log.w(TAG, "Could not fetch priority, defaulting to flexible", exception)
                        if (isFlexibleUpdateAllowed) {
                            updateState.value = UpdateState.Available(0)
                        }
                    }
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Failed to check for updates", exception)
            updateState.value = UpdateState.Error(exception.message ?: "Unknown error")
        }
    }
    
    fun startFlexibleUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            Log.d(TAG, "Starting flexible update")
            try {
                appUpdateManager.startUpdateFlow(
                    appUpdateInfo,
                    activity,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE)
                        .setAllowAssetPackDeletion(true)
                        .build()
                )
                appUpdateManager.registerListener(installStateUpdatedListener)
                updateState.value = UpdateState.Downloading(0)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting flexible update", e)
                updateState.value = UpdateState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            Log.d(TAG, "Starting immediate update")
            appUpdateManager.startUpdateFlow(
                appUpdateInfo,
                activity,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                    .setAllowAssetPackDeletion(true)
                    .build()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start immediate update", e)
            updateState.value = UpdateState.Error(e.message ?: "Unknown error")
        }
    }
    
    fun completeFlexibleUpdate() {
        Log.d(TAG, "Completing flexible update")
        try {
            appUpdateManager.completeUpdate()
        } catch (e: Exception) {
            Log.e(TAG, "Error completing update", e)
        }
    }
    
    fun unregisterListener() {
        try {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering listener", e)
        }
    }
    
    sealed class UpdateState {
        object NoUpdate : UpdateState()
        data class Available(val priority: Int) : UpdateState()
        data class Downloading(val progress: Int) : UpdateState()
        object ReadyToInstall : UpdateState()
        data class Error(val message: String) : UpdateState()
    }
}
