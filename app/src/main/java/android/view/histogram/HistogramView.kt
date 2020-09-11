package android.view.histogram

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class HistogramView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val intensityPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 2.0f
    }

    private val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 2.0f
    }

    private val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 2.0f
    }

    private val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 2.0f
    }

    var intensityPixelHistogram = true
        set(value) {
            field = value
            invalidate()
        }
    var redPixelHistogram = true
        set(value) {
            field = value
            invalidate()
        }
    var greenPixelHistogram = true
        set(value) {
            field = value
            invalidate()
        }
    var bluePixelHistogram = true
        set(value) {
            field = value
            invalidate()
        }

    var fillHistogram = false
        set(value) {
            field = value
            if (fillHistogram) {
                intensityPaint.style = Paint.Style.FILL
                redPaint.style = Paint.Style.FILL
                greenPaint.style = Paint.Style.FILL
                bluePaint.style = Paint.Style.FILL
            } else {
                intensityPaint.style = Paint.Style.STROKE
                redPaint.style = Paint.Style.STROKE
                greenPaint.style = Paint.Style.STROKE
                bluePaint.style = Paint.Style.STROKE
            }
            invalidate()
        }

    private val intensityPath = Path()
    private val redPath = Path()
    private val greenPath = Path()
    private val bluePath = Path()

    private var bitmapImage: Bitmap? = null

    private var intensityPixels = IntArray(256)
    private var redPixels = IntArray(256)
    private var greenPixels = IntArray(256)
    private var bluePixels = IntArray(256)

    init {
        processImage()
    }

    fun setBitmap(bitmap: Bitmap) {
        bitmapImage = bitmap
        processImage()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        generatePath()
    }

    private fun processImage() {
        bitmapImage?.let {
            val pixels = IntArray(it.width * it.height)
            it.getPixels(
                pixels,
                0,
                it.width,
                0,
                0,
                it.width,
                it.height
            )
            for (i in 0 until 256) {
                intensityPixels[i] = 0
                redPixels[i] = 0
                greenPixels[i] = 0
                bluePixels[i] = 0
            }
            for (i in pixels.indices) {
                var luma = ((0.299f * Color.red(pixels[i])) +
                        (0.597f * Color.green(pixels[i])) +
                        (0.114 * Color.blue(pixels[i]))).toInt()
                if (luma > 255) {
                    luma = 255
                }
                intensityPixels[luma]++
                redPixels[Color.red(pixels[i])]++
                greenPixels[Color.green(pixels[i])]++
                bluePixels[Color.blue(pixels[i])]++
            }
            generatePath()
        }
    }

    private fun generatePath() {
        if (width == 0 || height == 0) {
            return
        }
        if (bitmapImage == null) {
            return
        }
        intensityPath.reset()
        redPath.reset()
        greenPath.reset()
        bluePath.reset()
        intensityPath.moveTo(0.0f, height.toFloat())
        redPath.moveTo(0.0f, height.toFloat())
        greenPath.moveTo(0.0f, height.toFloat())
        bluePath.moveTo(0.0f, height.toFloat())
        var pathX = width / 255.0f
        val maxIntensityPixelCount = intensityPixels.maxOrNull() ?: 0
        val maxRedPixelCount = redPixels.maxOrNull() ?: 0
        val maxGreenPixelCount = greenPixels.maxOrNull() ?: 0
        val maxBluePixelCount = bluePixels.maxOrNull() ?: 0
        val maxPixels = max(
            max(maxIntensityPixelCount, maxRedPixelCount),
            max(maxGreenPixelCount, maxBluePixelCount)
        )
        val heightAr = maxPixels.toFloat() / height.toFloat()
        for (i in 0 until 256) {
            intensityPath.lineTo(pathX, height - intensityPixels[i] / heightAr)
            redPath.lineTo(pathX, height - redPixels[i] / heightAr)
            greenPath.lineTo(pathX, height - greenPixels[i] / heightAr)
            bluePath.lineTo(pathX, height - bluePixels[i] / heightAr)
            pathX += width / 256.0f
        }
        intensityPath.lineTo(width.toFloat(), height.toFloat())
        redPath.lineTo(width.toFloat(), height.toFloat())
        greenPath.lineTo(width.toFloat(), height.toFloat())
        bluePath.lineTo(width.toFloat(), height.toFloat())
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            if (intensityPixelHistogram) {
                it.drawPath(intensityPath, intensityPaint)
            }
            if (redPixelHistogram) {
                it.drawPath(redPath, redPaint)
            }
            if (greenPixelHistogram) {
                it.drawPath(greenPath, greenPaint)
            }
            if (bluePixelHistogram) {
                it.drawPath(bluePath, bluePaint)
            }
        }
    }
}