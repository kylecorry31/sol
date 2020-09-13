package com.kylecorry.trailsensecore.domain.astronomy.tides

import com.kylecorry.trailsensecore.domain.astronomy.Astro
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonTruePhase
import com.kylecorry.trailsensecore.domain.astronomy.tides.ITideService
import com.kylecorry.trailsensecore.domain.astronomy.tides.Tide
import java.time.ZonedDateTime

class TideService : ITideService {

    override fun getTidalRange(time: ZonedDateTime): Tide {
        for (i in 0..3){
            val phase = Astro.getMoonPhase(time.minusDays(i.toLong()))

            when(phase.phase){
                MoonTruePhase.New, MoonTruePhase.Full -> {
                    return Tide.Spring
                }
                MoonTruePhase.FirstQuarter, MoonTruePhase.ThirdQuarter -> {
                    return Tide.Neap
                }
                else -> {
                    // Do nothing
                }
            }
        }

        return Tide.Normal
    }
}