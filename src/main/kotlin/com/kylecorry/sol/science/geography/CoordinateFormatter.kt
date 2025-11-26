package com.kylecorry.sol.science.geography

import com.kylecorry.sol.science.geography.formatting.*
import com.kylecorry.sol.units.Coordinate

object CoordinateFormatter {
    val formats = listOf(
        DecimalDegreesCoordinateFormat(),
        DegreesDecimalMinutesCoordinateFormat(),
        DegreesMinutesSecondsCoordinateFormat(),
        UTMCoordinateFormat(),
        MGRSCoordinateFormat(),
        USNGCoordinateFormat(),
        OSGBCoordinateFormat()
    )

    fun Coordinate.Companion.parse(
        location: String,
        formats: List<CoordinateFormat> = CoordinateFormatter.formats
    ): Coordinate? {
        return formats.firstNotNullOfOrNull { it.parse(location) }
    }
}