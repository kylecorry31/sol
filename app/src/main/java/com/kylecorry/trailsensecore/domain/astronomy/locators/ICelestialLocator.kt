package com.kylecorry.trailsensecore.domain.astronomy.locators

import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.domain.astronomy.units.EquatorialCoordinate
import com.kylecorry.trailsensecore.domain.astronomy.units.UniversalTime

interface ICelestialLocator {
    fun getCoordinates(ut: UniversalTime): EquatorialCoordinate

    fun getDistance(ut: UniversalTime): Distance

    fun getAngularDiameter(ut: UniversalTime): Double

    fun getMeanAnomaly(ut: UniversalTime): Double

    fun getTrueAnomaly(ut: UniversalTime): Double
}