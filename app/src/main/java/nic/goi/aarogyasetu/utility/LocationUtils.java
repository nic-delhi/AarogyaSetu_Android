package nic.goi.aarogyasetu.utility;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import nic.goi.aarogyasetu.R;

public class LocationUtils {
    public static final int LOCATION_REQUEST = 1245;
    private Context context;

    public LocationUtils(Context context) {
        this.context = context;
    }

    public void turnLocationOn(TurnLocationListener turnLocationListener) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        LocationSettingsRequest mLocationSettingsRequest = builder.build();

        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(mLocationSettingsRequest);

        task.addOnSuccessListener((Activity) context, locationSettingsResponse -> {

            if (turnLocationListener != null) {
                turnLocationListener.locationStatus(true);
            }
        });

        task.addOnFailureListener((Activity) context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(((Activity) context),
                                LOCATION_REQUEST);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                        Toast.makeText(context, LocalizationUtil.getLocalisedString(context, R.string.error_location), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public interface TurnLocationListener {
        void locationStatus(boolean isTurnOn);
    }
}
