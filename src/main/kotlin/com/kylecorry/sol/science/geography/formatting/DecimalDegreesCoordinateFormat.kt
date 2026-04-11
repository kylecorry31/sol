package com.kylecorry.sol.science.geography.formatting

import com.kylecorry.sol.shared.DecimalFormatter
import com.kylecorry.sol.shared.toDoubleCompat
import com.kylecorry.sol.units.Coordinate

class DecimalDegreesCoordinateFormat(
    private val precision: Int = 6,
) : CoordinateFormat {
    override fun toString(coordinate: Coordinate): String {
        val formattedLatitude = DecimalFormatter.format(coordinate.latitude, precision)
        val formattedLongitude = DecimalFormatter.format(coordinate.longitude, precision)
        return "$formattedLatitude°,  $formattedLongitude°"
    }

    override fun parse(text: String): Coordinate? {
        val regex = Regex("^(-?\\d+(?:[.,]\\d+)?)°?[,\\s]+(-?\\d+(?:[.,]\\d+)?)°?\$")
        val matches = regex.find(text.trim()) ?: return null
        val latitude = matches.groupValues[1].toDoubleCompat() ?: return null
        val longitude = matches.groupValues[2].toDoubleCompat() ?: return null

        if (Coordinate.isValidLatitude(latitude) && Coordinate.isValidLongitude(longitude)) {
            return Coordinate(latitude, longitude)
        }

        return null
    }
}
