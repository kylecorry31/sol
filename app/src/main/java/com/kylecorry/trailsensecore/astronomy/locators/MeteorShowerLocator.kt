package com.kylecorry.trailsensecore.astronomy.locators

import com.kylecorry.trailsensecore.astronomy.MeteorShower
import com.kylecorry.trailsensecore.astronomy.units.EquatorialCoordinate
import com.kylecorry.trailsensecore.astronomy.units.UniversalTime

class MeteorShowerLocator(private val shower: MeteorShower) : ICelestialLocator {
    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        return shower.radiant
    }
}