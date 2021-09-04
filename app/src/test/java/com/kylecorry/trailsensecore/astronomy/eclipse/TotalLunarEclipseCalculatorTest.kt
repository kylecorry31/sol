package com.kylecorry.trailsensecore.astronomy.eclipse

import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.astronomy.eclipse.TotalLunarEclipseCalculator
import com.kylecorry.trailsensecore.tests.assertDate
import org.junit.jupiter.api.Test
import java.time.*

class TotalLunarEclipseCalculatorTest {

    @Test
    fun canGetNextEclipse() {
        val calculator = TotalLunarEclipseCalculator()
        val date = ZonedDateTime.of(LocalDateTime.of(2021, 8, 29, 0, 0), ZoneId.of("UTC"))
        val location = Coordinate(42.0, -70.0)

        val actual = calculator.getNextEclipse(date.toInstant(), location)

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2022, 5, 16, 3, 29), ZoneId.of("UTC")),
            actual!!.start.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2022, 5, 16, 4, 53), ZoneId.of("UTC")),
            actual.end.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )
    }

    @Test
    fun canGetNextEclipseOnDateOfEclipse() {
        val calculator = TotalLunarEclipseCalculator()
        val date = ZonedDateTime.of(LocalDateTime.of(2022, 5, 16, 5, 0), ZoneId.of("UTC"))
        val location = Coordinate(42.0, -70.0)

        val actual = calculator.getNextEclipse(date.toInstant(), location)

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2022, Month.NOVEMBER, 8, 10, 16), ZoneId.of("UTC")),
            actual!!.start.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2022, Month.NOVEMBER, 8, 11, 41), ZoneId.of("UTC")),
            actual.end.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )
    }

    @Test
    fun skipsEclipseIfNotVisible() {
        val calculator = TotalLunarEclipseCalculator()
        val date = ZonedDateTime.of(LocalDateTime.of(2022, 5, 15, 0, 0), ZoneId.of("UTC"))
        val location = Coordinate(46.0, -150.0)

        val actual = calculator.getNextEclipse(date.toInstant(), location)

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2022, Month.NOVEMBER, 8, 10, 16), ZoneId.of("UTC")),
            actual!!.start.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2022, Month.NOVEMBER, 8, 11, 41), ZoneId.of("UTC")),
            actual.end.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )
    }


}