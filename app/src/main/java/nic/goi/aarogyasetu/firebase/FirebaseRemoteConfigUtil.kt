package nic.goi.aarogyasetu.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings.Builder
import nic.goi.aarogyasetu.R

/**
 * @author Niharika
 *
 */
class FirebaseRemoteConfigUtil {

    companion object {

        private const val MAXIMUM_FETCH_INTERVAL_IN_SECONDS = 900L
        private const val SCAN_POLL_TIME = "scan_poll_time_android"
        private const val DISABLE_SYNC_CHOICE = "disable_sync_choice"
        private const val UPLOAD_ENABLED = "upload_enable"

        private const val ADAPTIVE_SCAN_ENABLED = "adaptive_scan_enable"
        private const val ADAPTIVE_SCAN_UPPER_BALANCED_UNIQUE_APP_DEVICES = "adaptive_scan_upper_balanced_unique_app_devices"
        private const val ADAPTIVE_SCAN_UPPER_BALANCED_ADVERTISEMENT_INTERVAL = "adaptive_scan_upper_balanced_advertisement_internal"
        private const val ADAPTIVE_SCAN_LOWER_BALANCED_UNIQUE_APP_DEVICES = "adaptive_scan_lower_balanced_unique_app_devices"
        private const val ADAPTIVE_SCAN_LOWER_BALANCED_ADVERTISEMENT_INTERVAL = "adaptive_scan_lower_balanced_advertisement_internal"
        private const val ADAPTIVE_SCAN_ACCEPTABLE_UNIQUE_DEVICE_DELTA = "adaptive_scan_acceptable_unique_device_delta"
        private const val ADAPTIVE_SCAN_K_SCAN_INTERVAL = "adaptive_scan_k_scan_interval"


        @JvmStatic
        val INSTANCE: FirebaseRemoteConfigUtil by lazy {
            FirebaseRemoteConfigUtil()
        }
    }

    private val firebaseRemoteConfig: FirebaseRemoteConfig by lazy {
        val builder = Builder()
        builder.minimumFetchIntervalInSeconds = MAXIMUM_FETCH_INTERVAL_IN_SECONDS
        FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(builder.build())
            setDefaultsAsync(R.xml.firebase_remoteconfig_defaults)
        }
    }

    fun init() {
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Fetched and activated
                }
            }
    }

    fun getScanPollTime(): Long {
        return firebaseRemoteConfig.getLong(SCAN_POLL_TIME)
    }

    fun disableSyncChoice(): Boolean {
        return firebaseRemoteConfig.getBoolean(DISABLE_SYNC_CHOICE)
    }

    fun isUploadEnabled(): Boolean {
        return firebaseRemoteConfig.getBoolean(UPLOAD_ENABLED)
    }

    fun isAdaptiveScanEnabled(): Boolean {
        return firebaseRemoteConfig.getBoolean(ADAPTIVE_SCAN_ENABLED)
    }

    fun getAdaptiveScanUpperBalancedUniqueAppDevices(): Long {
        return firebaseRemoteConfig.getLong(ADAPTIVE_SCAN_UPPER_BALANCED_UNIQUE_APP_DEVICES)
    }

    fun getAdaptiveScanUpperBalancedAdvertisementInterval(): Long {
        return firebaseRemoteConfig.getLong(ADAPTIVE_SCAN_UPPER_BALANCED_ADVERTISEMENT_INTERVAL)
    }

    fun getAdaptiveScanLowerBalancedUniqueAppDevices(): Long {
        return firebaseRemoteConfig.getLong(ADAPTIVE_SCAN_LOWER_BALANCED_UNIQUE_APP_DEVICES)
    }

    fun getAdaptiveScanLowerBalancedAdvertisementInterval(): Long {
        return firebaseRemoteConfig.getLong(ADAPTIVE_SCAN_LOWER_BALANCED_ADVERTISEMENT_INTERVAL)
    }

    fun getAdaptiveScanAcceptableUniqueDeviceDelta(): Long {
        return firebaseRemoteConfig.getLong(ADAPTIVE_SCAN_ACCEPTABLE_UNIQUE_DEVICE_DELTA)
    }

    fun getAdaptiveScanKScanInterval(): Long {
        return firebaseRemoteConfig.getLong(ADAPTIVE_SCAN_K_SCAN_INTERVAL)
    }
}