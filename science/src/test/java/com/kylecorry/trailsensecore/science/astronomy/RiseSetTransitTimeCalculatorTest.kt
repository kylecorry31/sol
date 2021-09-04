package com.kylecorry.trailsensecore.science.astronomy

import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.science.astronomy.locators.Sun
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class RiseSetTransitTimeCalculatorTest {

    @Test
    fun calculate() {
        val locator = Sun()
        val date = ZonedDateTime.now().plusDays(200)
        val location = Coordinate(42.0, -72.0)

        val rst = RiseSetTransitTimeCalculator().calculate(
            locator,
            date,
            location,
            -0.8333
        )

        println(rst)
    }
}