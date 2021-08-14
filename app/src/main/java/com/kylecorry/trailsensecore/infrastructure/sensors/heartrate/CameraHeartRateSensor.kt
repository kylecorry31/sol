package com.kylecorry.trailsensecore.infrastructure.sensors.heartrate

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Size
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LifecycleOwner
import com.kylecorry.andromeda.camera.Camera
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.trailsensecore.domain.math.MovingAverageFilter
import com.kylecorry.trailsensecore.infrastructure.images.BitmapUtils.toBitmap
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

    private val camera by lazy {
        Camera(context, lifecycleOwner, targetResolution = Size(200, 200))
    }

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
        if (!Camera.isAvailable(context)) {
            return
        }

        camera.start(this::onCameraUpdate)
        camera.setTorch(true)
        camera.setExposure(0)
        camera.stopFocusAndMetering()
        startUpTime = Instant.now()
    }

    private fun onCameraUpdate(): Boolean {
        val image = camera.image ?: return true
        analyzeImage(image)
        return true
    }

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
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
        camera.stop(this::onCameraUpdate)
        readings.clear()
        _heartBeats.clear()
    }

    companion object {
        private val WARMUP_TIME = Duration.ofSeconds(3)
    }
}