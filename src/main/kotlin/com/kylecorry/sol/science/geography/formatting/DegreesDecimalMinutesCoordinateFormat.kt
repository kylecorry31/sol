package com.kylecorry.sol.science.geography.formatting

import com.kylecorry.sol.math.SolMath.roundPlaces
import com.kylecorry.sol.shared.toDoubleCompat
import com.kylecorry.sol.units.Coordinate
import java.util.*
import kotlin.math.abs

class DegreesDecimalMinutesCoordinateFormat(private val precision: Int = 3) : CoordinateFormat {
    override fun toString(coordinate: Coordinate): String {
        val latDir = if (coordinate.latitude < 0) "S" else "N"
        val lngDir = if (coordinate.longitude < 0) "W" else "E"
        return "${ddmString(coordinate.latitude, precision)}$latDir    ${
            ddmString(
                coordinate.longitude,
                precision
            )
        }$lngDir"
    }

    override fun parse(text: String): Coordinate? {
        val ddmRegex =
            Regex("^(\\d+)°\\s*(\\d+(?:[.,]\\d+)?)[′']\\s*([nNsS])[,\\s]+(\\d+)°\\s*(\\d+(?:[.,]\\d+)?)[′']\\s*([wWeE])\$")
        val matches = ddmRegex.find(text.trim()) ?: return null

        var latitudeDecimal = 0.0
        latitudeDecimal += matches.groupValues[1].toDouble()
        latitudeDecimal += (matches.groupValues[2].toDoubleCompat() ?: 0.0) / 60
        latitudeDecimal *= if (matches.groupValues[3].lowercase(Locale.getDefault()) == "n") 1 else -1

        var longitudeDecimal = 0.0
        longitudeDecimal += matches.groupValues[4].toDouble()
        longitudeDecimal += (matches.groupValues[5].toDoubleCompat() ?: 0.0) / 60
        longitudeDecimal *= if (matches.groupValues[6].lowercase(Locale.getDefault()) == "e") 1 else -1

        if (Coordinate.isValidLatitude(latitudeDecimal) && Coordinate.isValidLongitude(
                longitudeDecimal
            )
        ) {
            return Coordinate(latitudeDecimal, longitudeDecimal)
        }

        return null
    }

    private fun ddmString(degrees: Double, precision: Int): String {
        val deg = abs(degrees.toInt())
        val minutes = abs((degrees % 1) * 60).roundPlaces(precision)
        return "$deg°$minutes'"
    }
}
