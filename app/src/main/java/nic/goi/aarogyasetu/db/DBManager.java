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

package nic.goi.aarogyasetu.db;


import nic.goi.aarogyasetu.models.BluetoothData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Damanpreet.Singh
 */
public class DBManager {


    private static int numCores = Runtime.getRuntime().availableProcessors();
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(numCores * 2, numCores * 2,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public static Task<List<Long>> insertNearbyDetectedDeviceInfo(List<BluetoothData> userDeviceInfoList) {

        return Tasks.call(executor, () -> FightCovidDB.getInstance().getBluetoothDataDao().insertAllNearbyUsers(userDeviceInfoList));
    }

    public static Task<Long> insertNearbyDetectedDeviceInfo(BluetoothData userDeviceInfo) {

        return Tasks.call(executor, () -> FightCovidDB.getInstance().getBluetoothDataDao().insertNearbyUser(userDeviceInfo));
    }
}
