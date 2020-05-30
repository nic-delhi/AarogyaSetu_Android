package nic.goi.aarogyasetu.network

import nic.goi.aarogyasetu.BuildConfig
import nic.goi.aarogyasetu.models.BulkDataObject
import nic.goi.aarogyasetu.models.network.FCMTokenObject
import nic.goi.aarogyasetu.models.network.GenerateOTP
import nic.goi.aarogyasetu.models.network.RegisterationData
import nic.goi.aarogyasetu.models.network.TokenValidationResponse
import nic.goi.aarogyasetu.models.network.ValidateOTP
import nic.goi.aarogyasetu.utility.Constants
import com.google.gson.JsonElement

import org.json.JSONObject

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface PostDataInterface {
    @POST(BuildConfig.BULK_UPLOAD_API)
    fun postUserData(@HeaderMap headers: Map<String, String>, @Body jsonObject: BulkDataObject): Call<JSONObject>

    @POST(BuildConfig.REGISTER_USER_API)
    fun registerUser(@HeaderMap headers: Map<String, String>, @Body jsonObject: RegisterationData): Call<JsonElement>

    @POST(BuildConfig.FCM_TOKEN_API)
    fun refreshFCM(@HeaderMap headers: Map<String, String>, @Body jsonObject: FCMTokenObject): Call<JSONObject>

    @GET(BuildConfig.CHECK_STATUS_API)
    fun updateStatus(@HeaderMap headers: Map<String, String>): Call<JsonElement>

    @GET(BuildConfig.QR_CODE_API)
    fun fetchQr(@HeaderMap headers: Map<String, String>): Call<JsonElement>

    @GET(BuildConfig.QR_PUBLIC_KEY_API)
    fun fetchQrPublicKey(@HeaderMap headers: Map<String, String>): Call<JsonElement>

    @GET(BuildConfig.CONFIG_API)
    fun appMeta(@HeaderMap headers: Map<String, String>): Call<JsonElement>

    @POST(BuildConfig.GENERATE_OTP_API)
    fun generateOTP(@HeaderMap headers: Map<String, String>, @Body generateOTP: GenerateOTP): Call<JSONObject>

    @POST(BuildConfig.VALIDATE_OTP_API)
    fun validateOTP(@HeaderMap headers: Map<String, String>, @Body validateOTP: ValidateOTP): Call<TokenValidationResponse>

    @GET(BuildConfig.REFRESH_TOKEN_API)
    fun refreshToken(@Header(Constants.AUTH) refreshToken: String): Call<TokenValidationResponse>

}
