package nic.goi.aarogyasetu.utility

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.annotation.WorkerThread
import nic.goi.aarogyasetu.BuildConfig
import nic.goi.aarogyasetu.CoronaApplication
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.analytics.EventNames
import nic.goi.aarogyasetu.models.network.GenerateOTP
import nic.goi.aarogyasetu.utility.authsp.AuthSpFactory
import nic.goi.aarogyasetu.views.PermissionActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import kotlin.concurrent.thread

/**
 * @author Aman kapoor
 */
object AuthUtility {
    private val TAG =
        AuthUtility::class.java.simpleName
    private const val AUTH_BASE_URL =  "https://"+BuildConfig.AUTH_HOST_URL+"/"
    private const val KEY_TOKEN = "uidtoken"
    private const val KEY_REFRESH_TOKEN = "refreshtoken"
    private const val KEY_MOBILE_NUMBER = "mobile"
    private const val KEY_USER_NAME = "userName"
    private val authSpHelper = AuthSpFactory.instance

    @AnyThread
    fun isSignedIn(): Boolean = !getToken().isNullOrBlank()

    @Throws(IOException::class)
    @WorkerThread
    @JvmStatic
    fun updateToken(): String = try {
        val preRefreshToken = getRefreshToken()
        val client = nic.goi.aarogyasetu.network.NetworkClient.getRetrofitClient(
            true,
            true,
            true,
            AUTH_BASE_URL,
            false
        )
        val call = client.create(nic.goi.aarogyasetu.network.PostDataInterface::class.java)
            .refreshToken(preRefreshToken)
        val execute = call.execute()
        if (!execute.isSuccessful) {
            throw IOException()
        }
        val body = execute.body()
        val token: String? = body?.authToken ?: ""
        val refreshToken: String? = body?.refreshToken ?: ""
        setToken(token)
        setRefreshToken(refreshToken)
        token!!
    } catch (e: Exception) {
        throw IOException()
    }

    @JvmStatic
    fun getMobile(): String? = authSpHelper.getString(KEY_MOBILE_NUMBER, null)

    private fun setMobile(mobile: String?) {
        if (mobile.isNullOrBlank()) {
            authSpHelper.removeKey(KEY_MOBILE_NUMBER)
        } else {
            authSpHelper.putString(KEY_MOBILE_NUMBER, mobile)
        }
    }

    private fun removeMobile() {
        setMobile(null)
    }

    @AnyThread
    fun signIn(mobile: String, listener: UserSignInListener?) {
        if (isSignedIn()) return

        val client = nic.goi.aarogyasetu.network.NetworkClient.getRetrofitClient(
            false,
            true,
            true,
            AUTH_BASE_URL,
            false
        )
        val generateOTP = GenerateOTP(mobile)
        val map = mutableMapOf<String, String>()
        map[Constants.X_API_KEY] = BuildConfig.AWS_API_KEY
        map[Constants.OS] = Build.VERSION.SDK_INT.toString()
        map[Constants.DEVICE_TYPE] = Build.MANUFACTURER + Constants.HYPHEN + Build.MODEL
        val call = client.create(nic.goi.aarogyasetu.network.PostDataInterface::class.java)
            .generateOTP(map, generateOTP)

        call.enqueue(object : retrofit2.Callback<JSONObject> {
            override fun onFailure(call: Call<JSONObject>, t: Throwable) {
                val e = Exception(t)
                sendErrorCallback(listener, e, AuthErrorUnknown())
            }

            override fun onResponse(call: Call<JSONObject>, response: Response<JSONObject>) {
                if (response.isSuccessful) {
                    AppExecutors.runOnMain {
                        listener?.onAskOtp()
                    }
                } else if (response.code() == 401) {
                    val e =
                        Exception("Api Response is not success. Response = ${response.errorBody()?.string()}")
                    sendErrorCallback(listener, e, AuthErrorUserDisabled())
                } else {
                    val e =
                        Exception("Api Response is not success. Response = ${response.errorBody()?.string()}")
                    sendErrorCallback(listener, e, AuthErrorUnknown())
                }
            }
        })
    }

    @AnyThread
    fun verifyOtp(mobile: String, otp: String, listener: UserVerifyListener?) {
        val client = nic.goi.aarogyasetu.network.NetworkClient.getRetrofitClient(
            false,
            true,
            true,
            AUTH_BASE_URL,
            false
        )
        val validateOTP =
            nic.goi.aarogyasetu.models.network.ValidateOTP(mobile, otp)
        val map = mutableMapOf<String, String>()
        map[Constants.X_API_KEY] = BuildConfig.AWS_API_KEY
        map[Constants.OS] = Build.VERSION.SDK_INT.toString()
        map[Constants.DEVICE_TYPE] = Build.MANUFACTURER + Constants.HYPHEN + Build.MODEL
        val call = client.create(nic.goi.aarogyasetu.network.PostDataInterface::class.java)
            .validateOTP(map, validateOTP)

        call.enqueue(object :
            retrofit2.Callback<nic.goi.aarogyasetu.models.network.TokenValidationResponse> {
            override fun onFailure(
                call: Call<nic.goi.aarogyasetu.models.network.TokenValidationResponse>,
                t: Throwable
            ) {
                val e = Exception(t)
                sendErrorCallback(listener, e, AuthErrorInvalidOtp())
            }

            override fun onResponse(
                call: Call<nic.goi.aarogyasetu.models.network.TokenValidationResponse>,
                response: Response<nic.goi.aarogyasetu.models.network.TokenValidationResponse>
            ) {
                if (response.isSuccessful && !response.body()?.authToken.isNullOrBlank() &&
                    !response.body()?.refreshToken.isNullOrBlank()
                ) {
                    val token = response.body()!!.authToken
                    val refreshToken = response.body()!!.refreshToken
                    setToken(token)
                    setRefreshToken(refreshToken)
                    setMobile(mobile)
                    AppExecutors.runOnMain {
                        listener?.onUserVerified(token)
                    }
                } else if (response.code() == 400) {
                    val e =
                        Exception("Api Response is not success. Invalid OTP = ${response.errorBody()?.string()}")
                    sendErrorCallback(listener, e, AuthErrorInvalidOtp())
                } else if (response.code() == 401) {
                    val e =
                        Exception("Api Response is not success. Response = ${response.errorBody()?.string()}")
                    sendErrorCallback(listener, e, AuthErrorUserDisabled())
                } else {
                    val e =
                        Exception("Api Response is not success. Response = ${response.errorBody()?.string()}")
                    sendErrorCallback(listener, e, AuthErrorUnknown())
                }
            }
        })
    }

    @JvmStatic
    @Nullable
    fun getToken(): String? = try {
        authSpHelper.getString(KEY_TOKEN, "")
    } catch (e: Exception) {
        ""
    }

    @JvmStatic
    @Nullable
    fun getRefreshToken(): String? = try {
        authSpHelper.getString(KEY_REFRESH_TOKEN, "")
    } catch (e: Exception) {
        ""
    }

    @JvmStatic
    @Nullable
    fun getUserName(): String? {
        return try {
            val name: String? = authSpHelper.getString(KEY_USER_NAME, "")
            if (!name.isNullOrEmpty()) {
                return CorUtility.toTitleCase(name)
            }
            return "";
        } catch (e: Exception) {
            ""
        }
    }

    @JvmStatic
    fun setToken(token: String?) {
        if (token.isNullOrBlank()) {
            authSpHelper.removeKey(KEY_TOKEN)
        } else {
            authSpHelper.putString(KEY_TOKEN, token)
        }
    }

    @JvmStatic
    fun setRefreshToken(token: String?) {
        if (token.isNullOrBlank()) {
            authSpHelper.removeKey(KEY_REFRESH_TOKEN)
        } else {
            authSpHelper.putString(KEY_REFRESH_TOKEN, token)
        }
    }

    @JvmStatic
    fun setUserName(name: String?) {
        if (name.isNullOrBlank()) {
            authSpHelper.removeKey(KEY_USER_NAME)
        } else {
            authSpHelper.putString(KEY_USER_NAME, name)
        }
    }

    @JvmStatic
    fun removeToken() {
        setToken(null)
    }

    @JvmStatic
    fun removeRefreshToken() {
        setRefreshToken(null)
    }

    @JvmStatic
    fun removeUserName() {
        setUserName(null)
    }

    @JvmStatic
    fun clearUserDetails() {
        removeToken()
        removeRefreshToken()
        removeMobile()
        removeUserName()
    }

    @JvmStatic
    @JvmOverloads
    fun logout(context: Context, moveToPermissionScreen: Boolean = true) {
        thread {
            synchronized(this) {
                if (isSignedIn()) {
                    clearUserDetails()
                    nic.goi.aarogyasetu.prefs.SharedPref.setStringParams(
                        nic.goi.aarogyasetu.CoronaApplication.instance,
                        nic.goi.aarogyasetu.prefs.SharedPrefsConstants.UNIQUE_ID,
                        ""
                    )
                }
                if (nic.goi.aarogyasetu.background.BluetoothScanningService.serviceRunning) {
                    try {
                        val myService = Intent(
                            context,
                            nic.goi.aarogyasetu.background.BluetoothScanningService::class.java
                        )
                        context.stopService(myService)
                    } catch (e: Exception) {
                        e.reportException()
                    }
                }
                AnalyticsUtils.updateUserTraits()
                if (moveToPermissionScreen) {
                    AppExecutors.runOnMain {
                        restartActivitySessions()
                    }
                }
            }
        }
    }

    private fun restartActivitySessions() {

        Toast.makeText(
            CoronaApplication.instance,
            LocalizationUtil.getLocalisedString(CoronaApplication.instance, R.string.login_failed),
            Toast.LENGTH_SHORT
        ).show()

        val intent = Intent(CoronaApplication.instance, PermissionActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        CoronaApplication.instance.startActivity(intent)

        AnalyticsUtils.sendEvent(EventNames.EVENT_USER_SESSION_EXPIRED)
    }

    private fun sendErrorCallback(
        listener: BaseAuthListener?,
        e: java.lang.Exception?,
        authError: AuthError
    ) {
        Logger.d(TAG, "sendErrorCallback Exception = ${e?.message}")
        AppExecutors.runOnMain {
            listener?.onAuthError(e, authError)
        }
    }

}

interface BaseAuthListener {
    @MainThread
    fun onAuthError(e: java.lang.Exception?, authError: AuthError)
}

interface UserSignInListener : BaseAuthListener {
    @MainThread
    fun onAskOtp()
}

interface UserVerifyListener : BaseAuthListener {
    @MainThread
    fun onUserVerified(token: String?)
}