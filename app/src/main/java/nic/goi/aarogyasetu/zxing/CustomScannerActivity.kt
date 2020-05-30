package nic.goi.aarogyasetu.zxing

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.journeyapps.barcodescanner.DecoratedBarcodeView

import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.io.DecodingException
import io.jsonwebtoken.security.SignatureException
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.utility.Constants
import nic.goi.aarogyasetu.utility.CorUtility
import nic.goi.aarogyasetu.utility.DecryptionUtil
import nic.goi.aarogyasetu.utility.LocalizationUtil
import nic.goi.aarogyasetu.views.QrActivity

import android.view.View.GONE
import android.view.View.VISIBLE
import nic.goi.aarogyasetu.utility.LocalizationUtil.getLocalisedString

/**
 * Custom Scanner Activity extending from Activity to display a custom layout form scanner view.
 *
 * @author Niharika.Arora
 */
class CustomScannerActivity : Activity(), CustomCaptureManager.CaptureViewListener {

    private var capture: CustomCaptureManager? = null
    private var barcodeScannerView: DecoratedBarcodeView? = null
    private var statusContainer: View? = null
    private var statusClose: ImageView? = null
    private var close: ImageView? = null
    private var desc: TextView? = null
    private var descReason: TextView? = null
    private var generateQr: TextView? = null
    private var promptContainer: View? = null

    @Override
    protected fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_custom_scanner)

        configureViews()
        configureClicks()
        initCapture(savedInstanceState)
    }

    private fun configureViews() {
        barcodeScannerView = findViewById(R.id.barcode_scanner)
        close = findViewById(R.id.close)
        statusClose = findViewById(R.id.status_close)
        statusContainer = findViewById(R.id.status_container)
        desc = findViewById(R.id.failure_reason)
        descReason = findViewById(R.id.failure_reason_desc)
        generateQr = findViewById(R.id.generate_qr)
        generateQr!!.setText(LocalizationUtil.getLocalisedString(this, R.string.generate_my_qr_code))
        promptContainer = findViewById(R.id.prompt_container)
        val statusDescription = findViewById(R.id.status_view)
        statusDescription.setText(LocalizationUtil.getLocalisedString(this, R.string.scan_prompt))
    }

    private fun configureClicks() {
        onGenerateQrClick()
        onCloseClick()
        onStatusCloseClick()
    }

    private fun initCapture(savedInstanceState: Bundle) {
        capture = CustomCaptureManager(this, barcodeScannerView)
        barcodeScannerView!!.getBarcodeView().getCameraSettings().setAutoFocusEnabled(true)
        capture!!.setViewCaptureListener(this)
        capture!!.initializeFromIntent(getIntent(), savedInstanceState)
        capture!!.decode()
    }

    private fun configurePromptContainer() {
        if (CorUtility.isQRPermissionAvailable(this)) {
            promptContainer!!.setVisibility(VISIBLE)
        } else {
            promptContainer!!.setVisibility(GONE)
        }
    }


    private fun onGenerateQrClick() {
        generateQr!!.setOnClickListener({ v ->
            QrActivity.start(this)
            finish()
        })
    }

    private fun onStatusCloseClick() {
        statusClose!!.setOnClickListener({ v -> closeStausView() })
    }

    private fun closeStausView() {
        statusContainer!!.setVisibility(GONE)
        capture!!.setViewCaptureListener(this)
        capture!!.decode()
        capture!!.onResume()
    }

    private fun onCloseClick() {
        close!!.setOnClickListener({ v -> finish() })
    }

    @Override
    protected fun onResume() {
        super.onResume()
        configurePromptContainer()
        capture!!.onResume()
    }

    @Override
    protected fun onPause() {
        super.onPause()
        capture!!.onPause()
    }

    @Override
    protected fun onDestroy() {
        super.onDestroy()
        capture!!.onDestroy()
    }

    @Override
    protected fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture!!.onSaveInstanceState(outState)
    }

    @Override
    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeScannerView!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    @Override
    fun onResultFetched(code: String) {
        statusContainer!!.setVisibility(VISIBLE)
        var claimsJws: Jws<Claims>? = null
        try {
            claimsJws = DecryptionUtil.decryptFile(code)
        } catch (e: ExpiredJwtException) {
            showExpiredCode()
        } catch (exception: NoSuchAlgorithmException) {
            showInvalidStatus()
        } catch (exception: DecodingException) {
            showInvalidStatus()
        } catch (exception: MalformedJwtException) {
            showInvalidStatus()
        } catch (exception: Exception) {
            showCommonInvalidStatus()
        }

        if (claimsJws != null) {
            try {
                val body = claimsJws!!.getBody()
                if (body != null) {
                    val expiry = body!!.get(Constants.EXPIRY, Long::class.java)
                    val name = body!!.get(Constants.NAME, String::class.java)
                    val mobileNo = body!!.get(Constants.MOBILE, String::class.java)
                    val status = body!!.get(Constants.STATUS, String::class.java)
                    val millisecondsMultiplier = 1000L
                    val countDownMilliSeconds = expiry * millisecondsMultiplier
                    if (expiry <= 0 || TextUtils.isEmpty(mobileNo) || TextUtils.isEmpty(status)) {
                        showInvalidStatus()
                    } else if (expiry > 0 && System.currentTimeMillis() - countDownMilliSeconds > 0) {
                        showExpiredCode()
                    } else if (!TextUtils.isEmpty(mobileNo) && !TextUtils.isEmpty(status)) {
                        showPersonStatus(name, mobileNo, status)
                    } else {
                        showInvalidStatus()
                    }
                } else {
                    showCommonInvalidStatus()
                }
            } catch (ex: Exception) {
                showCommonInvalidStatus()
            }

        }
        hideStatusContainerAfterDelay()
    }

    private fun hideStatusContainerAfterDelay() {
        statusContainer!!.postDelayed(???({ this.closeStausView() }), 5000)
    }

    private fun showPersonStatus(scannerName: String, mobileNo: String, status: String) {
        var name = ""
        if (!TextUtils.isEmpty(scannerName)) {
            name = CorUtility.Companion.toTitleCase(scannerName)
        }
        desc!!.setVisibility(GONE)
        val descVal: String
        if (status.equalsIgnoreCase(Constants.HEALTHY)) {
            descVal = name + " (" + mobileNo + ") " + LocalizationUtil.getLocalisedString(this, R.string.low_risk)
            descReason!!.setText(descVal)
            statusContainer!!.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.chat_bubble_green))
        } else if (status.equalsIgnoreCase(Constants.MODERATE)) {
            descVal = name + " (" + mobileNo + ") " + LocalizationUtil.getLocalisedString(this, R.string.moderate_risk)
            descReason!!.setText(descVal)
            statusContainer!!.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.chat_bubble_yellow))
        } else if (status.equalsIgnoreCase(Constants.HIGH)) {
            descVal = name + " (" + mobileNo + ") " + LocalizationUtil.getLocalisedString(this, R.string.high_risk)
            descReason!!.setText(descVal)
            statusContainer!!.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.chat_bubble_orange))
        } else {
            descVal = name + " (" + mobileNo + ") " + LocalizationUtil.getLocalisedString(
                this,
                R.string.tested_positive_status
            )
            descReason!!.setText(descVal)
            statusContainer!!.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.chat_bubble_red))
        }
        descReason!!.setTextColor(ContextCompat.getColorStateList(this, R.color.white))
        statusClose!!.setImageTintList(ContextCompat.getColorStateList(this, R.color.white))
    }

    private fun showExpiredCode() {
        desc!!.setVisibility(VISIBLE)
        descReason!!.setText(LocalizationUtil.getLocalisedString(this, R.string.request_new_code))
        desc!!.setText(LocalizationUtil.getLocalisedString(this, R.string.expired_code))
        desc!!.setTextColor(ContextCompat.getColorStateList(this, R.color.chat_title_orange))
        descReason!!.setTextColor(ContextCompat.getColorStateList(this, R.color.black))
        statusContainer!!.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.chat_bubble_light_orange))
        statusClose!!.setImageTintList(ContextCompat.getColorStateList(this, R.color.chat_close_dark))
    }

    private fun showInvalidStatus() {
        desc!!.setVisibility(VISIBLE)
        descReason!!.setText(LocalizationUtil.getLocalisedString(this, R.string.not_generated_aarogya_setu))
        desc!!.setText(LocalizationUtil.getLocalisedString(this, R.string.invalid_qr_code))
        desc!!.setTextColor(ContextCompat.getColorStateList(this, R.color.chat_title_red))
        descReason!!.setTextColor(ContextCompat.getColorStateList(this, R.color.black))
        statusContainer!!.setBackgroundTintList(null)
        statusClose!!.setImageTintList(ContextCompat.getColorStateList(this, R.color.chat_close_dark))
    }

    private fun showCommonInvalidStatus() {
        desc!!.setVisibility(VISIBLE)
        descReason!!.setText(LocalizationUtil.getLocalisedString(this, R.string.common_scanning_error))
        desc!!.setText(LocalizationUtil.getLocalisedString(this, R.string.invalid_qr_code))
        desc!!.setTextColor(ContextCompat.getColorStateList(this, R.color.chat_title_red))
        descReason!!.setTextColor(ContextCompat.getColorStateList(this, R.color.black))
        statusContainer!!.setBackgroundTintList(null)
        statusClose!!.setImageTintList(ContextCompat.getColorStateList(this, R.color.chat_close_dark))
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var isPermissionGranted = true
        if (grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                val showRationale = shouldShowRequestPermissionRationale(permissions[0])
                if (!showRationale) {
                    isPermissionGranted = false
                    showPermissionAlert()
                } else {
                    isPermissionGranted = false
                }
            }
        } else {
            isPermissionGranted = false
        }
        if (!isPermissionGranted) {
            Toast.makeText(this, getString(R.string.provide_necessary_permission), Toast.LENGTH_LONG).show()
        } else {
            promptContainer!!.setVisibility(VISIBLE)
        }
    }

    private fun showPermissionAlert() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage(getLocalisedString(this, R.string.scan_without_perm_alert))
            .setCancelable(false)
            .setPositiveButton(R.string.open_settings, { dialog, which -> openAppSettings() }
            )
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
        } catch (exception: ActivityNotFoundException) {
        }

    }
}
