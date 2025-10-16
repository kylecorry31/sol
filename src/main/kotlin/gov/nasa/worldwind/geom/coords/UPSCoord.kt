/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords

import gov.nasa.worldwind.avlist.AVKey
import gov.nasa.worldwind.geom.Angle

/**
 * This immutable class holds a set of UPS coordinates along with it's corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 */
class UPSCoord(
    val latitude: Angle,
    val longitude: Angle,
    val hemisphere: String?,
    val easting: Double,
    val northing: Double
) {

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(if (AVKey.NORTH == hemisphere) "N" else "S")
        sb.append(" ").append(easting).append("E")
        sb.append(" ").append(northing).append("N")
        return sb.toString()
    }

    companion object {
        fun fromLatLon(latitude: Angle, longitude: Angle): UPSCoord {
            val converter = UPSCoordConverter()
            val err = converter.convertGeodeticToUPS(latitude.radians, longitude.radians)

            require(err == UPSCoordConverter.UPS_NO_ERROR.toLong()) { "UPS Conversion Error" }

            return UPSCoord(
                latitude, longitude, converter.hemisphere,
                converter.easting, converter.northing
            )
        }

        fun fromUPS(hemisphere: String?, easting: Double, northing: Double): UPSCoord {
            val converter = UPSCoordConverter()
            val err = converter.convertUPSToGeodetic(hemisphere, easting, northing)

            require(err == UTMCoordConverter.UTM_NO_ERROR.toLong()) { "UTM Conversion Error" }

            return UPSCoord(
                Angle.fromRadians(converter.latitude),
                Angle.fromRadians(converter.longitude),
                hemisphere, easting, northing
            )
        }
    }
}
