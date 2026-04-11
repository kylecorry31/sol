/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords

import gov.nasa.worldwind.avlist.AVKey
import gov.nasa.worldwind.geom.Angle
import kotlin.math.floor
import kotlin.math.pow

/**
 * Converter used to translate MGRS coordinate strings to and from geodetic latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 * @see MGRSCoordinateFormat
 */
/**
 * Ported to Java from the NGA GeoTrans mgrs.c and mgrs.h code. Contains routines to convert from Geodetic to MGRS and
 * the other direction.
 *
 * @author Garrett Headley, Patrick Murris
 */
internal class MGRSCoordConverter {
    private val MGRS_Ellipsoid_Code = "WE"

    /** @return converted MGRS string
     */
    var mgrsString: String = ""
        private set
    private var ltr2_low_value: Long = 0
    private var ltr2_high_value: Long = 0 // this is only used for doing MGRS to xxx conversions.
    private var false_northing = 0.0

    /** @return Latitude band letter
     */
    private var lastLetter: Long = 0

    /**
     * Get the last error code.
     *
     * @return the last error code.
     */
    var error: Long = MGRS_NO_ERROR.toLong()
        private set
    private var north = 0.0
    private var south = 0.0
    private var min_northing = 0.0
    private var northing_offset = 0.0 //smithjl added north_offset
    var latitude: Double = 0.0
        private set
    var longitude: Double = 0.0
        private set

    private class MGRSComponents
        (
        val zone: Int,
        val latitudeBand: Int,
        val squareLetter1: Int,
        val squareLetter2: Int,
        val easting: Double,
        val northing: Double,
        val precision: Int
    ) {
        override fun toString(): String {
            return "MGRS: " + zone + " " +
                    alphabet.get(latitudeBand) + " " +
                    alphabet.get(squareLetter1) + alphabet.get(squareLetter2) + " " +
                    easting + " " +
                    northing + " " +
                    "(" + precision + ")"
        }
    }

    /**
     * The function ConvertMGRSToGeodetic converts an MGRS coordinate string to Geodetic (latitude and longitude)
     * coordinates according to the current ellipsoid parameters.  If any errors occur, the error code(s) are returned
     * by the function, otherwise UTM_NO_ERROR is returned.
     *
     * @param MGRSString MGRS coordinate string.
     *
     * @return the error code.
     */
    fun convertMGRSToGeodetic(MGRSString: String): Long {
        latitude = 0.0
        longitude = 0.0
        var error_code = checkZone(MGRSString)
        if (error_code == MGRS_NO_ERROR.toLong()) {
            val UTM = convertMGRSToUTM(MGRSString)
            if (UTM != null) {
                latitude = UTM.latitude.radians
                longitude = UTM.longitude.radians
            } else error_code = MGRS_UTM_ERROR.toLong()
        } else if (error_code == MGRS_NOZONE_WARNING.toLong()) {
            val UPS = convertMGRSToUPS(MGRSString)
            if (UPS != null) {
                latitude = UPS.latitude.radians
                longitude = UPS.longitude.radians
            } else error_code = MGRS_UPS_ERROR.toLong()
        }
        return (error_code)
    }

    /**
     * The function Break_MGRS_String breaks down an MGRS coordinate string into its component parts. Updates
     * last_error.
     *
     * @param MGRSString the MGRS coordinate string
     *
     * @return the corresponding `MGRSComponents` or `null`.
     */
    private fun breakMGRSString(MGRSString: String): MGRSComponents? {
        var num_digits: Int
        val num_letters: Int
        var i = 0
        var j = 0
        var error_code = MGRS_NO_ERROR.toLong()

        var zone = 0
        val letters = IntArray(3)
        var easting: Long = 0
        var northing: Long = 0
        var precision = 0

        while (i < MGRSString.length && MGRSString.get(i) == ' ') {
            i++ /* skip any leading blanks */
        }
        j = i
        while (i < MGRSString.length && Character.isDigit(MGRSString.get(i))) {
            i++
        }
        num_digits = i - j
        if (num_digits <= 2) if (num_digits > 0) {
            /* get zone */
            zone = MGRSString.substring(j, i).toInt()
            if ((zone < 1) || (zone > 60)) error_code = error_code or MGRS_STRING_ERROR.toLong()
        } else error_code = error_code or MGRS_STRING_ERROR.toLong()
        j = i

        while (i < MGRSString.length && Character.isLetter(MGRSString.get(i))) {
            i++
        }
        num_letters = i - j
        if (num_letters == 3) {
            /* get letters */
            letters[0] = alphabet.indexOf(MGRSString.get(j).uppercaseChar())
            if ((letters[0] == LETTER_I) || (letters[0] == LETTER_O)) error_code =
                error_code or MGRS_STRING_ERROR.toLong()
            letters[1] = alphabet.indexOf(MGRSString.get(j + 1).uppercaseChar())
            if ((letters[1] == LETTER_I) || (letters[1] == LETTER_O)) error_code =
                error_code or MGRS_STRING_ERROR.toLong()
            letters[2] = alphabet.indexOf(MGRSString.get(j + 2).uppercaseChar())
            if ((letters[2] == LETTER_I) || (letters[2] == LETTER_O)) error_code =
                error_code or MGRS_STRING_ERROR.toLong()
        } else error_code = error_code or MGRS_STRING_ERROR.toLong()
        j = i
        while (i < MGRSString.length && Character.isDigit(MGRSString.get(i))) {
            i++
        }
        num_digits = i - j
        if ((num_digits <= 10) && (num_digits % 2 == 0)) {
            /* get easting, northing and precision */
            val n: Int
            val multiplier: Double
            /* get easting & northing */
            n = num_digits / 2
            precision = n
            if (n > 0) {
                easting = MGRSString.substring(j, j + n).toInt().toLong()
                northing = MGRSString.substring(j + n, j + n + n).toInt().toLong()
                multiplier = 10.0.pow((5 - n).toDouble())
                easting = (easting * multiplier).toLong()
                northing = (northing * multiplier).toLong()
            } else {
                easting = 0
                northing = 0
            }
        } else error_code = error_code or MGRS_STRING_ERROR.toLong()

        this.error = error_code
        if (error_code == MGRS_NO_ERROR.toLong()) return MGRSComponents(
            zone,
            letters[0],
            letters[1],
            letters[2],
            easting.toDouble(),
            northing.toDouble(),
            precision
        )

        return null
    }

    /**
     * The function Check_Zone receives an MGRS coordinate string. If a zone is given, MGRS_NO_ERROR is returned.
     * Otherwise, MGRS_NOZONE_WARNING. is returned.
     *
     * @param MGRSString the MGRS coordinate string.
     *
     * @return the error code.
     */
    private fun checkZone(MGRSString: String): Long {
        var i = 0
        var j = 0
        var num_digits = 0
        var error_code = MGRS_NO_ERROR.toLong()

        /* skip any leading blanks */
        while (i < MGRSString.length && MGRSString.get(i) == ' ') {
            i++
        }
        j = i
        while (i < MGRSString.length && Character.isDigit(MGRSString.get(i))) {
            i++
        }
        num_digits = i - j
        if (num_digits > 2) error_code = error_code or MGRS_STRING_ERROR.toLong()
        else if (num_digits <= 0) error_code = error_code or MGRS_NOZONE_WARNING.toLong()

        return error_code
    }

    /**
     * The function Get_Latitude_Band_Min_Northing receives a latitude band letter and uses the Latitude_Band_Table to
     * determine the minimum northing for that latitude band letter. Updates min_northing.
     *
     * @param letter Latitude band letter.
     *
     * @return the error code.
     */
    private fun getLatitudeBandMinNorthing(letter: Int): Long {
        var error_code = MGRS_NO_ERROR.toLong()

        if ((letter >= LETTER_C) && (letter <= LETTER_H)) {
            min_northing = latitudeBandConstants[letter - 2]!![1]
            northing_offset = latitudeBandConstants[letter - 2]!![4] //smithjl
        } else if ((letter >= LETTER_J) && (letter <= LETTER_N)) {
            min_northing = latitudeBandConstants[letter - 3]!![1]
            northing_offset = latitudeBandConstants[letter - 3]!![4] //smithjl
        } else if ((letter >= LETTER_P) && (letter <= LETTER_X)) {
            min_northing = latitudeBandConstants[letter - 4]!![1]
            northing_offset = latitudeBandConstants[letter - 4]!![4] //smithjl
        } else error_code = error_code or MGRS_STRING_ERROR.toLong()
        return error_code
    }

    /**
     * The function Get_Latitude_Range receives a latitude band letter and uses the Latitude_Band_Table to determine the
     * latitude band boundaries for that latitude band letter. Updates north and south.
     *
     * @param letter the Latitude band letter
     *
     * @return the error code.
     */
    private fun getLatitudeRange(letter: Int): Long {
        var error_code = MGRS_NO_ERROR.toLong()

        if ((letter >= LETTER_C) && (letter <= LETTER_H)) {
            north = latitudeBandConstants[letter - 2]!![2] * DEG_TO_RAD
            south = latitudeBandConstants[letter - 2]!![3] * DEG_TO_RAD
        } else if ((letter >= LETTER_J) && (letter <= LETTER_N)) {
            north = latitudeBandConstants[letter - 3]!![2] * DEG_TO_RAD
            south = latitudeBandConstants[letter - 3]!![3] * DEG_TO_RAD
        } else if ((letter >= LETTER_P) && (letter <= LETTER_X)) {
            north = latitudeBandConstants[letter - 4]!![2] * DEG_TO_RAD
            south = latitudeBandConstants[letter - 4]!![3] * DEG_TO_RAD
        } else error_code = error_code or MGRS_STRING_ERROR.toLong()

        return error_code
    }

    /**
     * The function convertMGRSToUTM converts an MGRS coordinate string to UTM projection (zone, hemisphere, easting and
     * northing) coordinates according to the current ellipsoid parameters.  Updates last_error if any errors occured.
     *
     * @param MGRSString the MGRS coordinate string
     *
     * @return the corresponding `UTMComponents` or `null`.
     */
    private fun convertMGRSToUTM(MGRSString: String): UTMCoord? {
        var grid_easting: Double /* Easting for 100,000 meter grid square      */
        var grid_northing: Double /* Northing for 100,000 meter grid square     */
        var latitude = 0.0
        var divisor = 1.0
        var error_code = MGRS_NO_ERROR.toLong()

        var hemisphere = AVKey.NORTH
        var easting = 0.0
        var northing = 0.0
        var UTM: UTMCoord? = null

        val MGRS = breakMGRSString(MGRSString)
        if (MGRS == null) error_code = error_code or MGRS_STRING_ERROR.toLong()
        else {
            if (error_code == MGRS_NO_ERROR.toLong()) {
                if ((MGRS.latitudeBand == LETTER_X) && ((MGRS.zone == 32) || (MGRS.zone == 34) || (MGRS.zone == 36))) error_code =
                    error_code or MGRS_STRING_ERROR.toLong()
                else {
                    if (MGRS.latitudeBand < LETTER_N) hemisphere = AVKey.SOUTH
                    else hemisphere = AVKey.NORTH

                    getGridValues(MGRS.zone.toLong())

                    // Check that the second letter of the MGRS string is within
                    // the range of valid second letter values
                    // Also check that the third letter is valid
                    if ((MGRS.squareLetter1 < ltr2_low_value) || (MGRS.squareLetter1 > ltr2_high_value) ||
                        (MGRS.squareLetter2 > LETTER_V)
                    ) error_code = error_code or MGRS_STRING_ERROR.toLong()

                    if (error_code == MGRS_NO_ERROR.toLong()) {
                        grid_northing =
                            (MGRS.squareLetter2).toDouble() * ONEHT //   smithjl  commented out + false_northing;
                        grid_easting = ((MGRS.squareLetter1) - ltr2_low_value + 1).toDouble() * ONEHT
                        if ((ltr2_low_value == LETTER_J.toLong()) && (MGRS.squareLetter1 > LETTER_O)) grid_easting -= ONEHT

                        if (MGRS.squareLetter2 > LETTER_O) grid_northing -= ONEHT

                        if (MGRS.squareLetter2 > LETTER_I) grid_northing -= ONEHT

                        if (grid_northing >= TWOMIL) grid_northing -= TWOMIL

                        error_code = getLatitudeBandMinNorthing(MGRS.latitudeBand)
                        if (error_code == MGRS_NO_ERROR.toLong()) {
                            /*smithjl Deleted code here and added this*/
                            grid_northing -= false_northing

                            if (grid_northing < 0.0) grid_northing += TWOMIL

                            grid_northing += northing_offset

                            if (grid_northing < min_northing) grid_northing += TWOMIL

                            /* smithjl End of added code */
                            easting = grid_easting + MGRS.easting
                            northing = grid_northing + MGRS.northing

                            try {
                                UTM = UTMCoord.fromUTM(MGRS.zone, hemisphere, easting, northing)
                                latitude = UTM.latitude.radians
                                divisor = 10.0.pow(MGRS.precision.toDouble())
                                error_code = getLatitudeRange(MGRS.latitudeBand)
                                if (error_code == MGRS_NO_ERROR.toLong()) {
                                    if (!(((south - DEG_TO_RAD / divisor) <= latitude)
                                                && (latitude <= (north + DEG_TO_RAD / divisor)))
                                    ) error_code = error_code or MGRS_LAT_WARNING.toLong()
                                }
                            } catch (e: Exception) {
                                error_code = MGRS_UTM_ERROR.toLong()
                            }
                        }
                    }
                }
            }
        }

        this.error = error_code
        if (error_code == MGRS_NO_ERROR.toLong() || error_code == MGRS_LAT_WARNING.toLong()) return UTM

        return null
    } /* Convert_MGRS_To_UTM */

    /**
     * The function convertGeodeticToMGRS converts Geodetic (latitude and longitude) coordinates to an MGRS coordinate
     * string, according to the current ellipsoid parameters.  If any errors occur, the error code(s) are returned by
     * the function, otherwise MGRS_NO_ERROR is returned.
     *
     * @param latitude  Latitude in radians
     * @param longitude Longitude in radian
     * @param precision Precision level of MGRS string
     *
     * @return error code
     */
    fun convertGeodeticToMGRS(latitude: Double, longitude: Double, precision: Int): Long {
        this.mgrsString = ""

        var error_code = MGRS_NO_ERROR.toLong()
        if ((latitude < -PI_OVER_2) || (latitude > PI_OVER_2)) { /* Latitude out of range */
            error_code = MGRS_LAT_ERROR.toLong()
        }

        if ((longitude < -PI) || (longitude > (2 * PI))) { /* Longitude out of range */
            error_code = MGRS_LON_ERROR.toLong()
        }

        if ((precision < 0) || (precision > MAX_PRECISION)) error_code = MGRS_PRECISION_ERROR.toLong()

        if (error_code == MGRS_NO_ERROR.toLong()) {
            if ((latitude < MIN_UTM_LAT) || (latitude > MAX_UTM_LAT)) {
                try {
                    val UPS =
                        UPSCoord.fromLatLon(Angle.fromRadians(latitude), Angle.fromRadians(longitude))
                    error_code = error_code or convertUPSToMGRS(
                        UPS.hemisphere, UPS.easting,
                        UPS.northing, precision.toLong()
                    )
                } catch (e: Exception) {
                    error_code = MGRS_UPS_ERROR.toLong()
                }
            } else {
                try {
                    val UTM =
                        UTMCoord.fromLatLon(Angle.fromRadians(latitude), Angle.fromRadians(longitude))
                    error_code = error_code or convertUTMToMGRS(
                        UTM.zone.toLong(), latitude, UTM.easting,
                        UTM.northing, precision.toLong()
                    )
                } catch (e: Exception) {
                    error_code = MGRS_UTM_ERROR.toLong()
                }
            }
        }

        return error_code
    }

    /**
     * The function Convert_UPS_To_MGRS converts UPS (hemisphere, easting, and northing) coordinates to an MGRS
     * coordinate string according to the current ellipsoid parameters.  If any errors occur, the error code(s) are
     * returned by the function, otherwise MGRS_NO_ERROR is returned.
     *
     * @param Hemisphere Hemisphere either, [AVKey.NORTH] or [                   ][AVKey.SOUTH].
     * @param Easting    Easting/X in meters
     * @param Northing   Northing/Y in meters
     * @param Precision  Precision level of MGRS string
     *
     * @return error value
     */
    private fun convertUPSToMGRS(Hemisphere: String?, Easting: Double, Northing: Double, Precision: Long): Long {
        var Easting = Easting
        var Northing = Northing
        val false_easting: Double /* False easting for 2nd letter                 */
        val false_northing: Double /* False northing for 3rd letter                */
        var grid_easting: Double /* Easting used to derive 2nd letter of MGRS    */
        var grid_northing: Double /* Northing used to derive 3rd letter of MGRS   */
        val ltr2_low_value: Int /* 2nd letter range - low number                */
        val letters = LongArray(MGRS_LETTERS) /* Number location of 3 letters in alphabet     */
        val divisor: Double
        val index: Int
        var error_code = MGRS_NO_ERROR.toLong()

        if (AVKey.NORTH != Hemisphere && AVKey.SOUTH != Hemisphere) error_code =
            error_code or MGRS_HEMISPHERE_ERROR.toLong()
        if ((Easting < MIN_EAST_NORTH) || (Easting > MAX_EAST_NORTH)) error_code =
            error_code or MGRS_EASTING_ERROR.toLong()
        if ((Northing < MIN_EAST_NORTH) || (Northing > MAX_EAST_NORTH)) error_code =
            error_code or MGRS_NORTHING_ERROR.toLong()
        if ((Precision < 0) || (Precision > MAX_PRECISION)) error_code = error_code or MGRS_PRECISION_ERROR.toLong()

        if (error_code == MGRS_NO_ERROR.toLong()) {
            divisor = 10.0.pow((5 - Precision).toDouble())
            Easting = roundMGRS(Easting / divisor) * divisor
            Northing = roundMGRS(Northing / divisor) * divisor

            if (AVKey.NORTH == Hemisphere) {
                if (Easting >= TWOMIL) letters[0] = LETTER_Z.toLong()
                else letters[0] = LETTER_Y.toLong()

                index = letters[0].toInt() - 22
                //                ltr2_low_value = UPS_Constant_Table.get(index).ltr2_low_value;
//                false_easting = UPS_Constant_Table.get(index).false_easting;
//                false_northing = UPS_Constant_Table.get(index).false_northing;
                ltr2_low_value = upsConstants[index]!![1].toInt()
                false_easting = upsConstants[index]!![4].toDouble()
                false_northing = upsConstants[index]!![5].toDouble()
            } else  // AVKey.SOUTH.equals(Hemisphere)
            {
                if (Easting >= TWOMIL) letters[0] = LETTER_B.toLong()
                else letters[0] = LETTER_A.toLong()

                //                ltr2_low_value = UPS_Constant_Table.get((int) letters[0]).ltr2_low_value;
//                false_easting = UPS_Constant_Table.get((int) letters[0]).false_easting;
//                false_northing = UPS_Constant_Table.get((int) letters[0]).false_northing;
                ltr2_low_value = upsConstants[letters[0].toInt()]!![1].toInt()
                false_easting = upsConstants[letters[0].toInt()]!![4].toDouble()
                false_northing = upsConstants[letters[0].toInt()]!![5].toDouble()
            }

            grid_northing = Northing
            grid_northing -= false_northing
            letters[2] = (grid_northing / ONEHT).toInt().toLong()

            if (letters[2] > LETTER_H) letters[2] = letters[2] + 1

            if (letters[2] > LETTER_N) letters[2] = letters[2] + 1

            grid_easting = Easting
            grid_easting -= false_easting
            letters[1] = (ltr2_low_value + ((grid_easting / ONEHT).toInt())).toLong()

            if (Easting < TWOMIL) {
                if (letters[1] > LETTER_L) letters[1] = letters[1] + 3

                if (letters[1] > LETTER_U) letters[1] = letters[1] + 2
            } else {
                if (letters[1] > LETTER_C) letters[1] = letters[1] + 2

                if (letters[1] > LETTER_H) letters[1] = letters[1] + 1

                if (letters[1] > LETTER_L) letters[1] = letters[1] + 3
            }

            makeMGRSString(0, letters, Easting, Northing, Precision)
        }
        return (error_code)
    }

    /**
     * The function UTM_To_MGRS calculates an MGRS coordinate string based on the zone, latitude, easting and northing.
     *
     * @param Zone      Zone number
     * @param Latitude  Latitude in radians
     * @param Easting   Easting
     * @param Northing  Northing
     * @param Precision Precision
     *
     * @return error code
     */
    private fun convertUTMToMGRS(
        Zone: Long,
        Latitude: Double,
        Easting: Double,
        Northing: Double,
        Precision: Long
    ): Long {
        var Easting = Easting
        var Northing = Northing
        var grid_easting: Double /* Easting used to derive 2nd letter of MGRS   */
        var grid_northing: Double /* Northing used to derive 3rd letter of MGRS  */
        val letters = LongArray(MGRS_LETTERS) /* Number location of 3 letters in alphabet    */
        val divisor: Double
        val error_code: Long

        /* Round easting and northing values */
        divisor = 10.0.pow((5 - Precision).toDouble())
        Easting = roundMGRS(Easting / divisor) * divisor
        Northing = roundMGRS(Northing / divisor) * divisor

        getGridValues(Zone)

        error_code = getLatitudeLetter(Latitude)
        letters[0] = this.lastLetter

        if (error_code == MGRS_NO_ERROR.toLong()) {
            grid_northing = Northing
            if (grid_northing == 1e7) grid_northing = grid_northing - 1.0

            while (grid_northing >= TWOMIL) {
                grid_northing = grid_northing - TWOMIL
            }
            grid_northing = grid_northing + false_northing //smithjl

            if (grid_northing >= TWOMIL)  //smithjl
                grid_northing = grid_northing - TWOMIL //smithjl


            letters[2] = (grid_northing / ONEHT).toLong()
            if (letters[2] > LETTER_H) letters[2] = letters[2] + 1

            if (letters[2] > LETTER_N) letters[2] = letters[2] + 1

            grid_easting = Easting
            if (((letters[0] == LETTER_V.toLong()) && (Zone == 31L)) && (grid_easting == 500000.0)) grid_easting =
                grid_easting - 1.0 /* SUBTRACT 1 METER */

            letters[1] = ltr2_low_value + ((grid_easting / ONEHT).toLong() - 1)
            if ((ltr2_low_value == LETTER_J.toLong()) && (letters[1] > LETTER_N)) letters[1] = letters[1] + 1

            makeMGRSString(Zone, letters, Easting, Northing, Precision)
        }
        return error_code
    }

    /**
     * The function Get_Grid_Values sets the letter range used for the 2nd letter in the MGRS coordinate string, based
     * on the set number of the utm zone. It also sets the false northing using a value of A for the second letter of
     * the grid square, based on the grid pattern and set number of the utm zone.
     *
     *
     * Key values that are set in this function include:  ltr2_low_value, ltr2_high_value, and false_northing.
     *
     * @param zone Zone number
     */
    private fun getGridValues(zone: Long) {
        var set_number: Long /* Set number (1-6) based on UTM zone number */
        val aa_pattern: Long /* Pattern based on ellipsoid code */

        set_number = zone % 6

        if (set_number == 0L) set_number = 6

        if (MGRS_Ellipsoid_Code.compareTo(CLARKE_1866) == 0 || MGRS_Ellipsoid_Code.compareTo(CLARKE_1880) == 0 || MGRS_Ellipsoid_Code.compareTo(
                BESSEL_1841
            ) == 0 || MGRS_Ellipsoid_Code.compareTo(BESSEL_1841_NAMIBIA) == 0
        ) aa_pattern = 0L
        else aa_pattern = 1L

        if ((set_number == 1L) || (set_number == 4L)) {
            ltr2_low_value = LETTER_A.toLong()
            ltr2_high_value = LETTER_H.toLong()
        } else if ((set_number == 2L) || (set_number == 5L)) {
            ltr2_low_value = LETTER_J.toLong()
            ltr2_high_value = LETTER_R.toLong()
        } else if ((set_number == 3L) || (set_number == 6L)) {
            ltr2_low_value = LETTER_S.toLong()
            ltr2_high_value = LETTER_Z.toLong()
        }

        /* False northing at A for second letter of grid square */
        if (aa_pattern == 1L) {
            if ((set_number % 2) == 0L) false_northing = 500000.0 //smithjl was 1500000
            else false_northing = 0.0
        } else {
            if ((set_number % 2) == 0L) false_northing = 1500000.0 //smithjl was 500000
            else false_northing = 1000000.00
        }
    }

    /**
     * The function Get_Latitude_Letter receives a latitude value and uses the Latitude_Band_Table to determine the
     * latitude band letter for that latitude.
     *
     * @param latitude latitude to turn into code
     *
     * @return error code
     */
    private fun getLatitudeLetter(latitude: Double): Long {
        val temp: Double
        var error_code = MGRS_NO_ERROR.toLong()
        val lat_deg: Double = latitude * RAD_TO_DEG

        if (lat_deg >= 72 && lat_deg < 84.5) lastLetter = LETTER_X.toLong()
        else if (lat_deg > -80.5 && lat_deg < 72) {
            temp = ((latitude + (80.0 * DEG_TO_RAD)) / (8.0 * DEG_TO_RAD)) + 1.0e-12
            // lastLetter = Latitude_Band_Table.get((int) temp).letter;
            lastLetter = latitudeBandConstants[temp.toInt()]!![0].toLong()
        } else error_code = error_code or MGRS_LAT_ERROR.toLong()

        return error_code
    }

    /**
     * The function Round_MGRS rounds the input value to the nearest integer, using the standard engineering rule. The
     * rounded integer value is then returned.
     *
     * @param value Value to be rounded
     *
     * @return rounded double value
     */
    private fun roundMGRS(value: Double): Double {
        val ivalue = floor(value)
        var ival: Long
        val fraction = value - ivalue

        // double fraction = modf (value, &ivalue);
        ival = (ivalue).toLong()
        if ((fraction > 0.5) || ((fraction == 0.5) && (ival % 2 == 1L))) ival++
        return ival.toDouble()
    }

    /**
     * The function Make_MGRS_String constructs an MGRS string from its component parts.
     *
     * @param Zone      UTM Zone
     * @param Letters   MGRS coordinate string letters
     * @param Easting   Easting value
     * @param Northing  Northing value
     * @param Precision Precision level of MGRS string
     *
     * @return error code
     */
    private fun makeMGRSString(
        Zone: Long,
        Letters: LongArray,
        Easting: Double,
        Northing: Double,
        Precision: Long
    ): Long {
        var Easting = Easting
        var Northing = Northing
        var j: Int
        val divisor: Double
        val east: Long
        val north: Long
        val error_code = MGRS_NO_ERROR.toLong()

        if (Zone != 0L) this.mgrsString = String.format("%02d", Zone)
        else this.mgrsString = "  "

        j = 0
        while (j < 3) {
            if (Letters[j] < 0 || Letters[j] > 26) return MGRS_ZONE_ERROR.toLong()
            this.mgrsString = this.mgrsString + alphabet.get(Letters[j].toInt())
            j++
        }

        divisor = 10.0.pow((5 - Precision).toDouble())
        Easting = Easting % 100000.0
        if (Easting >= 99999.5) Easting = 99999.0
        east = (Easting / divisor).toLong()

        // Here we need to only use the number requesting in the precision
        val iEast = east.toInt()
        var sEast = iEast.toString()
        if (sEast.length > Precision) sEast = sEast.substring(0, Precision.toInt() - 1)
        else {
            var i: Int
            val length = sEast.length
            i = 0
            while (i < Precision - length) {
                sEast = "0" + sEast
                i++
            }
        }
        this.mgrsString = this.mgrsString + " " + sEast

        Northing = Northing % 100000.0
        if (Northing >= 99999.5) Northing = 99999.0
        north = (Northing / divisor).toLong()

        val iNorth = north.toInt()
        var sNorth = iNorth.toString()
        if (sNorth.length > Precision) sNorth = sNorth.substring(0, Precision.toInt() - 1)
        else {
            var i: Int
            val length = sNorth.length
            i = 0
            while (i < Precision - length) {
                sNorth = "0" + sNorth
                i++
            }
        }
        this.mgrsString = this.mgrsString + " " + sNorth

        return (error_code)
    }

    /**
     * The function Convert_MGRS_To_UPS converts an MGRS coordinate string to UPS (hemisphere, easting, and northing)
     * coordinates, according to the current ellipsoid parameters. If any errors occur, the error code(s) are returned
     * by the function, otherwide UPS_NO_ERROR is returned.
     *
     * @param MGRS the MGRS coordinate string.
     *
     * @return a corresponding [UPSCoord] instance.
     */
    private fun convertMGRSToUPS(MGRS: String): UPSCoord? {
        val ltr2_high_value: Long /* 2nd letter range - high number             */
        val ltr3_high_value: Long /* 3rd letter range - high number (UPS)       */
        val ltr2_low_value: Long /* 2nd letter range - low number              */
        val false_easting: Double /* False easting for 2nd letter               */
        val false_northing: Double /* False northing for 3rd letter              */
        var grid_easting: Double /* easting for 100,000 meter grid square      */
        var grid_northing: Double /* northing for 100,000 meter grid square     */
        var index = 0
        var error_code = MGRS_NO_ERROR.toLong()

        val hemisphere: String?
        var easting: Double
        var northing: Double

        val mgrs = breakMGRSString(MGRS) ?: return null

        if (mgrs.zone > 0) error_code = error_code or MGRS_STRING_ERROR.toLong()

        if (error_code == MGRS_NO_ERROR.toLong()) {
            easting = mgrs.easting
            northing = mgrs.northing

            if (mgrs.latitudeBand >= LETTER_Y) {
                hemisphere = AVKey.NORTH

                index = mgrs.latitudeBand - 22
                ltr2_low_value = upsConstants[index]!![1] //.ltr2_low_value;
                ltr2_high_value = upsConstants[index]!![2] //.ltr2_high_value;
                ltr3_high_value = upsConstants[index]!![3] //.ltr3_high_value;
                false_easting = upsConstants[index]!![4].toDouble() //.false_easting;
                false_northing = upsConstants[index]!![5].toDouble() //.false_northing;
            } else {
                hemisphere = AVKey.SOUTH

                ltr2_low_value = upsConstants[mgrs.latitudeBand]!![12] //.ltr2_low_value;
                ltr2_high_value = upsConstants[mgrs.latitudeBand]!![2] //.ltr2_high_value;
                ltr3_high_value = upsConstants[mgrs.latitudeBand]!![3] //.ltr3_high_value;
                false_easting = upsConstants[mgrs.latitudeBand]!![4].toDouble() //.false_easting;
                false_northing = upsConstants[mgrs.latitudeBand]!![5].toDouble() //.false_northing;
            }

            // Check that the second letter of the MGRS string is within
            // the range of valid second letter values
            // Also check that the third letter is valid
            if ((mgrs.squareLetter1 < ltr2_low_value) || (mgrs.squareLetter1 > ltr2_high_value) ||
                ((mgrs.squareLetter1 == LETTER_D) || (mgrs.squareLetter1 == LETTER_E) ||
                        (mgrs.squareLetter1 == LETTER_M) || (mgrs.squareLetter1 == LETTER_N) ||
                        (mgrs.squareLetter1 == LETTER_V) || (mgrs.squareLetter1 == LETTER_W)) ||
                (mgrs.squareLetter2 > ltr3_high_value)
            ) error_code = MGRS_STRING_ERROR.toLong()

            if (error_code == MGRS_NO_ERROR.toLong()) {
                grid_northing = mgrs.squareLetter2.toDouble() * ONEHT + false_northing
                if (mgrs.squareLetter2 > LETTER_I) grid_northing = grid_northing - ONEHT

                if (mgrs.squareLetter2 > LETTER_O) grid_northing = grid_northing - ONEHT

                grid_easting = ((mgrs.squareLetter1) - ltr2_low_value).toDouble() * ONEHT + false_easting
                if (ltr2_low_value != LETTER_A.toLong()) {
                    if (mgrs.squareLetter1 > LETTER_L) grid_easting = grid_easting - 300000.0

                    if (mgrs.squareLetter1 > LETTER_U) grid_easting = grid_easting - 200000.0
                } else {
                    if (mgrs.squareLetter1 > LETTER_C) grid_easting = grid_easting - 200000.0

                    if (mgrs.squareLetter1 > LETTER_I) grid_easting = grid_easting - ONEHT

                    if (mgrs.squareLetter1 > LETTER_L) grid_easting = grid_easting - 300000.0
                }

                easting = grid_easting + easting
                northing = grid_northing + northing
                return UPSCoord.fromUPS(hemisphere, easting, northing)
            }
        }

        return null
    }

    companion object {
        const val MGRS_NO_ERROR: Int = 0
        private const val MGRS_LAT_ERROR = 0x0001
        private const val MGRS_LON_ERROR = 0x0002
        const val MGRS_STRING_ERROR: Int = 0x0004
        private const val MGRS_PRECISION_ERROR = 0x0008
        private const val MGRS_EASTING_ERROR = 0x0040
        private const val MGRS_NORTHING_ERROR = 0x0080
        private const val MGRS_ZONE_ERROR = 0x0100
        private const val MGRS_HEMISPHERE_ERROR = 0x0200
        private const val MGRS_LAT_WARNING = 0x0400
        private const val MGRS_NOZONE_WARNING = 0x0800
        private const val MGRS_UTM_ERROR = 0x1000
        private const val MGRS_UPS_ERROR = 0x2000

        private const val PI = 3.14159265358979323
        private val PI_OVER_2: Double = (PI / 2.0e0)
        private const val MAX_PRECISION = 5
        private val MIN_UTM_LAT: Double = (-80 * PI) / 180.0 // -80 degrees in radians
        private val MAX_UTM_LAT: Double = (84 * PI) / 180.0 // 84 degrees in radians
        const val DEG_TO_RAD: Double = 0.017453292519943295 // PI/180
        private const val RAD_TO_DEG = 57.29577951308232087 // 180/PI

        private const val MIN_EAST_NORTH = 0.0
        private const val MAX_EAST_NORTH = 4000000.0
        private const val TWOMIL = 2000000.0
        private const val ONEHT = 100000.0

        private const val CLARKE_1866 = "CC"
        private const val CLARKE_1880 = "CD"
        private const val BESSEL_1841 = "BR"
        private const val BESSEL_1841_NAMIBIA = "BN"

        private const val LETTER_A = 0 /* ARRAY INDEX FOR LETTER A               */
        private const val LETTER_B = 1 /* ARRAY INDEX FOR LETTER B               */
        private const val LETTER_C = 2 /* ARRAY INDEX FOR LETTER C               */
        private const val LETTER_D = 3 /* ARRAY INDEX FOR LETTER D               */
        private const val LETTER_E = 4 /* ARRAY INDEX FOR LETTER E               */
        private const val LETTER_F = 5 /* ARRAY INDEX FOR LETTER E               */
        private const val LETTER_G = 6 /* ARRAY INDEX FOR LETTER H               */
        private const val LETTER_H = 7 /* ARRAY INDEX FOR LETTER H               */
        private const val LETTER_I = 8 /* ARRAY INDEX FOR LETTER I               */
        private const val LETTER_J = 9 /* ARRAY INDEX FOR LETTER J               */
        private const val LETTER_K = 10 /* ARRAY INDEX FOR LETTER J               */
        private const val LETTER_L = 11 /* ARRAY INDEX FOR LETTER L               */
        private const val LETTER_M = 12 /* ARRAY INDEX FOR LETTER M               */
        private const val LETTER_N = 13 /* ARRAY INDEX FOR LETTER N               */
        private const val LETTER_O = 14 /* ARRAY INDEX FOR LETTER O               */
        private const val LETTER_P = 15 /* ARRAY INDEX FOR LETTER P               */
        private const val LETTER_Q = 16 /* ARRAY INDEX FOR LETTER Q               */
        private const val LETTER_R = 17 /* ARRAY INDEX FOR LETTER R               */
        private const val LETTER_S = 18 /* ARRAY INDEX FOR LETTER S               */
        private const val LETTER_T = 19 /* ARRAY INDEX FOR LETTER S               */
        private const val LETTER_U = 20 /* ARRAY INDEX FOR LETTER U               */
        private const val LETTER_V = 21 /* ARRAY INDEX FOR LETTER V               */
        private const val LETTER_W = 22 /* ARRAY INDEX FOR LETTER W               */
        private const val LETTER_X = 23 /* ARRAY INDEX FOR LETTER X               */
        private const val LETTER_Y = 24 /* ARRAY INDEX FOR LETTER Y               */
        private const val LETTER_Z = 25 /* ARRAY INDEX FOR LETTER Z               */
        private const val MGRS_LETTERS = 3 /* NUMBER OF LETTERS IN MGRS              */

        private const val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        // UPS Constants are in the following order:
        //    long letter;            /* letter representing latitude band      */
        //    long ltr2_low_value;    /* 2nd letter range - high number         */
        //    long ltr2_high_value;   /* 2nd letter range - low number          */
        //    long ltr3_high_value;   /* 3rd letter range - high number (UPS)   */
        //    double false_easting;   /* False easting based on 2nd letter      */
        //    double false_northing;  /* False northing based on 3rd letter     */
        private val upsConstants = arrayOf<LongArray?>(
            longArrayOf(LETTER_A.toLong(), LETTER_J.toLong(), LETTER_Z.toLong(), LETTER_Z.toLong(), 800000, 800000),
            longArrayOf(LETTER_B.toLong(), LETTER_A.toLong(), LETTER_R.toLong(), LETTER_Z.toLong(), 2000000, 800000),
            longArrayOf(LETTER_Y.toLong(), LETTER_J.toLong(), LETTER_Z.toLong(), LETTER_P.toLong(), 800000, 1300000),
            longArrayOf(LETTER_Z.toLong(), LETTER_A.toLong(), LETTER_J.toLong(), LETTER_P.toLong(), 2000000, 1300000)
        )

        // Latitude Band Constants are in the following order:
        //        long letter;            /* letter representing latitude band  */
        //        double min_northing;    /* minimum northing for latitude band */
        //        double north;           /* upper latitude for latitude band   */
        //        double south;           /* lower latitude for latitude band   */
        private val latitudeBandConstants = arrayOf<DoubleArray?>(
            doubleArrayOf(LETTER_C.toDouble(), 1100000.0, -72.0, -80.5, 0.0),
            doubleArrayOf(LETTER_D.toDouble(), 2000000.0, -64.0, -72.0, 2000000.0),
            doubleArrayOf(LETTER_E.toDouble(), 2800000.0, -56.0, -64.0, 2000000.0),
            doubleArrayOf(LETTER_F.toDouble(), 3700000.0, -48.0, -56.0, 2000000.0),
            doubleArrayOf(LETTER_G.toDouble(), 4600000.0, -40.0, -48.0, 4000000.0),
            doubleArrayOf(LETTER_H.toDouble(), 5500000.0, -32.0, -40.0, 4000000.0),  //smithjl last column to table
            doubleArrayOf(LETTER_J.toDouble(), 6400000.0, -24.0, -32.0, 6000000.0),
            doubleArrayOf(LETTER_K.toDouble(), 7300000.0, -16.0, -24.0, 6000000.0),
            doubleArrayOf(LETTER_L.toDouble(), 8200000.0, -8.0, -16.0, 8000000.0),
            doubleArrayOf(LETTER_M.toDouble(), 9100000.0, 0.0, -8.0, 8000000.0),
            doubleArrayOf(LETTER_N.toDouble(), 0.0, 8.0, 0.0, 0.0),
            doubleArrayOf(LETTER_P.toDouble(), 800000.0, 16.0, 8.0, 0.0),
            doubleArrayOf(LETTER_Q.toDouble(), 1700000.0, 24.0, 16.0, 0.0),
            doubleArrayOf(LETTER_R.toDouble(), 2600000.0, 32.0, 24.0, 2000000.0),
            doubleArrayOf(LETTER_S.toDouble(), 3500000.0, 40.0, 32.0, 2000000.0),
            doubleArrayOf(LETTER_T.toDouble(), 4400000.0, 48.0, 40.0, 4000000.0),
            doubleArrayOf(LETTER_U.toDouble(), 5300000.0, 56.0, 48.0, 4000000.0),
            doubleArrayOf(LETTER_V.toDouble(), 6200000.0, 64.0, 56.0, 6000000.0),
            doubleArrayOf(LETTER_W.toDouble(), 7000000.0, 72.0, 64.0, 6000000.0),
            doubleArrayOf(LETTER_X.toDouble(), 7900000.0, 84.5, 72.0, 6000000.0)
        )
    }
}
