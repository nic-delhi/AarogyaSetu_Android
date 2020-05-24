package nic.goi.aarogyasetu.models.network;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TokenValidationResponse {

    @SerializedName("auth_token")
    @Expose
    private String authToken;
    @SerializedName("refresh_token")
    @Expose
    private String refreshToken;

    public String getAuthToken() {
        return authToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

}