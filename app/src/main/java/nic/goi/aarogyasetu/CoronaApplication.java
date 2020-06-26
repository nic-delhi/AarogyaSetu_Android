package nic.goi.aarogyasetu;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
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
public class CoronaApplication extends Application implements Configuration.Provider, LifecycleObserver {
    public static CoronaApplication instance;
    static Location lastKnownLocation = null;
    private boolean appIsInBackground = true;

    public static CoronaApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
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
        lastKnownLocation = location;
    }

    public Location getAppLastLocation()
    {
        return lastKnownLocation;
    }

    public Location getDeviceLastKnownLocation() {

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
                    if (lastKnownLocation == null || l.getAccuracy() > lastKnownLocation.getAccuracy()) {
                        lastKnownLocation = l;
                    }
                }catch (SecurityException e){

                }
            }
        }
        return lastKnownLocation;
    }
    /* warmUpLocation */
    public static void warmUpLocation() {
        if (CorUtility.Companion.isLocationPermissionAvailable(CoronaApplication.getInstance())) {
            LocationServices.getFusedLocationProviderClient(CoronaApplication.getInstance()).getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    lastKnownLocation = location;
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

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        //App in background
        appIsInBackground = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        // App in foreground
        appIsInBackground = false;
    }


    /**
     * Is app in background boolean.
     *
     * @return the boolean
     */
    public boolean isAppInBackground() {
        return appIsInBackground;
    }
}
