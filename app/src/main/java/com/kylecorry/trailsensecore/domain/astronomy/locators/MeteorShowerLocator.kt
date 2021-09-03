package com.kylecorry.trailsensecore.domain.astronomy.locators

import com.kylecorry.trailsensecore.domain.astronomy.MeteorShower
import com.kylecorry.trailsensecore.domain.astronomy.units.EquatorialCoordinate
import com.kylecorry.trailsensecore.domain.astronomy.units.UniversalTime

class MeteorShowerLocator(private val shower: MeteorShower) : ICelestialLocator {
    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        return EquatorialCoordinate(shower.radiant.declination, shower.radiant.rightAscension)
    }
}