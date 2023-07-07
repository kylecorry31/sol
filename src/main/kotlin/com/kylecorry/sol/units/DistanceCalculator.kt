/*
 * Copyright (C) 2007 The Android Open Source Project
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
 * Modified by Kyle Corry in 2021
 */

package com.kylecorry.sol.units

import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.power
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.toRadians
import kotlin.math.*

object DistanceCalculator {

    /**
     * Computes the distance and bearing to another location
     * @return a 3 element float array of the following [distance (m), initial bearing (true north), final bearing (true north)]
     */
    fun vincenty(
        location1: Coordinate,
        location2: Coordinate
    ): FloatArray {
        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        // using the "Inverse Formula" (section 4)
        val lat1 = location1.latitude.toRadians()
        val lon1 = location1.longitude.toRadians()
        val lat2 = location2.latitude.toRadians()
        val lon2 = location2.longitude.toRadians()
        val MAXITERS = 20
        val a = 6378137.0 // WGS84 major axis
        val b = 6356752.3142 // WGS84 semi-major axis
        val f = (a - b) / a
        val aSqMinusBSqOverBSq = (a * a - b * b) / (b * b)
        val L = lon2 - lon1
        var A = 0.0
        val U1 = atan((1.0 - f) * tan(lat1))
        val U2 = atan((1.0 - f) * tan(lat2))
        val cosU1 = cos(U1)
        val cosU2 = cos(U2)
        val sinU1 = sin(U1)
        val sinU2 = sin(U2)
        val cosU1cosU2 = cosU1 * cosU2
        val sinU1sinU2 = sinU1 * sinU2
        var sigma = 0.0
        var deltaSigma = 0.0
        var cosSqAlpha = 0.0
        var cos2SM = 0.0
        var cosSigma = 0.0
        var sinSigma = 0.0
        var cosLambda = 0.0
        var sinLambda = 0.0
        var lambda = L // initial guess
        for (iter in 0 until MAXITERS) {
            val lambdaOrig = lambda
            cosLambda = cos(lambda)
            sinLambda = sin(lambda)
            val t1 = cosU2 * sinLambda
            val t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda
            val sinSqSigma = t1 * t1 + t2 * t2 // (14)
            sinSigma = sqrt(sinSqSigma)
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda // (15)
            sigma = atan2(sinSigma, cosSigma) // (16)
            val sinAlpha = if (sinSigma == 0.0) 0.0 else cosU1cosU2 * sinLambda / sinSigma // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha
            cos2SM =
                if (cosSqAlpha == 0.0) 0.0 else cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha // (18)
            val uSquared = cosSqAlpha * aSqMinusBSqOverBSq // defn
            A = 1 + uSquared / 16384.0 *  // (3)
                    (4096.0 + uSquared *
                            (-768 + uSquared * (320.0 - 175.0 * uSquared)))
            val B = uSquared / 1024.0 *  // (4)
                    (256.0 + uSquared *
                            (-128.0 + uSquared * (74.0 - 47.0 * uSquared)))
            val C = f / 16.0 *
                    cosSqAlpha *
                    (4.0 + f * (4.0 - 3.0 * cosSqAlpha)) // (10)
            val cos2SMSq = cos2SM * cos2SM
            deltaSigma = (B * sinSigma *  // (6)
                    (cos2SM + B / 4.0 *
                            (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
                                    B / 6.0 * cos2SM *
                                    (-3.0 + 4.0 * sinSigma * sinSigma) *
                                    (-3.0 + 4.0 * cos2SMSq))))
            lambda = L +
                    ((1.0 - C) * f * sinAlpha *
                            (sigma + (C * sinSigma *
                                    (cos2SM + (C * cosSigma *
                                            (-1.0 + 2.0 * cos2SM * cos2SM)))))) // (11)
            val delta = (lambda - lambdaOrig) / lambda
            if (abs(delta) < 1.0e-12) {
                break
            }
        }
        val results = floatArrayOf(0f, 0f, 0f)
        val distance = (b * A * (sigma - deltaSigma)).toFloat()
        results[0] = distance
        val initialBearing = atan2(
            cosU2 * sinLambda,
            cosU1 * sinU2 - sinU1 * cosU2 * cosLambda
        ).toFloat().toDegrees()
        results[1] = initialBearing
        val finalBearing = atan2(
            cosU1 * sinLambda,
            -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda
        ).toFloat().toDegrees()
        results[2] = finalBearing
        return results
    }

    fun haversine(
        location1: Coordinate,
        location2: Coordinate,
        radius: Double = 6371.2e3
    ): FloatArray {
        // Adapted from https://www.movable-type.co.uk/scripts/latlong.html
        val distance = getGreatCircleDistance(location1, location2, radius)
        val initial = getInitialGreatCircleBearing(location1, location2)
        val final = Bearing.getBearing(initial + 180)
        return floatArrayOf(distance, initial, final)
    }

    private fun getInitialGreatCircleBearing(from: Coordinate, to: Coordinate): Float {
        val deltaLongitude = (to.longitude - from.longitude).toRadians()

        val x =
            cosDegrees(from.latitude) * sinDegrees(to.latitude) - sinDegrees(from.latitude) * cosDegrees(
                to.latitude
            ) * cos(deltaLongitude)
        val y = sin(deltaLongitude) * cosDegrees(to.latitude)
        val theta = atan2(y, x)
        return Bearing.getBearing(theta.toDegrees().toFloat())
    }

    private fun getGreatCircleDistance(from: Coordinate, to: Coordinate, radius: Double): Float {
        val deltaLatitude = to.latitude.toRadians() - from.latitude.toRadians()
        val deltaLongitude = to.longitude.toRadians() - from.longitude.toRadians()

        val a = power(
            sin(deltaLatitude / 2),
            2
        ) + cosDegrees(from.latitude) * cosDegrees(to.latitude) * power(sin(deltaLongitude / 2), 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (radius * c).toFloat()
    }

}