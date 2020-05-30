package nic.goi.aarogyasetu.models

import java.util.ArrayList

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DataPoint(postData: BluetoothData, decLatitude: String, decLongitude: String) {

    @SerializedName("ts")
    @Expose
    private val ts: String
    @SerializedName("l")
    @Expose
    private val locationObject: LocationObject
    @SerializedName("dl")
    @Expose
    private var dl: List<Dl>? = null


    init {
        this.ts = String.valueOf(postData.getTimeStamp())
        this.locationObject = LocationObject(decLatitude, decLongitude)
        dl = ArrayList()
        dl!!.add(
            Dl(
                postData.getBluetoothMacAddress(),
                postData.getDistance(),
                postData.getTxPowerLevel(),
                postData.getTxPower()
            )
        )
    }

}