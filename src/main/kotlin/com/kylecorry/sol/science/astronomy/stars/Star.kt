package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.timeToAngle

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
enum class Star(
    internal val coordinate: EquatorialCoordinate,
    val magnitude: Float,
    val motion: ProperMotion? = null
) {
    // TODO: Add proper motion to all stars
    Sirius(EquatorialCoordinate(toDegrees(-16.0, 42.0, 58.0), timeToAngle(6, 45, 8.92)), -1.46f),
    Canopus(EquatorialCoordinate(toDegrees(-52.0, 41.0, 44.4), timeToAngle(6, 23, 57.11)), -0.74f),
    RigilKentaurus(EquatorialCoordinate(toDegrees(-60.0, 50.0, 2.4), timeToAngle(14, 39, 36.49)), -0.1f),
    Arcturus(EquatorialCoordinate(toDegrees(19.0, 10.0, 56.7), timeToAngle(14, 15, 39.67)), -0.05f),
    Vega(EquatorialCoordinate(toDegrees(38.0, 47.0, 1.3), timeToAngle(18, 36, 56.34)), 0.03f),
    Rigel(EquatorialCoordinate(toDegrees(-8.0, 12.0, 5.9), timeToAngle(5, 14, 32.27)), 0.13f),
    Procyon(EquatorialCoordinate(toDegrees(5.0, 13.0, 30.0), timeToAngle(7, 39, 18.12)), 0.37f),
    Achernar(EquatorialCoordinate(toDegrees(-57.0, 14.0, 12.3), timeToAngle(1, 37, 42.85)), 0.46f),
    Betelgeuse(EquatorialCoordinate(toDegrees(7.0, 24.0, 25.4), timeToAngle(5, 55, 10.31)), 0.42f),
    Hadar(EquatorialCoordinate(toDegrees(-60.0, 22.0, 22.9), timeToAngle(14, 3, 49.41)), 0.58f),
    Altair(EquatorialCoordinate(toDegrees(8.0, 52.0, 6.0), timeToAngle(19, 50, 47.0)), 0.76f),
    Acrux(EquatorialCoordinate(toDegrees(-63.0, 5.0, 56.7), timeToAngle(12, 26, 35.9)), 0.76f),
    Aldebaran(EquatorialCoordinate(toDegrees(16.0, 30.0, 33.5), timeToAngle(4, 35, 55.24)), 0.86f),
    Antares(EquatorialCoordinate(toDegrees(-26.0, 25.0, 55.2), timeToAngle(16, 29, 24.46)), 0.91f),
    Spica(EquatorialCoordinate(toDegrees(-11.0, 9.0, 40.7), timeToAngle(13, 25, 11.58)), 0.97f),
    Pollux(EquatorialCoordinate(toDegrees(28.0, 1.0, 34.3), timeToAngle(7, 45, 18.95)), 1.14f),
    Fomalhaut(EquatorialCoordinate(toDegrees(-29.0, 37.0, 20.1), timeToAngle(22, 57, 39.05)), 1.16f),
    Deneb(EquatorialCoordinate(toDegrees(45.0, 16.0, 49.3), timeToAngle(20, 41, 25.92)), 1.25f),
    Mimosa(EquatorialCoordinate(toDegrees(-59.0, 41.0, 19.6), timeToAngle(12, 47, 43.27)), 1.25f),
    Regulus(EquatorialCoordinate(toDegrees(12.0, 18.0, 23.0), timeToAngle(10, 8, 28.1)), 1.4f),
    Adhara(EquatorialCoordinate(toDegrees(-28.0, 58.0, 19.5), timeToAngle(6, 58, 37.55)), 1.5f),
    Castor(EquatorialCoordinate(toDegrees(31.0, 53.0, 17.8), timeToAngle(7, 34, 35.87)), 1.58f),
    Shaula(EquatorialCoordinate(toDegrees(-37.0, 6.0, 13.8), timeToAngle(17, 33, 36.52)), 1.63f),
    Gacrux(EquatorialCoordinate(toDegrees(-57.0, 6.0, 47.6), timeToAngle(12, 31, 9.96)), 1.64f),
    Bellatrix(EquatorialCoordinate(toDegrees(6.0, 20.0, 58.9), timeToAngle(5, 25, 7.86)), 1.64f),
    Elnath(EquatorialCoordinate(toDegrees(28.0, 36.0, 26.8), timeToAngle(5, 26, 17.51)), 1.65f),
    Miaplacidus(EquatorialCoordinate(toDegrees(-69.0, 43.0, 1.9), timeToAngle(9, 13, 11.98)), 1.69f),
    Alnilam(EquatorialCoordinate(toDegrees(-1.0, 12.0, 6.9), timeToAngle(5, 36, 12.81)), 1.69f),
    Alnair(EquatorialCoordinate(toDegrees(-46.0, 57.0, 39.5), timeToAngle(22, 8, 13.98)), 1.71f),
    Alnitak(EquatorialCoordinate(toDegrees(-1.0, 56.0, 45.53), timeToAngle(5, 40, 45.53)), 1.77f),
    Alioth(EquatorialCoordinate(toDegrees(55.0, 57.0, 35.4), timeToAngle(12, 54, 1.75)), 1.77f),
    Dubhe(EquatorialCoordinate(toDegrees(61.0, 45.0, 3.7), timeToAngle(11, 3, 43.67)), 1.79f),
    Mirfak(EquatorialCoordinate(toDegrees(49.0, 51.0, 40.2), timeToAngle(3, 24, 19.37)), 1.79f),
    Wezen(EquatorialCoordinate(toDegrees(-26.0, 23.0, 35.5), timeToAngle(7, 8, 23.48)), 1.84f),
    Regor(EquatorialCoordinate(toDegrees(-47.0, 20.0, 11.7), timeToAngle(8, 9, 31.95)), 1.83f),
    Sargas(EquatorialCoordinate(toDegrees(-42.0, 59.0, 52.2), timeToAngle(17, 37, 19.13)), 1.85f),
    KausAustralis(EquatorialCoordinate(toDegrees(-34.0, 23.0, 4.6), timeToAngle(18, 24, 10.32)), 1.81f),
    Avior(EquatorialCoordinate(toDegrees(-59.0, 32.0, 34.1), timeToAngle(8, 22, 30.84)), 1.86f),
    Alkaid(EquatorialCoordinate(toDegrees(49.0, 18.0, 47.8), timeToAngle(13, 47, 32.44)), 1.86f),
    Menkalinan(EquatorialCoordinate(toDegrees(44.0, 56.0, 50.8), timeToAngle(5, 59, 31.72)), 1.9f),
    Atria(EquatorialCoordinate(toDegrees(-69.0, 1.0, 39.8), timeToAngle(16, 48, 39.0)), 1.88f),
    Alhena(EquatorialCoordinate(toDegrees(16.0, 23.0, 57.4), timeToAngle(6, 37, 42.7)), 1.92f),
    Peacock(EquatorialCoordinate(toDegrees(-56.0, 44.0, 6.3), timeToAngle(20, 25, 38.86)), 1.918f),
    Alsephina(EquatorialCoordinate(toDegrees(-54.0, 42.0, 31.8), timeToAngle(8, 44, 42.23)), 1.95f),
    Mirzam(EquatorialCoordinate(toDegrees(-17.0, 57.0, 21.3), timeToAngle(6, 22, 41.99)), 1.97f),
    Polaris(EquatorialCoordinate(toDegrees(89.0, 15.0, 50.8), timeToAngle(2, 31, 49.09)), 2.02f),
    Alphard(EquatorialCoordinate(toDegrees(-8.0, 39.0, 31.0), timeToAngle(9, 27, 35.24)), 1.97f),
    Hamal(EquatorialCoordinate(toDegrees(23.0, 27.0, 44.7), timeToAngle(2, 7, 10.41)), 2.01f),
    Diphda(EquatorialCoordinate(toDegrees(-17.0, 59.0, 11.8), timeToAngle(0, 43, 35.37)), 2.01f),

    ////// Important stars for navigation that aren't in the top 50 //////
    Alpheratz(EquatorialCoordinate(toDegrees(29.0, 5.0, 25.55), timeToAngle(0, 8, 23.26)), 2.06f),
    Ankaa(EquatorialCoordinate(toDegrees(-42.0, 18.0, 21.55), timeToAngle(0, 26, 17.051)), 2.38f),
    Schedar(EquatorialCoordinate(toDegrees(56.0, 32.0, 14.39), timeToAngle(0, 40, 30.443)), 2.23f),
    Acamar(EquatorialCoordinate(toDegrees(-40.0, 18.0, 16.85), timeToAngle(2, 58, 15.675)), 3.2f),
    Menkar(EquatorialCoordinate(toDegrees(4.0, 5.0, 23.06), timeToAngle(3, 2, 16.773)), 2.53f),
    Capella(EquatorialCoordinate(toDegrees(45.0, 59.0, 52.77), timeToAngle(5, 16, 41.359)), 0.08f),
    Suhail(EquatorialCoordinate(toDegrees(-43.0, 25.0, 57.33), timeToAngle(9, 7, 59.758)), 2.21f),
    Denebola(EquatorialCoordinate(toDegrees(14.0, 34.0, 19.41), timeToAngle(11, 49, 3.578)), 2.13f),
    Gienah(EquatorialCoordinate(toDegrees(-17.0, 32.0, 30.95), timeToAngle(12, 15, 48.371)), 2.58f),
    Menkent(EquatorialCoordinate(toDegrees(-36.0, 22.0, 11.84), timeToAngle(14, 6, 40.948)), 2.05f),
    Zubenelgenubi(EquatorialCoordinate(toDegrees(-16.0, 2.0, 30.4), timeToAngle(14, 50, 52.713)), 2.75f),
    Kochab(EquatorialCoordinate(toDegrees(74.0, 9.0, 19.81), timeToAngle(14, 50, 42.326)), 2.08f),
    Alphecca(EquatorialCoordinate(toDegrees(26.0, 42.0, 52.87), timeToAngle(15, 34, 41.268)), 2.24f),
    Sabik(EquatorialCoordinate(toDegrees(-15.0, 43.0, 29.66), timeToAngle(17, 10, 22.687)), 2.42f),
    Rasalhague(EquatorialCoordinate(toDegrees(17.0, 34.0, 56.069), timeToAngle(12, 33, 36.13)), 2.07f),
    Eltanin(EquatorialCoordinate(toDegrees(51.0, 29.0, 20.02), timeToAngle(17, 56, 36.37)), 2.23f),
    Nunki(EquatorialCoordinate(toDegrees(-26.0, 17.0, 48.21), timeToAngle(18, 55, 15.926)), 2.067f),
    Enif(EquatorialCoordinate(toDegrees(9.0, 52.0, 30.03), timeToAngle(21, 44, 11.156)), 2.39f),
    Markab(EquatorialCoordinate(toDegrees(15.0, 12.0, 18.96), timeToAngle(23, 4, 45.653)), 2.48f),

    ////// Important stars in constellations that aren't in the top 50 //////

    // Ursa Major
    Merak(EquatorialCoordinate(toDegrees(56.0, 22.0, 56.76), timeToAngle(11, 1, 50.48)), 2.37f),
    Phecda(EquatorialCoordinate(toDegrees(53.0, 41.0, 41.14), timeToAngle(11, 53, 49.847)), 2.44f),
    Megrez(EquatorialCoordinate(toDegrees(57.0, 1.0, 57.42), timeToAngle(12, 15, 25.56)), 3.32f),
    Mizar(EquatorialCoordinate(toDegrees(54.0, 55.0, 31.27), timeToAngle(13, 23, 55.54)), 2.04f),

    // Southern Cross
    Imai(EquatorialCoordinate(toDegrees(-58.0, 44.0, 56.13), timeToAngle(12, 15, 8.718)), 2.752f),
    Ginan(EquatorialCoordinate(toDegrees(-60.0, 24.0, 4.13), timeToAngle(12, 21, 21.608)), 3.57f),

    // Orion
    Saiph(EquatorialCoordinate(toDegrees(-9.0, 40.0, 10.58), timeToAngle(5, 47, 45.389)), 2.06f),
    Meissa(EquatorialCoordinate(toDegrees(9.0, 56.0, 2.96), timeToAngle(5, 35, 8.278)), 3.66f),
    Mintaka(EquatorialCoordinate(toDegrees(0.0, -17.0, 56.74), timeToAngle(5, 32, 0.4)), 2.41f),
}

/**
 * Proper motion of a star in milliarcseconds per year
 */
data class ProperMotion(val declination: Double, val rightAscension: Double)