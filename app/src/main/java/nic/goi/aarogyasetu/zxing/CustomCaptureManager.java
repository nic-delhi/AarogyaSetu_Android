package nic.goi.aarogyasetu.zxing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import nic.goi.aarogyasetu.utility.Constants;
import nic.goi.aarogyasetu.utility.Logger;

/**
 * @author Niharika.Arora
 */
public class CustomCaptureManager extends CaptureManager {
    private final Activity activity;
    private CaptureViewListener captureViewListener;
    private String TAG = this.getClass().getName();

    interface CaptureViewListener {

        void onResultFetched(String json);
    }

    public CustomCaptureManager(Activity activity, DecoratedBarcodeView barcodeView) {
        super(activity, barcodeView);
        this.activity = activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void returnResult(BarcodeResult rawResult) {
        Intent intent = resultIntent(rawResult, getBarcodeImagePath(rawResult));
        String contents = intent.getStringExtra(Intents.Scan.RESULT);
        if (contents == null) {
            //do nothing
        } else {
            captureViewListener.onResultFetched(rawResult.getText());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getBarcodeImagePath(BarcodeResult rawResult) {
        Bitmap bmp = rawResult.getBitmap();
        try {
            Path bitmapPath = Files.createTempFile(Paths.get(activity.getCacheDir().getAbsolutePath()), Constants.BAR_CODE_PATH, Constants.FILE_EXT);
            try (OutputStream outputStream = Files.newOutputStream(bitmapPath)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                return bitmapPath.toString();
            }
        } catch (IOException e) {
            //throw exception ?
            Logger.e(TAG, "getBarcodeImagePath exception", e);
            return null;
        }
    }

    public void setViewCaptureListener(CaptureViewListener captureViewListener) {
        this.captureViewListener = captureViewListener;
    }
}
