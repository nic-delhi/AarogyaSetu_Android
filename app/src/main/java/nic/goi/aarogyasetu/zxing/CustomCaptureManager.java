/*
 * Copyright 2020 Government of India
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package nic.goi.aarogyasetu.zxing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import nic.goi.aarogyasetu.utility.Constants;

/**
 * @author Niharika.Arora
 */
public class CustomCaptureManager extends CaptureManager {
    private final Activity activity;
    private CaptureViewListener captureViewListener;

    interface CaptureViewListener {

        void onResultFetched(String json);
    }

    public CustomCaptureManager(Activity activity, DecoratedBarcodeView barcodeView) {
        super(activity, barcodeView);
        this.activity = activity;
    }

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

    private String getBarcodeImagePath(BarcodeResult rawResult) {
        String barcodeImagePath = null;
        Bitmap bmp = rawResult.getBitmap();
        try {
            File bitmapFile = File.createTempFile(Constants.BAR_CODE_PATH, Constants.FILE_EXT, activity.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(bitmapFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            barcodeImagePath = bitmapFile.getAbsolutePath();
        } catch (IOException e) {
        }
        return barcodeImagePath;
    }

    public void setViewCaptureListener(CaptureViewListener captureViewListener) {
        this.captureViewListener = captureViewListener;
    }
}
