package com.kylecorry.trailsensecore.astronomy

import com.kylecorry.trailsensecore.astronomy.units.EquatorialCoordinate
import com.kylecorry.trailsensecore.domain.time.TimeUtils
import java.time.ZonedDateTime

enum class MeteorShower(val solarLongitude: Float, val radiant: EquatorialCoordinate, val rate: Int) {
    Quadrantids(283.3f, EquatorialCoordinate(49.7, TimeUtils.timeToAngle(15, 20, 0)), 120),
    Lyrids(32.5f, EquatorialCoordinate(33.3, TimeUtils.timeToAngle(18, 10, 0)), 18),
    EtaAquariids(46.2f, EquatorialCoordinate(-1.1, TimeUtils.timeToAngle(22, 30, 0)), 60),
    DeltaAquariids(126.9f, EquatorialCoordinate(-16.4, TimeUtils.timeToAngle(22, 42, 0)), 20),
    Perseids(140.0f, EquatorialCoordinate(58.0, TimeUtils.timeToAngle(3, 13, 0)), 100),
    Orionids(207.5f, EquatorialCoordinate(15.6, TimeUtils.timeToAngle(6, 19, 0)), 23),
    Leonids(236.0f, EquatorialCoordinate(21.6, TimeUtils.timeToAngle(10, 16, 0)), 15),
    Geminids(262.5f, EquatorialCoordinate(32.2, TimeUtils.timeToAngle(7, 36, 0)), 120),
    Ursids(270.5f, EquatorialCoordinate(75.3, TimeUtils.timeToAngle(14, 36, 0)), 10)
}

data class MeteorShowerPeak(val shower: MeteorShower, val peak: ZonedDateTime)