package nic.goi.aarogyasetu

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.text.TextUtils

import java.util.UUID

import nic.goi.aarogyasetu.analytics.EventNames
import nic.goi.aarogyasetu.prefs.SharedPref
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants
import nic.goi.aarogyasetu.utility.AnalyticsUtils
import nic.goi.aarogyasetu.utility.Constants
import nic.goi.aarogyasetu.utility.CorUtility
import nic.goi.aarogyasetu.utility.CorUtilityKt
import nic.goi.aarogyasetu.utility.Logger


/**
 * @author Niharika.Arora
 */
class GattServer {
    private val TAG = this.getClass().getName()
    private var mContext: Context? = null

    private var advertiser: BluetoothLeAdvertiser? = null
    private var mBluetoothGattServer: BluetoothGattServer? = null
    private var mBluetoothManager: BluetoothManager? = null

    private val advertisingCallback = object : AdvertiseCallback() {
        @Override
        fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
        }

        @Override
        fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
        }
    }

    private val mGattServerCallback = object : BluetoothGattServerCallback() {
        @Override
        fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //do nothing
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //do nothing
            }
        }

        @Override
        fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (UUID.fromString(BuildConfig.DID_UUID).equals(characteristic.getUuid())) {
                mBluetoothGattServer!!.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    characteristic.getValue()
                )
            } else if (UUID.fromString(BuildConfig.PINGER_UUID).equals(characteristic.getUuid())) {
                mBluetoothGattServer!!.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    characteristic.getValue()
                )
            } else {
                // Invalid characteristic
                mBluetoothGattServer!!.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
            }
        }
    }

    private val isServerStarted: Boolean
        get() {
            mBluetoothGattServer = mBluetoothManager!!.openGattServer(mContext, mGattServerCallback)
            if (mBluetoothGattServer != null) {
                mBluetoothGattServer!!.clearServices()
                return true
            } else {
                return false
            }
        }


    @Throws(RuntimeException::class)
    fun onCreate(context: Context) {
        mContext = context
        mBluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    fun advertise(advertisementMode: Int) {
        try {
            val defaultAdapter = BluetoothAdapter.getDefaultAdapter() ?: return
            val uniqueId = SharedPref.getStringParams(
                CoronaApplication.getInstance(),
                SharedPrefsConstants.UNIQUE_ID,
                Constants.EMPTY
            )
            if (uniqueId.isEmpty()) {
                return
            }
            if (!uniqueId.equalsIgnoreCase(defaultAdapter.getName())) {
                stopAdvertising()
            }
            defaultAdapter.setName(uniqueId)
            advertiser = defaultAdapter.getBluetoothLeAdvertiser()
            val settingsBuilder = AdvertiseSettings.Builder()
                .setAdvertiseMode(advertisementMode)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
                .setConnectable(true)

            val pUuid = ParcelUuid(UUID.fromString(BuildConfig.SERVICE_UUID))

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(pUuid)
                .setIncludeTxPowerLevel(false).build()
            if (advertiser != null) {
                try {
                    startAdvertising(settingsBuilder, data, true)
                } catch (e: Exception) {
                    // Adding common exception just to retry this if anything goes wrong in the first time
                    // (Chinese devices facing some legacy data issue)
                    //Some OEM shows Advertising data too large exception,so not sending txPowerLevel
                    if (e is IllegalArgumentException && !TextUtils.isEmpty(e.getMessage())
                        && e.getMessage().contains(Constants.LEGACY_ISSUE)
                    ) {
                        AnalyticsUtils.sendEvent(EventNames.ADVERTISING_LEGACY_ISSUE)
                    }
                    startAdvertising(settingsBuilder, data, false)
                }

            }
        } catch (ex: Exception) {
            //Reporting exception on Crashlytics if advertisement fails for other reason in devices and take corrective actions
            CorUtilityKt.reportException(ex)
        }

    }

    private fun startAdvertising(
        settingsBuilder: AdvertiseSettings.Builder,
        data: AdvertiseData,
        isConnectable: Boolean
    ) {
        settingsBuilder.setConnectable(isConnectable)
        if (CorUtility.isBluetoothAvailable() && advertiser != null && advertisingCallback != null) {
            advertiser!!.startAdvertising(settingsBuilder.build(), data, advertisingCallback)
        } else {
            //do nothing
        }
    }

    fun addGattService() {
        if (CorUtility.isBluetoothAvailable() && isServerStarted) {
            try {
                mBluetoothGattServer!!.addService(createGattService())
            } catch (ex: Exception) {
                //Android version 7.0 (Redmi Note 4 & Huawei MediaPad T3 & Nova2Plus device issue) Android BLE characterstic add issue  https://github.com/iDevicesInc/SweetBlue/issues/394
            }

        }
    }

    private fun createGattService(): BluetoothGattService {
        val service =
            BluetoothGattService(UUID.fromString(BuildConfig.SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val uniqueIdChar = BluetoothGattCharacteristic(
            UUID.fromString(BuildConfig.DID_UUID),
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        val uniqueId =
            SharedPref.getStringParams(CoronaApplication.getInstance(), SharedPrefsConstants.UNIQUE_ID, Constants.EMPTY)
        uniqueIdChar.setValue(uniqueId)

        //Adding this for iOS continuous ping
        val pingerChar = BluetoothGattCharacteristic(
            UUID.fromString(BuildConfig.PINGER_UUID),
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        pingerChar.setValue(String.valueOf(true))

        service.addCharacteristic(uniqueIdChar)
        service.addCharacteristic(pingerChar)

        return service
    }

    fun onDestroy() {
        if (mContext != null) {
            if (CorUtility.isBluetoothAvailable()) {
                stopServer()
                stopAdvertising()
            }
        }
    }

    fun stopServer() {
        try {
            if (mBluetoothGattServer != null) {
                mBluetoothGattServer!!.clearServices()
                mBluetoothGattServer!!.close()
            }
        } catch (e: Exception) {
            //Handle Bluetooth Gatt close internal bug
            Logger.e(TAG, "GATT server can't be closed elegantly" + e.getMessage())
        }

    }

    fun stopAdvertising() {
        try {
            if (advertiser != null) {
                advertiser!!.stopAdvertising(advertisingCallback)
            }
        } catch (ex: Exception) {
            //Handle StopAdvertisingSet Android Internal bug (Redmi Note 7 Pro Android 9)
        }

    }
}
