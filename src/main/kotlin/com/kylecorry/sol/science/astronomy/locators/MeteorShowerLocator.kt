package com.kylecorry.sol.science.astronomy.locators

import com.kylecorry.sol.science.astronomy.meteors.MeteorShower
import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.sol.science.astronomy.units.UniversalTime
import com.kylecorry.sol.science.astronomy.units.timeToAngle
import com.kylecorry.sol.units.Distance

internal class MeteorShowerLocator(private val shower: MeteorShower) : ICelestialLocator {
    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        return when (shower) {
            MeteorShower.Quadrantids -> EquatorialCoordinate(49.7, timeToAngle(15, 20, 0))
            MeteorShower.Lyrids -> EquatorialCoordinate(33.3, timeToAngle(18, 10, 0))
            MeteorShower.EtaAquariids -> EquatorialCoordinate(
                -1.0,
                timeToAngle(22, 30, 0)
            )

            MeteorShower.DeltaAquariids -> EquatorialCoordinate(
                -16.3,
                timeToAngle(22, 42, 0)
            )

            MeteorShower.Perseids -> EquatorialCoordinate(58.0, timeToAngle(3, 13, 0))
            MeteorShower.Orionids -> EquatorialCoordinate(15.6, timeToAngle(6, 21, 0))
            MeteorShower.Leonids -> EquatorialCoordinate(21.6, timeToAngle(10, 17, 0))
            MeteorShower.Geminids -> EquatorialCoordinate(32.3, timeToAngle(7, 24, 0))
            MeteorShower.Ursids -> EquatorialCoordinate(75.3, timeToAngle(14, 36, 0))
        }
    }

    override fun getDistance(ut: UniversalTime): Distance? {
        return null
    }

}