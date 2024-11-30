package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.timeToAngle

// Source: https://irsa.ipac.caltech.edu/cgi-bin/Gator/nph-dd?catalog=gaia_dr3_source&mode=html&passproj&
/**
 * This work has made use of data from the European Space Agency (ESA) mission Gaia (https://www.cosmos.esa.int/gaia), processed by the Gaia Data Processing and Analysis Consortium (DPAC, https://www.cosmos.esa.int/web/gaia/dpac/consortium). Funding for the DPAC has been provided by national institutions, in particular the institutions participating in the Gaia Multilateral Agreement.
 */

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
}