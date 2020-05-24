package nic.goi.aarogyasetu.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import nic.goi.aarogyasetu.utility.Constants;

public class BulkDataObject {
    @SerializedName("d")
    @Expose
    private String d;

    @SerializedName(Constants.UPLOAD_TYPE)
    @Expose
    private String uploadType;

    @SerializedName("data")
    @Expose
    private List<DataPoint> data;

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

    public String getUploadType() {
        return uploadType;
    }

    public void setUploadType(String type) {
        this.uploadType = type;
    }
}
