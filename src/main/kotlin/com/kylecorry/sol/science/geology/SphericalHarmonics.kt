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
 * This file was heavily modified by Kyle Corry. Convert to Kotlin, extracted from GeomagneticField.java, updated to support scalars.
 */

package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import java.time.Instant
import kotlin.math.*

internal class SphericalHarmonics(
    private val gCoefficients: Array<FloatArray>,
    private val hCoefficients: Array<FloatArray>,
    baseTime: Instant? = null,
    private val deltaGCoefficients: Array<FloatArray>? = null,
    private val deltaHCoefficients: Array<FloatArray>? = null
) {

    private val schmidtQuasiNormFactors =
        computeSchmidtQuasiNormFactors(gCoefficients.size)

    private val baseTimeMillis = baseTime?.toEpochMilli()

    fun getVector(
        coordinate: Coordinate,
        altitude: Distance = Distance.meters(0f),
        time: Instant = Instant.now(),
    ): Vector3 {
        val timeMillis = time.toEpochMilli()

        // Workaround to handle poles
        val gdLongitudeDeg = coordinate.longitude.toFloat()
        val gdLatitudeDeg = coordinate.latitude.toFloat().coerceIn(-90f + 1e-5f, 90f - 1e-5f)
        val altitudeMeters = altitude.meters().distance
        val geocentric = computeGeocentricCoordinates(gdLatitudeDeg, gdLongitudeDeg, altitudeMeters)
        val mGcLongitudeRad = geocentric.x
        val mGcLatitudeRad = geocentric.y
        val mGcRadiusKm = geocentric.z

        val maxN = gCoefficients.size
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
        val yearsSinceBase = (timeMillis - (baseTimeMillis ?: timeMillis)) / (365f * 24 * 60 * 60 * 1000)

        var gcX = 0f
        var gcY = 0f
        var gcZ = 0f

        for (n in 1 until maxN) {
            for (m in 0..n) {
                // Adjust for time
                val g = gCoefficients[n][m] + yearsSinceBase * (deltaGCoefficients?.get(n)?.get(m) ?: 0f)
                val h = hCoefficients[n][m] + yearsSinceBase * (deltaHCoefficients?.get(n)?.get(m) ?: 0f)

                // Negative derivative with respect to latitude, divided by
                // radius.  This looks like the negation of the version in the
                // NOAA Technical report because that report used
                // P_n^m(sin(theta)) and we use P_n^m(cos(90 - theta)), so the
                // derivative with respect to theta is negated.
                gcX += relativeRadiusPower[n + 2] * (g * cosMLon[m] + h * sinMLon[m]) * legendre.mPDeriv[n][m] * schmidtQuasiNormFactors[n][m]
                // Negative derivative with respect to longitude, divided by
                // radius.
                gcY += relativeRadiusPower[n + 2] * m * (g * sinMLon[m] - h * cosMLon[m]) * legendre.mP[n][m] * schmidtQuasiNormFactors[n][m] * inverseCosLatitude
                // Negative derivative with respect to radius.
                gcZ -= (n + 1) * relativeRadiusPower[n + 2] * (g * cosMLon[m] + h * sinMLon[m]) * legendre.mP[n][m] * schmidtQuasiNormFactors[n][m]
            }
        }

        val latDiffRad = gdLatitudeDeg.toRadians() - mGcLatitudeRad
        val x = (gcX * cos(latDiffRad) + gcZ * sin(latDiffRad))
        val y = gcY
        val z = (-gcX * sin(latDiffRad) + gcZ * cos(latDiffRad))
        return Vector3(x, y, z)
    }


    private fun computeGeocentricCoordinates(
        gdLatitudeDeg: Float,
        gdLongitudeDeg: Float,
        altitudeMeters: Float
    ): Vector3 {
        val altitudeKm = altitudeMeters / 1000.0f
        val a2 = EARTH_SEMI_MAJOR_AXIS_KM * EARTH_SEMI_MAJOR_AXIS_KM
        val b2 = EARTH_SEMI_MINOR_AXIS_KM * EARTH_SEMI_MINOR_AXIS_KM
        val gdLatRad = gdLatitudeDeg.toRadians()
        val clat = cos(gdLatRad)
        val slat = sin(gdLatRad)
        val tlat = slat / clat
        val latRad = sqrt(a2 * clat * clat + b2 * slat * slat)
        val mGcLatitudeRad = atan(tlat * (latRad * altitudeKm + b2) / (latRad * altitudeKm + a2))
        val mGcLongitudeRad = gdLongitudeDeg.toRadians()
        val radSq = altitudeKm * altitudeKm +
                2 * altitudeKm * sqrt(a2 * clat * clat + b2 * slat * slat) +
                (a2 * a2 * clat * clat + b2 * b2 * slat * slat) / (a2 * clat * clat + b2 * slat * slat)
        val mGcRadiusKm = sqrt(radSq)
        return Vector3(mGcLongitudeRad, mGcLatitudeRad, mGcRadiusKm)
    }

    companion object {
        private const val EARTH_SEMI_MAJOR_AXIS_KM = 6378.137f
        private const val EARTH_SEMI_MINOR_AXIS_KM = 6356.7524f
        private const val EARTH_REFERENCE_RADIUS_KM = 6371.2f

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


