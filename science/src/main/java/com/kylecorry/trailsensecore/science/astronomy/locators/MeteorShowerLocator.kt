package com.kylecorry.trailsensecore.science.astronomy.locators

import com.kylecorry.trailsensecore.science.astronomy.MeteorShower
import com.kylecorry.trailsensecore.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.trailsensecore.science.astronomy.units.UniversalTime

class MeteorShowerLocator(private val shower: MeteorShower) : ICelestialLocator {
    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        return shower.radiant
    }
}