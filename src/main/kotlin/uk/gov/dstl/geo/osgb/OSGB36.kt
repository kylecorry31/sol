/*
 * Crown Copyright (C) 2019 Dstl
 *
 * Converted to Kotlin by Kyle Corry (Automated) in 2025
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
 * <b>Convert between OSGB36 and WGS84 coordinate systems</b>
 *
 * <p>Values taken from
 * https://www.ordnancesurvey.co.uk/documents/resources/guide-coordinate-systems-great-britain.pdf
 */
internal object OSGB36 {
    private const val tX = -446.448
    private const val tY = 125.157
    private const val tZ = -542.060
    private const val s = 20.4894 / 1000000 // Value given by OS in ppm, so convert to a unitless quantity
    private const val rX = -0.1502 * (PI / 648000) // Value given by OS in arcseconds, so convert to radians
    private const val rY = -0.2470 * (PI / 648000) // Value given by OS in arcseconds, so convert to radians
    private const val rZ = -0.8421 * (PI / 648000) // Value given by OS in arcseconds, so convert to radians

    /**
     * Convert to WGS86 Lat Lon from OSGB.
     *
     * @param lat Latitude in OSGB36 coordinates
     * @param lon Longitude in OSGB36 coordinates
     * @return Array of coordinates [lat, long] in WGS84
     */
    fun toWGS84(lat: Double, lon: Double): DoubleArray {
        val cartesian = CartesianConversion.fromLatLon(
            doubleArrayOf(lat, lon, 0.0),
            Constants.ELLIPSOID_AIRY1830_MAJORAXIS,
            Constants.ELLIPSOID_AIRY1830_MINORAXIS
        )
        val transformed = CartesianConversion.helmertTransformation(cartesian, -tX, -tY, -tZ, -s, -rX, -rY, -rZ)
        val ret = CartesianConversion.toLatLon(
            transformed,
            Constants.ELLIPSOID_GRS80_MAJORAXIS,
            Constants.ELLIPSOID_GRS80_MINORAXIS,
            0.00000001
        )

        return doubleArrayOf(ret[0], ret[1])
    }

    /**
     * Convert from WGS86 Lat Lon to OSGB.
     *
     * @param lat Latitude in WGS84 coordinates
     * @param lon Longitude in WGS84 coordinates
     * @return Array of coordinates [lat, lon] in OSGB36
     */
    fun fromWGS84(lat: Double, lon: Double): DoubleArray {
        val cartesian = CartesianConversion.fromLatLon(
            doubleArrayOf(lat, lon, 0.0),
            Constants.ELLIPSOID_GRS80_MAJORAXIS,
            Constants.ELLIPSOID_GRS80_MINORAXIS
        )
        val transformed = CartesianConversion.helmertTransformation(cartesian, tX, tY, tZ, s, rX, rY, rZ)
        val ret = CartesianConversion.toLatLon(
            transformed,
            Constants.ELLIPSOID_AIRY1830_MAJORAXIS,
            Constants.ELLIPSOID_AIRY1830_MINORAXIS,
            0.00000001
        )

        return doubleArrayOf(ret[0], ret[1])
    }
}
