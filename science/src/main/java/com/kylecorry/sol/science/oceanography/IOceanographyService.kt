package com.kylecorry.sol.science.oceanography

import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Pressure
import java.time.LocalDate
import java.time.ZonedDateTime

interface IOceanographyService {

    fun getDepth(
        pressure: Pressure,
        seaLevelPressure: Pressure,
        isSaltWater: Boolean = true
    ): Distance

    fun getTidalRange(time: ZonedDateTime): TidalRange

    /**
     * Gets the tides for the day
     */
    fun getTides(
        harmonics: List<TidalHarmonic>,
        date: ZonedDateTime = ZonedDateTime.now()
    ): List<Tide>

    /**
     * Estimates the harmonics for a tide
     * @param highTide the reference high tide, preferably on a New or Full moon
     * @param frequency the frequency of the tide
     * @param amplitude the amplitude of the tide
     * @return the estimated tidal harmonics
     */
    fun estimateHarmonics(
        highTide: ZonedDateTime,
        frequency: TideFrequency,
        amplitude: Float = 1f
    ): List<TidalHarmonic>

    /**
     * Get the water level at a given time
     * @param harmonics the harmonics, referenced to GMT and MSL
     * @param time the time to get the water level
     * @return the water level, in the same units as the harmonic amplitude
     */
    fun getWaterLevel(
        harmonics: List<TidalHarmonic>,
        time: ZonedDateTime
    ): Float
}