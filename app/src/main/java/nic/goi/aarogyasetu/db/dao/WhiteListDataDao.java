package nic.goi.aarogyasetu.db.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import nic.goi.aarogyasetu.models.WhiteListData;

@Dao
public interface WhiteListDataDao {

    @Insert
    long insertWhiteListDevice(WhiteListData device);

    @Query("SELECT deviceId FROM white_list_devices")
    List<String> getAllWhiteListDevices();

    @Delete
    void deleteAll(List<WhiteListData> whiteListData);

}
