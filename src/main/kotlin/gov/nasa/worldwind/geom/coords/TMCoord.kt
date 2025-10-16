/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords

import gov.nasa.worldwind.geom.Angle

/**
 * This class holds a set of Transverse Mercator coordinates along with the
 * corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 * @see TMCoordConverter
 */
class TMCoord(
    val latitude: Angle,
    val longitude: Angle,
    val easting: Double,
    val northing: Double,
    val originLatitude: Angle,
    val centralMeridian: Angle,
    val falseEasting: Double,
    val falseNorthing: Double,
    val scale: Double
) {

    companion object {
        /**
         * Create a set of Transverse Mercator coordinates from a pair of latitude and longitude,
         * for the given projection parameters.
         *
         * @param latitude the latitude Angle.
         * @param longitude the longitude Angle.
         * @param a semi-major ellipsoid radius. If this and argument f are non-null, will use the specified a and f.
         * @param f ellipsoid flattening. If this and argument a are non-null, will use the specified a and f.
         * @param originLatitude the origin latitude Angle.
         * @param centralMeridian the central meridian longitude Angle.
         * @param falseEasting easting value at the center of the projection in meters.
         * @param falseNorthing northing value at the center of the projection in meters.
         * @param scale scaling factor.
         * @return the corresponding TMCoord.
         * @throws IllegalArgumentException if latitude or longitude is null,
         * or the conversion to TM coordinates fails.
         */
        @JvmStatic
        fun fromLatLon(
            latitude: Angle,
            longitude: Angle,
            a: Double?,
            f: Double?,
            originLatitude: Angle,
            centralMeridian: Angle,
            falseEasting: Double,
            falseNorthing: Double,
            scale: Double
        ): TMCoord {
            val converter = TMCoordConverter()
            val actualA = a ?: converter.a
            val actualF = f ?: converter.f

            var err = converter.setTransverseMercatorParameters(
                actualA, actualF, originLatitude.radians, centralMeridian.radians,
                falseEasting, falseNorthing, scale
            )
            if (err.toInt() == TMCoordConverter.TRANMERC_NO_ERROR)
                err = converter.convertGeodeticToTransverseMercator(latitude.radians, longitude.radians)

            if (err.toInt() != TMCoordConverter.TRANMERC_NO_ERROR && err.toInt() != TMCoordConverter.TRANMERC_LON_WARNING) {
                throw IllegalArgumentException("TM Conversion Error")
            }

            return TMCoord(
                latitude, longitude, converter.easting, converter.northing,
                originLatitude, centralMeridian, falseEasting, falseNorthing, scale
            )
        }

        /**
         * Create a set of Transverse Mercator coordinates for the given
         * easting, northing and projection parameters.
         *
         * @param easting the easting distance value in meters.
         * @param northing the northing distance value in meters.
         * @param originLatitude the origin latitude Angle.
         * @param centralMeridian the central meridian longitude Angle.
         * @param falseEasting easting value at the center of the projection in meters.
         * @param falseNorthing northing value at the center of the projection in meters.
         * @param scale scaling factor.
         * @return the corresponding TMCoord.
         * @throws IllegalArgumentException if originLatitude or centralMeridian
         * is null, or the conversion to geodetic coordinates fails.
         */
        @JvmStatic
        fun fromTM(
            easting: Double,
            northing: Double,
            originLatitude: Angle,
            centralMeridian: Angle,
            falseEasting: Double,
            falseNorthing: Double,
            scale: Double
        ): TMCoord {
            val converter = TMCoordConverter()

            val a = converter.a
            val f = converter.f
            var err = converter.setTransverseMercatorParameters(
                a, f, originLatitude.radians, centralMeridian.radians,
                falseEasting, falseNorthing, scale
            )
            if (err.toInt() == TMCoordConverter.TRANMERC_NO_ERROR)
                err = converter.convertTransverseMercatorToGeodetic(easting, northing)

            if (err.toInt() != TMCoordConverter.TRANMERC_NO_ERROR && err.toInt() != TMCoordConverter.TRANMERC_LON_WARNING) {
                throw IllegalArgumentException("TM Conversion Error")
            }

            return TMCoord(
                Angle.fromRadians(converter.latitude), Angle.fromRadians(converter.longitude),
                easting, northing, originLatitude, centralMeridian, falseEasting, falseNorthing, scale
            )
        }
    }
}
