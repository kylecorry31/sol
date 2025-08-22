package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate

/**
 * This research has made use of the SIMBAD database,
 * operated at CDS, Strasbourg, France
 *
 * 2000,A&AS,143,9 , "The SIMBAD astronomical database", Wenger et al.
 *
 * https://doi.org/10.1051/aas:2000332
 */

// http://cdsportal.u-strasbg.fr/?target=Rigel
// https://en.wikipedia.org/wiki/List_of_brightest_stars
// https://en.wikipedia.org/wiki/List_of_stars_for_navigation

class Star internal constructor(
    val hipDesignation: Int,
    val name: String,
    internal val coordinate: EquatorialCoordinate,
    val magnitude: Float,
    val motion: ProperMotion,
    val colorIndexBV: Float
)

/**
 * Proper motion of a star in degrees per year
 */
data class ProperMotion(val declination: Double, val rightAscension: Double)