package nic.goi.aarogyasetu.views

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import io.fabric.sdk.android.services.common.CommonUtils
import nic.goi.aarogyasetu.BuildConfig
import nic.goi.aarogyasetu.CoronaApplication
import nic.goi.aarogyasetu.analytics.EventNames
import nic.goi.aarogyasetu.analytics.ScreenNames
import nic.goi.aarogyasetu.background.BluetoothScanningService
import nic.goi.aarogyasetu.firebase.FirebaseRemoteConfigUtil
import nic.goi.aarogyasetu.prefs.SharedPref
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants
import nic.goi.aarogyasetu.utility.AnalyticsUtils
import nic.goi.aarogyasetu.utility.AuthUtility
import nic.goi.aarogyasetu.utility.Constants
import nic.goi.aarogyasetu.utility.CorUtility
import nic.goi.aarogyasetu.utility.CorUtility.Companion.isNetworkAvailable


/**
 * @author Chandrapal Yadav
 * @author Niharika.Arora
 */
class SplashActivity : AppCompatActivity(), SelectLanguageFragment.LanguageChangeListener,
    NoNetworkDialog.Retry, ProviderInstaller.ProviderInstallListener {
    companion object {
        const val TIME_DELAY: Long = 200
        const val REQUEST_CODE_GOOGLE_SERVICE_ERROR: Int = 2000
    }

    private var retryProviderInstall: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rooted: Boolean = CommonUtils.isRooted(this)
        if (rooted) {
            showDialogAndFinish(Constants.ROOT_ALERT)
            AnalyticsUtils.sendBasicEvent(EventNames.EVENT_PHONE_ROOTED, ScreenNames.SCREEN_SPLASH)
        } else {
            ProviderInstaller.installIfNeededAsync(this, this)
        }
        AnalyticsUtils.updateUserTraits()
        AnalyticsUtils.sendEvent(EventNames.EVENT_OPEN_SPLASH)
    }

    private fun startSplashLogic() {
        FirebaseRemoteConfigUtil.INSTANCE.init()
        if (isNetworkAvailable(this@SplashActivity)) {
            setBluetoothName()
            CorUtility.checkAppMeta()
            proceedToNextSteps()
        } else {
            showNoInternet()
        }
    }

    /**
     * This method is used to decide next step. If Language not selected then select app language.
     * if signed in then go to dashboard else go to On-boarding procedure.
     */
    private fun proceedToNextSteps() {
        CoronaApplication.warmUpLocation()
        if (SharedPref.hasKey(this, SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE)) {
            CorUtility.checkStatus(this)
            Handler().postDelayed({
                if (AuthUtility.isSignedIn()) {
                    if (!BluetoothScanningService.serviceRunning) {
                        CorUtility.startService(this)
                    }
                    CorUtility.startBackgroundWorker()
                    launchHomeScreen()
                } else {
                    startOnBoardingActivity()
                }
            }, TIME_DELAY)
        } else {
            showLanguageSelectionDialog()
        }
    }

    private fun launchHomeScreen() {
        val webIntent = HomeActivity.getLaunchIntent(BuildConfig.WEB_URL, Constants.EMPTY, this)
        webIntent.putExtra(HomeActivity.DO_NOT_SHOW_BACK, true)
        if (intent.extras != null && intent.extras?.containsKey(Constants.PUSH)!!) {
            if (intent.extras!!.getString(Constants.PUSH).equals("1")) {
                var uploadType = intent.extras!!.getString(Constants.UPLOAD_TYPE);
                if(uploadType.isNullOrBlank())
                {
                    uploadType = Constants.UPLOAD_TYPES.PUSH_CONSENT;
                }
                webIntent.putExtra(Constants.PUSH,true)
                webIntent.putExtra(Constants.UPLOAD_TYPE,uploadType)

            }
        }
        resolveDeeplinkNotification(webIntent);
        startActivity(webIntent)
        finish()
    }

    private fun resolveDeeplinkNotification(intent: Intent) {
        if (getIntent() != null && getIntent().data != null && !TextUtils.isEmpty(getIntent().data!!.scheme)) // Deeplink
        {
            val data = getIntent().data
            if (!TextUtils.isEmpty(data!!.query) && data.query!!.contains(Constants.DEEPLINK_TAG)) {
                val paramString =
                    data.getQueryParameter(Constants.DEEPLINK_TAG)
                if (!paramString.isNullOrBlank()) {
                    try {
                        val tagId = Integer.valueOf(paramString!!)
                        intent.putExtra(Constants.DEEPLINK_TAG, tagId)
                    } catch (e: Exception) {}
                }
            }
        } else if (getIntent().hasExtra(Constants.DEEPLINK_TAG)||getIntent().hasExtra(Constants.TARGET)) // Notification
        {
            try {
               val target = getIntent().getStringExtra(Constants.TARGET)
                if(!target.isNullOrBlank())
                intent.putExtra(Constants.URL,target)
                val tagId =
                    getIntent().getStringExtra(Constants.DEEPLINK_TAG)
                intent.putExtra(Constants.DEEPLINK_TAG, Integer.valueOf(tagId))
            } catch (e: Exception) {}
        }
    }


    /**
     * This method is used to set unique id in the bluetooth.
     */
    private fun setBluetoothName() {
        val defaultAdapter = BluetoothAdapter.getDefaultAdapter()
        val uniqueId = SharedPref.getStringParams(
            CoronaApplication.getInstance(),
            SharedPrefsConstants.UNIQUE_ID,
            Constants.EMPTY
        )
        if (!uniqueId.isNullOrEmpty()) {
            if (CorUtility.isBluetoothPermissionAvailable(CoronaApplication.instance)
                && defaultAdapter != null && !uniqueId.isNullOrEmpty()
            ) {
                defaultAdapter.name = uniqueId
            }
        }
    }

    private fun showDialogAndFinish(message: String) {

        val builder = AlertDialog.Builder(this)
        builder.apply {
            setPositiveButton(
                Constants.CLOSE
            ) { _, _ ->
                finish()
            }

        }
        // Set other dialog properties
        builder.setTitle(Constants.ALERT)
        builder.setMessage(message)
        builder.setCancelable(false)
        if (!isFinishing) {
            builder.create().show()
        }
    }



    /**
     * This method is used to redirect user on click of the notification.
     * If data need to be shown to user then open WebView.
     * If server request contact data then send data to the server.
     */
    private fun handleRedirection(): Boolean {
        if (intent != null && intent.extras != null) {
            when {
                intent.extras!!.containsKey(Constants.TARGET) -> {
                    CorUtility.openWebView(
                        intent.extras?.getString(Constants.TARGET)!!,
                        Constants.EMPTY,
                        this
                    )
                    return true

                }
            }
        }
        return false
    }

    private fun showLanguageSelectionDialog() {
        if (supportFragmentManager.isDestroyed) {
            startOnBoardingActivity()
        }
        SelectLanguageFragment.showDialog(supportFragmentManager, false)
    }

    private fun startOnBoardingActivity() {
        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()
    }

    override fun languageChange() {
        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()
    }

    private fun showNoInternet() {
        val networkDialog = NoNetworkDialog()
        networkDialog.isCancelable = false
        networkDialog.retryUrl = Constants.EMPTY
        val fragmentTransaction =
            supportFragmentManager.beginTransaction()
        fragmentTransaction.add(networkDialog, networkDialog.tag)
        fragmentTransaction.commitAllowingStateLoss()
    }

    override fun retry(url: String?) {
        startSplashLogic()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GOOGLE_SERVICE_ERROR) {
            retryProviderInstall = true
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        if (retryProviderInstall) {
            // We can now safely retry installation.
            ProviderInstaller.installIfNeededAsync(this, this)
        }
        retryProviderInstall = false
    }

    override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            GoogleApiAvailability.getInstance().apply {
                if (isUserResolvableError(errorCode)) {
                    // Recoverable error. Show a dialog prompting the user to
                    // install/update/enable Google Play services.
                    showErrorDialogFragment(this@SplashActivity, errorCode, REQUEST_CODE_GOOGLE_SERVICE_ERROR) {
                        // The user chose not to take the recovery action
                        showDialogAndFinish(this.getErrorString(errorCode))
                        AnalyticsUtils.sendBasicEvent(EventNames.EVENT_GOOGLE_SERVICE_ERROR_RESOLUTION_CANCEL, ScreenNames.SCREEN_SPLASH)
                    }
                    AnalyticsUtils.sendBasicEvent(EventNames.EVENT_GOOGLE_SERVICE_RESOLVABLE_ERROR, ScreenNames.SCREEN_SPLASH, this.getErrorString(errorCode))
                } else {
                    showDialogAndFinish(this.getErrorString(errorCode))
                    AnalyticsUtils.sendBasicEvent(EventNames.EVENT_GOOGLE_SERVICE_NON_RESOLVABLE_ERROR, ScreenNames.SCREEN_SPLASH, this.getErrorString(errorCode))
                }
            }
        } else {
            retryProviderInstall = true
        }
    }

    override fun onProviderInstalled() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            startSplashLogic()
        } else {
            retryProviderInstall = true
        }
    }
}
