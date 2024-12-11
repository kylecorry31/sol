package com.kylecorry.sol.science.astronomy.stars

import java.time.ZonedDateTime

data class StarReading(val star: Star, val altitude: Float, val azimuth: Float?, val time: ZonedDateTime)
