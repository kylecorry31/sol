package com.kylecorry.trailsensecore.domain.astronomy.moon

import com.kylecorry.trailsensecore.domain.Coordinate
import java.time.LocalDate

internal interface IMoonTimesCalculator {

    fun calculate(location: Coordinate, date: LocalDate = LocalDate.now()): MoonTimes

}