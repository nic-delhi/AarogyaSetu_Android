package nic.goi.aarogyasetu;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;

import java.util.List;
import java.util.concurrent.Executors;

import io.fabric.sdk.android.Fabric;
import nic.goi.aarogyasetu.utility.CorUtility;

/**
 * @author Chandrapal Yadav
 * @author Niharika.Arora
 */
public class CoronaApplication extends Application implements Configuration.Provider {

    public static CoronaApplication instance;
     static Location bestLocation = null;
    
    public static CoronaApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        instance = this;
        WorkManager.initialize(
                this,
                new Configuration.Builder()
                        .setExecutor(Executors.newFixedThreadPool(8))
                        .build());
        new Thread(() -> {
            Fabric.with(CoronaApplication.getInstance(), new Crashlytics());
        }).start();

    }

    public void setBestLocation(Location location)
    {
        bestLocation = location;
    }

    public Location getLastKnownLocation() {

        if(CorUtility.Companion.isLocationPermissionAvailable(CoronaApplication.getInstance()))
        {
            LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            List<String> providers = mLocationManager.getProviders(true);
            for (String provider : providers) {
                try {
                    Location l = mLocationManager.getLastKnownLocation(provider);
                    if (l == null) {
                        continue;
                    }
                    if (bestLocation == null || l.getAccuracy() > bestLocation.getAccuracy()) {
                        bestLocation = l;
                    }
                }catch (SecurityException e){

                }
            }
        }
        return bestLocation;
    }

    public static void warmUpLocation() {
        if (CorUtility.Companion.isLocationPermissionAvailable(CoronaApplication.getInstance())) {
            LocationServices.getFusedLocationProviderClient(CoronaApplication.getInstance()).getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    bestLocation = location;
                }
            });
        }
    }

    public Context getContext() {
        return getApplicationContext();
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().setExecutor(Executors.newFixedThreadPool(8)).build();
    }




}
