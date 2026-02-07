package com.kylecorry.sol.science.geology

import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.analysis.Trigonometry
import com.kylecorry.sol.math.arithmetic.Arithmetic
import com.kylecorry.sol.math.arithmetic.Arithmetic.wrap
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import kotlin.math.absoluteValue
import kotlin.math.atan

object Geology {

    private val riskClassifier = AvalancheRiskClassifier()

    /**
     * Determine the avalanche risk of a slope
     * @param inclination The inclination angle (degrees)
     * @return The avalanche risk
     */
    fun getAvalancheRisk(inclination: Float): AvalancheRisk {
        return riskClassifier.classify(inclination)
    }

    /**
     * Determines the grade (percent)
     * @param inclination The inclination angle (degrees)
     * @return The slope grade as a percentage
     */
    fun getSlopeGrade(inclination: Float): Float {
        if (inclination == 90f) {
            return Float.POSITIVE_INFINITY
        } else if (inclination == -90f) {
            return Float.NEGATIVE_INFINITY
        }

        return Trigonometry.tanDegrees(inclination) * 100
    }

    fun getInclinationFromSlopeGrade(grade: Float): Float {
        return atan(grade / 100f).toDegrees()
    }

    fun getInclination(distance: Distance, elevationChange: Distance): Float {
        return getInclinationFromSlopeGrade(getSlopeGrade(distance, elevationChange))
    }

    /**
     * Estimates the height of an object
     * @param distance The distance to the object
     * @param bottomInclination The inclination angle to the bottom (degrees)
     * @param topInclination The inclination angle to the top (degrees)
     * @return The estimated height of the object
     */
    fun getHeightFromInclination(
        distance: Distance,
        bottomInclination: Float,
        topInclination: Float
    ): Distance {
        val up = getSlopeGrade(topInclination) / 100f
        val down = getSlopeGrade(bottomInclination) / 100f

        if (up.isInfinite() || down.isInfinite()) {
            return Distance.from(Float.POSITIVE_INFINITY, distance.units)
        }

        return Distance.from(((up - down) * distance.value).absoluteValue, distance.units)
    }

    /**
     * Estimates the distance to an object
     * @param height The height to the object
     * @param bottomInclination The inclination angle to the bottom (degrees)
     * @param topInclination The inclination angle to the top (degrees)
     * @return The estimated distance to the object
     */
    fun getDistanceFromInclination(
        height: Distance,
        bottomInclination: Float,
        topInclination: Float
    ): Distance {
        val up = getSlopeGrade(topInclination) / 100f
        val down = getSlopeGrade(bottomInclination) / 100f

        if (up.isInfinite() || down.isInfinite()) {
            return Distance.from(0f, height.units)
        }
        return Distance.from((height.value / (up - down)).absoluteValue, height.units)
    }

    /**
     * Calculates the inclination from a unit angle
     * @param angle The angle, where 0 is the horizon (front), 90 is the sky (above), 180 is the horizon (behind), and 270 is the ground (below)
     */
    fun getInclination(angle: Float): Float {
        return when (val wrappedAngle = wrap(angle, 0f, 360f)) {
            in 90f..270f -> 180f - wrappedAngle
            in 270f..360f -> wrappedAngle - 360f
            else -> wrappedAngle
        }
    }

    /**
     * Determines the grade (percent)
     * @param horizontal The horizontal distance
     * @param vertical The vertical distance
     * @return The slope grade as a percentage
     */
    fun getSlopeGrade(horizontal: Distance, vertical: Distance): Float {
        val y = vertical.meters().value
        val x = horizontal.meters().value

        if (Arithmetic.isZero(x) && y > 0f) {
            return Float.POSITIVE_INFINITY
        }

        if (Arithmetic.isZero(x) && y < 0f) {
            return Float.NEGATIVE_INFINITY
        }

        if (Arithmetic.isZero(x)) {
            return 0f
        }

        return y / x * 100
    }

    /**
     * Determines the grade (percent)
     * @param start The starting coordinate
     * @param startElevation The starting elevation
     * @param end The ending coordinate
     * @param endElevation The ending elevation
     * @return The slope grade as a percentage
     */
    fun getSlopeGrade(
        start: Coordinate,
        startElevation: Distance,
        end: Coordinate,
        endElevation: Distance
    ): Float {
        return getSlopeGrade(
            Distance.meters(start.distanceTo(end)),
            Distance.meters(endElevation.meters().value - startElevation.meters().value)
        )
    }

}