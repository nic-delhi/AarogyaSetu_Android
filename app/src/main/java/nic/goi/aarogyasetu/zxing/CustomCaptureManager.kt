package nic.goi.aarogyasetu.zxing

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap

import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

import nic.goi.aarogyasetu.utility.Constants

/**
 * @author Niharika.Arora
 */
class CustomCaptureManager(private val activity: Activity, barcodeView: DecoratedBarcodeView) :
    CaptureManager(activity, barcodeView) {
    private var captureViewListener: CaptureViewListener? = null

    internal interface CaptureViewListener {

        fun onResultFetched(json: String)
    }

    @Override
    protected fun returnResult(rawResult: BarcodeResult) {
        val intent = resultIntent(rawResult, getBarcodeImagePath(rawResult))
        val contents = intent.getStringExtra(Intents.Scan.RESULT)
        if (contents == null) {
            //do nothing
        } else {
            captureViewListener!!.onResultFetched(rawResult.getText())
        }

    }

    private fun getBarcodeImagePath(rawResult: BarcodeResult): String? {
        var barcodeImagePath: String? = null
        val bmp = rawResult.getBitmap()
        try {
            val bitmapFile = File.createTempFile(Constants.BAR_CODE_PATH, Constants.FILE_EXT, activity.getCacheDir())
            val outputStream = FileOutputStream(bitmapFile)
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            barcodeImagePath = bitmapFile.getAbsolutePath()
        } catch (e: IOException) {
        }

        return barcodeImagePath
    }

    fun setViewCaptureListener(captureViewListener: CaptureViewListener) {
        this.captureViewListener = captureViewListener
    }
}
