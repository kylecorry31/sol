package com.kylecorry.trailsensecore.science.astronomy.locators

import com.kylecorry.trailsensecore.science.astronomy.meteors.MeteorShower
import com.kylecorry.trailsensecore.science.astronomy.units.EquatorialCoordinate
import com.kylecorry.trailsensecore.science.astronomy.units.UniversalTime
import com.kylecorry.trailsensecore.time.TimeUtils

internal class MeteorShowerLocator(private val shower: MeteorShower) : ICelestialLocator {
    override fun getCoordinates(ut: UniversalTime): EquatorialCoordinate {
        return getRadiant()
    }

    private fun getRadiant(): EquatorialCoordinate {
        return when (shower) {
            MeteorShower.Quadrantids -> EquatorialCoordinate(49.7, TimeUtils.timeToAngle(15, 20, 0))
            MeteorShower.Lyrids -> EquatorialCoordinate(33.3, TimeUtils.timeToAngle(18, 10, 0))
            MeteorShower.EtaAquariids -> EquatorialCoordinate(
                -1.1,
                TimeUtils.timeToAngle(22, 30, 0)
            )
            MeteorShower.DeltaAquariids -> EquatorialCoordinate(
                -16.4,
                TimeUtils.timeToAngle(22, 42, 0)
            )
            MeteorShower.Perseids -> EquatorialCoordinate(58.0, TimeUtils.timeToAngle(3, 13, 0))
            MeteorShower.Orionids -> EquatorialCoordinate(15.6, TimeUtils.timeToAngle(6, 19, 0))
            MeteorShower.Leonids -> EquatorialCoordinate(21.6, TimeUtils.timeToAngle(10, 16, 0))
            MeteorShower.Geminids -> EquatorialCoordinate(32.2, TimeUtils.timeToAngle(7, 36, 0))
            MeteorShower.Ursids -> EquatorialCoordinate(75.3, TimeUtils.timeToAngle(14, 36, 0))
        }
    }

}