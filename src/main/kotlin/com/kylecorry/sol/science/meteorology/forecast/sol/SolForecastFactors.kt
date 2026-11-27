package com.kylecorry.sol.science.meteorology.forecast.sol

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.science.meteorology.ForecastClimate
import com.kylecorry.sol.science.meteorology.PressureSystem
import com.kylecorry.sol.science.meteorology.PressureTendency
import com.kylecorry.sol.science.meteorology.WeatherFront
import com.kylecorry.sol.science.meteorology.clouds.CloudGenus
import com.kylecorry.sol.units.DistanceUnits
import com.kylecorry.sol.units.Reading
import com.kylecorry.sol.units.Temperature

/**
 * Pressures are in hPa with the atmospheric tide already removed. The changes are null when
 * there is too little history, and [change12h] and [change24h] fall back to the next shorter window.
 *
 * @param sensitivityScale 1 at the default sensitivity, larger values require larger pressure changes
 * @param stormDrop3h the 3 hour pressure drop (hPa, positive) considered a storm
 * @param climate the normal climate for the forecast month, null when unknown
 */
internal data class SolForecastFactors(
    val pressureTendency: PressureTendency,
    val pressureSystem: PressureSystem?,
    val doCloudsIndicateWeatherFront: Boolean,
    val weatherFrontFromCloudsOnly: WeatherFront?,
    val weatherFront: WeatherFront?,
    val sensitivityScale: Float,
    val stormDrop3h: Float,
    val pressure: Float? = null,
    val change3h: Float? = null,
    val change12h: Float? = null,
    val change24h: Float? = null,
    val range48h: Float? = null,
    val temperatureRangeDaily: Range<Temperature>? = null,
    val cloudReadingCurrent: Reading<CloudGenus?>? = null,
    val climate: ForecastClimate? = null
) {
    val monthlyPrecipitationMm: Float?
        get() = climate?.monthlyPrecipitation?.convertTo(DistanceUnits.Millimeters)?.value
}
