package nic.goi.aarogyasetu.models

import com.google.gson.annotations.SerializedName

data class TncDataModel(
    @SerializedName("t") val type: Int, @SerializedName("v") val text: String,
    @SerializedName("c") val clickText: String? = null,
    @SerializedName("url") val url: String? = null
)