/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords

import gov.nasa.worldwind.geom.Angle

/**
 * This class holds an immutable MGRS coordinate string along with
 * the corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 */
class MGRSCoord(
    val latitude: Angle,
    val longitude: Angle,
    private val MGRSString: String
) {
    
    override fun toString(): String = MGRSString

    companion object {
        /**
         * Create a WGS84 MGRS coordinate from a pair of latitude and longitude Angle
         * with the maximum precision of five digits (one meter).
         *
         * @param latitude the latitude Angle.
         * @param longitude the longitude Angle.
         * @return the corresponding MGRSCoord.
         * @throws IllegalArgumentException if latitude or longitude is null,
         * or the conversion to MGRS coordinates fails.
         */
        @JvmStatic
        @JvmOverloads
        fun fromLatLon(latitude: Angle, longitude: Angle, precision: Int = 5): MGRSCoord {
            val converter = MGRSCoordConverter()
            val err = converter.convertGeodeticToMGRS(latitude.radians, longitude.radians, precision)

            if (err.toInt() != MGRSCoordConverter.MGRS_NO_ERROR) {
                throw IllegalArgumentException("MGRS Conversion Error")
            }

            return MGRSCoord(latitude, longitude, converter.mgrsString)
        }

        /**
         * Create a MGRS coordinate from a standard MGRS coordinate text string.
         *
         * The string will be converted to uppercase and stripped of all spaces before being evaluated.
         *
         * Valid examples:
         * - 32TLP5626635418
         * - 32 T LP 56266 35418
         * - 11S KU 528 111
         *
         * @param MGRSString the MGRS coordinate text string.
         * @return the corresponding MGRSCoord.
         * @throws IllegalArgumentException if the MGRSString is null or empty,
         * or the conversion to geodetic coordinates fails (invalid coordinate string).
         */
        @JvmStatic
        fun fromString(MGRSString: String): MGRSCoord {
            require(MGRSString.isNotEmpty()) { "String Is Null or Empty" }

            val cleanedString = MGRSString.uppercase().replace(" ", "")

            val converter = MGRSCoordConverter()
            val err = converter.convertMGRSToGeodetic(cleanedString)

            if (err.toInt() != MGRSCoordConverter.MGRS_NO_ERROR) {
                throw IllegalArgumentException("MGRS Conversion Error")
            }

            return MGRSCoord(
                Angle.fromRadians(converter.latitude),
                Angle.fromRadians(converter.longitude),
                cleanedString
            )
        }
    }
}
