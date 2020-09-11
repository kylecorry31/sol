package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.Coordinate
import com.kylecorry.trailsensecore.domain.astronomy.moon.Tide
import java.time.ZonedDateTime

class TideService : ITideService {
    private val oldAstronomyService = OldAstronomyService()

    override fun getTidalRange(time: ZonedDateTime): Tide {
        return oldAstronomyService.getTides(time.toLocalDate())
    }
}