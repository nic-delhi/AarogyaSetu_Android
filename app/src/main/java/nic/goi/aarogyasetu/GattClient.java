package nic.goi.aarogyasetu;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nic.goi.aarogyasetu.db.DBManager;
import nic.goi.aarogyasetu.models.BluetoothData;
import nic.goi.aarogyasetu.utility.Logger;
import nic.goi.aarogyasetu.models.BluetoothModel;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * Class for testing gatt server connection and characterstics reading
 * @author Niharika.Arora
 */
public class GattClient {
    private static final String TAG = GattClient.class.getSimpleName();
    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice mDevice;
    private String txPower = "";
    private int mRssi;
    private String txPowerLevel = "";
    private List<BluetoothGattCharacteristic> chars = new ArrayList<>();

    private Context mContext;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stopClient();
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                BluetoothGattService service = gatt.getService(UUID.fromString(BuildConfig.SERVICE_UUID));
                if (service != null) {
                    BluetoothGattCharacteristic probCharacteristic = service.getCharacteristic(UUID.fromString(BuildConfig.PINGER_UUID));
                    if (probCharacteristic != null) {
                        chars.add(probCharacteristic);
                    }
                    BluetoothGattCharacteristic idCharacteristic = service.getCharacteristic(UUID.fromString(BuildConfig.DID_UUID));
                    if (idCharacteristic != null) {
                        chars.add(idCharacteristic);
                    }
                }
                requestCharacteristics(gatt);
            } else {
                Logger.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        public void requestCharacteristics(BluetoothGatt gatt) {
            if (!chars.isEmpty()) {
                gatt.readCharacteristic(chars.get(chars.size() - 1));
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            readCounterCharacteristic(characteristic, gatt);
        }

        private void readCounterCharacteristic(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
            if (UUID.fromString(BuildConfig.DID_UUID).equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();
                String uniqueId = new String(data, StandardCharsets.UTF_8);
                Logger.d("GattCLient", "Unique ID - " + uniqueId);
                BluetoothModel bluetoothModel = new BluetoothModel(uniqueId,
                        uniqueId, mRssi, txPower, txPowerLevel);
                storeDetectedUserDeviceInDB(bluetoothModel);
            } else if (UUID.fromString(BuildConfig.PINGER_UUID).equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();
                String uniqueId = new String(data, StandardCharsets.UTF_8);
                Logger.d("GattCLient", "Pinger ID - " + uniqueId);
            }
            chars.remove(chars.get(chars.size() - 1));

            if (chars.size() > 0) {
                requestCharacteristics(gatt);
            } else {
                gatt.disconnect();
            }
        }
    };

    /**
     * This method will stoer the detected device infos into the local database to query in future if the need arise
     * to push the data
     *
     * @param bluetoothModel The newly detected device nearby
     */

    private void storeDetectedUserDeviceInDB(BluetoothModel bluetoothModel) {
        Location loc = CoronaApplication.getInstance().getDeviceLastKnownLocation();
        if (loc != null) {
            if (bluetoothModel != null) {
                BluetoothData bluetoothData = new BluetoothData(bluetoothModel.getAddress(), bluetoothModel.getRssi(),
                        bluetoothModel.getTxPower(), bluetoothModel.getTxPowerLevel());
                bluetoothData.setLatitude(loc.getLatitude());
                bluetoothData.setLongitude(loc.getLongitude());
                DBManager.insertNearbyDetectedDeviceInfo(bluetoothData);
            }
        }
    }

    /**
     * Listens for Bluetooth adapter events to enable/disable
     * advertising and server functionality.
     */
    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    startClient();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stopClient();
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
    };

    public void onCreate(Context context, ScanResult result) throws RuntimeException {
        mContext = context;
        mRssi = result.getRssi();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            txPower = String.valueOf(result.getTxPower());
        }
        if (result.getScanRecord() != null) {
            txPowerLevel = String.valueOf(result.getScanRecord().getTxPowerLevel());
        }
        mBluetoothManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();

            // Register for system Bluetooth events
            registerReceiver();
            configureClient(result);
        }
    }

    private void configureClient(ScanResult result) {
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        } else {
            mDevice = mBluetoothAdapter.getRemoteDevice(result.getDevice().getAddress());
            startClient();
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mBluetoothReceiver, filter);
    }

    private void startClient() {
        if (mDevice != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBluetoothGatt = mDevice.connectGatt(mContext, false, mGattCallback, TRANSPORT_LE);
            } else {
                mBluetoothGatt = mDevice.connectGatt(mContext, false, mGattCallback);
            }
        }

        if (mBluetoothGatt == null) {
            return;
        }
    }

    private void stopClient() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    public void onDestroy() {
        if (mContext != null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter;
            if (mBluetoothManager != null) {
                bluetoothAdapter = mBluetoothManager.getAdapter();
                if (bluetoothAdapter.isEnabled()) {
                    stopClient();
                }
            }
        }
    }
}
