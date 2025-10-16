/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords

import gov.nasa.worldwind.avlist.AVKey
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.geom.LatLon
import gov.nasa.worldwind.util.WWUtil

/**
 * This immutable class holds a set of UTM coordinates along with it's corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 */
class UTMCoord(
    val latitude: Angle,
    val longitude: Angle,
    val zone: Int,
    val hemisphere: String,
    val easting: Double,
    val northing: Double,
    val centralMeridian: Angle = Angle.fromDegreesLongitude(0.0)
) {
    
    override fun toString(): String {
        return buildString {
            append(zone)
            append(" ").append(if (AVKey.NORTH == hemisphere) "N" else "S")
            append(" ").append(easting).append("E")
            append(" ").append(northing).append("N")
        }
    }

    companion object {
        /**
         * Create a set of UTM coordinates from a pair of latitude and longitude.
         *
         * @param latitude  the latitude Angle.
         * @param longitude the longitude Angle.
         *
         * @return the corresponding UTMCoord.
         *
         * @throws IllegalArgumentException if latitude or longitude is null, or the conversion to
         *                                  UTM coordinates fails.
         */
        @JvmStatic
        @JvmOverloads
        fun fromLatLon(latitude: Angle, longitude: Angle, datum: String? = null): UTMCoord {
            val converter = if (!WWUtil.isEmpty(datum) && datum == "NAD27") {
                val llNAD27 = UTMCoordConverter.convertWGS84ToNAD27(latitude, longitude)
                UTMCoordConverter(UTMCoordConverter.CLARKE_A, UTMCoordConverter.CLARKE_F).also {
                    it.convertGeodeticToUTM(llNAD27.latitude.radians, llNAD27.longitude.radians)
                }
            } else {
                UTMCoordConverter().also {
                    it.convertGeodeticToUTM(latitude.radians, longitude.radians)
                }
            }

            val err = if (!WWUtil.isEmpty(datum) && datum == "NAD27") {
                val llNAD27 = UTMCoordConverter.convertWGS84ToNAD27(latitude, longitude)
                converter.convertGeodeticToUTM(llNAD27.latitude.radians, llNAD27.longitude.radians)
            } else {
                converter.convertGeodeticToUTM(latitude.radians, longitude.radians)
            }

            if (err.toInt() != UTMCoordConverter.UTM_NO_ERROR) {
                throw IllegalArgumentException("UTM Conversion Error")
            }

            return UTMCoord(
                latitude, longitude, converter.zone, converter.hemisphere,
                converter.easting, converter.northing, Angle.fromRadians(converter.centralMeridian)
            )
        }

        /**
         * Create a set of UTM coordinates.
         *
         * @param zone       the UTM zone - 1 to 60.
         * @param hemisphere the hemisphere, either [AVKey.NORTH] or [AVKey.SOUTH].
         * @param easting    the easting distance in meters
         * @param northing   the northing distance in meters.
         *
         * @return the corresponding UTMCoord.
         *
         * @throws IllegalArgumentException if the conversion to UTM coordinates fails.
         */
        @JvmStatic
        fun fromUTM(zone: Int, hemisphere: String, easting: Double, northing: Double): UTMCoord {
            val converter = UTMCoordConverter()
            val err = converter.convertUTMToGeodetic(zone.toLong(), hemisphere, easting, northing)

            if (err.toInt() != UTMCoordConverter.UTM_NO_ERROR) {
                throw IllegalArgumentException("UTM Conversion Error")
            }

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
         * @param hemisphere the hemisphere, either [AVKey.NORTH] or [AVKey.SOUTH].
         * @param easting    the easting distance in meters
         * @param northing   the northing distance in meters.
         *
         * @return the geographic location corresponding to the specified UTM coordinate.
         */
        @JvmStatic
        fun locationFromUTMCoord(zone: Int, hemisphere: String, easting: Double, northing: Double): LatLon {
            val coord = fromUTM(zone, hemisphere, easting, northing)
            return LatLon(coord.latitude, coord.longitude)
        }
    }
}
