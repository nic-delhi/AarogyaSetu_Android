package nic.goi.aarogyasetu.models.network


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TokenValidationResponse {

    @SerializedName("auth_token")
    @Expose
    val authToken: String? = null
    @SerializedName("refresh_token")
    @Expose
    val refreshToken: String? = null

}