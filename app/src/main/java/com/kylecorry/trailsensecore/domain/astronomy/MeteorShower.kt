package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.astronomy.units.EquatorialCoordinate
import java.time.ZonedDateTime

enum class MeteorShower(val solarLongitude: Float, val radiant: EquatorialCoordinate, val rate: Int) {
    Quadrantids(283.3f, EquatorialCoordinate(49.7, Astro.timeToAngle(15, 20, 0)), 120),
    Lyrids(32.5f, EquatorialCoordinate(33.3, Astro.timeToAngle(18, 10, 0)), 18),
    EtaAquariids(46.2f, EquatorialCoordinate(-1.1, Astro.timeToAngle(22, 30, 0)), 60),
    DeltaAquariids(126.9f, EquatorialCoordinate(-16.4, Astro.timeToAngle(22, 42, 0)), 20),
    Perseids(140.0f, EquatorialCoordinate(58.0, Astro.timeToAngle(3, 13, 0)), 100),
    Orionids(207.5f, EquatorialCoordinate(15.6, Astro.timeToAngle(6, 19, 0)), 23),
    Leonids(236.0f, EquatorialCoordinate(21.6, Astro.timeToAngle(10, 16, 0)), 15),
    Geminids(262.5f, EquatorialCoordinate(32.2, Astro.timeToAngle(7, 36, 0)), 120),
    Ursids(270.5f, EquatorialCoordinate(75.3, Astro.timeToAngle(14, 36, 0)), 10)
}

data class MeteorShowerPeak(val shower: MeteorShower, val peak: ZonedDateTime)