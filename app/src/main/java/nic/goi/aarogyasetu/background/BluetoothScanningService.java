package nic.goi.aarogyasetu.background;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import nic.goi.aarogyasetu.BuildConfig;
import nic.goi.aarogyasetu.CoronaApplication;
import nic.goi.aarogyasetu.GattServer;
import nic.goi.aarogyasetu.R;
import nic.goi.aarogyasetu.db.DBManager;
import nic.goi.aarogyasetu.db.FightCovidDB;
import nic.goi.aarogyasetu.firebase.FirebaseRemoteConfigUtil;
import nic.goi.aarogyasetu.location.RetrieveLocationService;
import nic.goi.aarogyasetu.models.BluetoothData;
import nic.goi.aarogyasetu.models.WhiteListData;
import nic.goi.aarogyasetu.utility.AdaptiveScanHelper;
import nic.goi.aarogyasetu.utility.Constants;
import nic.goi.aarogyasetu.utility.CorUtility;
import nic.goi.aarogyasetu.utility.CorUtilityKt;
import nic.goi.aarogyasetu.utility.ExecutorHelper;
import nic.goi.aarogyasetu.utility.Logger;
import nic.goi.aarogyasetu.models.BluetoothModel;
import nic.goi.aarogyasetu.utility.WhiteListBroadcastReceiver;
import nic.goi.aarogyasetu.views.SplashActivity;

/**
 * @author Niharika.Arora
 * @author chandrapal.yadav
 */
public class BluetoothScanningService extends Service implements AdaptiveScanHelper.AdaptiveModeListener {

    private RetrieveLocationService retrieveLocationService;
    private BluetoothLeScanner mBluetoothLeScanner;
    private AdaptiveScanHelper mAdaptiveScanHelper;
    private List<String> mData = new ArrayList<>();

    private static final int FIVE_MINUTES = 5 * 60 * 1000;
    private long searchTimestamp;
    private List<String> whiteListDevices = new ArrayList<>();          //Id's for devices that are whitelisted
    private List<String> currentNearDevices = new ArrayList<>();        //Id's for current nearby available devices
    private AlertDialog breachDialog;
    private static final int breachRssiRange = -50;

    private final GattServer mGattServer = new GattServer();

    private static final int NOTIF_ID = 1973;
    private String TAG = this.getClass().getName();
    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (CorUtility.isBluetoothPermissionAvailable(CoronaApplication.instance)) {
                //#Impetus add new device dialog here using result.getDevice()
                if (result == null || result.getDevice() == null){
                    return;
                }
                Logger.d(TAG, "onScanResult : Scanning : " + result.getDevice() + " RSSI:" + result.getRssi());
                if (result.getRssi() > breachRssiRange) {
                    if (!whiteListDevices.contains(result.getDevice().getAddress()) && !currentNearDevices.contains(result.getDevice().getAddress())) {
                            currentNearDevices.add(result.getDevice().getAddress());
                        if (CoronaApplication.appIsInBackground) {
                            showBreachNotification(result);
                        } else {
                            showDeviceDialog(result);
                        }
                    } else {
                        Logger.d(TAG, "Nearby whitelist device found:" + result.getDevice());
                    }
                }
                if (result.getDevice().getName() == null)
                    return;
                String deviceName = result.getDevice().getName();
                clearList();
                mAdaptiveScanHelper.addScanResult(result);
                if (mData.contains(deviceName)) {
                    return;
                }

                String txPower = Constants.EMPTY;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    txPower = String.valueOf(result.getTxPower());
                }
                String txPowerLevel = "";
                if (result.getScanRecord() != null) {
                    txPowerLevel = String.valueOf(result.getScanRecord().getTxPowerLevel());
                }
                BluetoothModel bluetoothModel = new BluetoothModel(result.getDevice().getName(),
                        deviceName, result.getRssi(), txPower, txPowerLevel);
                mData.add(deviceName);
                storeDetectedUserDeviceInDB(bluetoothModel);
                Logger.d(TAG, "onScanResult : Information Updated, Device : " + deviceName);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Logger.d(TAG, "onBatchScanResults : Devices : " + results.toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Logger.e(TAG, "onScanFailed : errorCode : " + errorCode);
        }
    };

    public static boolean serviceRunning;
    private Timer timer;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = getNotification(Constants.NOTIFICATION_DESC);
        startForeground(NOTIF_ID, notification);
        searchTimestamp = System.currentTimeMillis();
        //fetch current list of whiteListed devices from DB
        ExecutorHelper.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                whiteListDevices = FightCovidDB.getInstance().getWhiteListDataDao().getAllWhiteListDevices();
            }
        });

    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            String channelId = CorUtility.Companion.getNotificationChannel();
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.enableLights(false);
            channel.setSound(null, null);
            channel.setShowBadge(false);

            channel.setDescription(Constants.NOTIFICATION_DESC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showDeviceDialog(ScanResult scanResult) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, "Error while playing sound " + e.toString());
        }
        if (breachDialog != null && breachDialog.isShowing()) {
            if (currentNearDevices.size() > 1) {
                breachDialog.setMessage(getString(R.string.multiple_breaches_message) + new Gson().toJson(currentNearDevices).toString());
            } else {
                String deviceName = scanResult.getDevice().getName() != null ? scanResult.getDevice().getName() : scanResult.getDevice().getAddress();
                breachDialog.setMessage(getString(R.string.single_breache_message));
            }
        } else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getApplicationContext(), R.style.MaterialDialogTheme);
            dialogBuilder.setTitle(getString(R.string.title_distance_breach));
            dialogBuilder.setMessage(getString(R.string.single_breache_message));
            dialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    currentNearDevices.clear();
                }
            });

            //Add this device to white list if it is my own device or someone from my family
            dialogBuilder.setNeutralButton(getString(R.string.btn_white_list_device), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DBManager.insertWhiteListData(new WhiteListData(scanResult.getDevice().getName(), scanResult.getDevice().getAddress()));
                    dialog.dismiss();
                    currentNearDevices.clear();
                }
            });
            breachDialog = dialogBuilder.create();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                breachDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY - 1);
            } else {
                breachDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
            breachDialog.show();
        }
    }

    private void showBreachNotification(ScanResult scanResult) {
        Notification notification = getBreachNotification(scanResult);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
// notificationId is a unique int for each notification that you must define
        notificationManager.notify(12, notification);
    }

    private Notification getBreachNotification(ScanResult scanResult) {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Intent notificationIntent = new Intent(this, SplashActivity.class);
        notificationIntent.putExtra("device", scanResult.getDevice());
        PendingIntent notificationClickIntent = PendingIntent.getActivities(this, 0, new Intent[]{notificationIntent}, PendingIntent.FLAG_UPDATE_CURRENT);

        String notificationDescText = getString(R.string.single_breache_message);


        // Add action button in the notification
        Intent snoozeIntent = new Intent(this, WhiteListBroadcastReceiver.class);
        snoozeIntent.setAction(Constants.ACTION_WHITELIST_DEVICE);
        snoozeIntent.putExtra(Constants.ARGS_DEVICE_ADDRESS, scanResult.getDevice().getAddress());
        snoozeIntent.putExtra(Constants.ARGS_DEVICE_NAME, scanResult.getDevice().getName());
        snoozeIntent.putExtra(Constants.ARGS_NOTIFICATION_ID, 12);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(this, 0, snoozeIntent, 0);

        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? CorUtility.Companion.getNotificationChannel() : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(getResources().getString(R.string.app_name));
        bigTextStyle.bigText(notificationDescText);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

        return notificationBuilder
                .setStyle(bigTextStyle)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentText(notificationDescText)
                .setContentIntent(notificationClickIntent)
                .setOngoing(true)
                .setSound(soundUri)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.notification_icon)
                .addAction(R.drawable.notification_icon, getString(R.string.btn_white_list_device),
                        snoozePendingIntent)
                .build();
    }


    /**
     * Method to clear list after specified scan poll time for same device scanning
     */
    void clearList() {
        long scanPollTime = FirebaseRemoteConfigUtil.getINSTANCE().getScanPollTime();
        long pollTime = scanPollTime * 1000;
        long difference = System.currentTimeMillis() - searchTimestamp;
        if (difference >= pollTime && !mData.isEmpty()) {
            searchTimestamp = System.currentTimeMillis();
            mData.clear();
        }
    }

    /**
     * Method to restart advertisement and scanning in every 5 min.
     */
    private void advertiseAndScan() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

                                      @Override
                                      public void run() {
                                          if (isBluetoothAvailable()) {
                                              mGattServer.advertise(mAdaptiveScanHelper.getAdvertisementMode());
                                              discover(mAdaptiveScanHelper.getScanMode());
                                          }
                                      }
                                  },
                0,
                FIVE_MINUTES);
        mAdaptiveScanHelper.start();
    }


    /**
     * Start scanning BLE devices with provided scan mode
     *
     * @param scanMode
     */
    private void discover(int scanMode) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return;
        }
        mBluetoothLeScanner = adapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            return;
        }
//        List<ScanFilter> filters = new ArrayList<>();
//
//        ScanFilter filter = new ScanFilter.Builder()
//                .setServiceUuid(new ParcelUuid(UUID.fromString(BuildConfig.SERVICE_UUID)))
//                .build();
//        filters.add(filter);
        ScanSettings.Builder settings = new ScanSettings.Builder()
                .setScanMode(scanMode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            settings.setMatchMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setLegacy(false);
            settings.setPhy(BluetoothDevice.PHY_LE_1M);
        }
        try {
            if (isBluetoothAvailable()) {
                mBluetoothLeScanner.startScan(null, settings.build(), mScanCallback);
            } else {
                Logger.e(TAG, "startingScan failed : Bluetooth not available");
            }
        } catch (Exception ex) {
            //Handle Android internal exception for BT adapter not turned ON(Known Android bug)
            CorUtilityKt.reportException(ex);
        }
    }


    /**
     * This method will store the detected device info into the local database to query in future if the need arise
     * to push the data
     *
     * @param bluetoothModel The newly detected device nearby
     */
    void storeDetectedUserDeviceInDB(BluetoothModel bluetoothModel) {
        if (bluetoothModel != null) {
            BluetoothData bluetoothData = new BluetoothData(bluetoothModel.getAddress(), bluetoothModel.getRssi(),
                    bluetoothModel.getTxPower(), bluetoothModel.getTxPowerLevel());
            Location loc = CoronaApplication.getInstance().getAppLastLocation();
            if (loc != null) {
                bluetoothData.setLatitude(loc.getLatitude());
                bluetoothData.setLongitude(loc.getLongitude());
            }
            DBManager.insertNearbyDetectedDeviceInfo(bluetoothData);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        serviceRunning = true;
        configureNotification();
        mAdaptiveScanHelper = new AdaptiveScanHelper(this);
        mGattServer.onCreate(BluetoothScanningService.this);
        mGattServer.addGattService();
        advertiseAndScan();
        startLocationUpdate();
        registerBluetoothStateListener();
        registerLocationStateListener();
        Logger.d(TAG, "onStartCommand service started");
        return START_STICKY;
    }

    private void configureNotification() {
        Notification notification;
        if (!CorUtility.isLocationOn(CoronaApplication.instance.getContext())) {
            notification = getNotification(Constants.PLEASE_ALLOW_LOCATION);
        } else if (!CorUtility.isBluetoothAvailable()) {
            notification = getNotification(Constants.PLEASE_ALLOW_BLUETOOTH);
        } else {
            notification = getNotification(Constants.NOTIFICATION_DESC);
        }
        startForeground(NOTIF_ID, notification);
    }

    private Notification getNotification(String notificationDescText) {
        Intent notificationIntent = new Intent(this, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{notificationIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? CorUtility.Companion.getNotificationChannel() : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(getResources().getString(R.string.app_name));
        bigTextStyle.bigText(notificationDescText);
        return notificationBuilder
                .setStyle(bigTextStyle)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(notificationDescText)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSound(null)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.notification_icon)
                .build();
    }

    private void startLocationUpdate() {
        retrieveLocationService = new RetrieveLocationService();
        retrieveLocationService.startService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, "onDestroy");
        super.onDestroy();
        serviceRunning = false;
        try {
            if (mBluetoothStatusChangeReceiver != null) {
                unregisterReceiver(mBluetoothStatusChangeReceiver);
            }
            if (mLocationChangeListener != null) {
                unregisterReceiver(mLocationChangeListener);
            }
            stopForeground(true);
            if (retrieveLocationService != null) {
                retrieveLocationService.stopService();
            }
            if (mBluetoothLeScanner != null && isBluetoothAvailable()) {
                mBluetoothLeScanner.stopScan(mScanCallback);
            }
            if (timer != null) {
                timer.cancel();
            }
            mGattServer.onDestroy();
            if (mAdaptiveScanHelper != null) {
                mAdaptiveScanHelper.reset();
            }
        } catch (Exception ex) {
            //As this exception doesn't matter for user,service already destroying,so just logging this on firebase
            CorUtilityKt.reportException(ex);
        }

    }

    public static boolean isBluetoothAvailable() {
        return CorUtility.isBluetoothAvailable();
    }

    private void registerBluetoothStateListener() {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStatusChangeReceiver, filter);
    }

    private void registerLocationStateListener() {
        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(mLocationChangeListener, filter);
    }

    private BroadcastReceiver mLocationChangeListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Notification notification;
            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(action)) {
                if (!CorUtility.isLocationOn(CoronaApplication.instance.getContext())) {
                    notification = getNotification(Constants.PLEASE_ALLOW_LOCATION);
                    updateNotification(notification);

                } else {
                    if (isBluetoothAvailable()) {
                        notification = getNotification(Constants.NOTIFICATION_DESC);
                        updateNotification(notification);
                    } else {
                        notification = getNotification(Constants.PLEASE_ALLOW_BLUETOOTH);
                        updateNotification(notification);
                    }

                }
            }
        }
    };

    private BroadcastReceiver mBluetoothStatusChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Notification notification;
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        mGattServer.stopServer();
                        break;

                    case BluetoothAdapter.STATE_OFF:
                        notification = getNotification(Constants.PLEASE_ALLOW_BLUETOOTH);
                        updateNotification(notification);
                        mAdaptiveScanHelper.stop();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        if (!CorUtility.isLocationOn(CoronaApplication.instance.getContext())) {
                            notification = getNotification(Constants.PLEASE_ALLOW_LOCATION);
                            updateNotification(notification);
                        } else {
                            notification = getNotification(Constants.NOTIFICATION_DESC);
                            updateNotification(notification);
                        }
                        mGattServer.addGattService();
                        advertiseAndScan();
                        break;
                }
            }
        }
    };

    private void updateNotification(Notification notification) {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(NOTIF_ID, notification);
        }
    }

    @Override
    public void onLowMemory() {
        Logger.d(TAG, "onLowMemory");
        super.onLowMemory();
        stopSelf();
        serviceRunning = false;
    }

    @Override
    public void onModeChange(int scanMode, int advertisementMode) {
        try {
            if (isBluetoothAvailable()) {
                if (mBluetoothLeScanner != null) {
                    mBluetoothLeScanner.stopScan(mScanCallback);
                }
                mGattServer.stopAdvertising();
                discover(scanMode);
                mGattServer.advertise(advertisementMode);
            } else {
                Logger.d(TAG, "onModeChange failed due to bluetooth not available");
            }
        } catch (Exception ex) {
            //Handle Android internal exception for BT adapter not turned ON(Known Android bug)
            CorUtilityKt.reportException(ex);
        }
    }
}
