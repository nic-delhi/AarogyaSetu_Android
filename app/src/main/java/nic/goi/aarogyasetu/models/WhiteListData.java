package nic.goi.aarogyasetu.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

/**
 * WhiteListData table to save whiteListed devices in Room DB.
 */
@TypeConverters({Converters.class})
@Entity(tableName = "white_list_devices")
public class WhiteListData {

    @ColumnInfo(name = "name")
    @SerializedName("name")
    @Expose
    private String name;

    @ColumnInfo(name = "deviceId")
    @SerializedName("deviceId")
    @PrimaryKey
    @Expose
    @NotNull
    private String deviceId;


    /**
     * Instantiates a new White list data.
     *
     * @param name     the name
     * @param deviceId the device id
     */
    public WhiteListData(String name, String deviceId) {
        this.name = name;
        this.deviceId = deviceId;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets device id.
     *
     * @return the device id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets device id.
     *
     * @param deviceId the device id
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
