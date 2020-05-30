package nic.goi.aarogyasetu.db.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import nic.goi.aarogyasetu.models.BluetoothData

@Dao
interface BluetoothDataDao {

    @get:Query("SELECT * FROM nearby_devices_info_table")
    val allNearbyDevices: List<BluetoothData>

    @get:Query("SELECT COUNT(*) FROM nearby_devices_info_table")
    val rowCount: Long

    @Insert
    fun insertNearbyUser(bluetoothData: BluetoothData): Long

    @Insert
    fun insertAllNearbyUsers(bluetoothData: List<BluetoothData>): List<Long>

    @Query("SELECT * FROM nearby_devices_info_table LIMIT :limit")
    fun getFirstXNearbyDeviceInfo(limit: Int): List<BluetoothData>

    @Query("SELECT * FROM nearby_devices_info_table  LIMIT :limit OFFSET :offset")
    fun getXNearbyDeviceInfoWithOffset(limit: Int, offset: Int): List<BluetoothData>

    @Query("DELETE FROM nearby_devices_info_table WHERE (:currentTimeStamp - timestamp) >= :daysTimeStamp")
    fun deleteXDaysOldData(daysTimeStamp: Int, currentTimeStamp: Int): Int

    @Delete
    fun deleteAll(bluetoothData: List<BluetoothData>)

}
