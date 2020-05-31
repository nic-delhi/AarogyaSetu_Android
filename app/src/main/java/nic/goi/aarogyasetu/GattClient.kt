package nic.goi.aarogyasetu

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Build

import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.UUID

import nic.goi.aarogyasetu.db.DBManager
import nic.goi.aarogyasetu.models.BluetoothData
import nic.goi.aarogyasetu.utility.Logger
import nic.goi.aarogyasetu.models.BluetoothModel

import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.content.Context.BLUETOOTH_SERVICE

/**
 * Class for testing gatt server connection and characterstics reading
 * @author Niharika.Arora
 */
class GattClient {
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mDevice: BluetoothDevice? = null
    private var txPower = ""
    private var mRssi: Int = 0
    private var txPowerLevel = ""
    private val chars = ArrayList()

    private var mContext: Context? = null

    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null

    private val mGattCallback = object : BluetoothGattCallback() {
        @Override
        fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stopClient()
            }
        }


        @Override
        fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                val service = gatt.getService(UUID.fromString(BuildConfig.SERVICE_UUID))
                if (service != null) {
                    val probCharacteristic = service!!.getCharacteristic(UUID.fromString(BuildConfig.PINGER_UUID))
                    if (probCharacteristic != null) {
                        chars.add(probCharacteristic)
                    }
                    val idCharacteristic = service!!.getCharacteristic(UUID.fromString(BuildConfig.DID_UUID))
                    if (idCharacteristic != null) {
                        chars.add(idCharacteristic)
                    }
                }
                requestCharacteristics(gatt)
            } else {
                Logger.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        fun requestCharacteristics(gatt: BluetoothGatt) {
            if (!chars.isEmpty()) {
                gatt.readCharacteristic(chars.get(chars.size() - 1))
            }
        }

        @Override
        fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            readCounterCharacteristic(characteristic, gatt)
        }

        private fun readCounterCharacteristic(characteristic: BluetoothGattCharacteristic, gatt: BluetoothGatt) {
            if (UUID.fromString(BuildConfig.DID_UUID).equals(characteristic.getUuid())) {
                val data = characteristic.getValue()
                val uniqueId = String(data, StandardCharsets.UTF_8)
                Logger.d("GattCLient", "Unique ID - $uniqueId")
                val bluetoothModel = BluetoothModel(
                    uniqueId,
                    uniqueId, mRssi, txPower, txPowerLevel
                )
                storeDetectedUserDeviceInDB(bluetoothModel)
            } else if (UUID.fromString(BuildConfig.PINGER_UUID).equals(characteristic.getUuid())) {
                val data = characteristic.getValue()
                val uniqueId = String(data, StandardCharsets.UTF_8)
                Logger.d("GattCLient", "Pinger ID - $uniqueId")
            }
            chars.remove(chars.get(chars.size() - 1))

            if (chars.size() > 0) {
                requestCharacteristics(gatt)
            } else {
                gatt.disconnect()
            }
        }
    }

    /**
     * Listens for Bluetooth adapter events to enable/disable
     * advertising and server functionality.
     */
    private val mBluetoothReceiver = object : BroadcastReceiver() {
        @Override
        fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)

            when (state) {
                BluetoothAdapter.STATE_ON -> startClient()
                BluetoothAdapter.STATE_OFF -> stopClient()
                else -> {
                }
            }// Do nothing
        }
    }

    /**
     * This method will stoer the detected device infos into the local database to query in future if the need arise
     * to push the data
     *
     * @param bluetoothModel The newly detected device nearby
     */

    private fun storeDetectedUserDeviceInDB(bluetoothModel: BluetoothModel?) {
        val loc = CoronaApplication.getInstance().getDeviceLastKnownLocation()
        if (loc != null) {
            if (bluetoothModel != null) {
                val bluetoothData = BluetoothData(
                    bluetoothModel!!.getAddress(), bluetoothModel!!.getRssi(),
                    bluetoothModel!!.getTxPower(), bluetoothModel!!.getTxPowerLevel()
                )
                bluetoothData.setLatitude(loc!!.getLatitude())
                bluetoothData.setLongitude(loc!!.getLongitude())
                DBManager.insertNearbyDetectedDeviceInfo(bluetoothData)
            }
        }
    }

    @Throws(RuntimeException::class)
    fun onCreate(context: Context, result: ScanResult) {
        mContext = context
        mRssi = result.getRssi()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            txPower = String.valueOf(result.getTxPower())
        }
        if (result.getScanRecord() != null) {
            txPowerLevel = String.valueOf(result.getScanRecord().getTxPowerLevel())
        }
        mBluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager!!.getAdapter()

            // Register for system Bluetooth events
            registerReceiver()
            configureClient(result)
        }
    }

    private fun configureClient(result: ScanResult) {
        if (!mBluetoothAdapter!!.isEnabled()) {
            mBluetoothAdapter!!.enable()
        } else {
            mDevice = mBluetoothAdapter!!.getRemoteDevice(result.getDevice().getAddress())
            startClient()
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        mContext!!.registerReceiver(mBluetoothReceiver, filter)
    }

    private fun startClient() {
        if (mDevice != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBluetoothGatt = mDevice!!.connectGatt(mContext, false, mGattCallback, TRANSPORT_LE)
            } else {
                mBluetoothGatt = mDevice!!.connectGatt(mContext, false, mGattCallback)
            }
        }

        if (mBluetoothGatt == null) {
            return
        }
    }

    private fun stopClient() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt!!.close()
            mBluetoothGatt = null
        }
    }

    fun onDestroy() {
        if (mContext != null) {
            mBluetoothManager = mContext!!.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter: BluetoothAdapter
            if (mBluetoothManager != null) {
                bluetoothAdapter = mBluetoothManager!!.getAdapter()
                if (bluetoothAdapter.isEnabled()) {
                    stopClient()
                }
            }
        }
    }

    companion object {
        private val TAG = GattClient::class.java!!.getSimpleName()
    }
}
