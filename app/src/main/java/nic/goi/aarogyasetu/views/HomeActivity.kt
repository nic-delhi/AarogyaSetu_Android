package nic.goi.aarogyasetu.views

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.view.ActionMode
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList
import java.util.HashMap
import java.util.Locale
import java.util.Random
import java.util.Stack

import nic.goi.aarogyasetu.BuildConfig
import nic.goi.aarogyasetu.CoronaApplication
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.analytics.EventNames
import nic.goi.aarogyasetu.analytics.ScreenNames
import nic.goi.aarogyasetu.firebase.FirebaseRemoteConfigUtil
import nic.goi.aarogyasetu.prefs.SharedPref
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants
import nic.goi.aarogyasetu.utility.AnalyticsUtils
import nic.goi.aarogyasetu.utility.AppExecutors
import nic.goi.aarogyasetu.utility.AuthUtility
import nic.goi.aarogyasetu.utility.Constants
import nic.goi.aarogyasetu.utility.CorUtility
import nic.goi.aarogyasetu.utility.CorUtilityKt
import nic.goi.aarogyasetu.utility.ExecutorHelper
import nic.goi.aarogyasetu.utility.Logger
import nic.goi.aarogyasetu.utility.UploadDataUtil
import nic.goi.aarogyasetu.views.sync.SyncDataConsentDialog
import nic.goi.aarogyasetu.views.sync.SyncDataDialog
import nic.goi.aarogyasetu.views.sync.SyncDataStateDialog

import nic.goi.aarogyasetu.utility.LocalizationUtil.getLocalisedString

/**
 * @author Chandrapal Yadav
 * @author Niharika.Arora
 */
class HomeActivity : AppCompatActivity(), SelectLanguageFragment.LanguageChangeListener, NoNetworkDialog.Retry,
    NoBluetoothDialog.BluetoothActionListener, InstallStateUpdatedListener, SyncDataDialog.SyncDataModeListener,
    SyncDataConsentDialog.ConfirmationListener, SyncDataStateDialog.SyncListener, UploadDataUtil.UploadListener,
    View.OnClickListener {
    private var mActionMode: ActionMode? = null
    private var mUploadDataUtil: UploadDataUtil? = null
    private val webPageStack = Stack()
    private var menu: View? = null
    private var menuIntro: View? = null
    private var back: View? = null
    private var doNotShowBack: Boolean = false
    private var progressBar: View? = null
    private var networkDialog: NoNetworkDialog? = null
    private var webView: WebView? = null
    private var homeNavigationView: HomeNavigationView? = null

    internal val javaScriptInterface: Object = object : Object() {

        val headers: String
            @JavascriptInterface
            get() {
                val crunchifyMap = CorUtility.Companion.getHeaders(false)
                val lastLocation = CoronaApplication.getInstance().getDeviceLastKnownLocation()
                if (lastLocation != null) {
                    crunchifyMap.put(Constants.LATITUDE, String.valueOf(lastLocation!!.getLatitude()))
                    crunchifyMap.put(Constants.LONGITUDE, String.valueOf(lastLocation!!.getLongitude()))
                }
                val gsonMapBuilder = GsonBuilder()

                val gsonObject = gsonMapBuilder.create()

                return gsonObject.toJson(crunchifyMap)
            }

        @JavascriptInterface
        fun shareApp() {
            val appUrl = CorUtility.getShareText(this@HomeActivity)
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT, appUrl)
            startActivity(Intent.createChooser(intent, ""))
        }

        @JavascriptInterface
        fun changeLanguage() {
            showLanguageSelectionDialog()
        }

        @JavascriptInterface
        fun backPressed() {
            if (webView!!.canGoBack()) {
                webView!!.goBack()
            }
        }


        @JavascriptInterface
        fun copyToClipboard(text: String) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard != null) {
                clipboard!!.setPrimaryClip(ClipData.newPlainText(null, text))
            }
        }


        @JavascriptInterface
        fun hideLoader() {
            if (progressBar != null) {
                progressBar!!.setVisibility(View.GONE)
            }
        }

        @JavascriptInterface
        fun askForUpload() {
            onUploadDataClicked()
        }

    }
    private var fullScreenVideoWebChromeClient: FullScreenVideoWebChromeClient? = null

    private val currentBaseURL: String
        get() {
            var url = ""
            if (getIntent().getData() != null) {
                val uri = this.getIntent().getData()
                url += uri.getScheme() + "://" + uri.getHost() + uri.getPath()
            } else {
                url = getIntent().getStringExtra(Constants.URL)
            }

            url += "?locale=" + SharedPref.getStringParams(
                CoronaApplication.instance,
                SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE,
                "en"
            )

            return url
        }

    private var mBluetoothStatusChangeReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        @Override
        fun onReceive(context: Context, intent: Intent) {
            val action = intent.getAction()
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_OFF -> showNoBluetoothDialog()
                    BluetoothAdapter.STATE_ON -> hideNoBluetoothDialog()
                }
            }
        }
    }

    private fun disableScreenShot() {
        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        } catch (e: Exception) {
            Logger.d(TAG, e.getLocalizedMessage())
        }

    }

    @Override
    fun onCreate(@Nullable savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_home)
        } catch (e: Exception) {
            //Android OS internal bug - When view is being inflated, the webview package is being updated by the os and therefore it can't find the webview package for those few moments.
            if (e.getMessage() != null && e.getMessage().contains("webview")) {
                Toast.makeText(getApplicationContext(), Constants.WEB_NOT_SUPPORTED, Toast.LENGTH_LONG).show()
            }
            mBluetoothStatusChangeReceiver = null
            finish()
            return
        }

        if (!BuildConfig.DEBUG) {
            disableScreenShot()
        }
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progress_bar)
        progressBar!!.setVisibility(View.VISIBLE)
        back = findViewById(R.id.back)
        menu = findViewById(R.id.menu)

        val languageChange = findViewById(R.id.language_change)
        languageChange.setOnClickListener({ v -> showLanguageSelectionDialog() })

        back!!.setOnClickListener({ v -> handleBack() })

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(mBluetoothStatusChangeReceiver, filter)
        handleShare()
        checkForUpdates()

        doNotShowBack = getIntent().getBooleanExtra(HomeActivity.DO_NOT_SHOW_BACK, false)

        setupNavigationMenu()
        setupWebView()

        if (!CorUtility.isNetworkAvailable(this@HomeActivity)) {
            showRetryDialog(currentBaseURL)
        } else {
            loadUrl(currentBaseURL)
        }

        val needPermissions = getIntent().getBooleanExtra(EXTRA_ASK_PERMISSION, false)
        if (needPermissions) {
            if (!CorUtility.arePermissionsGranted(this)) {
                CorUtility.requestPermissions(this, REQUEST_CODE_PERMISSION)
            } else {
                if (!CorUtility.isLocationOn(this)) {
                    CorUtility.enableLocation(this)
                }
            }
        }
        AnalyticsUtils.sendEvent(EventNames.EVENT_OPEN_WEB_VIEW)
        CorUtility.startService(this)
        checkForDataUpload()

        if (!SharedPref.hasKey(this, SharedPrefsConstants.APPLICATION_INSTALL_TIME)) {
            SharedPref.setStringParams(
                this,
                SharedPrefsConstants.APPLICATION_INSTALL_TIME,
                String.valueOf(System.currentTimeMillis())
            )
        }

        checkOldData()
    }

    private fun checkOldData() {
        ExecutorHelper.getThreadPoolExecutor().execute(???({ CorUtility.remove30DaysOldData() }))
    }

    private fun setupNavigationMenu() {
        val drawerLayout = findViewById(R.id.drawer)

        // Hide and show hamburger menu and menu action
        menuIntro = findViewById(R.id.hamburger_menu_intro)
        val menuIntroCount = Integer.parseInt(
            SharedPref.getStringParams(
                this,
                SharedPrefsConstants.MENU_INTRO_COUNT,
                SharedPrefsConstants.DEFAULT_MENU_INTRO_COUNT
            )
        ) + 1
        if (menuIntroCount > Constants.MAX_INTRO_VIEWS) {
            menuIntro!!.setVisibility(View.GONE)
        } else {
            SharedPref.setStringParams(this, SharedPrefsConstants.MENU_INTRO_COUNT, "" + menuIntroCount)
            findViewById(R.id.close_menu_intro).setOnClickListener({ v -> menuIntro!!.setVisibility(View.GONE) })
        }

        // Setup navigation drawer UI
        homeNavigationView = findViewById(R.id.navigation_drawer)
        homeNavigationView!!.inflate({ v ->
            drawerLayout.closeDrawer(GravityCompat.START)
            this@HomeActivity.onClick(v)
        })
        menu!!.setOnClickListener({ v -> drawerLayout.openDrawer(GravityCompat.START) })

        //Hide hamburger intro when drawer opens
        val lockMode =
            if (AuthUtility.INSTANCE.isSignedIn()) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        drawerLayout.setDrawerLockMode(lockMode)
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            @Override
            fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                menuIntro!!.setVisibility(View.GONE)
                SharedPref.setStringParams(
                    this@HomeActivity,
                    SharedPrefsConstants.MENU_INTRO_COUNT,
                    "" + Constants.MAX_INTRO_VIEWS
                )
            }
        })
    }

    private fun setupWebView() {
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        webView!!.getSettings().setJavaScriptEnabled(true)
        webView!!.getSettings().setDomStorageEnabled(true)
        webView!!.addJavascriptInterface(javaScriptInterface, "JSMobileCrm")
        fullScreenVideoWebChromeClient = FullScreenVideoWebChromeClient()
        webView!!.setWebChromeClient(fullScreenVideoWebChromeClient)
        webView!!.setWebViewClient(object : WebViewClient() {
            @Override
            fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("tel:")) {
                    try {
                        val intent = Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse(url)
                        )
                        startActivity(intent)
                    } catch (ignored: ActivityNotFoundException) {
                        // do nothing
                    }

                } else {

                    val isMyUrl = BuildConfig.WEB_HOST.equals(Uri.parse(url).getHost())

                    if (!CorUtility.isNetworkAvailable(this@HomeActivity)) {
                        showRetryDialog(if (isMyUrl) url else "")
                        progressBar!!.setVisibility(View.GONE)
                    } else {
                        if (isMyUrl) {
                            // This is my website, so do not override; let my WebView load the page
                            return false
                        }
                        progressBar!!.setVisibility(View.GONE)
                        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
                        openDefaultBrowser(url)
                        return true
                    }
                }
                progressBar!!.setVisibility(View.GONE)
                return true
            }

            @Override
            fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {

                if (url.startsWith("tel:")) {
                    val intent = Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse(url)
                    )
                    startActivity(intent)
                } else if (url.startsWith("http:") || url.startsWith("https:")) {

                    if (!CorUtility.isNetworkAvailable(this@HomeActivity)) {
                        showRetryDialog(url)

                    } else {
                        progressBar!!.setVisibility(View.VISIBLE)
                        webView!!.clearHistory()
                        if (!isTopUrlSame(url))
                            webPageStack.push(url)
                        super.onPageStarted(view, url, favicon)
                        if (webPageStack.isEmpty() || webPageStack.size() === 1 && doNotShowBack) {
                            menu!!.setVisibility(View.VISIBLE)
                            back!!.setVisibility(View.GONE)
                        } else {
                            menu!!.setVisibility(View.GONE)
                            menuIntro!!.setVisibility(View.GONE)
                            back!!.setVisibility(View.VISIBLE)
                        }
                    }
                }

            }

            @Override
            fun onPageFinished(view: WebView, url: String) {
                progressBar!!.setVisibility(View.GONE)
                super.onPageFinished(view, url)
            }

            @Override
            fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (errorResponse.getStatusCode() === 401) {
                    progressBar!!.setVisibility(View.VISIBLE)
                    ExecutorHelper.getThreadPoolExecutor().execute({

                        try {
                            var isTokenUpdated = false
                            for (i in 0..2) {
                                val isNewTokenUpdated = updateToken()
                                if (isNewTokenUpdated) {
                                    isTokenUpdated = true
                                    break
                                }
                            }
                            if (!isTokenUpdated) {
                                throw IOException()
                            }
                            AppExecutors.INSTANCE.runOnMain({
                                try {
                                    progressBar!!.setVisibility(View.GONE)
                                    loadUrl(currentBaseURL)
                                } catch (e: Exception) {
                                    CorUtilityKt.reportException(e)
                                }

                                null
                            })
                        } catch (e: IOException) {
                            AuthUtility.logout(CoronaApplication.instance)
                            // What to do here??
                            AppExecutors.INSTANCE.runOnMain(
                                {
                                    if (isFinishing()) {
                                        return@AppExecutors.INSTANCE.runOnMain null
                                    }
                                    progressBar!!.setVisibility(View.GONE)
                                    null
                                }// Maybe show some error here
                            )
                        }
                    })
                }
            }
        })
    }

    private fun openDefaultBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            if (webPageStack.empty()) {
                finish()
            }
        } catch (e: Exception) {
            if (!isFinishing()) {
                Toast.makeText(
                    this@HomeActivity,
                    Constants.Errors.SELECT_OTHER_APP, Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    private fun updateToken(): Boolean {
        try {
            AuthUtility.updateToken()
        } catch (ee: IOException) {
            return false
        }

        return true
    }


    /**
     * This method is used to share the application with other user.
     */
    private fun handleShare() {
        val share = findViewById(R.id.share)
        share.setOnClickListener({ v ->
            val appUrl = CorUtility.getShareText(this)
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT, appUrl)
            startActivity(Intent.createChooser(intent, ""))
            AnalyticsUtils.sendBasicEvent(EventNames.EVENT_SHARE_CLICKED, ScreenNames.SCREEN_DASHBOARD)
        })
    }

    @Override
    protected fun onStart() {
        super.onStart()
        if (!CorUtility.isLocationOn(this))
            enableLocation()
    }

    @Override
    protected fun onResume() {
        super.onResume()
        updateNavigationDrawer()
        if (CorUtility.arePermissionsGranted(this)) {
            checkBluetooth()
        } else {
            showPermissionAlert()
        }
        if (CorUtility.isForceUpgradeRequired()) {
            appUpdateManager!!.getAppUpdateInfo().addOnSuccessListener({ appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() === UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        requestHardUpdate(appUpdateInfo)
                    }
                }
            })
        }

    }

    private fun updateNavigationDrawer() {
        if (homeNavigationView != null) {
            homeNavigationView!!.setDetail()
        }
    }

    /**
     * When activity is paused, make sure action mode is ended properly.
     * This check would feel better to have in onDestroy(), but that seems to be
     * too late down the life cycle and the crash keeps on occurring. The drawback
     * of this solution is that the action mode is finished when app is minimized etc.
     */
    @Override
    protected fun onPause() {
        super.onPause()
        endActionMode()
        hideNoBluetoothDialog()
    }

    @Override
    protected fun onDestroy() {
        super.onDestroy()
        if (mBluetoothStatusChangeReceiver != null) {
            unregisterReceiver(mBluetoothStatusChangeReceiver)
        }
        try {
            if (appUpdateManager != null)
                appUpdateManager!!.unregisterListener(this)
        } catch (ignored: Exception) {
            //do nothing
        }

    }

    @Override
    fun onClick(v: View) {
        when (v.getId()) {
            R.id.qr -> openQrScreen()
            R.id.verify_installed_app -> loadUrl(BuildConfig.VERIFY_APP_URL)
            R.id.share_data -> onUploadDataClicked()
            R.id.call -> loadUrl(BuildConfig.CALL_US_URL)
            R.id.faq -> loadUrl(BuildConfig.FAQ_URL)
            R.id.privacy_policy -> loadUrl(BuildConfig.PRIVACY_POLICY_URL)
            R.id.terms -> loadUrl(BuildConfig.TNC_URL)
            else -> {
            }
        }
    }

    private fun openQrScreen() {
        val qrItent = Intent(this@HomeActivity, QrActivity::class.java)
        startActivity(qrItent)
    }

    /**
     * Store action mode if it is started
     *
     * @param mode: Mode that needs to be stored. It is of type android.view.ActionMode
     */
    @Override
    fun onActionModeStarted(mode: android.view.ActionMode) {
        super.onActionModeStarted(mode)
        mActionMode = mode
    }

    @Override
    fun onActionModeFinished(mode: ActionMode) {
        super.onActionModeFinished(mode)
        mActionMode = null
    }

    /**
     * Makes sure action mode is ended
     */
    private fun endActionMode() {
        if (mActionMode != null) {
            mActionMode!!.finish() /* immediately calls {@link #onActionModeFinished(ActionMode)} */
        }
    }

    private fun showRetryDialog(retryUrl: String) {
        networkDialog = NoNetworkDialog()
        networkDialog!!.setRetryUrl(retryUrl)
        showDialog(networkDialog, networkDialog!!.getTag())
    }

    private fun isTopUrlSame(urlS: String): Boolean {
        try {
            if (webPageStack.isEmpty()) {
                return false
            }
            val url = URL(urlS)
            val currUrl = url.getProtocol() + Constants.DOUBLE_SLASH + url.getHost() + url.getPath()
            val stackUrl = URL(webPageStack.peek())
            val stack = stackUrl.getProtocol() + Constants.DOUBLE_SLASH + stackUrl.getHost() + stackUrl.getPath()
            return currUrl.equalsIgnoreCase(stack)
        } catch (e: MalformedURLException) {
            //do nothing
        }

        return true
    }

    private fun handleBack() {
        if (this.fullScreenVideoWebChromeClient != null && this.fullScreenVideoWebChromeClient!!.onBackPressed()) {
            return
        }
        if (!webPageStack.isEmpty())
            webPageStack.pop()
        if (webPageStack.isEmpty()) {
            finish()
        } else {
            try {
                loadUrl(getChangedUrl(webPageStack.peek()))
            } catch (e: MalformedURLException) {
                loadUrl(webPageStack.peek())
            }

        }
    }


    private fun loadUrl(url: String) {
        if (BuildConfig.WEB_HOST.equals(Uri.parse(url).getHost())) {
            val headers = HashMap()
            headers.put(Constants.AUTH, AuthUtility.getToken())
            headers.put(Constants.VERSION, String.valueOf(BuildConfig.VERSION_CODE))
            headers.put(Constants.PLATFORM, BuildConfig.PLATFORM_KEY)
            headers.put(
                Constants.UNIQUE_ID, SharedPref.getStringParams(
                    CoronaApplication.getInstance(),
                    SharedPrefsConstants.UNIQUE_ID,
                    ""
                )
            )

            webView!!.loadUrl(url, headers)
        } else {
            openDefaultBrowser(url)
        }
    }

    fun showLanguageSelectionDialog() {
        SelectLanguageFragment.showDialog(getSupportFragmentManager(), true)
    }

    @Override
    fun languageChange() {
        if (!webPageStack.isEmpty()) {
            try {
                loadUrl(getChangedUrl(webPageStack.pop()))
            } catch (e: MalformedURLException) {
                //do nothing
            }

        }
        updateNavigationDrawer()
    }

    /**
     * This method is used to change language of the pages with selected language
     *
     * @param urlToChange: This variable is the original URL that need to be changed
     * @return The URL with selected language
     * @throws MalformedURLException: This exception is received when format of original URL is incorrect
     * or there are some invalid data in the URl.
     */
    @Throws(MalformedURLException::class)
    fun getChangedUrl(urlToChange: String): String {
        val url = URL(urlToChange)
        val map = getQueryMap(url.getQuery())
        if (map.containsKey(Constants.LOCALE)) {
            map.put(
                Constants.LOCALE,
                SharedPref.getStringParams(
                    CoronaApplication.instance,
                    SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE,
                    "en"
                )
            )
            map.put(
                Constants.LANG,
                SharedPref.getStringParams(
                    CoronaApplication.instance,
                    SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE,
                    "en"
                )
            )
        }
        if (map.containsKey(Constants.LANG)) {
            map.put(
                Constants.LOCALE,
                SharedPref.getStringParams(
                    CoronaApplication.instance,
                    SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE,
                    "en"
                )
            )
            map.put(
                Constants.LANG,
                SharedPref.getStringParams(
                    CoronaApplication.instance,
                    SharedPrefsConstants.USER_SELECTED_LANGUAGE_CODE,
                    "en"
                )
            )
        }
        var fquery = ""
        if (map.keySet().size() > 0) {
            val query = StringBuilder("?")
            for (key in map.keySet()) {
                query.append(key).append("=").append(map[key]).append("&")
            }
            fquery = query.toString().substring(0, query.toString().length() - 1)
        }
        return url.getProtocol() + Constants.DOUBLE_SLASH + url.getHost() + url.getPath() + fquery
    }

    @Override
    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.getAction() === KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                handleBack()
                return true
            }

        }
        return super.onKeyDown(keyCode, event)
    }

    @Override
    fun retry(retryUrl: String) {

        if (CorUtility.isNetworkAvailable(this@HomeActivity)) {
            if (!TextUtils.isEmpty(retryUrl))
                loadUrl(retryUrl)
        } else {
            showRetryDialog(retryUrl)
        }
    }

    @Override
    protected fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NO_NETWORK) {
            if (networkDialog != null && networkDialog!!.isAdded() && !networkDialog!!.isDetached()) {
                if (CorUtility.isNetworkAvailable(this@HomeActivity)) {
                    networkDialog!!.dismiss()
                    if (!TextUtils.isEmpty(networkDialog!!.getRetryUrl()))
                        retry(networkDialog!!.getRetryUrl())
                }

            }
        } else if (requestCode == REQUEST_CODE_IMMEDIATE_UPDATE) {
            finish()
        } else if (requestCode == REQUEST_CODE_FLEXIBLE_UPDATE) {
            //todo handle this accordingly for Immediate when user cancelled
            when (resultCode) {

            }
        }
    }

    private fun checkBluetooth() {
        showNoBluetoothDialog()
    }

    private fun showNoBluetoothDialog() {
        if (!CorUtility.isBluetoothAvailable()) {
            showDialog(NoBluetoothDialog(), FRAG_NO_BT_DIALOG)
        }
    }

    private fun hideNoBluetoothDialog() {
        val btDialog = getSupportFragmentManager().findFragmentByTag(FRAG_NO_BT_DIALOG)
        if (btDialog != null) {
            getSupportFragmentManager()
                .beginTransaction()
                .remove(btDialog)
                .commitAllowingStateLoss()
        }
    }

    @Override
    fun onBluetoothRequested() {
        CorUtility.enableBluetooth()
        Handler().postDelayed({
            if (!isFinishing() && !CorUtility.isBluetoothAvailable()) {
                val enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBT, 123)
            }
        }, 2000)
    }

    private fun enableLocation() {
        CorUtility.enableLocation(this, { aBoolean -> null })
    }


    private fun notifyUserForFail() {
        Snackbar.make(webView, Constants.DOWNLOAD_FAIL, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.message_failed_tap_to_retry, { v -> checkForUpdates() }).show()
    }

    private fun notifyUser() {
        Snackbar.make(webView, Constants.RESTART_TO_UPDATE, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.restart, { v ->
                if (appUpdateManager != null) {
                    appUpdateManager!!.completeUpdate()
                    appUpdateManager!!.unregisterListener(this)
                    appUpdateManager = null
                }
            }).show()
    }

    /**
     * This method is used to check for app update on the play store and start update if required.
     */
    private fun checkForUpdates() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager!!.registerListener(this)
        appUpdateManager!!.getAppUpdateInfo().addOnSuccessListener({ appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() === UpdateAvailability.UPDATE_AVAILABLE) {
                if (CorUtility.isForceUpgradeRequired()) {
                    requestHardUpdate(appUpdateInfo)
                } else {
                    requestSoftUpdate(appUpdateInfo)
                }
            }
        })
    }

    private fun requestSoftUpdate(appUpdateInfo: AppUpdateInfo) {
        try {

            appUpdateManager!!.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE, //  HERE specify the type of update flow you want
                this, //  the instance of an activity
                REQUEST_CODE_FLEXIBLE_UPDATE
            )
        } catch (e: IntentSender.SendIntentException) {
            //do nothing
        }

    }

    private fun requestHardUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            if (CorUtility.isForceUpgradeRequired()) {
                appUpdateManager!!.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE, //  HERE specify the type of update flow you want
                    this, //  the instance of an activity
                    REQUEST_CODE_IMMEDIATE_UPDATE
                )
            }
        } catch (e: IntentSender.SendIntentException) {
            //do nothing
        }

    }

    @Override
    fun onStateUpdate(installState: InstallState) {
        if (installState.installStatus() === InstallStatus.DOWNLOADED) {
            notifyUser()
        } else if (installState.installStatus() === InstallStatus.FAILED) {
            notifyUserForFail()
        }
    }

    private fun showPermissionAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getLocalisedString(this, R.string.permission_alert_message)).setCancelable(false)
            .setPositiveButton(Constants.ACTION_GOTO_SETTINGS, { dialog, which -> openAppSettings() })
            .setNegativeButton(Constants.ACTION_REMIND_LATER, { dialog, which -> dialog.cancel() })
        val alertDialog = builder.create()
        if (!isFinishing()) {
            alertDialog.show()
        }

    }

    private fun openAppSettings() {
        val intent = Intent()
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts(Constants.PACKAGE, getPackageName(), null)
        intent.setData(uri)
        try {
            startActivity(intent)
        } catch (ignored: ActivityNotFoundException) {
        }

    }

    private fun showSyncDataDialog() {
        showDialog(SyncDataDialog(), SYNC_DATA_DIALOG)
    }

    private fun showSyncDataConsentDialog(uploadType: String) {
        showDialog(SyncDataConsentDialog.newInstance(uploadType), SYNC_DATA_CONSENT_DIALOG)
    }

    private fun showSyncingDataDialog(state: SyncDataStateDialog.State, uploadType: String?) {
        showDialog(SyncDataStateDialog.newInstance(state, uploadType), SYNCING_DIALOG)
    }

    private fun showDialog(dialog: DialogFragment, dialogType: String) {
        val previousDialog = getSupportFragmentManager()
            .findFragmentByTag(dialogType) as DialogFragment
        if (previousDialog != null) {
            previousDialog!!.dismissAllowingStateLoss()
        }
        dialog.setCancelable(false)
        val fragmentTransaction = getSupportFragmentManager().beginTransaction()
        fragmentTransaction.add(dialog, dialogType)
        fragmentTransaction.commitAllowingStateLoss()

    }

    @Override
    fun syncDataWith(@NotNull mode: String) {
        showSyncDataConsentDialog(mode)
    }

    @Override
    fun proceedSyncing(@NotNull uploadType: String) {
        showSyncingDataDialog(SyncDataStateDialog.State.SYNCING, uploadType)
        startUploading(uploadType)
    }

    @Override
    fun cancelSyncing() {

    }

    @Override
    fun retrySyncing(@NotNull uploadType: String) {
        showSyncingDataDialog(SyncDataStateDialog.State.SYNCING, uploadType)
        startUploading(uploadType)
    }

    @Override
    fun onUploadSuccess() {
        showSyncingDataDialog(SyncDataStateDialog.State.SUCCESS, null)
        mUploadDataUtil = null
    }

    @Override
    fun onUploadFailure(@NotNull uploadType: String) {
        showSyncingDataDialog(SyncDataStateDialog.State.FAILURE, uploadType)
        mUploadDataUtil = null
    }

    private fun checkForDataUpload() {

        if (!FirebaseRemoteConfigUtil.getINSTANCE().isUploadEnabled() || SharedPref.getBooleanParams(
                getBaseContext(),
                Constants.PUSH_COVID_POSTIVE_P
            ) || !AuthUtility.INSTANCE.isSignedIn()
        )
        // Put status and condition here if possible
        {
            homeNavigationView!!.hideShareData()
        }

        if (getIntent() != null && getIntent().hasExtra(Constants.PUSH) && getIntent().hasExtra(Constants.UPLOAD_TYPE)) {
            val uploadType = getIntent().getStringExtra(Constants.UPLOAD_TYPE)
            showSyncDataConsentDialog(uploadType)
        }
    }

    private fun onUploadDataClicked() {
        if (FirebaseRemoteConfigUtil.getINSTANCE().disableSyncChoice()) {
            showSyncDataConsentDialog(Constants.UPLOAD_TYPES.SELF_CONSENT)
        } else {
            showSyncDataDialog()
        }

        AnalyticsUtils.sendBasicEvent(EventNames.EVENT_OPEN_UPLOAD_CONSENT_SCREEN, EventNames.EVENT_OPEN_WEB_VIEW)
    }

    private fun startUploading(uploadType: String) {
        mUploadDataUtil = UploadDataUtil(uploadType, this)
        mUploadDataUtil!!.startInBackground()
    }

    private inner class FullScreenVideoWebChromeClient : WebChromeClient() {
        private var mCustomViewCallback: CustomViewCallback? = null
        private var mOriginalOrientation: Int = 0
        private var mOriginalSystemUiVisibility: Int = 0
        private var mCustomView: View? = null

        @Override
        fun onShowCustomView(paramView: View, callback: CustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView()
                return
            }
            this.mCustomView = paramView
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility()
            this.mOriginalOrientation = getRequestedOrientation()
            this.mCustomViewCallback = callback
            (getWindow().getDecorView() as FrameLayout).addView(this.mCustomView, FrameLayout.LayoutParams(-1, -1))
            getWindow().getDecorView().setSystemUiVisibility(3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

        }

        @Override
        fun onHideCustomView() {
            (getWindow().getDecorView() as FrameLayout).removeView(this.mCustomView)
            this.mCustomView = null
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility)
            setRequestedOrientation(this.mOriginalOrientation)
            this.mCustomViewCallback!!.onCustomViewHidden()
            this.mCustomViewCallback = null
        }

        fun onBackPressed(): Boolean {
            if (this.mCustomView != null) {
                onHideCustomView()
                return true
            }
            return false
        }
    }

    companion object {
        private val TAG = HomeActivity::class.java!!.getSimpleName()

        val FRAG_NO_BT_DIALOG = "frag_no_bt_dialog"
        val SYNC_DATA_DIALOG = "sync_data_dialog"
        val SYNC_DATA_CONSENT_DIALOG = "sync_data_consent_dialog"
        val SYNCING_DIALOG = "syncing_dialog"
        val EXTRA_ASK_PERMISSION = "need_permissions"
        val DO_NOT_SHOW_BACK = "do_not_show_back"

        val NO_NETWORK: Integer = 1000

        private val REQUEST_CODE_PERMISSION = 642
        private val REQUEST_CODE_FLEXIBLE_UPDATE = 1734
        private val REQUEST_CODE_IMMEDIATE_UPDATE = 1736

        private var appUpdateManager: AppUpdateManager? = null

        fun getQueryMap(query: String): Map<String, String> {
            val map = HashMap()
            if (TextUtils.isEmpty(query)) {
                return map
            }
            val params = query.split("&")

            for (param in params) {
                var value = ""
                val name: String
                val splitArray = param.split("=")
                if (splitArray.size > 0) {
                    name = splitArray[0]
                    if (splitArray.size > 1) {
                        value = splitArray[1]
                    }
                    map.put(name, value)
                }
            }
            return map
        }

        fun getLaunchIntent(@NotNull url: String, @NotNull title: String, @NotNull context: Context): Intent {
            val intent = Intent(context, HomeActivity::class.java)
            intent.putExtra(Constants.URL, url)
            intent.putExtra(Constants.TITLE, title)
            return intent
        }
    }
}
