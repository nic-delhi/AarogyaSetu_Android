/*
 * Copyright 2020 Government of India
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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