package nic.goi.aarogyasetu.utility

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.WorkerThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.crashlytics.android.Crashlytics
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import nic.goi.aarogyasetu.BuildConfig
import nic.goi.aarogyasetu.CoronaApplication
import nic.goi.aarogyasetu.analytics.EventNames
import nic.goi.aarogyasetu.analytics.ScreenNames
import nic.goi.aarogyasetu.background.BackgroundWorker
import nic.goi.aarogyasetu.background.BluetoothScanningService
import nic.goi.aarogyasetu.listener.QrCodeListener
import nic.goi.aarogyasetu.listener.QrPublicKeyListener
import nic.goi.aarogyasetu.models.BulkDataObject
import nic.goi.aarogyasetu.models.network.RegisterationData
import nic.goi.aarogyasetu.network.NetworkClient
import nic.goi.aarogyasetu.prefs.SharedPref
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants
import nic.goi.aarogyasetu.views.HomeActivity
import nic.goi.aarogyasetu.views.PermissionActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Damanpreet Singh
 * updated by Niharika
 */
class CorUtility {

    companion object {
        private var isForceUpgrade: Boolean = false

        fun getCurrentEpochTimeInSec(): Int {
            val dateObj = Date()
            return (dateObj.time / 1000).toInt()
        }

        @JvmStatic
        fun isNetworkAvailable(context: Context?): Boolean {
            val connectivityManager =
                context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }


        @WorkerThread
        fun hitNetworkRequest(
            bulkDataObject: BulkDataObject?
        ): Response<JSONObject>? {
            if (!AuthUtility.isSignedIn()) {
                return null
            }


            val client =
                NetworkClient.getRetrofitClient(true, true, true, "")
            val call = client.create(nic.goi.aarogyasetu.network.PostDataInterface::class.java)
                .postUserData(
                    getHeaders(false)
                    ,
                    bulkDataObject
                )

            return call.execute()
        }

        fun checkAppMeta() {
            val client = NetworkClient.getRetrofitClient(
                false,
                false,
                false,
                ""
            )

            val call = client.create(nic.goi.aarogyasetu.network.PostDataInterface::class.java)
                .appMeta(getHeadersWithoutSSL())
            call.enqueue(object : retrofit2.Callback<JsonElement> {

                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonObject = response.body()!!.asJsonObject
                            if (jsonObject.has(Constants.FORCE_UPGRADE)) {
                                parseForceUpgradeJson(jsonObject.get(Constants.FORCE_UPGRADE).toString())
                            }

                        } catch (e: java.lang.Exception) {
                            //do nothing
                        }
                    }
                }

                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    //do nothing
                }
            })

        }

        fun registerUser(context: Context, listener: PermissionActivity.LoginSuccess) {
            val client =
                NetworkClient.getRetrofitClient(false, false, true, "")

            var registerationData = RegisterationData(
                BluetoothAdapter.getDefaultAdapter().name,
                getBluetoothMacAddress(), FirebaseInstanceId.getInstance().getToken()
            )

            registerationData.isBlAllowed =
                (isBluetoothPermissionAvailable(CoronaApplication.instance))
            registerationData.isLocAllowed =
                isLocationPermissionAvailable(CoronaApplication.instance)
            registerationData.isBlOn = isBluetoothAvailable()
            registerationData.isLocOn = isLocationOn(CoronaApplication.instance)

            val eventName =
                if (!registerationData.lat.isNullOrBlank()) EventNames.EVENT_REGISTER_LOC
                else EventNames.EVENT_REGISTER_WITHOUT_LOC
            AnalyticsUtils.sendBasicEvent(eventName, ScreenNames.SCREEN_PERMISSION)

            val call = client.create(nic.goi.aarogyasetu.network.PostDataInterface::class.java)
                .registerUser(getHeaders(false), registerationData)

            call.enqueue(object : retrofit2.Callback<JsonElement> {


                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful && response.body() != null) {
                        parseResponse(response, listener)
                        if (!SharedPref.getStringParams(
                                CoronaApplication.instance,
                                SharedPrefsConstants.UNIQUE_ID,
                                ""
                            ).isNullOrEmpty()
                        ) {
                            listener.loginSuccess()
                            checkStatus(context)
                        } else {
                            checkStatus(context, listener)
                        }
                    } else {
                        listener.loginFailed()
                    }
                }

                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    val e = Exception(t)
                    e.reportException()
                    listener.loginFailed()
                }


            })
        }


        /**
         * This method checks if contact data needed on the serve and upload them  if needed.
         * This method also get unique id of the user
         */
        fun checkStatus(context: Context, listener: PermissionActivity.LoginSuccess? = null) {
            if (!AuthUtility.isSignedIn()) return
            val client =
                NetworkClient.getRetrofitClient(false, false, true, "")

            val call = client.create(nic.goi.aarogyasetu.network.PostDataInterface::class.java)
                .updateStatus(
                    getHeaders(false)
                )

            call.enqueue(object : retrofit2.Callback<JsonElement> {

                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful && response.body() != null) {
                        val jsonObject = response.body()!!.asJsonObject
                        if (jsonObject.has(Constants.PUSH_COVID_POSTIVE_P) && jsonObject.get(
                                Constants.PUSH_COVID_POSTIVE_P
                            ).asInt == 1
                        ) {
                            SharedPref.setBooleanParams(
                                context,
                                Constants.PUSH_COVID_POSTIVE_P,
                                true
                            )
                            val uploadDataUtil = UploadDataUtil()
                            uploadDataUtil.startInBackground()

                        } else {
                            SharedPref.setBooleanParams(
                                context,
                                Constants.PUSH_COVID_POSTIVE_P,
                                false
                            )
                        }

                        savePublicKey(jsonObject)

                        if (jsonObject.has(Constants.UNIQUE_ID)) {
                            SharedPref.setStringParams(
                                CoronaApplication.instance,
                                SharedPrefsConstants.UNIQUE_ID,
                                jsonObject.get(Constants.UNIQUE_ID).asString
                            )
                            listener?.loginSuccess()
                        }

                        if (SharedPref.getStringParams(
                                context,
                                SharedPrefsConstants.UNIQUE_ID,
                                ""
                            ).isNullOrEmpty()
                        ) {
                            listener?.loginFailed()
                        }
                    }
                }

                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    listener?.loginFailed()
                }
            })
        }

        private fun savePublicKey(jsonObject: JsonObject) {
            if (jsonObject.has(Constants.QR_PUBLIC_KEY)) {
                SharedPref.setStringParams(
                    CoronaApplication.instance,
                    SharedPrefsConstants.PUBLIC_KEY,
                    jsonObject.get(Constants.QR_PUBLIC_KEY).asString
                )
            }
        }

        private fun parseQrFetchResponse(
            response: Response<JsonElement>,
            listener: QrCodeListener? = null
        ) {
            try {
                val jsonObject = response.body()!!.asJsonObject
                val data =
                    jsonObject.get(Constants.DATA).asString
                if (data.isNullOrEmpty()) {
                    listener?.onFailure()
                } else {
                    listener?.onQrCodeFetched(data)
                }
            } catch (e: java.lang.Exception) {
                e.reportException()
                listener?.onFailure()
            }
        }

        private fun parseQrPublicKeyResponse(
            response: Response<JsonElement>,
            listener: QrPublicKeyListener? = null
        ) {
            try {
                val jsonObject = response.body()!!.asJsonObject
                val data =
                    jsonObject.get(Constants.DATA).asString
                if (data.isNullOrEmpty()) {
                    listener?.onPublicKeyFetchFailure()
                } else {
                    SharedPref.setStringParams(
                        CoronaApplication.instance,
                        SharedPrefsConstants.PUBLIC_KEY,
                        data
                    )
                    listener?.onQrPublicKeyFetched()
                }
            } catch (e: java.lang.Exception) {
                listener?.onPublicKeyFetchFailure()
            }
        }

        private fun parseResponse(
            response: Response<JsonElement>,
            listener: PermissionActivity.LoginSuccess? = null
        ) {
            try {
                val jsonObject = response.body()!!.asJsonObject
                val asJsonObject =
                    jsonObject.getAsJsonObject(Constants.DATA)
                val id = asJsonObject.get(Constants.UNIQUE_ID).asString

                SharedPref.setStringParams(
                    CoronaApplication.instance,
                    SharedPrefsConstants.UNIQUE_ID,
                    id
                )
            } catch (e: java.lang.Exception) {
                e.reportException()
                listener?.loginFailed()
            }
        }

        fun sendTokenAndRegisterUser(context: Context, listener: PermissionActivity.LoginSuccess) {
            ExecutorHelper.getThreadPoolExecutor().execute {
                // Get new Instance ID token
                val token = AuthUtility.getToken()
                if (token.isNullOrBlank()) {
                    return@execute
                }
                registerUser(context, listener)
            }
        }

        fun refreshFCMToken(token: String) {
            val uidToken = AuthUtility.getToken()
            if (uidToken.isNullOrEmpty())
                return
            val client =
                NetworkClient.getRetrofitClient(false, false, true, "")

            val call = client.create(nic.goi.aarogyasetu.network.PostDataInterface::class.java)
                .refreshFCM(
                    getHeaders(false),
                    nic.goi.aarogyasetu.models.network.FCMTokenObject(token)
                )


            call.enqueue(object : retrofit2.Callback<JSONObject> {


                override fun onResponse(p0: Call<JSONObject>?, p1: Response<JSONObject>?) {
                }

                override fun onFailure(p0: Call<JSONObject>?, p1: Throwable?) {
                }
            })
        }

        private fun getHeadersWithoutSSL(): Map<String, String> {

            val map = mutableMapOf<String, String>()
            map[Constants.PLATFORM] = BuildConfig.PLATFORM_KEY
            map[Constants.VERSION] = BuildConfig.VERSION_CODE.toString()
            map[Constants.APP_VERSION_NAME] = BuildConfig.VERSION_NAME
            map[Constants.OS] = Build.VERSION.SDK_INT.toString()
            map[Constants.DEVICE_TYPE] = Build.MANUFACTURER + Constants.HYPHEN + Build.MODEL
            map[Constants.CONTENT_TYPE] =
                Constants.APPLICATION_JSON
            return map
        }

        fun getHeaders(isMultipart: Boolean): Map<String, String> {
            val token = AuthUtility.getToken()
            val map = mutableMapOf<String, String>()
            map[Constants.AUTH] = token ?: ""
            map[Constants.PLATFORM] = BuildConfig.PLATFORM_KEY
            map[Constants.OS] = Build.VERSION.SDK_INT.toString()
            map[Constants.DEVICE_TYPE] = Build.MANUFACTURER + Constants.HYPHEN + Build.MODEL
            map[Constants.VERSION] = BuildConfig.VERSION_CODE.toString()
            map[Constants.APP_VERSION_NAME] = BuildConfig.VERSION_NAME
            if (isMultipart) {
                map[Constants.CONTENT_TYPE] =
                    Constants.CONTENT_TYPE_MULTIPART

                map[Constants.ACCEPT_ENCODING] =
                    Constants.GZIP
            } else {
                map[Constants.CONTENT_TYPE] =
                    Constants.APPLICATION_JSON
            }
            return map
        }

        private fun getBluetoothMacAddress(): String? {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            var bluetoothMacAddress: String? = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val mServiceField: Field =
                        bluetoothAdapter.javaClass.getDeclaredField("mService")
                    mServiceField.isAccessible = true
                    val btManagerService: Any? = mServiceField.get(bluetoothAdapter)
                    if (btManagerService != null) {
                        bluetoothMacAddress =
                            btManagerService.javaClass.getMethod("getAddress").invoke(
                                btManagerService
                            ) as String?
                    }
                } catch (e: NoSuchFieldException) {
                } catch (e: NoSuchMethodException) {
                } catch (e: IllegalAccessException) {
                } catch (e: InvocationTargetException) {
                } finally {
                    if (bluetoothMacAddress.isNullOrEmpty()) {
                        bluetoothMacAddress = android.provider.Settings.Secure.getString(
                            CoronaApplication.getInstance().contentResolver,
                            Constants.BLUETOOTH_ADDRESS
                        );
                    }
                }
            } else {
                bluetoothMacAddress = bluetoothAdapter.address
            }
            if (bluetoothMacAddress.isNullOrEmpty()) {
                bluetoothMacAddress = bluetoothAdapter.address
            }
            return bluetoothMacAddress
        }

        @JvmOverloads
        fun openWebView(
            url: String,
            title: String,
            context: Context,
            isNewTask: Boolean = false,
            extrasBundle: Bundle? = null
        ) {
            val intent =
                HomeActivity.getLaunchIntent(url, title, context)
            if (isNewTask) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            extrasBundle?.let {
                intent.putExtras(extrasBundle)
            }
            context.startActivity(intent)
        }


        fun parseForceUpgradeJson(jsonString: String) {
            val obj = JSONObject(jsonString)
            val array = obj.optJSONArray(Constants.SPECIFIC_VERSION)
            val minVer = obj.optInt(Constants.MIN_VERSION, -1)
            if (minVer != -1) {
                if (BuildConfig.VERSION_CODE < minVer)
                    isForceUpgrade = true
                return
            } else if (array != null) {
                for (x in 0 until array.length()) {
                    if (BuildConfig.VERSION_CODE == array[x]) {
                        isForceUpgrade = true
                        return
                    }
                }
            } else {
                isForceUpgrade = false
            }
        }

        @JvmStatic
        fun isForceUpgradeRequired(): Boolean {
            return isForceUpgrade
        }

        @JvmStatic
        fun requestPermissions(
            context: Activity,
            permissionRequestCode: Int
        ) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    permissionRequestCode
                )
            } else {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    permissionRequestCode
                )
            }
        }

        @JvmStatic
        fun enableLocation(context: Context) {
            LocationUtils(context).turnLocationOn {

            }
        }

        @JvmStatic
        fun enableLocation(context: Context, locationOn: (isOn: Boolean) -> Unit?) {
            LocationUtils(context).turnLocationOn {
                if (locationOn != null)
                    locationOn(it)
            }
        }

        @JvmStatic
        @JvmOverloads
        fun isLocationOn(context: Context): Boolean {

            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            var gps_enabled = false;
            var network_enabled = false;

            try {
                gps_enabled =
                    locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            } catch (e: Exception) {
            }

            try {
                network_enabled =
                    locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false
            } catch (e: Exception) {
            }

            return (gps_enabled || network_enabled)
        }

        @JvmStatic
        public fun arePermissionsGranted(context: Context): Boolean {
            val permission1 =
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH)
            val permission2 =
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN)

            val coarseLocation =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

            val gpsPermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

            val locPermission =
                if (coarseLocation == PackageManager.PERMISSION_GRANTED || gpsPermission == PackageManager.PERMISSION_GRANTED) PackageManager.PERMISSION_GRANTED else -1

            if (permission1 != PackageManager.PERMISSION_GRANTED
                || permission2 != PackageManager.PERMISSION_GRANTED
                || locPermission != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }


            return true
        }

        @JvmStatic
        fun startService(activity: Activity) {
            if (!BluetoothScanningService.serviceRunning) {
                val uniqueId = SharedPref.getStringParams(
                    CoronaApplication.getInstance(),
                    SharedPrefsConstants.UNIQUE_ID,
                    ""
                )
                if (uniqueId.isNotEmpty() && !activity.isFinishing) {
                    val intent = Intent(activity, BluetoothScanningService::class.java)
                    ContextCompat.startForegroundService(activity, intent)
                }

            }

        }


        @JvmStatic
        @JvmOverloads
        public fun isBluetoothPermissionAvailable(context: Context): Boolean {
            val permission1 =
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH)
            val permission2 =
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN)

            return (permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED)
        }

        @JvmStatic
        @JvmOverloads
        public fun isQRPermissionAvailable(context: Context): Boolean {
            val permission1 =
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            return (permission1 == PackageManager.PERMISSION_GRANTED)
        }

        public fun isLocationPermissionAvailable(context: Context): Boolean {
            val permission3 =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            val permission4 =
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

            return (permission3 == PackageManager.PERMISSION_GRANTED || permission4 == PackageManager.PERMISSION_GRANTED)
        }


        fun getNotificationChannel(): String {
            return nic.goi.aarogyasetu.utility.Constants.NOTIFICATION_CHANNEL
        }

        @JvmStatic
        fun getShareText(context: Context): String? {
            var shareMessage: String = Constants.SHARE_TEXT
            shareMessage =
                shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n" + Constants.IOS_URL_TEXT + "\n" + Constants.IOS_URL_LINK + "\n"
            return shareMessage
        }

        fun pushDataToServer(context: Context) {

            val mWorkManager = WorkManager.getInstance(context)
            val workRequest = OneTimeWorkRequest.Builder(
                nic.goi.aarogyasetu.background.UploadDataToServerWorker::class.java
            ).build()
            mWorkManager.enqueue(workRequest)
        }

        @JvmStatic
        fun isBluetoothAvailable(): Boolean {
            if (isBluetoothPermissionAvailable(CoronaApplication.instance.context)) {
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                return bluetoothAdapter != null &&
                        bluetoothAdapter.isEnabled &&
                        bluetoothAdapter.state == BluetoothAdapter.STATE_ON
            }
            return false
        }

        @JvmStatic
        fun enableBluetooth() {
            val defaultAdapter = BluetoothAdapter.getDefaultAdapter()
            if (defaultAdapter != null && !defaultAdapter.isEnabled) {
                defaultAdapter.enable()
            }
        }

        fun setTextViewHTML(
            textView: TextView,
            html: Spanned,
            onLinkClick: (link: String) -> Unit
        ) {
            val strBuilder = SpannableStringBuilder(html)
            val urls = strBuilder.getSpans(0, html.length, URLSpan::class.java)
            urls.forEach {
                makeLinkClickable(strBuilder, it, onLinkClick)
            }
            AppExecutors.runOnMain {
                if (textView.context != null) {
                    textView.text = strBuilder
                    textView.movementMethod = LinkMovementMethod.getInstance()
                }
            }
        }

        private fun makeLinkClickable(
            strBuilder: SpannableStringBuilder,
            span: URLSpan,
            onLinkClick: (link: String) -> Unit
        ) {
            val start = strBuilder.getSpanStart(span)
            val end = strBuilder.getSpanEnd(span)
            val flags = strBuilder.getSpanFlags(span)
            val clickable = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onLinkClick(span.url)
                }
            }
            strBuilder.setSpan(clickable, start, end, flags)
            strBuilder.removeSpan(span)
        }

        fun getInstallTimes(): InstallTimeModel? {
            return try {
                val packageManager = CoronaApplication.instance.packageManager
                val packageName = CoronaApplication.instance.packageName
                val pInfo: PackageInfo =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
                val lastUpdateTime = pInfo.lastUpdateTime.toString()
                val firstInstallTime = pInfo.firstInstallTime.toString()
                InstallTimeModel(firstInstallTime, lastUpdateTime)
            } catch (e: Exception) {
                null
            }
        }

        fun startBackgroundWorker() {
            val workManager = WorkManager.getInstance(CoronaApplication.getInstance())
            val workRequest = PeriodicWorkRequest.Builder(
                BackgroundWorker::class.java,
                16,
                TimeUnit.MINUTES
            ).build()
            workManager.enqueueUniquePeriodicWork(
                BackgroundWorker.UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun fetchQrCodeText(listener: QrCodeListener) {
            Logger.d(Constants.QR_SCREEN_TAG, "Api hit for fetch ")

            val client =
                NetworkClient.getRetrofitClient(false, false, true, "")

            val call = client.create(nic.goi.aarogyasetu.network.PostDataInterface::class.java)
                .fetchQr(getHeaders(false))

            call.enqueue(object : retrofit2.Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful && response.body() != null) {
                        Logger.d(Constants.QR_SCREEN_TAG, "Api success")
                        parseQrFetchResponse(response, listener)
                    } else {
                        listener.onFailure()
                    }
                }

                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    Logger.d(Constants.QR_SCREEN_TAG, "Api failure")
                    listener.onFailure()
                }
            })
        }

        fun fetchQrPublicKey(listener: QrPublicKeyListener) {
            Logger.d(Constants.QR_SCREEN_TAG, "Api hit for public key ")

            val client =
                NetworkClient.getRetrofitClient(false, false, true, "")

            val call = client.create(nic.goi.aarogyasetu.network.PostDataInterface::class.java)
                .fetchQrPublicKey(getHeaders(false))

            call.enqueue(object : retrofit2.Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful && response.body() != null) {
                        Logger.d(Constants.QR_SCREEN_TAG, "Public key Api success")
                        parseQrPublicKeyResponse(response, listener)
                    } else {
                        listener.onPublicKeyFetchFailure()
                    }
                }

                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    Logger.d(Constants.QR_SCREEN_TAG, "Public key Api failure")
                    listener.onPublicKeyFetchFailure()
                }
            })
        }

        @JvmStatic
        fun remove30DaysOldData() {
            val dbInstance = nic.goi.aarogyasetu.db.FightCovidDB.getInstance()
            val timestamp30 = 30 * 24 * 60 * 60  // timestamp for 30 days
            dbInstance.bluetoothDataDao.deleteXDaysOldData(timestamp30, getCurrentEpochTimeInSec())
        }

        @JvmStatic
        fun toTitleCase(str: String?): String? {
            if (str == null) {
                return null
            }
            var space = true
            val builder = StringBuilder(str)
            val len = builder.length
            for (i in 0 until len) {
                val c = builder[i]
                if (space) {
                    if (!Character.isWhitespace(c)) {
                        // Convert to title case and switch out of whitespace mode.
                        builder.setCharAt(i, Character.toTitleCase(c))
                        space = false
                    }
                } else if (Character.isWhitespace(c)) {
                    space = true
                } else {
                    builder.setCharAt(i, Character.toLowerCase(c))
                }
            }
            return builder.toString()
        }
    }
}

data class InstallTimeModel(val firstInstallTime: String, val lastUpdateTime: String)

fun Exception?.reportException() {
    this?.let {
        if (!BuildConfig.DEBUG) {
            try {
                Crashlytics.getInstance().core.logException(it)
            } catch (e: java.lang.Exception) {
                //
            }
        }
    }



}



