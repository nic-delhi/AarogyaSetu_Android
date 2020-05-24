package nic.goi.aarogyasetu.models.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FCMTokenObject{

    @SerializedName("ft")
    @Expose
    private String ft;
    public FCMTokenObject(String ft){
        this.ft = ft;
    }

}
