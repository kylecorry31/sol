package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.time.ZonedDateTime

data class StarReading(val star: Star, val altitude: Float, val azimuth: Float?, val time: ZonedDateTime)
