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

package nic.goi.aarogyasetu.background

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import nic.goi.aarogyasetu.utility.Constants
import nic.goi.aarogyasetu.utility.CorUtility

/**
 *  @author Damanpreet.Singh
 * @author Niharika.Arora
 */
class BackgroundWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val mContext: Context = context

    override fun doWork(): Result {
        val intent =
            Intent(mContext, nic.goi.aarogyasetu.background.BluetoothScanningService::class.java)
        intent.putExtra(Constants.FROM_MY_WORKER, true)

        startService(intent)

        if (CorUtility.isBluetoothAvailable()) {
            BluetoothAdapter.getDefaultAdapter().startDiscovery()
        }
        CorUtility.remove30DaysOldData()
        return Result.success()

    }

    private fun startService(intent: Intent) {
        val uniqueId = nic.goi.aarogyasetu.prefs.SharedPref.getStringParams(
            nic.goi.aarogyasetu.CoronaApplication.getInstance(),
            nic.goi.aarogyasetu.prefs.SharedPrefsConstants.UNIQUE_ID,
            Constants.EMPTY
        )
        if (!BluetoothScanningService.serviceRunning && uniqueId.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mContext.startForegroundService(intent)
            } else {
                mContext.startService(intent)
            }
        }
    }

    companion object {
        val UNIQUE_WORK_NAME = BackgroundWorker::class.java.simpleName
    }
}