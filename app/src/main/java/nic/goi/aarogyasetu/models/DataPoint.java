package nic.goi.aarogyasetu.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataPoint {

    @SerializedName("ts")
    @Expose
    private String ts;
    @SerializedName("l")
    @Expose
    private LocationObject locationObject;
    @SerializedName("dl")
    @Expose
    private List<Dl> dl = null;


    public DataPoint(BluetoothData postData, String decLatitude, String decLongitude) {
        this.ts = String.valueOf(postData.getTimeStamp());
        this.locationObject = new LocationObject(decLatitude, decLongitude);
        dl = new ArrayList<>();
        dl.add(new Dl(postData.getBluetoothMacAddress(), postData.getDistance(), postData.getTxPowerLevel(), postData.getTxPower()));
    }

}