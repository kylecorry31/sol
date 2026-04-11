package com.kylecorry.sol.science.geography.formatting

import com.kylecorry.sol.math.arithmetic.Arithmetic.power
import com.kylecorry.sol.shared.toDoubleCompat
import com.kylecorry.sol.units.Coordinate
import gov.nasa.worldwind.avlist.AVKey
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.geom.coords.UPSCoord
import gov.nasa.worldwind.geom.coords.UTMCoord

class UTMCoordinateFormat(private val precision: Int = 7) : CoordinateFormat {
    override fun toString(coordinate: Coordinate): String {
        try {
            val lat = Angle.fromDegreesLatitude(coordinate.latitude)
            val lng = Angle.fromDegreesLongitude(coordinate.longitude)
            val utm = UTMCoord.fromLatLon(lat, lng)

            val zone = utm.zone.toString().padStart(2, '0')

            val letter =
                if (coordinate.latitude < -72) 'C' else if (coordinate.latitude < -64) 'D' else if (coordinate.latitude < -56) 'E' else if (coordinate.latitude < -48) 'F' else if (coordinate.latitude < -40) 'G' else if (coordinate.latitude < -32) 'H' else if (coordinate.latitude < -24) 'J' else if (coordinate.latitude < -16) 'K' else if (coordinate.latitude < -8) 'L' else if (coordinate.latitude < 0) 'M' else if (coordinate.latitude < 8) 'N' else if (coordinate.latitude < 16) 'P' else if (coordinate.latitude < 24) 'Q' else if (coordinate.latitude < 32) 'R' else if (coordinate.latitude < 40) 'S' else if (coordinate.latitude < 48) 'T' else if (coordinate.latitude < 56) 'U' else if (coordinate.latitude < 64) 'V' else if (coordinate.latitude < 72) 'W' else 'X'


            val easting =
                roundUTMPrecision(precision, utm.easting.toInt()).toString().padStart(7, '0') + "E"
            val northing =
                roundUTMPrecision(precision, utm.northing.toInt()).toString().padStart(7, '0') + "N"

            return "$zone$letter $easting $northing"
        } catch (e: Exception) {
            return toUPS(coordinate, precision)
        }
    }

    override fun parse(text: String): Coordinate? {
        val regex =
            Regex("(\\d*)\\s*([a-z,A-Z^ioIO])\\s*(\\d+(?:[.,]\\d+)?)[\\smMeE]+(\\d+(?:[.,]\\d+)?)[\\smMnN]*")
        val matches = regex.find(text) ?: return null

        val zone = matches.groupValues[1].toIntOrNull() ?: 0
        val letter = matches.groupValues[2].toCharArray().first()
        val easting = matches.groupValues[3].toDoubleCompat() ?: 0.0
        val northing = matches.groupValues[4].toDoubleCompat() ?: 0.0

        return fromUTM(zone, letter, easting, northing)
    }

    private fun toUPS(coordinate: Coordinate, precision: Int): String {
        try {
            val lat = Angle.fromDegreesLatitude(coordinate.latitude)
            val lng = Angle.fromDegreesLongitude(coordinate.longitude)
            val ups = UPSCoord.fromLatLon(lat, lng)

            val easting =
                roundUTMPrecision(precision, ups.easting.toInt()).toString().padStart(7, '0') + "E"
            val northing =
                roundUTMPrecision(precision, ups.northing.toInt()).toString().padStart(7, '0') + "N"

            val letter = if (coordinate.isNorthernHemisphere) {
                if (coordinate.latitude == 90.0 || coordinate.longitude >= 0) {
                    'Z'
                } else {
                    'Y'
                }
            } else {
                if (coordinate.latitude == -90.0 || coordinate.longitude >= 0) {
                    'B'
                } else {
                    'A'
                }
            }

            return "$letter $easting $northing"
        } catch (e: Exception) {
            return "?"
        }
    }

    private fun roundUTMPrecision(precision: Int, utmValue: Int): Int {
        return (utmValue / power(10.0, 7 - precision)).toInt() * power(10.0, 7 - precision).toInt()
    }

    private fun fromUTM(
        zone: Int,
        letter: Char,
        easting: Double,
        northing: Double
    ): Coordinate? {
        val polarLetters = listOf('A', 'B', 'Y', 'Z')
        return try {
            if (polarLetters.contains(letter.uppercaseChar())) {
                // Get it into the catch block
                throw Exception()
            }
            val latLng = UTMCoord.locationFromUTMCoord(
                zone,
                if (letter.uppercaseChar() <= 'M') AVKey.SOUTH else AVKey.NORTH,
                easting,
                northing
            )
            Coordinate(latLng.latitude.degrees, latLng.longitude.degrees)
        } catch (e: Exception) {
            val letters = listOf('A', 'B', 'Y', 'Z')
            if (zone != 0 || !letters.contains(letter.uppercaseChar())) {
                return null
            }
            try {
                val latLng = UPSCoord.fromUPS(
                    if (letter.uppercaseChar() <= 'M') AVKey.SOUTH else AVKey.NORTH,
                    easting,
                    northing
                )
                Coordinate(latLng.latitude.degrees, latLng.longitude.degrees)
            } catch (e2: Exception) {
                null
            }
        }
    }
}
