package com.kylecorry.sol.science.astronomy.meteors

import java.time.ZonedDateTime

enum class MeteorShower(val solarLongitude: Float, val rate: Int) {
    Quadrantids(283.3f, 120),
    Lyrids(32.4f, 18),
    EtaAquariids(46.2f, 60),
    DeltaAquariids(127.6f, 20),
    Perseids(140.0f, 100),
    Orionids(207.5f, 23),
    Leonids(236.0f, 15),
    Geminids(262.0f, 120),
    Ursids(270.5f, 10)
}

data class MeteorShowerPeak(
    val shower: MeteorShower,
    val start: ZonedDateTime,
    val peak: ZonedDateTime,
    val end: ZonedDateTime
)