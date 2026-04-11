/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords

import gov.nasa.worldwind.avlist.AVKey
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.geom.LatLon

/**
 * This immutable class holds a set of UTM coordinates along with it's corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 */
class UTMCoord(
    val latitude: Angle, val longitude: Angle, val zone: Int,
    private val hemisphere: String?, val easting: Double, val northing: Double
) {

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(zone)
        sb.append(" ").append(if (AVKey.NORTH == hemisphere) "N" else "S")
        sb.append(" ").append(easting).append("E")
        sb.append(" ").append(northing).append("N")
        return sb.toString()
    }

    companion object {
        fun fromLatLon(latitude: Angle, longitude: Angle): UTMCoord {
            val converter = UTMCoordConverter()
            val err = converter.convertGeodeticToUTM(latitude.radians, longitude.radians)

            require(err == UTMCoordConverter.UTM_NO_ERROR.toLong()) { "UTM Conversion Error" }

            return UTMCoord(
                latitude, longitude, converter.zone, converter.hemisphere,
                converter.easting, converter.northing
            )
        }

        fun fromUTM(zone: Int, hemisphere: String, easting: Double, northing: Double): UTMCoord {
            val converter = UTMCoordConverter()
            val err = converter.convertUTMToGeodetic(zone.toLong(), hemisphere, easting, northing)

            require(err == UTMCoordConverter.UTM_NO_ERROR.toLong()) { "UTM Conversion Error" }

            return UTMCoord(
                Angle.fromRadians(converter.latitude),
                Angle.fromRadians(converter.longitude),
                zone, hemisphere, easting, northing
            )
        }

        fun locationFromUTMCoord(zone: Int, hemisphere: String, easting: Double, northing: Double): LatLon {
            val coord: UTMCoord = fromUTM(zone, hemisphere, easting, northing)
            return LatLon(coord.latitude, coord.longitude)
        }
    }
}
