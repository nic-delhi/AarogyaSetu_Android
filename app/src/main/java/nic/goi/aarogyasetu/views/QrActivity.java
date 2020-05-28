package nic.goi.aarogyasetu.views;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.encoder.QRCode;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import nic.goi.aarogyasetu.CoronaApplication;
import nic.goi.aarogyasetu.R;
import nic.goi.aarogyasetu.listener.QrCodeListener;
import nic.goi.aarogyasetu.listener.QrPublicKeyListener;
import nic.goi.aarogyasetu.prefs.SharedPref;
import nic.goi.aarogyasetu.prefs.SharedPrefsConstants;
import nic.goi.aarogyasetu.utility.AuthUtility;
import nic.goi.aarogyasetu.utility.Constants;
import nic.goi.aarogyasetu.utility.CorUtility;
import nic.goi.aarogyasetu.utility.DecryptionUtil;
import nic.goi.aarogyasetu.utility.LocalizationUtil;
import nic.goi.aarogyasetu.utility.Logger;
import nic.goi.aarogyasetu.zxing.CustomScannerActivity;

/**
 * QrActivity for generating user's Qr code
 *
 * @author Niharika.Arora
 */
public class QrActivity extends AppCompatActivity implements QrCodeListener, QrPublicKeyListener {

    private ImageView qrCodeView;
    private ProgressBar progress;
    private View nestedView;
    private TextView qrExpiryView, phoneView, nameView, qrTapToRefresh, scanBtn, refreshView;
    private final int COUNT_DOWN_INTERVAL_MILLISECONDS = 1000;
    private final int SECONDS_PER_DAY = 86400;
    private final int SECONDS_PER_HOUR = 3600;
    private final int SECONDS_PER_MINUTE = 60;
    private CountDownTimer timer;
    private BitMatrix bitMatrix;
    private boolean isPublicKeyToBeFetched = false;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, QrActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        configureView();
        configureClicks();
        checkQrStatus();
    }

    private void configureView() {
        qrCodeView = findViewById(R.id.scan_code);
        progress = findViewById(R.id.progress);
        qrExpiryView = findViewById(R.id.expiry_time);
        phoneView = findViewById(R.id.phone);
        nameView = findViewById(R.id.name);
        nestedView = findViewById(R.id.nested_view);
        qrTapToRefresh = findViewById(R.id.tap_to_refresh);
        refreshView = findViewById(R.id.refresh_view);
        qrTapToRefresh.setText(LocalizationUtil.getLocalisedString(this, R.string.tap_to_refresh));
        TextView scanTextDescription = findViewById(R.id.scan_text_desc);
        scanTextDescription.setText(LocalizationUtil.getLocalisedString(this, R.string.scan_to_check_status));
        TextView expiryDescription = findViewById(R.id.expiry_desc);
        expiryDescription.setText(LocalizationUtil.getLocalisedString(this, R.string.qr_code_valid_for));
        scanBtn = findViewById(R.id.scan_btn);
        scanBtn.setText(LocalizationUtil.getLocalisedString(this, R.string.scan_other_s_qr_code));
        refreshView.setText(LocalizationUtil.getLocalisedString(this, R.string.refresh));

    }

    private void configureClicks() {
        onDoneClick();
        onScanClick();
        onRefreshClick();
    }

    private void fetchQrCode() {
        if (CorUtility.isNetworkAvailable(this)) {
            SharedPref.setStringParams(CoronaApplication.instance, SharedPrefsConstants.QR_TEXT, Constants.EMPTY);
            nestedView.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            configureQr();
        } else {
            Toast.makeText(this, LocalizationUtil.getLocalisedString(this, R.string.make_sure_your_phone_is_connected_to_the_wifi_or_switch_to_mobile_data), Toast.LENGTH_LONG).show();
            showQrFailureView();
        }
    }

    private void configureQr() {
        if (isPublicKeyToBeFetched) {
            CorUtility.Companion.fetchQrPublicKey(this);
        } else {
            CorUtility.Companion.fetchQrCodeText(this);
        }
    }

    private void showQrFailureView() {
        nestedView.setVisibility(View.VISIBLE);
        findViewById(R.id.scan_validity_container).setVisibility(View.GONE);
        SharedPref.setStringParams(CoronaApplication.instance, SharedPrefsConstants.QR_TEXT, Constants.EMPTY);
        findViewById(R.id.refresh_container).setVisibility(View.VISIBLE);
        qrCodeView.setAlpha(0.1f);
    }

    private void onRefreshClick() {
        findViewById(R.id.refresh_container).setOnClickListener(v -> fetchQrCode());
        findViewById(R.id.refresh_icon).setOnClickListener(v -> fetchQrCode());
        refreshView.setOnClickListener(v -> fetchQrCode());
        qrTapToRefresh.setOnClickListener(v -> fetchQrCode());
    }

    private void onScanClick() {
        scanBtn.setOnClickListener(v -> {
            startActivity(new Intent(QrActivity.this, CustomScannerActivity.class));
            finish();
        });
    }

    private void onDoneClick() {
        findViewById(R.id.close).setOnClickListener(v -> finish());
    }

    @Override
    public void onQrCodeFetched(String text) {
        Logger.d(Constants.QR_SCREEN_TAG, "ON qr fetched ");
        showViews();
        Jws<Claims> claimsJws = null;
        try {
            claimsJws = DecryptionUtil.decryptFile(text);
        } catch (InvalidKeySpecException | SignatureException e) {
            isPublicKeyToBeFetched = true;
        } catch (NoSuchAlgorithmException | JwtException e) {
            Logger.d(Constants.QR_SCREEN_TAG, e.getMessage());
        }
        Logger.d(Constants.QR_SCREEN_TAG, "Decryption end");
        if (claimsJws == null) {
            onFailure();
        } else {
            configureScreen(text, claimsJws);
        }
    }

    private void showViews() {
        nestedView.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        findViewById(R.id.scan_validity_container).setVisibility(View.VISIBLE);
        findViewById(R.id.refresh_container).setVisibility(View.GONE);
        qrCodeView.setAlpha(1f);
    }

    private void configureScreen(String text, Jws<Claims> claimsJws) {
        Claims body = claimsJws.getBody();
        long expiry = body.get(Constants.EXPIRY, Long.class);
        String name = body.get(Constants.NAME, String.class);
        String mobileNo = body.get(Constants.MOBILE, String.class);
        SharedPref.setStringParams(CoronaApplication.instance, SharedPrefsConstants.QR_TEXT, text);
        AuthUtility.setUserName(name);
        configureTextViews(name, mobileNo);
        startTimer(expiry);
        setImage();
    }

    private void configureTextViews(String name, String mobileNo) {
        if (!TextUtils.isEmpty(mobileNo)) {
            phoneView.setText(mobileNo);
        }
        if (!TextUtils.isEmpty(name)) {
            nameView.setText(CorUtility.Companion.toTitleCase(name));
        }
    }

    private void setImage() {
        Logger.d(Constants.QR_SCREEN_TAG, "Image write start");
        String qrText = SharedPref.getStringParams(CoronaApplication.getInstance(), SharedPrefsConstants.QR_TEXT, Constants.EMPTY);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            if (bitMatrix != null) {
                bitMatrix.clear();
            }
            bitMatrix = multiFormatWriter.encode(qrText, BarcodeFormat.QR_CODE, convertDpToPixel(250), convertDpToPixel(250));
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrCodeView.setImageBitmap(bitmap);
            Logger.d(Constants.QR_SCREEN_TAG, "Image write end ");
        } catch (WriterException e) {
            //do nothing
        }
    }

    private void startTimer(long expiry) {
        final long millisecondsMultiplier = 1000L;
        long countDownMilliSeconds = expiry * millisecondsMultiplier;
        Time nowTime = new Time(Time.getCurrentTimezone());
        nowTime.setToNow();
        nowTime.normalize(true);
        long currentTimeMilliSeconds = nowTime.toMillis(true);
        long milliDiff = countDownMilliSeconds - currentTimeMilliSeconds;
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(milliDiff, COUNT_DOWN_INTERVAL_MILLISECONDS) {

            public void onTick(long millisUntilFinished) {
                int days = (int) ((millisUntilFinished / COUNT_DOWN_INTERVAL_MILLISECONDS) / SECONDS_PER_DAY);
                int hours = (int) (((millisUntilFinished / COUNT_DOWN_INTERVAL_MILLISECONDS) - (days * SECONDS_PER_DAY)) / SECONDS_PER_HOUR);
                int minutes = (int) (((millisUntilFinished / COUNT_DOWN_INTERVAL_MILLISECONDS) - ((days * SECONDS_PER_DAY) + (hours * SECONDS_PER_HOUR))) / SECONDS_PER_MINUTE);
                int seconds = (int) ((millisUntilFinished / COUNT_DOWN_INTERVAL_MILLISECONDS) % SECONDS_PER_MINUTE);
                configureExpiryTime(minutes, seconds);
            }

            public void onFinish() {
                showQrFailureView();
            }

        }.start();
    }

    private void configureExpiryTime(int minutes, int seconds) {
        String expiryTme;
        if (minutes < 1) {
            qrExpiryView.setText(LocalizationUtil.getLocalisedString(this, R.string.few_seconds));
        } else if (seconds < 1) {
            expiryTme = minutes + Constants.SPACE + LocalizationUtil.getLocalisedString(this, R.string.minutes);
            qrExpiryView.setText(expiryTme);
        } else {
            expiryTme = minutes + Constants.SPACE + LocalizationUtil.getLocalisedString(this, R.string.minutes) + Constants.SPACE + seconds + Constants.SPACE + LocalizationUtil.getLocalisedString(this, R.string.seconds);
            qrExpiryView.setText(expiryTme);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    private  int convertDpToPixel(float dp){
        return (int)(dp * ((float) getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    //Show Refresh view when qr code generation failed/qr code expire
    public void onFailure() {
        progress.setVisibility(View.GONE);
        showQrFailureView();
        SharedPref.setStringParams(CoronaApplication.instance, SharedPrefsConstants.QR_TEXT, Constants.EMPTY);
    }

    //Method to check Qr status validity or other issues,if valid show Qr else fetch the Qr and show to the user
    private void checkQrStatus() {
        Jws<Claims> claimsJws = null;
        String qrText = SharedPref.getStringParams(CoronaApplication.getInstance(), SharedPrefsConstants.QR_TEXT, Constants.EMPTY);
        if (!TextUtils.isEmpty(qrText)) {
            try {
                claimsJws = DecryptionUtil.decryptFile(qrText);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | JwtException e) {
                //do nothing
            }
            if (claimsJws != null) {
                long qrExpiry = claimsJws.getBody().get(Constants.EXPIRY, Long.class);
                final long millisecondsMultiplier = 1000L;
                long countDownMilliSeconds = qrExpiry * millisecondsMultiplier;
                if (qrExpiry > 0 && System.currentTimeMillis() - countDownMilliSeconds < 0) {
                    nestedView.setVisibility(View.VISIBLE);
                    configureScreen(qrText, claimsJws);
                } else {
                    onFailure();
                }
            } else {
                fetchQrCode();
            }
        } else {
            fetchQrCode();
        }
    }

    @Override
    public void onQrPublicKeyFetched() {
        isPublicKeyToBeFetched = false;
        CorUtility.Companion.fetchQrCodeText(this);
    }

    @Override
    public void onPublicKeyFetchFailure() {
        onFailure();
    }


}
