package nic.goi.aarogyasetu.network;

import nic.goi.aarogyasetu.BuildConfig;
import nic.goi.aarogyasetu.models.BulkDataObject;
import nic.goi.aarogyasetu.models.network.FCMTokenObject;
import nic.goi.aarogyasetu.models.network.GenerateOTP;
import nic.goi.aarogyasetu.models.network.RegisterationData;
import nic.goi.aarogyasetu.models.network.TokenValidationResponse;
import nic.goi.aarogyasetu.models.network.ValidateOTP;
import nic.goi.aarogyasetu.utility.Constants;
import com.google.gson.JsonElement;

import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface PostDataInterface {
    @POST(BuildConfig.BULK_UPLOAD_API)
    Call<JSONObject> postUserData(@HeaderMap Map<String, String> headers, @Body BulkDataObject jsonObject);

    @POST(BuildConfig.REGISTER_USER_API)
    Call<JsonElement> registerUser(@HeaderMap Map<String, String> headers, @Body RegisterationData jsonObject);

    @POST(BuildConfig.FCM_TOKEN_API)
    Call<JSONObject> refreshFCM(@HeaderMap Map<String, String> headers, @Body FCMTokenObject jsonObject);

    @GET(BuildConfig.CHECK_STATUS_API)
    Call<JsonElement> updateStatus(@HeaderMap Map<String, String> headers);

    @GET(BuildConfig.QR_CODE_API)
    Call<JsonElement> fetchQr(@HeaderMap Map<String, String> headers);

    @GET(BuildConfig.QR_PUBLIC_KEY_API)
    Call<JsonElement> fetchQrPublicKey(@HeaderMap Map<String, String> headers);

    @GET(BuildConfig.CONFIG_API)
    Call<JsonElement> appMeta(@HeaderMap Map<String,String> headers);

    @POST(BuildConfig.GENERATE_OTP_API)
    Call<JSONObject> generateOTP(@HeaderMap Map<String, String> headers,@Body GenerateOTP generateOTP);

    @POST(BuildConfig.VALIDATE_OTP_API)
    Call<TokenValidationResponse> validateOTP(@HeaderMap Map<String, String> headers,@Body ValidateOTP validateOTP);

    @GET(BuildConfig.REFRESH_TOKEN_API)
    Call<TokenValidationResponse> refreshToken(@Header(Constants.AUTH) String refreshToken);

}
