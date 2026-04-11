package com.kylecorry.sol.science.astronomy.meteors

import java.time.ZonedDateTime

enum class MeteorShower(val solarLongitude: Float, val rate: Int, val activeDays: Int) {
    Quadrantids(283.3f, 120, 20),
    Lyrids(32.4f, 18, 10),
    EtaAquariids(46.2f, 50, 40),
    DeltaAquariids(127.6f, 25, 25),
    Perseids(140.0f, 100, 35),
    Orionids(207.5f, 20, 40),
    Leonids(236.0f, 15, 30),
    Geminids(262.0f, 150, 20),
    Ursids(270.5f, 10, 10)
}

data class MeteorShowerPeak(
    val shower: MeteorShower,
    val start: ZonedDateTime,
    val peak: ZonedDateTime,
    val end: ZonedDateTime
)