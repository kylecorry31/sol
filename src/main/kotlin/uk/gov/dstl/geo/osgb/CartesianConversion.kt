/*
 * Crown Copyright (C) 2019 Dstl
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
 */
package uk.gov.dstl.geo.osgb

import kotlin.math.*

/**
 * <b>Convert between LatLon and Cartesian coordinate systems</b>
 *
 * <p>This code uses an approximate Helmert transformation, with an error of up to 5 metres (both
 * horizontally and vertically) Equations taken from
 * https://www.ordnancesurvey.co.uk/documents/resources/guide-coordinate-systems-great-britain.pdf
 */
object CartesianConversion {
    /**
     * @param inputCoordinates Array of coordinates [lat, lon, ellipsoidHeight] to convert
     * @param a Semi-major axis of ellipsoid
     * @param b Semi-minor axis of the ellipsoid
     * @return Array of cartesian coordinates [x, y, z]
     */
    fun fromLatLon(inputCoordinates: DoubleArray, a: Double, b: Double): DoubleArray {
        val lat = Math.toRadians(inputCoordinates[0])
        val lon = Math.toRadians(inputCoordinates[1])
        val height = inputCoordinates[2]

        val e2 = (a.pow(2) - b.pow(2)) / a.pow(2)
        val v = a / sqrt(1 - e2 * sin(lat).pow(2))

        val x = (v + height) * cos(lat) * cos(lon)
        val y = (v + height) * cos(lat) * sin(lon)
        val z = ((1 - e2) * v + height) * sin(lat)

        return doubleArrayOf(x, y, z)
    }

    /**
     * @param inputCoordinates Array of cartesian coordinates [x, y, z] to convert
     * @param a Semi-major axis of ellipsoid
     * @param b Semi-minor axis of the ellipsoid
     * @param precision Precision to calculate the latitude to
     * @return Array of coordinates [lat, lon, ellipsoidHeight]
     */
    fun toLatLon(inputCoordinates: DoubleArray, a: Double, b: Double, precision: Double): DoubleArray {
        val x = inputCoordinates[0]
        val y = inputCoordinates[1]
        val z = inputCoordinates[2]

        val e2 = (a.pow(2) - b.pow(2)) / a.pow(2)

        var lon = atan(y / x)

        val p = sqrt(x.pow(2) + y.pow(2))
        var lat = atan(z / p * (1 - e2))

        var v = 0.0
        var delta = 2 * precision

        while (delta > precision) {
            v = a / sqrt(1 - e2 * sin(lat).pow(2))
            val newLat = atan((z + e2 * v * sin(lat)) / p)

            delta = abs(Math.toDegrees(lat - newLat))
            lat = newLat
        }

        val height = (p / cos(lat)) - v

        return doubleArrayOf(Math.toDegrees(lat), Math.toDegrees(lon), height)
    }

    /**
     * @param inputCoordinates Array of coordinates [x, y, z] to transform using the specified
     *     transformation parameters
     * @param tX Translation along the x axis (in metres)
     * @param tY Translation along the y axis (in metres)
     * @param tZ Translation along the z azies (in metres)
     * @param s Scale factor
     * @param rX Rotation about the x axis (in radians)
     * @param rY Rotation about the y axis (in radians)
     * @param rZ Rotation about the z axis (in radians)
     * @return Array of coordinates [x, y, z] that have been transformed
     */
    fun helmertTransformation(
        inputCoordinates: DoubleArray,
        tX: Double,
        tY: Double,
        tZ: Double,
        s: Double,
        rX: Double,
        rY: Double,
        rZ: Double
    ): DoubleArray {
        val aX = inputCoordinates[0]
        val aY = inputCoordinates[1]
        val aZ = inputCoordinates[2]

        val bX = tX + ((1 + s) * aX) + (-rZ * aY) + (rY * aZ)
        val bY = tY + (rZ * aX) + ((1 + s) * aY) + (-rX * aZ)
        val bZ = tZ + (-rY * aX) + (rX * aY) + ((1 + s) * aZ)

        return doubleArrayOf(bX, bY, bZ)
    }
}
