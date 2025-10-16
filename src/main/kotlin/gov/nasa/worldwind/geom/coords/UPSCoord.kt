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
class UPSCoord
    (latitude: Angle, longitude: Angle, hemisphere: String?, easting: Double, northing: Double) {
    val latitude: Angle
    val longitude: Angle
    val hemisphere: String?
    val easting: Double
    val northing: Double

    /**
     * Create an arbitrary set of UPS coordinates with the given values.
     *
     * @param latitude   the latitude `Angle`.
     * @param longitude  the longitude `Angle`.
     * @param hemisphere the hemisphere, either [AVKey.NORTH] or [                   ][AVKey.SOUTH].
     * @param easting    the easting distance in meters
     * @param northing   the northing distance in meters.
     *
     * @throws IllegalArgumentException if `latitude`, `longitude`, or `hemisphere` is
     * null.
     */
    init {
        require(!(latitude == null || longitude == null)) { "Latitude Or Longitude Is Null" }

        this.latitude = latitude
        this.longitude = longitude
        this.hemisphere = hemisphere
        this.easting = easting
        this.northing = northing
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(if (AVKey.NORTH == hemisphere) "N" else "S")
        sb.append(" ").append(easting).append("E")
        sb.append(" ").append(northing).append("N")
        return sb.toString()
    }

    companion object {
        /**
         * Create a set of UPS coordinates from a pair of latitude and longitude for the given `Globe`.
         *
         * @param latitude  the latitude `Angle`.
         * @param longitude the longitude `Angle`.
         *
         * @return the corresponding `UPSCoord`.
         *
         * @throws IllegalArgumentException if `latitude` or `longitude` is null, or the conversion to
         * UPS coordinates fails.
         */
        fun fromLatLon(latitude: Angle, longitude: Angle): UPSCoord {
            require(!(latitude == null || longitude == null)) { "Latitude Or Longitude Is Null" }

            val converter = UPSCoordConverter()
            val err = converter.convertGeodeticToUPS(latitude.radians, longitude.radians)

            require(err == UPSCoordConverter.UPS_NO_ERROR.toLong()) { "UPS Conversion Error" }

            return UPSCoord(
                latitude, longitude, converter.hemisphere,
                converter.easting, converter.northing
            )
        }

        /**
         * Create a set of UPS coordinates for the given `Globe`.
         *
         * @param hemisphere the hemisphere, either [AVKey.NORTH] or [                   ][AVKey.SOUTH].
         * @param easting    the easting distance in meters
         * @param northing   the northing distance in meters.
         *
         * @return the corresponding `UPSCoord`.
         *
         * @throws IllegalArgumentException if the conversion to UPS coordinates fails.
         */
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
