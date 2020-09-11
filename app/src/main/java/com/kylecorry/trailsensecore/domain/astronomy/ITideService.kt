package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.astronomy.moon.Tide
import java.time.ZonedDateTime

interface ITideService {

    fun getTidalRange(time: ZonedDateTime): Tide

}