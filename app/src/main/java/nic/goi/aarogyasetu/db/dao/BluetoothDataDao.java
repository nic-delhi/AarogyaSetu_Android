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

package nic.goi.aarogyasetu.db.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import nic.goi.aarogyasetu.models.BluetoothData;

import java.util.List;

import nic.goi.aarogyasetu.models.BluetoothData;

@Dao
public interface BluetoothDataDao {

    @Insert
    long insertNearbyUser(BluetoothData bluetoothData);

    @Insert
    List<Long> insertAllNearbyUsers(List<BluetoothData> bluetoothData);

    @Query("SELECT * FROM nearby_devices_info_table")
    List<BluetoothData> getAllNearbyDevices();

    @Query("SELECT COUNT(*) FROM nearby_devices_info_table")
    long getRowCount();

    @Query("SELECT * FROM nearby_devices_info_table LIMIT :limit")
    List<BluetoothData> getFirstXNearbyDeviceInfo(int limit);

    @Query("SELECT * FROM nearby_devices_info_table  LIMIT :limit OFFSET :offset")
    List<BluetoothData> getXNearbyDeviceInfoWithOffset(int limit, int offset);

    @Query("DELETE FROM nearby_devices_info_table WHERE (:currentTimeStamp - timestamp) >= :daysTimeStamp")
    int deleteXDaysOldData(int daysTimeStamp, int currentTimeStamp);

    @Delete
    void deleteAll(List<BluetoothData> bluetoothData);

}
