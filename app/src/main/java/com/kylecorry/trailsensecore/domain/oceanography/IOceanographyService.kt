package com.kylecorry.trailsensecore.domain.oceanography

import java.time.LocalDate
import java.time.ZonedDateTime

interface IOceanographyService {
    fun getTidalRange(time: ZonedDateTime): TidalRange

    fun getTideType(referenceHighTide: ZonedDateTime, now: ZonedDateTime = ZonedDateTime.now()): TideType

    fun getNextTide(referenceHighTide: ZonedDateTime, now: ZonedDateTime = ZonedDateTime.now()): Tide?

    fun getTides(referenceHighTide: ZonedDateTime, date: LocalDate = LocalDate.now()): List<Tide>
}