/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file was heavily modified by Kyle Corry. Convert to Kotlin, update to the 2025 WMM.
 */

package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.toRadians
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*

internal class GeomagneticField(
    gdLatitudeDegInput: Float,
    gdLongitudeDeg: Float,
    altitudeMeters: Float,
    timeMillis: Long
) {
    var x: Float = 0f
        private set
    var y: Float = 0f
        private set
    var z: Float = 0f
        private set

    private var mGcLatitudeRad: Float = 0f
    private var mGcLongitudeRad: Float = 0f
    private var mGcRadiusKm: Float = 0f

    init {
        // Workaround to handle poles
        val gdLatitudeDeg = gdLatitudeDegInput.coerceIn(-90f + 1e-5f, 90f - 1e-5f)
        computeGeocentricCoordinates(gdLatitudeDeg, gdLongitudeDeg, altitudeMeters)

        val maxN = G_COEFF.size
        val legendre = LegendreTable(maxN - 1, (PI / 2f - mGcLatitudeRad).toFloat())

        val relativeRadiusPower = FloatArray(maxN + 2)
        relativeRadiusPower[0] = 1.0f
        relativeRadiusPower[1] = EARTH_REFERENCE_RADIUS_KM / mGcRadiusKm
        for (i in 2 until relativeRadiusPower.size) {
            relativeRadiusPower[i] = relativeRadiusPower[i - 1] * relativeRadiusPower[1]
        }

        // Precompute sin(lon * m) and cos(lon * m)
        val sinMLon = FloatArray(maxN)
        val cosMLon = FloatArray(maxN)
        sinMLon[0] = 0.0f
        cosMLon[0] = 1.0f
        sinMLon[1] = sin(mGcLongitudeRad)
        cosMLon[1] = cos(mGcLongitudeRad)
        for (m in 2 until maxN) {
            val x = m shr 1
            sinMLon[m] = sinMLon[m - x] * cosMLon[x] + cosMLon[m - x] * sinMLon[x]
            cosMLon[m] = cosMLon[m - x] * cosMLon[x] - sinMLon[m - x] * sinMLon[x]
        }

        val inverseCosLatitude = 1.0f / cos(mGcLatitudeRad)
        val yearsSinceBase = (timeMillis - BASE_TIME) / (365f * 24 * 60 * 60 * 1000)

        var gcX = 0f
        var gcY = 0f
        var gcZ = 0f

        for (n in 1 until maxN) {
            for (m in 0..n) {
                // Adjust for time
                val g = G_COEFF[n][m] + yearsSinceBase * DELTA_G[n][m]
                val h = H_COEFF[n][m] + yearsSinceBase * DELTA_H[n][m]

                // Negative derivative with respect to latitude, divided by
                // radius.  This looks like the negation of the version in the
                // NOAA Technical report because that report used
                // P_n^m(sin(theta)) and we use P_n^m(cos(90 - theta)), so the
                // derivative with respect to theta is negated.
                gcX += relativeRadiusPower[n + 2] * (g * cosMLon[m] + h * sinMLon[m]) * legendre.mPDeriv[n][m] * SCHMIDT_QUASI_NORM_FACTORS[n][m]
                // Negative derivative with respect to longitude, divided by
                // radius.
                gcY += relativeRadiusPower[n + 2] * m * (g * sinMLon[m] - h * cosMLon[m]) * legendre.mP[n][m] * SCHMIDT_QUASI_NORM_FACTORS[n][m] * inverseCosLatitude
                // Negative derivative with respect to radius.
                gcZ -= (n + 1) * relativeRadiusPower[n + 2] * (g * cosMLon[m] + h * sinMLon[m]) * legendre.mP[n][m] * SCHMIDT_QUASI_NORM_FACTORS[n][m]
            }
        }

        val latDiffRad = gdLatitudeDeg.toRadians() - mGcLatitudeRad
        x = (gcX * cos(latDiffRad) + gcZ * sin(latDiffRad))
        y = gcY
        z = (-gcX * sin(latDiffRad) + gcZ * cos(latDiffRad))
    }

    val declination: Float
        get() = atan2(y, x).toDegrees()

    val inclination: Float
        get() = atan2(z, horizontalStrength).toDegrees()

    val horizontalStrength: Float
        get() = hypot(x, y)

    val fieldStrength: Float
        get() = sqrt(x * x + y * y + z * z)

    private fun computeGeocentricCoordinates(
        gdLatitudeDeg: Float,
        gdLongitudeDeg: Float,
        altitudeMeters: Float
    ) {
        val altitudeKm = altitudeMeters / 1000.0f
        val a2 = EARTH_SEMI_MAJOR_AXIS_KM * EARTH_SEMI_MAJOR_AXIS_KM
        val b2 = EARTH_SEMI_MINOR_AXIS_KM * EARTH_SEMI_MINOR_AXIS_KM
        val gdLatRad = gdLatitudeDeg.toRadians()
        val clat = cos(gdLatRad)
        val slat = sin(gdLatRad)
        val tlat = slat / clat
        val latRad = sqrt(a2 * clat * clat + b2 * slat * slat)
        mGcLatitudeRad = atan(tlat * (latRad * altitudeKm + b2) / (latRad * altitudeKm + a2))
        mGcLongitudeRad = gdLongitudeDeg.toRadians()
        val radSq = altitudeKm * altitudeKm +
                2 * altitudeKm * sqrt(a2 * clat * clat + b2 * slat * slat) +
                (a2 * a2 * clat * clat + b2 * b2 * slat * slat) / (a2 * clat * clat + b2 * slat * slat)
        mGcRadiusKm = sqrt(radSq)
    }

    companion object {
        private const val EARTH_SEMI_MAJOR_AXIS_KM = 6378.137f
        private const val EARTH_SEMI_MINOR_AXIS_KM = 6356.7524f
        private const val EARTH_REFERENCE_RADIUS_KM = 6371.2f

        private val BASE_TIME = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
            .toInstant().toEpochMilli()

        private val G_COEFF = arrayOf<FloatArray>(
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
        private val H_COEFF = arrayOf<FloatArray>(
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
        private val DELTA_G = arrayOf<FloatArray>(
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
        private val DELTA_H = arrayOf<FloatArray>(
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

        private val SCHMIDT_QUASI_NORM_FACTORS =
            computeSchmidtQuasiNormFactors(G_COEFF.size)

        fun computeSchmidtQuasiNormFactors(maxN: Int): Array<FloatArray> {
            val schmidtQuasiNorm = arrayOfNulls<FloatArray>(maxN + 1)
            schmidtQuasiNorm[0] = floatArrayOf(1.0f)
            for (n in 1..maxN) {
                schmidtQuasiNorm[n] = FloatArray(n + 1)
                schmidtQuasiNorm[n]!![0] =
                    schmidtQuasiNorm[n - 1]!![0] * (2 * n - 1) / n.toFloat()
                for (m in 1..n) {
                    schmidtQuasiNorm[n]!![m] = (schmidtQuasiNorm[n]!![m - 1]
                            * sqrt(
                        ((n - m + 1) * (if (m == 1) 2 else 1)
                                / (n + m).toFloat()).toDouble()
                    ).toFloat())
                }
            }
            @Suppress("UNCHECKED_CAST")
            return schmidtQuasiNorm as Array<FloatArray>
        }
    }

}


internal class LegendreTable(maxN: Int, thetaRad: Float) {
    val mP: Array<FloatArray> = Array(maxN + 1) { FloatArray(it + 1) }
    val mPDeriv: Array<FloatArray> = Array(maxN + 1) { FloatArray(it + 1) }

    init {
        val cos = cos(thetaRad)
        val sin = sin(thetaRad)
        mP[0][0] = 1.0f
        mPDeriv[0][0] = 0.0f

        for (n in 1..maxN) {
            for (m in 0..n) {
                when {
                    n == m -> {
                        mP[n][m] = sin * mP[n - 1][m - 1]
                        mPDeriv[n][m] = cos * mP[n - 1][m - 1] + sin * mPDeriv[n - 1][m - 1]
                    }

                    n == 1 || m == n - 1 -> {
                        mP[n][m] = cos * mP[n - 1][m]
                        mPDeriv[n][m] = -sin * mP[n - 1][m] + cos * mPDeriv[n - 1][m]
                    }

                    else -> {
                        val k = ((n - 1) * (n - 1) - m * m).toFloat() / ((2 * n - 1) * (2 * n - 3))
                        mP[n][m] = cos * mP[n - 1][m] - k * mP[n - 2][m]
                        mPDeriv[n][m] = -sin * mP[n - 1][m] + cos * mPDeriv[n - 1][m] - k * mPDeriv[n - 2][m]
                    }
                }
            }
        }
    }
}