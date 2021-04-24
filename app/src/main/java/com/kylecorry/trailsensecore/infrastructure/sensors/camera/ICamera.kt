package com.kylecorry.trailsensecore.infrastructure.sensors.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.kylecorry.trailsensecore.domain.pixels.PixelCoordinate
import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface ICamera: ISensor {
    val image: ImageProxy?
    fun setZoom(zoom: Float)

    @SuppressLint("UnsafeExperimentalUsageError")
    fun setExposure(index: Int)
    fun setTorch(isOn: Boolean)
    fun getFOV(): Pair<Float, Float>?
    fun stopFocusAndMetering()
    fun startFocusAndMetering(point: PixelCoordinate)
}