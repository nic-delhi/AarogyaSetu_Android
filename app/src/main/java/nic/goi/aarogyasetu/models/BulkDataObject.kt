package nic.goi.aarogyasetu.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import nic.goi.aarogyasetu.utility.Constants

class BulkDataObject {
    @SerializedName("d")
    @Expose
    var d: String? = null

    @SerializedName(Constants.UPLOAD_TYPE)
    @Expose
    var uploadType: String? = null

    @SerializedName("data")
    @Expose
    var data: List<DataPoint>? = null
}
