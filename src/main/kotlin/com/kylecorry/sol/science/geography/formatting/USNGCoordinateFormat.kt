package com.kylecorry.sol.science.geography.formatting

import com.kylecorry.sol.units.Coordinate
import gov.nasa.worldwind.geom.coords.MGRSCoordinateFormat as MGRS

class USNGCoordinateFormat(private val precision: Int = 5) : CoordinateFormat {
    override fun toString(coordinate: Coordinate): String {
        val mgrs = try {
            MGRS.getString(coordinate.latitude, coordinate.longitude, precision)
        } catch (_: Exception) {
            "?"
        }
        if (mgrs.length > 3) {
            return mgrs.substring(0, 3) + " " + mgrs.substring(3)
        }
        return mgrs
    }

    override fun parse(text: String): Coordinate? {
        return try {
            MGRS.fromString(text)
        } catch (e: Exception) {
            null
        }
    }
}
