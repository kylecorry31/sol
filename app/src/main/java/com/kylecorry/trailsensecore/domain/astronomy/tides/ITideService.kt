package com.kylecorry.trailsensecore.domain.astronomy.tides

import java.time.ZonedDateTime

interface ITideService {

    fun getTidalRange(time: ZonedDateTime): Tide

}