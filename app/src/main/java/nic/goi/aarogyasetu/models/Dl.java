package nic.goi.aarogyasetu.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Niharika.Arora
 */
public class Dl {

    @SerializedName("d")
    @Expose
    private String d;
    @SerializedName("dist")
    @Expose
    private Integer dist;
    @SerializedName("tx_level")
    @Expose
    private String txLevel;
    @SerializedName("tx_power")
    @Expose
    private String txPower;

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public Dl(String d, Integer dist, String txPowerLevel, String txPower) {
        this.d = d;
        this.dist = dist;
        this.txPower = txPower;
        this.txLevel = txPowerLevel;
    }
}