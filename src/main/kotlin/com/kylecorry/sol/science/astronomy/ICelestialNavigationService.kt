package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.science.astronomy.stars.StarReading
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Quantity

interface ICelestialNavigationService {
    fun getZenithDistance(altitude: Float): Quantity<Distance>
    fun getLocationFromStars(
        starReadings: List<StarReading>,
        approximateLocation: Coordinate? = null
    ): Coordinate?
}