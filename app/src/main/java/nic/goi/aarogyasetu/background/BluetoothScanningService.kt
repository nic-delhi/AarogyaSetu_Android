package nic.goi.aarogyasetu.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import androidx.core.app.NotificationCompat

import java.util.ArrayList
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

import nic.goi.aarogyasetu.BuildConfig
import nic.goi.aarogyasetu.CoronaApplication
import nic.goi.aarogyasetu.GattServer
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.db.DBManager
import nic.goi.aarogyasetu.firebase.FirebaseRemoteConfigUtil
import nic.goi.aarogyasetu.location.RetrieveLocationService
import nic.goi.aarogyasetu.models.BluetoothData
import nic.goi.aarogyasetu.utility.AdaptiveScanHelper
import nic.goi.aarogyasetu.utility.Constants
import nic.goi.aarogyasetu.utility.CorUtility
import nic.goi.aarogyasetu.utility.CorUtilityKt
import nic.goi.aarogyasetu.utility.Logger
import nic.goi.aarogyasetu.models.BluetoothModel
import nic.goi.aarogyasetu.views.SplashActivity

/**
 * @author Niharika.Arora
 * @author chandrapal.yadav
 */
class BluetoothScanningService : Service(), AdaptiveScanHelper.AdaptiveModeListener {

    private var retrieveLocationService: RetrieveLocationService? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    private var mAdaptiveScanHelper: AdaptiveScanHelper? = null
    private val mData = ArrayList()
    private var searchTimestamp: Long = 0

    private val mGattServer = GattServer()
    private val TAG = this.getClass().getName()
    private val mScanCallback = object : ScanCallback() {

        @Override
        fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Logger.d(TAG, "onScanResult : Scanning : " + result!!.getDevice().getName())
            if (CorUtility.isBluetoothPermissionAvailable(CoronaApplication.instance)) {
                if (result == null || result!!.getDevice() == null || result!!.getDevice().getName() == null)
                    return
                val deviceName = result!!.getDevice().getName()
                clearList()
                mAdaptiveScanHelper!!.addScanResult(result)
                if (mData.contains(deviceName)) {
                    return
                }

                var txPower = Constants.EMPTY
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    txPower = String.valueOf(result!!.getTxPower())
                }
                var txPowerLevel = ""
                if (result!!.getScanRecord() != null) {
                    txPowerLevel = String.valueOf(result!!.getScanRecord().getTxPowerLevel())
                }
                val bluetoothModel = BluetoothModel(
                    result!!.getDevice().getName(),
                    deviceName, result!!.getRssi(), txPower, txPowerLevel
                )
                mData.add(deviceName)
                storeDetectedUserDeviceInDB(bluetoothModel)
                Logger.d(TAG, "onScanResult : Information Updated, Device : $deviceName")
            }
        }

        @Override
        fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            Logger.d(TAG, "onBatchScanResults : Devices : $results")
        }

        @Override
        fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Logger.e(TAG, "onScanFailed : errorCode : $errorCode")
        }
    }
    private var timer: Timer? = null

    private val mLocationChangeListener = object : BroadcastReceiver() {
        @Override
        fun onReceive(context: Context, intent: Intent) {
            val action = intent.getAction()
            val notification: Notification
            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(action)) {
                if (!CorUtility.isLocationOn(CoronaApplication.instance.getContext())) {
                    notification = getNotification(Constants.PLEASE_ALLOW_LOCATION)
                    updateNotification(notification)

                } else {
                    if (isBluetoothAvailable) {
                        notification = getNotification(Constants.NOTIFICATION_DESC)
                        updateNotification(notification)
                    } else {
                        notification = getNotification(Constants.PLEASE_ALLOW_BLUETOOTH)
                        updateNotification(notification)
                    }

                }
            }
        }
    }

    private val mBluetoothStatusChangeReceiver = object : BroadcastReceiver() {
        @Override
        fun onReceive(context: Context, intent: Intent) {
            val action = intent.getAction()
            val notification: Notification
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_TURNING_OFF -> mGattServer.stopServer()

                    BluetoothAdapter.STATE_OFF -> {
                        notification = getNotification(Constants.PLEASE_ALLOW_BLUETOOTH)
                        updateNotification(notification)
                        mAdaptiveScanHelper!!.stop()
                    }
                    BluetoothAdapter.STATE_ON -> {
                        if (!CorUtility.isLocationOn(CoronaApplication.instance.getContext())) {
                            notification = getNotification(Constants.PLEASE_ALLOW_LOCATION)
                            updateNotification(notification)
                        } else {
                            notification = getNotification(Constants.NOTIFICATION_DESC)
                            updateNotification(notification)
                        }
                        mGattServer.addGattService()
                        advertiseAndScan()
                    }
                }
            }
        }
    }

    @Override
    fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = getNotification(Constants.NOTIFICATION_DESC)
        startForeground(NOTIF_ID, notification)
        searchTimestamp = System.currentTimeMillis()

    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channelId = CorUtility.Companion.getNotificationChannel()
            val channel = NotificationChannel(channelId, name, importance)
            channel.enableLights(false)
            channel.setSound(null, null)
            channel.setShowBadge(false)

            channel.setDescription(Constants.NOTIFICATION_DESC)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager != null) {
                notificationManager!!.createNotificationChannel(channel)
            }
        }
    }


    /**
     * Method to clear list after specified scan poll time for same device scanning
     */
    internal fun clearList() {
        val scanPollTime = FirebaseRemoteConfigUtil.getINSTANCE().getScanPollTime()
        val pollTime = scanPollTime * 1000
        val difference = System.currentTimeMillis() - searchTimestamp
        if (difference >= pollTime && !mData.isEmpty()) {
            searchTimestamp = System.currentTimeMillis()
            mData.clear()
        }
    }

    /**
     * Method to restart advertisement and scanning in every 5 min.
     */
    private fun advertiseAndScan() {
        if (timer != null) {
            timer!!.cancel()
        }
        timer = Timer()
        timer!!.scheduleAtFixedRate(
            object : TimerTask() {

                @Override
                fun run() {
                    if (isBluetoothAvailable) {
                        mGattServer.advertise(mAdaptiveScanHelper!!.getAdvertisementMode())
                        discover(mAdaptiveScanHelper!!.getScanMode())
                    }
                }
            },
            0,
            FIVE_MINUTES
        )
        mAdaptiveScanHelper!!.start()
    }


    /**
     * Start scanning BLE devices with provided scan mode
     * @param scanMode
     */
    private fun discover(scanMode: Int) {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
        mBluetoothLeScanner = adapter.getBluetoothLeScanner()
        if (mBluetoothLeScanner == null) {
            return
        }
        val filters = ArrayList()

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID.fromString(BuildConfig.SERVICE_UUID)))
            .build()
        filters.add(filter)
        val settings = ScanSettings.Builder()
            .setScanMode(scanMode)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            settings.setMatchMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setLegacy(false)
            settings.setPhy(BluetoothDevice.PHY_LE_1M)
        }
        try {
            if (isBluetoothAvailable) {
                mBluetoothLeScanner!!.startScan(filters, settings.build(), mScanCallback)
            } else {
                Logger.e(TAG, "startingScan failed : Bluetooth not available")
            }
        } catch (ex: Exception) {
            //Handle Android internal exception for BT adapter not turned ON(Known Android bug)
            CorUtilityKt.reportException(ex)
        }

    }


    /**
     * This method will store the detected device info into the local database to query in future if the need arise
     * to push the data
     *
     * @param bluetoothModel The newly detected device nearby
     */
    internal fun storeDetectedUserDeviceInDB(bluetoothModel: BluetoothModel?) {
        if (bluetoothModel != null) {
            val bluetoothData = BluetoothData(
                bluetoothModel!!.getAddress(), bluetoothModel!!.getRssi(),
                bluetoothModel!!.getTxPower(), bluetoothModel!!.getTxPowerLevel()
            )
            val loc = CoronaApplication.getInstance().getAppLastLocation()
            if (loc != null) {
                bluetoothData.setLatitude(loc!!.getLatitude())
                bluetoothData.setLongitude(loc!!.getLongitude())
            }
            DBManager.insertNearbyDetectedDeviceInfo(bluetoothData)
        }

    }

    @Override
    fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        serviceRunning = true
        val notification = getNotification(Constants.NOTIFICATION_DESC)
        startForeground(NOTIF_ID, notification)
        mAdaptiveScanHelper = AdaptiveScanHelper(this)
        mGattServer.onCreate(this@BluetoothScanningService)
        mGattServer.addGattService()
        advertiseAndScan()
        startLocationUpdate()
        registerBluetoothStateListener()
        registerLocationStateListener()
        Logger.d(TAG, "onStartCommand service started")
        return START_STICKY
    }

    private fun getNotification(notificationDescText: String): Notification {
        val notificationIntent = Intent(this, SplashActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivities(this, 0, arrayOf<Intent>(notificationIntent), PendingIntent.FLAG_UPDATE_CURRENT)
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) CorUtility.Companion.getNotificationChannel() else ""
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle(getResources().getString(R.string.app_name))
        bigTextStyle.bigText(notificationDescText)
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
            .build()
    }

    private fun startLocationUpdate() {
        retrieveLocationService = RetrieveLocationService()
        retrieveLocationService!!.startService()
    }

    @Nullable
    @Override
    fun onBind(intent: Intent): IBinder? {
        return null
    }

    @Override
    fun onDestroy() {
        Logger.d(TAG, "onDestroy")
        super.onDestroy()
        serviceRunning = false
        try {
            if (mBluetoothStatusChangeReceiver != null) {
                unregisterReceiver(mBluetoothStatusChangeReceiver)
            }
            if (mLocationChangeListener != null) {
                unregisterReceiver(mLocationChangeListener)
            }
            stopForeground(true)
            if (retrieveLocationService != null) {
                retrieveLocationService!!.stopService()
            }
            if (mBluetoothLeScanner != null && isBluetoothAvailable) {
                mBluetoothLeScanner!!.stopScan(mScanCallback)
            }
            if (timer != null) {
                timer!!.cancel()
            }
            mGattServer.onDestroy()
            if (mAdaptiveScanHelper != null) {
                mAdaptiveScanHelper!!.reset()
            }
        } catch (ex: Exception) {
            //As this exception doesn't matter for user,service already destroying,so just logging this on firebase
            CorUtilityKt.reportException(ex)
        }

    }

    private fun registerBluetoothStateListener() {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(mBluetoothStatusChangeReceiver, filter)
    }

    private fun registerLocationStateListener() {
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(mLocationChangeListener, filter)
    }

    private fun updateNotification(notification: Notification) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager != null) {
            notificationManager!!.notify(NOTIF_ID, notification)
        }
    }

    @Override
    fun onLowMemory() {
        Logger.d(TAG, "onLowMemory")
        super.onLowMemory()
        stopSelf()
        serviceRunning = false
    }

    @Override
    fun onModeChange(scanMode: Int, advertisementMode: Int) {
        if (isBluetoothAvailable) {
            if (mBluetoothLeScanner != null) {
                mBluetoothLeScanner!!.stopScan(mScanCallback)
            }
            mGattServer.stopAdvertising()
            discover(scanMode)
            mGattServer.advertise(advertisementMode)
        } else {
            Logger.d(TAG, "onModeChange failed due to bluetooth not available")
        }
    }

    companion object {

        private val FIVE_MINUTES = 5 * 60 * 1000

        private val NOTIF_ID = 1973

        var serviceRunning: Boolean = false

        val isBluetoothAvailable: Boolean
            get() = CorUtility.isBluetoothAvailable()
    }
}
