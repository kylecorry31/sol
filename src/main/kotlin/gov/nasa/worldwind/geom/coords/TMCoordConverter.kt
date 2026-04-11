/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords

import kotlin.math.*

/**
 * Converter used to translate Transverse Mercator coordinates to and from geodetic latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 * @see TMCoord, UTMCoordConverter, MGRSCoordConverter
 */

/**
 * Ported to Java from the NGA GeoTrans code tranmerc.c and tranmerc.h
 *
 * @author Garrett Headley, Patrick Murris
 */
internal class TMCoordConverter {
    /* Ellipsoid Parameters, default to WGS 84  */
    var a: Double = 6378137.0 /* Semi-major axis of ellipsoid i meters */
        private set
    var f: Double = 1 / 298.257223563 /* Flattening of ellipsoid  */
        private set
    private var TranMerc_es = 0.0066943799901413800 /* Eccentricity (0.08181919084262188000) squared */
    private var TranMerc_ebs = 0.0067394967565869 /* Second Eccentricity squared */

    /* Transverse_Mercator projection Parameters */
    private var TranMerc_Origin_Lat = 0.0 /* Latitude of origin in radians */
    private var TranMerc_Origin_Long = 0.0 /* Longitude of origin in radians */
    private var TranMerc_False_Northing = 0.0 /* False northing in meters */
    private var TranMerc_False_Easting = 0.0 /* False easting in meters */
    private var TranMerc_Scale_Factor = 1.0 /* Scale factor  */

    /* Isometeric to geodetic latitude parameters, default to WGS 84 */
    private var TranMerc_ap = 6367449.1458008
    private var TranMerc_bp = 16038.508696861
    private var TranMerc_cp = 16.832613334334
    private var TranMerc_dp = 0.021984404273757
    private var TranMerc_ep = 3.1148371319283e-005

    /* Maximum variance for easting and northing values for WGS 84. */
    private var TranMerc_Delta_Easting = 40000000.0
    private var TranMerc_Delta_Northing = 40000000.0

    /** @return Easting/X at the center of the projection
     */
    var easting: Double = 0.0
        private set

    /** @return Northing/Y at the center of the projection
     */
    var northing: Double = 0.0
        private set

    /** @return Longitude in radians.
     */
    var longitude: Double = 0.0
        private set

    /** @return Latitude in radians.
     */
    var latitude: Double = 0.0
        private set

    /**
     * The function Set_Tranverse_Mercator_Parameters receives the ellipsoid parameters and Tranverse Mercator
     * projection parameters as inputs, and sets the corresponding state variables. If any errors occur, the error
     * code(s) are returned by the function, otherwise TRANMERC_NO_ERROR is returned.
     *
     * @param a                Semi-major axis of ellipsoid, in meters
     * @param f                Flattening of ellipsoid
     * @param Origin_Latitude  Latitude in radians at the origin of the projection
     * @param Central_Meridian Longitude in radians at the center of the projection
     * @param False_Easting    Easting/X at the center of the projection
     * @param False_Northing   Northing/Y at the center of the projection
     * @param Scale_Factor     Projection scale factor
     *
     * @return error code
     */
    fun setTransverseMercatorParameters(
        a: Double, f: Double, Origin_Latitude: Double,
        Central_Meridian: Double,
        False_Easting: Double, False_Northing: Double, Scale_Factor: Double
    ): Long {
        var Central_Meridian = Central_Meridian
        val tn: Double /* True Meridianal distance constant  */
        val tn2: Double
        val tn3: Double
        val tn4: Double
        val tn5: Double
        val TranMerc_b: Double /* Semi-minor axis of ellipsoid, in meters */
        val inv_f = 1 / f
        var Error_Code = TRANMERC_NO_ERROR.toLong()

        if (a <= 0.0) { /* Semi-major axis must be greater than zero */
            Error_Code = Error_Code or TRANMERC_A_ERROR.toLong()
        }
        if ((inv_f < 250) || (inv_f > 350)) { /* Inverse flattening must be between 250 and 350 */
            Error_Code = Error_Code or TRANMERC_INV_F_ERROR.toLong()
        }
        if ((Origin_Latitude < -MAX_LAT) || (Origin_Latitude > MAX_LAT)) { /* origin latitude out of range */
            Error_Code = Error_Code or TRANMERC_ORIGIN_LAT_ERROR.toLong()
        }
        if ((Central_Meridian < -PI) || (Central_Meridian > (2 * PI))) { /* origin longitude out of range */
            Error_Code = Error_Code or TRANMERC_CENT_MER_ERROR.toLong()
        }
        if ((Scale_Factor < MIN_SCALE_FACTOR) || (Scale_Factor > MAX_SCALE_FACTOR)) {
            Error_Code = Error_Code or TRANMERC_SCALE_FACTOR_ERROR.toLong()
        }
        if (Error_Code == TRANMERC_NO_ERROR.toLong()) { /* no errors */
            this.a = a
            this.f = f
            TranMerc_Origin_Lat = 0.0
            TranMerc_Origin_Long = 0.0
            TranMerc_False_Northing = 0.0
            TranMerc_False_Easting = 0.0
            TranMerc_Scale_Factor = 1.0

            /* Eccentricity Squared */
            TranMerc_es = 2 * this.f - this.f * this.f
            /* Second Eccentricity Squared */
            TranMerc_ebs = (1 / (1 - TranMerc_es)) - 1

            TranMerc_b = this.a * (1 - this.f)
            /*True meridianal constants  */
            tn = (this.a - TranMerc_b) / (this.a + TranMerc_b)
            tn2 = tn * tn
            tn3 = tn2 * tn
            tn4 = tn3 * tn
            tn5 = tn4 * tn

            TranMerc_ap = this.a * (1e0 - tn + 5e0 * (tn2 - tn3) / 4e0 + 81e0 * (tn4 - tn5) / 64e0)
            TranMerc_bp = 3e0 * this.a * (tn - tn2 + (7e0 * (tn3 - tn4)
                    / 8e0) + 55e0 * tn5 / 64e0) / 2e0
            TranMerc_cp = 15e0 * this.a * (tn2 - tn3 + 3e0 * (tn4 - tn5) / 4e0) / 16.0
            TranMerc_dp = 35e0 * this.a * (tn3 - tn4 + 11e0 * tn5 / 16e0) / 48e0
            TranMerc_ep = 315e0 * this.a * (tn4 - tn5) / 512e0

            convertGeodeticToTransverseMercator(MAX_LAT, MAX_DELTA_LONG)

            TranMerc_Delta_Easting = this.easting
            TranMerc_Delta_Northing = this.northing

            convertGeodeticToTransverseMercator(0.0, MAX_DELTA_LONG)
            TranMerc_Delta_Easting = this.easting

            TranMerc_Origin_Lat = Origin_Latitude
            if (Central_Meridian > PI) Central_Meridian -= (2 * PI)
            TranMerc_Origin_Long = Central_Meridian
            TranMerc_False_Northing = False_Northing
            TranMerc_False_Easting = False_Easting
            TranMerc_Scale_Factor = Scale_Factor
        }
        return (Error_Code)
    }

    /**
     * The function Convert_Geodetic_To_Transverse_Mercator converts geodetic (latitude and longitude) coordinates to
     * Transverse Mercator projection (easting and northing) coordinates, according to the current ellipsoid and
     * Transverse Mercator projection coordinates.  If any errors occur, the error code(s) are returned by the function,
     * otherwise TRANMERC_NO_ERROR is returned.
     *
     * @param Latitude  Latitude in radians
     * @param Longitude Longitude in radians
     *
     * @return error code
     */
    fun convertGeodeticToTransverseMercator(Latitude: Double, Longitude: Double): Long {
        var Longitude = Longitude
        val c: Double /* Cosine of latitude                          */
        val c2: Double
        val c3: Double
        val c5: Double
        val c7: Double
        var dlam: Double /* Delta longitude - Difference in Longitude       */
        val eta: Double /* constant - TranMerc_ebs *c *c                   */
        val eta2: Double
        val eta3: Double
        val eta4: Double
        val s: Double /* Sine of latitude                        */
        val sn: Double /* Radius of curvature in the prime vertical       */
        val t: Double /* Tangent of latitude                             */
        val tan2: Double
        val tan3: Double
        val tan4: Double
        val tan5: Double
        val tan6: Double
        val t1: Double /* Term in coordinate conversion formula - GP to Y */
        val t2: Double /* Term in coordinate conversion formula - GP to Y */
        val t3: Double /* Term in coordinate conversion formula - GP to Y */
        val t4: Double /* Term in coordinate conversion formula - GP to Y */
        val t5: Double /* Term in coordinate conversion formula - GP to Y */
        val t6: Double /* Term in coordinate conversion formula - GP to Y */
        val t7: Double /* Term in coordinate conversion formula - GP to Y */
        val t8: Double /* Term in coordinate conversion formula - GP to Y */
        val t9: Double /* Term in coordinate conversion formula - GP to Y */
        val tmd: Double /* True Meridional distance                        */
        val tmdo: Double /* True Meridional distance for latitude of origin */
        var Error_Code = TRANMERC_NO_ERROR.toLong()
        val temp_Origin: Double
        val temp_Long: Double

        if ((Latitude < -MAX_LAT) || (Latitude > MAX_LAT)) {  /* Latitude out of range */
            Error_Code = Error_Code or TRANMERC_LAT_ERROR.toLong()
        }
        if (Longitude > PI) Longitude -= (2 * PI)
        if ((Longitude < (TranMerc_Origin_Long - MAX_DELTA_LONG))
            || (Longitude > (TranMerc_Origin_Long + MAX_DELTA_LONG))
        ) {
            if (Longitude < 0) temp_Long = Longitude + 2 * PI
            else temp_Long = Longitude
            if (TranMerc_Origin_Long < 0) temp_Origin = TranMerc_Origin_Long + 2 * PI
            else temp_Origin = TranMerc_Origin_Long
            if ((temp_Long < (temp_Origin - MAX_DELTA_LONG))
                || (temp_Long > (temp_Origin + MAX_DELTA_LONG))
            ) Error_Code = Error_Code or TRANMERC_LON_ERROR.toLong()
        }
        if (Error_Code == TRANMERC_NO_ERROR.toLong()) { /* no errors */
            /*
             *  Delta Longitude
             */
            dlam = Longitude - TranMerc_Origin_Long

            if (abs(dlam) > (9.0 * PI / 180)) { /* Distortion will result if Longitude is more than 9 degrees from the Central Meridian */
                Error_Code = Error_Code or TRANMERC_LON_WARNING.toLong()
            }

            if (dlam > PI) dlam -= (2 * PI)
            if (dlam < -PI) dlam += (2 * PI)
            if (abs(dlam) < 2e-10) dlam = 0.0

            s = sin(Latitude)
            c = cos(Latitude)
            c2 = c * c
            c3 = c2 * c
            c5 = c3 * c2
            c7 = c5 * c2
            t = tan(Latitude)
            tan2 = t * t
            tan3 = tan2 * t
            tan4 = tan3 * t
            tan5 = tan4 * t
            tan6 = tan5 * t
            eta = TranMerc_ebs * c2
            eta2 = eta * eta
            eta3 = eta2 * eta
            eta4 = eta3 * eta

            /* radius of curvature in prime vertical */
            // sn = SPHSN(Latitude);
            sn = this.a / sqrt(1 - TranMerc_es * sin(Latitude).pow(2.0))

            /* True Meridianal Distances */
            // tmd = SPHTMD(Latitude);
            tmd = (TranMerc_ap * Latitude
                    - TranMerc_bp * sin(2.0 * Latitude)
                    + TranMerc_cp * sin(4.0 * Latitude)
                    - TranMerc_dp * sin(6.0 * Latitude)
                    + TranMerc_ep * sin(8.0 * Latitude))

            /*  Origin  */

            // tmdo = SPHTMD (TranMerc_Origin_Lat);
            tmdo = (TranMerc_ap * TranMerc_Origin_Lat
                    - TranMerc_bp * sin(2.0 * TranMerc_Origin_Lat)
                    + TranMerc_cp * sin(4.0 * TranMerc_Origin_Lat)
                    - TranMerc_dp * sin(6.0 * TranMerc_Origin_Lat)
                    + TranMerc_ep * sin(8.0 * TranMerc_Origin_Lat))

            /* northing */
            t1 = (tmd - tmdo) * TranMerc_Scale_Factor
            t2 = sn * s * c * TranMerc_Scale_Factor / 2e0
            t3 = sn * s * c3 * TranMerc_Scale_Factor * (5e0 - tan2 + 9e0 * eta + 4e0 * eta2) / 24e0

            t4 =
                sn * s * c5 * TranMerc_Scale_Factor * (((61e0 - 58e0 * tan2 + tan4 + 270e0 * eta - 330e0 * tan2 * eta) + 445e0 * eta2 + 324e0 * eta3 - 680e0 * tan2 * eta2 + 88e0 * eta4
                        ) - 600e0 * tan2 * eta3 - 192e0 * tan2 * eta4) / 720e0

            t5 = sn * s * c7 * TranMerc_Scale_Factor * (1385e0 - 3111e0 *
                    tan2 + 543e0 * tan4 - tan6) / 40320e0

            this.northing =
                (TranMerc_False_Northing + t1 + dlam.pow(2e0) * t2 + dlam.pow(4e0) * t3 + dlam.pow(6e0) * t4 + dlam.pow(
                    8e0
                ) * t5)

            /* Easting */
            t6 = sn * c * TranMerc_Scale_Factor
            t7 = sn * c3 * TranMerc_Scale_Factor * (1e0 - tan2 + eta) / 6e0
            t8 = sn * c5 * TranMerc_Scale_Factor * ((5e0 - 18e0 * tan2 + tan4
                    + 14e0 * eta) - 58e0 * tan2 * eta + 13e0 * eta2 + 4e0 * eta3 - 64e0 * tan2 * eta2 - 24e0 * tan2 * eta3) / 120e0
            t9 = sn * c7 * TranMerc_Scale_Factor * (61e0 - 479e0 * tan2
                    + 179e0 * tan4 - tan6) / 5040e0

            this.easting =
                (TranMerc_False_Easting + dlam * t6 + dlam.pow(3e0) * t7 + dlam.pow(5e0) * t8 + dlam.pow(7e0) * t9)
        }
        return (Error_Code)
    }

    /**
     * The function Convert_Transverse_Mercator_To_Geodetic converts Transverse Mercator projection (easting and
     * northing) coordinates to geodetic (latitude and longitude) coordinates, according to the current ellipsoid and
     * Transverse Mercator projection parameters.  If any errors occur, the error code(s) are returned by the function,
     * otherwise TRANMERC_NO_ERROR is returned.
     *
     * @param Easting  Easting/X in meters
     * @param Northing Northing/Y in meters
     *
     * @return error code
     */
    fun convertTransverseMercatorToGeodetic(Easting: Double, Northing: Double): Long {
        val c: Double /* Cosine of latitude                          */
        var de: Double /* Delta easting - Difference in Easting (Easting-Fe)    */
        val dlam: Double /* Delta longitude - Difference in Longitude       */
        val eta: Double /* constant - TranMerc_ebs *c *c                   */
        val eta2: Double
        val eta3: Double
        val eta4: Double
        var ftphi: Double /* Footpoint latitude                              */
        var i: Int /* Loop iterator                   */
        //double s;       /* Sine of latitude                        */
        val sn: Double /* Radius of curvature in the prime vertical       */
        var sr: Double /* Radius of curvature in the meridian             */
        val t: Double /* Tangent of latitude                             */
        val tan2: Double
        val tan4: Double
        var t10: Double /* Term in coordinate conversion formula - GP to Y */
        val t11: Double /* Term in coordinate conversion formula - GP to Y */
        val t12: Double /* Term in coordinate conversion formula - GP to Y */
        val t13: Double /* Term in coordinate conversion formula - GP to Y */
        val t14: Double /* Term in coordinate conversion formula - GP to Y */
        val t15: Double /* Term in coordinate conversion formula - GP to Y */
        val t16: Double /* Term in coordinate conversion formula - GP to Y */
        val t17: Double /* Term in coordinate conversion formula - GP to Y */
        val tmd: Double /* True Meridional distance                        */
        val tmdo: Double /* True Meridional distance for latitude of origin */
        var Error_Code = TRANMERC_NO_ERROR.toLong()

        if ((Easting < (TranMerc_False_Easting - TranMerc_Delta_Easting))
            || (Easting > (TranMerc_False_Easting + TranMerc_Delta_Easting))
        ) { /* Easting out of range  */
            Error_Code = Error_Code or TRANMERC_EASTING_ERROR.toLong()
        }
        if ((Northing < (TranMerc_False_Northing - TranMerc_Delta_Northing))
            || (Northing > (TranMerc_False_Northing + TranMerc_Delta_Northing))
        ) { /* Northing out of range */
            Error_Code = Error_Code or TRANMERC_NORTHING_ERROR.toLong()
        }

        if (Error_Code == TRANMERC_NO_ERROR.toLong()) {
            /* True Meridional Distances for latitude of origin */
            // tmdo = SPHTMD(TranMerc_Origin_Lat);
            tmdo = (TranMerc_ap * TranMerc_Origin_Lat
                    - TranMerc_bp * sin(2.0 * TranMerc_Origin_Lat)
                    + TranMerc_cp * sin(4.0 * TranMerc_Origin_Lat)
                    - TranMerc_dp * sin(6.0 * TranMerc_Origin_Lat)
                    + TranMerc_ep * sin(8.0 * TranMerc_Origin_Lat))

            /*  Origin  */
            tmd = tmdo + (Northing - TranMerc_False_Northing) / TranMerc_Scale_Factor

            /* First Estimate */
            //sr = SPHSR(0.e0);
            sr = this.a * (1e0 - TranMerc_es) / sqrt(1e0 - TranMerc_es * sin(0e0).pow(2.0)).pow(3.0)

            ftphi = tmd / sr

            i = 0
            while (i < 5) {
                // t10 = SPHTMD (ftphi);
                t10 = (TranMerc_ap * ftphi
                        - TranMerc_bp * sin(2.0 * ftphi)
                        + TranMerc_cp * sin(4.0 * ftphi)
                        - TranMerc_dp * sin(6.0 * ftphi)
                        + TranMerc_ep * sin(8.0 * ftphi))
                // sr = SPHSR(ftphi);
                sr = this.a * (1e0 - TranMerc_es) / sqrt(1e0 - TranMerc_es * sin(ftphi).pow(2.0)).pow(3.0)
                ftphi = ftphi + (tmd - t10) / sr
                i++
            }

            /* Radius of Curvature in the meridian */
            // sr = SPHSR(ftphi);
            sr = this.a * (1e0 - TranMerc_es) / sqrt(1e0 - TranMerc_es * sin(ftphi).pow(2.0)).pow(3.0)

            /* Radius of Curvature in the meridian */
            // sn = SPHSN(ftphi);
            sn = this.a / sqrt(1e0 - TranMerc_es * sin(ftphi).pow(2.0))

            /* Sine Cosine terms */
            //s = Math.sin(ftphi);
            c = cos(ftphi)

            /* Tangent Value  */
            t = tan(ftphi)
            tan2 = t * t
            tan4 = tan2 * tan2
            eta = TranMerc_ebs * c.pow(2.0)
            eta2 = eta * eta
            eta3 = eta2 * eta
            eta4 = eta3 * eta
            de = Easting - TranMerc_False_Easting
            if (abs(de) < 0.0001) de = 0.0

            /* Latitude */
            t10 = t / (2e0 * sr * sn * TranMerc_Scale_Factor.pow(2.0))
            t11 =
                t * (5e0 + 3e0 * tan2 + eta - 4e0 * eta.pow(2.0) - 9e0 * tan2 * eta) / ((24e0 * sr * sn.pow(3.0) * TranMerc_Scale_Factor.pow(
                    4.0
                )))
            t12 = (t * ((61e0 + 90e0 * tan2 + 46e0 * eta + 45e0 * tan4 - 252e0 * tan2 * eta - 3e0 * eta2 + 100e0
                    * eta3) - 66e0 * tan2 * eta2 - (90e0 * tan4
                    * eta) + 88e0 * eta4 + 225e0 * tan4 * eta2 + 84e0 * tan2 * eta3 - 192e0 * tan2 * eta4)
                    / (720e0 * sr * sn.pow(5.0) * TranMerc_Scale_Factor.pow(6.0)))
            t13 = t * (1385e0 + 3633e0 * tan2 + 4095e0 * tan4 + (1575e0
                    * t.pow(6.0))) / (40320e0 * sr * sn.pow(7.0) * TranMerc_Scale_Factor.pow(8.0))
            this.latitude = (ftphi - de.pow(2.0) * t10 + de.pow(4.0) * t11 - de.pow(6.0) * t12
                    + de.pow(8.0) * t13)

            t14 = 1e0 / (sn * c * TranMerc_Scale_Factor)

            t15 = (1e0 + 2e0 * tan2 + eta) / (6e0 * sn.pow(3.0) * c * TranMerc_Scale_Factor.pow(3.0))

            t16 = ((5e0 + 6e0 * eta + 28e0 * tan2 - 3e0 * eta2 + 8e0 * tan2 * eta + 24e0 * tan4 - 4e0
                    * eta3) + 4e0 * tan2 * eta2 + (24e0
                    * tan2 * eta3)) / ((120e0 * sn.pow(5.0) * c
                    * TranMerc_Scale_Factor.pow(5.0)))

            t17 = (61e0 + 662e0 * tan2 + 1320e0 * tan4 + (720e0
                    * t.pow(6.0))) / ((5040e0 * sn.pow(7.0) * c
                    * TranMerc_Scale_Factor.pow(7.0)))

            /* Difference in Longitude */
            dlam = de * t14 - de.pow(3.0) * t15 + de.pow(5.0) * t16 - de.pow(7.0) * t17

            /* Longitude */
            this.longitude = TranMerc_Origin_Long + dlam

            if (abs(this.latitude) > (90.0 * PI / 180.0)) Error_Code = Error_Code or TRANMERC_NORTHING_ERROR.toLong()

            if ((this.longitude) > (PI)) {
                this.longitude -= (2 * PI)
                if (abs(this.longitude) > PI) Error_Code = Error_Code or TRANMERC_EASTING_ERROR.toLong()
            }

            if (abs(dlam) > (9.0 * PI / 180) * cos(this.latitude)) { /* Distortion will result if Longitude is more than 9 degrees from the Central Meridian at the equator */
                /* and decreases to 0 degrees at the poles */
                /* As you move towards the poles, distortion will become more significant */
                Error_Code = Error_Code or TRANMERC_LON_WARNING.toLong()
            }

            if (this.latitude > 1.0e10) Error_Code = Error_Code or TRANMERC_LON_WARNING.toLong()
        }
        return (Error_Code)
    }

    companion object {
        const val TRANMERC_NO_ERROR: Int = 0x0000
        private const val TRANMERC_LAT_ERROR = 0x0001
        private const val TRANMERC_LON_ERROR = 0x0002
        const val TRANMERC_EASTING_ERROR: Int = 0x0004
        const val TRANMERC_NORTHING_ERROR: Int = 0x0008
        private const val TRANMERC_ORIGIN_LAT_ERROR = 0x0010
        private const val TRANMERC_CENT_MER_ERROR = 0x0020
        private const val TRANMERC_A_ERROR = 0x0040
        private const val TRANMERC_INV_F_ERROR = 0x0080
        private const val TRANMERC_SCALE_FACTOR_ERROR = 0x0100
        const val TRANMERC_LON_WARNING: Int = 0x0200

        private const val PI = 3.14159265358979323 /* PI     */
        val PI_OVER: Double = (PI / 2.0) /* PI over 2 */
        private val MAX_LAT: Double = ((PI * 89.99) / 180.0) /* 90 degrees in radians */
        private val MAX_DELTA_LONG: Double = ((PI * 90) / 180.0) /* 90 degrees in radians */
        private const val MIN_SCALE_FACTOR = 0.3
        private const val MAX_SCALE_FACTOR = 3.0
    }
} // end TMConverter class

