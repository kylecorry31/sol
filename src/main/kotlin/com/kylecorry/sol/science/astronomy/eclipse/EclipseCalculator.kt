package com.kylecorry.sol.science.astronomy.eclipse

import com.kylecorry.sol.units.Coordinate
import java.time.Instant

internal interface EclipseCalculator {
    fun getNextEclipse(after: Instant, location: Coordinate): Eclipse?

    fun getMagnitude(time: Instant, location: Coordinate): Float?

    fun getObscuration(time: Instant, location: Coordinate): Float?
}