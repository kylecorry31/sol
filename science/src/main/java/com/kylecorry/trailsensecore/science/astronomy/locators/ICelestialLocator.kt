package com.kylecorry.trailsensecore.science.astronomy.locators

import com.kylecorry.trailsensecore.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.trailsensecore.science.astronomy.units.UniversalTime

interface ICelestialLocator {
    fun getCoordinates(ut: UniversalTime): EquatorialCoordinate
}