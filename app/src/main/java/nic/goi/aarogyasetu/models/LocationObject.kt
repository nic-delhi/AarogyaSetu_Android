package nic.goi.aarogyasetu.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LocationObject internal constructor(
    @field:SerializedName("lat")
    @field:Expose
    private val lat: String, @field:SerializedName("lon")
    @field:Expose
    private val lon: String
)
