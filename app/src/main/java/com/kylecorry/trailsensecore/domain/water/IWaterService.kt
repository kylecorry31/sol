package com.kylecorry.trailsensecore.domain.water

import com.kylecorry.trailsensecore.domain.units.Distance
import java.time.Duration

interface IWaterService {
    fun getPurificationTime(altitude: Distance?): Duration
}