package com.kylecorry.sol.math

import com.kylecorry.sol.math.SolMath.tanDegrees
import kotlin.math.absoluteValue
import kotlin.math.max

class InclinationService : IInclinationService {
    override fun grade(inclination: Float): Float {
        if (inclination == 90f){
            return Float.POSITIVE_INFINITY
        } else if (inclination == -90f){
            return Float.NEGATIVE_INFINITY
        }

        return tanDegrees(inclination)
    }

    override fun height(
        distance: Float,
        bottomInclination: Float,
        topInclination: Float
    ): Float {
        val up = grade(topInclination)
        val down = grade(bottomInclination)

        if (up.isInfinite() || down.isInfinite()){
            return Float.POSITIVE_INFINITY
        }

        return max(0f, (up - down) * distance)
    }

    override fun distance(
        height: Float,
        bottomInclination: Float,
        topInclination: Float
    ): Float {
        val up = grade(topInclination)
        val down = grade(bottomInclination)

        if (up.isInfinite() || down.isInfinite()){
            return 0f
        }
        return height / (up - down)
    }


}