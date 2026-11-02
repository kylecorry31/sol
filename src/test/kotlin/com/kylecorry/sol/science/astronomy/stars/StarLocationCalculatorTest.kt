package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.science.astronomy.Astronomy
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

class StarLocationCalculatorTest {

    private val random = Random(12)

    @Test
    fun getLocationFromAltitudesConverges() {
        val errors = (0 until 20).map {
            val location = randomLocation()
            val time = randomTime()
            val readings = visibleStars(location, time, 2.5f).filter { it.second.altitude > 15 }.shuffled(random).take(5)
                .map { (star, position) -> StarReading(star, position.altitude, null, time) }
            val guess = location.plus(800_000.0, Bearing.from(random.nextDouble(0.0, 360.0).toFloat()))
            Astronomy.getLocationFromStars(readings, guess)!!.distanceTo(location)
        }.sorted()
        assertTrue(errors.last() < 100f, "Worst error ${errors.last()} m")
    }

    @Test
    fun getLocationFromNoisyAltitudes() {
        val errors = (0 until 20).map {
            val location = randomLocation()
            val time = randomTime()
            val readings = visibleStars(location, time, 2.5f).filter { it.second.altitude > 15 }.shuffled(random).take(6)
                .map { (star, position) -> StarReading(star, position.altitude + gaussian(0.05), null, time) }
            val guess = location.plus(800_000.0, Bearing.from(random.nextDouble(0.0, 360.0).toFloat()))
            Astronomy.getLocationFromStars(readings, guess)!!.distanceTo(location) / 1000f
        }.sorted()
        assertTrue(errors[errors.size / 2] < 10f, "Median error ${errors[errors.size / 2]} km")
        assertTrue(errors.last() < 50f, "Worst error ${errors.last()} km")
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
