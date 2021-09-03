package com.kylecorry.trailsensecore.domain.astronomy.locators

import com.kylecorry.trailsensecore.domain.astronomy.units.EquatorialCoordinate
import com.kylecorry.trailsensecore.domain.astronomy.units.UniversalTime

interface ICelestialLocator {
    fun getCoordinates(ut: UniversalTime): EquatorialCoordinate
}