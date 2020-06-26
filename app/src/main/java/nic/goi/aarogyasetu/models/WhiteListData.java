package nic.goi.aarogyasetu.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

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


    public WhiteListData(String name, String deviceId) {
        this.name = name;
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
