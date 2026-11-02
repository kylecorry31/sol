package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.science.astronomy.Astronomy
import com.kylecorry.sol.science.astronomy.units.CelestialObservation
import com.kylecorry.sol.units.Bearing
import com.kylecorry.sol.units.Coordinate
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

class PlateSolverTest {

    private val random = Random(12)

    @Test
    fun plateSolveWithCompassError() {
        var correct = 0
        var identified = 0
        var total = 0
        repeat(10) {
            val location = randomLocation()
            val time = randomTime()
            val stars = visibleStars(location, time, 3f).filter { it.second.altitude > 25 }.take(12)
            val compassError = random.nextDouble(-5.0, 5.0).toFloat()
            val readings = stars.map { (_, position) ->
                CelestialObservation(
                    Bearing.from(position.azimuth.value + compassError + gaussian(0.2)),
                    position.altitude + gaussian(0.2)
                )
            }
            val guess = location.plus(500_000.0, Bearing.from(random.nextDouble(0.0, 360.0).toFloat()))
            val detected = Astronomy.plateSolve(readings, time, guess)
            total += readings.size
            identified += detected.size
            correct += detected.count { d -> stars[readings.indexOf(d.reading)].first == d.star }
        }
        assertTrue(identified > total * 0.8, "Identified $identified of $total")
        assertTrue(correct == identified, "Correct $correct of $identified")
    }

    private fun visibleStars(location: Coordinate, time: ZonedDateTime, maxMagnitude: Float) =
        STAR_CATALOG.filter { it.magnitude <= maxMagnitude }
            .map { it to Astronomy.getStarPosition(it, time, location, true) }
            .filter { it.second.altitude > 0 }

    private fun randomLocation(): Coordinate {
        return Coordinate(random.nextDouble(-55.0, 60.0), random.nextDouble(-180.0, 180.0))
    }

    private fun randomTime(): ZonedDateTime {
        val start = Instant.parse("2026-01-01T00:00:00Z")
        return ZonedDateTime.ofInstant(start.plusSeconds(random.nextLong(0, 365L * 24 * 3600)), ZoneOffset.UTC)
    }

    private fun gaussian(sigma: Double): Float {
        return (sigma * sqrt(-2 * ln(random.nextDouble(1e-12, 1.0))) * cos(2 * Math.PI * random.nextDouble())).toFloat()
    }
}
