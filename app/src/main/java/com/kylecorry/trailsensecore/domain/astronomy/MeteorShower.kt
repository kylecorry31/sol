package com.kylecorry.trailsensecore.domain.astronomy

import java.time.LocalDateTime

enum class MeteorShower(val peak: LocalDateTime, val rate: Int) {
    Quadrantids(
        LocalDateTime.of(2021, 1, 3, 5, 0),
        120
    ),
    Lyrids(
        LocalDateTime.of(2021, 4, 22, 4, 0),
        18
    ),
    EtaAquariids(
        LocalDateTime.of(2021, 5, 5, 4, 0),
        60
    ),
    DeltaAquariids(
        LocalDateTime.of(2021, 7, 30, 3, 0),
        20
    ),
    Perseids(
        LocalDateTime.of(2021, 8, 12, 4, 0),
        100
    ),
    Orionids(
        LocalDateTime.of(2021, 10, 21, 5, 0),
        23
    ),
    Leonids(
        LocalDateTime.of(2021, 11, 18, 5, 0),
        15
    ),
    Geminids(
        LocalDateTime.of(2021, 12, 14, 1, 0),
        120
    ),
    Ursids(
        LocalDateTime.of(2021, 12, 22, 5, 0),
        10
    ),
}


// Get meteor showers for today
// Determine when radiant is above horizon (if already above horizon, go backwards to when it rises)
// Visibility = Radiant rise to dawn
// Direction = Get bearing to radiant at the halfway point and convert to compass direction