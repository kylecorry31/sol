package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.science.astronomy.stars.StarAltitudeReading
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance

interface ICelestialNavigationService {
    fun getZenithDistance(altitude: Float): Distance
    fun getLocationFromStars(starReadings: List<StarAltitudeReading>): Coordinate?
}