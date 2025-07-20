package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.normalizeAngle
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.SolMath.tanDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.science.astronomy.units.EclipticCoordinate
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.toJulianDay
import com.kylecorry.sol.units.Distance
import kotlin.math.PI
import kotlin.math.asin
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
        val daysSinceEpoch = ut.toJulianDay() - 2451545.0
        val meanAnomaly = normalizeAngle(
            (360.0 * daysSinceEpoch) / (365.242191 * planet.orbitalPeriod) + planet.eclipticLongitudeEpoch - planet.eclipticLongitudePerihelion
        )
        // TODO: Solve kelper's equation
        val equationOfCenter = 360.0 / PI * planet.eccentricity * sinDegrees(meanAnomaly)
        // TODO: Solve kelper's equation
        val trueAnomaly = meanAnomaly + equationOfCenter
        val heliocentricEclipticLongitude = normalizeAngle(trueAnomaly + planet.eclipticLongitudePerihelion)
        val heliocentricEclipticLatitude = normalizeAngle(
            asin(
                sinDegrees(heliocentricEclipticLongitude - planet.eclipticLongitudeAscendingNodeEpoch) * sinDegrees(
                    planet.inclination
                )
            ).toDegrees()
        )
        val coordinate = EclipticCoordinate(heliocentricEclipticLatitude, heliocentricEclipticLongitude)
        val radius = planet.semimajorAxis * (1 - SolMath.square(planet.eccentricity)) /
                (1 + planet.eccentricity * cosDegrees(trueAnomaly))
        return Pair(coordinate, radius)
    }
}

enum class Planet(
    val order: Int,
    val orbitalPeriod: Double,
    val mass: Double,
    val radius: Double,
    val lengthOfDay: Double,
    internal val eccentricity: Double,
    internal val semimajorAxis: Double,
    internal val angularDiameter: Double,
    internal val visualMagnitude: Double,
    internal val standardGravitationalParameter: Double,
    internal val inclination: Double,
    internal val eclipticLongitudeEpoch: Double,
    internal val eclipticLongitudePerihelion: Double,
    internal val eclipticLongitudeAscendingNodeEpoch: Double
) {
    Mercury(
        1,
        0.240847,
        0.055274,
        2439.7,
        58.6462,
        0.205636,
        0.3870993,
        6.74,
        -0.42,
        22032.0,
        7.004979,
        252.250324,
        77.457796,
        48.330766
    ),
    Venus(
        2,
        0.615197,
        0.814998,
        6051.8,
        243.018,
        0.0067767,
        0.723336,
        16.92,
        -4.40,
        324860.0,
        3.394676,
        181.979100,
        131.602467,
        76.679843
    ),
    Earth(
        3,
        1.000017,
        1.0,
        6378.14,
        1.0,
        0.0167112,
        1.000003,
        0.0,
        0.0,
        398600.0,
        -0.000015,
        100.464572,
        102.937682,
        0.0
    ),
    Mars(
        4,
        1.880848,
        0.107447,
        3389.5,
        1.025957,
        0.093394,
        1.523710,
        9.36,
        -1.52,
        42828.0,
        1.849691,
        -4.553432,
        -23.943630,
        49.559539
    ),
    Jupiter(
        5,
        11.862615,
        317.828133,
        69911.0,
        0.41354,
        0.048393,
        5.202887,
        196.74,
        -9.40,
        126687000.0,
        1.3043975,
        34.396441,
        14.728480,
        100.473909
    ),
    Saturn(
        6,
        29.447498,
        95.160904,
        58232.0,
        0.44401,
        0.053862,
        9.536676,
        165.60,
        -8.88,
        37931000.0,
        2.485992,
        49.954244,
        92.598878,
        113.662424
    ),
    Uranus(
        7,
        84.016846,
        14.535757,
        25362.0,
        0.71833,
        0.0472574,
        19.189165,
        65.80,
        -7.19,
        5794000.0,
        0.772638,
        313.232810,
        170.954276,
        74.016925
    ),
    Neptune(
        8,
        164.79132,
        17.147813,
        24622.0,
        0.67125,
        0.008590,
        30.069923,
        62.20,
        -6.87,
        6835100.0,
        1.770043,
        -55.120030,
        44.964762,
        131.784226
    ),
}