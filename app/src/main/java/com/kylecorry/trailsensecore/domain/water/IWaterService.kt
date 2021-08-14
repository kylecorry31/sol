package com.kylecorry.trailsensecore.domain.water

import com.kylecorry.andromeda.core.units.Distance
import java.time.Duration

interface IWaterService {
    fun getPurificationTime(altitude: Distance?): Duration
}