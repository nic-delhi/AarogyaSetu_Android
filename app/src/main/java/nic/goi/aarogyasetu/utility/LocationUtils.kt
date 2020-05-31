package nic.goi.aarogyasetu.utility

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.widget.Toast

import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task

import nic.goi.aarogyasetu.R

class LocationUtils(private val context: Context) {

    fun turnLocationOn(turnLocationListener: TurnLocationListener?) {
        val locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val mLocationSettingsRequest = builder.build()

        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(mLocationSettingsRequest)

        task.addOnSuccessListener(context as Activity, { locationSettingsResponse ->

            turnLocationListener?.locationStatus(true)
        })

        task.addOnFailureListener(context as Activity, object : OnFailureListener() {
            @Override
            fun onFailure(@NonNull e: Exception) {
                if (e is ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        val resolvable = e as ResolvableApiException
                        resolvable.startResolutionForResult(
                            context as Activity,
                            LOCATION_REQUEST
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                        Toast.makeText(
                            context,
                            LocalizationUtil.getLocalisedString(context, R.string.error_location),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
        })
    }

    interface TurnLocationListener {
        fun locationStatus(isTurnOn: Boolean)
    }

    companion object {
        val LOCATION_REQUEST = 1245
    }
}
