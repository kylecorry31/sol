package com.kylecorry.sol.science.astronomy.eclipse

import com.kylecorry.sol.science.astronomy.eclipse.lunar.PartialLunarEclipseCalculator
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.tests.assertDate
import org.junit.jupiter.api.Test
import java.time.*

class PartialLunarEclipseCalculatorTest {

    @Test
    fun canGetNextEclipse() {
        val calculator = PartialLunarEclipseCalculator()
        val date = ZonedDateTime.of(LocalDateTime.of(2021, 8, 29, 0, 0), ZoneId.of("UTC"))
        val location = Coordinate(42.0, -70.0)

        val actual = calculator.getNextEclipse(date.toInstant(), location)

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2021, 11, 19, 7, 18), ZoneId.of("UTC")),
            actual!!.start.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2021, 11, 19, 10, 47), ZoneId.of("UTC")),
            actual.end.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )
    }

    @Test
    fun canGetNextEclipseOnDateOfEclipse() {
        val calculator = PartialLunarEclipseCalculator()
        val date = ZonedDateTime.of(LocalDateTime.of(2021, 11, 19, 12, 0), ZoneId.of("UTC"))
        val location = Coordinate(42.0, -70.0)

        val actual = calculator.getNextEclipse(date.toInstant(), location)

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2022, Month.MAY, 16, 2, 27), ZoneId.of("UTC")),
            actual!!.start.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2022, Month.MAY, 16, 5, 55), ZoneId.of("UTC")),
            actual.end.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )
    }

    @Test
    fun skipsEclipseIfNotVisible() {
        val calculator = PartialLunarEclipseCalculator()
        val date = ZonedDateTime.of(LocalDateTime.of(2021, 11, 1, 0, 0), ZoneId.of("UTC"))
        val location = Coordinate(6.0, 22.0)

        val actual = calculator.getNextEclipse(date.toInstant(), location)

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2022, Month.MAY, 16, 2, 27), ZoneId.of("UTC")),
            actual!!.start.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )

        assertDate(
            ZonedDateTime.of(LocalDateTime.of(2022, Month.MAY, 16, 5, 55), ZoneId.of("UTC")),
            actual.end.atZone(ZoneId.of("UTC")),
            Duration.ofMinutes(2)
        )
    }


}