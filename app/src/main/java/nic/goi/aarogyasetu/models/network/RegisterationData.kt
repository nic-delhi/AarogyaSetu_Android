package nic.goi.aarogyasetu.models.network

import android.location.Location

import nic.goi.aarogyasetu.CoronaApplication

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RegisterationData(
    @field:SerializedName("n")
    @field:Expose
    var n: String?, @field:SerializedName("d")
    @field:Expose
    var d: String?, @field:SerializedName("ft")
    private val fcmToken: String
) {

    @SerializedName("lat")
    var lat: String? = null
        private set

    @SerializedName("lon")
    var lon: String? = null
        private set

    @SerializedName("is_bl_allowed")
    var isBlAllowed: Boolean = false

    @SerializedName("is_loc_allowed")
    var isLocAllowed: Boolean = false

    @SerializedName("is_bl_on")
    var isBlOn: Boolean = false

    @SerializedName("is_loc_on")
    var isLocOn: Boolean = false

    init {
        val location = CoronaApplication.getInstance().getDeviceLastKnownLocation()
        if (location != null) {
            this.lat = String.valueOf(location!!.getLatitude())
            this.lon = String.valueOf(location!!.getLongitude())
        }
    }


}
