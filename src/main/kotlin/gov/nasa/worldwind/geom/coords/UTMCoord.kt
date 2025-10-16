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
class UTMCoord
    (
    latitude: Angle, longitude: Angle, zone: Int, hemisphere: String?, easting: Double, northing: Double,
    centralMeridian: Angle?
) {
    val latitude: Angle
    val longitude: Angle
    private val hemisphere: String?
    val zone: Int
    val easting: Double
    val northing: Double
    val centralMeridian: Angle?

    /**
     * Create an arbitrary set of UTM coordinates with the given values.
     *
     * @param latitude        the latitude `Angle`.
     * @param longitude       the longitude `Angle`.
     * @param zone            the UTM zone - 1 to 60.
     * @param hemisphere      the hemisphere, either [AVKey.NORTH] or [                        ][AVKey.SOUTH].
     * @param easting         the easting distance in meters
     * @param northing        the northing distance in meters.
     * @param centralMeridian the central meridian `Angle`.
     *
     * @throws IllegalArgumentException if `latitude` or `longitude` is null.
     */
    init {
        this.latitude = latitude
        this.longitude = longitude
        this.hemisphere = hemisphere
        this.zone = zone
        this.easting = easting
        this.northing = northing
        this.centralMeridian = centralMeridian
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(zone)
        sb.append(" ").append(if (AVKey.NORTH == hemisphere) "N" else "S")
        sb.append(" ").append(easting).append("E")
        sb.append(" ").append(northing).append("N")
        return sb.toString()
    }

    companion object {
        /**
         * Create a set of UTM coordinates from a pair of latitude and longitude for the given `Globe`.
         *
         * @param latitude  the latitude `Angle`.
         * @param longitude the longitude `Angle`.
         *
         * @return the corresponding `UTMCoord`.
         *
         * @throws IllegalArgumentException if `latitude` or `longitude` is null, or the conversion to
         * UTM coordinates fails.
         */
        fun fromLatLon(latitude: Angle, longitude: Angle): UTMCoord {

            val converter = UTMCoordConverter()
            val err = converter.convertGeodeticToUTM(latitude.radians, longitude.radians)

            require(err == UTMCoordConverter.UTM_NO_ERROR.toLong()) { "UTM Conversion Error" }

            return UTMCoord(
                latitude, longitude, converter.zone, converter.hemisphere,
                converter.easting, converter.northing, Angle.fromRadians(converter.centralMeridian)
            )
        }

        /**
         * Create a set of UTM coordinates for the given `Globe`.
         *
         * @param zone       the UTM zone - 1 to 60.
         * @param hemisphere the hemisphere, either [AVKey.NORTH] or [                   ][AVKey.SOUTH].
         * @param easting    the easting distance in meters
         * @param northing   the northing distance in meters.
         *
         * @return the corresponding `UTMCoord`.
         *
         * @throws IllegalArgumentException if the conversion to UTM coordinates fails.
         */
        fun fromUTM(zone: Int, hemisphere: String, easting: Double, northing: Double): UTMCoord {
            val converter = UTMCoordConverter()
            val err = converter.convertUTMToGeodetic(zone.toLong(), hemisphere, easting, northing)

            require(err == UTMCoordConverter.UTM_NO_ERROR.toLong()) { "UTM Conversion Error" }

            return UTMCoord(
                Angle.fromRadians(converter.latitude),
                Angle.fromRadians(converter.longitude),
                zone, hemisphere, easting, northing, Angle.fromRadians(converter.centralMeridian)
            )
        }

        /**
         * Convenience method for converting a UTM coordinate to a geographic location.
         *
         * @param zone       the UTM zone: 1 to 60.
         * @param hemisphere the hemisphere, either [AVKey.NORTH] or [                   ][AVKey.SOUTH].
         * @param easting    the easting distance in meters
         * @param northing   the northing distance in meters.
         *
         * @return the geographic location corresponding to the specified UTM coordinate.
         */
        fun locationFromUTMCoord(zone: Int, hemisphere: String, easting: Double, northing: Double): LatLon {
            val coord: UTMCoord = fromUTM(zone, hemisphere, easting, northing)
            return LatLon(coord.latitude, coord.longitude)
        }
    }
}
