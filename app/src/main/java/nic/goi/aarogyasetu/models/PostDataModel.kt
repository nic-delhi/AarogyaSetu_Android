package nic.goi.aarogyasetu.models

/**
 * Created by Kshitij Khatri on 21/03/20.
 */


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PostDataModel {

    @SerializedName("d")
    @Expose
    var d: String? = null
    @SerializedName("data")
    @Expose
    var data: List<DataPoint>? = null

}