package com.kylecorry.trailsensecore.astronomy.eclipse

import com.kylecorry.andromeda.core.units.Coordinate
import java.time.Instant

interface EclipseCalculator {
    fun getNextEclipse(after: Instant, location: Coordinate): Eclipse?
}