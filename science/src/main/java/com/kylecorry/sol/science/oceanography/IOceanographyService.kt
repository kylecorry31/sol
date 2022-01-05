package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Pressure
import java.time.LocalDate
import java.time.ZonedDateTime

interface IOceanographyService {
    fun getTidalRange(time: ZonedDateTime): TidalRange

    fun getTideType(
        referenceHighTide: ZonedDateTime,
        frequency: TideFrequency,
        now: ZonedDateTime = ZonedDateTime.now()
    ): TideType

    fun getNextTide(
        referenceHighTide: ZonedDateTime,
        frequency: TideFrequency,
        now: ZonedDateTime = ZonedDateTime.now()
    ): Tide?

    fun getTides(
        referenceHighTide: ZonedDateTime,
        frequency: TideFrequency,
        date: LocalDate = LocalDate.now()
    ): List<Tide>

    fun getDepth(
        pressure: Pressure,
        seaLevelPressure: Pressure,
        isSaltWater: Boolean = true
    ): Distance

    /**
     * Get the water level at a given time
     * @param time the time to get the water level
     * @param harmonics the harmonics, referenced to GMT and MSL
     * @param isLocal determines if the harmonics are in local time
     * @param referenceTime the reference time from which the harmonics are calculated from, defaults to the first of the year that time is in
     * @return the water level, in the same units as the harmonic amplitude
     */
    fun getWaterLevel(
        time: ZonedDateTime,
        harmonics: List<TidalHarmonic>,
        isLocal: Boolean = false,
        referenceTime: ZonedDateTime? = null
    ): Float
}