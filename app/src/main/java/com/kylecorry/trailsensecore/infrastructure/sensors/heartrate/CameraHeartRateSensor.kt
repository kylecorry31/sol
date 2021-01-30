package com.kylecorry.trailsensecore.infrastructure.sensors.heartrate

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.Image
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.kylecorry.trailsensecore.domain.math.MovingAverageFilter
import com.kylecorry.trailsensecore.infrastructure.sensors.AbstractSensor
import com.kylecorry.trailsensecore.infrastructure.sensors.SensorChecker
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.Instant
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

class CameraHeartRateSensor(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : IHeartRateSensor, AbstractSensor() {

    private val readings = mutableListOf<Pair<Instant, Float>>()
    private val _heartBeats = mutableListOf<Instant>()
    private val maxReadingInterval = Duration.ofSeconds(10)
    private var filter = MovingAverageFilter(4)
    private var _bpm = 0

    private var startUpTime = Instant.MIN

    private var cameraProvider: ProcessCameraProvider? = null

    override val pulseWave: List<Pair<Instant, Float>>
        get() = readings.toList()

    override val heartBeats: List<Instant>
        get() = _heartBeats.toList()

    override val bpm: Int
        get() = _bpm

    override val hasValidReading: Boolean
        get() = true

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun startImpl() {
        if (!SensorChecker(context).hasCamera()) {
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(200, 200))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), { image ->
                analyzeImage(image)
                image.close()
            })

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            startUpTime = Instant.now()
            val camera =
                cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
            val controls = camera?.cameraControl
            controls?.enableTorch(true)
            controls?.setExposureCompensationIndex(0)
            controls?.cancelFocusAndMetering()

        }, ContextCompat.getMainExecutor(context))
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun analyzeImage(image: ImageProxy) {
        if (Duration.between(startUpTime, Instant.now()) < WARMUP_TIME) {
            return
        }

        val bitmap = image.image?.toBitmap() ?: return
        var averageR = 0f
        val total = bitmap.width * bitmap.height.toFloat()
        for (w in 0 until bitmap.width) {
            for (h in 0 until bitmap.height) {
                averageR += Color.red(bitmap.getPixel(w, h)) / total
            }
        }
        bitmap.recycle()

        if (averageR < 100) {
            return
        }

        if (readings.isEmpty()) {
            filter = MovingAverageFilter(4)
            readings.add(Pair(Instant.now(), averageR))
        } else {
            readings.add(Pair(Instant.now(), filter.filter(averageR.toDouble()).toFloat()))
        }

        while (Duration.between(
                readings.first().first,
                readings.last().first
            ) > maxReadingInterval
        ) {
            readings.removeAt(0)
        }

        calculateHeartRate()
        notifyListeners()
    }

    private fun calculateHeartRate() {
        val dt = Duration.between(readings.first().first, readings.last().first)

        if (dt < Duration.ofSeconds(1) && readings.size > 3) {
            _bpm = 0
            return
        }
        _heartBeats.clear()
        val beats =
            findPeaks(readings, Duration.ofMillis(400)).map { readings[it].first }.toMutableList()
        _heartBeats.addAll(beats)

        val durations = mutableListOf<Duration>()
        for (i in 1 until heartBeats.size) {
            durations.add(Duration.between(heartBeats[i - 1], heartBeats[i]))
        }

        val averageDuration = 1 / (durations.map { it.toMillis() }.average() / 1000 / 60)

        if (!averageDuration.isNaN()) {
            _bpm = averageDuration.roundToInt()
        }
    }

    private fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }


    private fun findPeaks(readings: List<Pair<Instant, Float>>, minDuration: Duration): List<Int> {
        val minHeight = 100
        val maxNum = 30
        val peaks = mutableListOf<Int>()
        // Find peaks above min height
        var i = 1
        var width: Int
        while (i < readings.size - 1) {
            val prev = readings[i - 1]
            val curr = readings[i]
            if (curr.second > minHeight && curr.second > prev.second) {
                width = 1
                while (i + width < readings.size && abs(curr.second - readings[i + width].second) < 0.2) {
                    width++
                }
                i += if (curr.second > readings[min(
                        i + width,
                        readings.size - 1
                    )].second && peaks.size < maxNum
                ) {
                    peaks.add(i)
                    width + 1
                } else {
                    width
                }
            } else {
                i++
            }
        }

        peaks.sortByDescending { readings[it].second }

        val filteredPeaks = mutableListOf<Int>()
        filteredPeaks.addAll(peaks)

        for (k in 0 until peaks.size) {
            for (j in (k + 1) until peaks.size) {
                val dist = Duration.between(readings[peaks[j]].first, readings[peaks[k]].first)
                if (dist.abs() < minDuration) {
                    filteredPeaks.remove(j)
                }
            }
        }

        return filteredPeaks.sortedBy { it }
    }

    override fun stopImpl() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        readings.clear()
        _heartBeats.clear()
    }

    companion object {
        private val WARMUP_TIME = Duration.ofSeconds(3)
    }
}