package com.kylecorry.trailsensecore.domain.astronomy.sun

import com.kylecorry.trailsensecore.domain.Coordinate
import java.time.LocalDate

internal interface ISunTimesCalculator {

    fun calculate(coordinate: Coordinate, date: LocalDate): SunTimes

}