package com.kylecorry.trailsensecore.domain.astronomy.locators

import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.trailsensecore.domain.astronomy.units.EquatorialCoordinate
import com.kylecorry.trailsensecore.domain.astronomy.units.UniversalTime

class MoonLocator: ICelestialLocator {
    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        TODO("Not yet implemented")
    }

    override fun getDistance(ut: UniversalTime): Distance {
        TODO("Not yet implemented")
    }

    override fun getAngularDiameter(ut: UniversalTime): Double {
        TODO("Not yet implemented")
    }

    override fun getMeanAnomaly(ut: UniversalTime): Double {
        TODO("Not yet implemented")
    }

    override fun getTrueAnomaly(ut: UniversalTime): Double {
        TODO("Not yet implemented")
    }
}