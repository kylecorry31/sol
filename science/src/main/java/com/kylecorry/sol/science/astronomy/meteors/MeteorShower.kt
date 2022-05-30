package com.kylecorry.sol.science.astronomy.meteors

import java.time.ZonedDateTime

enum class MeteorShower(val solarLongitude: Float, val rate: Int) {
    Quadrantids(283.3f, 120),
    Lyrids(32.5f, 18),
    EtaAquariids(46.2f, 60),
    TauHerculids(69.72f, 10000), // TODO: Once the shower happens update the rate
    DeltaAquariids(126.9f, 20),
    Perseids(140.0f, 100),
    Orionids(207.5f, 23),
    Leonids(236.0f, 15),
    Geminids(262.5f, 120),
    Ursids(270.5f, 10)
}

data class MeteorShowerPeak(val shower: MeteorShower, val peak: ZonedDateTime)