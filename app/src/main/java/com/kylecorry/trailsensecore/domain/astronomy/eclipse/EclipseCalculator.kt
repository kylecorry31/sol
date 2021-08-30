package com.kylecorry.trailsensecore.domain.astronomy.eclipse

import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.InstantRange
import java.time.Instant

interface EclipseCalculator {
    fun getNextEclipse(after: Instant, location: Coordinate): Eclipse?
}