package com.kylecorry.trailsensecore.science.oceanography

import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.andromeda.core.units.Pressure
import java.time.LocalDate
import java.time.ZonedDateTime

interface IOceanographyService {
    fun getTidalRange(time: ZonedDateTime): TidalRange

    fun getTideType(referenceHighTide: ZonedDateTime, now: ZonedDateTime = ZonedDateTime.now()): TideType

    fun getNextTide(referenceHighTide: ZonedDateTime, now: ZonedDateTime = ZonedDateTime.now()): Tide?

    fun getTides(referenceHighTide: ZonedDateTime, date: LocalDate = LocalDate.now()): List<Tide>

    fun getDepth(pressure: Pressure, seaLevelPressure: Pressure, isSaltWater: Boolean = true): Distance
}