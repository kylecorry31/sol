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
class TMCoord
    (
    latitude: Angle, longitude: Angle, easting: Double, northing: Double,
    originLatitude: Angle, centralMeridian: Angle,
    falseEasting: Double, falseNorthing: Double,
    scale: Double
) {
    val latitude: Angle
    val longitude: Angle
    private val originLatitude: Angle
    private val centralMeridian: Angle
    private val falseEasting: Double
    private val falseNorthing: Double
    val scale: Double
    val easting: Double
    val northing: Double

    /**
     * Create an arbitrary set of Transverse Mercator coordinates with the given values.
     *
     * @param latitude the latitude `Angle`.
     * @param longitude the longitude `Angle`.
     * @param easting the easting distance value in meters.
     * @param northing the northing distance value in meters.
     * @param originLatitude the origin latitude `Angle`.
     * @param centralMeridian the central meridian longitude `Angle`.
     * @param falseEasting easting value at the center of the projection in meters.
     * @param falseNorthing northing value at the center of the projection in meters.
     * @param scale scaling factor.
     * @throws IllegalArgumentException if `latitude`, `longitude`, `originLatitude`
     * or `centralMeridian` is null.
     */
    init {
        require(!(latitude == null || longitude == null)) { "Latitude Or Longitude Is Null" }
        require(!(originLatitude == null || centralMeridian == null)) { "Angle Is Null" }

        this.latitude = latitude
        this.longitude = longitude
        this.easting = easting
        this.northing = northing
        this.originLatitude = originLatitude
        this.centralMeridian = centralMeridian
        this.falseEasting = falseEasting
        this.falseNorthing = falseNorthing
        this.scale = scale
    }

    companion object {
        /**
         * Create a set of Transverse Mercator coordinates from a pair of latitude and longitude,
         * for the given `Globe` and projection parameters.
         *
         * @param latitude the latitude `Angle`.
         * @param longitude the longitude `Angle`.
         * @param a semi-major ellipsoid radius. If this and argument f are non-null and globe is null, will use the specfied a and f.
         * @param f ellipsoid flattening. If this and argument a are non-null and globe is null, will use the specfied a and f.
         * @param originLatitude the origin latitude `Angle`.
         * @param centralMeridian the central meridian longitude `Angle`.
         * @param falseEasting easting value at the center of the projection in meters.
         * @param falseNorthing northing value at the center of the projection in meters.
         * @param scale scaling factor.
         * @return the corresponding `TMCoord`.
         * @throws IllegalArgumentException if `latitude` or `longitude` is null,
         * or the conversion to TM coordinates fails. If the globe is null conversion will default
         * to using WGS84.
         */
        fun fromLatLon(
            latitude: Angle, longitude: Angle, a: Double?, f: Double?,
            originLatitude: Angle, centralMeridian: Angle,
            falseEasting: Double, falseNorthing: Double,
            scale: Double
        ): TMCoord {
            var a = a
            var f = f
            require(!(latitude == null || longitude == null)) { "Latitude Or Longitude Is Null" }
            require(!(originLatitude == null || centralMeridian == null)) { "Angle Is Null" }

            val converter = TMCoordConverter()
            if (a == null || f == null) {
                a = converter.a
                f = converter.f
            }
            var err = converter.setTransverseMercatorParameters(
                a, f, originLatitude.radians, centralMeridian.radians,
                falseEasting, falseNorthing, scale
            )
            if (err == TMCoordConverter.TRANMERC_NO_ERROR.toLong()) err =
                converter.convertGeodeticToTransverseMercator(latitude.radians, longitude.radians)

            require(!(err != TMCoordConverter.TRANMERC_NO_ERROR.toLong() && err != TMCoordConverter.TRANMERC_LON_WARNING.toLong())) { "TM Conversion Error" }

            return TMCoord(
                latitude, longitude, converter.easting, converter.northing,
                originLatitude, centralMeridian, falseEasting, falseNorthing, scale
            )
        }

        /**
         * Create a set of Transverse Mercator coordinates for the given `Globe`,
         * easting, northing and projection parameters.
         *
         * @param easting the easting distance value in meters.
         * @param northing the northing distance value in meters.
         * @param originLatitude the origin latitude `Angle`.
         * @param centralMeridian the central meridian longitude `Angle`.
         * @param falseEasting easting value at the center of the projection in meters.
         * @param falseNorthing northing value at the center of the projection in meters.
         * @param scale scaling factor.
         * @return the corresponding `TMCoord`.
         * @throws IllegalArgumentException if `originLatitude` or `centralMeridian`
         * is null, or the conversion to geodetic coordinates fails. If the globe is null conversion will default
         * to using WGS84.
         */
        fun fromTM(
            easting: Double, northing: Double,
            originLatitude: Angle, centralMeridian: Angle,
            falseEasting: Double, falseNorthing: Double,
            scale: Double
        ): TMCoord {
            require(!(originLatitude == null || centralMeridian == null)) { "Angle Is Null" }

            val converter = TMCoordConverter()

            val a = converter.a
            val f = converter.f
            var err = converter.setTransverseMercatorParameters(
                a, f, originLatitude.radians, centralMeridian.radians,
                falseEasting, falseNorthing, scale
            )
            if (err == TMCoordConverter.TRANMERC_NO_ERROR.toLong()) err =
                converter.convertTransverseMercatorToGeodetic(easting, northing)

            require(!(err != TMCoordConverter.TRANMERC_NO_ERROR.toLong() && err != TMCoordConverter.TRANMERC_LON_WARNING.toLong())) { "TM Conversion Error" }

            return TMCoord(
                Angle.fromRadians(converter.latitude), Angle.fromRadians(converter.longitude),
                easting, northing, originLatitude, centralMeridian, falseEasting, falseNorthing, scale
            )
        }
    }
}
