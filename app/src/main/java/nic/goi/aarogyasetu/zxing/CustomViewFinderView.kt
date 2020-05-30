package nic.goi.aarogyasetu.zxing


import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet

import com.google.zxing.ResultPoint
import com.google.zxing.client.android.R
import com.journeyapps.barcodescanner.CameraPreview
import com.journeyapps.barcodescanner.ViewfinderView

import java.util.ArrayList

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author Niharika
 */
class CustomViewFinderView// This constructor is used when the class is built from an XML resource.
    (context: Context, attrs: AttributeSet) : ViewfinderView(context, attrs) {

    protected val paint: Paint
    protected var resultBitmap: Bitmap? = null
    protected var maskColor: Int = 0
    protected val resultColor: Int
    protected val laserColor: Int
    protected val resultPointColor: Int
    protected var scannerAlpha: Int = 0
    protected var possibleResultPoints: List<ResultPoint>
    protected var lastPossibleResultPoints: List<ResultPoint>? = null
    protected var cameraPreview: CameraPreview? = null

    // Cache the framingRect and previewFramingRect, so that we can still draw it after the preview
    // stopped.
    protected var framingRect: Rect? = null
    protected var previewFramingRect: Rect? = null

    init {

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.setColor(getResources().getColor(android.R.color.white))

        val resources = getResources()
        // Get setted attributes on view
        val attributes = getContext().obtainStyledAttributes(attrs, R.styleable.zxing_finder)

        this.maskColor = attributes.getColor(
            R.styleable.zxing_finder_zxing_viewfinder_mask,
            resources.getColor(R.color.zxing_viewfinder_mask)
        )
        this.resultColor = attributes.getColor(
            R.styleable.zxing_finder_zxing_result_view,
            resources.getColor(R.color.zxing_result_view)
        )
        this.laserColor = attributes.getColor(
            R.styleable.zxing_finder_zxing_viewfinder_laser,
            resources.getColor(R.color.zxing_transparent)
        )
        this.resultPointColor = attributes.getColor(
            R.styleable.zxing_finder_zxing_possible_result_points,
            resources.getColor(R.color.zxing_possible_result_points)
        )

        attributes.recycle()

        scannerAlpha = 0
        possibleResultPoints = ArrayList(5)
        lastPossibleResultPoints = null
    }

    fun setCameraPreview(view: CameraPreview) {
        this.cameraPreview = view
        view.addStateListener(object : CameraPreview.StateListener() {
            @Override
            fun previewSized() {
                refreshSizes()
                invalidate()
            }

            @Override
            fun previewStarted() {

            }

            @Override
            fun previewStopped() {

            }

            @Override
            fun cameraError(error: Exception) {

            }
        })
    }

    protected fun refreshSizes() {
        if (cameraPreview == null) {
            return
        }
        val framingRect = cameraPreview!!.getFramingRect()
        val previewFramingRect = cameraPreview!!.getPreviewFramingRect()
        if (framingRect != null && previewFramingRect != null) {
            this.framingRect = framingRect
            this.previewFramingRect = previewFramingRect
        }
    }


    @SuppressLint("DrawAllocation")
    @Override
    fun onDraw(canvas: Canvas) {
        refreshSizes()
        if (framingRect == null || previewFramingRect == null) {
            return
        }

        val frame = framingRect
        val previewFrame = previewFramingRect

        //inside onDraw
        val distance = (frame!!.bottom - frame!!.top) / 4
        val thickness = 15

        //top left corner
        canvas.drawRect(frame!!.left - thickness, frame!!.top - thickness, distance + frame!!.left, frame!!.top, paint)
        canvas.drawRect(frame!!.left - thickness, frame!!.top, frame!!.left, distance + frame!!.top, paint)

        //top right corner
        canvas.drawRect(
            frame!!.right - distance,
            frame!!.top - thickness,
            frame!!.right + thickness,
            frame!!.top,
            paint
        )
        canvas.drawRect(frame!!.right, frame!!.top, frame!!.right + thickness, distance + frame!!.top, paint)

        //bottom left corner
        canvas.drawRect(
            frame!!.left - thickness,
            frame!!.bottom,
            distance + frame!!.left,
            frame!!.bottom + thickness,
            paint
        )
        canvas.drawRect(frame!!.left - thickness, frame!!.bottom - distance, frame!!.left, frame!!.bottom, paint)

        //bottom right corner
        canvas.drawRect(
            frame!!.right - distance,
            frame!!.bottom,
            frame!!.right + thickness,
            frame!!.bottom + thickness,
            paint
        )
        canvas.drawRect(frame!!.right, frame!!.bottom - distance, frame!!.right + thickness, frame!!.bottom, paint)


        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY)
            canvas.drawBitmap(resultBitmap, null, frame, paint)
        } else {

            val scaleX = frame!!.width() / previewFrame!!.width() as Float
            val scaleY = frame!!.height() / previewFrame!!.height() as Float

            val currentPossible = possibleResultPoints
            val currentLast = lastPossibleResultPoints
            val frameLeft = frame!!.left
            val frameTop = frame!!.top
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null
            } else {
                possibleResultPoints = ArrayList(5)
                lastPossibleResultPoints = currentPossible
                paint.setAlpha(CURRENT_POINT_OPACITY)
                paint.setColor(resultPointColor)
                for (point in currentPossible) {
                    canvas.drawCircle(
                        frameLeft + (point.getX() * scaleX) as Int,
                        frameTop + (point.getY() * scaleY) as Int,
                        POINT_SIZE, paint
                    )
                }
            }
            if (currentLast != null) {
                paint.setAlpha(CURRENT_POINT_OPACITY / 2)
                paint.setColor(resultPointColor)
                val radius = POINT_SIZE / 2.0f
                for (point in currentLast) {
                    canvas.drawCircle(
                        frameLeft + (point.getX() * scaleX) as Int,
                        frameTop + (point.getY() * scaleY) as Int,
                        radius, paint
                    )
                }
            }

            // Request another update at the animation interval, but only repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(
                ANIMATION_DELAY,
                frame!!.left - POINT_SIZE,
                frame!!.top - POINT_SIZE,
                frame!!.right + POINT_SIZE,
                frame!!.bottom + POINT_SIZE
            )
        }
    }

    fun drawViewfinder() {
        val resultBitmap = this.resultBitmap
        this.resultBitmap = null
        if (resultBitmap != null) {
            resultBitmap!!.recycle()
        }
        invalidate()
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param result An image of the result.
     */
    fun drawResultBitmap(result: Bitmap) {
        resultBitmap = result
        invalidate()
    }

    /**
     * Only call from the UI thread.
     *
     * @param point a point to draw, relative to the preview frame
     */
    fun addPossibleResultPoint(point: ResultPoint) {
        val points = possibleResultPoints
        points.add(point)
        val size = points.size()
        if (size > MAX_RESULT_POINTS) {
            // trim it
            points.subList(0, size - MAX_RESULT_POINTS / 2).clear()
        }
    }

    companion object {
        protected val TAG = CustomViewFinderView::class.java!!.getSimpleName()

        protected val SCANNER_ALPHA = intArrayOf(0, 64, 128, 192, 255, 192, 128, 64)
        protected val ANIMATION_DELAY = 80L
        protected val CURRENT_POINT_OPACITY = 0xA0
        protected val MAX_RESULT_POINTS = 20
        protected val POINT_SIZE = 6
    }
}
