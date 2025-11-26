package com.kylecorry.sol.science.geography.formatting

import com.kylecorry.sol.units.Coordinate
import gov.nasa.worldwind.geom.coords.MGRSCoordinateFormat as MGRS

class MGRSCoordinateFormat(private val precision: Int = 5) : CoordinateFormat {
    override fun toString(coordinate: Coordinate): String {
        return try {
            MGRS.getString(coordinate.latitude, coordinate.longitude, precision)
        } catch (_: Exception) {
            "?"
        }
    }

    override fun parse(text: String): Coordinate? {
        return try {
            MGRS.fromString(text)
        } catch (e: Exception) {
            null
        }
    }
}
