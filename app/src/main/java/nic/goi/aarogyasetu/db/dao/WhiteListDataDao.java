package nic.goi.aarogyasetu.db.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import nic.goi.aarogyasetu.models.WhiteListData;


/**
 * The interface WhiteListDataDao to access whiteList DB table.
 */
@Dao
public interface WhiteListDataDao {

    /**
     * Insert white list device .
     *
     * @param device the device
     * @return the long uniqueId of inserted device
     */
    @Insert
    long insertWhiteListDevice(WhiteListData device);

    /**
     * Gets all white list devices.
     *
     * @return the all white list devices
     */
    @Query("SELECT deviceId FROM white_list_devices")
    List<String> getAllWhiteListDevices();

}
