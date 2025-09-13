package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.normalizeAngle
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.square
import com.kylecorry.sol.math.SolMath.tanDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.toRadians
import com.kylecorry.sol.math.optimization.Optimization
import com.kylecorry.sol.science.astronomy.units.EclipticCoordinate
import kotlin.math.*

internal object OrbitalMath {

    const val EARTH_ORBITAL_PERIOD_DAYS = 365.242191

    fun getEclipticCoordinate(
        trueAnomaly: Double,
        perihelionEclipticLongitude: Double,
        ascendingNodeEclipticLongitude: Double,
        inclination: Double
    ): EclipticCoordinate {
        val heliocentricEclipticLongitude = normalizeAngle(trueAnomaly + perihelionEclipticLongitude)
        val heliocentricEclipticLatitude = normalizeAngle(
            asin(
                sinDegrees(heliocentricEclipticLongitude - ascendingNodeEclipticLongitude) * sinDegrees(
                    inclination
                )
            ).toDegrees()
        )
        return EclipticCoordinate(heliocentricEclipticLatitude, heliocentricEclipticLongitude)
    }

    fun getRadius(
        trueAnomaly: Double,
        semimajorAxis: Double,
        eccentricity: Double
    ): Double {
        return semimajorAxis * (1 - square(eccentricity)) /
                (1 + eccentricity * cosDegrees(trueAnomaly))
    }

    fun getTrueAnomaly(meanAnomaly: Double, equationOfCenter: Double): Double {
        return normalizeAngle(meanAnomaly + equationOfCenter)
    }

    fun getMeanAnomaly(
        daysSinceEpoch: Double,
        orbitalPeriodEarthDays: Double,
        epochEclipticLongitude: Double,
        perihelionEclipticLongitude: Double
    ): Double {
        val orbitPercent = daysSinceEpoch / orbitalPeriodEarthDays
        return normalizeAngle(
            360.0 * orbitPercent + epochEclipticLongitude - perihelionEclipticLongitude
        )
    }

    fun estimateTrueAnomaly(
        eccentricity: Double,
        meanAnomaly: Double,
        useEccentricAnomaly: Boolean = true
    ): Double {
        return if (useEccentricAnomaly) {
            val eccentricAnomaly = estimateEccentricAnomaly(eccentricity, meanAnomaly)
            normalizeAngle(2 * atan(sqrt((1 + eccentricity) / (1 - eccentricity)) * tanDegrees(eccentricAnomaly / 2)).toDegrees())
        } else {
            getTrueAnomaly(
                meanAnomaly,
                estimateEquationOfCenter(eccentricity, meanAnomaly)
            )
        }
    }

    // TODO: Expand the infinite series up to the desired number of terms
    fun estimateEquationOfCenter(eccentricity: Double, meanAnomaly: Double): Double {
        return normalizeAngle((2 * eccentricity * sinDegrees(meanAnomaly)).toDegrees())
    }

    fun estimateEccentricAnomaly(
        eccentricity: Double,
        meanAnomaly: Double,
        toleranceDegrees: Double = 1e-8
    ): Double {
        val toleranceRadians = toleranceDegrees.toRadians()
        val meanAnomalyRadians = meanAnomaly.toRadians()

        return Optimization.newtonRaphsonIteration(meanAnomalyRadians, tolerance = toleranceRadians) {
            it - (it - eccentricity * sin(it) - meanAnomalyRadians) / (1 - eccentricity * cos(it))
        }.toDegrees()
    }

}