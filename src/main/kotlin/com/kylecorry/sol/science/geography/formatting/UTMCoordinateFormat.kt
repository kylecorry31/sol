package com.kylecorry.sol.science.geography.formatting

import com.kylecorry.sol.math.arithmetic.Arithmetic.power
import com.kylecorry.sol.shared.toDoubleCompat
import com.kylecorry.sol.units.Coordinate
import gov.nasa.worldwind.avlist.AVKey
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.geom.coords.UPSCoord
import gov.nasa.worldwind.geom.coords.UTMCoord

class UTMCoordinateFormat(private val precision: Int = 7) : CoordinateFormat {

    private val latitudeBands = listOf(
        -72.0 to 'C',
        -64.0 to 'D',
        -56.0 to 'E',
        -48.0 to 'F',
        -40.0 to 'G',
        -32.0 to 'H',
        -24.0 to 'J',
        -16.0 to 'K',
        -8.0 to 'L',
        0.0 to 'M',
        8.0 to 'N',
        16.0 to 'P',
        24.0 to 'Q',
        32.0 to 'R',
        40.0 to 'S',
        48.0 to 'T',
        56.0 to 'U',
        64.0 to 'V',
        72.0 to 'W'
    )

    override fun toString(coordinate: Coordinate): String {
        try {
            val lat = Angle.fromDegreesLatitude(coordinate.latitude)
            val lng = Angle.fromDegreesLongitude(coordinate.longitude)
            val utm = UTMCoord.fromLatLon(lat, lng)

            val zone = utm.zone.toString().padStart(2, '0')
            val letter = getLatitudeBandLetter(coordinate.latitude)
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

    private fun getLatitudeBandLetter(latitude: Double): Char {
        return latitudeBands.firstOrNull { latitude < it.first }?.second ?: 'X'
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
