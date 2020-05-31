package nic.goi.aarogyasetu.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * @author Niharika.Arora
 */
class Dl(
    @field:SerializedName("d")
    @field:Expose
    var d: String?, @field:SerializedName("dist")
    @field:Expose
    private val dist: Integer, @field:SerializedName("tx_level")
    @field:Expose
    private val txLevel: String, @field:SerializedName("tx_power")
    @field:Expose
    private val txPower: String
)