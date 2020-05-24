package nic.goi.aarogyasetu.utility

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import nic.goi.aarogyasetu.background.BluetoothScanningService

class BluetoothLocationReceiver: BroadcastReceiver() {
    private val TAG = BluetoothLocationReceiver::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        Logger.d(TAG, "onReceive action: ${intent?.action} isRunning: ${BluetoothScanningService.serviceRunning}")
        if (!BluetoothScanningService.serviceRunning) {
            intent?.action?.let {
                if (LocationManager.PROVIDERS_CHANGED_ACTION == it) {
                    CorUtility.startBackgroundWorker()
                } else if (BluetoothAdapter.ACTION_STATE_CHANGED == it) {
                    val state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    );
                    if (state == BluetoothAdapter.STATE_ON) {
                        CorUtility.startBackgroundWorker()
                    }
                }
            }
        }
    }

}