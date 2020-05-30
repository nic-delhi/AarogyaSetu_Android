package nic.goi.aarogyasetu.models.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class FCMTokenObject(
    @field:SerializedName("ft")
    @field:Expose
    private val ft: String
)
