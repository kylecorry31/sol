package com.kylecorry.trailsensecore.astronomy.locators

import com.kylecorry.trailsensecore.astronomy.units.EquatorialCoordinate
import com.kylecorry.trailsensecore.astronomy.units.UniversalTime

interface ICelestialLocator {
    fun getCoordinates(ut: UniversalTime): EquatorialCoordinate
}