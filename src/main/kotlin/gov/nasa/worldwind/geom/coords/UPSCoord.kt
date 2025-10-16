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
    val hemisphere: String,
    val easting: Double,
    val northing: Double
) {
    
    override fun toString(): String {
        return buildString {
            append(if (AVKey.NORTH == hemisphere) "N" else "S")
            append(" ").append(easting).append("E")
            append(" ").append(northing).append("N")
        }
    }

    companion object {
        /**
         * Create a set of UPS coordinates from a pair of latitude and longitude.
         *
         * @param latitude  the latitude Angle.
         * @param longitude the longitude Angle.
         *
         * @return the corresponding UPSCoord.
         *
         * @throws IllegalArgumentException if latitude or longitude is null, or the conversion to
         *                                  UPS coordinates fails.
         */
        @JvmStatic
        fun fromLatLon(latitude: Angle, longitude: Angle): UPSCoord {
            val converter = UPSCoordConverter()
            val err = converter.convertGeodeticToUPS(latitude.radians, longitude.radians)

            if (err.toInt() != UPSCoordConverter.UPS_NO_ERROR) {
                throw IllegalArgumentException("UPS Conversion Error")
            }

            return UPSCoord(latitude, longitude, converter.hemisphere,
                converter.easting, converter.northing)
        }

        /**
         * Create a set of UPS coordinates.
         *
         * @param hemisphere the hemisphere, either [AVKey.NORTH] or [AVKey.SOUTH].
         * @param easting    the easting distance in meters
         * @param northing   the northing distance in meters.
         *
         * @return the corresponding UPSCoord.
         *
         * @throws IllegalArgumentException if the conversion to UPS coordinates fails.
         */
        @JvmStatic
        fun fromUPS(hemisphere: String, easting: Double, northing: Double): UPSCoord {
            val converter = UPSCoordConverter()
            val err = converter.convertUPSToGeodetic(hemisphere, easting, northing)

            if (err.toInt() != UTMCoordConverter.UTM_NO_ERROR) {
                throw IllegalArgumentException("UTM Conversion Error")
            }

            return UPSCoord(
                Angle.fromRadians(converter.latitude),
                Angle.fromRadians(converter.longitude),
                hemisphere, easting, northing
            )
        }
    }
}
