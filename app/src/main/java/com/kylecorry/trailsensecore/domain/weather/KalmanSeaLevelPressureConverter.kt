package com.kylecorry.trailsensecore.domain.weather

import com.kylecorry.trailsensecore.domain.math.KalmanFilter
import com.kylecorry.trailsensecore.domain.math.removeOutliers

class KalmanSeaLevelPressureConverter(
    private val defaultGPSError: Float = 30f,
    private val altitudeOutlierThreshold: Float = defaultGPSError,
    private val altitudeProcessError: Float = 1f,
    private val defaultPressureError: Float = 0.5f,
    private val pressureOutlierThreshold: Float = defaultPressureError,
    private val pressureProcessError: Float = 0.01f,
) : ISeaLevelPressureConverter {

    override fun convert(
        readings: List<PressureAltitudeReading>,
        factorInTemperature: Boolean
    ): List<PressureReading> {
        val altitudes = readings.map { it.altitude.toDouble() }
        val altitudeErrors =
            readings.map { if (it.altitudeError == null || it.altitudeError == 0f) defaultGPSError.toDouble() else it.altitudeError.toDouble() }
        val pressures = readings.map { it.pressure.toDouble() }

        val altitudesNoOutliers = removeOutliers(altitudes, altitudeOutlierThreshold.toDouble())
        val filteredAltitudes = KalmanFilter.filter(
            altitudesNoOutliers,
            altitudeErrors,
            altitudeProcessError.toDouble()
        )

        val filteredPressures = removeOutliers(pressures, pressureOutlierThreshold.toDouble())
        val seaLevel = mutableListOf<PressureReading>()

        for (i in readings.indices) {
            val pressure = filteredPressures[i]
            val time = readings[i].time
            val temp = readings[i].temperature
            val altitude = filteredAltitudes[i]
            seaLevel.add(
                PressureAltitudeReading(
                    time,
                    pressure.toFloat(),
                    altitude.toFloat(),
                    temp
                ).seaLevel(factorInTemperature)
            )
        }

        val kalmanSeaLevel = KalmanFilter.filter(
            seaLevel.map { it.value.toDouble() },
            defaultPressureError.toDouble(),
            pressureProcessError.toDouble()
        )

        return kalmanSeaLevel.mapIndexed { index, pressure ->
            seaLevel[index].copy(value = pressure.toFloat())
        }

    }
}