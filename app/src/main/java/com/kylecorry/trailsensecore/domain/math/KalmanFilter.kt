package com.kylecorry.trailsensecore.domain.math

class KalmanFilter(
    initialEstimate: Double,
    private val initialError: Double,
    private val processError: Double
) {

    private var estimate = initialEstimate
    private var estimateError = initialError

    fun filter(measurement: Double, error: Double = initialError): Double {
        var divisor = (estimateError + error)
        if (divisor == 0.0) {
            divisor = 0.0001
        }

        val kg = estimateError / divisor
        estimate += kg * (measurement - estimate)
        estimateError = (1 - kg) * (estimateError + processError)
        return estimate
    }

    companion object {
        fun filter(
            measurements: List<Double>,
            errors: List<Double>,
            processError: Double
        ): List<Double> {
            if (measurements.isEmpty()) {
                return emptyList()
            } else if (measurements.size < 2) {
                return listOf(measurements[0])
            }

            val kalman = KalmanFilter(measurements[0], errors[0], processError)
            val values = mutableListOf(measurements[0])

            for (i in 1..measurements.lastIndex) {
                values.add(kalman.filter(measurements[i], errors[i]))
            }

            return values
        }

        fun filter(measurements: List<Double>, error: Double, processError: Double): List<Double> {
            return filter(measurements, List(measurements.size) { error }, processError)
        }

    }

}