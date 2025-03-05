package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.science.astronomy.stars.StarReading
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance

interface ICelestialNavigationService {
    fun getZenithDistance(altitude: Float): Distance
    fun getLocationFromStars(
        starReadings: List<StarReading>,
        approximateLocation: Coordinate? = null
    ): Coordinate?
}