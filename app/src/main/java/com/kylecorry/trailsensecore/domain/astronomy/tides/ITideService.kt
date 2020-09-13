package com.kylecorry.trailsensecore.domain.astronomy.tides

import com.kylecorry.trailsensecore.domain.astronomy.tides.Tide
import java.time.ZonedDateTime

interface ITideService {

    fun getTidalRange(time: ZonedDateTime): Tide

}