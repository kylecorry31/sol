package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.math.analysis.Trigonometry.cosDegrees
import com.kylecorry.sol.math.analysis.Trigonometry.normalizeAngle
import com.kylecorry.sol.math.analysis.Trigonometry.sinDegrees
import com.kylecorry.sol.math.arithmetic.Arithmetic.square
import com.kylecorry.sol.math.analysis.Trigonometry.tanDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.science.astronomy.OrbitalMath
import com.kylecorry.sol.science.astronomy.units.*
import com.kylecorry.sol.units.Distance
import kotlin.math.atan2
import kotlin.math.log10
import kotlin.math.sqrt

// Equations from Celestial Calculations by J. L. Lawrence chapter 8
internal class PlanetLocator(private val planet: Planet) : ICelestialLocator {
    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        val planetPosition = getHeliocentricPosition(planet, ut)
        val earthPosition = getHeliocentricPosition(Planet.Earth, ut)
        val longitudeAdjustment = normalizeAngle(
            planet.eclipticLongitudeAscendingNodeEpoch + atan2(
                sinDegrees(planetPosition.first.eclipticLongitude - planet.eclipticLongitudeAscendingNodeEpoch) * cosDegrees(
                    planet.inclination
                ),
                cosDegrees(planetPosition.first.eclipticLongitude - planet.eclipticLongitudeAscendingNodeEpoch)
            ).toDegrees()
        )
        val geocentricEclipticLongitude = normalizeAngle(
            if (planet.order <= Planet.Earth.order) {
                180 + earthPosition.first.eclipticLongitude + atan2(
                    planetPosition.second * cosDegrees(planetPosition.first.eclipticLatitude) * sinDegrees(earthPosition.first.eclipticLongitude - longitudeAdjustment),
                    earthPosition.second - planetPosition.second * cosDegrees(planetPosition.first.eclipticLatitude) * cosDegrees(
                        earthPosition.first.eclipticLongitude - longitudeAdjustment
                    )
                ).toDegrees()
            } else {
                longitudeAdjustment + atan2(
                    earthPosition.second * sinDegrees(longitudeAdjustment - earthPosition.first.eclipticLongitude),
                    planetPosition.second * cosDegrees(planetPosition.first.eclipticLatitude) - earthPosition.second * cosDegrees(
                        earthPosition.first.eclipticLongitude - longitudeAdjustment
                    )
                ).toDegrees()
            }
        )
        val geocentricEclipticLatitude = normalizeAngle(
            atan2(
                planetPosition.second * cosDegrees(planetPosition.first.eclipticLatitude) * tanDegrees(planetPosition.first.eclipticLatitude) * sinDegrees(
                    geocentricEclipticLongitude - longitudeAdjustment
                ),
                earthPosition.second * sinDegrees(longitudeAdjustment - earthPosition.first.eclipticLongitude)
            ).toDegrees()
        )
        val coordinate = EclipticCoordinate(geocentricEclipticLatitude, geocentricEclipticLongitude)
        return coordinate.toEquatorial(ut)
    }

    override fun getDistance(ut: UniversalTime): Distance? {
        return Distance.kilometers(
            (149597871.0 * getDistanceAU(
                getHeliocentricPosition(planet, ut),
                getHeliocentricPosition(Planet.Earth, ut)
            )).toFloat()
        )
    }

    fun getAngularDiameter(ut: UniversalTime): Double {
        val distance = getDistanceAU(getHeliocentricPosition(planet, ut), getHeliocentricPosition(Planet.Earth, ut))
        return planet.angularDiameter / distance
    }

    fun getMagnitude(ut: UniversalTime): Double {
        val planetPosition = getHeliocentricPosition(planet, ut)
        val earthPosition = getHeliocentricPosition(Planet.Earth, ut)
        val geocentricPlanetPosition = getGeocentricPosition(planetPosition, earthPosition)
        val distance = getDistanceAU(planetPosition, earthPosition)
        val phaseAngle =
            (1 + cosDegrees(geocentricPlanetPosition.eclipticLongitude - planetPosition.first.eclipticLongitude)) / 2
        return planet.visualMagnitude + 5 * log10((planetPosition.second * distance) / sqrt(phaseAngle))
    }

    private fun getGeocentricPosition(
        planetPosition: Pair<EclipticCoordinate, Double>,
        earthPosition: Pair<EclipticCoordinate, Double>
    ): EclipticCoordinate {
        val longitudeAdjustment = normalizeAngle(
            planet.eclipticLongitudeAscendingNodeEpoch + atan2(
                sinDegrees(planetPosition.first.eclipticLongitude - planet.eclipticLongitudeAscendingNodeEpoch) * cosDegrees(
                    planet.inclination
                ),
                cosDegrees(planetPosition.first.eclipticLongitude - planet.eclipticLongitudeAscendingNodeEpoch)
            ).toDegrees()
        )
        val geocentricEclipticLongitude = normalizeAngle(
            if (planet.order <= Planet.Earth.order) {
                180 + earthPosition.first.eclipticLongitude + atan2(
                    planetPosition.second * cosDegrees(planetPosition.first.eclipticLatitude) * sinDegrees(earthPosition.first.eclipticLongitude - longitudeAdjustment),
                    earthPosition.second - planetPosition.second * cosDegrees(planetPosition.first.eclipticLatitude) * cosDegrees(
                        earthPosition.first.eclipticLongitude - longitudeAdjustment
                    )
                ).toDegrees()
            } else {
                longitudeAdjustment + atan2(
                    earthPosition.second * sinDegrees(longitudeAdjustment - earthPosition.first.eclipticLongitude),
                    planetPosition.second * cosDegrees(planetPosition.first.eclipticLatitude) - earthPosition.second * cosDegrees(
                        earthPosition.first.eclipticLongitude - longitudeAdjustment
                    )
                ).toDegrees()
            }
        )
        val geocentricEclipticLatitude = normalizeAngle(
            atan2(
                planetPosition.second * cosDegrees(planetPosition.first.eclipticLatitude) * tanDegrees(planetPosition.first.eclipticLatitude) * sinDegrees(
                    geocentricEclipticLongitude - longitudeAdjustment
                ),
                earthPosition.second * sinDegrees(longitudeAdjustment - earthPosition.first.eclipticLongitude)
            ).toDegrees()
        )
        return EclipticCoordinate(geocentricEclipticLatitude, geocentricEclipticLongitude)
    }

    private fun getDistanceAU(
        planetPosition: Pair<EclipticCoordinate, Double>,
        earthPosition: Pair<EclipticCoordinate, Double>
    ): Double {
        return sqrt(
            square(earthPosition.second) + square(planetPosition.second) - 2 * earthPosition.second * planetPosition.second * cosDegrees(
                planetPosition.first.eclipticLongitude - earthPosition.first.eclipticLongitude
            )
        )
    }

    private fun getHeliocentricPosition(planet: Planet, ut: UniversalTime): Pair<EclipticCoordinate, Double> {
        val meanAnomaly = OrbitalMath.getMeanAnomaly(
            ut.toJulianDay() - planet.epoch,
            OrbitalMath.EARTH_ORBITAL_PERIOD_DAYS * planet.orbitalPeriod,
            planet.eclipticLongitudeEpoch,
            planet.eclipticLongitudePerihelion
        )
        val trueAnomaly = OrbitalMath.estimateTrueAnomaly(planet.eccentricity, meanAnomaly)
        val coordinate = OrbitalMath.getEclipticCoordinate(
            trueAnomaly,
            planet.eclipticLongitudePerihelion,
            planet.eclipticLongitudeAscendingNodeEpoch,
            planet.inclination
        )
        val radius = OrbitalMath.getRadius(trueAnomaly, planet.semimajorAxis, planet.eccentricity)
        return Pair(coordinate, radius)
    }
}

enum class Planet(
    val order: Int,
    internal val epoch: Double,
    val orbitalPeriod: Double,
    val radius: Double,
    internal val eccentricity: Double,
    internal val semimajorAxis: Double,
    internal val angularDiameter: Double,
    internal val visualMagnitude: Double,
    internal val inclination: Double,
    internal val eclipticLongitudeEpoch: Double,
    internal val eclipticLongitudePerihelion: Double,
    internal val eclipticLongitudeAscendingNodeEpoch: Double
) {
    Mercury(
        1,
        JD_2000,
        0.240847,
        2439.7,
        0.205636,
        0.3870993,
        toDegrees(0.0, 0.0, 6.74),
        -0.42,
        7.004979,
        252.250324,
        77.457796,
        48.330766
    ),
    Venus(
        2,
        JD_2000,
        0.615197,
        6051.8,
        0.0067767,
        0.723336,
        toDegrees(0.0, 0.0, 16.92),
        -4.40,
        3.394676,
        181.979100,
        131.602467,
        76.679843
    ),
    Earth(
        3,
        JD_2000,
        1.000017,
        6378.14,
        0.0167112,
        1.000003,
        0.0,
        0.0,
        -0.000015,
        100.464572,
        102.937682,
        0.0
    ),
    Mars(
        4,
        JD_2000,
        1.880848,
        3389.5,
        0.093394,
        1.523710,
        toDegrees(0.0, 0.0, 9.36),
        -1.52,
        1.849691,
        -4.553432,
        -23.943630,
        49.559539
    ),
    Jupiter(
        5,
        JD_2000,
        11.862615,
        69911.0,
        0.048393,
        5.202887,
        toDegrees(0.0, 0.0, 196.74),
        -9.40,
        1.3043975,
        34.396441,
        14.728480,
        100.473909
    ),
    Saturn(
        6,
        JD_2000,
        29.447498,
        58232.0,
        0.053862,
        9.536676,
        toDegrees(0.0, 0.0, 165.60),
        -8.88,
        2.485992,
        49.954244,
        92.598878,
        113.662424
    ),
    Uranus(
        7,
        JD_2000,
        84.016846,
        25362.0,
        0.0472574,
        19.189165,
        toDegrees(0.0, 0.0, 65.80),
        -7.19,
        0.772638,
        313.232810,
        170.954276,
        74.016925
    ),
    Neptune(
        8,
        JD_2000,
        164.79132,
        24622.0,
        0.008590,
        30.069923,
        toDegrees(0.0, 0.0, 62.20),
        -6.87,
        1.770043,
        -55.120030,
        44.964762,
        131.784226
    ),
}