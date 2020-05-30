package nic.goi.aarogyasetu

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.work.Configuration
import androidx.work.WorkManager

import com.crashlytics.android.Crashlytics
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import java.util.concurrent.Executors

import io.fabric.sdk.android.Fabric
import nic.goi.aarogyasetu.utility.CorUtility

/**
 * @author Chandrapal Yadav
 * @author Niharika.Arora
 */
class CoronaApplication : Application(), Configuration.Provider {

    val appLastLocation: Location?
        get() = lastKnownLocation

    val deviceLastKnownLocation: Location?
        get() {

            if (CorUtility.Companion.isLocationPermissionAvailable(CoronaApplication.instance)) {
                val mLocationManager = getApplicationContext().getSystemService(LOCATION_SERVICE) as LocationManager
                val providers = mLocationManager.getProviders(true)
                for (provider in providers) {
                    try {
                        val l = mLocationManager.getLastKnownLocation(provider) ?: continue
                        if (lastKnownLocation == null || l.getAccuracy() > lastKnownLocation!!.getAccuracy()) {
                            lastKnownLocation = l
                        }
                    } catch (e: SecurityException) {

                    }

                }
            }
            return lastKnownLocation
        }

    val context: Context
        get() = getApplicationContext()

    val workManagerConfiguration: Configuration
        @NonNull
        @Override
        get() = Configuration.Builder().setExecutor(Executors.newFixedThreadPool(8)).build()

    @Override
    fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        instance = this
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setExecutor(Executors.newFixedThreadPool(8))
                .build()
        )
        Thread { Fabric.with(CoronaApplication.instance, Crashlytics()) }.start()

    }

    fun setBestLocation(location: Location) {
        lastKnownLocation = location
    }

    companion object {

        var instance: CoronaApplication
        internal var lastKnownLocation: Location? = null
        /* warmUpLocation */
        fun warmUpLocation() {
            if (CorUtility.Companion.isLocationPermissionAvailable(CoronaApplication.instance)) {
                LocationServices.getFusedLocationProviderClient(CoronaApplication.instance).getLastLocation()
                    .addOnSuccessListener({ location ->
                        if (location != null) {
                            lastKnownLocation = location
                        }
                    })
            }
        }
    }


}
