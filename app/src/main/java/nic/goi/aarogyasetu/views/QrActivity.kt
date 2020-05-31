package nic.goi.aarogyasetu.views


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.text.format.Time
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder

import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.security.SignatureException
import nic.goi.aarogyasetu.CoronaApplication
import nic.goi.aarogyasetu.R
import nic.goi.aarogyasetu.listener.QrCodeListener
import nic.goi.aarogyasetu.listener.QrPublicKeyListener
import nic.goi.aarogyasetu.prefs.SharedPref
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants
import nic.goi.aarogyasetu.utility.AuthUtility
import nic.goi.aarogyasetu.utility.Constants
import nic.goi.aarogyasetu.utility.CorUtility
import nic.goi.aarogyasetu.utility.DecryptionUtil
import nic.goi.aarogyasetu.utility.LocalizationUtil
import nic.goi.aarogyasetu.utility.Logger
import nic.goi.aarogyasetu.zxing.CustomScannerActivity

/**
 * QrActivity for generating user's Qr code
 *
 * @author Niharika.Arora
 */
class QrActivity : AppCompatActivity(), QrCodeListener, QrPublicKeyListener {

    private var qrCodeView: ImageView? = null
    private var progress: ProgressBar? = null
    private var nestedView: View? = null
    private var qrExpiryView: TextView? = null
    private var phoneView: TextView? = null
    private var nameView: TextView? = null
    private var qrTapToRefresh: TextView? = null
    private var scanBtn: TextView? = null
    private var refreshView: TextView? = null
    private val COUNT_DOWN_INTERVAL_MILLISECONDS = 1000
    private val SECONDS_PER_DAY = 86400
    private val SECONDS_PER_HOUR = 3600
    private val SECONDS_PER_MINUTE = 60
    private var timer: CountDownTimer? = null
    private var bitMatrix: BitMatrix? = null
    private var isPublicKeyToBeFetched = false

    @Override
    protected fun onCreate(@Nullable savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        configureView()
        configureClicks()
        checkQrStatus()
    }

    private fun configureView() {
        qrCodeView = findViewById(R.id.scan_code)
        progress = findViewById(R.id.progress)
        qrExpiryView = findViewById(R.id.expiry_time)
        phoneView = findViewById(R.id.phone)
        nameView = findViewById(R.id.name)
        nestedView = findViewById(R.id.nested_view)
        qrTapToRefresh = findViewById(R.id.tap_to_refresh)
        refreshView = findViewById(R.id.refresh_view)
        qrTapToRefresh!!.setText(LocalizationUtil.getLocalisedString(this, R.string.tap_to_refresh))
        val scanTextDescription = findViewById(R.id.scan_text_desc)
        scanTextDescription.setText(LocalizationUtil.getLocalisedString(this, R.string.scan_to_check_status))
        val expiryDescription = findViewById(R.id.expiry_desc)
        expiryDescription.setText(LocalizationUtil.getLocalisedString(this, R.string.qr_code_valid_for))
        scanBtn = findViewById(R.id.scan_btn)
        scanBtn!!.setText(LocalizationUtil.getLocalisedString(this, R.string.scan_other_s_qr_code))
        refreshView!!.setText(LocalizationUtil.getLocalisedString(this, R.string.refresh))

    }

    private fun configureClicks() {
        onDoneClick()
        onScanClick()
        onRefreshClick()
    }

    private fun fetchQrCode() {
        if (CorUtility.isNetworkAvailable(this)) {
            SharedPref.setStringParams(CoronaApplication.instance, SharedPrefsConstants.QR_TEXT, Constants.EMPTY)
            nestedView!!.setVisibility(View.GONE)
            progress!!.setVisibility(View.VISIBLE)
            configureQr()
        } else {
            Toast.makeText(
                this,
                LocalizationUtil.getLocalisedString(
                    this,
                    R.string.make_sure_your_phone_is_connected_to_the_wifi_or_switch_to_mobile_data
                ),
                Toast.LENGTH_LONG
            ).show()
            showQrFailureView()
        }
    }

    private fun configureQr() {
        if (isPublicKeyToBeFetched) {
            CorUtility.Companion.fetchQrPublicKey(this)
        } else {
            CorUtility.Companion.fetchQrCodeText(this)
        }
    }

    private fun showQrFailureView() {
        nestedView!!.setVisibility(View.VISIBLE)
        findViewById(R.id.scan_validity_container).setVisibility(View.GONE)
        SharedPref.setStringParams(CoronaApplication.instance, SharedPrefsConstants.QR_TEXT, Constants.EMPTY)
        findViewById(R.id.refresh_container).setVisibility(View.VISIBLE)
        qrCodeView!!.setAlpha(0.1f)
    }

    private fun onRefreshClick() {
        findViewById(R.id.refresh_container).setOnClickListener({ v -> fetchQrCode() })
        findViewById(R.id.refresh_icon).setOnClickListener({ v -> fetchQrCode() })
        refreshView!!.setOnClickListener({ v -> fetchQrCode() })
        qrTapToRefresh!!.setOnClickListener({ v -> fetchQrCode() })
    }

    private fun onScanClick() {
        scanBtn!!.setOnClickListener({ v ->
            startActivity(Intent(this@QrActivity, CustomScannerActivity::class.java))
            finish()
        })
    }

    private fun onDoneClick() {
        findViewById(R.id.close).setOnClickListener({ v -> finish() })
    }

    @Override
    fun onQrCodeFetched(text: String) {
        Logger.d(Constants.QR_SCREEN_TAG, "ON qr fetched ")
        showViews()
        var claimsJws: Jws<Claims>? = null
        try {
            claimsJws = DecryptionUtil.decryptFile(text)
        } catch (e: InvalidKeySpecException) {
            isPublicKeyToBeFetched = true
        } catch (e: SignatureException) {
            isPublicKeyToBeFetched = true
        } catch (e: NoSuchAlgorithmException) {
            Logger.d(Constants.QR_SCREEN_TAG, e.getMessage())
        } catch (e: JwtException) {
            Logger.d(Constants.QR_SCREEN_TAG, e.getMessage())
        }

        Logger.d(Constants.QR_SCREEN_TAG, "Decryption end")
        if (claimsJws == null) {
            onFailure()
        } else {
            configureScreen(text, claimsJws!!)
        }
    }

    private fun showViews() {
        nestedView!!.setVisibility(View.VISIBLE)
        progress!!.setVisibility(View.GONE)
        findViewById(R.id.scan_validity_container).setVisibility(View.VISIBLE)
        findViewById(R.id.refresh_container).setVisibility(View.GONE)
        qrCodeView!!.setAlpha(1f)
    }

    private fun configureScreen(text: String, claimsJws: Jws<Claims>) {
        val body = claimsJws.getBody()
        val expiry = body.get(Constants.EXPIRY, Long::class.java)
        val name = body.get(Constants.NAME, String::class.java)
        val mobileNo = body.get(Constants.MOBILE, String::class.java)
        SharedPref.setStringParams(CoronaApplication.instance, SharedPrefsConstants.QR_TEXT, text)
        AuthUtility.setUserName(name)
        configureTextViews(name, mobileNo)
        startTimer(expiry)
        setImage()
    }

    private fun configureTextViews(name: String, mobileNo: String) {
        if (!TextUtils.isEmpty(mobileNo)) {
            phoneView!!.setText(mobileNo)
        }
        if (!TextUtils.isEmpty(name)) {
            nameView!!.setText(CorUtility.Companion.toTitleCase(name))
        }
    }

    private fun setImage() {
        Logger.d(Constants.QR_SCREEN_TAG, "Image write start")
        val qrText =
            SharedPref.getStringParams(CoronaApplication.getInstance(), SharedPrefsConstants.QR_TEXT, Constants.EMPTY)
        val multiFormatWriter = MultiFormatWriter()
        try {
            if (bitMatrix != null) {
                bitMatrix!!.clear()
            }
            bitMatrix = multiFormatWriter.encode(qrText, BarcodeFormat.QR_CODE, 440, 440)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            qrCodeView!!.setImageBitmap(bitmap)
            Logger.d(Constants.QR_SCREEN_TAG, "Image write end ")
        } catch (e: WriterException) {
            //do nothing
        }

    }

    private fun startTimer(expiry: Long) {
        val millisecondsMultiplier = 1000L
        val countDownMilliSeconds = expiry * millisecondsMultiplier
        val nowTime = Time(Time.getCurrentTimezone())
        nowTime.setToNow()
        nowTime.normalize(true)
        val currentTimeMilliSeconds = nowTime.toMillis(true)
        val milliDiff = countDownMilliSeconds - currentTimeMilliSeconds
        if (timer != null) {
            timer!!.cancel()
        }
        timer = object : CountDownTimer(milliDiff, COUNT_DOWN_INTERVAL_MILLISECONDS) {

            fun onTick(millisUntilFinished: Long) {
                val days = (millisUntilFinished / COUNT_DOWN_INTERVAL_MILLISECONDS / SECONDS_PER_DAY).toInt()
                val hours =
                    ((millisUntilFinished / COUNT_DOWN_INTERVAL_MILLISECONDS - days * SECONDS_PER_DAY) / SECONDS_PER_HOUR).toInt()
                val minutes =
                    ((millisUntilFinished / COUNT_DOWN_INTERVAL_MILLISECONDS - (days * SECONDS_PER_DAY + hours * SECONDS_PER_HOUR)) / SECONDS_PER_MINUTE).toInt()
                val seconds = (millisUntilFinished / COUNT_DOWN_INTERVAL_MILLISECONDS % SECONDS_PER_MINUTE).toInt()
                configureExpiryTime(minutes, seconds)
            }

            fun onFinish() {
                showQrFailureView()
            }

        }.start()
    }

    private fun configureExpiryTime(minutes: Int, seconds: Int) {
        val expiryTme: String
        if (minutes < 1) {
            qrExpiryView!!.setText(LocalizationUtil.getLocalisedString(this, R.string.few_seconds))
        } else if (seconds < 1) {
            expiryTme = minutes + Constants.SPACE + LocalizationUtil.getLocalisedString(this, R.string.minutes)
            qrExpiryView!!.setText(expiryTme)
        } else {
            expiryTme = minutes + Constants.SPACE + LocalizationUtil.getLocalisedString(
                this,
                R.string.minutes
            ) + Constants.SPACE + seconds + Constants.SPACE + LocalizationUtil.getLocalisedString(
                this,
                R.string.seconds
            )
            qrExpiryView!!.setText(expiryTme)
        }
    }

    @Override
    protected fun onDestroy() {
        super.onDestroy()
        if (timer != null) {
            timer!!.cancel()
        }
    }

    //Show Refresh view when qr code generation failed/qr code expire
    fun onFailure() {
        progress!!.setVisibility(View.GONE)
        showQrFailureView()
        SharedPref.setStringParams(CoronaApplication.instance, SharedPrefsConstants.QR_TEXT, Constants.EMPTY)
    }

    //Method to check Qr status validity or other issues,if valid show Qr else fetch the Qr and show to the user
    private fun checkQrStatus() {
        var claimsJws: Jws<Claims>? = null
        val qrText =
            SharedPref.getStringParams(CoronaApplication.getInstance(), SharedPrefsConstants.QR_TEXT, Constants.EMPTY)
        if (!TextUtils.isEmpty(qrText)) {
            try {
                claimsJws = DecryptionUtil.decryptFile(qrText)
            } catch (e: NoSuchAlgorithmException) {
                //do nothing
            } catch (e: InvalidKeySpecException) {
            } catch (e: JwtException) {
            }

            if (claimsJws != null) {
                val qrExpiry = claimsJws!!.getBody().get(Constants.EXPIRY, Long::class.java)
                val millisecondsMultiplier = 1000L
                val countDownMilliSeconds = qrExpiry * millisecondsMultiplier
                if (qrExpiry > 0 && System.currentTimeMillis() - countDownMilliSeconds < 0) {
                    nestedView!!.setVisibility(View.VISIBLE)
                    configureScreen(qrText, claimsJws!!)
                } else {
                    onFailure()
                }
            } else {
                fetchQrCode()
            }
        } else {
            fetchQrCode()
        }
    }

    @Override
    fun onQrPublicKeyFetched() {
        isPublicKeyToBeFetched = false
        CorUtility.Companion.fetchQrCodeText(this)
    }

    @Override
    fun onPublicKeyFetchFailure() {
        onFailure()
    }

    companion object {

        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, QrActivity::class.java))
        }
    }
}
