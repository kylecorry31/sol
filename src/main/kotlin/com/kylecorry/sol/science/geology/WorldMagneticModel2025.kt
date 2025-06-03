package com.kylecorry.sol.science.geology

import com.kylecorry.sol.time.Time.toUTC
import java.time.LocalDate

object WorldMagneticModel2025 {
    val BASE_TIME = LocalDate.of(2025, 1, 1).atStartOfDay().toUTC().toInstant()

    val G_COEFFICIENTS = arrayOf(
        floatArrayOf(0.0f),
        floatArrayOf(-29351.8f, -1410.8f),
        floatArrayOf(-2556.6f, 2951.1f, 1649.3f),
        floatArrayOf(1361.0f, -2404.1f, 1243.8f, 453.6f),
        floatArrayOf(895.0f, 799.5f, 55.7f, -281.1f, 12.1f),
        floatArrayOf(-233.2f, 368.9f, 187.2f, -138.7f, -142.0f, 20.9f),
        floatArrayOf(64.4f, 63.8f, 76.9f, -115.7f, -40.9f, 14.9f, -60.7f),
        floatArrayOf(79.5f, -77.0f, -8.8f, 59.3f, 15.8f, 2.5f, -11.1f, 14.2f),
        floatArrayOf(23.2f, 10.8f, -17.5f, 2.0f, -21.7f, 16.9f, 15.0f, -16.8f, 0.9f),
        floatArrayOf(4.6f, 7.8f, 3.0f, -0.2f, -2.5f, -13.1f, 2.4f, 8.6f, -8.7f, -12.9f),
        floatArrayOf(-1.3f, -6.4f, 0.2f, 2.0f, -1.0f, -0.6f, -0.9f, 1.5f, 0.9f, -2.7f, -3.9f),
        floatArrayOf(2.9f, -1.5f, -2.5f, 2.4f, -0.6f, -0.1f, -0.6f, -0.1f, 1.1f, -1.0f, -0.2f, 2.6f),
        floatArrayOf(-2.0f, -0.2f, 0.3f, 1.2f, -1.3f, 0.6f, 0.6f, 0.5f, -0.1f, -0.4f, -0.2f, -1.3f, -0.7f)
    )

    val H_COEFFICIENTS = arrayOf(
        floatArrayOf(0.0f),
        floatArrayOf(0.0f, 4545.4f),
        floatArrayOf(0.0f, -3133.6f, -815.1f),
        floatArrayOf(0.0f, -56.6f, 237.5f, -549.5f),
        floatArrayOf(0.0f, 278.6f, -133.9f, 212.0f, -375.6f),
        floatArrayOf(0.0f, 45.4f, 220.2f, -122.9f, 43.0f, 106.1f),
        floatArrayOf(0.0f, -18.4f, 16.8f, 48.8f, -59.8f, 10.9f, 72.7f),
        floatArrayOf(0.0f, -48.9f, -14.4f, -1.0f, 23.4f, -7.4f, -25.1f, -2.3f),
        floatArrayOf(0.0f, 7.1f, -12.6f, 11.4f, -9.7f, 12.7f, 0.7f, -5.2f, 3.9f),
        floatArrayOf(0.0f, -24.8f, 12.2f, 8.3f, -3.3f, -5.2f, 7.2f, -0.6f, 0.8f, 10.0f),
        floatArrayOf(0.0f, 3.3f, 0.0f, 2.4f, 5.3f, -9.1f, 0.4f, -4.2f, -3.8f, 0.9f, -9.1f),
        floatArrayOf(0.0f, 0.0f, 2.9f, -0.6f, 0.2f, 0.5f, -0.3f, -1.2f, -1.7f, -2.9f, -1.8f, -2.3f),
        floatArrayOf(0.0f, -1.3f, 0.7f, 1.0f, -1.4f, -0.0f, 0.6f, -0.1f, 0.8f, 0.1f, -1.0f, 0.1f, 0.2f)
    )

    val DELTA_G = arrayOf(
        floatArrayOf(0.0f),
        floatArrayOf(12.0f, 9.7f),
        floatArrayOf(-11.6f, -5.2f, -8.0f),
        floatArrayOf(-1.3f, -4.2f, 0.4f, -15.6f),
        floatArrayOf(-1.6f, -2.4f, -6.0f, 5.6f, -7.0f),
        floatArrayOf(0.6f, 1.4f, 0.0f, 0.6f, 2.2f, 0.9f),
        floatArrayOf(-0.2f, -0.4f, 0.9f, 1.2f, -0.9f, 0.3f, 0.9f),
        floatArrayOf(-0.0f, -0.1f, -0.1f, 0.5f, -0.1f, -0.8f, -0.8f, 0.8f),
        floatArrayOf(-0.1f, 0.2f, 0.0f, 0.5f, -0.1f, 0.3f, 0.2f, -0.0f, 0.2f),
        floatArrayOf(-0.0f, -0.1f, 0.1f, 0.3f, -0.3f, 0.0f, 0.3f, -0.1f, 0.1f, -0.1f),
        floatArrayOf(0.1f, 0.0f, 0.1f, 0.1f, -0.0f, -0.3f, 0.0f, -0.1f, -0.1f, -0.0f, -0.0f),
        floatArrayOf(0.0f, -0.0f, 0.0f, 0.0f, 0.0f, -0.1f, 0.0f, -0.0f, -0.1f, -0.1f, -0.1f, -0.1f),
        floatArrayOf(0.0f, 0.0f, -0.0f, -0.0f, -0.0f, -0.0f, 0.1f, -0.0f, 0.0f, 0.0f, -0.1f, -0.0f, -0.1f)
    )

    val DELTA_H = arrayOf(
        floatArrayOf(0.0f),
        floatArrayOf(0.0f, -21.5f),
        floatArrayOf(0.0f, -27.7f, -12.1f),
        floatArrayOf(0.0f, 4.0f, -0.3f, -4.1f),
        floatArrayOf(0.0f, -1.1f, 4.1f, 1.6f, -4.4f),
        floatArrayOf(0.0f, -0.5f, 2.2f, 0.4f, 1.7f, 1.9f),
        floatArrayOf(0.0f, 0.3f, -1.6f, -0.4f, 0.9f, 0.7f, 0.9f),
        floatArrayOf(0.0f, 0.6f, 0.5f, -0.8f, 0.0f, -1.0f, 0.6f, -0.2f),
        floatArrayOf(0.0f, -0.2f, 0.5f, -0.4f, 0.4f, -0.5f, -0.6f, 0.3f, 0.2f),
        floatArrayOf(0.0f, -0.3f, 0.3f, -0.3f, 0.3f, 0.2f, -0.1f, -0.2f, 0.4f, 0.1f),
        floatArrayOf(0.0f, 0.0f, -0.0f, -0.2f, 0.1f, -0.1f, 0.1f, 0.0f, -0.1f, 0.2f, -0.0f),
        floatArrayOf(0.0f, -0.0f, 0.1f, -0.0f, 0.1f, -0.0f, -0.0f, 0.1f, -0.0f, 0.0f, 0.0f, 0.0f),
        floatArrayOf(0.0f, -0.0f, 0.0f, -0.1f, 0.1f, -0.0f, -0.0f, -0.0f, 0.0f, -0.0f, -0.0f, 0.0f, -0.1f)
    )

}