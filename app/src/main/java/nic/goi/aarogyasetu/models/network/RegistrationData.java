package nic.goi.aarogyasetu.models.network;

import android.location.Location;

import nic.goi.aarogyasetu.CoronaApplication;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegisterationData {


    @SerializedName("n")
    @Expose
    private String n;
    @SerializedName("d")
    @Expose
    private String d;

    @SerializedName("ft")
    private String fcmToken;

    @SerializedName("lat")
    private String lat;

    @SerializedName("lon")
    private String lon;

    @SerializedName("is_bl_allowed")
    private boolean isBlAllowed;

    @SerializedName("is_loc_allowed")
    private boolean isLocAllowed;

    @SerializedName("is_bl_on")
    private boolean isBlOn;

    @SerializedName("is_loc_on")
    private boolean isLocOn;

    public RegisterationData(String n, String d, String fcmToken) {
        this.n = n;
        this.d = d;
        this.fcmToken = fcmToken;
        Location location = CoronaApplication.getInstance().getDeviceLastKnownLocation();
        if (location != null) {
            this.lat = String.valueOf(location.getLatitude());
            this.lon = String.valueOf(location.getLongitude());
        }
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public boolean isBlAllowed() {
        return isBlAllowed;
    }

    public void setBlAllowed(boolean blAllowed) {
        isBlAllowed = blAllowed;
    }

    public boolean isLocAllowed() {
        return isLocAllowed;
    }

    public void setLocAllowed(boolean locAllowed) {
        isLocAllowed = locAllowed;
    }

    public boolean isBlOn() {
        return isBlOn;
    }

    public void setBlOn(boolean blOn) {
        isBlOn = blOn;
    }

    public boolean isLocOn() {
        return isLocOn;
    }

    public void setLocOn(boolean locOn) {
        isLocOn = locOn;
    }


}
