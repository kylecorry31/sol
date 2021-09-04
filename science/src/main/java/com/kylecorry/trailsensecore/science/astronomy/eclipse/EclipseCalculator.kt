package com.kylecorry.trailsensecore.science.astronomy.eclipse

import com.kylecorry.andromeda.core.units.Coordinate
import java.time.Instant

internal interface EclipseCalculator {
    fun getNextEclipse(after: Instant, location: Coordinate): Eclipse?
}