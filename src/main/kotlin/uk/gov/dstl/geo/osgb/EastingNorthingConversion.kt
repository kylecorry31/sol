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
 * <b>Convert between LatLon and Easting/Northings for Transverse Mercator projection</b>
 *
 * <p>Equations taken from
 * https://www.ordnancesurvey.co.uk/documents/resources/guide-coordinate-systems-great-britain.pdf
 */
object EastingNorthingConversion {
    /**
     * Convert from Lat lon
     *
     * @param inputCoordinates Array of coordinates [lat, lon] to convert
     * @param a Semi-major axis of ellipsoid
     * @param b Semi-minor axis of the ellipsoid
     * @param n0 Northing of true origin
     * @param e0 Easting of true origin
     * @param f0 Scale factor on central meridian
     * @param lat0 Latitude of true origin
     * @param lon0 Longitude of true origin
     * @return Array of easting/northings [easting, northing]
     */
    fun fromLatLon(
        inputCoordinates: DoubleArray,
        a: Double,
        b: Double,
        n0: Double,
        e0: Double,
        f0: Double,
        lat0: Double,
        lon0: Double
    ): DoubleArray {
        val lat = Math.toRadians(inputCoordinates[0])
        val lon = Math.toRadians(inputCoordinates[1])

        val lat0Rad = Math.toRadians(lat0)
        val lon0Rad = Math.toRadians(lon0)

        val e2 = (a.pow(2) - b.pow(2)) / a.pow(2)

        val n = (a - b) / (a + b)
        val n2 = n.pow(2)
        val n3 = n.pow(3)

        val eSinPhi = 1 - e2 * sin(lat).pow(2) // eSinPhi = 1 - e^2 * sin^2 phi
        val nu = a * f0 * eSinPhi.pow(-0.5)
        val rho = a * f0 * (1 - e2) * eSinPhi.pow(-1.5)
        val eta2 = (nu / rho) - 1

        val m = b * f0 * ((1 + n + (5.0 / 4.0) * n2 + (5.0 / 4.0) * n3) * (lat - lat0Rad)
                - (3.0 * n + 3.0 * n2 + (21.0 / 8.0) * n3)
                    * sin(lat - lat0Rad)
                    * cos(lat + lat0Rad)
                + ((15.0 / 8.0) * n2 + (15.0 / 8.0) * n3)
                    * sin(2.0 * (lat - lat0Rad))
                    * cos(2.0 * (lat + lat0Rad))
                - (35.0 / 24.0)
                    * n3
                    * sin(3.0 * (lat - lat0Rad))
                    * cos(3.0 * (lat + lat0Rad)))

        val i = m + n0
        val ii = (nu / 2) * sin(lat) * cos(lat)
        val iii = (nu / 24) * sin(lat) * cos(lat).pow(3) * (5 - tan(lat).pow(2) + 9 * eta2)
        val iiiA = (nu / 720) * sin(lat) * cos(lat).pow(5) * (61 - 58 * tan(lat).pow(2) + tan(lat).pow(4))
        val iv = nu * cos(lat)
        val v = (nu / 6) * cos(lat).pow(3) * ((nu / rho) - tan(lat).pow(2))
        val vi = (nu / 120) * cos(lat).pow(5) * (5
                - 18 * tan(lat).pow(2)
                + tan(lat).pow(4)
                + 14 * eta2
                - 58 * (tan(lat).pow(2)) * eta2)

        val retN = i + ii * (lon - lon0Rad).pow(2) + iii * (lon - lon0Rad).pow(4) + iiiA * (lon - lon0Rad).pow(6)
        val retE = e0 + iv * (lon - lon0Rad) + v * (lon - lon0Rad).pow(3) + vi * (lon - lon0Rad).pow(5)

        return doubleArrayOf(retE, retN)
    }

    /**
     * Convert to Lat Lon.
     *
     * @param inputCoordinates Array of easting/northings [easting, northing] to convert
     * @param a Semi-major axis of ellipsoid
     * @param b Semi-minor axis of the ellipsoid
     * @param n0 Northing of true origin
     * @param e0 Easting of true origin
     * @param f0 Scale factor on central meridian
     * @param lat0Degrees Latitude of true origin
     * @param lon0Degrees Longitude of true origin
     * @return Array of coordinates [lat, lon]
     */
    fun toLatLon(
        inputCoordinates: DoubleArray,
        a: Double,
        b: Double,
        n0: Double,
        e0: Double,
        f0: Double,
        lat0Degrees: Double,
        lon0Degrees: Double
    ): DoubleArray {
        val coordE = inputCoordinates[0]
        val coordN = inputCoordinates[1]

        val e2 = (a.pow(2) - b.pow(2)) / a.pow(2)

        val n = (a - b) / (a + b)
        val n2 = n.pow(2)
        val n3 = n.pow(3)

        val lat0 = Math.toRadians(lat0Degrees)
        val lon0 = Math.toRadians(lon0Degrees)

        var m = 0.0
        var latPrime = lat0
        var delta = 1.0

        while (delta > 0.00001) {
            latPrime = ((coordN - n0 - m) / (a * f0)) + latPrime

            m = b * f0 * ((1 + n + (5.0 / 4.0) * n2 + (5.0 / 4.0) * n3) * (latPrime - lat0)
                    - (3.0 * n + 3.0 * n2 + (21.0 / 8.0) * n3)
                        * sin(latPrime - lat0)
                        * cos(latPrime + lat0)
                    + ((15.0 / 8.0) * n2 + (15.0 / 8.0) * n3)
                        * sin(2.0 * (latPrime - lat0))
                        * cos(2.0 * (latPrime + lat0))
                    - (35.0 / 24.0)
                        * n3
                        * sin(3.0 * (latPrime - lat0))
                        * cos(3.0 * (latPrime + lat0)))

            delta = abs(coordN - n0 - m)
        }

        val eSinPhi = 1 - e2 * sin(latPrime).pow(2) // eSinPhi = 1 - e^2 * sin^2 phi
        val nu = a * f0 * eSinPhi.pow(-0.5)
        val rho = a * f0 * (1 - e2) * eSinPhi.pow(-1.5)
        val eta2 = (nu / rho) - 1

        val vii = tan(latPrime) / (2.0 * rho * nu)
        val viii = (tan(latPrime) / (24.0 * rho * nu.pow(3))) * (5.0
                + 3.0 * tan(latPrime).pow(2)
                + eta2
                - 9.0 * tan(latPrime).pow(2) * eta2)
        val ix = (tan(latPrime) / (720.0 * rho * nu.pow(5))) * (61.0
                + 90.0 * tan(latPrime).pow(2)
                + 45.0 * tan(latPrime).pow(4))
        val x = 1.0 / (cos(latPrime) * nu)
        val xi = (1.0 / (6.0 * cos(latPrime) * nu.pow(3))) * (nu / rho + 2.0 * tan(latPrime).pow(2))
        val xii = (1.0 / (120.0 * cos(latPrime) * nu.pow(5))) * (5.0
                + 28.0 * tan(latPrime).pow(2)
                + 24.0 * tan(latPrime).pow(4))
        val xiiA = (1.0 / (5040.0 * cos(latPrime) * nu.pow(7))) * (61.0
                + 662.0 * tan(latPrime).pow(2)
                + 1320.0 * tan(latPrime).pow(4)
                + 720.0 * tan(latPrime).pow(6))

        val dE = coordE - e0

        var lat = latPrime - vii * dE.pow(2) + viii * dE.pow(4) - ix * dE.pow(6)
        var lon = lon0 + x * dE - xi * dE.pow(3) + xii * dE.pow(5) - xiiA * dE.pow(7)

        lat = Math.toDegrees(lat)
        lon = Math.toDegrees(lon)

        return doubleArrayOf(lat, lon)
    }
}
