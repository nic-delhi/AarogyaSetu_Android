package nic.goi.aarogyasetu.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationObject {

    @SerializedName("lat")
    @Expose
    private String lat;
    @SerializedName("lon")
    @Expose
    private String lon;

    LocationObject(String latitude, String longitude) {
        this.lat = latitude;
        this.lon = longitude;
    }

}
