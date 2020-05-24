package nic.goi.aarogyasetu.models;

/**
 * Created by Kshitij Khatri on 21/03/20.
 */


import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostDataModel {

    @SerializedName("d")
    @Expose
    private String d;
    @SerializedName("data")
    @Expose
    private List<DataPoint> data = null;

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public List<DataPoint> getData() {
        return data;
    }

    public void setData(List<DataPoint> data) {
        this.data = data;
    }

}