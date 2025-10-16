/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords

import gov.nasa.worldwind.geom.Angle
import java.util.*

/**
 * This class holds an immutable MGRS coordinate string along with
 * the corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 */
class MGRSCoord
    (latitude: Angle, longitude: Angle, MGRSString: String) {
    private val MGRSString: String
    val latitude: Angle
    val longitude: Angle

    /**
     * Create an arbitrary MGRS coordinate from a pair of latitude-longitude `Angle`
     * and the corresponding MGRS coordinate string.
     *
     * @param latitude the latitude `Angle`.
     * @param longitude the longitude `Angle`.
     * @param MGRSString the corresponding MGRS coordinate string.
     * @throws IllegalArgumentException if `latitude` or `longitude` is null,
     * or the MGRSString is null or empty.
     */
    init {
        require(MGRSString.isNotEmpty()) { "String Is Empty" }
        this.latitude = latitude
        this.longitude = longitude
        this.MGRSString = MGRSString
    }

    override fun toString(): String {
        return this.MGRSString
    }

    companion object {
        /**
         * Create a MGRS coordinate from a pair of latitude and longitude `Angle`
         * with the given precision or number of digits (1 to 5).
         *
         * @param latitude the latitude `Angle`.
         * @param longitude the longitude `Angle`.
         * @param precision the number of digits used for easting and northing (1 to 5).
         * @return the corresponding `MGRSCoord`.
         * @throws IllegalArgumentException if `latitude` or `longitude` is null,
         * or the conversion to MGRS coordinates fails.
         */
        fun fromLatLon(latitude: Angle, longitude: Angle, precision: Int): MGRSCoord {
            val converter = MGRSCoordConverter()
            val err = converter.convertGeodeticToMGRS(latitude.radians, longitude.radians, precision)

            require(err == MGRSCoordConverter.MGRS_NO_ERROR.toLong()) { "MGRS Conversion Error" }

            return MGRSCoord(latitude, longitude, converter.mgrsString)
        }

        /**
         * Create a MGRS coordinate from a standard MGRS coordinate text string.
         *
         *
         * The string will be converted to uppercase and stripped of all spaces before being evaluated.
         *
         *
         * Valid examples:<br></br>
         * 32TLP5626635418<br></br>
         * 32 T LP 56266 35418<br></br>
         * 11S KU 528 111<br></br>
         *
         * @param MGRSString the MGRS coordinate text string.
         * @return the corresponding `MGRSCoord`.
         * @throws IllegalArgumentException if the `MGRSString` is null or empty,
         * the `globe` is null, or the conversion to geodetic coordinates fails (invalid coordinate string).
         */
        fun fromString(MGRSString: String): MGRSCoord {
            var upperString = MGRSString
            require(upperString.isNotEmpty()) { "String Is Null" }

            upperString = upperString.uppercase(Locale.getDefault()).replace(" ".toRegex(), "")

            val converter = MGRSCoordConverter()
            val err = converter.convertMGRSToGeodetic(upperString)

            require(err == MGRSCoordConverter.MGRS_NO_ERROR.toLong()) { "MGRS Conversion Error" }

            return MGRSCoord(
                Angle.fromRadians(converter.latitude),
                Angle.fromRadians(converter.longitude),
                upperString
            )
        }
    }
}
