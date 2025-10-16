/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
/** */ /* RSC IDENTIFIER: POLAR STEREOGRAPHIC
 *
 *
 * ABSTRACT
 *
 *    This component provides conversions between geodetic (latitude and
 *    longitude) coordinates and Polar Stereographic (easting and northing)
 *    coordinates.
 *
 * ERROR HANDLING
 *
 *    This component checks parameters for valid values.  If an invalid
 *    value is found the error code is combined with the current error code
 *    using the bitwise or.  This combining allows multiple error codes to
 *    be returned. The possible error codes are:
 *
 *          POLAR_NO_ERROR           : No errors occurred in function
 *          POLAR_LAT_ERROR          : Latitude outside of valid range
 *                                      (-90 to 90 degrees)
 *          POLAR_LON_ERROR          : Longitude outside of valid range
 *                                      (-180 to 360 degrees)
 *          POLAR_ORIGIN_LAT_ERROR   : Latitude of true scale outside of valid
 *                                      range (-90 to 90 degrees)
 *          POLAR_ORIGIN_LON_ERROR   : Longitude down from pole outside of valid
 *                                      range (-180 to 360 degrees)
 *          POLAR_EASTING_ERROR      : Easting outside of valid range,
 *                                      depending on ellipsoid and
 *                                      projection parameters
 *          POLAR_NORTHING_ERROR     : Northing outside of valid range,
 *                                      depending on ellipsoid and
 *                                      projection parameters
 *          POLAR_RADIUS_ERROR       : Coordinates too far from pole,
 *                                      depending on ellipsoid and
 *                                      projection parameters
 *          POLAR_A_ERROR            : Semi-major axis less than or equal to zero
 *          POLAR_INV_F_ERROR        : Inverse flattening outside of valid range
 *								  	                  (250 to 350)
 *
 *
 * REUSE NOTES
 *
 *    POLAR STEREOGRAPHIC is intended for reuse by any application that
 *    performs a Polar Stereographic projection.
 *
 *
 * REFERENCES
 *
 *    Further information on POLAR STEREOGRAPHIC can be found in the
 *    Reuse Manual.
 *
 *
 *    POLAR STEREOGRAPHIC originated from :
 *                                U.S. Army Topographic Engineering Center
 *                                Geospatial Information Division
 *                                7701 Telegraph Road
 *                                Alexandria, VA  22310-3864
 *
 *
 * LICENSES
 *
 *    None apply to this component.
 *
 *
 * RESTRICTIONS
 *
 *    POLAR STEREOGRAPHIC has no restrictions.
 *
 *
 * ENVIRONMENT
 *
 *    POLAR STEREOGRAPHIC was tested and certified in the following
 *    environments:
 *
 *    1. Solaris 2.5 with GCC, version 2.8.1
 *    2. Window 95 with MS Visual C++, version 6
 *
 *
 * MODIFICATIONS
 *
 *    Date              Description
 *    ----              -----------
 *    06-11-95          Original Code
 *    03-01-97          Original Code
 *
 *
 */
package gov.nasa.worldwind.geom.coords

import kotlin.math.*

/**
 * Ported to Java from the NGA GeoTrans polarst.c and polarst.h code.
 *
 * @author Garrett Headley - Feb 12, 2007 4:48:11 PM
 * @version $Id$
 */
class PolarCoordConverter
internal constructor() {
    /* Ellipsoid Parameters, default to WGS 84  */
    private var Polar_a = 6378137.0 /* Semi-major axis of ellipsoid in meters  */
    private var Polar_f = 1 / 298.257223563 /* Flattening of ellipsoid  */
    private var es = 0.08181919084262188000 /* Eccentricity of ellipsoid    */
    private var es_OVER_2 = .040909595421311 /* es / 2.0 */
    private var Southern_Hemisphere = 0.0 /* Flag variable */
    private var mc = 1.0
    private var tc = 1.0
    private var e4 = 1.0033565552493
    private var Polar_a_mc = 6378137.0 /* Polar_a * mc */
    private var two_Polar_a = 12756274.0 /* 2.0 * Polar_a */

    /* Polar Stereographic projection Parameters */
    private var Polar_Origin_Lat: Double = ((PI * 90) / 180) /* Latitude of origin in radians */
    private var Polar_Origin_Long = 0.0 /* Longitude of origin in radians */
    private var Polar_False_Easting = 0.0 /* False easting in meters */
    private var Polar_False_Northing = 0.0 /* False northing in meters */

    /* Maximum variance for easting and northing values for WGS 84. */
    private var Polar_Delta_Easting = 12713601.0
    private var Polar_Delta_Northing = 12713601.0

    var easting: Double = 0.0
        private set
    var northing: Double = 0.0
        private set

    /**
     * @return Latitude in radians.
     */
    var latitude: Double = 0.0
        private set

    /**
     * @return Longitude in radians.
     */
    var longitude: Double = 0.0
        private set

    /**
     * The function setPolarStereographicParameters receives the ellipsoid parameters and Polar Stereograpic projection
     * parameters as inputs, and sets the corresponding state variables.  If any errors occur, error code(s) are
     * returned by the function, otherwise POLAR_NO_ERROR is returned.
     *
     * @param a                        Semi-major axis of ellipsoid, in meters
     * @param f                        Flattening of ellipsoid
     * @param Latitude_of_True_Scale   Latitude of true scale, in radians
     * @param Longitude_Down_from_Pole Longitude down from pole, in radians
     * @param False_Easting            Easting (X) at center of projection, in meters
     * @param False_Northing           Northing (Y) at center of projection, in meters
     * @return error code
     */
    fun setPolarStereographicParameters(
        a: Double, f: Double, Latitude_of_True_Scale: Double,
        Longitude_Down_from_Pole: Double, False_Easting: Double, False_Northing: Double
    ): Long {
        var Longitude_Down_from_Pole = Longitude_Down_from_Pole
        val es2: Double
        val slat: Double
        val clat: Double
        val essin: Double
        val one_PLUS_es: Double
        val one_MINUS_es: Double
        val pow_es: Double
        val inv_f = 1 / f
        val epsilon = 1.0e-2
        var Error_Code: Long = POLAR_NO_ERROR

        if (a <= 0.0) { /* Semi-major axis must be greater than zero */
            Error_Code = Error_Code or POLAR_A_ERROR
        }
        if ((inv_f < 250) || (inv_f > 350)) { /* Inverse flattening must be between 250 and 350 */
            Error_Code = Error_Code or POLAR_INV_F_ERROR
        }
        if ((Latitude_of_True_Scale < -PI_OVER_2) || (Latitude_of_True_Scale > PI_OVER_2)) { /* Origin Latitude out of range */
            Error_Code = Error_Code or POLAR_ORIGIN_LAT_ERROR
        }
        if ((Longitude_Down_from_Pole < -PI) || (Longitude_Down_from_Pole > TWO_PI)) { /* Origin Longitude out of range */
            Error_Code = Error_Code or POLAR_ORIGIN_LON_ERROR
        }

        if (Error_Code == POLAR_NO_ERROR) { /* no errors */

            Polar_a = a
            two_Polar_a = 2.0 * Polar_a
            Polar_f = f

            if (Longitude_Down_from_Pole > PI) Longitude_Down_from_Pole -= TWO_PI
            if (Latitude_of_True_Scale < 0) {
                Southern_Hemisphere = 1.0
                Polar_Origin_Lat = -Latitude_of_True_Scale
                Polar_Origin_Long = -Longitude_Down_from_Pole
            } else {
                Southern_Hemisphere = 0.0
                Polar_Origin_Lat = Latitude_of_True_Scale
                Polar_Origin_Long = Longitude_Down_from_Pole
            }
            Polar_False_Easting = False_Easting
            Polar_False_Northing = False_Northing

            es2 = 2 * Polar_f - Polar_f * Polar_f
            es = sqrt(es2)
            es_OVER_2 = es / 2.0

            if (abs(abs(Polar_Origin_Lat) - PI_OVER_2) > 1.0e-10) {
                slat = sin(Polar_Origin_Lat)
                essin = es * slat
                pow_es = ((1.0 - essin) / (1.0 + essin)).pow(es_OVER_2)
                clat = cos(Polar_Origin_Lat)
                mc = clat / sqrt(1.0 - essin * essin)
                Polar_a_mc = Polar_a * mc
                tc = tan(PI_Over_4 - Polar_Origin_Lat / 2.0) / pow_es
            } else {
                one_PLUS_es = 1.0 + es
                one_MINUS_es = 1.0 - es
                e4 = sqrt(one_PLUS_es.pow(one_PLUS_es) * one_MINUS_es.pow(one_MINUS_es))
            }
        }

        /* Calculate Radius */
        convertGeodeticToPolarStereographic(0.0, Polar_Origin_Long)

        Polar_Delta_Northing = this.northing * 2 // Increased range for accepted easting and northing values
        Polar_Delta_Northing = abs(Polar_Delta_Northing) + epsilon
        Polar_Delta_Easting = Polar_Delta_Northing

        return (Error_Code)
    }

    /**
     * The function Convert_Geodetic_To_Polar_Stereographic converts geodetic coordinates (latitude and longitude) to
     * Polar Stereographic coordinates (easting and northing), according to the current ellipsoid and Polar
     * Stereographic projection parameters. If any errors occur, error code(s) are returned by the function, otherwise
     * POLAR_NO_ERROR is returned.
     *
     * @param Latitude  latitude, in radians
     * @param Longitude Longitude, in radians
     * @return error code
     */
    fun convertGeodeticToPolarStereographic(Latitude: Double, Longitude: Double): Long {
        var Latitude = Latitude
        var Longitude = Longitude
        var dlam: Double
        val slat: Double
        val essin: Double
        val t: Double
        val rho: Double
        val pow_es: Double
        var Error_Code: Long = POLAR_NO_ERROR

        if ((Latitude < -PI_OVER_2) || (Latitude > PI_OVER_2)) {   /* Latitude out of range */
            Error_Code = Error_Code or POLAR_LAT_ERROR
        }
        if ((Latitude < 0) && (Southern_Hemisphere == 0.0)) {   /* Latitude and Origin Latitude in different hemispheres */
            Error_Code = Error_Code or POLAR_LAT_ERROR
        }
        if ((Latitude > 0) && (Southern_Hemisphere == 1.0)) {   /* Latitude and Origin Latitude in different hemispheres */
            Error_Code = Error_Code or POLAR_LAT_ERROR
        }
        if ((Longitude < -PI) || (Longitude > TWO_PI)) {  /* Longitude out of range */
            Error_Code = Error_Code or POLAR_LON_ERROR
        }

        if (Error_Code == POLAR_NO_ERROR) {  /* no errors */

            if (abs(abs(Latitude) - PI_OVER_2) < 1.0e-10) {
                this.easting = 0.0
                this.northing = 0.0
            } else {
                if (Southern_Hemisphere != 0.0) {
                    Longitude *= -1.0
                    Latitude *= -1.0
                }
                dlam = Longitude - Polar_Origin_Long
                if (dlam > PI) {
                    dlam -= TWO_PI
                }
                if (dlam < -PI) {
                    dlam += TWO_PI
                }
                slat = sin(Latitude)
                essin = es * slat
                pow_es = ((1.0 - essin) / (1.0 + essin)).pow(es_OVER_2)
                t = tan(PI_Over_4 - Latitude / 2.0) / pow_es

                if (abs(abs(Polar_Origin_Lat) - PI_OVER_2) > 1.0e-10) rho = Polar_a_mc * t / tc
                else rho = two_Polar_a * t / e4


                if (Southern_Hemisphere != 0.0) {
                    this.easting = -(rho * sin(dlam) - Polar_False_Easting)
                    //Easting *= -1.0;
                    this.northing = rho * cos(dlam) + Polar_False_Northing
                } else this.easting = rho * sin(dlam) + Polar_False_Easting
                this.northing = -rho * cos(dlam) + Polar_False_Northing
            }
        }
        return (Error_Code)
    }

    /**
     * The function Convert_Polar_Stereographic_To_Geodetic converts Polar
     * Stereographic coordinates (easting and northing) to geodetic
     * coordinates (latitude and longitude) according to the current ellipsoid
     * and Polar Stereographic projection Parameters. If any errors occur, the
     * code(s) are returned by the function, otherwise POLAR_NO_ERROR
     * is returned.
     *
     * @param Easting Easting (X), in meters
     * @param Northing Northing (Y), in meters
     * @return error code
     */
    fun convertPolarStereographicToGeodetic(Easting: Double, Northing: Double): Long {
        var dy = 0.0
        var dx = 0.0
        var rho = 0.0
        val t: Double
        var PHI: Double
        var sin_PHI: Double
        var tempPHI = 0.0
        var essin: Double
        var pow_es: Double
        val delta_radius: Double
        var Error_Code: Long = POLAR_NO_ERROR
        val min_easting = Polar_False_Easting - Polar_Delta_Easting
        val max_easting = Polar_False_Easting + Polar_Delta_Easting
        val min_northing = Polar_False_Northing - Polar_Delta_Northing
        val max_northing = Polar_False_Northing + Polar_Delta_Northing

        if (Easting > max_easting || Easting < min_easting) { /* Easting out of range */
            Error_Code = Error_Code or POLAR_EASTING_ERROR
        }
        if (Northing > max_northing || Northing < min_northing) { /* Northing out of range */
            Error_Code = Error_Code or POLAR_NORTHING_ERROR
        }

        if (Error_Code == POLAR_NO_ERROR) {
            dy = Northing - Polar_False_Northing
            dx = Easting - Polar_False_Easting

            /* Radius of point with origin of false easting, false northing */
            rho = sqrt(dx * dx + dy * dy)

            delta_radius = sqrt(Polar_Delta_Easting * Polar_Delta_Easting + Polar_Delta_Northing * Polar_Delta_Northing)

            if (rho > delta_radius) { /* Point is outside of projection area */
                Error_Code = Error_Code or POLAR_RADIUS_ERROR
            }
        }

        if (Error_Code == POLAR_NO_ERROR) { /* no errors */
            if ((dy == 0.0) && (dx == 0.0)) {
                this.latitude = PI_OVER_2
                this.longitude = Polar_Origin_Long
            } else {
                if (Southern_Hemisphere != 0.0) {
                    dy *= -1.0
                    dx *= -1.0
                }

                if (abs(abs(Polar_Origin_Lat) - PI_OVER_2) > 1.0e-10) t = rho * tc / (Polar_a_mc)
                else t = rho * e4 / (two_Polar_a)
                PHI = PI_OVER_2 - 2.0 * atan(t)
                while (abs(PHI - tempPHI) > 1.0e-10) {
                    tempPHI = PHI
                    sin_PHI = sin(PHI)
                    essin = es * sin_PHI
                    pow_es = ((1.0 - essin) / (1.0 + essin)).pow(es_OVER_2)
                    PHI = PI_OVER_2 - 2.0 * atan(t * pow_es)
                }
                this.latitude = PHI
                this.longitude = Polar_Origin_Long + atan2(dx, -dy)

                if (this.longitude > PI) this.longitude -= TWO_PI
                else if (this.longitude < -PI) this.longitude += TWO_PI


                if (this.latitude > PI_OVER_2)  /* force distorted values to 90, -90 degrees */
                    this.latitude = PI_OVER_2
                else if (this.latitude < -PI_OVER_2) this.latitude = -PI_OVER_2

                if (this.longitude > PI)  /* force distorted values to 180, -180 degrees */
                    this.longitude = PI
                else if (this.longitude < -PI) this.longitude = -PI
            }
            if (Southern_Hemisphere != 0.0) {
                this.latitude *= -1.0
                this.longitude *= -1.0
            }
        }
        return (Error_Code)
    }

    companion object {
        private const val POLAR_NO_ERROR: Long = 0x0000
        private const val POLAR_LAT_ERROR: Long = 0x0001
        private const val POLAR_LON_ERROR: Long = 0x0002
        private const val POLAR_ORIGIN_LAT_ERROR: Long = 0x0004
        private const val POLAR_ORIGIN_LON_ERROR: Long = 0x0008
        const val POLAR_EASTING_ERROR: Long = 0x0010
        const val POLAR_NORTHING_ERROR: Long = 0x0020
        private const val POLAR_A_ERROR: Long = 0x0040
        private const val POLAR_INV_F_ERROR: Long = 0x0080
        const val POLAR_RADIUS_ERROR: Long = 0x0100

        private const val PI = 3.14159265358979323
        private val PI_OVER_2: Double = PI / 2.0
        private val PI_Over_4: Double = PI / 4.0
        private val TWO_PI: Double = 2.0 * PI
    }
}

