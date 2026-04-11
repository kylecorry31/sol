/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
/** */ /* RSC IDENTIFIER: UPS
 *
 *
 * ABSTRACT
 *
 *    This component provides conversions between geodetic (latitude
 *    and longitude) coordinates and Universal Polar Stereographic (UPS)
 *    projection (hemisphere, easting, and northing) coordinates.
 *
 *
 * ERROR HANDLING
 *
 *    This component checks parameters for valid values.  If an
 *    invalid value is found the error code is combined with the
 *    current error code using the bitwise or.  This combining allows
 *    multiple error codes to be returned. The possible error codes
 *    are:
 *
 *         UPS_NO_ERROR           : No errors occurred in function
 *         UPS_LAT_ERROR          : Latitude outside of valid range
 *                                   (North Pole: 83.5 to 90,
 *                                    South Pole: -79.5 to -90)
 *         UPS_LON_ERROR          : Longitude outside of valid range
 *                                   (-180 to 360 degrees)
 *         UPS_HEMISPHERE_ERROR   : Invalid hemisphere ('N' or 'S')
 *         UPS_EASTING_ERROR      : Easting outside of valid range,
 *                                   (0 to 4,000,000m)
 *         UPS_NORTHING_ERROR     : Northing outside of valid range,
 *                                   (0 to 4,000,000m)
 *         UPS_A_ERROR            : Semi-major axis less than or equal to zero
 *         UPS_INV_F_ERROR        : Inverse flattening outside of valid range
 *								  	               (250 to 350)
 *
 *
 * REUSE NOTES
 *
 *    UPS is intended for reuse by any application that performs a Universal
 *    Polar Stereographic (UPS) projection.
 *
 *
 * REFERENCES
 *
 *    Further information on UPS can be found in the Reuse Manual.
 *
 *    UPS originated from :  U.S. Army Topographic Engineering Center
 *                           Geospatial Information Division
 *                           7701 Telegraph Road
 *                           Alexandria, VA  22310-3864
 *
 *
 * LICENSES
 *
 *    None apply to this component.
 *
 *
 * RESTRICTIONS
 *
 *    UPS has no restrictions.
 *
 *
 * ENVIRONMENT
 *
 *    UPS was tested and certified in the following environments:
 *
 *    1. Solaris 2.5 with GCC version 2.8.1
 *    2. Windows 95 with MS Visual C++ version 6
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

import gov.nasa.worldwind.avlist.AVKey

/**
 * Ported to Java from the NGA GeoTrans ups.c and ups.h code - Feb 12, 2007 4:52:59 PM
 *
 * @author Garrett Headley, Patrick Murris
 * @version $Id$
 */
class UPSCoordConverter
internal constructor() {
    private var UPS_Origin_Latitude: Double = MAX_ORIGIN_LAT /*set default = North Hemisphere */
    private val UPS_Origin_Longitude = 0.0

    /* Ellipsoid Parameters, default to WGS 84  */
    private val UPS_a = 6378137.0 /* Semi-major axis of ellipsoid in meters   */
    private val UPS_f = 1 / 298.257223563 /* Flattening of ellipsoid  */
    private val UPS_False_Easting = 2000000.0
    private val UPS_False_Northing = 2000000.0
    private val false_easting = 0.0
    private val false_northing = 0.0
    private var UPS_Easting = 0.0
    private var UPS_Northing = 0.0

    /** @return Easting/X in meters
     */
    var easting: Double = 0.0
        private set

    /** @return Northing/Y in meters
     */
    var northing: Double = 0.0
        private set

    /**
     * @return Hemisphere, either [AVKey.NORTH] or [         ][AVKey.SOUTH].
     */
    var hemisphere: String = AVKey.NORTH
        private set

    /** @return Latitude in radians.
     */
    var latitude: Double = 0.0
        private set

    /** @return Longitude in radians.
     */
    var longitude: Double = 0.0
        private set

    private val polarConverter = PolarCoordConverter()

    /**
     * The function convertGeodeticToUPS converts geodetic (latitude and longitude) coordinates to UPS (hemisphere,
     * easting, and northing) coordinates, according to the current ellipsoid parameters. If any errors occur, the error
     * code(s) are returned by the function, otherwide UPS_NO_ERROR is returned.
     *
     * @param latitude  Latitude in radians
     * @param longitude Longitude in radians
     *
     * @return error code
     */
    fun convertGeodeticToUPS(latitude: Double, longitude: Double): Long {
        if ((latitude < -MAX_LAT) || (latitude > MAX_LAT)) {   /* latitude out of range */
            return UPS_LAT_ERROR.toLong()
        }
        if ((latitude < 0) && (latitude > MIN_SOUTH_LAT)) return UPS_LAT_ERROR.toLong()
        if ((latitude >= 0) && (latitude < MIN_NORTH_LAT)) return UPS_LAT_ERROR.toLong()

        if ((longitude < -PI) || (longitude > (2 * PI))) {  /* slam out of range */
            return UPS_LON_ERROR.toLong()
        }

        if (latitude < 0) {
            UPS_Origin_Latitude = -MAX_ORIGIN_LAT
            this.hemisphere = AVKey.SOUTH
        } else {
            UPS_Origin_Latitude = MAX_ORIGIN_LAT
            this.hemisphere = AVKey.NORTH
        }

        polarConverter.setPolarStereographicParameters(
            UPS_a, UPS_f,
            UPS_Origin_Latitude, UPS_Origin_Longitude,
            false_easting, false_northing
        )

        polarConverter.convertGeodeticToPolarStereographic(latitude, longitude)

        UPS_Easting = UPS_False_Easting + polarConverter.easting
        UPS_Northing = UPS_False_Northing + polarConverter.northing
        if (AVKey.SOUTH == this.hemisphere) UPS_Northing = UPS_False_Northing - polarConverter.northing

        this.easting = UPS_Easting
        this.northing = UPS_Northing

        return UPS_NO_ERROR.toLong()
    }

    /**
     * The function Convert_UPS_To_Geodetic converts UPS (hemisphere, easting, and northing) coordinates to geodetic
     * (latitude and longitude) coordinates according to the current ellipsoid parameters.  If any errors occur, the
     * error code(s) are returned by the function, otherwise UPS_NO_ERROR is returned.
     *
     * @param Hemisphere Hemisphere, either [AVKey.NORTH] or [                   ][AVKey.SOUTH].
     * @param Easting    Easting/X in meters
     * @param Northing   Northing/Y in meters
     *
     * @return error code
     */
    fun convertUPSToGeodetic(Hemisphere: String?, Easting: Double, Northing: Double): Long {
        var Error_Code = UPS_NO_ERROR.toLong()

        if (AVKey.NORTH != Hemisphere && AVKey.SOUTH != Hemisphere) Error_Code =
            Error_Code or UPS_HEMISPHERE_ERROR.toLong()
        if ((Easting < MIN_EAST_NORTH) || (Easting > MAX_EAST_NORTH)) Error_Code =
            Error_Code or UPS_EASTING_ERROR.toLong()
        if ((Northing < MIN_EAST_NORTH) || (Northing > MAX_EAST_NORTH)) Error_Code =
            Error_Code or UPS_NORTHING_ERROR.toLong()

        if (AVKey.NORTH == Hemisphere) UPS_Origin_Latitude = MAX_ORIGIN_LAT
        if (AVKey.SOUTH == Hemisphere) UPS_Origin_Latitude = -MAX_ORIGIN_LAT

        if (Error_Code == UPS_NO_ERROR.toLong()) {   /*  no errors   */
            polarConverter.setPolarStereographicParameters(
                UPS_a,
                UPS_f,
                UPS_Origin_Latitude,
                UPS_Origin_Longitude,
                UPS_False_Easting,
                UPS_False_Northing
            )

            polarConverter.convertPolarStereographicToGeodetic(Easting, Northing)
            this.latitude = polarConverter.latitude
            this.longitude = polarConverter.longitude

            if ((this.latitude < 0) && (this.latitude > MIN_SOUTH_LAT)) Error_Code =
                Error_Code or UPS_LAT_ERROR.toLong()
            if ((this.latitude >= 0) && (this.latitude < MIN_NORTH_LAT)) Error_Code =
                Error_Code or UPS_LAT_ERROR.toLong()
        }
        return Error_Code
    }

    companion object {
        const val UPS_NO_ERROR: Int = 0x0000
        private const val UPS_LAT_ERROR = 0x0001
        private const val UPS_LON_ERROR = 0x0002
        const val UPS_HEMISPHERE_ERROR: Int = 0x0004
        const val UPS_EASTING_ERROR: Int = 0x0008
        const val UPS_NORTHING_ERROR: Int = 0x0010

        private const val PI = 3.14159265358979323
        private val MAX_LAT: Double = (PI * 90) / 180.0 // 90 degrees in radians

        // Min and max latitude values accepted
        private val MIN_NORTH_LAT: Double = 72 * PI / 180.0 // 83.5
        private val MIN_SOUTH_LAT: Double = -72 * PI / 180.0 // -79.5

        private val MAX_ORIGIN_LAT: Double = (81.114528 * PI) / 180.0
        private const val MIN_EAST_NORTH = 0.0
        private const val MAX_EAST_NORTH = 4000000.0
    }
}


