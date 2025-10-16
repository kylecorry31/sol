/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords

import com.kylecorry.sol.units.Coordinate
import gov.nasa.worldwind.geom.Angle

/**
 * This class holds an immutable MGRS coordinate string along with
 * the corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 */
object MGRSCoordinateFormat {
    fun getString(latitude: Double, longitude: Double, precision: Int): String {
        val converter = MGRSCoordConverter()
        val error = converter.convertGeodeticToMGRS(
            Angle.fromDegreesLatitude(latitude).radians,
            Angle.fromDegreesLongitude(longitude).radians,
            precision
        )
        require(error == MGRSCoordConverter.MGRS_NO_ERROR.toLong()) { "MGRS Conversion Error" }
        return converter.mgrsString.trim()
    }

    fun fromString(str: String): Coordinate {
        val upperString = str.uppercase().replace(" ", "")
        val converter = MGRSCoordConverter()
        val error = converter.convertMGRSToGeodetic(upperString)

        require(error == MGRSCoordConverter.MGRS_NO_ERROR.toLong()) { "MGRS Conversion Error" }

        return Coordinate(
            Angle.fromRadians(converter.latitude).degrees,
            Angle.fromRadians(converter.longitude).degrees
        )
    }
}
