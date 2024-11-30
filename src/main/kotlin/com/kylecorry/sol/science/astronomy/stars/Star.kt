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
// List from https://en.wikipedia.org/wiki/List_of_brightest_stars
enum class Star(internal val coordinate: EquatorialCoordinate) {
    Sirius(EquatorialCoordinate(toDegrees(-16.0, 42.0, 58.0), timeToAngle(6, 45, 8.92))),
    Canopus(EquatorialCoordinate(toDegrees(-52.0, 41.0, 44.4), timeToAngle(6, 23, 57.11))),
    Rigil(EquatorialCoordinate(toDegrees(-60.0, 50.0, 2.4), timeToAngle(14, 39, 36.49))),
    Arcturus(EquatorialCoordinate(toDegrees(19.0, 10.0, 56.7), timeToAngle(14, 15, 39.67))),
    Vega(EquatorialCoordinate(toDegrees(38.0, 47.0, 1.3), timeToAngle(18, 36, 56.34))),
    Rigel(EquatorialCoordinate(toDegrees(-8.0, 12.0, 5.9), timeToAngle(5, 14, 32.27))),
    Procyon(EquatorialCoordinate(toDegrees(5.0, 13.0, 30.0), timeToAngle(7, 39, 18.12))),
    Achernar(EquatorialCoordinate(toDegrees(-57.0, 14.0, 12.3), timeToAngle(1, 37, 42.85))),
    Betelgeuse(EquatorialCoordinate(toDegrees(7.0, 24.0, 25.4), timeToAngle(5, 55, 10.31))),
    Hadar(EquatorialCoordinate(toDegrees(-60.0, 22.0, 22.9), timeToAngle(14, 3, 49.41))),
    Altair(EquatorialCoordinate(toDegrees(8.0, 52.0, 6.0), timeToAngle(19, 50, 47.0))),
    Acrux(EquatorialCoordinate(toDegrees(-63.0, 5.0, 56.7), timeToAngle(12, 26, 35.9))),
    Aldebaran(EquatorialCoordinate(toDegrees(16.0, 30.0, 33.5), timeToAngle(4, 35, 55.24))),
    Antares(EquatorialCoordinate(toDegrees(-26.0, 25.0, 55.2), timeToAngle(16, 29, 24.46))),
    Spica(EquatorialCoordinate(toDegrees(-11.0, 9.0, 40.7), timeToAngle(13, 25, 11.58))),
    Pollux(EquatorialCoordinate(toDegrees(28.0, 1.0, 34.3), timeToAngle(7, 45, 18.95))),
    Fomalhaut(EquatorialCoordinate(toDegrees(-29.0, 37.0, 20.1), timeToAngle(22, 57, 39.05))),
    Deneb(EquatorialCoordinate(toDegrees(45.0, 16.0, 49.3), timeToAngle(20, 41, 25.92))),
    Mimosa(EquatorialCoordinate(toDegrees(-59.0, 41.0, 19.6), timeToAngle(12, 47, 43.27))),
    Regulus(EquatorialCoordinate(toDegrees(12.0, 18.0, 23.0), timeToAngle(10, 8, 28.1))),
    Adhara(EquatorialCoordinate(toDegrees(-28.0, 58.0, 19.5), timeToAngle(6, 58, 37.55))),
    Castor(EquatorialCoordinate(toDegrees(31.0, 53.0, 17.8), timeToAngle(7, 34, 35.87))),
    Shaula(EquatorialCoordinate(toDegrees(-37.0, 6.0, 13.8), timeToAngle(17, 33, 36.52))),
    Gacrux(EquatorialCoordinate(toDegrees(-57.0, 6.0, 47.6), timeToAngle(12, 31, 9.96))),
    Bellatrix(EquatorialCoordinate(toDegrees(6.0, 20.0, 58.9), timeToAngle(5, 25, 7.86))),
    Elnath(EquatorialCoordinate(toDegrees(28.0, 36.0, 26.8), timeToAngle(5, 26, 17.51))),
    Miaplacidus(EquatorialCoordinate(toDegrees(-69.0, 43.0, 1.9), timeToAngle(9, 13, 11.98))),
    Alnilam(EquatorialCoordinate(toDegrees(-1.0, 12.0, 6.9), timeToAngle(5, 36, 12.81))),
    Alnair(EquatorialCoordinate(toDegrees(-46.0, 57.0, 39.5), timeToAngle(22, 8, 13.98))),
    Alnitak(EquatorialCoordinate(toDegrees(-1.0, 56.0, 45.53), timeToAngle(5, 40, 45.53))),
    Alioth(EquatorialCoordinate(toDegrees(55.0, 57.0, 35.4), timeToAngle(12, 54, 1.75))),
    Dubhe(EquatorialCoordinate(toDegrees(61.0, 45.0, 3.7), timeToAngle(11, 3, 43.67))),
    Mirfak(EquatorialCoordinate(toDegrees(49.0, 51.0, 40.2), timeToAngle(3, 24, 19.37))),
    Wezen(EquatorialCoordinate(toDegrees(-26.0, 23.0, 35.5), timeToAngle(7, 8, 23.48))),
    Regor(EquatorialCoordinate(toDegrees(-47.0, 20.0, 11.7), timeToAngle(8, 9, 31.95))),
    Sargas(EquatorialCoordinate(toDegrees(-42.0, 59.0, 52.2), timeToAngle(17, 37, 19.13))),
    KausAustralis(EquatorialCoordinate(toDegrees(-34.0, 23.0, 4.6), timeToAngle(18, 24, 10.32))),
    Avior(EquatorialCoordinate(toDegrees(-59.0, 32.0, 34.1), timeToAngle(8, 22, 30.84))),
    Alkaid(EquatorialCoordinate(toDegrees(49.0, 18.0, 47.8), timeToAngle(13, 47, 32.44))),
    Menkalinan(EquatorialCoordinate(toDegrees(44.0, 56.0, 50.8), timeToAngle(5, 59, 31.72))),
    Atria(EquatorialCoordinate(toDegrees(-69.0, 1.0, 39.8), timeToAngle(16, 48, 39.0))),
    Alhena(EquatorialCoordinate(toDegrees(16.0, 23.0, 57.4), timeToAngle(6, 37, 42.7))),
    Peacock(EquatorialCoordinate(toDegrees(-56.0, 44.0, 6.3), timeToAngle(20, 25, 38.86))),
    Alsephina(EquatorialCoordinate(toDegrees(-54.0, 42.0, 31.8), timeToAngle(8, 44, 42.23))),
    Mirzam(EquatorialCoordinate(toDegrees(-17.0, 57.0, 21.3), timeToAngle(6, 22, 41.99))),
    Polaris(EquatorialCoordinate(toDegrees(89.0, 15.0, 50.8), timeToAngle(2, 31, 49.09))),
    Alphard(EquatorialCoordinate(toDegrees(-8.0, 39.0, 31.0), timeToAngle(9, 27, 35.24))),
    Hamal(EquatorialCoordinate(toDegrees(23.0, 27.0, 44.7), timeToAngle(2, 7, 10.41))),
    Diphda(EquatorialCoordinate(toDegrees(-17.0, 59.0, 11.8), timeToAngle(0, 43, 35.37))),
}