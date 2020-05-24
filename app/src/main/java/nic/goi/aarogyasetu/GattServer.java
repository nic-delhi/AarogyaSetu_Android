package nic.goi.aarogyasetu;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.text.TextUtils;

import java.util.UUID;

import nic.goi.aarogyasetu.analytics.EventNames;
import nic.goi.aarogyasetu.prefs.SharedPref;
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants;
import nic.goi.aarogyasetu.utility.AnalyticsUtils;
import nic.goi.aarogyasetu.utility.Constants;
import nic.goi.aarogyasetu.utility.CorUtility;
import nic.goi.aarogyasetu.utility.CorUtilityKt;
import nic.goi.aarogyasetu.utility.Logger;


/**
 * @author Niharika.Arora
 */
public class GattServer {
    private String TAG = this.getClass().getName();
    private Context mContext;

    private BluetoothLeAdvertiser advertiser;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothManager mBluetoothManager;

    private AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
        }
    };

    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //do nothing
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //do nothing
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            if (UUID.fromString(BuildConfig.DID_UUID).equals(characteristic.getUuid())) {
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, characteristic.getValue());
            } else if (UUID.fromString(BuildConfig.PINGER_UUID).equals(characteristic.getUuid())) {
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, characteristic.getValue());
            } else {
                // Invalid characteristic
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
            }
        }
    };


    public void onCreate(Context context) throws RuntimeException {
        mContext = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public void advertise(int advertisementMode) {
        try {
            BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
            if (defaultAdapter == null) {
                return;
            }
            String uniqueId = SharedPref.getStringParams(CoronaApplication.getInstance(), SharedPrefsConstants.UNIQUE_ID, Constants.EMPTY);
            if (uniqueId.isEmpty()) {
                return;
            }
            if (!uniqueId.equalsIgnoreCase(defaultAdapter.getName())) {
                stopAdvertising();
            }
            defaultAdapter.setName(uniqueId);
            advertiser = defaultAdapter.getBluetoothLeAdvertiser();
            AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(advertisementMode)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
                    .setConnectable(true);

            ParcelUuid pUuid = new ParcelUuid(UUID.fromString(BuildConfig.SERVICE_UUID));

            AdvertiseData data = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .addServiceUuid(pUuid)
                    .setIncludeTxPowerLevel(false).build();
            if (advertiser != null) {
                try {
                    startAdvertising(settingsBuilder, data, true);
                } catch (Exception e) {
                    // Adding common exception just to retry this if anything goes wrong in the first time
                    // (Chinese devices facing some legacy data issue)
                    //Some OEM shows Advertising data too large exception,so not sending txPowerLevel
                    if (e instanceof IllegalArgumentException && !TextUtils.isEmpty(e.getMessage())
                            && e.getMessage().contains(Constants.LEGACY_ISSUE)) {
                        AnalyticsUtils.sendEvent(EventNames.ADVERTISING_LEGACY_ISSUE);
                    }
                    startAdvertising(settingsBuilder, data, false);
                }
            }
        } catch (Exception ex) {
            //Reporting exception on Crashlytics if advertisement fails for other reason in devices and take corrective actions
            CorUtilityKt.reportException(ex);
        }
    }

    private void startAdvertising(AdvertiseSettings.Builder settingsBuilder, AdvertiseData data, boolean isConnectable) {
        settingsBuilder.setConnectable(isConnectable);
        if (CorUtility.isBluetoothAvailable() && advertiser != null && advertisingCallback != null) {
            advertiser.startAdvertising(settingsBuilder.build(), data, advertisingCallback);
        } else {
            //do nothing
        }
    }

    public void addGattService() {
        if (CorUtility.isBluetoothAvailable() && isServerStarted()) {
            try {
                mBluetoothGattServer.addService(createGattService());
            } catch (Exception ex) {
                //Android version 7.0 (Redmi Note 4 & Huawei MediaPad T3 & Nova2Plus device issue) Android BLE characterstic add issue  https://github.com/iDevicesInc/SweetBlue/issues/394
            }
        }
    }

    private BluetoothGattService createGattService() {
        BluetoothGattService service = new BluetoothGattService(UUID.fromString(BuildConfig.SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic uniqueIdChar = new BluetoothGattCharacteristic(UUID.fromString(BuildConfig.DID_UUID),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        String uniqueId = SharedPref.getStringParams(CoronaApplication.getInstance(), SharedPrefsConstants.UNIQUE_ID, Constants.EMPTY);
        uniqueIdChar.setValue(uniqueId);

        //Adding this for iOS continuous ping
        BluetoothGattCharacteristic pingerChar = new BluetoothGattCharacteristic(UUID.fromString(BuildConfig.PINGER_UUID),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        pingerChar.setValue(String.valueOf(true));

        service.addCharacteristic(uniqueIdChar);
        service.addCharacteristic(pingerChar);

        return service;
    }

    public void onDestroy() {
        if (mContext != null) {
            if (CorUtility.isBluetoothAvailable()) {
                stopServer();
                stopAdvertising();
            }
        }
    }

    public void stopServer() {
        try {
            if (mBluetoothGattServer != null) {
                mBluetoothGattServer.clearServices();
                mBluetoothGattServer.close();
            }
        } catch (Exception e) {
            //Handle Bluetooth Gatt close internal bug
            Logger.e(TAG, "GATT server can't be closed elegantly" + e.getMessage());
        }
    }

    private boolean isServerStarted() {
        mBluetoothGattServer = mBluetoothManager.openGattServer(mContext, mGattServerCallback);
        if (mBluetoothGattServer != null) {
            mBluetoothGattServer.clearServices();
            return true;
        } else {
            return false;
        }
    }

    public void stopAdvertising() {
        try {
            if (advertiser != null) {
                advertiser.stopAdvertising(advertisingCallback);
            }
        } catch (Exception ex) {
            //Handle StopAdvertisingSet Android Internal bug (Redmi Note 7 Pro Android 9)
        }
    }
}
