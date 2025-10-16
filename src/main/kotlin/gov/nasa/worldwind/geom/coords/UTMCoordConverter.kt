/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords

import gov.nasa.worldwind.avlist.AVKey
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.geom.coords.TMCoord.Companion.fromLatLon
import gov.nasa.worldwind.geom.coords.TMCoord.Companion.fromTM

/**
 * Converter used to translate UTM coordinates to and from geodetic latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 * @see UTMCoord, TMCoordConverter
 */
/**
 * Ported to Java from the NGA GeoTrans utm.c and utm.h
 *
 * @author Garrett Headley, Patrick Murris
 */
internal class UTMCoordConverter {
    private var UTM_a = 6378137.0 /* Semi-major axis of ellipsoid in meters  */
    private var UTM_f = 1 / 298.257223563 /* Flattening of ellipsoid                 */
    private var UTM_Override: Long = 0 /* Zone override flag                      */

    /** @return Easting (X) in meters
     */
    var easting: Double = 0.0
        private set

    /** @return Northing (Y) in meters
     */
    var northing: Double = 0.0
        private set

    /**
     * @return The coordinate hemisphere, either [AVKey.NORTH] or [         ][AVKey.SOUTH].
     */
    var hemisphere: String? = null
        private set

    /** @return UTM zone
     */
    var zone: Int = 0
        private set

    /** @return Latitude in radians.
     */
    var latitude: Double = 0.0
        private set

    /** @return Longitude in radians.
     */
    var longitude: Double = 0.0
        private set

    /** @return Central_Meridian in radians.
     */
    var centralMeridian: Double = 0.0
        private set

    constructor()

    constructor(a: Double, f: Double) {
        setUTMParameters(a, f, 0)
    }

    /**
     * The function Set_UTM_Parameters receives the ellipsoid parameters and UTM zone override parameter as inputs, and
     * sets the corresponding state variables.  If any errors occur, the error code(s) are returned by the function,
     * otherwise UTM_NO_ERROR is returned.
     *
     * @param a        Semi-major axis of ellipsoid, in meters
     * @param f        Flattening of ellipsoid
     * @param override UTM override zone, zero indicates no override
     *
     * @return error code
     */
    private fun setUTMParameters(a: Double, f: Double, override: Long): Long {
        val inv_f = 1 / f
        var Error_Code = UTM_NO_ERROR.toLong()

        if (a <= 0.0) { /* Semi-major axis must be greater than zero */
            Error_Code = Error_Code or UTM_A_ERROR.toLong()
        }
        if ((inv_f < 250) || (inv_f > 350)) { /* Inverse flattening must be between 250 and 350 */
            Error_Code = Error_Code or UTM_INV_F_ERROR.toLong()
        }
        if ((override < 0) || (override > 60)) {
            Error_Code = Error_Code or UTM_ZONE_OVERRIDE_ERROR.toLong()
        }
        if (Error_Code == UTM_NO_ERROR.toLong()) { /* no errors */
            UTM_a = a
            UTM_f = f
            UTM_Override = override
        }
        return (Error_Code)
    }

    /**
     * The function Convert_Geodetic_To_UTM converts geodetic (latitude and longitude) coordinates to UTM projection
     * (zone, hemisphere, easting and northing) coordinates according to the current ellipsoid and UTM zone override
     * parameters.  If any errors occur, the error code(s) are returned by the function, otherwise UTM_NO_ERROR is
     * returned.
     *
     * @param Latitude  Latitude in radians
     * @param Longitude Longitude in radians
     *
     * @return error code
     */
    fun convertGeodeticToUTM(Latitude: Double, Longitude: Double): Long {
        var Longitude = Longitude
        val Lat_Degrees: Long
        val Long_Degrees: Long
        var temp_zone: Long
        var Error_Code = UTM_NO_ERROR.toLong()
        val Origin_Latitude = 0.0
        val False_Easting = 500000.0
        var False_Northing = 0.0
        val Scale = 0.9996

        if ((Latitude < MIN_LAT) || (Latitude > MAX_LAT)) { /* Latitude out of range */
            Error_Code = Error_Code or UTM_LAT_ERROR.toLong()
        }
        if ((Longitude < -PI) || (Longitude > (2 * PI))) { /* Longitude out of range */
            Error_Code = Error_Code or UTM_LON_ERROR.toLong()
        }
        if (Error_Code == UTM_NO_ERROR.toLong()) { /* no errors */
            if (Longitude < 0) Longitude += (2 * PI) + 1.0e-10
            Lat_Degrees = (Latitude * 180.0 / PI).toLong()
            Long_Degrees = (Longitude * 180.0 / PI).toLong()

            if (Longitude < PI) temp_zone = (31 + ((Longitude * 180.0 / PI) / 6.0)).toLong()
            else temp_zone = (((Longitude * 180.0 / PI) / 6.0) - 29).toLong()
            if (temp_zone > 60) temp_zone = 1
            /* UTM special cases */
            if ((Lat_Degrees > 55) && (Lat_Degrees < 64) && (Long_Degrees > -1) && (Long_Degrees < 3)) temp_zone = 31
            if ((Lat_Degrees > 55) && (Lat_Degrees < 64) && (Long_Degrees > 2) && (Long_Degrees < 12)) temp_zone = 32
            if ((Lat_Degrees > 71) && (Long_Degrees > -1) && (Long_Degrees < 9)) temp_zone = 31
            if ((Lat_Degrees > 71) && (Long_Degrees > 8) && (Long_Degrees < 21)) temp_zone = 33
            if ((Lat_Degrees > 71) && (Long_Degrees > 20) && (Long_Degrees < 33)) temp_zone = 35
            if ((Lat_Degrees > 71) && (Long_Degrees > 32) && (Long_Degrees < 42)) temp_zone = 37

            if (UTM_Override != 0L) {
                if ((temp_zone == 1L) && (UTM_Override == 60L)) temp_zone = UTM_Override
                else if ((temp_zone == 60L) && (UTM_Override == 1L)) temp_zone = UTM_Override
                else if (((temp_zone - 1) <= UTM_Override) && (UTM_Override <= (temp_zone + 1))) temp_zone =
                    UTM_Override
                else Error_Code = UTM_ZONE_OVERRIDE_ERROR.toLong()
            }
            if (Error_Code == UTM_NO_ERROR.toLong()) {
                if (temp_zone >= 31) this.centralMeridian = (6 * temp_zone - 183) * PI / 180.0
                else this.centralMeridian = (6 * temp_zone + 177) * PI / 180.0
                this.zone = temp_zone.toInt()
                if (Latitude < 0) {
                    False_Northing = 10000000.0
                    this.hemisphere = AVKey.SOUTH
                } else this.hemisphere = AVKey.NORTH

                try {
                    val TM = fromLatLon(
                        Angle.fromRadians(Latitude), Angle.fromRadians(Longitude),
                        this.UTM_a, this.UTM_f, Angle.fromRadians(Origin_Latitude),
                        Angle.fromRadians(this.centralMeridian), False_Easting, False_Northing, Scale
                    )
                    this.easting = TM.easting
                    this.northing = TM.northing

                    if ((this.easting < MIN_EASTING) || (this.easting > MAX_EASTING)) Error_Code =
                        UTM_EASTING_ERROR.toLong()
                    if ((this.northing < MIN_NORTHING) || (this.northing > MAX_NORTHING)) Error_Code =
                        Error_Code or UTM_NORTHING_ERROR.toLong()
                } catch (e: Exception) {
                    Error_Code = UTM_TM_ERROR.toLong()
                }
            }
        }
        return (Error_Code)
    }

    /**
     * The function Convert_UTM_To_Geodetic converts UTM projection (zone, hemisphere, easting and northing) coordinates
     * to geodetic(latitude and  longitude) coordinates, according to the current ellipsoid parameters.  If any errors
     * occur, the error code(s) are returned by the function, otherwise UTM_NO_ERROR is returned.
     *
     * @param Zone       UTM zone.
     * @param Hemisphere The coordinate hemisphere, either [AVKey.NORTH] or [                   ][AVKey.SOUTH].
     * @param Easting    Easting (X) in meters.
     * @param Northing   Northing (Y) in meters.
     *
     * @return error code.
     */
    fun convertUTMToGeodetic(Zone: Long, Hemisphere: String, Easting: Double, Northing: Double): Long {
        // TODO: arg checking
        var Error_Code = UTM_NO_ERROR.toLong()
        val Origin_Latitude = 0.0
        val False_Easting = 500000.0
        var False_Northing = 0.0
        val Scale = 0.9996

        if ((Zone < 1) || (Zone > 60)) Error_Code = Error_Code or UTM_ZONE_ERROR.toLong()
        if (Hemisphere != AVKey.SOUTH && Hemisphere != AVKey.NORTH) Error_Code =
            Error_Code or UTM_HEMISPHERE_ERROR.toLong()
        //        if ((Easting < MIN_EASTING) || (Easting > MAX_EASTING))    //removed check to enable reprojecting images
//            Error_Code |= UTM_EASTING_ERROR;                       //that extend into another zone
        if ((Northing < MIN_NORTHING) || (Northing > MAX_NORTHING)) Error_Code =
            Error_Code or UTM_NORTHING_ERROR.toLong()

        if (Error_Code == UTM_NO_ERROR.toLong()) { /* no errors */
            if (Zone >= 31) this.centralMeridian = ((6 * Zone - 183) * PI / 180.0 /*+ 0.00000005*/)
            else this.centralMeridian = ((6 * Zone + 177) * PI / 180.0 /*+ 0.00000005*/)
            if (Hemisphere == AVKey.SOUTH) False_Northing = 10000000.0
            try {
                val TM = fromTM(
                    Easting, Northing,
                    Angle.fromRadians(Origin_Latitude), Angle.fromRadians(this.centralMeridian),
                    False_Easting, False_Northing, Scale
                )
                this.latitude = TM.latitude.radians
                this.longitude = TM.longitude.radians

                if ((this.latitude < MIN_LAT) || (this.latitude > MAX_LAT)) { /* Latitude out of range */
                    Error_Code = Error_Code or UTM_NORTHING_ERROR.toLong()
                }
            } catch (e: Exception) {
                Error_Code = UTM_TM_ERROR.toLong()
            }
        }
        return (Error_Code)
    }

    companion object {
        const val UTM_NO_ERROR: Int = 0x0000
        const val UTM_LAT_ERROR: Int = 0x0001
        const val UTM_LON_ERROR: Int = 0x0002
        const val UTM_EASTING_ERROR: Int = 0x0004
        const val UTM_NORTHING_ERROR: Int = 0x0008
        const val UTM_ZONE_ERROR: Int = 0x0010
        const val UTM_HEMISPHERE_ERROR: Int = 0x0020
        const val UTM_ZONE_OVERRIDE_ERROR: Int = 0x0040
        const val UTM_A_ERROR: Int = 0x0080
        const val UTM_INV_F_ERROR: Int = 0x0100
        const val UTM_TM_ERROR: Int = 0x0200

        private const val PI = 3.14159265358979323

        //private final static double MIN_LAT = ((-80.5 * PI) / 180.0); /* -80.5 degrees in radians    */
        //private final static double MAX_LAT = ((84.5 * PI) / 180.0);  /* 84.5 degrees in radians     */
        private val MIN_LAT: Double = ((-82 * PI) / 180.0) /* -82 degrees in radians    */
        private val MAX_LAT: Double = ((86 * PI) / 180.0) /* 86 degrees in radians     */

        private const val MIN_EASTING = 100000
        private const val MAX_EASTING = 900000
        private const val MIN_NORTHING = 0
        private const val MAX_NORTHING = 10000000
    }
}
